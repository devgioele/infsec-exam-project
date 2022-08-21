package client.util;

import http.HttpError;
import util.Convert;

import java.io.IOException;

public class HttpException extends IOException {

	private String body;

	public HttpException(Exception e) {
		super("Unexpected server response: " + e.getMessage());
	}

	public HttpException(String url, String method, int statusCode, String body) {
		super("Failed HTTP " + method + " to " + url + " with status code " + statusCode +
				"\nBody:\n" + body);
		this.body = body;
	}

	public String getUserMessage() {
		if (body == null) return getMessage();
		HttpError error = Convert.gson.fromJson(body, HttpError.class);
		return error.message;
	}

}
