package searchengine.service.indexing;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ThreadManagerImpl implements ThreadManager {
    private final List<Thread> threads = new ArrayList<>();

    @Override
    public List<Thread> getThreads() {
        return threads;
    }

    @Override
    public void addThread(Thread thread) {
        threads.add(thread);
    }

    @Override
    public void stopAllThreads() {
        threads.forEach(Thread::interrupt);
        clearStoppedThreadsCache();
    }

    @Override
    public void startAllThreads() {
        threads.forEach(Thread::start);
    }

    @Override
    public boolean areThreadsAlive() {
        return threads.stream().anyMatch(Thread::isAlive);
    }

    @Override
    public boolean areThreadsNotAlive() {
        return threads.stream().noneMatch(Thread::isAlive);
    }

    private void clearStoppedThreadsCache() {
        threads.removeIf(Thread::isInterrupted);
    }
}

