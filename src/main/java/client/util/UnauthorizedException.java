package client.util;


public class UnauthorizedException extends Exception {

	public UnauthorizedException() {
		super("401 - unauthorized");
	}

}
