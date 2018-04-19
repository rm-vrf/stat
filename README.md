# STAT hub

<a name="1"></a>
## 1.简要说明

STAT hub是一个集群管理/应用编排/进程守护工具，可以用来高效的管理服务集群。功能有：

- 节点管理：管理集群中的节点，掌握节点的状态，了解每个节点上运行了哪些进程；
- 应用管理：创建和管理应用，制定应用的分配规则，了解进程的数量和分配情况。

STAT hub分为master和agent两个部分。master提供应用操作界面，用户可以编排服务；agent运行在工作节点上，守护工作进程。master和agent之间使用HTTP协议通信。

<a name="2"></a>
## 2.运行环境

- 操作系统
	- master：基于纯Java环境，可以在大部分操作系统上运行：Windows、Linux、macOS
	- agent：Linux操作系统，内核版本2.6.24以上，x86或arm架构，32或64位都经过测试；
- 基础环境：Java 1.8+

<a name="3"></a>
## 3.构建下载
<a name="3.1"></a>
### 3.1 源码编译

代码地址：

https://github.com/lane-cn/stat

运行构建命令：

```
mvn package
```

得到构建包：

- agent/target/stat-agent-1.0.0-SNAPSHOT.jar
- server/target/stat-server-1.0.0-SNAPSHOT.jar

<a name="3.2"></a>
### 3.2 下载

下载构建包：

- http://121.199.25.213/stat/stat-agent-1.0.0-SNAPSHOT.jar
- http://121.199.25.213/stat/stat-server-1.0.0-SNAPSHOT.jar

<a name="4"></a>
## 4.启动运行
<a name="4.1"></a>
### 4.1 master

启动master进程：

```
java -jar stat-server-1.0.0-SNAPSHOT.jar
```

完整的启动参数：

| 参数 | 说明 | 默认值 |
| --- | --- | --- |
| server.port | 服务端口 | 51026 |
| store.directory | 数据存储路径 | ${HOME}/.stat_server |
| elastic.http.port | 内置Elasticsearch存储端口（HTTP） | 51027 |
| elastic.tcp.port | 内置Elasticsearch存储端口（TCP） | 51028 |

启动后在以下位置提供服务：

| 服务地址 | 说明 |
| --- | --- |
| http://master:51026 | 用户界面 |
| http://master:51026/_plugin/head | Elasticsearch数据管理界面 |
| http://master:51027 | Elasticsearch HTTP服务端口 |
| tcp://master:51028 | Elasticsearch TCP服务端口 |

<a name="4.2"></a>
### 4.2 agent

启动agent进程：

```
java -jar stat-agent-1.0.0-SNAPSHOT.jar --master.address=http://master:51026
```

完整的启动参数：

| 参数 | 说明 | 默认值 |
| --- | --- | --- |
| master.address | master位置。可以不指定，以独立模式运行 |  |
| server.port | 服务端口 | 51025 |
| store.directory | 数据存储路径 | ${HOME}/.stat_agent |
| agent.address | 一般情况下agent可以自动报告自己的地址。在某些环境下无法获取正确的IP地址，需要指定agent.address参数 |  |

启动后在以下位置提供服务：

| 服务地址 | 说明 |
| --- | --- |
| http://agent:51025 | 用户界面 |

<a name="5"></a>
## 5.开始使用
<a name="5.1"></a>
### 5.1 节点
<a name="5.1.1"></a>
#### 5.1.1 定义

agent启动后向master报告节点信息。节点信息如下：

```
{
    "hostname": "hostname1",
    "agentAddress": "http://192.168.1.165:51025",
    "os": {
        "name": "Mac OS X",
        "version": "10.13.1",
        "cpus": 4,
        "architecture": "x86_64"
    },    
    "memory": {
        "total": 8589934592,
        "ram": 8589934592
    }, 
    "networks": [{
            "address": "192.168.1.165",
            "name": "en4",
            "mtu": 1500,
            "broadcast": "192.168.1.255",
            "description": "en4",
            "distination": "0.0.0.0",
            "flags": 34915,
            "hwaddr": "00:9A:9F:9F:A6:EB",
            "metric": 0,
            "netmask": "255.255.255.0",
            "type": "Ethernet"
    }],
    "disks": [{
            "dirName": "/",
            "devName": "/dev/disk1s1",
            "flags": 0,
            "option": "rw,local,rootfs",
            "sysTypeName": "apfs",
            "type": 1,
            "typeName": "none",
            "total": 250790436864
        },{
            "dirName": "/private/var/vm",
            "devName": "/dev/disk1s4",
            "flags": 0,
            "option": "rw,noexec,noatime,local",
            "sysTypeName": "apfs",
            "type": 1,
            "typeName": "none",
            "total": 250790436864
    }],
    "tags": [
        "appserver",
        "ssd"
    ]
}
```

