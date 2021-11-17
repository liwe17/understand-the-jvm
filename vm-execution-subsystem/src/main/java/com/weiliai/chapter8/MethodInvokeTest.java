package com.weiliai.chapter8;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;

import static java.lang.invoke.MethodHandles.lookup;

/**
 * 方法调用测试
 *
 * @author LiWei
 * @date 2021/11/17
 */
public class MethodInvokeTest {

    class GrandFather {
        void thinking() {
            System.out.println("i am grandfather");
        }
    }

    class Father extends GrandFather {
        void thinking() {
            System.out.println("i am father");
        }
    }

    class Son extends Father {
        void thinking() {
//            try {
//                MethodType mt = MethodType.methodType(void.class);
//                MethodHandle mh = lookup().findSpecial(GrandFather.class, "thinking", mt, getClass());
//                mh.invoke(this);
//            } catch (Throwable e) {
//                e.printStackTrace();
//            }
            try {
                MethodType mt = MethodType.methodType(void.class);
                Field lookupImpl = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
                lookupImpl.setAccessible(true);
                MethodHandle mh = ((MethodHandles.Lookup) lookupImpl.get(null)).findSpecial(GrandFather.class, "thinking", mt, GrandFather.class);
                mh.invoke(this);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        (new MethodInvokeTest().new Son()).thinking();
    }

}
