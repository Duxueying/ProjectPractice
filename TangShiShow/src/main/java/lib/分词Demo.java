package lib;

import java.util.List;

import org.ansj.domain.Term;
import  org.ansj.splitWord.analysis.NlpAnalysis;
public class 分词Demo {
    public static void main(String[] args) {
        String sentence="中华人民共和国成立了！中国人民从此站起来了";
       List<Term> termlist=NlpAnalysis.parse(sentence).getTerms();
       for(Term term:termlist){
           System.out.println(term.getNatureStr()+":"+term.getRealName());
       }
    }
}
