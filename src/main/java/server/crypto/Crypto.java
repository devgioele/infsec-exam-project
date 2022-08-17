package server.crypto;

import jakarta.servlet.http.HttpServletRequest;
import server.util.ServerLogger;
import util.Common;
import util.Convert;

import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Date;

import static util.IO.jsonFromFile;
import static util.IO.jsonToFile;

public class Crypto {

	private static Crypto INSTANCE;
	private CryptoConfig config;

	public static final String jwtId = "Authorization";
	public static final String bearerPrefix = "Bearer ";

	public static Crypto getInstance() {
		if (INSTANCE == null) {
			String pathConfig = Paths.get("server", "crypto.json").toAbsolutePath().toString();
			INSTANCE = new Crypto(pathConfig);
		}
		return INSTANCE;
	}

	public Crypto(String pathConfig) {
		config = jsonFromFile(pathConfig, CryptoConfig.class);
		if (config != null) {
			return;
		}
		ServerLogger.println("Creating a new secret.");
		SecureRandom rnd = new SecureRandom();
		byte[] secret = new byte[100];
		rnd.nextBytes(secret);
		config = new CryptoConfig(secret, 60_000, "SHA-256");
		jsonToFile(config, pathConfig);
	}

	public static String extractJwtHeader(HttpServletRequest request) {
		return removeBearer(request.getHeader(jwtId));
	}

	private static String removeBearer(String bearer) {
		return bearer.replaceFirst("^" + bearerPrefix, "");
	}

	private static String hash(String algorithm, byte[]... input) throws NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance(algorithm);
		byte[] total = Common.concatByteArrays(input);
		md.update(total);
		return Convert.toBase16(md.digest());
	}

	private String genJwt(String email, String algorithm)
			throws NoSuchAlgorithmException {
		JwtHeader header = new JwtHeader(config.jwtAlgorithm);
		JwtPayload payload = new JwtPayload(email, new Date().getTime() + config.jwtLifetime);
		String headerPayload = Convert.toBase64(Convert.gson.toJson(header)) + "." +
				Convert.toBase64(Convert.gson.toJson(payload));
		String signature = hash(algorithm, headerPayload.getBytes(), config.secret);
		return headerPayload + "." + signature;
	}

	public String genJwt(String email) {
		try {
			return genJwt(email, config.jwtAlgorithm);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

	private boolean isJwtValid(String jwt, String algorithm) throws NoSuchAlgorithmException {
		String[] parts = jwt.split("\\.");
		// Verify structure
		if (parts.length != 3) {
			ServerLogger.printf("JWT ill structured with a length of %d.%n%s%n", parts.length, jwt);
			return false;
		}
		String encodedHeader = parts[0];
		String encodedPayload = parts[1];
		String signature = parts[2];
		String headerPayload = encodedHeader + "." + encodedPayload;
		// Verify signature
		String localSignature = hash(algorithm, headerPayload.getBytes(), config.secret);
		if (!signature.equals(localSignature)) {
			ServerLogger.printf("JWT with invalid signature.%nGot: %s%nBut expected: %s%n",
					signature,
					localSignature);
			return false;
		}
		JwtHeader header = Convert.gson.fromJson(Convert.fromBase64(encodedHeader), JwtHeader.class);
		// Verify header
		JwtHeader localHeader = new JwtHeader(algorithm);
		if (!header.equals(localHeader)) {
			ServerLogger.printf("JWT with invalid header.%nGot: %s%nBut expected: %s%n", header,
					localHeader);
			return false;
		}
		JwtPayload payload = Convert.gson.fromJson(Convert.fromBase64(encodedPayload), JwtPayload.class);
		// Verify expiration time
		if (payload.exp <= new Date().getTime()) {
			ServerLogger.println("Expired JWT.");
			return false;
		}
		return true;
	}

	public boolean isJwtValid(String jwt) {
		try {
			return isJwtValid(jwt, config.jwtAlgorithm);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

}
