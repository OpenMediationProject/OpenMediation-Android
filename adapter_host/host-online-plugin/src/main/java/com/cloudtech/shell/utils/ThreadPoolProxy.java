package com.cloudtech.shell.utils;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadPoolProxy {
    private volatile static ThreadPoolProxy mNormalThreadPoolProxy;
    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int CORE_POOL_SIZE = CPU_COUNT + 1;
    private static final int MAXIMUM_POOL_SIZE = CPU_COUNT * 2 + 1;
    private static final int KEEP_ALIVE = 10;
    private ThreadPoolExecutor mThreadPoolExecutor;// 只需要初始化一次


    private ThreadPoolProxy() {
        initThreadPoolExecutor();
    }


    private void initThreadPoolExecutor() {
        if (mThreadPoolExecutor == null || mThreadPoolExecutor.isShutdown() ||
            mThreadPoolExecutor.isTerminated()) {
            synchronized (ThreadPoolProxy.class) {
                if (mThreadPoolExecutor == null || mThreadPoolExecutor.isShutdown()
                    || mThreadPoolExecutor.isTerminated()) {
                    TimeUnit unit = TimeUnit.SECONDS;
                    BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<>(128);
                    ThreadFactory threadFactory = Executors.defaultThreadFactory();
                    mThreadPoolExecutor = new ThreadPoolExecutor(CORE_POOL_SIZE, // 核心池的大小
                        MAXIMUM_POOL_SIZE, // 最大线程数
                        KEEP_ALIVE, // 保持时间
                        unit, // 保持时间的单位
                        workQueue, // 工作队列
                        threadFactory, // 线程工厂
                        new ThreadPoolExecutor.DiscardOldestPolicy()
                    );
                }
            }
        }
    }


    /**
     * 提交任务
     */
    public Future<?> submit(Runnable task) {
        initThreadPoolExecutor();
        return mThreadPoolExecutor.submit(task);
    }


    /**
     * 执行任务
     */
    public void execute(Runnable task) {
        initThreadPoolExecutor();
        mThreadPoolExecutor.execute(task);
    }


    /**
     * 移除任务
     */
    public void remove(Runnable task) {
        initThreadPoolExecutor();
        mThreadPoolExecutor.remove(task);
    }

    public void stopAll(){
        if (mThreadPoolExecutor != null && !mThreadPoolExecutor.isShutdown()) {
            mThreadPoolExecutor.shutdownNow();
        }
    }


    /**
     * 返回普通线程池的代理
     * 双重检查加锁,保证只有第一次实例化的时候才启用同步机制,提高效率
     */
    public static ThreadPoolProxy getInstance() {
        if (mNormalThreadPoolProxy == null) {
            synchronized (ThreadPoolProxy.class) {
                if (mNormalThreadPoolProxy == null) {
                    mNormalThreadPoolProxy = new ThreadPoolProxy();
                }
            }
        }
        return mNormalThreadPoolProxy;
    }

    public static void clean(){
        synchronized (ThreadPoolProxy.class){
            if(mNormalThreadPoolProxy!=null){
                mNormalThreadPoolProxy.stopAll();
                mNormalThreadPoolProxy.mThreadPoolExecutor=null;
            }
            mNormalThreadPoolProxy=null;
        }
    }

}
