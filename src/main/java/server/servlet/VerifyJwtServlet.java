package server.servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import server.crypto.Crypto;
import http.JwtPayload;
import server.util.ServerLogger;
import util.Convert;

import java.io.IOException;

import static server.crypto.Crypto.extractJwtHeader;

@WebServlet(name = "ServerVerifyJwtServlet", urlPatterns = {"/server/jwt/verify"})
public class VerifyJwtServlet extends HttpServlet {

	protected void doPost(HttpServletRequest request, HttpServletResponse response) {
		response.setContentType("application/json");

		String jwt = extractJwtHeader(request);
		JwtPayload payload = Crypto.getInstance().getJwtPayload(jwt);
		if (payload == null) {
			ServerLogger.println("JWT is invalid.");
			response.setStatus(401);
		} else {
			ServerLogger.println("JWT is valid.");
			response.setStatus(200);
		}
	}

}

