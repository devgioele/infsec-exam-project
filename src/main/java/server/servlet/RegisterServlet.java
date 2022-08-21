package server.servlet;

import com.google.gson.JsonSyntaxException;
import http.HttpError;
import http.Jwt;
import client.crypto.RsaKey;
import http.RsaKey64;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import server.crypto.Crypto;
import server.util.Database;
import server.util.ServerLogger;
import util.Common;
import util.Convert;
import util.Sanitize;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
		String body = new String(request.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
		RsaKey64 publicKey = null;
		try {
			publicKey = gson.fromJson(body, RsaKey64.class);
		} catch (JsonSyntaxException ignored) {

		}

		if (Common.anyNull(name, surname, email, pwd, publicKey)) {
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
					response.setStatus(400);
				} else {
					String salt = Crypto.genSalt();
					String pwdSaltDigest = Crypto.getInstance().hashPwd(pwd, salt);
					conn.setAutoCommit(false);
					Database.update(conn, "INSERT INTO [user] ( name, surname, email, password, salt )" +
							" VALUES ( ?, ?, ?, ?, ? )", name, surname, email, pwdSaltDigest, salt);
					Database.update(conn, "INSERT INTO public_key ( email, modulus, exponent )" +
							" VALUES ( ?, ?, ? )", email, publicKey.modulus, publicKey.exponent);
					conn.commit();
					conn.setAutoCommit(true);
					String jwt = Crypto.getInstance().genJwt(email);
					response.getWriter().println(gson.toJson(new Jwt(jwt)));
					ServerLogger.println("Registration succeeded!");
					response.setStatus(200);
				}
			} catch (SQLException e) {
				e.printStackTrace();
				try {
					ServerLogger.println("Rolling back transaction");
					conn.rollback();
					conn.setAutoCommit(true);
				} catch (SQLException ex) {
					e.printStackTrace();
				}
				response.setStatus(500);
			}
		} else {
			String msg = String.format("Email '%s' is invalid!%n", email);
			ServerLogger.println(msg);
			response.getWriter().println(gson.toJson(new HttpError(msg)));
			response.setStatus(400);
		}
	}

}
