package lib;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Java配置 {
    public static void main(String[] args) throws IOException {
        InputStream is=Java配置 .class.getClassLoader()
                .getResourceAsStream("some.properties");
        /**
         * SomeClass.class.getClassLoader().getResourceAsStream()
         * 相对于类编译出的类所在的根路径找的
         */
        Properties properties=new Properties();
        properties.load(is);
       String v=properties.getProperty("mysql.host");
        System.out.println(v);
    }
}
