package com.weiliai;

import java.util.*;

/**
 * TODO
 *
 * @author LiWei
 * @date 2021/11/18
 */
public class Test {

    public static void main(String[] args) {
        String str = "";
        String[] split = str.split(" ");
        Map<String, Integer> map = new HashMap<>();
        for (String s : split) {
            map.compute(s, (k, v) -> (null == v ? 1 : (v = v + 1)));
        }
        map.entrySet().stream().sorted((K1, K2) -> -(K1.getValue().compareTo(K2.getValue()))).limit(3).forEach(System.out::println);
    }

    static class Node implements Comparable<Node> {

        private final String name;

        private final Integer size;

        public Node(String name, Integer size) {
            this.name = name;
            this.size = size;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Node)
                return ((Node) obj).name.equals(this.name);
            return false;
        }

        @Override
        public int compareTo(Node o) {
            return this.size - o.size;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
