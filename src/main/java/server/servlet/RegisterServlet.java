package server.servlet;

import jakarta.servlet.http.HttpServlet;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import util.DBConnection;

/**
 * Servlet implementation class RegisterServlet
 */
@WebServlet("/RegisterServlet")
public class RegisterServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static Connection conn;
    
    public void init() {
    	conn = DBConnection.create();
    }

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html");

		String name = request.getParameter("name");
		String surname = request.getParameter("surname");
		String email = request.getParameter("email");
		String pwd = request.getParameter("password");
		
		try (Statement st = conn.createStatement()) {
			// PWN: SQL injection injecting into email and commenting out the rest
			/* PWN: Reflected XSS A, passing `<script>console.log("pwned");</script>` as email.
				However, the string cannot be longer than 50 characters without crashing the server,
				which is very limiting. */
			// PWN: Reflected XSS, passing `"><script>console.log("pwned");</script>` as password.
			ResultSet sqlRes = st.executeQuery(
					"SELECT * "
							+ "FROM [user] "
							+ "WHERE email='" + email + "'"
			);
			
			if (sqlRes.next()) {
				System.out.println("Email already registered!");
				request.getRequestDispatcher("register.html").forward(request, response);
			} else {
				st.execute(
					"INSERT INTO [user] ( name, surname, email, password ) "
					+ "VALUES ( '" + name + "', '" + surname + "', '" + email + "', '" + pwd + "' )"
				);

				// PWN: Stored XSS A
				request.setAttribute("email", email);
				request.setAttribute("password", pwd);
				
				System.out.println("Registration succeeded!");
				// PWN: Stored XSS A
				request.getRequestDispatcher("home.jsp").forward(request, response);
			}

		} catch (SQLException e) {
			e.printStackTrace();
			request.getRequestDispatcher("register.html").forward(request, response);
		}
	}

}
