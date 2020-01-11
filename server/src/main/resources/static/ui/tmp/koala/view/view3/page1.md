### 什么是跨域？
- 跨域是指一个域下的文档或脚本试图去请求另一个域下的资源.
- 本质上是由于浏览器实现的同源策略的限制。
- XmlHttpRequest只允许请求当前源（域名、协议、端口）的资源，所以AJAX是不允许跨域的。
- &lt;script&gt;,&lt;link&gt;,&lt;img&gt;,&lt;iframe&gt;这些标签是允许跨域的，但你并不能修 改这些资源，比如iframe里的内容。

### 常见跨域场景
			URL                                      说明                    是否允许通信
			http://www.domain.com/a.js
			http://www.domain.com/b.js         同一域名，不同文件或路径           允许
			http://www.domain.com/lab/c.js
			
			http://www.domain.com:8000/a.js
			http://www.domain.com/b.js         同一域名，不同端口                不允许
			 
			http://www.domain.com/a.js
			https://www.domain.com/b.js        同一域名，不同协议                不允许
			 
			http://www.domain.com/a.js
			http://192.168.4.12/b.js           域名和域名对应相同ip              不允许
			 
			http://www.domain.com/a.js
			http://x.domain.com/b.js           主域相同，子域不同                不允许
			http://domain.com/c.js
			 
			http://www.domain1.com/a.js
			http://www.domain2.com/b.js        不同域名                         不允许


### 跨域解决方案
#### 1、跨域资源共享（CORS）
普通跨域请求：只服务端设置Access-Control-Allow-Origin即可，前端无须设置，若要带cookie请求：前后端都需要设置。

需注意的是：由于同源策略的限制，所读取的cookie为跨域请求接口所在域的cookie，而非当前页。

目前，所有浏览器都支持该功能(IE8+：IE8/9需要使用XDomainRequest对象来支持CORS）)，CORS也已经成为主流的跨域解决方案。

- 1）前端设置：
 
		$.ajax({
		    ...
		   xhrFields: {
		       withCredentials: true    // 前端设置是否带cookie
		   },
		   crossDomain: true,   // 会让请求头中包含跨域的额外信息，但不会含cookie
		    ...
		});

- 2）服务器设置：
	
		/*
		 * 导入包：import javax.servlet.http.HttpServletResponse;
		 * 接口参数中定义：HttpServletResponse response
		 */
		response.setHeader("Access-Control-Allow-Origin", "http://www.domain1.com");  // 若有端口需写全（协议+域名+端口）
		response.setHeader("Access-Control-Allow-Credentials", "true");

#### 2、通过&lt;script&gt;标签实现：jsonp
- 1）原生实现：


		<script>
		    var script = document.createElement('script');
		    script.type = 'text/javascript';
		
		    // 传参并指定回调执行函数为onBack
		    script.src = 'http://www.domain2.com:8080/login?user=admin&callback=onBack';
		    document.head.appendChild(script);
		
		    // 回调执行函数
		    function onBack(res) {
		        alert(JSON.stringify(res));
		    }
		 </script>
服务端返回如下（返回时即执行全局函数）：
		
		onBack({"status": true, "user": "admin"})

- 	2）jquery ajax：


			$.ajax({
			    url: 'http://www.domain2.com:8080/login',
			    type: 'get',
			    dataType: 'jsonp',  // 请求方式为jsonp
			    jsonpCallback: "onBack",    // 自定义回调函数名
			    data: {}
			});

#### 3、通过&lt;iframe&gt;标签实现：window.name + iframe
&nbsp;&nbsp;window.name属性的独特之处：name值在不同的页面（甚至不同域名）加载后依旧存在，并且可以支持非常长的 name 值（2MB）。

-  1）a.html：(http://www.domain1.com/a.html)


		var proxy = function(url, callback) {
		    var state = 0;
		    var iframe = document.createElement('iframe');
		
		    // 加载跨域页面
		    iframe.src = url;
		
		    // onload事件会触发2次，第1次加载跨域页，并留存数据于window.name
		    iframe.onload = function() {
		        if (state === 1) {
		            // 第2次onload(同域proxy页)成功后，读取同域window.name中数据
		            callback(iframe.contentWindow.name);
		            destoryFrame();
		
		        } else if (state === 0) {
		            // 第1次onload(跨域页)成功后，切换到同域代理页面
		            iframe.contentWindow.location = 'http://www.domain1.com/proxy.html';
		            state = 1;
		        }
		    };
		
		    document.body.appendChild(iframe);
		
		    // 获取数据以后销毁这个iframe，释放内存；这也保证了安全（不被其他域frame js访问）
		    function destoryFrame() {
		        iframe.contentWindow.document.write('');
		        iframe.contentWindow.close();
		        document.body.removeChild(iframe);
		    }
		};
		
		// 请求跨域b页面数据
		proxy('http://www.domain2.com/b.html', function(data){
		    alert(data);
		});

- 2）proxy.html：(http://www.domain1.com/proxy.html

	中间代理页，与a.html同域，内容为空即可。

- 3）b.html：(http://www.domain2.com/b.html)


		<script>
		    window.name = 'This is domain2 data!';
		</script>

- 4）通过iframe的src属性由外域转向本地域，跨域数据即由iframe的window.name从外域传递到本地域。这个就巧妙地绕过了浏览器的跨域访问限制，但同时它又是安全操作。



> 参考[前端常见跨域解决方案（全）](https://segmentfault.com/a/1190000011145364 "前端常见跨域解决方案（全）")