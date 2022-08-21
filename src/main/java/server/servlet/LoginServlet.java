package server.servlet;

import http.HttpError;
import http.Jwt;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import server.crypto.Crypto;
import server.util.Database;
import server.util.ServerLogger;
import util.Common;
import util.Sanitize;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import static util.Convert.gson;

@WebServlet(name = "ServerLoginServlet", urlPatterns = {"/server/login"})
public class LoginServlet extends HttpServlet {

	private static Connection conn;

	public void init() {
		conn = Database.newConnection();
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		response.setContentType("application/json");

		String email = Sanitize.noHtml(request.getParameter("email"));
		String pwd = Sanitize.noHtml(request.getParameter("password"));

		if(Common.anyNull(email, pwd)) {
			response.setStatus(400);
			return;
		}

		try (ResultSet sqlRes = Database.query(conn,
				"SELECT * FROM [user] WHERE email=? AND password=?", email, pwd)) {
			if (sqlRes.next()) {
				String jwt = Crypto.getInstance().genJwt(email);
				ServerLogger.println("Login succeeded!");
				response.getWriter().println(gson.toJson(new Jwt(jwt)));
				response.setStatus(200);
			} else {
				String msg = String.format("User '%s' does not exist or the credentials are incorrect!", email);
				ServerLogger.println(msg);
				response.getWriter().println(gson.toJson(new HttpError(msg)));
				response.setStatus(401);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			response.setStatus(500);
		}
	}

}
