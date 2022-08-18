package server.servlet;

import http.Email;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import server.crypto.Crypto;
import server.crypto.JwtPayload;
import server.util.Database;
import util.Convert;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static server.crypto.Crypto.extractJwtHeader;

@WebServlet(name = "ServerGetSentEmailsServlet", urlPatterns = {"/server/email/sent"})
public class GetSentEmailsServlet extends HttpServlet {

	private static Connection conn;

	public void init() {
		conn = Database.newConnection();
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		response.setContentType("application/json");

		String jwt = extractJwtHeader(request);
		JwtPayload payload = Crypto.getInstance().getJwtPayload(jwt);
		if (payload == null) {
			response.setStatus(401);
			return;
		}
		String sender = payload.email;

		try (ResultSet sqlRes = Database.query(conn,
				"SELECT * FROM mail WHERE sender=? ORDER BY [time] DESC", sender)) {
			List<Email> sentEmails = new ArrayList<>();
			while (sqlRes.next()) {
				sentEmails.add(Email.fromSql(sqlRes));
			}
			response.getWriter().write(Convert.gson.toJson(sentEmails.toArray()));
			response.setStatus(200);
		} catch (SQLException e) {
			e.printStackTrace();
			response.setStatus(500);
		}
	}

}
