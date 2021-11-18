package com.weiliai;

import java.util.ArrayList;
import java.util.List;

/**
 * 当泛型遇见重载
 *
 * @author LiWei
 * @date 2021/11/18
 */
public class GenericTypes {

//    public static void method(List<String> list) {
//        System.out.println("invoke method(List<String> list)");
//    }

//    public static void method(List<Integer> list) {
//        System.out.println("invoke method(List<Integer> list)");
//    }

//    public static String method(List<String> list) {
//        System.out.println("invoke method(List<String> list)");
//        return "";
//    }

    public static int method(List<Integer> list) {
        System.out.println("invoke method(List<Integer> list)");
        return 1;
    }


}
