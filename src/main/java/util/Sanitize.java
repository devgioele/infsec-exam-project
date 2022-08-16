package util;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Sanitize {

	private static Pattern regexEmail = Pattern.compile("^[a-zA-Z0-9_!#$%&*+/=?`~^.-]+@[a-zA-Z0-9.-]+$", Pattern.CASE_INSENSITIVE);

	/**
	 * Uses the RFC 5322 validation of emails, but does not allow the characters `|` and `'`.
	 * @param email The email to be verified.
	 * @return Whether the given email is valid.
	 */
	public static boolean isEmail(String email) {
		Matcher m = regexEmail.matcher(email);
		return m.find();
	}

	public static String noHTML(String str) {
		return Jsoup.clean(str, Safelist.none());
	}

	public static String safeHTML(String str) {
		return Jsoup.clean(str, Safelist.relaxed());
	}

}
