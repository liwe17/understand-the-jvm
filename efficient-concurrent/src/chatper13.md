# 线程安全与锁优化

## 概述

- 高效并发首先需要保证并发的正确性,然后在此基础上来实现高效

## 线程安全

- 当多个线程同时访问一个对象时,如果不用考虑这些线程在运行时环境下的调度和交替执行,也不需要进行额外的同步,或者在调用方进行任何其他的协调操作,调用这个对象的行为都可以获得正确的结果,那就称这个对象是线程安全的
- 代码本身封装了所有必要的正确性保障手段如互斥同步等,令调用者无须关心多线程下的调用问题,更无须自己实现任何措施来保证多线程环境下的正确调用

### Java语言中的线程安全

- 多个线程之间存在共享数据访问是前提,如果根本不存在多线程又或者不存在共享数据,那么从线程安全的角度看,程序是串行执行还是多线程执行对它来说是没有什么区别的

- Java语言中各种操作共享数据分为五类:不可变,绝对线程安全,相对线程安全,线程兼容和线程独立
    - 不可变
        - 不可变(Immutable)的对象一定是线程安全的,无论是对象的方法实现还是方法的调用者,都不需要再进行任何线程安全保障措施
        - 不可变带来的安全性是最直接,最纯粹的
        - Java语言中,如果多线程共享的数据是一个基本数据类型,那么只要在定义时使用final关键字修饰它就可以保证它是不可变的
        - 如果共享数据是一个对象,由于Java语言目前暂时还没有提供值类型的支持,那就需要对象自行保证其行为不会对其状态产生任何影响才行
            - 保证对象行为不影响自己状态的途径有很多种,最简单的一种就是把对象里面带有状态的变量都声明为final,这样在构造函数结束之后,它就是不可变的
    - 绝对线程安全
        - 在Java API中标注自己是线程安全的类,大多数都不是绝对的线程安全
            - Vector的get(),remove()和size()方法都是同步的,但是在多线程的环境中,如果不在方法调用端做额外的同步措施,使用这段代码仍然是不安全的
            - 因为如果另一个线程恰好在错误的时间里删除了一个元素,导致序号i已经不再可用,再用i访问数组就会抛出一个ArrayIndexOutOfBoundsException异常
    - 相对线程安全
        - 相对线程安全就是我们通常意义上所讲的线程安全,它需要保证对这个对象单次的操作是线程安全的,我们在调用的时候不需要进行额外的保障措施,但是对于一些特定顺序的连续调用,就可能需要在调用端使用额外的同步手段来保证调用的正确性
        - 例如Vector,HashTable,Collections的synchronizedCollection()方法包装的集合等
    - 线程兼容
        - 线程兼容是指对象本身并不是线程安全的,但是可以通过在调用端正确地使用同步手段来保证对象在并发环境中可以安全地使用
    - 线程对立
        - 线程对立是指不管调用端是否采取了同步措施,都无法在多线程环境中并发使用代码
        - 由于Java语言天生就支持多线程的特性,线程对立这种排斥多线程的代码是很少出现的,而且通常都是有害的,应当尽量避免
            - 一个线程对立的例子是Thread类的suspend()和resume()方法
                - 如果有两个线程同时持有一个线程对象,一个尝试去中断线程,一个尝试去恢复线程,在并发进行的情况下,无论调用时是否进行了同步,目标线程都存在死锁风险

### 线程安全的实现方法

#### 互斥同步

- 互斥同步(Mutual Exclusion & Synchronization)是一种最常见也是最主要的并发正确性保障手段
    - 同步是指在多个线程并发访问共享数据时,保证共享数据在同一个时刻只被一条(或者是一些,当使用信号量的时候)线程使用
    - 互斥是实现同步的一种手段,临界区(Critical Section),互斥量(Mutex)和信号量(Semaphore)都是常见互斥实现方式

