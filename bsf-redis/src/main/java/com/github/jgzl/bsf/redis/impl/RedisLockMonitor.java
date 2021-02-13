package com.github.jgzl.bsf.redis.impl;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Huang Zhaoping
 * @date 2020/5/14
 */
@Slf4j
public class RedisLockMonitor {

    private static final Map<String, Task> taskCache = new ConcurrentHashMap<>();
    private static final TaskMonitor taskMonitor = new TaskMonitor();

    public static void add(RedisLock lock) {
        taskCache.put(lock.getClientId(), new Task(lock));
        if (!taskMonitor.isRunning()) {
            taskMonitor.start();
        }
    }

    public static void remove(RedisLock lock) {
        taskCache.remove(lock.getClientId());
    }

    public static void destroy() {
        taskMonitor.stop(true);
    }

    private static long executeTasks() {
        if (taskCache.isEmpty()) {
            return 0;
        }
        // 找到需要刷新的任务
        long current = System.currentTimeMillis(), min = 0;
        List<Task> list = new ArrayList<>();
        for (Task task : taskCache.values()) {
            long time = task.getNextTime();
            if (time <= current) {
                list.add(task);
            } else if (min <= 0 || time < min) {
                min = time;
            }
        }
        // 执行任务列表
        for (Task task : list) {
            try {
                if (!task.execute()) {
                    // 只有锁已经被释放的情况才会失败，直接移除
                    taskCache.remove(task.getClientId());
                }
            } catch (Exception e) {
                log.error("刷新Redis锁异常，3秒后重试", e);
                return current + 3000;
            }
        }
        return min;
    }

    private static class TaskMonitor implements Runnable {
        private volatile boolean isRunning;
        private Thread thread;

        @Override
        public void run() {
            while (isRunning) {
                long next = executeTasks();
                if (next > 0) {
                    long time = Math.min(Math.max(next - System.currentTimeMillis(), 1000), 60000);
                    try {
                        Thread.sleep(time);
                    } catch (InterruptedException e) {
                    }
                } else if (isIdle()) {
                    stop(false);
                }
            }
        }

        private boolean isIdle() {
            for (int i = 0; i < 60; i++) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    return false;
                }
                if (taskCache.size() > 0) {
                    return false;
                }
            }
            return true;
        }

        public synchronized void start() {
            if (!isRunning) {
                isRunning = true;
                thread = new Thread(this, "RedisLockMonitor");
                thread.setDaemon(true);
                thread.start();
            }
        }

        public boolean isRunning() {
            return isRunning;
        }

        public synchronized void stop(boolean force) {
            if (!isRunning) return;
            isRunning = false;
            if (!force && taskCache.size() > 0) {
                isRunning = true;
                return;
            } else if (force) {
                taskCache.clear();
            }
            if (thread != null) {
                thread.interrupt();
                thread = null;
            }
        }
    }

    private static class Task {
        private final RedisLock lock;
        private long executeTime;

        public Task(RedisLock lock) {
            this.lock = lock;
            this.executeTime = System.currentTimeMillis();
        }

        public String getClientId() {
            return lock.getClientId();
        }

        public long getNextTime() {
            return executeTime + lock.getTimeout() * 500;
        }

        public boolean execute() {
            boolean result = lock.doRenew();
            if (result) {
                executeTime = System.currentTimeMillis();
            }
            return result;
        }
    }

}
