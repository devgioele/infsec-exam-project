package server.servlet;

import http.RsaKey64;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import server.crypto.Crypto;
import http.JwtPayload;
import server.util.Database;
import util.Common;
import util.Convert;
import util.Sanitize;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import static server.crypto.Crypto.extractJwtHeader;

@WebServlet(name = "ServerPublicKeyServlet", urlPatterns = {"/server/public-key"})
public class PublicKeyServlet extends HttpServlet {

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

		String email = Sanitize.noHtml(request.getParameter("email"));

		if (Common.anyNull(email)) {
			response.setStatus(400);
			return;
		}

		try (ResultSet sqlRes = Database.query(conn,
				"SELECT modulus, exponent FROM public_key WHERE email=?", email)) {
			if (sqlRes.next()) {
				String modulus = sqlRes.getString(1);
				String exponent = sqlRes.getString(2);
				RsaKey64 publicKey = new RsaKey64(modulus, exponent);
				response.getWriter().write(Convert.gson.toJson(publicKey));
			}
			response.setStatus(200);
		} catch (SQLException e) {
			e.printStackTrace();
			response.setStatus(500);
		}
	}

}

