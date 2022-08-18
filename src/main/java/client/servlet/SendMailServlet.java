package client.servlet;

import client.crypto.Crypto;
import client.server.Server;
import client.util.HttpException;
import client.util.UnauthorizedException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import server.crypto.JwtPayload;

import java.io.IOException;

@WebServlet("/SendMailServlet")
public class SendMailServlet extends HttpServlet {

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("text/html");

		String jwt = Crypto.extractJwtCookie(request);
		String email = request.getParameter("email");
		String receiver = request.getParameter("receiver");
		String subject = request.getParameter("subject");
		String body = request.getParameter("body");
		try {
			request.setAttribute("content", getContent(jwt, receiver, subject, body));
		} catch (UnauthorizedException ex) {
			request.getRequestDispatcher("login.html").forward(request, response);
			return;
		}
		request.setAttribute("email", email);
		request.getRequestDispatcher("home.jsp").forward(request, response);
	}

	private String getContent(String jwt, String receiver, String subject, String body)
			throws UnauthorizedException {
		try {
			Server.getInstance().sendMail(jwt, receiver, subject, body);
		} catch (IOException ex) {
			if (ex instanceof UnauthorizedException) {
				throw (UnauthorizedException) ex;
			}
			String msg = "Email could not be sent!";
			if(ex instanceof HttpException) {
				msg += "\n" + ((HttpException) ex).getUserMessage();
			}
			return msg;
		}
		return "Sent!";
	}

}