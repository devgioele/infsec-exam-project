package server.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import server.crypto.Crypto;
import server.util.Database;

import java.io.IOException;
import java.sql.Connection;

import static server.crypto.Crypto.extractJwtHeader;

@SuppressWarnings("unused")
@WebServlet("/server/VerifyJwtServlet")
public class VerifyJwtServlet extends HttpServlet {

	private static Connection conn;

	public void init() {
		conn = Database.newConnection();
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("application/json");

		String jwt = extractJwtHeader(request);
		if (!Crypto.getInstance().isJwtValid(jwt)) {
			response.setStatus(401);
		} else {
			response.setStatus(200);
		}
	}

}

