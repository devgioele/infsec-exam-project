package server.servlet;

import http.HttpError;
import http.Jwt;
import jakarta.servlet.ServletException;
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

@WebServlet(name = "ServerRegisterServlet", urlPatterns = {"/server/register"})
public class RegisterServlet extends HttpServlet {

	private static Connection conn;

	public void init() {
		conn = Database.newConnection();
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		response.setContentType("application/json");

		String name = Sanitize.noHtml(request.getParameter("name"));
		String surname = Sanitize.noHtml(request.getParameter("surname"));
		String email = Sanitize.noHtml(request.getParameter("email"));
		String pwd = Sanitize.noHtml(request.getParameter("password"));

		if(Common.anyNull(name, surname, email, pwd)) {
			response.setStatus(400);
			return;
		}

		if (Sanitize.isEmail(email)) {
			try (ResultSet sqlRes = Database.query(conn, "SELECT * FROM [user] WHERE email=?",
					email)) {
				if (sqlRes.next()) {
					String msg = String.format("Email '%s' already registered!", email);
					ServerLogger.println(msg);
					response.getWriter().println(gson.toJson(new HttpError(msg)));
					response.setStatus(406);
				} else {
					Database.update(conn,
							"INSERT INTO [user] ( name, surname, email, password )" + " " +
									"VALUES ( ?, ?, ?, ? )", name, surname, email, pwd);
					String jwt = Crypto.getInstance().genJwt(email);
					response.getWriter().println(gson.toJson(new Jwt(jwt)));
					ServerLogger.println("Registration succeeded!");
					response.setStatus(200);
				}
			} catch (SQLException e) {
				e.printStackTrace();
				response.setStatus(500);
			}
		} else {
			String msg = String.format("Email '%s' is invalid!%n", email);
			ServerLogger.println(msg);
			response.getWriter().println(gson.toJson(new HttpError(msg)));
			response.setStatus(406);
		}
	}

}
