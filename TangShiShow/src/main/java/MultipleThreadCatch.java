/*
    ���߳�ִ���ٶ�̫������Ϊ���̡߳��б�ҳ������ͽ���������Ŀִֻ��һ�Σ����Է������߳���ȥ����������ҳ������ͽ�����Ҫִ��320�Σ��ڶ��߳���ȥ����ͬ����ȡʫ����Ϣ�����⣬���ߣ����ݣ������� sha256 ��ֵ������ִʡ���Ϣ�������ݿ�Ȳ���ŵ����߳���ȥ��
    ��������⣺�̲߳���ȫ��webclient messagedigest datasource���̲߳���ȫ�������Ļ��ﲻ����Ч�ʵ�Ŀ�ģ���ÿ���߳̽����Լ��Ķ���
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
    //�Զ����һ���߳��࣬�̳�Runnable�ӿ�
    private static class Job implements Runnable{
        private String url;               //����ҳ�� url
        private DataSource dataSource;   //���ݿ�����Դ��// �����γ�ʼ���������߳���ֱ�Ӵ���

        public Job(String url, DataSource dataSource) {
            this.url = url;
            this.dataSource = dataSource;
        }

        //�߳���
        public void run(){
            // WebClient �̲߳���ȫ����취��ÿ���̴߳����Լ���webclient����
            WebClient webClient=new WebClient(BrowserVersion.CHROME);
            webClient.getOptions().setJavaScriptEnabled(false);
            webClient.getOptions().setCssEnabled(false);

            try{
                HtmlPage page=webClient.getPage(url);  //��������ҳ
                String xpath;
                DomText domText;

                //����
                xpath="//div[@class='cont']/h1/text()";
                domText=(DomText)page.getBody().getByXPath(xpath).get(0);    //ֻȡ List �б�ĵ�һ������Ϊ h1 ��ǩ������ body �в�ֹһ��
                String title=domText.asText();

                //��������һ�� a ��ǩ��
                xpath="//div[@class='cont']/p[@class='source']/a[1]/text()";
                domText=(DomText)page.getBody().getByXPath(xpath).get(0);   //ֻȡ��һ��
                String dynasty=domText.asText();

                //���ߣ��ڶ��� a ��ǩ��
                xpath="//div[@class='cont']/p[@class='source']/a[2]/text()";
                domText=(DomText)page.getBody().getByXPath(xpath).get(0);   //ֻȡ��һ��
                String author=domText.asText();

                //����
                xpath="//div[@class='cont']/div[@class='contson']";
                HtmlElement element=(HtmlElement)page.getBody().getByXPath(xpath).get(0);   //ֻȡ��һ��
                String content=element.getTextContent().trim(); //ȥ���������ߵĿո�

                //���� SHA-256 ��ϣ�㷨
                MessageDigest messageDigest=MessageDigest.getInstance("SHA-256");

                //���� SHA-256 ��ֵ
                String s=title+content;   //���ñ�������� ���� SHA-256 ��ֵ
                messageDigest.update(s.getBytes("UTF-8"));
                byte[] result=messageDigest.digest();//�洢���ܺ��ֵ
                StringBuilder sha256=new StringBuilder();
                for(byte b:result){
                    sha256.append(String.format("%02x",b));           //UTF-8 һ���ֽ�ռ��λ
                }

                //��ִ�
                List<Term> termList=new ArrayList<>();
                termList.addAll(NlpAnalysis.parse(title).getTerms());  //���ݱ�������Ľ��зִ�
                termList.addAll(NlpAnalysis.parse(content).getTerms());

                List<String> words=new ArrayList<>();  //�洢���յĴ�
                for(Term term:termList){
                    if(term.getNatureStr().equals("w")){
                        continue;  //�����Ų�Ҫ
                    }
                    if(term.getNatureStr().equals("null")){
                        continue;  //null��Ҫ
                    }
                    if(term.getRealName().length()<2){
                        continue;  //����С��2 �Ĵʲ�Ҫ
                    }
                    words.add(term.getRealName());
                }
                String insertWords=String.join(",",words); //�� List �б������������ ,����

                //�����ݿ��в�������
                try(Connection connection=dataSource.getConnection()){
                    //Connection Ҳ���̰߳�ȫ�ģ�ÿ���̴߳����Լ��� Connection ����

                    String sql="insert into t_tangshi(sha256,dynasty,author,title,content,words) values(?,?,?,?,?,?)";
                    try(PreparedStatement statement=connection.prepareStatement(sql)){
                        //PreparedStatement �����̰߳�ȫ�ģ�ÿ���̴߳����Լ��� PreparedStatement ����Ϳɱ�֤�̰߳�ȫ
             //prepareStatament�����ܿ죬��sql,
            // ��Ԥ����Ĺ��ܣ����Ҵ���Ŀɶ��Ժû���ȫ
                        statement.setString(1, sha256.toString());    //sha256 ��ԭ������ String Builder
                        statement.setString(2, dynasty);
                        statement.setString(3, author);
                        statement.setString(4, title);
                        statement.setString(5, content);
                        statement.setString(6, insertWords);
                        statement.executeUpdate();                   //�����ݿ����һ������

                        com.mysql.jdbc.PreparedStatement mysqlStatement=(com.mysql.jdbc.PreparedStatement)statement;
                        System.out.println(mysqlStatement.asSql());    //��ӡ�ող������������
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

        String baseUrl="https://so.gushiwen.org";  //�б�ҳ��ǰ�벿�� url
        String pathUrl="/gushi/tangshi.aspx";      //�б�ҳ�ĺ�벿�� url

        //�洢ÿһ������ҳ������ URL
        List<String> detailUrlList=new ArrayList<>();

        //�б�ҳ������ͽ����������߳���ȥ��
        {
            String url=baseUrl+pathUrl;             //�б�ҳ������ URL
            HtmlPage page=webClient.getPage(url);   //�����б�ҳ
            List<HtmlElement> divs=page.getBody().getElementsByAttribute("div","class","typecont");

            /* divs �е����ݣ�
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

                /* as �е����ݣ�
                       HtmlAnchor[<a href="/shiwenv_45c396367f59.aspx" target="_blank">]
                       HtmlAnchor[<a href="/shiwenv_c90ff9ea5a71.aspx" target="_blank">]
                       HtmlAnchor[<a href="/shiwenv_5917bc6dca91.aspx" target="_blank">]
                       HtmlAnchor[<a href="/shiwenv_f324eea45183.aspx" target="_blank">]
                       HtmlAnchor[<a href="/shiwenv_8d889937d1fe.aspx" target="_blank">]
                 */

                for(HtmlElement a:as){
                    String detailUrl=a.getAttribute("href");   //��������ҳ�ĺ�벿�� url
                    detailUrlList.add(baseUrl+detailUrl);  //��ÿһ������ҳ������ url ���� List �б���
                }
            }
        }

        //�������ݿ�
        MysqlConnectionPoolDataSource dataSource=new MysqlConnectionPoolDataSource();  //����������ӳ�
        dataSource.setServerName("127.0.0.1");
        dataSource.setPort(3306);
        dataSource.setUser("root");
        dataSource.setPassword("981112dxyxc1017");
        dataSource.setDatabaseName("tangshi");
        dataSource.setUseSSL(false);
        dataSource.setCharacterEncoding("UTF-8");

        //����ҳ������ͽ����ŵ����̣߳�Ҫ���� 320 �Σ����Բ��ö��̷߳�ʽȥ�������
        for(String url:detailUrlList){
            Thread thread=new Thread(new Job(url,dataSource));
            //������ҳ�� url��SHA-256�㷨�����ݿ������Դ����ȥ

            thread.start();    //����һ���߳�
        }
    }
}