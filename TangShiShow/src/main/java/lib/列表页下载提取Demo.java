package lib;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import java.io.IOException;
import java.util.List;

public class 列表页下载提取Demo {
    public static void main(String[] args)throws IOException {
        //无界面的浏览器（HTTP）客户端,用try是为了结束之后自动关闭
       try( WebClient webclient=new WebClient(BrowserVersion.CHROME)) {
           //关闭了浏览器的js执行引擎,不再执行网页中的js脚本
           webclient.getOptions().setJavaScriptEnabled(false);
           //关闭了浏览器的css执行引擎，不再执行css布局
           webclient.getOptions().setCssEnabled(false);
           //列表页的访问功能
           HtmlPage page = webclient.getPage("https://so.gushiwen.org/gushi/tangshi.aspx");
           //page.save(new File("唐诗三百首\\列表页.html"));
           HtmlElement body = page.getBody();
           //标签，属性，值
          List<HtmlElement> elements = body.getElementsByAttribute("div", "class", "typecont");
           int count = 0;
           for (HtmlElement e : elements) {
               List<HtmlElement> aelements = e.getElementsByTagName("a");//获取每首诗的标签
               for (HtmlElement ae : aelements) {
                   System.out.println(ae.getAttribute("href"));//获取的是每首诗的详情页url
                   count++;
               }
           }
           System.out.println(count);
     }
    }
}
