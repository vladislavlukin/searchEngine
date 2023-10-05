package searchengine.service.search;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import searchengine.service.task.search.SnippetGenerator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SnippetGeneratorTest {

    private SnippetGenerator snippetGenerator;
    private StringBuilder stringBuilder;

    @BeforeEach
    public void setUp() throws IOException {
        List<String> line = Files.readAllLines(Paths.get("src/test/resources/test_html/test.html"));
        stringBuilder = new StringBuilder();
        line.forEach(lines -> stringBuilder.append(line).append("\n"));

        snippetGenerator = new SnippetGenerator();
    }

    @Test
    public void testGetSnippet() {
        Set<String> lemma = new HashSet<>();
        lemma.add("горизонт");
        lemma.add("красота");

        String expected = "парку, наслаждаясь осенней природой в ее великолепии., , На <strong>горизонте</strong> " +
                "были видны горы, покрытые снегом, придавая пейзажу особую <strong>красоту.</strong> " +
                "Вдали слышались звуки пение птиц, радостно чирикающих на ветвях. В этот";

        String snippet = snippetGenerator.generateSnippet(stringBuilder.toString(), lemma);

        assertEquals(expected.trim(), snippet.trim());
    }
}

