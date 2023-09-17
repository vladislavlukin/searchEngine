package searchengine.service.indexing;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ThreadManagerImpl implements ThreadManager{
    private final List<Thread> threads = new ArrayList<>();
    @Override
    public void addThread(Thread thread) {
        threads.add(thread);
    }

    @Override
    public List<Thread> getThreads() {
        return threads;
    }

    @Override
    public void stopAllThreads() {
        List<Thread> threads = getThreads();
        for (Thread thread : threads) {
            thread.stop();
        }
        threads.clear();
    }

    @Override
    public void startAllThreads() {
        threads.forEach(Thread::start);
    }
}
