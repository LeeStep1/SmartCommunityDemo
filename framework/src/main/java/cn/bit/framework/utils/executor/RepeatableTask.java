package cn.bit.framework.utils.executor;/**
 * Created by terry on 2016/7/25.
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 可重复执行任务类
 * 如执行失败，可按照指定的重试次数，重新执行
 *
 * @author terry
 * @create 2016-07-25 10:20
 **/
public abstract class RepeatableTask<T> implements Runnable, Delayed {

    private final Logger log = LoggerFactory.getLogger(RepeatableTask.class);

    //执行次数
    private AtomicInteger execTimes = new AtomicInteger(0);

    //执行时间
    private long execTime;

    //重试次数
    private int retries;

    //首次执行延时
    private long firstDelay;

    //重试延时
    private long[] retryDelays;

    private TimeUnit timeUnit;

    private RepeatableTaskExecutor executor;

    protected T data;

    private static final TimeUnit DEFAULT_TIME_UNIT = TimeUnit.SECONDS;
    private static final int DEFAULT_RETRIES = 2;
    private static final long DEFAULT_FIRST_DELAY = 1L;
    private static final long[] DEFAULT_RETRY_DELAYS = {5, 10};

    /**
     * @param executor    执行器
     * @param data        任务数据
     * @param retries     重试次数
     * @param firstDelay  首次延时
     * @param retryDelays 重试延时
     * @param timeUnit    时间单位
     */
    public RepeatableTask(RepeatableTaskExecutor executor, T data, int retries, long firstDelay, long[] retryDelays,
                          TimeUnit timeUnit) {

        Assert.isTrue(retries >= 0);
        Assert.isTrue(firstDelay >= 0);
        Assert.isTrue(retryDelays.length == retries);

        for (long retryDelay : retryDelays) {
            Assert.isTrue(retryDelay >= 0);
        }

        this.retries = retries;
        this.firstDelay = firstDelay;
        this.retryDelays = retryDelays;
        this.timeUnit = timeUnit;
        this.executor = executor;
        this.data = data;
        this.execTime = TimeUnit.NANOSECONDS.convert(firstDelay, timeUnit) + System.nanoTime();
    }

    protected RepeatableTask(RepeatableTaskExecutor executor, T data) {
        this(executor, data, DEFAULT_RETRIES, DEFAULT_FIRST_DELAY, DEFAULT_RETRY_DELAYS, DEFAULT_TIME_UNIT);
    }

    @Override
    public long getDelay(TimeUnit unit) {
        return unit.convert(execTime - System.nanoTime(), unit.NANOSECONDS);
    }

    @Override
    public int compareTo(Delayed o) {
        RepeatableTask other = (RepeatableTask) o;
        return this.execTime > other.execTime ? 1 : (this.execTime < other.execTime ? -1 : 0);
    }

    @Override
    public void run() {
        /**
         * 如果任务执行失败，并且已执行次数在指定重试次数以内,重新放回队列
         * 如已执行次数超过指定重试次数，直接丢弃
         */
        int number = this.execTimes.getAndIncrement();//当前已执行次数
        if (!doWork() && number < retries) {
            log.info("task exec failed ,prepare retry ====> {}", number + 1);
            //设置下次执行时间
            this.execTime = TimeUnit.NANOSECONDS.convert(retryDelays[number], timeUnit)
                    + System.nanoTime();
            //重新放入队列
            executor.addTask(this);
        }
    }

    public abstract boolean doWork();
}
