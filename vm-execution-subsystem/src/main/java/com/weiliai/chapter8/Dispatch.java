package com.weiliai.chapter8;

/**
 * 单分派,多分派演示
 * <p>
 * 首先是编译阶段中编译器的选择过程,也就是静态分派的过程
 * 选择目标方法的依据有两点
 * 1.静态类型是Father还是Son,2.方法参数是QQ还是360
 * 最终产物是产生了两条invokevirtual指令,两条指令的参数分别为常量池中指向Father::hardChoice(360)及Father::hardChoice(QQ)方法的符号引用
 * <p>
 * 运行阶段中虚拟机的选择,也就是动态分派的过程,更准确地说,是在执行这行代码所对应的invokevirtual指令时,
 * 唯一可以影响虚拟机选择的因素只有该方法的接受者的实际类型是Father还是Son,只有一个宗量作为选择依据
 * <p>
 *
 * @author LiWei
 * @date 2021/11/17
 */
public class Dispatch {

    static class QQ {
    }

    static class _360 {
    }

    public static class Father {
        public void hardChoice(QQ arg) {
            System.out.println("father choose qq");
        }

        public void hardChoice(_360 arg) {
            System.out.println("father choose 360");
        }
    }

    public static class Son extends Father {
        public void hardChoice(QQ arg) {
            System.out.println("son choose qq");
        }

        public void hardChoice(_360 arg) {
            System.out.println("son choose 360");
        }
    }

    public static void main(String[] args) {
        Father father = new Father();
        Father son = new Son();
        father.hardChoice(new _360());
        son.hardChoice(new QQ());
    }


}
