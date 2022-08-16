package crypto;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import util.Common;
import util.Convert;
import util.IO;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;

public class Crypto {

	private static final String jwtCookieName = "Authorization";
	private static final String bearerPrefix = "Bearer ";
	/** The duration of a JWT in milliseconds */
	private static final int jwtLifetime = 60_000;
	private static final Gson gson = new GsonBuilder().create();
	private static final String jwtAlgorithm = "SHA-256";
	private static Crypto INSTANCE;
	private CryptoConfig config;

	public static Crypto getInstance() {
		if (INSTANCE == null) {
			String pathConfig = Paths.get("crypto", "config.json").toAbsolutePath().toString();
			INSTANCE = new Crypto(pathConfig);
		}
		return INSTANCE;
	}

	public Crypto(String pathConfig) {
		config = loadConfig(pathConfig);
		if(config != null) {
			return;
		}
		System.out.println("Creating a new secret.");
		SecureRandom rnd = new SecureRandom();
		byte[] secret = new byte[100];
		rnd.nextBytes(secret);
		config = new CryptoConfig(secret);
		storeConfig(config, pathConfig);
	}

	private static CryptoConfig loadConfig(String pathConfig) {
		try (FileReader reader = new FileReader(pathConfig)) {
			return gson.fromJson(reader, CryptoConfig.class);
		} catch (FileNotFoundException e) {
			System.err.println("No crypto config found.");
		} catch (IOException e) {
			System.err.println("Crypto config could not be read!\n" + e);
		}
		return null;
	}

	private static void storeConfig(CryptoConfig config, String pathConfig) {
		try {
			File configFile = IO.createOpen(pathConfig);
			try (FileWriter writer = new FileWriter(configFile)) {
				gson.toJson(config, writer);
			}
		} catch (IOException e) {
			System.err.println("Crypto config could not be created.\n" + e);
		}
	}

	private static String hash(String algorithm, byte[]... input) throws NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance(algorithm);
		byte[] total = Common.concatByteArrays(input);
		md.update(total);
		return Convert.toBase16(md.digest());
	}

	private static String addBearer(String jwt) {
		return bearerPrefix + jwt;
	}

	private static String removeBearer(String bearer) {
		return bearer.replaceFirst("^" + bearerPrefix, "");
	}

	public static void clearJwt(HttpServletResponse response) {
		response.addCookie(new Cookie(jwtCookieName, ""));
	}

	private void setJwt(HttpServletResponse response, String email, String algorithm)
			throws NoSuchAlgorithmException {
		JwtHeader header = new JwtHeader(jwtAlgorithm);
		JwtPayload payload = new JwtPayload(email, new Date().getTime() + jwtLifetime);
		String headerPayload = Convert.toBase64(gson.toJson(header)) + "." +
				Convert.toBase64(gson.toJson(payload));
		String signature = hash(algorithm, headerPayload.getBytes(), config.secret);
		String jwt = headerPayload + "." + signature;
		System.out.println("Setting JWT: " + jwt);
		response.addCookie(new Cookie(jwtCookieName, jwt));
	}

	public void setJwt(HttpServletResponse response, String email) {
		try {
			setJwt(response, email, jwtAlgorithm);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

	private boolean validJwt(HttpServletRequest request, String email, String algorithm)
			throws NoSuchAlgorithmException {
		Cookie[] cookies = request.getCookies();
		Optional<Cookie> cookie = cookies == null ? Optional.empty() :
				Arrays.stream(cookies).filter(c -> c.getName().equals(jwtCookieName)).findFirst();
		// Verify existence of cookie
		if (cookie.isEmpty()) {
			System.out.printf("Cookie '%s' not found!%n", jwtCookieName);
			return false;
		}
		String jwt = cookie.get().getValue();
		String[] parts = jwt.split("\\.");
		// Verify structure
		if (parts.length != 3) {
			System.out.printf("JWT ill structured with a length of %d.%n%s%n", parts.length, jwt);
			return false;
		}
		String encodedHeader = parts[0];
		String encodedPayload = parts[1];
		String signature = parts[2];
		String headerPayload = encodedHeader + "." + encodedPayload;
		// Verify signature
		String localSignature = hash(algorithm, headerPayload.getBytes(), config.secret);
		if (!signature.equals(localSignature)) {
			System.out.printf("JWT with invalid signature.%nGot: %s%nBut expected: %s%n",
					signature,
					localSignature);
			return false;
		}
		JwtHeader header = gson.fromJson(Convert.fromBase64(encodedHeader), JwtHeader.class);
		// Verify header
		JwtHeader localHeader = new JwtHeader(algorithm);
		if (!header.equals(localHeader)) {
			System.out.printf("JWT with invalid header.%nGot: %s%nBut expected: %s%n", header,
					localHeader);
			return false;
		}
		// Verify claims
		JwtPayload payload = gson.fromJson(Convert.fromBase64(encodedPayload), JwtPayload.class);
		if (!payload.email.equals(email)) {
			System.out.printf("JWT with mismatching claims.%nGot: %s%nBut expected: %s%n",
					payload.email, email);
			return false;
		}
		// Verify expiration time
		if (payload.exp <= new Date().getTime()) {
			System.out.println("Expired JWT.");
			return false;
		}
		return true;
	}

	public boolean validJwt(HttpServletRequest request, String email) {
		try {
			return validJwt(request, email, jwtAlgorithm);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

}
