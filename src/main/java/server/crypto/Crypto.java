package server.crypto;

import http.JwtPayload;
import jakarta.servlet.http.HttpServletRequest;
import server.util.ServerLogger;
import util.Convert;

import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Date;

import static util.Crypto.*;
import static util.IO.jsonFromFile;
import static util.IO.jsonToFile;

public class Crypto {

	private static Crypto INSTANCE;

	private final CryptoConfig config;
	private final static SecureRandom secureRnd = new SecureRandom();

	public static Crypto getInstance() {
		if (INSTANCE == null) {
			String pathConfig = Paths.get("server", "crypto.json").toAbsolutePath().toString();
			INSTANCE = new Crypto(pathConfig);
		}
		return INSTANCE;
	}

	public Crypto(String pathConfig) {
		CryptoConfig config = jsonFromFile(pathConfig, CryptoConfig.class);
		if (config == null) {
			ServerLogger.println("Creating a new crypto config.");
			ServerLogger.println("Creating a new secret for JWT signatures.");
			byte[] secret = new byte[100];
			secureRnd.nextBytes(secret);
			config = new CryptoConfig(secret, 60_000, HashingAlgorithm.SHA256,
					HashingAlgorithm.SHA256);
			jsonToFile(config, pathConfig);
		}
		this.config = config;
	}

	public static String extractJwtHeader(HttpServletRequest request) {
		String header = request.getHeader(jwtId);
		if (header == null) return null;
		return removeBearer(header);
	}

	private static String removeBearer(String bearer) {
		return bearer.replaceFirst("^" + bearerPrefix, "");
	}

	/**
	 * @return Random bytes encoded in base 64.
	 * The length of the returned string will be 56, because base64 uses one character for every 6 bits
	 * and the number of bytes used must be a multiple of 4.
	 */
	public static String genSalt() {
		byte[] salt = new byte[40];
		secureRnd.nextBytes(salt);
		return Convert.toBase64(salt);
	}

	private String genJwt(String email, HashingAlgorithm algorithm)
			throws NoSuchAlgorithmException {
		JwtHeader header = new JwtHeader(config.jwtAlgorithm.toString());
		JwtPayload payload = new JwtPayload(email, new Date().getTime() + config.jwtLifetime);
		String headerPayload = Convert.toBase64Url(Convert.gson.toJson(header)) + "." +
				Convert.toBase64Url(Convert.gson.toJson(payload));
		String signature =
				hash(algorithm, headerPayload.getBytes(StandardCharsets.UTF_8), config.secret);
		return headerPayload + "." + signature;
	}

	public String genJwt(String email) {
		try {
			return genJwt(email, config.jwtAlgorithm);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

	private JwtPayload getJwtPayload(String jwt, HashingAlgorithm algorithm) {
		if(jwt == null) return null;
		String[] parts = jwt.split("\\.");
		// Verify structure
		if (parts.length != 3) {
			ServerLogger.printf("JWT ill structured with a length of %d:%n%s%n", parts.length,
					jwt);
			return null;
		}
		String encodedHeader = parts[0];
		String encodedPayload = parts[1];
		String signature = parts[2];
		String headerPayload = encodedHeader + "." + encodedPayload;
		// Verify signature
		String localSignature =
				hash(algorithm, headerPayload.getBytes(StandardCharsets.UTF_8), config.secret);
		if (!signature.equals(localSignature)) {
			ServerLogger.printf("JWT with invalid signature.%nGot: %s%nBut expected: %s%n",
					signature, localSignature);
			return null;
		}
		// Verify header
		JwtHeader header =
				Convert.gson.fromJson(Convert.fromBase64Url(encodedHeader), JwtHeader.class);
		JwtHeader localHeader = new JwtHeader(algorithm.toString());
		if (!header.equals(localHeader)) {
			ServerLogger.printf("JWT with invalid header.%nGot: %s%nBut expected: %s%n", header,
					localHeader);
			return null;
		}
		// Decode payload
		JwtPayload payload =
				Convert.gson.fromJson(Convert.fromBase64Url(encodedPayload), JwtPayload.class);
		// Verify expiration time
		if (payload.exp <= new Date().getTime()) {
			ServerLogger.println("Expired JWT.");
			return null;
		}
		return payload;
	}

	public JwtPayload getJwtPayload(String jwt) {
		return getJwtPayload(jwt, config.jwtAlgorithm);
	}

	public String hashPwd(String pwd, String salt) {
		return util.Crypto.hash(config.passwordAlgorithm,
				(pwd + salt).getBytes(StandardCharsets.UTF_8));
	}

}
