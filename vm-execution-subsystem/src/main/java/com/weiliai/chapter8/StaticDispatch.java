package com.weiliai.chapter8;

/**
 * 静态分派演示
 * <p>
 * Human man = new Man();
 * Human称为变量的静态类型(static type)或者外观类型(Apparent Type)
 * Man称为实际类型(Actual Type)或者叫做运行时类型(Runtime Type)
 * <p>
 * 静态类型和实际类型在运行过程中都可能发生变化,
 * 区别是静态类型的变化仅仅在使用时发生,变量本身的静态类型不会被改变,
 * 并且最终的静态类型是在编译期可知的,而实际类型变化的结果在运行期才可确定,编译器在编译程序的时候并不知道一个对象的实际类型是什么
 * <p>
 *
 * @author LiWei
 * @date 2021/11/17
 */
public class StaticDispatch {

    static abstract class Human {
    }

    static class Man extends Human {
    }

    static class Woman extends Human {
    }

    public void sayHello(Human guy) {
        System.out.println("hello,guy!");
    }

    public void sayHello(Man guy) {
        System.out.println("hello,gentleman!");
    }

    public void sayHello(Woman guy) {
        System.out.println("hello,lady!");
    }

    public static void main(String[] args) {
        Human man = new Man();
        Human woman = new Woman();
        StaticDispatch sr = new StaticDispatch();
        sr.sayHello(man);
        sr.sayHello(woman);
    }


}
