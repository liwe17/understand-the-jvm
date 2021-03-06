# Java内存模型与线程

- 并发处理的广泛应用是Amdahl定律代替摩尔定律成为计算机性能发展源动力的根本原因,也是人类压榨计算机运算能力的最有力武器

## 概述

- 高效并发介绍虚拟机如何实现多线程,多线程之间由于共享和竞争数据而导致的一系列问题及解决方案

## 硬件的效率与一致性

- 基于高速缓存的存储交互很好地解决了处理器与内存速度之间的矛盾,引出了新问题:缓存一致性(Cache Coherence)
    - 在多路处理器系统中,每个处理器都有自己的高速缓存,而它们又共享同一主内存(Main Memory),这种系统称为共享内存多核系统(Shared Memory Multiprocessors System)
    - 当多个处理器的运算任务都涉及同一块主内存区域时,将可能导致各自的缓存数据不一致,为了解决一致性的问题,需要各个处理器访问缓存时都遵循一些协议
- 除了增加高速缓存之外,为了使处理器内部的运算单元能尽量被充分利用,处理器可能会对输入代码进行乱序执行(Out-Of-Order Execution)优化
    - 处理器会在计算之后将乱序执行的结果重组,保证该结果与顺序执行的结果是一致的,但并不保证程序中各个语句计算的先后顺序与输入代码中的顺序一致
    - 因此如果存在一个计算任务依赖另外一个计算任务的中间结果,那么其顺序性并不能靠代码的先后顺序来保证
    - Java虚拟机的即时编译器中也有指令重排序(Instruction Reorder)优化

## Java内存模型

- Java内存模型(Java Memory Model,JMM)来屏蔽各种硬件和操作系统的内存访问差异,以实现让Java程序在各种平台下都能达到一致的内存访问效果
- 定义Java内存模型并非一件容易的事情,这个模型必须定义得足够严谨,才能让Java的并发内存访问操作不会产生歧义但是也必须定义得足够宽松,使得虚拟机的实现能有足够的自由空间去利用硬件的各种特性(寄存器,高速缓存和指令集中某些特有的指令)
  来获取更好的执行速度直至JDK5(实现了JSR-133)发布后,Java内存模型才终于成熟,完善起来了

### 主内存与工作内存

- Java内存模型的主要目的是定义程序中各种变量的访问规则,即关注在虚拟机中把变量值存储到内存和从内存中取出变量值这样的底层细节
    - 此处的变量(Variables)与Java编程中所说的变量有所区别
        - 包括实例字段,静态字段和构成数组对象的元素,但是不包括局部变量与方法参数,因为后者是线程私有的,不会被共享,自然就不会存在竞争问题
    - Java内存模型规定了所有的变量都存储在主内存,每条线程还有自己的工作内存(Working Memory)线程的工作内存中保存了被该线程使用的变量的主内存副本
    - 线程对变量的所有操作(读取,赋值等)都必须在工作内存中进行,而不能直接读写主内存中的数据
    - 不同的线程之间也无法直接访问对方工作内存中的变量,线程间变量值的传递均需要通过主内存来完成

### 内存间交互操作

- 关于主内存与工作内存之间具体的交互协议,即一个变量如何从主内存拷贝到工作内存,如何从工作内存同步回主内存这一类的实现细节,Java内存模型中定义了以下8种操作来完成
    - lock(锁定):作用于主内存的变量,它把一个变量标识为一条线程独占的状态
    - unlock(解锁):作用于主内存变量,它把一个处于锁定状态的变量释放出来,释放后的变量才可以被其他线程锁定
    - read(读取):作用于主内存变量,它把一个变量值从主内存传输到线程的工作内存中,以便随后的load动作使用
    - load(载入):作用于工作内存的变量,它把read操作从主内存中得到的变量值放入工作内存的变量副本中
    - use(使用):作用于工作内存的变量,它把工作内存中一个变量值传递给执行引擎,每当虚拟机遇到一个需要使用变量的值的字节码指令时将会执行这个操作
    - assign(赋值):作用于工作内存的变量,它把一个从执行引擎接收的值赋给工作内存的变量,每当虚拟机遇到一个给变量赋值的字节码指令时执行这个操作
    - store(存储):作用于工作内存的变量,它把工作内存中一个变量的值传送到主内存中,以便随后的write操作使用
    - write(写入):作用于主内存的变量,它把store操作从工作内存中得到的变量的值放入主内存的变量中

