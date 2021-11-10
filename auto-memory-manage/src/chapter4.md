# 虚拟机性能监控-故障处理工具

## 概述

- 给一个系统定位问题的时候
    - 知识,经验是关键基础
    - 数据是依据
    - 工具是运用知识处理数据的手段
        - 异常堆栈
        - 虚拟机运行日志
        - 垃圾收集器日志
        - 线程快照(threadDump/javaCore文件)
        - 堆转储快照(heapDump/hprof文件)
        - 等

## 基础故障处理工具

- JDK自带的工具(bin目录)
    - 商业授权工具
        - 主要是JMC及它要使用到的JFR
    - 正式支持工具
        - 这一类工具属于被长期支持的工具
        - 不同平台,不同版本的JDK之间,这类工具可能会略有差异,但是不会出现某一个工具突然消失的情况
    - 实验性工具
        - 这一类工具在它们的使用说明中被声明为没有技术支持,实验性质的
        - 事实上它们通常都非常稳定而且功能强大,也能在处理应用程序性能问题,定位故障时发挥很大的作用

- JMX的开启
    - 工作中需要监控运行于JDK5的虚拟机的程序,需要启动时新增"-Dcom.sun.management.jmxremote"
    - 监控程序运行于JDK6或以上版本的虚拟机之上,那JMX管理默认是开启的,虚拟机启动时无须再添加任何参数

### jps:虚拟机进程状况工具

- 类似于PS命令,查看进程情况
    - 可以列出正在运行的虚拟机进程
    - 显示虚拟机执行主类(Main Class,main()函数所在的类)名称以及这些进程的本地虚拟机唯一ID(LVMID，Local Virtual Machine Identifier)

- 语法

```
jps [ options ] [ hostid ]
```

- 参数含义

<table>
    <tr>
        <td>选项</td>
        <td>作用</td>
    </tr>
    <tr>
        <td>-q</td>
        <td>只输出LVMID,省略主类的名称</td>
    </tr>
    <tr>
        <td>-m</td>
        <td>输出虚拟机进程启动时传递给主类的main()函数的参数</td>
    </tr>
    <tr>
        <td>-l</td>
        <td>输出主类的全类名,如果进程是jar包,则输出jar的路径</td>
    </tr>
    <tr>
        <td>-v</td>
        <td>输出虚拟机进程启动的jvm参数</td>
    </tr>
</table>

### jstat:虚拟机统计信息监视工具

- jstat是用于监视虚拟机各种运行状态信息的命令行工具
    - 可以显示本地或者远程虚拟机进程中的类加载,内存,垃圾收集,即时编译等运行时数据

- 语法

```
jstat [ option vmid [interval[s|ms] [count]] ]

其中远程VMID,格式如下
[protocol:][//]lvmid[@hostname[:port]/servername]

```

- [jstat 参数详解](https://docs.oracle.com/javase/1.5.0/docs/tooldocs/share/jstat.html#class_option)

### jinfo:Java配置信息工具

- jinfo的作用是实时查看和调整虚拟机各项参数

- java -XX:+PrintFlagsFinal JDK6或以上版本可用

- 语法

```
Usage:
    jinfo [option] <pid>
        (to connect to running process)
    jinfo [option] <executable <core>
        (to connect to a core file)
    jinfo [option] [server_id@]<remote server IP or hostname>
        (to connect to remote debug server)

where <option> is one of:
    -flag <name>         to print the value of the named VM flag
    -flag [+|-]<name>    to enable or disable the named VM flag
    -flag <name>=<value> to set the named VM flag to the given value
    -flags               to print VM flags
    -sysprops            to print Java system properties
    <no option>          to print both of the above
    -h | -help           to print this help message

```

### jmap:Java内存映像工具

- jmap命令用于生成堆转储快照,也可以使用其他方式生成堆快照文件
    - 通过参数 -XX:+HeapDumpOnOutOfMemoryError
    - 通过参数 -XX:+HeapDumpOnCtrlBreak,可以使用[Ctrl]+[Break]键让虚拟机生成堆转储快照文件
    - 在Linux系统下通过Kill-3命令发送进程退出信号,也能拿到快照
- jmap它还可以查询finalize执行队列,Java堆和方法区的详细信息,如空间使用率,当前用的是哪种收集器等

- 命令

```
Usage:
    jmap [option] <pid>
        (to connect to running process)
    jmap [option] <executable <core>
        (to connect to a core file)
    jmap [option] [server_id@]<remote server IP or hostname>
        (to connect to remote debug server)

where <option> is one of:
    <none>               to print same info as Solaris pmap
    -heap                to print java heap summary
    -histo[:live]        to print histogram of java object heap; if the "live"
                         suboption is specified, only count live objects
    -clstats             to print class loader statistics
    -finalizerinfo       to print information on objects awaiting finalization
    -dump:<dump-options> to dump java heap in hprof binary format
                         dump-options:
                           live         dump only live objects; if not specified,
                                        all objects in the heap are dumped.
                           format=b     binary format
                           file=<file>  dump heap to <file>
                         Example: jmap -dump:live,format=b,file=heap.bin <pid>
    -F                   force. Use with -dump:<dump-options> <pid> or -histo
                         to force a heap dump or histogram when <pid> does not
                         respond. The "live" suboption is not supported
                         in this mode.
    -h | -help           to print this help message
    -J<flag>             to pass <flag> directly to the runtime system
```

### jhat:虚拟机堆转储快照分析工具

- jhat与jmap搭配使用,来分析jmap生成的堆转储快照,jhat内置了一个微型的HTTP/Web服务器,生成堆转储快照的分析结果后,可以在浏览器中查看
    - 一般不用,知道即可

### jstack:Java堆栈跟踪工具

- jstack命令用于生成虚拟机当前时刻的线程快照(一般称为threaddump或者javacore文件)
    - 线程快照就是当前虚拟机内每一条线程正在执行的方法堆栈的集合
        - 定位线程出现长时间停顿的原因

- 命令

```
Usage:
    jstack [-l] <pid>
        (to connect to running process)
    jstack -F [-m] [-l] <pid>
        (to connect to a hung process)
    jstack [-m] [-l] <executable> <core>
        (to connect to a core file)
    jstack [-m] [-l] [server_id@]<remote server IP or hostname>
        (to connect to a remote debug server)

Options:
    -F  to force a thread dump. Use when jstack <pid> does not respond (process is hung)
    -m  to print both java and native frames (mixed mode)
    -l  long listing. Prints additional information about locks
    -h or -help to print this help message
    
```

## 可视化故障处理工具

- 可视化工具
    - JConsole
        - JConsole是最古老,在JDK5时期就已经存在
    - JHSDB
    - VisualVM
        - JDK6 Update7中首次发布
    - JMC
        - JDK7 Update40首次发布,配合飞行记录仪(Java Flight Recorder，JFR)使用,付费

### JHSDB:基于服务性代理的调试工具

- JDK中提供了JCMD和JHSDB两个集成式的多功能工具箱,JHSDB是一款基于服务性代理实现的进程外调试工具

- 测试代码
    - com.weiliai.chapter4.JHSDBTestCase

### JConsole:Java监视与管理控制台

- JConsole是一款基于JMX(Java Management Extensions)的可视化,管理工具
    - 主要功能是通过JMX的MBean(Managed Bean)对系统进行信息收集和参数动态调整

- 测试代码
    - com.weiliai.chapter4.JConsoleTestCase

### VisualVM

- VisualVM是功能最强大的运行监视和故障处理程序之一

