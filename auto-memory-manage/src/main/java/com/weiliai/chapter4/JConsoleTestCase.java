package com.weiliai.chapter4;

import java.util.ArrayList;
import java.util.List;

/**
 * 清单4-7
 * <p>
 * -Xms100m -Xmx100m -XX:+UseSerialGC
 *
 * @author LiWei
 * @date 2021/11/10
 */
public class JConsoleTestCase {

    /**
     * 内存占位符对象，一个OOMObject大约占64KB
     */
    static class OOMObject {
        public byte[] placeholder = new byte[64 * 1024];
    }

    public static void fillHeap(int num) throws InterruptedException {
        List<OOMObject> list = new ArrayList<>();
        for (int i = 0; i < num; i++) {
            // 稍作延时，令监视曲线的变化更加明显
            Thread.sleep(50);
            list.add(new OOMObject());
        }
        System.gc();
    }

    public static void main(String[] args) throws Exception {
        fillHeap(1000);
    }

}
