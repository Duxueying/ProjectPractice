package lab;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * author dxy
 */
public class HtmlUnitDemo {
    public static void main(String[] args) throws IOException {

        //无界面的浏览器（HTTP客户端）,假装自己是谷歌浏览器
        WebClient webClient=new WebClient(BrowserVersion.CHROME);
        //可能第三方库里写的css和js不标准，运行会报警告，所以先关了。关闭浏览器css执行引擎
        webClient.getOptions().setCssEnabled(false);
        webClient.getOptions().setJavaScriptEnabled(false);
        //把url放进去，它会返回HtmlPage类型的数据
        HtmlPage page = webClient.getPage("https://so.gushiwen.org/gushi/tangshi.aspx");

        //保存列表页html文档，放到文件里
        page.save(new File("唐诗三百首\\列表页.html"));

        //html的body
        HtmlElement body=page.getBody();

        //筛选包含我们所需内容的标签，保存标签的内容，3个参数：元素名称，标签名称。标签值，
        List<HtmlElement> elements = body.getElementsByAttribute("div",
                "class",
                "typecont");

        HtmlElement divElement = elements.get(0);

        List<HtmlElement> aElement = divElement.getElementsByAttribute("a",
                "target",
                "_blank");

        //一个详情页的URL
        String str=aElement.get(0).getAttribute("href");
    }
}
