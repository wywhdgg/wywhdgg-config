package com.wywhdgg.dzb.LongPolling;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/***
 *@author dzb
 *@date 2019/7/28 10:17
 *@Description:
 *@version 1.0
 *
 * 服务端
 */
@Slf4j
public class LongPollingServlet extends HttpServlet {
    private Random random = new Random();
    /** 原子序列 */
    private AtomicLong sequenceId = new AtomicLong();
    /** 原子计算 */
    private AtomicLong count = new AtomicLong();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        log.info("第" + (count.incrementAndGet()) + "次 longpolling");
        int sleepSecends = random.nextInt(100);
        //随机获取等待时间，来通过sleep模拟服务端是否准备好数据
        log.info("wait " + sleepSecends + " second");
        try {
            TimeUnit.SECONDS.sleep(sleepSecends);//sleep
        } catch (InterruptedException e) {

        }
        PrintWriter out = response.getWriter();
        long value = sequenceId.getAndIncrement();
        out.write(Long.toString(value));
    }
}
