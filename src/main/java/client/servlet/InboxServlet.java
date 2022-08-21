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
		JwtPayload payload = Crypto.validJwt(jwt);

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
		} catch (IOException ex) {
			return "ERROR IN FETCHING INBOX!";
		}
		StringBuilder output = new StringBuilder();
		output.append("<div>\r\n");

		int amount = 0;
		for (Email e : inbox) {
			amount++;
			// Attempt decryption
			Optional<RsaKey> privateKey = Crypto.getInstance().getPrivateKey(email);
			if (privateKey.isPresent()) {
				try {
					e.subject = Crypto.decrypt(privateKey.get(), e.subject);
					e.body = Crypto.decrypt(privateKey.get(), e.body);
				} catch (CharacterCodingException ex) {
					output.append(
							"<b style='color: grey'>Email not encrypted or encrypted with a foreign public " +
									"key.</b>");
				}
			} else {
				output.append("<p style='color: red'>Private key not found for decryption of email.</p>");
			}
			// Check existence of signature
			if(e.signature == null) {
				output.append("<b style='color: grey'>Unsigned</b>");
			} else {
				// Verify signature
				try {
					if(Crypto.getInstance().isSignatureValid(e.sender, e.subject, e.body, e.signature, jwt)) {
						output.append("<b style='color: blue'>Valid signature</b>");
					} else {
						output.append("<b style='color: red'>Invalid signature</b>");
					}
				} catch (IOException ex) {
					output.append("<b style='color: orange'>Unknown signature</b>");
				}
			}
			// Content of email
			output.append("<div style=\"white-space: pre-wrap;\"><span style=\"color:grey;\">")
					.append("FROM:&emsp;").append(e.sender).append("&emsp;&emsp;AT:&emsp;")
					.append(e.time).append("</span><br><b>").append(e.subject)
					.append("</b>\r\n<br>").append(e.body)
					.append("</div>\n<hr style=\"border-top: 2px solid black;\">\r\n");
		}
		if (amount == 0) {
			output.append("<p><b>Your inbox is empty</b></p>");
		}
		output.append("</div>");
		return output.toString();
	}

}
