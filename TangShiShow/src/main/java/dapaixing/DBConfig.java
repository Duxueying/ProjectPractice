package dapaixing;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class DBConfig {
    private static final String host = "127.0.0.1";
    private static final int port = 3306;
    private static final String user = "root";
//    private static final String password = "mysql";
    private static final String password = "LMJian521";
    private static final String database = "2020-1-9-唐诗";

    // 用的饿汉模式的单例
    private static final DataSource dataSource;
    static {
        MysqlDataSource mysqlDataSource = new MysqlDataSource();
        mysqlDataSource.setServerName(host);
        mysqlDataSource.setPort(port);
        mysqlDataSource.setUser(user);
        mysqlDataSource.setPassword(password);
        mysqlDataSource.setDatabaseName(database);
        mysqlDataSource.setUseSSL(false);
        mysqlDataSource.setCharacterEncoding("utf8");

        dataSource = mysqlDataSource;
    }

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
}
