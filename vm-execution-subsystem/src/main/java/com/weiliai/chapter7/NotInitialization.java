package com.weiliai.chapter7;

/**
 * 非主动使用类字段演示
 * <p>
 * 1.通过子类引用父类的静态字段,不会导致子类初始化
 * 2.通过数组定义来引用类,不会触发此类的初始化
 * 3.常量在编译阶段会存入调用类的常量池中,本质上没有直接引用到定义常量的类,因此不会触发定义常量的类的初始化
 *
 * @author LiWei
 * @date 2021/11/15
 */
public class NotInitialization {
    public static void main(String[] args) {
        //System.out.println(SubClass.class);
        //SuperClass[] sca = new SuperClass[10];
        System.out.println(ConstClass.HELLO_WORLD);
    }
}
