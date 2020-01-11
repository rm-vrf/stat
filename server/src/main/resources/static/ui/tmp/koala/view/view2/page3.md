### 一、通过RAP工具
>   <a href="/koala/view/view2/前后端分离Demo-rap.zip">Download</a>

- 代码示例

			<!DOCTYPE html>
			<html>
				<head>
					<meta charset="UTF-8">
					<title></title>
					<script type="text/javascript" src="js/rely/jquery-3.2.1.min.js" ></script>
					<script type="text/javascript"  src="http://192.168.59.179:8888/mock/createPluginScript.action?projectId=27"></script>
					<script type="text/javascript" src="js/util/iflytek.ajax.js" ></script>
				</head>
				<body>
					<h4>返回结果是：</h4>
					<div id="result"></div>
				</body>
				<script>
					/**
					 * 自定义ajax封装demo， params与调用$.ajax保持一致
					 */
					iflytek.ajax({
						url:"/kws/shareKeywords/query",
						data:{
							pageSize: 1,
							pageNum: 10
						},
						success:function(data){
							var dataStr = JSON.stringify(data);
							console.log("返回的结果是：" + dataStr);
							document.getElementById("result").innerHTML = dataStr;  
						}
					})
			
				</script>
			</html>
			
### 二、通过调用本地文件
>   <a href="/koala/view/view2/前后端分离Demo-本地json.zip">Download</a>

- 代码示例

			<!DOCTYPE html>
			<head>
			    <meta charset="UTF-8">
			    <title>前后端分离demo</title>
			</head>
			<body>
				<div id="result"></div>
			</body>
			<script type="text/javascript" src="js/rely/jquery-3.2.1.min.js" ></script>
			<script type="text/javascript" >
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
				// 页面初始化
				  $(document).ready(function(){
						doAjax({
							url: "/kws/shareKeywords/query.json",
							type: "post",
							success: function(data){
								$("#result").html("返回结果为："+JSON.stringify(data))
							}
						});
				   });
			</script>
			</html>
			