package client.servlet;

import client.crypto.Crypto;
import client.util.HttpException;
import client.util.UnauthorizedException;
import client.crypto.RsaKey;
import client.server.Server;
import client.util.ClientLogger;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(name = "RegisterServlet", urlPatterns = {"/register"})
public class RegisterServlet extends HttpServlet {

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("text/html");

		String name = request.getParameter("name");
		String surname = request.getParameter("surname");
		String email = request.getParameter("email");
		String pwd = request.getParameter("password");

		ClientLogger.println("Generating RSA keys");
		RsaKey publicKey = Crypto.getInstance().genKeyPair(email);

		try {
			String jwt = Server.getInstance().register(name, surname, email, pwd, publicKey);
			ClientLogger.println("Registration succeeded!");
			Crypto.setJwtCookie(response, jwt);
			request.setAttribute("email", email);
			request.getRequestDispatcher("home.jsp").forward(request, response);
			return;
		} catch (HttpException e) {
			ClientLogger.printlnErr("Registration failed:\n" + e.getUserMessage());
		} catch (IOException e) {
			e.printStackTrace();
		} catch (UnauthorizedException e) {
			ClientLogger.printlnErr("Registration failed:\n" + e.getMessage());
		}

		// On error, stay on the registration page
		request.getRequestDispatcher("register.html").forward(request, response);
	}

}
