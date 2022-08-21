package http;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Email {

	public final String sender;
	public final String receiver;
	public final String signature;
	public String subject;
	public String body;
	public final String time;

	public Email(String sender, String receiver, String signature, String subject, String body,
				 String time) {
		this.sender = sender;
		this.receiver = receiver;
		this.signature = signature;
		this.subject = subject;
		this.body = body;
		this.time = time;
	}

	public static Email fromSql(ResultSet sqlRes) throws SQLException {
		String sender = sqlRes.getString(1);
		String receiver = sqlRes.getString(2);
		String signature = sqlRes.getString(3);
		String subject = sqlRes.getString(4);
		String body = sqlRes.getString(5);
		String time = sqlRes.getString(6);
		return new Email(sender, receiver, signature, subject, body, time);
	}

}
