package client.servlet;

import crypto.Crypto;
import jakarta.servlet.http.HttpServlet;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import util.Database;
import util.Sanitize;

@WebServlet("/NavigationServlet")
public class NavigationServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static Connection conn;

	public void init() {
		conn = Database.newConnection();
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("text/html");
		String email = Sanitize.noHTML(request.getParameter("email"));

		if(!Crypto.validJwt(request, email)) {
			request.getRequestDispatcher("login.html").forward(request, response);
		}

		if (request.getParameter("newMail") != null)
			request.setAttribute("content", getHtmlForNewMail(email));
		else if (request.getParameter("inbox") != null)
			request.setAttribute("content", getHtmlForInbox(email));
		else if (request.getParameter("sent") != null)
			request.setAttribute("content", getHtmlForSent(email));

		request.setAttribute("email", email);
		request.getRequestDispatcher("home.jsp").forward(request, response);
	}

	private String getHtmlForInbox(String email) {
		try (ResultSet sqlRes = Database.query(conn,
				"SELECT * FROM mail WHERE receiver=? ORDER BY [time] DESC", email)) {
			StringBuilder output = new StringBuilder();
			output.append("<div>\r\n");
			int amount = 0;
			while (sqlRes.next()) {
				amount++;
				output.append(
						"<div style=\"white-space: pre-wrap;\"><span style=\"color:grey;" + "\">");
				output.append("FROM:&emsp;").append(sqlRes.getString(1))
						.append("&emsp;&emsp;AT:&emsp;").append(sqlRes.getString(5));
				output.append("</span>");
				output.append("<br><b>").append(sqlRes.getString(3)).append("</b>\r\n");
				output.append("<br>").append(sqlRes.getString(4));
				output.append("</div>\r\n");

				output.append("<hr style=\"border-top: 2px solid black;\">\r\n");
			}
			if (amount == 0) {
				output.append("<p><b>Your inbox is empty</b></p>");
			}
			output.append("</div>");
			return output.toString();
		} catch (SQLException ex) {
			ex.printStackTrace();
			return "ERROR IN FETCHING INBOX MAILS!";
		}

	}

	private String getHtmlForNewMail(String email) {
		return "<form id=\"submitForm\" class=\"form-resize\" action=\"SendMailServlet\" " +
				"method=\"post\">\r\n<input type=\"hidden\" name=\"sender\" value=\"" + email +
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

	private String getHtmlForSent(String email) {
		try (ResultSet sqlRes = Database.query(conn,
				"SELECT * FROM mail WHERE sender=? ORDER BY [time] DESC", email)) {
			StringBuilder output = new StringBuilder();
			output.append("<div>\r\n");
			int amount = 0;
			while (sqlRes.next()) {
				amount++;
				output.append(
						"<div style=\"white-space: pre-wrap;\"><span style=\"color:grey;" + "\">");
				output.append("TO:&emsp;").append(sqlRes.getString(2))
						.append("&emsp;&emsp;AT:&emsp;").append(sqlRes.getString(5));
				output.append("</span>");
				output.append("<br><b>").append(sqlRes.getString(3)).append("</b>\r\n");
				output.append("<br>").append(sqlRes.getString(4));
				output.append("</div>\r\n");
				output.append("<hr style=\"border-top: 2px solid black;\">\r\n");
			}
			if (amount == 0) {
				output.append("<p><b>Nothing sent yet</b></p>");
			}
			output.append("</div>");
			return output.toString();
		} catch (SQLException e) {
			e.printStackTrace();
			return "ERROR IN FETCHING SENT MAILS!";
		}
	}

}
