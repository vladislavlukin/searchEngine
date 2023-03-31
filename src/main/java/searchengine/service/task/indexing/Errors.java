package searchengine.service.task.indexing;


import searchengine.service.indexingService.IndexingService;

import java.util.List;
import java.util.Set;

public class Errors {
    public static String textError;
    public static boolean responseError(Set<String> sites, List<Thread> thread, IndexingService indexingService){
        if (sites == null || sites.isEmpty()) {
            textError = "Добавтье не менее одного сайта или обновите текущий";
            return true;
        }

        if (thread != null) {
            for (Thread newThread : thread) {
                if (newThread.isAlive()) {
                    textError = "Индексикация уже запущена";
                    return true;
                }
            }
        }
        int stop = 0;
        for (String nameSite : sites){
            if(!indexingService.statusIsIndexing(nameSite)){
                stop++;
            }
        }
        if(stop == sites.size()){
            textError = "Все сайты проиндексированы или с ошибкой!" +
                    " Если только добавили сайт, то поробуйте запустить индаксацию позднее.";
            return true;
        }
        return false;
    }
}
