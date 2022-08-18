package client.servlet;

import client.crypto.Crypto;
import client.server.Server;
import client.util.ClientLogger;
import client.util.UnauthorizedException;
import http.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/LoginServlet")
public class LoginServlet extends HttpServlet {

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("text/html");

		String email = request.getParameter("email");
		String pwd = request.getParameter("password");
		User user = new User(email, pwd);

		try {
			String jwt = Server.getInstance().login(user);
			ClientLogger.println("Login succeeded!");
			Crypto.setJwtCookie(response, jwt);
			request.setAttribute("email", user.email);
			request.setAttribute("password", user.password);
			request.setAttribute("content", "Welcome!");
			request.getRequestDispatcher("home.jsp").forward(request, response);
			return;
		} catch(UnauthorizedException ignored) {

		} catch (IOException e) {
			e.printStackTrace();
		}

		// On error, stay on the login page
		request.getRequestDispatcher("login.html").forward(request, response);
	}

}
