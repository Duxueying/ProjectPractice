/*
规定如果是插入重复的数据，用异常处理，引入线程池
          ②进程无法结束，鼠标一直在闪动
          ----JVM 结束的条件：所有的非后台线程结束，才会结束
              而线程池中的线程是不会结束的
              解决办法：等所有的诗都上传数据库成功后，调用 pool.shutdown();
              让线程池显示结束即可
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

import java.security.MessageDigest;
import java.sql.PreparedStatement;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadPoolCatch {
    private static class Job implements Runnable{
        private String url;                      // 详情页的 url
        private DataSource dataSource;          // 数据库数据源
        private CountDownLatch countDownLatch;  //用来记录所有诗什么时候下载结束
        public Job(String url , DataSource dataSource,CountDownLatch countDownLatch) {
            this.url = url;
            this.dataSource = dataSource;
            this.countDownLatch=countDownLatch;//初始值是 320
        }

        public void run(){
            WebClient webClient=new WebClient(BrowserVersion.CHROME);
            webClient.getOptions().setJavaScriptEnabled(false);
            webClient.getOptions().setCssEnabled(false);

            try{
                MessageDigest messageDigest=MessageDigest.getInstance("SHA-256");    // SHA-256 哈希算法
                //MessageDigest 不是线程安全的，

                HtmlPage page=webClient.getPage(url);    //请求详情页
                String xpath;
                DomText domText;

                //标题
                xpath="//div[@class='cont']/h1/text()";
                domText=(DomText)page.getBody().getByXPath(xpath).get(0);   //h1 标签可能有很多个，我们只取第一个 h1 标签
                String title=domText.asText();

                //朝代
                xpath="//div[@class='cont']/p[@class='source']/a[1]/text()";
                domText=(DomText)page.getBody().getByXPath(xpath).get(0);   //第一个 a 标签对应的值
                String dynasty=domText.asText();

                //作者
                xpath="//div[@class='cont']/p[@class='source']/a[2]/text()";
                domText=(DomText)page.getBody().getByXPath(xpath).get(0);   //第二个 a 标签对应的值
                String author=domText.asText();

                //正文
                xpath="//div[@class='cont']/div[@class='contson']";
                HtmlElement element=(HtmlElement)page.getBody().getByXPath(xpath).get(0);   //只取第一个
                String content=element.getTextContent().trim();

                //计算 SHA-256 的值
                String s=title+content;         //利用 标题和正文 计算 SHA-256 的值
                //  messageDigest.update（）; 方法要求传入一个 byte[] 类型的数组
                messageDigest.update(s.getBytes("UTF-8"));   //先把要加密的内容放进去
                byte[] result=messageDigest.digest();       //得到加密后的值

                StringBuilder sha256=new StringBuilder();
                for(byte b:result){
                    sha256.append(String.format("%02x",b));    //UTF-8 一个字节占两位
                }

                //4.计算分词
                List<Term> termList=new ArrayList<>();
                termList.addAll(NlpAnalysis.parse(title).getTerms());//根据标题和内容进行分词
                termList.addAll(NlpAnalysis.parse(content).getTerms());//NLP返回list

                List<String> words=new ArrayList<>();  //存储最终的词
                for(Term term:termList){
                    if(term.getNatureStr().equals("w")){
                        continue;    //标点符号不要
                    }
                    if(term.getNatureStr().equals("null")){
                        continue;    //null不要
                    }
                    if(term.getRealName().length()<2){
                        continue;    //长度小于2 的词不要
                    }
                    words.add(term.getRealName());
                }
                String insertWords=String.join(",",words);  //把 List 列表里面的内容用 ,连接

                //往数据库中插入数据
                try(Connection connection=dataSource.getConnection()){
                    //Connection 不是线程安全的

                    String sql="insert into tangshi(sha256,dynasty,author,title,content,words) values(?,?,?,?,?,?)";
                    try(PreparedStatement statement=connection.prepareStatement(sql)){
                        //PreparedStatement 不是线程安全的，每个线程创建自己的 PreparedStatement 对象就可保证线程安全
                        //prepareStatament的性能快，绑定sql,
                        // 有预编译的功能，而且代码的可读性好还安全
                        statement.setString(1, sha256.toString());     //sha256 的原类型是 String Builder
                        statement.setString(2, dynasty);
                        statement.setString(3, author);
                        statement.setString(4, title);
                        statement.setString(5, content);
                        statement.setString(6, insertWords);
                        statement.executeUpdate();                   //往数据库插入一行数据

                        com.mysql.jdbc.PreparedStatement mysqlStatement=(com.mysql.jdbc.PreparedStatement)statement;
                        System.out.println(mysqlStatement.asSql());     //打印刚刚插入的数据内容


                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (SQLException e) {    //数据库异常
                if(!e.getMessage().contains("Duplicate entry")){  //重复插入对我们来说不是错误，不用打印错误信息
                    e.printStackTrace();
                }
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }finally {
                countDownLatch.countDown();   //个数减1
            }
        }
    }

    public static void main(String[] args) throws Exception {
        //创建一个固定大小的线程池
        ExecutorService pool=Executors.newFixedThreadPool(30);
        //指定工作线程大小的线程池
        WebClient webClient=new WebClient(BrowserVersion.CHROME);
        webClient.getOptions().setJavaScriptEnabled(false);
        webClient.getOptions().setCssEnabled(false);

        String baseUrl="https://so.gushiwen.org";  //列表页的前半部分 url
        String pathUrl="/gushi/tangshi.aspx";      //列表页的后半部分 url

        //存储每一个详情页的完整 URL
        List<String> detailUrlList=new ArrayList<>();


        {
            String url=baseUrl+pathUrl;             //列表页的 URL
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
                    String detailUrl=a.getAttribute("href");    //返回 href 属性对应的值（即：详情页的后半部分 url）
                    detailUrlList.add(baseUrl+detailUrl);  //将每一个详情页的完整 url 加入到 List 列表中
                }
            }
        }

        //建立数据源
        MysqlConnectionPoolDataSource dataSource=new MysqlConnectionPoolDataSource();  //这个带有连接池
        dataSource.setServerName("127.0.0.1");
        dataSource.setPort(3306);
        dataSource.setUser("root");
        dataSource.setPassword("98112dxyxc1017");
        dataSource.setDatabaseName("tangshi");
        dataSource.setUseSSL(false);
        dataSource.setCharacterEncoding("UTF-8");

        System.out.println("一共有多少首诗？"+detailUrlList.size());             //320
        CountDownLatch countDownLatch=new CountDownLatch(detailUrlList.size());   //诗的个数（ 320 ），根据详情页 URL 的数量确定

        for(String url:detailUrlList){
            pool.execute(new Job(url,dataSource,countDownLatch));
        }

        //JVM结束条件：所有的非后台线程结束它才会结束

        countDownLatch.await();    //等待 320 首诗都成功上传到数据库(一直等到 countDownLatch 对象里面的属性值为 0，才可以执行接下来的语句)
        pool.shutdown();           //关闭线程池
    }
}