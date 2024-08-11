
package com.hrd.rpc.fault.retry;

import com.hrd.rpc.model.RpcResponse;

import java.util.concurrent.*;

/**
 * description:
 */
public class FixedIntervalRetryStratey implements RetryStrategy{

    private int retryTimes = 3; // 重试次数
    private long timeoutMillis = 3000L; // 超时时间3秒
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2); // 增加线程数
    private long retryIntervalMillis = 1000L; // 每次重试的间隔时间

    @Override
    public RpcResponse doRetry(Callable<RpcResponse> callable) throws Exception {
        int attempts = 0;
        try {
            while (attempts < retryTimes) {
                attempts++;
                Future<RpcResponse> future = scheduler.submit(callable);

                try {
                    // 使用带有超时参数的get方法
                    return future.get(timeoutMillis, TimeUnit.MILLISECONDS);
                } catch (TimeoutException e) {
                    future.cancel(true); // 超时后取消任务
                    if (attempts >= retryTimes) {
                        throw new TimeoutException("请求超时 " + attempts + " 次");
                    }
                    System.out.println("第 " + attempts + " 次重试由于超时而发起");
                    Thread.sleep(retryIntervalMillis); // 等待一段时间后再重试
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // 恢复中断状态
                    throw new RuntimeException("线程被中断", e);
                } catch (ExecutionException e) {
                    // 处理callable内部抛出的异常
                    if (attempts >= retryTimes) {
                        throw new RuntimeException("重试失败", e.getCause());
                    }
                } finally {
                    future.cancel(true); // 确保任务被取消
                }
            }
        } finally {
            scheduler.shutdown(); // 关闭线程池
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow(); // 如果不能正常关闭，强制关闭
            }
        }
        throw new TimeoutException("请求超时，重试了 " + retryTimes + " 次仍然失败");
    }
}
