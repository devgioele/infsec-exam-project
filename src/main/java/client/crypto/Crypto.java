package client.crypto;

import client.server.Server;
import client.util.ClientLogger;
import client.util.UnauthorizedException;
import http.JwtPayload;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import server.crypto.JwtHeader;
import server.util.ServerLogger;
import util.Convert;

import java.io.IOException;
import java.math.BigInteger;
import java.net.http.HttpRequest;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;

import static util.Crypto.*;
import static util.IO.jsonFromFile;
import static util.IO.jsonToFile;


public class Crypto {

	private static Crypto INSTANCE;

	private final String pathConfig;
	private final CryptoConfig config;

	public static Crypto getInstance() {
		if (INSTANCE == null) {
			String pathConfig = Paths.get("client", "crypto.json").toAbsolutePath().toString();
			INSTANCE = new Crypto(pathConfig);
		}
		return INSTANCE;
	}

	public Crypto(String pathConfig) {
		this.pathConfig = pathConfig;
		CryptoConfig config = jsonFromFile(pathConfig, CryptoConfig.class);
		if (config == null) {
			ClientLogger.println("Creating a new crypto config.");
			config = new CryptoConfig(4096, HashingAlgorithm.SHA256);
			saveConfig();
		}
		this.config = config;
	}

	/**
	 * @param key       The RSA key to use for encryption.
	 * @param plaintext The UTF-8 string to encrypt.
	 * @return The encrypted base 64 string.
	 */
	public static String encrypt(RsaKey key, String plaintext) {
		// Interpret bytes of utf-8 string as big integer
		BigInteger bi = new BigInteger(plaintext.getBytes(StandardCharsets.UTF_8));
		// Encrypt bytes
		BigInteger encrypted = key.apply(bi);
		// Convert bytes to base 64 string
		return Convert.toBase64(encrypted);
	}

	/**
	 * @param key        The RSA key to use for decryption.
	 * @param ciphertext The base 64 string decrypt.
	 * @return The decrypted UTF-8 string.
	 * @throws CharacterCodingException When the given ciphertext cannot be decrypted to a valid
	 *                                  UTF-8 string using the given key.
	 */
	public static String decrypt(RsaKey key, String ciphertext) throws CharacterCodingException {
		// Bytes from base 64 string
		BigInteger bi = Convert.fromBase64(ciphertext);
		// Decrypt
		BigInteger decrypted = key.apply(bi);
		// Interpret bytes of big integer as utf-8 string
		ByteBuffer buffer = ByteBuffer.wrap(decrypted.toByteArray());
		CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder();
		return decoder.decode(buffer).toString();
	}

	public Optional<RsaKey> getPrivateKey(String id) {
		return config.getPrivateKey(id);
	}

	private void saveConfig() {
		jsonToFile(config, pathConfig);
	}

	/** Generates an RSA key pair, stores the private key and returns the public key. */
	public RsaKey genKeyPair(String id) {
		RsaKeyPair pair = Rsa.genKeyPair(config.rsaKeySize);
		config.addPrivateKey(id, pair.privateKey);
		saveConfig();
		return pair.publicKey;
	}

	private static String addBearer(String jwt) {
		return bearerPrefix + jwt;
	}

	public static HttpRequest.Builder setJwtHeader(HttpRequest.Builder builder, String jwt) {
		return builder.setHeader(jwtId, addBearer(jwt));
	}

	public static void setJwtCookie(HttpServletResponse response, String jwt) {
		response.addCookie(new Cookie(jwtId, jwt));
	}

	public static void clearJwtCookie(HttpServletResponse response) {
		response.addCookie(new Cookie(jwtId, ""));
	}

	public static String extractJwtCookie(HttpServletRequest request) {
		Cookie[] cookies = request.getCookies();
		Optional<Cookie> cookie = cookies == null ? Optional.empty() :
				Arrays.stream(cookies).filter(c -> c.getName().equals(jwtId)).findAny();
		// Verify existence of cookie
		if (cookie.isEmpty()) {
			ClientLogger.printf("Cookie '%s' not found!%n", jwtId);
			return null;
		}
		return cookie.get().getValue();
	}

	/**
	 * @return The payload of the JWT if it is valid or null otherwise.
	 */
	public static JwtPayload validJwt(String jwt) {
		if(Server.getInstance().isJwtValid(jwt)) {
			String[] parts = jwt.split("\\.");
			// Verify structure
			if (parts.length != 3) {
				ServerLogger.printf("JWT ill structured with a length of %d:%n%s%n", parts.length,
						jwt);
				return null;
			}
			String encodedPayload = parts[1];
			return Convert.gson.fromJson(Convert.fromBase64Url(encodedPayload), JwtPayload.class);
		}
		return null;
	}

	public String sign(String sender, String subject, String body) {
		Optional<RsaKey> privateKey = Crypto.getInstance().getPrivateKey(sender);
		if (privateKey.isEmpty()) {
			ClientLogger.printfErr("Could not find private key for user '%s'%n", sender);
			return null;
		}
		byte[] content = (subject + body).getBytes(StandardCharsets.UTF_8);
		String digest = hash(config.signatureAlgorithm, content);
		return encrypt(privateKey.get(), digest);
	}

	public boolean isSignatureValid(String sender, String subject, String body, String signature, String jwt)
			throws UnauthorizedException, IOException {
		Optional<RsaKey> publicKey = Server.getInstance().getPublicKey(jwt, sender);
		if(publicKey.isEmpty()) {
			ClientLogger.printfErr("Could not find public key for user '%s'%n", sender);
			return false;
		}
		byte[] content = (subject + body).getBytes(StandardCharsets.UTF_8);
		String digest = hash(config.signatureAlgorithm, content);
		String decryptedDigest = decrypt(publicKey.get(), signature);
		return digest.equals(decryptedDigest);
	}

}
