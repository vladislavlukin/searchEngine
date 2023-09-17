package searchengine.service.task.indexing.indexing;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ErrorsHandler {
    /*private final ThreadManager threadManager;
    public String textError;
    public Integer stopIndex;
    public boolean returnError(SiteRepository siteRepository){
        stopIndex = 0;
        Set<String> sites = new HashSet<>();
        siteRepository.findAll().forEach(site -> {
            sites.add(site.getUrl());
            if(!site.getStatus().equals(Status.INDEXING)){
                stopIndex++;
            }
        });
        if (sites.isEmpty()) {
            textError = "Добавтье не менее одного сайта или обновите текущий";
            return true;
        }

        if (threadManager.getThreads() != null) {
            for (Thread newThread : threadManager.getThreads()) {
                if (newThread.isAlive()) {
                    textError = "Индексикация уже запущена";
                    return true;
                }
            }
        }

        if(stopIndex == sites.size()){
            textError = "Все сайты проиндексированы или с ошибкой!" +
                    " Если только добавили сайт, то поробуйте запустить индаксацию позднее.";
            return true;
        }
        return false;
    }

     */
}
