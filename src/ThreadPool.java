import java.util.ArrayDeque;
import java.util.Queue;

public class ThreadPool {

    private int coreThreadNum;
    private int maxThreadNum;

    private boolean isRun;
    private Thread[] threads;

    Queue<Runnable> mQueue;

    public ThreadPool(int coreThreadNum, int maxThreadNum) {
        this.coreThreadNum = coreThreadNum;
        this.maxThreadNum = maxThreadNum;
        isRun = false;
        this.threads = new Thread[coreThreadNum];

        mQueue = new ArrayDeque<>();
    }

    public ThreadPool() {
        this(5, 10);
    }

    public void push(Runnable runnable) {
        mQueue.add(runnable);
        for (int i = 0; i < coreThreadNum; i++) {
            if(isRun&&!threads[i].isAlive()){
                threads[i].start();
            }
        }
    }

    public final Object locked = new Object();

    public void run() {
        if (isRun) {
            return;
        }
        isRun = true;
        for (int i = 0; i < coreThreadNum; i++) {
            threads[i] = new Thread(() -> {
                while (!mQueue.isEmpty()) {
                    Runnable nowWork;
                    synchronized (locked) {
                        if (!mQueue.isEmpty()) {
                            nowWork = mQueue.remove();
                        }else{
                            break;
                        }
                    }
                    nowWork.run();
                }
            });
            threads[i].start();
        }
    }
}

