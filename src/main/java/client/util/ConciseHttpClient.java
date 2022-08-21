package client.util;

import client.crypto.Crypto;
import org.jsoup.HttpStatusException;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;

public class ConciseHttpClient {

	private final HttpClient client = HttpClient.newHttpClient();
	private final String baseUrl;

	public ConciseHttpClient(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	private String sendRequest(HttpRequest request) throws IOException, UnauthorizedException {
		// Send
		HttpResponse<String> response;
		try {
			response = client.send(request, HttpResponse.BodyHandlers.ofString());
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		String body = response.body();
		// If successful
		if (response.statusCode() >= 200 && response.statusCode() < 400) {
			return body;
		}
		// Else if unauthorized
		else if (response.statusCode() == 401) {
			throw new UnauthorizedException();
		}
		throw new HttpException(request.uri().toString(), request.method(), response.statusCode(),
				body);
	}

	private HttpRequest.Builder compileRequest(String path, String jwt,
											   Map<String, String> params) {
		String paramsEncoded = "";
		if (params != null) {
			paramsEncoded = params.entrySet().stream().map(e -> e.getKey() + "=" +
							URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
					.collect(Collectors.joining("&"));
		}
		if (!paramsEncoded.isEmpty()) {
			paramsEncoded = "?" + paramsEncoded;
		}
		HttpRequest.Builder builder = HttpRequest.newBuilder();
		if (jwt != null) {
			builder = Crypto.setJwtHeader(builder, jwt);
		}
		return builder.uri(URI.create(baseUrl + "/" + path + paramsEncoded));
	}

	public String get(String path, String jwt, Map<String, String> params)
			throws IOException, UnauthorizedException {
		HttpRequest.Builder builder = compileRequest(path, jwt, params);
		return sendRequest(builder.GET().build());
	}

	public String get(String path, String jwt) throws IOException, UnauthorizedException {
		return get(path, jwt, null);
	}

	public String post(String path, String jwt, Map<String, String> params, String body)
			throws IOException, UnauthorizedException {
		HttpRequest.Builder builder = compileRequest(path, jwt, params);
		builder.header("Content-Type", "application/json; charset=utf-8");
		return sendRequest(builder.POST(HttpRequest.BodyPublishers.ofString(body)).build());
	}

	public String post(String path, String jwt, Map<String, String> params)
			throws IOException, UnauthorizedException {
		HttpRequest.Builder builder = compileRequest(path, jwt, params);
		return sendRequest(builder.POST(HttpRequest.BodyPublishers.noBody()).build());
	}

	public String post(String path, Map<String, String> params, String body)
			throws IOException, UnauthorizedException {
		return post(path, null, params, body);
	}

	public String post(String path, Map<String, String> params)
			throws IOException, UnauthorizedException {
		return post(path, null, params);
	}

	public String post(String path, String jwt) throws IOException, UnauthorizedException {
		return post(path, jwt, null);
	}

}
