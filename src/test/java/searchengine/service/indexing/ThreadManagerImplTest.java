package searchengine.service.indexing;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.mockito.Mockito;


public class ThreadManagerImplTest {

    private ThreadManager threadManager;

    @BeforeEach
    public void setUp() {
        threadManager = new ThreadManagerImpl();
    }

    @Test
    public void testAddThread() {
        Thread thread = new Thread();
        threadManager.addThread(thread);
        assertTrue(threadManager.getThreads().contains(thread));
    }

    @Test
    public void testStartAndStopAllThreads() {
        Thread thread1 = Mockito.mock(Thread.class);
        Thread thread2 = Mockito.mock(Thread.class);

        threadManager.addThread(thread1);
        threadManager.addThread(thread2);

        threadManager.startAllThreads();

        Mockito.verify(thread1).start();
        Mockito.verify(thread2).start();

        threadManager.stopAllThreads();

        assertFalse(threadManager.areThreadsAlive());
    }

    @Test
    public void testAreThreadsAlive() {
        Thread thread1 = new Thread();
        Thread thread2 = new Thread();

        threadManager.addThread(thread1);
        threadManager.addThread(thread2);

        assertFalse(threadManager.areThreadsAlive());

        thread1.start();

        assertTrue(threadManager.areThreadsAlive());
    }

    @Test
    public void testAreThreadsNotAlive() {
        Thread thread1 = new Thread();
        Thread thread2 = new Thread();

        threadManager.addThread(thread1);
        threadManager.addThread(thread2);

        assertTrue(threadManager.areThreadsNotAlive());

        thread1.start();

        assertFalse(threadManager.areThreadsNotAlive());
    }
}


