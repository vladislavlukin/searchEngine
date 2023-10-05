package searchengine.service.task.search;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;
import java.util.*;

import org.apache.commons.text.similarity.LevenshteinDistance;

@Component
public class SnippetGenerator {
    public String resultSnippet(String text){
        String[] words = text.split("\\s+");

        int limitSnippet = 0;
        int countActualWords = 0;
        int currentIndex = 0;

        StringBuilder stringBuilder = new StringBuilder();
        Map<Integer, StringBuilder> relevantSnippet = new TreeMap<>();

        while (currentIndex < words.length){
            if(limitSnippet == 30){
                relevantSnippet.put(countActualWords, stringBuilder);
                stringBuilder = new StringBuilder();
                countActualWords = 0;
                limitSnippet = 0;
            }

            stringBuilder.append(words[currentIndex]).append(" ");

            if(words[currentIndex].startsWith("<strong>")){
                countActualWords++;
            }
            limitSnippet++;
            currentIndex++;
        }

        if (!stringBuilder.isEmpty()) {
            relevantSnippet.put(countActualWords, stringBuilder);
        }

        if (relevantSnippet.isEmpty()){
            return text;
        }

        return relevantSnippet.values()
                .stream()
                .reduce((first, second) -> second)
                .map(StringBuilder::toString)
                .orElse("");
    }

    public String generateSnippet(String htmlText, Set<String> wordList) {
        Document document = Jsoup.parse(htmlText);

        String[] words = document.text().split("\\s+");

        StringBuilder textParts = new StringBuilder();
        LevenshteinDistance distance = new LevenshteinDistance();

        int threshold = 2;
        int currentIndex = 0;

        while (currentIndex < words.length) {
            String word = words[currentIndex];
            boolean containsWordInSearchWord = wordList.stream().anyMatch(searchWord -> {
                int levenshteinDistance = distance.apply(searchWord, word);
                return levenshteinDistance <= threshold;
            });
            if (containsWordInSearchWord) {
                textParts.append("<strong>").append(words[currentIndex]).append("</strong>").append(" ");
            } else {
                textParts.append(words[currentIndex]).append(" ");
            }
            currentIndex++;
        }

        return resultSnippet(textParts.toString());
    }
}
