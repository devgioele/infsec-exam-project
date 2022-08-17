package server.util;

import java.sql.*;
import java.util.Properties;

public class Database {

	private static final String USER = "worker";
	private static final String PWD = "P)0r893.mDlA923kwc2-l";
	private static final String DRIVER_CLASS = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
	private static final String DB_NAME = "dev";
	private static final String DB_URL =
			"jdbc:sqlserver://localhost:1433;databaseName=" + DB_NAME +
			";encrypt=true;trustServerCertificate=true;";

	public static Connection newConnection() {
		try {
			// Load driver
			Class.forName(DRIVER_CLASS);
			// Setup connection
			Properties connectionProps = new Properties();
			connectionProps.put("user", USER);
			connectionProps.put("password", PWD);
			// Connect
			return DriverManager.getConnection(DB_URL, connectionProps);
		} catch (ClassNotFoundException ex) {
			ServerLogger.printlnErr("The driver for SQL Server could not be loaded.\n" + ex);
		} catch(SQLException ex) {
			ServerLogger.printlnErr("Could not connect to the database.\n" + ex);
		}
		return null;
	}

	private static PreparedStatement compileStatement(Connection conn, String sql,
													  String... params)
			throws SQLException {
		PreparedStatement st = conn.prepareStatement(sql);
		// Set parameters
		for (int i = 0; i < params.length; i++) {
			// DEBUG: ServerLogger.printf("SQL: %s%nIndex: %s%nParam: %s%n", sql, i + 1, params[i]);
			st.setString(i + 1, params[i]);
		}
		return st;
	}

	/**
	 * Executes the given query and keeps the statement object open.
	 */
	public static ResultSet query(Connection conn, String sql, String... params)
			throws SQLException {
		return compileStatement(conn, sql, params).executeQuery();
	}

	/**
	 * Executes the given update and closes the statement object.
	 */
	public static void update(Connection conn, String sql, String... params) throws SQLException {
		try (var st = compileStatement(conn, sql, params)) {
			st.executeUpdate();
		}
	}

}
