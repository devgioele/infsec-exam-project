package client.servlet;

import client.crypto.Crypto;
import client.server.Server;
import client.util.Jakarta;
import client.util.UnauthorizedException;
import http.Email;
import http.JwtPayload;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import util.Sanitize;

import java.io.IOException;

@WebServlet(name = "SentEmailsServlet", urlPatterns = {"/email-sent"})
public class SentEmailsServlet extends HttpServlet {

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("text/html");
		Jakarta.disableCaching(response);

		String jwt = Crypto.extractJwtCookie(request);
		JwtPayload payload = Crypto.validJwt(jwt);

		try {
			request.setAttribute("content", getContent(jwt));
		} catch (UnauthorizedException ex) {
			request.getRequestDispatcher("login.html").forward(request, response);
			return;
		}
		request.setAttribute("email", payload.email);
		request.getRequestDispatcher("home.jsp").forward(request, response);
	}

	private String getContent(String jwt) throws UnauthorizedException {
		Email[] sentEmails;
		try {
			sentEmails = Server.getInstance().loadSentEmails(jwt);
		} catch (IOException ex) {
			return "ERROR IN FETCHING SENT EMAILS!";
		}
		StringBuilder output = new StringBuilder();
		output.append("<div>\r\n");
		int amount = 0;
		for (Email e : sentEmails) {
			amount++;
			if(e.signature == null) {
				output.append("<b style='color: grey'>Unsigned</b>");
			} else {
				output.append("<b style='color: blue'>Signed</b>");
			}
			output.append("<div style=\"white-space: pre-wrap;\"><span style=\"color:grey;\">")
					.append("TO:&emsp;").append(e.receiver).append("&emsp;&emsp;AT:&emsp;")
					.append(e.time).append("</span>").append("<br><b>").append(e.subject)
					.append("</b>\r\n").append("<br>").append(e.body).append("<br><br>");
			output.append("</div>\r\n")
					.append("<hr style=\"border-top: 2px solid black;\">\r\n");
		}
		if (amount == 0) {
			output.append("<p><b>Nothing sent yet</b></p>");
		}
		output.append("</div>");
		return output.toString();
	}

}