- 在Java里面,最基本的互斥同步手段就是synchronized关键字,这是一种块结构(Block Structured)的同步语法
    - 经过Javac编译后,会在同步块的前后分别形成monitorenter和monitorexit这两个字节码指令
    - 这两个字节码指令都需要一个reference类型的参数来指明要锁定和解锁的对象
        - 如果Java源码中的synchronized明确指定了对象参数,那就以这个对象的引用作为reference
        - 如果没有明确指定,那将根据synchronized修饰的方法类型(如实例方法或类方法),来决定是取代码所在的对象实例还是取类型对应的Class对象来作为线程要持有的锁

- 两个关于synchronized的直接推论,这是使用它时需特别注意
    - 被synchronized修饰的同步块对同一条线程来说是可重入的,这意味着同一线程反复进入同步块也不会出现自己把自己锁死的情况
    - 被synchronized修饰的同步块在持有锁的线程执行完毕并释放锁之前,会无条件地阻塞后面其他线程的进入,这意味着无法像处理某些数据库中的锁那样,强制已获取锁的线程释放锁;也无法强制正在等待锁的线程中断等待或超时退出


- ReentrantLock与synchronized相比增加了一些高级功能,主要有以下三项:等待可中断,可实现公平锁及锁可以绑定多个条件
    - 等待可中断
        - 当持有锁的线程长期不释放锁的时候,正在等待的线程可以选择放弃等待,改为处理其他事情.可中断特性对处理执行时间非常长的同步块很有帮助
    - 公平锁
        - 多个线程在等待同一个锁时,必须按照申请锁的时间顺序来依次获得锁;而非公平锁则不保证这一点,在锁被释放时,任何一个等待锁的线程都有机会获得锁
        - synchronized中的锁是非公平的,ReentrantLock在默认情况下也是非公平的
        - ReentrantLock可以通过带布尔值的构造函数要求使用公平锁.不过一旦使用了公平锁,将会导致ReentrantLock的性能急剧下降,会明显影响吞吐量
    - 锁绑定多个条件
        - 一个ReentrantLock对象可以同时绑定多个Condition对象
            - 在synchronized中,锁对象的wait()跟它的notify()或者notifyAll()方法配合可以实现一个隐含的条件,如果要和多于一个的条件关联的时候,就不得不额外添加一个锁
            - ReentrantLock则无须这样做,多次调用newCondition()方法即可

#### 非阻塞同步

- 互斥同步面临的主要问题是进行线程阻塞和唤醒所带来的性能开销,因此这种同步也被称为阻塞同步(Blocking Synchronization)
    - 互斥同步属于一种悲观并发策略,其总是认为只要不去做正确同步措施
    - 无论共享的数据是否真的会出现竞争,它都会进行加锁这将会导致用户态到核心态转换,维护锁计数器和检查是否有被阻塞的线程需要被唤醒等开销

- 随着硬件指令集的发展,产生了基于冲突检测的乐观并发策略,这种乐观并发策略的实现不再需要把线程阻塞挂起,因此这种同步操作被称为非阻塞同步(Non-Blocking Synchronization)
    - 先进行操作,如果没有其他线程争用共享数据,那操作就直接成功了;如果共享的数据的确被争用,产生了冲突,那再进行其他的补偿措施,最常用的补偿措施是不断地重试,直到出现没有竞争的共享数据为止
    - 使用这种措施的代码也常被称为无锁(Lock-Free)编程

- 多次操作的行为可以只通过一条处理器指令就能完成,这类常用指令
    - 测试并设置(Test-and-Set)
    - 获取并增加(Fetch-and-Increment)
    - 交换(Swap)
    - 比较并交换(Compare-and-Swap,下文称CAS),在IA64,x86指令集中有用cmpxchg指令完成的CAS功能
    - 加载链接/条件储存(Load-Linked/Store-Conditional,下文称LL/SC)


- CAS指令需要有三个操作数,分别是内存位置(在Java中可以简单地理解为变量的内存地址,用V表示),旧的预期值(用A表示)和准备设置的新值(用B表示)
    - CAS指令执行时,当且仅当V符合A时,处理器才会用B更新V的值,否则它就不执行更新
    - 但是,不管是否更新了V的值,都会返回V的旧值,上述的处理过程是一个原子操作,执行期间不会被其他线程中断

