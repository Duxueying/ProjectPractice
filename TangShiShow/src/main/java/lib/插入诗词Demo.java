package lib;

import com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class 插入诗词Demo {
    public static void main(String[] args) throws ClassNotFoundException, SQLException {
        String 朝代="唐代";
        String 作者="王维";
        String 标题="相思";
        String 正文="红豆生南国，春来发几枝。愿君多采撷，此物最相思。";
        //1.注册Driver
        //2.获取Connection通过DriverManager
//        Class.forName("com.mysql.jdbc.Driver");
//        String url="jdbc:mysql://127.0.0.1/2020-1-9-唐诗?useSSL=false&characterEncoding=utf8";
//        Connection connection= DriverManager.getConnection(url,"root","mysql");
//        System.out.println(connection);

        //通过DataSource 获取Connection
        //这个不带线程池
        //DataSource dataSource1=new MysqlDataSource();
        //这个带有连接池，好处参照线程池
        //DataSource datasource2=new MysqlConnectionPoolDataSource();
        MysqlConnectionPoolDataSource datasource=new MysqlConnectionPoolDataSource();
        datasource.setServerName("127.0.0.1");
        datasource.setPort(3306);
        datasource.setUser("root");
        datasource.setPassword("mysql");
        datasource.setDatabaseName("2020-1-9-唐诗");
        datasource.setUseSSL(false);
        datasource.setCharacterEncoding("UTF-8");

      try( Connection connection= datasource.getConnection()){
          String sql="INSERT INTO tangpoetry(sha256, dynasty,title,author,content,words)values(?,?,?,?,?,?)";
         try( PreparedStatement statement=connection.prepareStatement(sql)) {
             statement.setString(1, "sha256");
             statement.setString(2, 朝代);
             statement.setString(3, 标题);
             statement.setString(4, 作者);
             statement.setString(5, 正文);
             statement.setString(6, "");
             statement.executeUpdate();
         }
      }
    }
}
