package com.weiliai.chapter7;

import java.io.IOException;
import java.io.InputStream;

/**
 * 类加载器与instanceof关键字
 *
 * @author LiWei
 * @date 2021/11/16
 */
public class ClassLoaderTest {

    public static void main(String[] args) throws Exception {
        ClassLoader myLoader = new ClassLoader() {
            @Override
            public Class<?> loadClass(String name) throws ClassNotFoundException {
                try {
                    String fileName = name.substring(name.lastIndexOf(".") + 1) + ".class";
                    InputStream is = getClass().getResourceAsStream(fileName);
                    if (null == is) {
                        return super.loadClass(name);
                    }
                    byte[] b = new byte[is.available()];
                    is.read(b);
                    return defineClass(name, b, 0, b.length);
                } catch (IOException ex) {
                    throw new ClassNotFoundException(name);
                }
            }
        };
        Object instance = myLoader.loadClass("com.weiliai.chapter7.ClassLoaderTest").newInstance();
        System.err.println(instance.getClass().getClassLoader());
        System.out.println(ClassLoaderTest.class.getClassLoader());
        System.err.println(instance.getClass());
        System.out.println(instance instanceof com.weiliai.chapter7.ClassLoaderTest);
    }
}
