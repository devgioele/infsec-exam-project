package server.servlet;

import crypto.Crypto;
import jakarta.servlet.http.HttpServlet;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import util.Database;
import util.Sanitize;

@WebServlet("/SendMailServlet")
public class SendMailServlet extends HttpServlet {

	private Connection conn;
	private static final long serialVersionUID = 1L;

	public void init() {
		conn = Database.newConnection();
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("text/html");
		String email = Sanitize.noHTML(request.getParameter("email"));
		String receiver = Sanitize.noHTML(request.getParameter("receiver"));
		String subject = Sanitize.noHTML(request.getParameter("subject"));
		String body = Sanitize.safeHTML(request.getParameter("body"));
		String timestamp = new Date(System.currentTimeMillis()).toInstant().toString();

		if(!Crypto.validJwt(request, email)) {
			request.getRequestDispatcher("login.html").forward(request, response);
		}

		try {
			Database.update(conn,
					"INSERT INTO mail ( sender, receiver, subject, body, [time] ) " +
							"VALUES ( ?, ?, ?, ?, ? )", email, receiver, subject, body,
					timestamp);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		request.setAttribute("email", email);
		request.getRequestDispatcher("home.jsp").forward(request, response);
	}

}
