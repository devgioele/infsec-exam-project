package util;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Sanitize {

	private static final Pattern regexEmail = Pattern.compile("^[a-zA-Z0-9_!#$%&*+/=?`~^.-]+@[a-zA-Z0-9.-]+$", Pattern.CASE_INSENSITIVE);

	/**
	 * Uses the RFC 5322 validation of emails, but does not allow the characters `|` and `'`.
	 * @param str The email to be verified.
	 * @return Whether the given email is valid.
	 */
	public static boolean isEmail(String str) {
		if(str == null) return false;
		Matcher m = regexEmail.matcher(str);
		return m.find();
	}

	public static String noHtml(String str) {
		if(str == null) return null;
		return Jsoup.clean(str, Safelist.none());
	}

	public static String safeHtml(String str) {
		if(str == null) return null;
		return Jsoup.clean(str, Safelist.relaxed());
	}

}
