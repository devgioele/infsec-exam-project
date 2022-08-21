package http;

public class JwtPayload {

	public final String email;
	public final long exp;

	public JwtPayload(String email, long exp) {
		this.email = email;
		this.exp = exp;
	}

}
