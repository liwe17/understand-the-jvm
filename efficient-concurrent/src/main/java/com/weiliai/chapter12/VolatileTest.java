package com.weiliai.chapter12;

import java.io.IOException;

/**
 * volatile变量自增运算测试
 *
 * @author LiWei
 * @date 2021/11/24
 */
public class VolatileTest {

    public static volatile int race = 0;

    public static void increase() {
        race++;
    }

    private static final int THREADS_COUNT = 20;

    public static void main(String[] args) throws InterruptedException {
        Thread[] threads = new Thread[THREADS_COUNT];
        for (int i = 0; i < THREADS_COUNT; i++) {
            threads[i] = new Thread(() -> {
                for (int i1 = 0; i1 < 10000; i1++) {
                    increase();
                }
            });
            threads[i].start();
        }

        // 等待所有累加线程都结束
        for (Thread thread : threads) {
            thread.join();
        }
        System.out.println(race);
    }

}
