package client.servlet;

import client.crypto.Crypto;
import client.server.Server;
import client.util.ClientLogger;
import client.util.Jakarta;
import client.util.UnauthorizedException;
import http.Email;
import client.crypto.RsaKey;
import http.JwtPayload;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import util.Sanitize;

import java.io.IOException;
import java.nio.charset.CharacterCodingException;
import java.util.Optional;

@WebServlet(name = "InboxServlet", urlPatterns = {"/email-inbox"})
public class InboxServlet extends HttpServlet {

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("text/html");
		Jakarta.disableCaching(response);

		String jwt = Crypto.extractJwtCookie(request);
		JwtPayload payload = Server.getInstance().isJwtValid(jwt);

		if (payload != null) {
			try {
				request.setAttribute("content", getContent(jwt, payload.email));
			} catch (UnauthorizedException ex) {
				payload = null;
			}
		}
		if (payload != null) {
			request.setAttribute("email", payload.email);
			request.getRequestDispatcher("home.jsp").forward(request, response);
		} else {
			request.getRequestDispatcher("login.html").forward(request, response);
		}
	}

	private String getContent(String jwt, String email) throws UnauthorizedException {
		Email[] inbox;
		try {
			inbox = Server.getInstance().loadInbox(jwt);
		} catch (Exception ex) {
			if (ex instanceof UnauthorizedException) {
				throw (UnauthorizedException) ex;
			}
			return "ERROR IN FETCHING INBOX!";
		}
		StringBuilder output = new StringBuilder();
		output.append("<div>\r\n");

		int amount = 0;
		for (Email e : inbox) {
			amount++;
			// Decrypt if private key has been found
			Optional<RsaKey> privateKey = Crypto.getInstance().getPrivateKey(email);
			if (privateKey.isPresent()) {
				ClientLogger.printf("Decrypting subject: %s%nWith private key: %s%n", e.subject,
						privateKey.get());
				try {
					e.subject = Crypto.decrypt(privateKey.get(), e.subject);
					e.body = Crypto.decrypt(privateKey.get(), e.body);
				} catch (CharacterCodingException ex) {
					output.append(
							"<p>Email was not encrypted or was encrypted with a foreign public " +
									"key" + ".</p>");
				}
			} else {
				output.append("<p>Private key not found for decryption of email.</p>");
			}
			output.append("<div style=\"white-space: pre-wrap;\"><span style=\"color:grey;\">")
					.append("TO:&emsp;").append(e.sender).append("&emsp;&emsp;AT:&emsp;")
					.append(e.time).append("</span><br><b>").append(e.subject)
					.append("</b>\r\n<br>").append(e.body).append("<br><br>").append(e.signature)
					.append("</div>\n<hr style=\"border-top: 2px solid black;\">\r\n");
		}
		if (amount == 0) {
			output.append("<p><b>Your inbox is empty</b></p>");
		}
		output.append("</div>");
		return output.toString();
	}

}
