public class Main {
    public static void main(String[] args) {
        ThreadPool threadPool = new ThreadPool();
        for (int i = 0; i < 10; i++) {
            int finalI = i;
            threadPool.push(()->{
                try {
                    System.out.println("start "+finalI);
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                System.out.println("this is "+finalI);
            });
        }
        threadPool.run();
    }
}