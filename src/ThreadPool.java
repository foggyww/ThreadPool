import java.util.ArrayDeque;
import java.util.Queue;

public class ThreadPool {

    private final int coreThreadNum;
    private final int maxThreadNum;
    private boolean isRun;
    private final Thread[] threads;
    private final Object locked = new Object();
    private final Object[] lockedArray;
    Queue<Runnable> mQueue;

    public ThreadPool(int coreThreadNum, int maxThreadNum) {
        this.coreThreadNum = coreThreadNum;
        this.maxThreadNum = maxThreadNum;
        isRun = false;
        this.threads = new Thread[coreThreadNum];
        mQueue = new ArrayDeque<>();
        lockedArray = new Object[coreThreadNum];
        for (int i = 0; i < coreThreadNum; i++) {
            lockedArray[i] = new Object();
        }
    }

    public ThreadPool() {
        this(5, 10);
    }

    public void push(Runnable runnable) {
        mQueue.add(runnable);
        if (isRun) {
            for (int i = 0; i < coreThreadNum; i++) {
                synchronized (lockedArray[i]) {
                    lockedArray[i].notify();
                }
            }
        }
    }
    public void run() {
        if (isRun) {
            return;
        }
        isRun = true;
        for (int i = 0; i < coreThreadNum; i++) {
            int finalI = i;
            threads[i] = new Thread(() -> {
                while (true) {
                    Runnable nowWork;
                    synchronized (lockedArray[finalI]) {
                        while (mQueue.isEmpty()) {
                            try {
                                lockedArray[finalI].wait();
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                        }
                        synchronized (locked) {
                            if (!mQueue.isEmpty()) {
                                nowWork = mQueue.remove();
                            }else{
                                continue;
                            }
                        }
                    }
                    nowWork.run();
                }
            });
            threads[i].start();
        }
    }
}

