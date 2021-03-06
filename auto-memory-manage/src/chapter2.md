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

- 说明
    - 创建对象通常仅仅是一个new关键字(例外:复制,反序列化)
    - 这里的对象创建不包含数组以及Class对象等,仅包含普通的Java对象

- 对象的创建过程(new指令)
    - 首先检查是否能在常量池中定位到类的符号引用,并检查这个符号引用代表的类是否已被加载,解析和初始化过,如果没有进行类加载
    - 类加载检查通过后,为新生对象分配内存,对象所需内存大小在类加载完成后便可完全确定,分配内存等同于把一块确定大小的内存块从Java堆中划分出来
        - 分配方式
            - 指针碰撞(Bump The Pointer):
              假设Java堆中内存是绝对规整的,所有被使用过的内存都被放在一边,空闲的内存被放在另一边,中间放着一个指针作为分界点的指示器那所分配内存就仅仅是把那个指针向空闲空间方向挪动一段与对象大小相等的距离
            - 空闲列表(Free List):
              但如果Java堆中的内存并不是规整的,已被使用的内存和空闲的内存相互交错在一起,那就没有办法简单地进行指针碰撞了,虚拟机就必须维护一个列表,记录上哪些内存块是可用的,在分配的时候从列表中找到一块足够大的空间划分给对象实例,并更新列表上的记录
        - 安全保证
            - 针对分配内存空间的动作进行同步处理,实际上通过CAS保证更新操作的原子性
            - 按照线程划分到不同的空间之中进行,即每个线程在Java堆中预先分配一小块内存,称为本地线程分配缓冲(Thread Local Allocation Buffer,TLAB)
                - -XX：+/-UseTLAB参数来设定是否使用TLAB
        - 分配过程
            - 哪个线程需要分配内存,就在其本地缓冲区中分配,只有本地缓冲区用完了,分配新的缓存区才使用同步锁定
    - 内存分配完成后,虚拟机必须将分配到的内存空间(但不包括对象头)都初始化为零值,如果启用TLAB,那么将会提前到TLAB时顺便执行
    - 虚拟机对对象进行必要设置,例如:对象是那个类的实例,如何找到元数据信息,对象的哈希码(延后到调用Object::hashCode时),对象的分代年龄等信息
    - 执行Class文件中<init>()方法即执行构造方法,完成对象的初始化.

### 对象的内存布局

- 对象在堆中存储布局,分为三部分
    - 对象头(Header)
    - 实例数据(Instance Data)
    - 对齐填充(Padding)

- 对象头部分包含两类信息
    - 存储对象自身运行时的数据,如哈希码(HashCode),GC分代年龄,锁状态标志,线程持有的锁,偏向线程ID,偏向时间戳等,在32/64位虚拟机分别为32/64个比特位,称为MarkWord
    - 类型指针,即对象指向它的类型元数据指针,Java虚拟机通过这个指针来确定该对象是那个类的实例,并不是所有的虚拟机实现都必须在对象数据上保留类型指针,即查找对象的元数据信息并不一定要经过对象本身
- 实例数据部分是对象真正存储的有效信息,即我们在程序代码里面定义的各种类型的字段内容,无论是从父类继承下来的,还是子类中定义的都必须记录起来
- 对齐填充,仅仅起着占位符的作用
    - 由于HotSpot虚拟机的自动内存管理系统要求对象起始地址必须是8字节整数倍,即任何对象的大小都必须是8字节整数倍
    - 对象头部分已经被精心设计成正好是8字节倍数(1倍或2倍)
    - 如果对象实例数据部分没有对齐,就需要通过对齐填充补全

### 对象的访问定位

- 创建对象为了后续使用该对象,我们Java程序会通过栈上的reference数据来操作堆上的具体对象
- 对象的访问方式由虚拟机实现而定,主流的访问方式主要由两种
    - 使用句柄:Java堆中将可能划分出一块内存来作为句柄池,reference中存储的就是对象的句柄地址,句柄中包含了对象的实例数据和类型数据具体的地址信息,如果对象被移动(垃圾回收移动对象),只改句柄中的实例数据指针
    - 直接指针:Java堆中对象的内存布局就必须考虑如何放置访问类型数据的相关信息,reference存储的就是对象的地址,如果只是访问对象,就无需间接访问的开销,速度快

### OutOfMemoryError异常

- 代码验证<Java虚拟机规范>中描述的各个运行时区域存储的内容

#### Java堆溢出

- Java堆用于储存对象实例,我们只要不断地创建对象并且保证不被垃圾回收机制清楚这些对象,那么随着对象数量的增加,总容量触及最大堆的容量限制后就会产生内存溢出异常
- 业务代码
    - com.weiliai.chapter2.HeapOOM

#### 虚拟机栈和本地方法栈溢出

- Java虚拟机规范明确允许Java虚拟机实现自行选择是否支持栈的动态扩展
    - HotSpot虚拟机的选择是不支持扩展,除非在创建线程申请内存时就因无法获得足够内存而出现OutOfMemoryError异常,否则在线程运行时是不会因为扩展而导致内存溢出的
    - 只会因为栈容量无法容纳新的栈帧而导致StackOverflowError异常
- 业务代码
    - com.weiliai.chapter2.StackSOF










