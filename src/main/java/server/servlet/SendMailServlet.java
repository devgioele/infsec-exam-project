package server.servlet;

import server.crypto.Crypto;
import jakarta.servlet.http.HttpServlet;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import server.util.Database;
import util.Sanitize;

import static server.crypto.Crypto.extractJwtHeader;

@WebServlet("/server/SendMailServlet")
public class SendMailServlet extends HttpServlet {

	private Connection conn;

	public void init() {
		conn = Database.newConnection();
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) {
		response.setContentType("application/json");

		String jwt = extractJwtHeader(request);
		if (!Crypto.getInstance().isJwtValid(jwt)) {
			response.setStatus(401);
			return;
		}
		String sender = Sanitize.noHtml(request.getParameter("sender"));
		String receiver = Sanitize.noHtml(request.getParameter("receiver"));
		String subject = Sanitize.noHtml(request.getParameter("subject"));
		String body = Sanitize.safeHtml(request.getParameter("body"));
		String timestamp = new Date(System.currentTimeMillis()).toInstant().toString();

		try {
			Database.update(conn, "INSERT INTO mail ( sender, receiver, subject, body, [time] ) " +
					"VALUES ( ?, ?, ?, ?, ? )", sender, receiver, subject, body, timestamp);
			response.setStatus(200);
		} catch (SQLException e) {
			e.printStackTrace();
			response.setStatus(500);
		}
	}

}
