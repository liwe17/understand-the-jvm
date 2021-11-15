package com.weiliai.chapter7;

/**
 *
 * @author LiWei
 * @date 2021/11/15
 */
public class InitTest {

    static int i = 1;

    static {
        i = 0;  //  给变量复制可以正常编译通过
        System.out.print(i);  // 这句编译器会提示“非法向前引用”
    }

    //static int i = 1;

    static class Parent {
        public static int A = 1;
        static {
            A = 2;
        }
    }

    static class Sub extends Parent {
        public static int B = A;
    }

    public static void main(String[] args) {
        System.out.println(Sub.B);
    }



}

