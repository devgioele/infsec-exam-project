package client.servlet;

import client.crypto.Crypto;
import client.server.Server;
import client.util.ClientLogger;
import client.util.HttpException;
import client.util.UnauthorizedException;
import client.crypto.RsaKey;
import http.JwtPayload;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Optional;

@WebServlet(name = "SendEmailServlet", urlPatterns = {"/email-send"})
public class SendEmailServlet extends HttpServlet {

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("text/html");

		String receiver = request.getParameter("receiver");
		String subject = request.getParameter("subject");
		String body = request.getParameter("body");
		boolean sign = request.getParameter("digital-sign") != null;

		String jwt = Crypto.extractJwtCookie(request);
		JwtPayload payload = Crypto.validJwt(jwt);

		if (payload != null) {
			try {
				request.setAttribute("content",
						getContent(jwt, payload.email, receiver, subject, body, sign));
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

	private String getContent(String jwt, String sender, String receiver, String subject,
							  String body, boolean sign) throws UnauthorizedException {
		// Sign digitally if asked so
		String signature = null;
		if (sign) {
			signature = Crypto.getInstance().sign(sender, subject, body);
			if(signature == null) {
				return "Could not digitally sign the email!";
			}
		}

		// Get public key of receiver
		Optional<RsaKey> publicKey;
		try {
			publicKey = Server.getInstance().getPublicKey(jwt, receiver);
		} catch (IOException e) {
			return "Cannot retrieve public key of receiver: " + e.getMessage();
		}
		if (publicKey.isEmpty()) {
			return "Could not find public key of receiver. Does the receiver '" + receiver +
					"' exist?";
		}

		// Encrypt
		subject = Crypto.encrypt(publicKey.get(), subject);
		body = Crypto.encrypt(publicKey.get(), body);

		try {
			Server.getInstance().sendEmail(jwt, receiver, subject, body, signature);
		} catch (IOException ex) {
			String msg = "Email could not be sent!";
			if (ex instanceof HttpException) {
				msg += "\n" + ((HttpException) ex).getUserMessage();
			}
			return msg;
		}
		return "Sent!";
	}

}