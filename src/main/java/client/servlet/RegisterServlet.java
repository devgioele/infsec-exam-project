package client.servlet;

import client.crypto.Crypto;
import client.server.ServerException;
import client.server.Server;
import client.util.ClientLogger;
import http.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/RegisterServlet")
public class RegisterServlet extends HttpServlet {

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("text/html");

		String name = request.getParameter("name");
		String surname = request.getParameter("surname");
		String email = request.getParameter("email");
		String pwd = request.getParameter("password");
		User user = new User(name, surname, email, pwd);

		try {
			String jwt = Server.getInstance().register(user);
			ClientLogger.println("Registration succeeded!");
			Crypto.setJwtCookie(response, jwt);
			request.getRequestDispatcher("home.jsp").forward(request, response);
			return;
		} catch (ServerException e) {
			ClientLogger.println(e.getMessage());
		}

		// On error, stay on the registration page
		request.getRequestDispatcher("register.html").forward(request, response);
	}

}
