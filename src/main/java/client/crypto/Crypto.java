package client.crypto;

import client.server.Server;
import client.util.ClientLogger;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import okhttp3.Request;

import java.util.Arrays;
import java.util.Optional;


public class Crypto {

	public static final String jwtId = "Authorization";
	public static final String bearerPrefix = "Bearer ";

	private static String addBearer(String jwt) {
		return bearerPrefix + jwt;
	}

	public static Request.Builder setJwtHeader(Request.Builder builder, String jwt) {
		return builder.addHeader(jwtId, addBearer(jwt));
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
				Arrays.stream(cookies).filter(c -> c.getName().equals(jwtId)).findFirst();
		// Verify existence of cookie
		if (cookie.isEmpty()) {
			ClientLogger.printf("Cookie '%s' not found!%n", jwtId);
			return null;
		}
		return cookie.get().getValue();
	}

	public static boolean isJwtValid(String jwt) {
		return Server.getInstance().isJwtValid(jwt);
	}

	public static boolean isJwtValid(HttpServletRequest request) {
		String jwt = extractJwtCookie(request);
		return jwt != null && isJwtValid(jwt);
	}

}
