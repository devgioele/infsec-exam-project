package server.servlet;

import crypto.Crypto;
import jakarta.servlet.http.HttpServlet;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import util.Database;
import util.Sanitize;

@WebServlet("/LoginServlet")
public class LoginServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static Connection conn;

	public void init() {
		conn = Database.newConnection();
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("text/html");
		String email = Sanitize.noHTML(request.getParameter("email"));
		String pwd = Sanitize.noHTML(request.getParameter("password"));

		try (ResultSet sqlRes = Database.query(conn,
				"SELECT * FROM [user] WHERE email=? AND password=?", email, pwd)) {
			if (sqlRes.next()) {
				Crypto.setJwt(response, email);
				request.setAttribute("email", sqlRes.getString(3));
				request.setAttribute("password", sqlRes.getString(4));
				request.setAttribute("content", "Welcome!");
				System.out.println("Login succeeded!");
				request.getRequestDispatcher("home.jsp").forward(request, response);
			} else {
				System.out.println("Login failed!");
				request.getRequestDispatcher("login.html").forward(request, response);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			request.getRequestDispatcher("login.html").forward(request, response);
		}
	}

}
