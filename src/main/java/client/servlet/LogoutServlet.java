package client.servlet;

import crypto.Crypto;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/LogoutServlet")
public class LogoutServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		logout(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		logout(request, response);
	}

	private void logout(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		System.out.println("Clearing JWT");
		Crypto.clearJwt(response);
		request.getRequestDispatcher("login.html").forward(request, response);
	}

}