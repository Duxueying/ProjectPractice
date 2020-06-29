/*
    单线程执行速度太慢，改为多线程。列表页的请求和解析整个项目只执行一次，所以放在主线程中去做而；详情页的请求和解析需要执行320次，在多线程中去做，同理提取诗词信息（标题，作者，内容）、计算 sha256 的值、计算分词、信息插入数据库等步骤放到多线程中去做
    引入的问题：线程不安全，webclient messagedigest datasource都线程不安全，加锁的话达不到高效率的目的，让每个线程建立自己的对象，
 */
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomText;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource;
import org.ansj.domain.Term;
import org.ansj.splitWord.analysis.NlpAnalysis;

import javax.sql.DataSource;
import java.io.IOException;
import java.security.MessageDigest;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MultipleThreadCatch {
    //自定义的一个线程类，继承Runnable接口
    private static class Job implements Runnable{
        private String url;               //详情页的 url
        private DataSource dataSource;   //数据库数据源（// 不想多次初始化，从主线程中直接传）

        public Job(String url, DataSource dataSource) {
            this.url = url;
            this.dataSource = dataSource;
        }

        //线程体
        public void run(){
            // WebClient 线程不安全解决办法，每个线程创建自己的webclient对象，
            WebClient webClient=new WebClient(BrowserVersion.CHROME);
            webClient.getOptions().setJavaScriptEnabled(false);
            webClient.getOptions().setCssEnabled(false);

            try{
                HtmlPage page=webClient.getPage(url);  //请求详情页
                String xpath;
                DomText domText;

                //标题
                xpath="//div[@class='cont']/h1/text()";
                domText=(DomText)page.getBody().getByXPath(xpath).get(0);    //只取 List 列表的第一个，因为 h1 标签在整个 body 中不止一个
                String title=domText.asText();

                //朝代（第一个 a 标签）
                xpath="//div[@class='cont']/p[@class='source']/a[1]/text()";
                domText=(DomText)page.getBody().getByXPath(xpath).get(0);   //只取第一个
                String dynasty=domText.asText();

                //作者（第二个 a 标签）
                xpath="//div[@class='cont']/p[@class='source']/a[2]/text()";
                domText=(DomText)page.getBody().getByXPath(xpath).get(0);   //只取第一个
                String author=domText.asText();

                //正文
                xpath="//div[@class='cont']/div[@class='contson']";
                HtmlElement element=(HtmlElement)page.getBody().getByXPath(xpath).get(0);   //只取第一个
                String content=element.getTextContent().trim(); //去掉正文两边的空格

                //引入 SHA-256 哈希算法
                MessageDigest messageDigest=MessageDigest.getInstance("SHA-256");

                //计算 SHA-256 的值
                String s=title+content;   //利用标题和正文 计算 SHA-256 的值
                messageDigest.update(s.getBytes("UTF-8"));
                byte[] result=messageDigest.digest();//存储加密后的值
                StringBuilder sha256=new StringBuilder();
                for(byte b:result){
                    sha256.append(String.format("%02x",b));           //UTF-8 一个字节占两位
                }

                //算分词
                List<Term> termList=new ArrayList<>();
                termList.addAll(NlpAnalysis.parse(title).getTerms());  //根据标题和正文进行分词
                termList.addAll(NlpAnalysis.parse(content).getTerms());

                List<String> words=new ArrayList<>();  //存储最终的词
                for(Term term:termList){
                    if(term.getNatureStr().equals("w")){
                        continue;  //标点符号不要
                    }
                    if(term.getNatureStr().equals("null")){
                        continue;  //null不要
                    }
                    if(term.getRealName().length()<2){
                        continue;  //长度小于2 的词不要
                    }
                    words.add(term.getRealName());
                }
                String insertWords=String.join(",",words); //把 List 列表里面的内容用 ,连接

                //往数据库中插入数据
                try(Connection connection=dataSource.getConnection()){
                    //Connection 也是线程安全的，每个线程创建自己的 Connection 对象

                    String sql="insert into tangshi(sha256,dynasty,author,title,content,words) values(?,?,?,?,?,?)";
                    try(PreparedStatement statement=connection.prepareStatement(sql)){
                        //PreparedStatement 不是线程安全的，每个线程创建自己的 PreparedStatement 对象就可保证线程安全
             //prepareStatament的性能快，绑定sql,
            // 有预编译的功能，而且代码的可读性好还安全
                        statement.setString(1, sha256.toString());    //sha256 的原类型是 String Builder
                        statement.setString(2, dynasty);
                        statement.setString(3, author);
                        statement.setString(4, title);
                        statement.setString(5, content);
                        statement.setString(6, insertWords);
                        statement.executeUpdate();                   //往数据库插入一行数据

                        com.mysql.jdbc.PreparedStatement mysqlStatement=(com.mysql.jdbc.PreparedStatement)statement;
                        System.out.println(mysqlStatement.asSql());    //打印刚刚插入的数据内容
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (SQLException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws Exception {
        WebClient webClient=new WebClient(BrowserVersion.CHROME);
        webClient.getOptions().setJavaScriptEnabled(false);
        webClient.getOptions().setCssEnabled(false);

        String baseUrl="https://so.gushiwen.org";  //列表页的前半部分 url
        String pathUrl="/gushi/tangshi.aspx";      //列表页的后半部分 url

        //存储每一个详情页的完整 URL
        List<String> detailUrlList=new ArrayList<>();

        //列表页的请求和解析放在主线程中去做
        {
            String url=baseUrl+pathUrl;             //列表页的完整 URL
            HtmlPage page=webClient.getPage(url);   //请求列表页
            List<HtmlElement> divs=page.getBody().getElementsByAttribute("div","class","typecont");

            /* divs 中的内容：
                  HtmlDivision[<div class="typecont">]
                  HtmlDivision[<div class="typecont">]
                  HtmlDivision[<div class="typecont">]
                  HtmlDivision[<div class="typecont">]
                  HtmlDivision[<div class="typecont">]
                  HtmlDivision[<div class="typecont">]
                  HtmlDivision[<div class="typecont" style="border:0px;">]
             */

            for(HtmlElement div:divs){
                List<HtmlElement> as=div.getElementsByTagName("a");

                /* as 中的内容：
                       HtmlAnchor[<a href="/shiwenv_45c396367f59.aspx" target="_blank">]
                       HtmlAnchor[<a href="/shiwenv_c90ff9ea5a71.aspx" target="_blank">]
                       HtmlAnchor[<a href="/shiwenv_5917bc6dca91.aspx" target="_blank">]
                       HtmlAnchor[<a href="/shiwenv_f324eea45183.aspx" target="_blank">]
                       HtmlAnchor[<a href="/shiwenv_8d889937d1fe.aspx" target="_blank">]
                 */

                for(HtmlElement a:as){
                    String detailUrl=a.getAttribute("href");   //返回详情页的后半部分 url
                    detailUrlList.add(baseUrl+detailUrl);  //把每一个详情页的完整 url 加入 List 列表中
                }
            }
        }

        //连接数据库
        MysqlConnectionPoolDataSource dataSource=new MysqlConnectionPoolDataSource();  //这个带有连接池
        dataSource.setServerName("127.0.0.1");
        dataSource.setPort(3306);
        dataSource.setUser("root");
        dataSource.setPassword("981112dxyxc1017");
        dataSource.setDatabaseName("poetry");
        dataSource.setUseSSL(false);
        dataSource.setCharacterEncoding("UTF-8");

        //详情页的请求和解析放到多线程（要进行 320 次，所以采用多线程方式去完成任务）
        for(String url:detailUrlList){
            Thread thread=new Thread(new Job(url,dataSource));
            //把详情页的 url，SHA-256算法，数据库的数据源传进去

            thread.start();    //启动一个线程
        }
    }
}