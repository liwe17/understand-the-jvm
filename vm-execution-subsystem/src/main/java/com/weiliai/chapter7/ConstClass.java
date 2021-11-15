package com.weiliai.chapter7;

/**
 * 常量在编译阶段会存入调用类的常量池中,本质上没有直接引用到定义常量的类,因此不会触发定义常量的类的初始化
 *
 * @author LiWei
 * @date 2021/11/15
 */
public class ConstClass {

    static {
        System.out.println("ConstClass init!");
    }

    public static final String HELLO_WORLD = "hello world";

}
