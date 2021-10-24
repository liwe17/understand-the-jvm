package com.weiliai.chapter2;

/**
 * 虚拟机栈/本地方法栈
 * -Xss128k
 *
 * @author LiWei
 * @date 2021/10/24
 */
public class StackSOF {

    //线程请求的栈深度大于虚拟机所允许的最大深度
    private static int stackLength = 1;

    public void stackLeak() {
        stackLength++;
        stackLeak();
    }

    public static void main(String[] args) {
        //testSOF();

        //在JDK6中,intern()方法会把首次遇到的字符串实例复制到永久代的字符串常量池中存储,返回的也是永久代里面这个字符串实例的引用
        //在JDK7中,常量池本身就在堆中,所以直接返回引用
        String str1 = new StringBuilder("计算机").append("软件").toString();
        System.out.println(str1.intern() == str1);
        //Java这个词在字符串常量池中已经存在
        String str2 = new StringBuilder("ja").append("va").toString();
        System.out.println(str2.intern() == str2);

    }

    public static void testSOF() {
        StackSOF oom = null;
        try {
            oom = new StackSOF();
            oom.stackLeak();
        } catch (Throwable ex) {
            ex.printStackTrace();
            System.out.printf("stackLength: %d", oom.stackLength);
        }
    }

}
