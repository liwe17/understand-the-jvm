package com.weiliai.chapter7;

/**
 * 变量槽测试
 * -verbose:gc
 *
 * @author LiWei
 * @date 2021/11/16
 */
public class VariableSlotTest {

    /**
     * placeholder能否被回收的根本原因就是
     * 局部变量表中的变量槽是否还存有关于placeholder数组对象的引用
     *
     */
    public static void main(String[] args) {
//        清单8-1
//        byte[] placeholder = new byte[64 * 1024 * 1024];
//        System.gc();

//        清单8-2
//        {
//            byte[] placeholder = new byte[64 * 1024 * 1024];
//        }
//        System.gc();

//        清单8-3
        {
            byte[] placeholder = new byte[64 * 1024 * 1024];
        }
        int a = 0;
        System.gc();
    }

}
