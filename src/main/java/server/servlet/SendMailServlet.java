package server.servlet;

import http.HttpError;
import http.Jwt;
import server.crypto.Crypto;
import jakarta.servlet.http.HttpServlet;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import server.crypto.JwtPayload;
import server.util.Database;
import server.util.ServerLogger;
import util.Common;
import util.Sanitize;

import static server.crypto.Crypto.extractJwtHeader;
import static util.Convert.gson;

@WebServlet(name = "ServerSendMailServlet", urlPatterns = {"/server/email/send"})
public class SendMailServlet extends HttpServlet {

	private Connection conn;

	public void init() {
		conn = Database.newConnection();
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		response.setContentType("application/json");

		String jwt = extractJwtHeader(request);
		JwtPayload payload = Crypto.getInstance().getJwtPayload(jwt);
		if (payload == null) {
			response.setStatus(401);
			return;
		}
		String sender = payload.email;
		String receiver = Sanitize.noHtml(request.getParameter("receiver"));
		// TODO: Use real signature
		String signature = "";
		String subject = Sanitize.noHtml(request.getParameter("subject"));
		String body = Sanitize.safeHtml(request.getParameter("body"));
		String timestamp = new Date(System.currentTimeMillis()).toInstant().toString();

		if(Common.anyNull(sender, receiver, subject, body)) {
			response.setStatus(400);
			return;
		}

		// Verify existence of receiver
		try (ResultSet sqlRes = Database.query(conn,
				"SELECT * FROM [user] WHERE email=?", receiver)) {
			if (sqlRes.next()) {
				// Receiver exists, send email
				try {
					Database.update(conn, "INSERT INTO mail ( sender, receiver, signature, subject, body, [time] ) " +
							"VALUES ( ?, ?, ?, ?, ?, ? )", sender, receiver, signature, subject, body, timestamp);
					response.setStatus(200);
				} catch (SQLException e) {
					e.printStackTrace();
					response.setStatus(500);
				}
			} else {
				String msg = String.format("Receiver '%s' does not exist!", receiver);
				ServerLogger.println(msg);
				response.getWriter().println(gson.toJson(new HttpError(msg)));
				response.setStatus(422);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			response.setStatus(500);
		}
	}

}