- Java内存模型还规定了在执行上述8种基本操作时必须满足如下规则
    - 不允许read和load,store和write操作之一单独出现,即不允许一个变量从主内存读取了但工作内存不接受,或者工作内存发起回写了但主内存不接受的情况出现
    - 不允许一个线程丢弃它最近的assign操作,即变量在工作内存中改变了之后必须把该变化同步回主内存
    - 不允许一个线程无原因地(没有发生过任何assign操作)把数据从线程的工作内存同步回主内存中
    - 一个新的变量只能在主内存中诞生,不允许在工作内存中直接使用一个未被初始化(load或assign)的变量,换句话说就是对一个变量实施use,store操作之前,必须先执行load,assign操作
    - 一个变量在同一个时刻只允许一条线程对其进行lock操作,但lock操作可以被同一条线程重复执行多次,多次执行lock后,只有执行相同次数的unlock操作,变量才会被解锁
    - 如果对一个变量执行lock操作,那将会清空工作内存中此变量的值,在执行引擎使用这个变量前,需要重新执行load或assign操作以初始化变量的值
    - 如果一个变量事先没有被lock操作锁定,那就不允许对它执行unlock操作,也不允许去unlock一个被其他线程锁定的变量
    - 对一个变量执行unlock操作之前,必须先把此变量同步回主内存中(执行store,write操作)

- 如果要把一个变量从主内存拷贝到工作内存,那就要按顺序执行read和load操作,如果要把变量从工作内存同步回主内存,就要按顺序执行store和write操作
    - 注意,Java内存模型只要求上述两个操作必须按顺序执行,但不要求是连续执行
    - 也就是说read与load之间,store与write之间是可插入其他指令的,如对主内存中的变量a,b进行访问时,一种可能出现的顺序是read a,read b,load b,load a

### 对于volatile型变量的特殊规则

- 关键字volatile可以说是Java虚拟机提供的最轻量级的同步机制,Java内存模型为volatile专门定义了一些特殊的访问规则,当一个变量被定义成volatile之后,它将具备两项特性
    - 第一项是保证此变量对所有线程的可见性,指当一条线程修改了这个变量的值,新值对于其他线程来说是可以立即得知的
    - 第二个语义是禁止指令重排序优化

- Java内存模型中对volatile变量定义的特殊规则的定义,假定T表示一个线程,V和W分别表示两个volatile型变量,那么在进行read,load,use,assign,store和write操作时需要满足如下规则
    - 这条规则要求在工作内存中,每次使用V前都必须先从主内存刷新最新的值,用于保证能看见其他线程对变量V所做的修改
        - 只有当线程T对变量V执行的前一个动作是load的时候,线程T才能对变量V执行use动作;
        - 并且只有当线程T对变量V执行的后一个动作是use的时候,线程T才能对变量V执行load动作
        - 线程T对变量V的use动作可以认为是和线程T对变量V的load,read动作相关联的,必须连续且一起出现
    - 这条规则要求在工作内存中,每次修改V后都必须立刻同步回主内存中,用于保证其他线程可以看到自己对变量V所做的修改
        - 只有当线程T对变量V执行的前一个动作是assign的时候,线程T才能对变量V执行store动作
        - 并且只有当线程T对变量V执行的后一个动作是store的时候,线程T才能对变量V执行assign动作
        - 线程T对变量V的assign动作可以认为是和线程T对变量V的store,write动作相关联的,必须连续且一起出现
    - 这条规则要求volatile修饰的变量不会被指令重排序优化,从而保证代码的执行顺序与程序的顺序相同
        - 假定动作A是线程T对变量V实施的use或assign动作,假定动作F是和动作A相关联的load或store动作,假定动作P是和动作F相应的对变量V的read或write动作;
        - 与此类似,假定动作B是线程T对变量W实施的use或assign动作,假定动作G是和动作B相关联的load或store动作,假定动作Q是和动作G相应的对变量W的read或write动作,如果A先于B,那么P先于Q

### 针对long和double型变量的特殊规则

### 原子性,可见性与有序性

- 原子性(Atomicity)
    - 由Java内存模型来直接保证的原子性变量操作包括read,load,assign,use,store和write这六个(例外就是long和double的非原子性协定)
    - 如果应用场景需要一个更大范围的原子性保证(经常会遇到),Java内存模型还提供了lock和unlock操作来满足这种需求
- 可见性(Visibility)
    - 可见性就是指当一个线程修改了共享变量的值时,其他线程能够立即得知这个修改
    - 除了volatile之外,synchronized和final也可以保证可见性
        - 同步块的可见性是由对一个变量执行unlock操作之前,必须先把此变量同步回主内存中(store,write)
        - final关键字的可见性,被final修饰的字段在构造器中一旦被初始化完成,并且构造器没有把this的引用传递出去,那么在其他线程中就能看见final字段的值
