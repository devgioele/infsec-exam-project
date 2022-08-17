package client.servlet;

import client.crypto.Crypto;
import client.server.Server;
import client.server.UnauthorizedException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/SendMailServlet")
public class SendMailServlet extends HttpServlet {

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("text/html");

		String jwt = Crypto.extractJwtCookie(request);
		String email = request.getParameter("email");
		String receiver = request.getParameter("receiver");
		String subject = request.getParameter("subject");
		String body = request.getParameter("body");
		try {
			request.setAttribute("content", getContent(jwt, email, receiver, subject, body));
		} catch(UnauthorizedException ex) {
			request.getRequestDispatcher("login.html").forward(request, response);
			return;
		}
		request.setAttribute("email", email);
		request.getRequestDispatcher("home.jsp").forward(request, response);
	}

	private String getContent(String jwt, String sender, String receiver, String subject, String body) throws UnauthorizedException {
		try {
			Server.getInstance().sendMail(jwt, sender, receiver, subject, body);
		} catch(Exception ex) {
			if(ex instanceof UnauthorizedException) {
				throw (UnauthorizedException) ex;
			}
			return "ERROR SENDING EMAIL!";
		}
		return "Sent!";
	}

}