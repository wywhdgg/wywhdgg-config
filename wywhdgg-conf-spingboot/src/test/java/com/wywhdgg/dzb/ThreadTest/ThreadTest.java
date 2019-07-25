package com.wywhdgg.dzb.ThreadTest;

import java.io.IOException;

/**
 * @author: dongzhb
 * @date: 2019/7/25
 * @Description:
 */
public class ThreadTest {
    public static void main(String[] args) {
        Thread thread = new Thread(new Runnable() {
            //int a = 0;
            @Override
            public void run() {
                while (true) {
                    for (int i = 0; i < 10; i++) {
                        System.out.println("跑批数据++" +i);
                    }

                }
            }
        });
        thread.setDaemon(true);
        thread.start();
        System.out.println("isDaemon = " + thread.isDaemon());
        try {
            System.in.read();   // 接受输入，使程序在此停顿，一旦接收到用户输入，main线程结束，守护线程自动结束
        } catch (IOException ex) {}
    }

    public static void heelo() {
        System.out.println("hello word");
    }
}
