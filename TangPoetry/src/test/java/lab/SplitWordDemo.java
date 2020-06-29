package lab;
import org.ansj.domain.Term;
import org.ansj.splitWord.analysis.NlpAnalysis;

import java.util.List;

/**
 * @author yolo
 * @date 2020/1/13-17:19
 */
public class SplitWordDemo {
    public static void main(String[] args)
    {
        String sentence="中华人民中华人民共和国成立了！中国人民从此站起来了。";
        /*
        AnsjSeg提供了四种分词调用的方式：
            ①基本分词(BaseAnalysis)
            ②精准分词(ToAnalysis)
            ③NLP分词(NlpAnalysis)
            ④面向索引分词(IndexAnalysis)
          NLP分词方式可是未登录词，但速度较慢：
          NlpAnalysis.parse(sentence)--分词,一个terms就是一个单词
          public List<Term> getTerms() {return this.terms;}--类型转换

         */

        List<Term> termList= NlpAnalysis.parse(sentence).getTerms();
        for (Term term : termList) {
            //词性和词名
            System.out.println(term.getNatureStr()+":"+term.getRealName());
        }
    }
}
