### JS设置cookie
假设在A页面中要保存变量username的值("jack")到cookie中,key值为name，则相应的JS代码为：

	document.cookie="name="+username;

在cookie 的名或值中不能使用分号（;）、逗号（,）、等号（=）以及空格。在cookie的名中做到这点很容易，但要保存的值是不确定的。如何来存储这些值呢？方 法是用escape()函数进行编码，它能将一些特殊符号使用十六进制表示，例如空格将会编码为“20%”，从而可以存储于cookie值中，而且使用此 种方案还可以避免中文乱码的出现。

	document.cookie="str="+escape("I love ajax"); 
	// document.cookie="str=I%20love%20ajax";
当使用escape()编码后，在取出值以后需要使用unescape()进行解码才能得到原来的cookie值
### JS读取cookie
假设cookie中存储的内容为：`name=jack;password=123`
则在B页面中获取变量username的值的JS代码如下：

	var username=document.cookie.split(";")[0].split("=")[1];
	//JS操作cookies方法!
	//写cookies
	function setCookie(name,value)
	{
	var Days = 30;
	var exp = new Date();
	exp.setTime(exp.getTime() + Days*24*60*60*1000);
	document.cookie = name + "="+ escape (value) + ";expires=" + exp.toGMTString();

###Cookie的优缺点
cookie虽然在持久保存客户端数据提供了方便，分担了服务器存储的负担，但还是有很多局限性的。
#### 第一：每个特定的域名下最多生成20个cookie
1.	IE6或更低版本最多20个cookie
2.	IE7和之后的版本最后可以有50个cookie。
3.	Firefox最多50个cookie
4.	chrome和Safari没有做硬性限制,IE和Opera 会清理近期最少使用的cookie，Firefox会随机清理cookie。cookie的最大大约为4096字节，为了兼容性，一般不能超过4095字节。
IE 提供了一种存储可以持久化用户数据，叫做uerData，从IE5.0就开始支持。每个数据最多128K，每个域名下最多1M。这个持久化数据放在缓存中，如果缓存没有清理，那么会一直存在。
#### 优点：极高的扩展性和可用性
1.	通过良好的编程，控制保存在cookie中的session对象的大小。
2.	通过加密和安全传输技术（SSL），减少cookie被破解的可能性。
3.	只在cookie中存放不敏感数据，即使被盗也不会有重大损失。
4.	控制cookie的生命期，使之不会永远有效。偷盗者很可能拿到一个过期的cookie。
#### 缺点：
1.	`Cookie`数量和长度的限制。每个domain最多只能有20条cookie，每个cookie长度不能超过4KB，否则会被截掉。
2.	安全性问题。如果cookie被人拦截了，那人就可以取得所有的session信息。即使加密也与事无补，因为拦截者并不需要知道cookie的意义，他只要原样转发cookie就可以达到目的了。
3.	有些状态不可能保存在客户端。例如，为了防止重复提交表单，我们需要在服务器端保存一个计数器。如果我们把这个计数器保存在客户端，那么它起不到任何作用。
以

----------

参考[http://www.cnblogs.com/fly-xfa/archive/2017/05/12/6846429.html](http://www.cnblogs.com/fly-xfa/archive/2017/05/12/6846429.html)


