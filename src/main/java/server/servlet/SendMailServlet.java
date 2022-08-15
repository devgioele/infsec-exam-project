package server.servlet;

import jakarta.servlet.http.HttpServlet;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import util.DBConnection;

/**
 * Servlet implementation class SendMailServlet
 */
@WebServlet("/SendMailServlet")
public class SendMailServlet extends HttpServlet {

	private Connection conn;
	private static final long serialVersionUID = 1L;
    
    public void init() throws ServletException {
    	conn = DBConnection.create();
    }

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html");

		String sender = request.getParameter("email");
		String receiver = request.getParameter("receiver");
		String subject = request.getParameter("subject");
		String body = request.getParameter("body");
		String timestamp = new Date(System.currentTimeMillis()).toInstant().toString();
		
		try (Statement st = conn.createStatement()) {
			// PWN: Stored XSS A, passing `<script>javascript</script>` as subject or body
			// The script is not allowed to use single quotes.
			st.execute(
				"INSERT INTO mail ( sender, receiver, subject, body, [time] ) "
				+ "VALUES ( '" + sender + "', '" + receiver + "', '" + subject + "', '" + body + "', '" + timestamp + "' )"
			);
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		request.setAttribute("email", sender);
		request.getRequestDispatcher("home.jsp").forward(request, response);
	}

}