- 有序性(ordering)

### 先行发生原则

- Java语言中有一个先行发生Happens-Before的原则
    - 程序次序规则(Program Order Rule)
        - 在一个线程内,按照控制流顺序,书写在前面的操作先行发生于书写在后面的操作.注意,这里说的是控制流顺序而不是程序代码顺序,因为要考虑分支,循环等结构
    - 管程锁定规则(Monitor Lock Rule)
        - 一个unlock操作先行发生于后面对同一个锁的lock操作
    - volatile变量规则(Volatile Variable Rule)
        - 对一个volatile变量的写操作先行发生于后面对这个变量的读操作
    - 线程启动规则(Thread Start Rule)
        - Thread对象的start()方法先行发生于此线程的每一个动作
    - 线程终止规则(Thread Termination Rule)
        - 线程中的所有操作都先行发生于对此线程的终止检测,我们可以通过Thread::join()方法是否结束,Thread::isAlive()的返回值等手段检测线程是否已经终止执行
    - 线程中断规则(Thread Interruption Rule)
        - 对线程interrupt()方法的调用先行发生于被中断线程的代码检测到中断事件的发生,可以通过Thread::interrupted()方法检测到是否有中断发生
    - 对象终结规则(Finalizer Rule)
        - 一个对象的初始化完成(构造函数执行结束)先行发生于它的finalize()方法的开始
    - 传递性(Transitivity)
        - 如果操作A先行发生于操作B,操作B先行发生于操作C,那就可以得出操作A先行发生于操作C的结论

## Java与线程

- 目前线程是Java里面进行处理器资源调度的最基本单位,线程是比进程更轻量级的调度执行单位

### 线程的实现

- 实现线程主要有三种方式
    - 使用内核线程实现(1:1实现)
    - 使用用户线程实现(1:N实现)
    - 使用用户线程加轻量级进程混合实现(N:M实现)


