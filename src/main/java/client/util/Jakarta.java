package client.util;

import jakarta.servlet.http.HttpServletResponse;

import java.util.Date;

public class Jakarta {

	public static void disableCaching(HttpServletResponse response) {
		response.setHeader("Expires", new Date(0).toString());
		response.setHeader("Last-Modified", new Date().toString());
		response.setHeader("Cache-Control",
				"max-age=0, no-cache, must-revalidate, proxy-revalidate");

	}

}
