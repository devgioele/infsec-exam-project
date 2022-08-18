package client.server;

import client.util.ClientLogger;
import client.util.ConciseHttpClient;
import client.util.UnauthorizedException;
import http.Email;
import http.Jwt;
import http.User;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static util.Convert.gson;
import static util.IO.jsonFromFile;
import static util.IO.jsonToFile;

public class Server {

	private static Server INSTANCE;

	private final ConciseHttpClient client;
	private final ServerConfig config;

	public static Server getInstance() {
		if (INSTANCE == null) {
			String pathConfig = Paths.get("client", "server.json").toAbsolutePath().toString();
			INSTANCE = new Server(pathConfig);
		}
		return INSTANCE;
	}

	public Server(String pathConfig) {
		ServerConfig config = jsonFromFile(pathConfig, ServerConfig.class);
		if (config == null) {
			ClientLogger.println("No server config found. Creating a template");
			config = new ServerConfig("http://localhost:8080/exam-project/server");
			jsonToFile(config, pathConfig);
		}
		client = new ConciseHttpClient(config.location);
		this.config = config;
	}

	public Email[] loadSentEmails(String jwt)
			throws IOException {
		String json = client.get("email/sent", jwt);
		return gson.fromJson(json, Email[].class);
	}

	public Email[] loadInbox(String jwt) throws IOException {
		String json = client.get("inbox", jwt);
		return gson.fromJson(json, Email[].class);
	}

	public void sendMail(String jwt, String receiver, String subject, String body)
			throws IOException {
		Map<String, String> params = new HashMap<>();
		params.put("receiver", receiver);
		params.put("subject", subject);
		params.put("body", body);
		client.post("email/send", jwt, params);
	}

	/**
	 * Registers the given user.
	 *
	 * @return The JWT generated by the server.
	 */
	public String register(User user) throws IOException {
		Map<String, String> params = new HashMap<>();
		params.put("name", user.name);
		params.put("surname", user.surname);
		params.put("email", user.email);
		params.put("password", user.password);
		String json = client.post("register", params);
		return gson.fromJson(json, Jwt.class).jwt;
	}

	public String login(User user) throws IOException {
		Map<String, String> params = new HashMap<>();
		params.put("email", user.email);
		params.put("password", user.password);
		String json = client.post("login", params);
		return gson.fromJson(json, Jwt.class).jwt;
	}

	public boolean isJwtValid(String jwt) {
		try {
			client.post("jwt/verify", jwt);
			return true;
		} catch (UnauthorizedException ex) {
			return false;
		} catch(IOException ex) {
			ex.printStackTrace();
			return false;
		}
	}

}
