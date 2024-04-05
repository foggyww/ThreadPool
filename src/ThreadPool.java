import java.util.ArrayDeque;
import java.util.Queue;

public class ThreadPool {

    private final int coreThreadNum;
    private final int maxThreadNum;
    private boolean isRun;
    private final Thread[] threads;
    private int nowCoreThreadAliveNum;
    private final Object locked = new Object();
    private final Object[] lockedArray;
    private int nowThreadNum;
    private final Object creatLocked = new Object();
    Queue<Runnable> mQueue;

    public ThreadPool(int coreThreadNum, int maxThreadNum) {
        this.coreThreadNum = coreThreadNum;
        this.maxThreadNum = maxThreadNum;
        this.isRun = false;
        this.threads = new Thread[coreThreadNum];
        this.mQueue = new ArrayDeque<>();
        this.lockedArray = new Object[coreThreadNum];
        this.nowThreadNum = coreThreadNum;
        this.nowCoreThreadAliveNum = 0;
        for (int i = 0; i < coreThreadNum; i++) {
            this.lockedArray[i] = new Object();
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

    private void createTempThread() {
        new Thread(()->{
            while (true){
                Runnable nowWork;
                synchronized (creatLocked){
                    while (mQueue.isEmpty()||nowThreadNum>=maxThreadNum||nowCoreThreadAliveNum<coreThreadNum){
                        try {
                            creatLocked.wait();
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    synchronized (locked) {
                        if (!mQueue.isEmpty()) {
                            nowWork = mQueue.remove();
                        } else {
                            continue;
                        }
                    }
                    nowThreadNum++;
                    new Thread(() -> {
                        nowWork.run();
                        nowThreadNum--;
                    }).start();
                }
            }
        }).start();
    }

    public void run() {
        if (isRun) {
            return;
        }
        isRun = true;
        createTempThread();
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
                            } else {
                                continue;
                            }
                        }
                    }
                    nowCoreThreadAliveNum++;
                    synchronized (creatLocked){
                        creatLocked.notify();
                    }
                    nowWork.run();
                    nowCoreThreadAliveNum--;
                }
            });
            threads[i].start();
        }
    }
}

