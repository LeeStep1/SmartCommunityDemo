package cn.bit.framework.utils.executor;/**
 * Created by terry on 2016/7/25.
 */

import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.DelayQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 可重复任务执行器
 *
 * @author terry
 * @create 2016-07-25 10:18
 **/
public class RepeatableTaskExecutor<T extends RepeatableTask> {

    private final AtomicBoolean started = new AtomicBoolean();
    private ThreadPoolTaskExecutor executor;//线程池
    private DelayQueue<T> taskQueue = new DelayQueue<>();//任务队列

    public ThreadPoolTaskExecutor getExecutor() {
        return executor;
    }

    public void setExecutor(ThreadPoolTaskExecutor executor) {
        this.executor = executor;
    }


    public void start() throws InterruptedException {
        startExecution();
        started.set(true);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                RepeatableTaskExecutor.this.stop();
            }
        });
    }

    private void startExecution() {
        executor.execute(() -> {
            //当活跃线程数量到达阀值,不执行
            while (executor.getActiveCount() < executor.getMaxPoolSize()) {
                try {
                    //取出任务并执行
                    executor.execute(taskQueue.take());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void stop() {
        if (started.getAndSet(false)) {
            executor.shutdown();
        }
    }

    public void addTask(T task) {
        this.taskQueue.put(task);
    }

    public boolean isStarted() {
        return started.get();
    }
}
