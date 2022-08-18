package client.servlet;

import client.crypto.Crypto;
import client.server.Server;
import client.util.Jakarta;
import client.util.UnauthorizedException;
import http.Email;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import util.Sanitize;

import java.io.IOException;

@WebServlet("/InboxServlet")
public class InboxServlet extends HttpServlet {

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("text/html");
		Jakarta.disableCaching(response);

		String jwt = Crypto.extractJwtCookie(request);
		String email = Sanitize.noHtml(request.getParameter("email"));

		try {
			request.setAttribute("content", getContent(jwt));
		} catch(UnauthorizedException ex) {
			request.getRequestDispatcher("login.html").forward(request, response);
			return;
		}
		request.setAttribute("email", email);
		request.getRequestDispatcher("home.jsp").forward(request, response);
	}

	private String getContent(String jwt) throws UnauthorizedException {
		Email[] inbox;
		try {
			inbox = Server.getInstance().loadInbox(jwt);
		} catch(Exception ex) {
			if(ex instanceof UnauthorizedException) {
				throw (UnauthorizedException) ex;
			}
			return "ERROR IN FETCHING INBOX!";
		}
		StringBuilder output = new StringBuilder();
		output.append("<div>\r\n");
		int amount = 0;
		for (Email e : inbox) {
			amount++;
			output.append("<div style=\"white-space: pre-wrap;\"><span style=\"color:grey;\">")
					.append("TO:&emsp;").append(e.sender).append("&emsp;&emsp;AT:&emsp;")
					.append(e.time).append("</span>").append("<br><b>").append(e.subject)
					.append("</b>\r\n").append("<br>").append(e.body).append("<br><br>")
					.append(e.signature).append("</div>\r\n")
					.append("<hr style=\"border-top: 2px solid black;\">\r\n");
		}
		if (amount == 0) {
			output.append("<p><b>Your inbox is empty</b></p>");
		}
		output.append("</div>");
		return output.toString();
	}

}
