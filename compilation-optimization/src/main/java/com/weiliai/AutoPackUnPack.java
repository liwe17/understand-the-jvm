package com.weiliai;

/**
 * 自动拆箱装箱,foreach
 *
 * @author LiWei
 * @date 2021/11/18
 */
public class AutoPackUnPack {

    public static void main(String[] args) {
        Integer a = 1;
        Integer b = 2;
        Integer c = 3;
        Integer d = 3;
        Integer e = 321;
        Integer f = 321;
        Long g = 3L;
        System.out.println(c == d); //ture
        System.out.println(e == f); //false
        System.out.println(c == (a + b)); //ture
        System.out.println(c.equals(a + b));//ture
        System.out.println(g == (a + b));//ture
        System.out.println(g.equals(a + b));//false
    }
}
