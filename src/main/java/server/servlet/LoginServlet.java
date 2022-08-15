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
 * Servlet implementation class HelloWorldServlet
 */
@WebServlet("/LoginServlet")
public class LoginServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static Connection conn;
    
    public void init() {
    	conn = DBConnection.create();
    }

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html");

		// PWN: SQL injection injecting into email and commenting out the rest
		String email = request.getParameter("email");
		String pwd = request.getParameter("password");
		
		try (Statement st = conn.createStatement()) {
			ResultSet sqlRes = st.executeQuery(
				"SELECT * "
				+ "FROM [user] "
				+ "WHERE email='" + email + "' "
					+ "AND password='" + pwd + "'"
			);

			if (sqlRes.next()) {
				request.setAttribute("email", sqlRes.getString(3));
				request.setAttribute("password", sqlRes.getString(4));
				
				System.out.println("Login succeeded!");
				request.setAttribute("content", "");
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
