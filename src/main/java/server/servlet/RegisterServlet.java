package server.servlet;

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

@WebServlet("/RegisterServlet")
public class RegisterServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static Connection conn;

	public void init() {
		conn = Database.newConnection();
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("text/html");

		String name = Sanitize.noHTML(request.getParameter("name"));
		String surname = Sanitize.noHTML(request.getParameter("surname"));
		String email = Sanitize.noHTML(request.getParameter("email"));
		String pwd = Sanitize.noHTML(request.getParameter("password"));

		if (Sanitize.isEmail(email)) {
			try (ResultSet sqlRes = Database.query(conn,
					"SELECT * FROM [user] WHERE email=?", email)) {
				if (sqlRes.next()) {
					System.out.println("Email already registered!");
				} else {
					Database.update(conn,
							"INSERT INTO [user] ( name, surname, email, password ) " +
									"VALUES ( ?, ?, ?, ? )", name, surname, email, pwd);
					request.setAttribute("email", email);
					System.out.println("Registration succeeded!");
					request.getRequestDispatcher("home.jsp").forward(request, response);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} else {
			System.out.printf("Email '%s' is invalid!%n", email);
		}
		// On error, stay on the registration page
		request.getRequestDispatcher("register.html").forward(request, response);
	}

}
