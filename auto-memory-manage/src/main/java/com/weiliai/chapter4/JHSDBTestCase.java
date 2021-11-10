package com.weiliai.chapter4;

import java.util.concurrent.TimeUnit;

/**
 * 清单 4-6
 * <p>
 * staticObj,instanceObj,localObj存放在哪里?
 * <p>
 * staticObj:存在于方法区
 * instanceObj:存在于堆中
 * localObj:栈帧中的局部变量表中
 * <p>
 * 运行参数:-Xmx10m -XX:+UseSerialGC -XX:-UseCompressedOops
 *
 * @author LiWei
 * @date 2021/11/10
 */
public class JHSDBTestCase {

    static class Test {
        static ObjectHolder staticObj = new ObjectHolder();
        ObjectHolder instanceObj = new ObjectHolder();

        void foo() {
            ObjectHolder localObj = new ObjectHolder();
            System.out.println("done");    // 这里设一个断点
        }
    }

    private static class ObjectHolder {
    }

    public static void main(String[] args) throws InterruptedException {
        Test test = new JHSDBTestCase.Test();
        test.foo();
        TimeUnit.MINUTES.sleep(5);
    }


}
