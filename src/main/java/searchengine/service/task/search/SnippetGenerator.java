package searchengine.service.task.search;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.*;

public class SnippetGenerator {
    public SnippetGenerator(Set<String> lemma, String textQuery) {
        this.lemma = lemma;
        this.textQuery = textQuery;
    }
    private final Set<String> lemma;
    private final String textQuery;
    private final Set<String> words = new HashSet<>();
    public String getSnippet(String text) {
        String resultSnippet = " ";
        String[] token = getReformatText(text).split(" ");
        Map<Integer, String> ratingSnippets = new HashMap<>();
        for (String snippet : getAllSnippets(token)) {
            List<String> listSnippet = new ArrayList<>();
            for (String word : words) {
                if(snippet.contains(word)){
                    listSnippet.add(snippet);
                }
            }
            ratingSnippets.put(listSnippet.size(), snippet);
        }
        for (Map.Entry<Integer, String> entry : ratingSnippets.entrySet()){
            resultSnippet = entry.getValue();
        }

        return resultSnippet;
    }
    private String getReformatText(String text){
        Document doc = Jsoup.parse(text);
        String allText = "";
        String[] token = doc.text().split(" ");
        for (String beginnerWord : lemma){
            String[] copyToken = allText.split(" ");
            if(!allText.isEmpty()){
                token = copyToken;
            }
            String copyText = " ";
            for (String finalWord : token){
                int allowableLengthWord = 3;
                if((textQuery.contains(finalWord) || finalWord.contains(beginnerWord))
                        && beginnerWord.length() >= allowableLengthWord
                        && finalWord.length() >= allowableLengthWord){
                    words.add(finalWord);
                    copyText += " <strong>" + finalWord + "</strong>";
                }else {copyText += " " + finalWord;}
            }
            allText = copyText;
        }
        return allText;
    }
    private List<String> getAllSnippets(String[] token){
        int stopIndex = 0;
        int countWordOfSnippet = 30;
        String snippet = " ";
        List<String> listSnippet = new ArrayList<>();
        for (String word : token){
            snippet += " " + word;
            if(stopIndex == countWordOfSnippet){
                listSnippet.add(snippet);
                stopIndex = 0;
                snippet = " ";
            }
            stopIndex++;
        }
        return listSnippet;
    }
}
