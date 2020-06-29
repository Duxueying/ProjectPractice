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

        //�޽�����������HTTP�ͻ��ˣ�,��װ�Լ��ǹȸ������
        WebClient webClient=new WebClient(BrowserVersion.CHROME);
        //���ܵ���������д��css��js����׼�����лᱨ���棬�����ȹ��ˡ��ر������cssִ������
        webClient.getOptions().setCssEnabled(false);
        webClient.getOptions().setJavaScriptEnabled(false);
        //��url�Ž�ȥ�����᷵��HtmlPage���͵�����
        HtmlPage page = webClient.getPage("https://so.gushiwen.org/gushi/tangshi.aspx");

        //�����б�ҳhtml�ĵ����ŵ��ļ���
        page.save(new File("��ʫ������\\�б�ҳ.html"));

        //html��body
        HtmlElement body=page.getBody();

        //ɸѡ���������������ݵı�ǩ�������ǩ�����ݣ�3��������Ԫ�����ƣ���ǩ���ơ���ǩֵ��
        List<HtmlElement> elements = body.getElementsByAttribute("div",
                "class",
                "typecont");

        HtmlElement divElement = elements.get(0);

        List<HtmlElement> aElement = divElement.getElementsByAttribute("a",
                "target",
                "_blank");

        //һ������ҳ��URL
        String str=aElement.get(0).getAttribute("href");
    }
}
