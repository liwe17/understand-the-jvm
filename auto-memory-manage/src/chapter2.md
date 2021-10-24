# Java内存区域与内存溢出异常

- Java虚拟机内存的各区域的作用,服务对象以及可能出现的问题

## 运行时数据区域

- 运行时数据区域
    - Method Area
    - Heap
    - VM Stack
    - Native Method Stack
    - Program Counter Register

- 分类
    - 私有
        - Program Counter Register,VM Stack,Native Method Stack
    - 共享
        - Heap,Method Area

### 程序计数器

- 程序计数器(Program Counter Register):当前线程所执行字节码行号指示器
    - 线程私有
    - 记录指令执行位置,为了线程切换后能恢复到执行位置
        - 如果线程正在执行的是一个Java方法,这个计数器记录的是正在执行的虚拟机字节码指令的地址
        - 如果正在执行的是本地(Native)方法,这个计数器值则应为空(Undefined)
    - Java虚拟机规范中唯一一个没有规定任何OOM情况的区域

### Java虚拟机栈

- Java虚拟机栈(Java VM Stack):Java方法执行的线程内存模型
    - 线程私有,生命周期与线程相同
    - 方法执行的时候,VM都会同步创建一个栈帧(Stack Frame),每个方法被调用直至执行完毕的过程,就对应着栈帧在虚拟机栈中从入栈到出栈的过程
        - 栈帧用于保存局部变量表,操作数栈,动态链接,方法出口等信息
    - Java虚拟机规范中规定了两类异常情况
        - 如果线程请求的栈深度大于虚拟机所允许的深度,将抛出StackOverflowError异常
        - 如果Java虚拟机栈容量可以动态扩展,当栈扩展时无法申请到足够的内存会抛出OutOfMemoryError异常

### 本地方法栈

- 本地方法栈(Native Method Stack):为虚拟机使用到的本地(Native)方法服务
    - 对本地方法栈中方法使用的语言,使用方式与数据结构并没有任何强制规定,具体的虚拟机可以自由实现它
    - Java虚拟机规范中规定了两类异常情况
        - 如果线程请求的栈深度大于虚拟机所允许的深度,将抛出(SOF)StackOverflowError异常
        - 如果本地方法栈容量可以动态扩展,当栈扩展时无法申请到足够的内存会抛出(OOM)OutOfMemoryError异常

### Java堆

- Java堆(Java Heap):存放对象实例
    - 虚拟机所管理的内存中最大的一块
    - 所有线程共享,启动时创建
    - 如果堆中没有内存完成实例分配且堆也无法扩展时,抛出OOM异常

### 方法区

- 方法区(Method Area):存储已被虚拟机加载的类型信息,常量,静态变量,即时编译器编译后的代码缓存等数据
    - 所有线程共享
    - 逻辑上是方法区
        - JDK8以前使用永久代(Permanent Generation)来实现方法区,JDK8使用的是元空间(Meta-space)
        - JDK7已经将将字符串常量池,静态变量从永久代移除,JDK8完全废弃永久代,把剩余部分全部移动到元空间
    - 如果方法区无法满足新的内存分配需求时,将抛出OutOfMemoryError异常

### 运行时常量池

- 运行时常量池(Runtime Constant Pool):方法区的一部分,用于存放编译期生成的各种字面量与符号引用
    - 编译期生成的各种字面量与符号引用,将在类加载后存放到方法区的运行时常量池中
    - 运行时常量池相对于Class文件常量池的另外一个重要特征是具备动态性,例如通过String类的intern()方法,将运行时常量放入常量池
    - 受方法区限制,当常量池无法再申请到内存时会抛出OutOfMemoryError异常

## HotSpot虚拟机对象探究

- HotSpot虚拟机在Java堆中对象分配,布局和访问的全过程

### 对象的创建

