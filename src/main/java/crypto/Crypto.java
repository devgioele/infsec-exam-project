package crypto;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import util.Convert;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;

public class Crypto {

	private static final String secret = "yd-rsN2cLk6gXNe4PhouhNovnJPVxVMznEF" +
			".PZcfePqon@MTasJ7d4FdNYdBbpcdCpHB8Cskb7BJx3ZWtAEd4VnyviP2rWRa8Nus";
	private static final String jwtCookieName = "Authorization";
	private static final String bearerPrefix = "Bearer ";
	/** The duration of a JWT in milliseconds */
	private static final int jwtLifetime = 60_000;
	private static final Gson gson = new GsonBuilder().create();
	private static final String jwtAlgorithm = "SHA-256";

	private static String hash(byte[] input, String algorithm) throws NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance(algorithm);
		md.update(input);
		return Convert.toBase16(md.digest());
	}

	private static String addBearer(String jwt) {
		return bearerPrefix + jwt;
	}

	private static String removeBearer(String bearer) {
		return bearer.replaceFirst("^" + bearerPrefix, "");
	}

	private static void setJwt(HttpServletResponse response, String email, String algorithm)
			throws NoSuchAlgorithmException {
		JwtHeader header = new JwtHeader(jwtAlgorithm);
		JwtPayload payload = new JwtPayload(email, new Date().getTime() + jwtLifetime);
		String headerPayload = Convert.toBase64(gson.toJson(header)) + "." +
				Convert.toBase64(gson.toJson(payload));
		String signature = hash((headerPayload + secret).getBytes(), algorithm);
		String jwt = headerPayload + "." + signature;
		String bearer = addBearer(jwt);
		response.addCookie(new Cookie(jwtCookieName, bearer));
	}

	public static void setJwt(HttpServletResponse response, String email) {
		try {
			setJwt(response, email, jwtAlgorithm);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

	private static boolean validJwt(HttpServletRequest request, String email, String algorithm)
			throws NoSuchAlgorithmException {
		Optional<Cookie> cookie =
				Arrays.stream(request.getCookies()).filter(c -> c.getName().equals(jwtCookieName))
						.findFirst();
		if (cookie.isEmpty()) {
			System.out.printf("Cookie '%s' not found!%n", jwtCookieName);
			return false;
		}
		String bearer = cookie.get().getValue();
		String jwt = removeBearer(bearer);
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
		String localSignature = hash((headerPayload + secret).getBytes(), algorithm);
		if (!signature.equals(localSignature)) {
			System.out.printf("JWT with invalid signature.%nGot: %s%nBut expected: %s%n", signature, localSignature);
			return false;
		}
		JwtHeader header = gson.fromJson(Convert.fromBase64(encodedHeader), JwtHeader.class);
		// Verify header
		JwtHeader localHeader = new JwtHeader(algorithm);
		if (!header.equals(localHeader)) {
			System.out.printf("JWT with invalid header.%nGot: %s%nBut expected: %s%n", header, localHeader);
			return false;
		}
		// Verify claims
		JwtPayload payload = gson.fromJson(Convert.fromBase64(encodedPayload), JwtPayload.class);
		if (!payload.email.equals(email)) {
			System.out.printf("JWT with mismatching claims.%nGot: %s%nBut expected: %s%n", payload.email, email);
			return false;
		}
		// Verify expiration time
		if (payload.exp <= new Date().getTime()) {
			System.out.println("Expired JWT.");
			return false;
		}
		return true;
	}

	public static boolean validJwt(HttpServletRequest request, String email) {
		try {
			return validJwt(request, email, jwtAlgorithm);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

}
