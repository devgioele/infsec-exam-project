package client.server;

import org.jsoup.HttpStatusException;

public class UnauthorizedException extends HttpStatusException {

	public UnauthorizedException(String message, String url) {
		super(message, 401, url);
	}

}
