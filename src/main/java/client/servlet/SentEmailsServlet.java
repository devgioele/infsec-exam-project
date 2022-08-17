package client.servlet;

import client.crypto.Crypto;
import client.server.Server;
import client.server.UnauthorizedException;
import http.Email;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import util.Sanitize;

import java.io.IOException;

@WebServlet("/SentEmailsServlet")
public class SentEmailsServlet extends HttpServlet {

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("text/html");

		String jwt = Crypto.extractJwtCookie(request);
		String email = Sanitize.noHtml(request.getParameter("email"));
		try {
		request.setAttribute("content", getContent(jwt, email));
		} catch(UnauthorizedException ex) {
			request.getRequestDispatcher("login.html").forward(request, response);
			return;
		}
		request.setAttribute("email", email);
		request.getRequestDispatcher("home.jsp").forward(request, response);
	}

	private String getContent(String jwt, String email) throws UnauthorizedException {
		Email[] sentEmails;
		try {
			sentEmails = Server.getInstance().loadSentEmails(jwt, email);
		} catch(Exception ex) {
			if(ex instanceof UnauthorizedException) {
				throw (UnauthorizedException) ex;
			}
			return "ERROR IN FETCHING SENT MAILS!";
		}
		StringBuilder output = new StringBuilder();
		output.append("<div>\r\n");
		int amount = 0;
		for (Email e : sentEmails) {
			amount++;
			output.append("<div style=\"white-space: pre-wrap;\"><span style=\"color:grey;\">")
					.append("TO:&emsp;").append(e.sender).append("&emsp;&emsp;AT:&emsp;")
					.append(e.time).append("</span>").append("<br><b>").append(e.subject)
					.append("</b>\r\n").append("<br>").append(e.body).append("<br><br>")
					.append(e.signature).append("</div>\r\n")
					.append("<hr style=\"border-top: 2px solid black;\">\r\n");
		}
		if (amount == 0) {
			output.append("<p><b>Nothing sent yet</b></p>");
		}
		output.append("</div>");
		return output.toString();
	}

}
