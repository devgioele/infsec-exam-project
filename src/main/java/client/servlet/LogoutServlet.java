package client.servlet;

import client.crypto.Crypto;
import client.util.ClientLogger;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/LogoutServlet")
public class LogoutServlet extends HttpServlet {

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("text/html");

		ClientLogger.println("Clearing JWT");
		Crypto.clearJwtCookie(response);
		request.getRequestDispatcher("login.html").forward(request, response);
	}

}