#### 无同步方案

- 要保证线程安全,也并非一定要进行阻塞或非阻塞同步,同步与线程安全两者没有必然的联系

- 同步只是保障存在共享数据争用时正确性的手段,如果能让一个方法本来就不涉及共享数据,那它自然就不需要任何同步措施去保证其正确性,因此会有一些代码天生就是线程安全的
    - 可重入代码(Reentrant Code)也成为纯代码(Pure Code)
        - 可以在代码执行的任何时刻中断它,转而去执行另外一段代码包括递归调用它本身,而在控制权返回后,原来的程序不会出现任何错误,也不会对结果有所影响
            - 不依赖全局变量,存储在堆上的数据和公用的系统资源,用到的状态量都由参数中传入,不调用非可重入的方法等
    - 线程本地存储(Thread Local Storage)
        - 如果一段代码中所需要的数据必须与其他代码共享,那就看看这些共享数据的代码是否能保证在同一个线程中执行,如果能保证,我们就可以把共享数据的可见范围限制在同一个线程之内,这样,无须同步也能保证线程之间不出现数据争用的问题
            - 大部分使用消费队列的架构模式都会将产品的消费过程限制在一个线程中消费完

## 锁优化

- 高效并发是从JDK5升级到JDK6后一项重要的改进项,HotSpot虚拟机开发团队在这个版本上花费了大量的资源去实现各种锁优化技术,如适应性自旋(Adaptive Spinning),锁消除(Lock Elimination),锁膨胀(
  Lock Coarsening),轻量级锁(Lightweight Locking),偏向锁(Biased Locking)等,这些技术都是为了在线程之间更高效地共享数据及解决竞争问题,从而提高程序的执行效率

### 自旋锁与自适应自旋

- 互斥同步对性能最大的影响是阻塞的实现,挂起线程和恢复线程的操作都需要转入内核态中完成,这些操作给Java虚拟机的并发性能带来了很大的压力
    - 如果物理机器有一个以上的处理器或者处理器核心,能让两个或以上的线程同时并行执行,我们就可以让后面请求锁的那个线程稍等一会,但不放弃处理器的执行时间,看看持有锁的线程是否很快就会释放锁
    - 为了让线程等待,我们只须让线程执行一个忙循环(自旋)这项技术就是所谓的自旋锁

- 自旋锁在JDK1.4.2已经引入默认关闭,使用-XX:+UseSpinning参数开启,JDK6默认开启了,默认值是10次,用户也可以使用参数-XX:PreBlockSpin来自行更改

- JDK6对自旋锁优化,引入自适应自旋锁.自适应意味着自旋的时间不在是固定的了,由前一次在同一个锁上的自旋时间及锁的拥有者的状态来决定的
    - 如果在同一个锁对象上,自旋等待刚刚成功获得过锁,并且持有锁的线程正在运行中,那么虚拟机就会认为这次自旋也很有可能再次成功,进而允许自旋等待持续相对更长的时间,比如持续100次忙循环
    - 如果对于某个锁,自旋很少成功获得过锁,那在以后要获取这个锁时将有可能直接省略掉自旋过程,以避免浪费处理器资源

### 锁消除

- 锁消除是指虚拟机即时编译器在运行时,对一些代码要求同步,但是对检测到不可能存在共享数据竞争的锁进行消除
- 锁消除的主要判定依据来源于逃逸分析的数据支持,如果判断到一段代码中,在堆上的所有数据都不会逃逸出去被其他线程访问到,那就可以把它们当作栈上数据对待,认为它们是线程私有的,同步加锁自然就无须再进行

### 锁粗化

