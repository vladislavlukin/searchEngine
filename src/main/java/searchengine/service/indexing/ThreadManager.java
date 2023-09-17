package searchengine.service.indexing;

import java.util.List;

public interface ThreadManager {
    void addThread(Thread thread);
    List<Thread> getThreads();
   /* default void stopAllThreads() {
        List<Thread> threads = getThreads();
        for (Thread thread : threads) {
            thread.interrupt();
        }
        threads.clear();
    }

    */
    void stopAllThreads();
    void startAllThreads();
}
