package client.crypto;

import client.server.Server;
import client.util.ClientLogger;
import http.JwtPayload;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import util.Convert;

import java.math.BigInteger;
import java.net.http.HttpRequest;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Optional;

import static util.Crypto.bearerPrefix;
import static util.Crypto.jwtId;
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
			config = new CryptoConfig(4096);
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
		// TODO: Is it more efficient to divide the string into multiple big integers?
		// Interpret bytes of utf-8 string as big integer
		BigInteger bi = new BigInteger(plaintext.getBytes());
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
	 * UTF-8 string using the given key.
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
		ClientLogger.printf("Generated keys for '%s':%nPrivate: %s%nPublic: %s%n", id, pair.privateKey, pair.publicKey);
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

}