标签属性可以在用户界面上修改；其他信息是自动收集的，不用修改。

<a name="5.1.2"></a>
#### 5.1.2 检索

节点检索可以用来对节点进行查询和管理，也可以用来对进程运行位置进行规划。使用全文检索语法对节点进行检索，最简单的检索语法是一个词，比如`hostname1`、`appserver`。复杂一些的检索可以使用通配符，比如这个通配符，可以返回所有的节点：

```
*
```

用这个通配符，可以返回所有以“hostname”开头的节点

```
hostname*
```

可以使用通配符查询IP地址段：

```
192.168.1.*
```

可以在查询语法中加上属性，这样查询更加精确：

```
networks.address:192.168.1.165
```

可以准确指定一个地址段，这样：

```
networks.address:[192.168.1.100 TO 192.168.1.150]
```

对数值属性可以查询数值范围，比如要查询CPU大于8核的节点：

```
os.cpus:>8
```

可以关联组合多个查询语法，比如要查询内存大于8G，并且地址在某个段内的节点，可以这样：

```
memory.total:>8000000000 AND networks.address:[192.168.1.100 TO 192.168.1.150]
```

可以用标签属性查询。为不同用途的节点添加标签，然后按照标签进行节点管理和应用编排，这是一个好办法：

```
tags:appserver AND tags:hpc
```

<a name="5.2"></a>
### 5.2 应用
<a name="5.2.1"></a>
#### 5.2.1 部署

应用是一个持续运行的服务，可以运行在多个节点上，每个节点上可以运行多个进程。应用定义了这些进程的启动运行参数。在STAT hub上部署一个应用：

```
{
    "name": "http_server",
    "toProcess": "python",
    "workingDirectory": "/opt",
    "envs": {
    },
    "args": [
        "http_server.py"
    ],
    "ports": [
        0,
        0
    ],
    "killSignal": 15,
    "uris": [
        "http://121.199.25.213/stat/http_server.py"
    ],
    "healthChecks": [{
            "initialDelaySeconds": 60,
            "intervalSeconds": 20,
            "maxConsecutiveFailures": 3,
            "path": "/",
            "portIndex": 0,
            "protocol": "HTTP",
            "timeoutSeconds": 20
    }],
    "resAlloc": null
}
```

![](https://raw.githubusercontent.com/lane-cn/stat/master/server/src/main/resources/META-INF/resources/images/new_app.png)

`http_server.py`是一个Python脚本，它启动一个HTTP监听端口。agent运行命令启动进程，在Linux环境下，命令会封装成`/bin/sh -c python http_server.py 8080`形式。进程启动后，agent会获取进程的`stderr`和`stdout`输出流。

agent在启动前会检查`uri`指定的位置。如果有更新的文件，把文件下载到本地工作目录。如果文件是`*.zip`格式，agent会尝试对文件进行解压。

可以指定进程使用的端口。`0`表示随机端口，由agent选择。多个端口输入的时候用逗号（半角）分隔。

agent对进程健康情况进行检查。进程启动60秒后，每隔20秒对第0个端口发起一个HTTP请求，检查请求的返回代码。如果连续3次返回错误代码，就杀死进程重新启动。

agent为进程分配必要的资源，包括内存、CPU核数、磁盘空间。//TODO 资源分配未实现

<a name="5.2.2"></a>
#### 5.2.2 编排

编排是控制服务的运行位置和进程数量。

![](https://raw.githubusercontent.com/lane-cn/stat/master/server/src/main/resources/META-INF/resources/images/app_choreo.png)

运行节点控制服务运行的位置，输入节点检索条件，默认的条件是`*`，代表所有节点。计划进程数控制服务运行的进程数。

应用启动后，master会按照编排规则自动分配服务进程。

<a name="6"></a>
## 6.待办事项

[ ] agent资源控制，使用cgroup控制进程使用的资源  
[ ] 优化agent与master之间的接口，减少数据流量  
[ ] 系统监控功能：网络、磁盘、CPU、内存...  
[ ] Windows系统支持：守护进程在windows环境下得不到工作进程的pid  