- 原则上,我们在编写代码的时候,总是推荐将同步块的作用范围限制得尽量小——只在共享数据的实际作用域中才进行同步,这样是为了使得需要同步的操作数量尽可能变少,即使存在锁竞争,等待锁的线程也能尽可能快地拿到锁
- 但是如果一系列的连续操作都对同一个对象反复加锁和解锁,甚至加锁操作是出现在循环体之中的,那即使没有线程竞争,频繁地进行互斥同步操作也会导致不必要的性能损耗

```
public String concatString(String s1, String s2, String s3) {
    StringBuffer sb = new StringBuffer();
    sb.append(s1);
    sb.append(s2);
    sb.append(s3);
    return sb.toString();
}
```

### 轻量级锁

- 轻量级锁是JDK6时加入的新型锁机制,它名字中的轻量级是相对于使用操作系统互斥量来实现的传统锁而言的,因此传统的锁机制就被称为重量级锁
    - 轻量级锁并不是用来代替重量级锁的,它设计的初衷是在没有多线程竞争的前提下,减少传统的重量级锁使用操作系统互斥量产生的性能消耗

- 轻量锁的加锁工作过程
    - 在代码即将进入同步块的时候,如果此同步对象没有被锁定(锁标志位为01状态),虚拟机首先将在当前线程的栈帧中建立一个名为锁记录(LockRecord)的空间,用于存储锁对象目前的MarkWord的拷贝(
      官方为这份拷贝加了一个Displaced前缀,即Displaced MarkWord)
    - 虚拟机将使用CAS操作尝试把对象的MarkWord更新为指向LockRecord的指针,如果这个更新动作成功了,即代表该线程拥有了这个对象的锁,并且对象MarkWord的锁标志位(MarkWork)
      将转变为00,表示处于轻量锁阶段
    - 如果这个更新操作失败了,虚拟机首先会检查对象的MarkWord是否指向当前线程的栈帧
        - 如果是,说明当前线程已经拥有了这个对象的锁,那直接进入同步块继续执行就可以了
        - 如果不是,那就意味着至少存在一条线程与当前线程竞争获取该对象的锁,并且这个锁对象已经被其他线程抢占了
        - 那轻量级锁就不再有效,必须要膨胀为重量级锁,锁标志的状态值变为10,此时MarkWork中存储就是指向重量级锁(互斥量)的指针
- 轻量锁的解锁过程,解锁过程也同样是通过CAS操作来进行的
    - 如果对象的MarkWord仍然指向线程的锁记录,那就用CAS操作把对象当前的MarkWord和线程中复制的Displaced MarkWord替换回来
        - 假如能够成功替换,那整个同步过程就顺利完成了
        - 如果替换失败,则说明有其他线程尝试过获取该锁,就要在释放锁的同时,唤醒被挂起的线程(此时存的已经变为互斥量指针)

### 偏向锁

- 偏向锁也是JDK6中引入的一项锁优化措施,它的目的是消除数据在无竞争情况下的同步原语,进一步提高程序的运行性能

- 偏向锁的原理
    - 假设当前虚拟机启用了偏向锁,那么当锁对象第一次被线程获取的时候,虚拟机将会把对象头中的标志位设置为01把偏向模式设置为1,表示进入偏向模式
        - 启用参数-XX:+UseBiasedLocking,这是自JDK6起HotSpot虚拟机的默认值
    - 同时使用CAS操作把获取到这个锁的线程的ID记录在对象的MarkWord之中
        - 如果CAS操作成功,持有偏向锁的线程以后每次进入这个锁相关的同步块时,虚拟机都可以不再进行任何同步操作,例如加锁,解锁及对MarkWord的更新操作等
    - 一旦出现另外一个线程去尝试获取这个锁的情况,偏向模式就马上宣告结束
        - 根据锁对象目前是否处于被锁定的状态决定是否撤销偏向(偏向模式设置为0),撤销后标志位回复到未锁定(01),或者轻量级锁定(00)的状态

- 当一个对象已经计算过一致性哈希码后,它就再也无法进入偏向锁状态了;而当一个对象当前正处于偏向锁状态,又收到需要计算其一致性哈希码请求时,它的偏向状态会被立即撤销,并且锁会膨胀为重量级锁




























