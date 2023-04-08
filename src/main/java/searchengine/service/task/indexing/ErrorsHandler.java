package searchengine.service.task.indexing;


import searchengine.model.site.SiteRepository;
import searchengine.model.site.Status;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class ErrorsHandler {
    public static String textError;
    public static boolean returnError(SiteRepository siteRepository, List<Thread> thread){
        AtomicInteger stop = new AtomicInteger();
        Set<String> sites = new HashSet<>();
        siteRepository.findAll().forEach(site -> {
            sites.add(site.getUrl());
            if(!site.getStatus().equals(Status.INDEXING)){
                stop.getAndIncrement();
            }
        });
        if (sites.isEmpty()) {
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

        if(stop.get() == sites.size()){
            textError = "Все сайты проиндексированы или с ошибкой!" +
                    " Если только добавили сайт, то поробуйте запустить индаксацию позднее.";
            return true;
        }
        return false;
    }
}
