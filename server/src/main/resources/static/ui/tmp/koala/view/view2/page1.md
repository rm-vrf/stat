### **前后端分离解决方案** 主要是在前后端交互的地方进行解耦，让前端人员能够独立开发。
##### 下面展示2种前解决方法：
### 一、通过[RAP工具](http://baijiahao.baidu.com/s?id=1564690651492210&wfr=spider&for=pc)
#### 1、简介

- 提供了一个协作平台，前后端在上面定义前后端交互的接口
- 可以为前端ajax接口提供数据
- 可以测试后台controller层接口
- 操作简单，公司RAP服务为[192.168.59.179](http://192.168.59.179:8888/org/index.do "192.168.59.179")，使用方法见[前后端联调福音——RAP](http://baijiahao.baidu.com/s?id=1564690651492210&wfr=spider&for=pc)
#### 2、使用步骤：
1. 阅读[前后端联调福音——RAP](http://baijiahao.baidu.com/s?id=1564690651492210&wfr=spider&for=pc)
2. 注册rap账号；在rap上定义好前后端接口规范； 添加mock数据
3. 在页面中引用rap的js文件
	  `<script type="text/javascript"  src="http://192.168.59.179:8888/mock/createPluginScript.action?projectId=27"></script>`
  	其中参数设置ip(192.168.59.179)和projectId见	
4. 对ajax方法的url参数进行修改，代码示例中对url的获取方法进行了处理，实现<font color="red">生产环境和开发环境的一键切换</font>。
   
### 二、通过本地json文件
#### 1、简介
- 接口需要前后端共同定义
- 数据文件需要根据路径地址自己制造
- 不用部署服务
#### 2、使用步骤：
- <1>定义前后端接口规范，参考如下

			接口名称： 查询用户列表
			请求类型： post
			请求Url: /users/query.json
			请求参数：
			      {
			      	id: string ; // 用户id
			      	name: string; // 用户名称
			      	children: [// 子孩子
			      		{
			      			id: string; // 子孩子id
			      			name: name; // 子孩子名称
			      		}
			      	]
			      }
			响应参数：
			    	{
			    		success:true, // 成功或失败
			    		data: [{  // 成功后获取的数据
			    			id: string; // 用户id
			    			name: string; // 用户名称
			    		}], 
			    		resultCode:1000,  // 状态码
			    		message:""// 后台给予的提示，如错误提示及友好提示
			    }
    		

- <2>根据接口规范创建json文件，如请求url为/users/query.json，则在制定目录下新建users/query.json文件
- <3>根据配置对ajax方法进行处理， 代码如下
 
		// 配置项
		var config = {
		    develop: true, // 是否是开发环境
		    develop_url: "http://127.0.0.1:8020/demo/mock/site",
		    product_url: "http://192.168.2.1:8080/ybk"
		};
		// ajax封装
		function doAjax(params){
		    if(!config.develop){ // 生产环境
		        params = config.product_url + params.url;
		        $.ajax(params);  // 发送请求到远程服务器
		    }else{
		        // 发送请求到本地文件
		        $.ajax({
		            url: config.develop_url + params.url,
		            type: "get",
		            success: function(json){
		            	params.success(json);
		            }
		        });
		    }
		}