- 内核线程实现
    - 使用内核线程实现的方式也被称为1:1实现,内核线程(Kernel-Level Thread,KLT)就是直接由操作系统内核(Kernel,内核)支持的线程
    - 这种线程由内核来完成线程切换,内核通过操纵调度器(Scheduler)对线程进行调度,并负责将线程的任务映射到各个处理器上
    - 每个内核线程可以视为内核的一个分身,这样操作系统就有能力同时处理多件事情,支持多线程的内核就称为多线程内核((Multi-Threads Kernel)
    - 程序一般不会直接使用内核线程,而是使用内核线程的一种高级接口—轻量级进程(Light Weight Process,LWP)
    - 轻量级进程就是我们通常意义上所讲的线程,由于每个轻量级进程都由一个内核线程支持,因此只有先支持内核线程,才能有轻量级进程

- 用户线程实现
    - 使用用户线程实现的方式被称为1:N实现
    - 广义上来讲,一个线程只要不是内核线程,都可以认为是用户线程(User Thread,UT)
      的一种,从这个定义上看,轻量级进程也属于用户线程但轻量级进程的实现始终是建立在内核之上的,许多操作都要进行系统调用,因此效率会受到限制,并不具备通常意义上的用户线程的优点
    - 狭义上的用户线程指的是完全建立在用户空间的线程库上,系统内核不能感知到用户线程的存在及如何实现的,用户线程的建立,同步,销毁和调度完全在用户态中完成,不需要内核的帮助
        - 如果程序实现得当,这种线程不需要切换到内核态,因此操作可以是非常快速且低消耗的,也能够支持规模更大的线程数量,部分高性能数据库中的多线程就是由用户线程实现的
    - 用户线程的优势在于不需要系统内核支援,劣势也在于没有系统内核的支援,所有的线程操作都需要由用户程序自己去处理
        - 线程的创建,销毁,切换和调度都是用户必须考虑的问题,而且由于操作系统只把处理器资源分配到进程,那诸如"阻塞如何处理""多处理器系统中如何将线程映射到其他处理器上"这类问题解决起来将会异常困难

- 线程除了依赖内核线程实现和完全由用户程序自己实现之外还有一种将内核线程与用户线程一起使用的实现方式被称为N:M实现
    - 用户线程还是完全建立在用户空间中因此用户线程的创建,切换,析构等操作依然廉价并且可以支持大规模的用户线程并发
    - 而操作系统支持的轻量级进程则作为用户线程和内核线程之间的桥梁这样可以使用内核提供的线程调度功能及处理器映射并且用户线程的系统调用要通过轻量级进程来完成这大大降低了整个进程被完全阻塞的风险

- Java线程的实现
    - Java线程如何实现并不受Java虚拟机规范的约束,这是一个与具体虚拟机相关的话题
    - Java线程在早期的Classic虚拟机上(JDK1.2之前),基于一种被称为绿色线程(Green Threads)的用户线程实现
    - 从JDK1.3起,主流平台上的主流商用Java虚拟机的线程模型普遍都被替换为基于操作系统原生线程模型来实现,即采用1:1的线程模型

### Java线程调度

- 线程调度是指系统为线程分配处理器使用权的过程,调度主要方式有两种
    - 协同式(Cooperative Threads-Scheduling)线程调度
    - 抢占式(Preemptive Threads-Scheduling)线程调度

- 协同式调度的多线程系统线程的执行时间由线程本身来控制线程把自己的工作执行完了之后要主动通知系统切换到另外一个线程上去
    - 协同式多线程的最大好处是实现简单,而且由于线程要干完事后才会进行线程切换,切换操作对线程自己是可知的,一般没有线程同步问题
    - 线程执行时间不可控制甚至如果一个线程的代码编写有问题一直不告知系统进行线程切换那么程序就会一直阻塞在那里

- 抢占式调度的多线程系统,每个线程将由系统来分配执行时间,线程的切换不由线程本身来决定
    - Java中有Thread::yield()方法可以主动让出执行时间但是如果想要主动获取执行时间线程本身是没有什么办法的
    - 线程的执行时间是系统可控的也不会有一个线程导致整个进程甚至整个系统阻塞的问题
    - Java使用的线程调度方式就是抢占式调度

### 状态转换

- Java语言定义了6种线程状态,在任意一个时间点中一个线程只能有且只有其中的一种状态,并且可以通过特定的方法在不同状态之间转换
    - 新建(new)
        - 创建后尚未启动的线程处于这种状态
    - 运行(Runnable)
        - 包括操作系统线程状态中的Running和Ready也就是处于此状态的线程有可能正在执行也有可能正在等待着操作系统为它分配执行时间
    - 无限期等待(Waiting)
        - 处于这种状态的线程不会被分配处理器执行时间,它们要等待被其他线程显式唤醒,以下方法会让线程陷入无限期的等待状态
            - 没有设置Timeout参数的Object::wait()方法
            - 没有设置Timeout参数的Thread::join()方法
            - LockSupport::park()方法
    - 限期等待(Timed Waiting)
        - 处于这种状态的线程也不会被分配处理器执行时间,不过无须等待被其他线程显式唤醒,在一定时间之后它们会由系统自动唤醒
            - Thread::sleep()方法
            - 设置了Timeout参数的Object::wait()方法
            - 设置了Timeout参数的Thread::join()方法
            - LockSupport::parkNanos()方法
            - LockSupport::parkUntil()方法
    - 阻塞(Blocked)
        - 线程被阻塞了
            - 阻塞状态和等待状态的区别是阻塞状态在等待着获取一个排它锁,这个事件将在另外一个线程放弃这个锁的时候发生
            - 等待状态,则是在等待一段时间或者唤醒动作的发生
            - 在程序等待进入同步区域的时候,线程将进入这种状态
    - 结束(Terminated)
        - 已终止线程的线程状态,线程已经结束执行

## Java与协程

- Java语言抽象出来隐藏了各种操作系统线程差异性的统一线程接口

### 内核线程的局限

- 1:1的内核线程模型是如今Java虚拟机线程实现的主流选择,但是这种映射到操作系统上的线程天然的缺陷是切换,调度成本高昂,系统能容纳的线程数量也很有限

### 协程的复苏

- 为什么内核线程调度切换起来成本就要更高
    - 内核线程的调度成本主要来自用户态与核心态之间的状态转换,而这两种状态转换的开销主要来自响应中断,保护和恢复执行现场的成本

- 由于最初多数的用户线程是被设计成协同式调度(Cooperative Scheduling)的,所以它有了一个别名—协程(Coroutine),又由于这时候的协程会完整地做调用栈的保护,恢复工作,所以今天也被称为有栈协程(
  StackFull Coroutine),起这样的名字是为了便于跟后来的无栈协程(Stackless Coroutine)区分开

### Java的解决方案

- 对于有栈协程,有一种特例实现名为纤程(Fiber)













