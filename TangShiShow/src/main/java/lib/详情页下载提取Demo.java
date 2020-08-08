package lib;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomText;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import java.io.IOException;

/**
 * 查询每个作者的诗词数量
 * 给出字的使用频率【分词需求】ansj_seg（方便）  结巴分词
 */
public class 详情页下载提取Demo {
    public static void main(String[] args) throws IOException {
        try(WebClient webclient=new WebClient(BrowserVersion.CHROME)) {
            webclient.getOptions().setJavaScriptEnabled(false);
            //关闭了浏览器的css执行引擎，不再执行css布局
            webclient.getOptions().setCssEnabled(false);

            String url = "https://so.gushiwen.org/shiwenv_f324eea45183.aspx";
            HtmlPage page = webclient.getPage(url);
            HtmlElement body = page.getBody();
        /*
       List<HtmlElement> elements= body .getElementsByAttribute("div","class","contson");
        for(HtmlElement e:elements){
            System.out.println(e);
        }
        //拿到正文信息
        System.out.println(elements.get(0).getTextContent().trim());
        */
            //标题
            {
                String xpath = "//div[@class='cont']/h1/text()";
                Object o = body.getByXPath(xpath).get(0);//取第一个h1
                DomText domtext = (DomText) o;
                System.out.println(domtext.asText());
            }
            //朝代
            {
                String xpath = "//div[@class='cont']/p[@class='source']/a[1]/text()";
                Object o = body.getByXPath(xpath).get(0);//取第一个h1
                DomText domtext = (DomText) o;
                System.out.println(domtext.asText());
            }
            //作者
            {
                String xpath = "//div[@class='cont']/p[@class='source']/a[2]/text()";
                Object o = body.getByXPath(xpath).get(0);//取第一个h1
                DomText domtext = (DomText) o;
                System.out.println(domtext.asText());
            }
            //正文
            {
                String xpath = "//div[@class='cont']/div[@class='contson']";
                Object o = body.getByXPath(xpath).get(0);//取第一个h1
                HtmlElement element = (HtmlElement) o;
                System.out.println(element.getTextContent().trim());
            }
        }

    }
}
