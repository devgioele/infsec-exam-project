package client.servlet;

import client.crypto.Crypto;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import util.Sanitize;

import java.io.IOException;

@WebServlet("/NewMailServlet")
public class NewMailServlet extends HttpServlet {

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("text/html");

		if(!Crypto.isJwtValid(request)) {
			request.getRequestDispatcher("login.html").forward(request, response);
			return;
		}

		String email = Sanitize.noHtml(request.getParameter("email"));
		request.setAttribute("content", getContent(email));
		request.setAttribute("email", email);
		request.getRequestDispatcher("home.jsp").forward(request, response);
	}

	private String getContent(String email) {
		return "<form id=\"submitForm\" class=\"form-resize\" action=\"SendMailServlet\" " +
				"method=\"post\">\r\n<input type=\"hidden\" name=\"email\" value=\"" + email +
				"\">\r\n " +
				"		<input class=\"single-row-input\" type=\"email\" name=\"receiver\" " +
				"placeholder=\"Receiver\" required>\r\n" +
				"		<input class=\"single-row-input\" type=\"text\"  name=\"subject\" " +
				"placeholder=\"Subject\" required>\r\n" +
				"		<textarea class=\"textarea-input\" name=\"body\" placeholder=\"Body\" " +
				"wrap=\"hard\" required></textarea>\r\n" +
				"		<input type=\"submit\" name=\"sent\" value=\"Send\">\r\n" + "<span><a> " +
				"Sign digitally:</a><input type=\"checkbox\" name=\"digital-sign\"></span>\n" +
				"</form>";
	}

}
