package client.util;

public class ClientLogger {

	private static final String prefix = "[CLIENT] ";

	public static void printlnErr(String x) {
		printfErr("%s%n", x);
	}

	public static void printfErr(String format, Object... args) {
		System.err.printf("%s %s", prefix, format);
	}

	public static void println(String x) {
		printf("%s%n", x);
	}

	public static void printf(String format, Object... args) {
		System.out.printf("%s %s", prefix, format);
	}

}
