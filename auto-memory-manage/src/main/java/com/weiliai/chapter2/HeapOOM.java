package com.weiliai.chapter2;

import java.util.ArrayList;
import java.util.List;

/**
 * Java堆内存溢出异常
 * <p>
 * VM args: -Xms20m -Xmx20m -XX:+HeapDumpOnOutOfMemoryError
 *
 * @author LiWei
 * @date 2021/10/24
 */
public class HeapOOM {

    /**
     * 限制Java堆的大小为20MB,不可扩展,将堆的最小值-Xms参数与最大值-Xmx参数设置为一样即可避免堆自动扩展
     * 过参数-XX：+HeapDumpOnOutOfMemoryError可以让虚拟机在出现内存溢出异常的时候Dump出当前的内存堆转储快照以便进行事后分析
     */
    static class OOMObject {
        byte[] arr = new byte[1024];
    }

    public static void main(String[] args) {
        List<OOMObject> list = new ArrayList<>();
        for (; ; ) {
            list.add(new OOMObject());
        }
    }

}
