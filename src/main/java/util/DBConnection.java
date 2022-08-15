package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBConnection {

    private static final String USER = "worker";
    private static final String PWD = "P)0r893.mDlA923kwc2-l";
    private static final String DRIVER_CLASS = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
    private static final String DB_NAME = "dev";
    private static final String DB_URL = "jdbc:sqlserver://localhost:1433;databaseName=" + DB_NAME + ";encrypt=true;trustServerCertificate=true;";

    public static Connection create() {
        try {
            // Load driver
            Class.forName(DRIVER_CLASS);
            // Setup connection
            Properties connectionProps = new Properties();
            connectionProps.put("user", USER);
            connectionProps.put("password", PWD);
            // Connect
            return DriverManager.getConnection(DB_URL, connectionProps);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}
