package searchengine.service.indexing;


import java.util.List;

public interface ThreadManager {
    List<Thread> getThreads();
    void addThread(Thread thread);
    void startAllThreads();
    boolean areThreadsAlive();
    boolean areThreadsNotAlive();
    void stopAllThreads();

}
