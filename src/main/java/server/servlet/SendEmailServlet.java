package server.servlet;

import http.HttpError;
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
import http.JwtPayload;
import server.util.Database;
import server.util.ServerLogger;
import util.Common;
import util.Sanitize;

import static server.crypto.Crypto.extractJwtHeader;
import static util.Convert.gson;

@WebServlet(name = "ServerSendEmailServlet", urlPatterns = {"/server/email/send"})
public class SendEmailServlet extends HttpServlet {

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
		String signature = request.getParameter("signature");
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
					Database.update(conn, "INSERT INTO email ( sender, receiver, subject, body, signature, [time] ) " +
							"VALUES ( ?, ?, ?, ?, ?, ? )", sender, receiver, subject, body, signature, timestamp);
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
