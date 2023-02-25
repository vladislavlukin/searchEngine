package searchengine.services.search;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import searchengine.model.site.PageRepository;
import searchengine.services.LemmaFinder;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SearchSnippet {
    public SearchSnippet(PageRepository pageRepository, Set<String> lemma) {
        this.pageRepository = pageRepository;
        this.lemma = lemma;
    }

    private PageRepository pageRepository;
    private Set<String> lemma;
    private StringBuilder stringBuilder = new StringBuilder();

    public void workOfContent(String text) {
        Document doc = Jsoup.parse(text);
        String allText = " ";
        for (String word : lemma) {
            if (doc.text().contains(word) && word.length() > 1) {
                Elements elements = doc.getElementsContainingOwnText(word);
                if (!allText.contains(elements.toString())) {
                    allText += elements + " ";
                }
            }
        }
            for(String word : lemma) {
                allText = allText.replace(word, "<d>" + word + "</d>");
            }
        String result = " ";
            for (String word : lemma){
                int limitChar = 10;
                if(allText.split(" ").length < limitChar){
                    result = allText;
                    break;
                }
                String[] token = allText.split(" ", limitChar);
                int index = 1;
                while (index > 0) {
                    int k = limitChar - 1;
                    String name = " ";
                    for (int i = 0; i < limitChar - 1; i++){
                        name += token[i] + " ";
                    }
                    if (name.contains(word)) {
                        if(!result.contains(name)) {
                            result += name + " ..... ";
                        }
                    }
                    if(token[k].split(" ").length < limitChar){
                         if(token[k].contains(word)){
                             result += token[k] + " ..... ";
                         }
                         index = 0;
                    }else {
                        token = token[k].split(" ", limitChar);
                    }
                }
            }
        stringBuilder.append(result);
        }
    public String search (String path, String url){
        pageRepository.findAll().forEach(page -> {
            if(page.getPath().equals(path) && page.getSite().getUrl().equals(url)){
                workOfContent(page.getContent());
            }
        });

        return stringBuilder.toString();
    }
}
