### 流程图如下所示
### 1、 ajax调用代码如下，方法参数与

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

### 2、iflytek.ajax方法处理步骤, 主要是对参数params进行处理
1. 设置默认参数
 
  
	    // 初始化ajax参数
    	var baseParams = {
    		type: "post",
    		beforeSend:function(xmlrequest){
    			// this为调用本次AJAX请求时传递的options参数
    		},
    		error: function(){
    			showError("调用服务出错！");
    		  }	
    	};		

2. 用户参数覆盖

    	$.extend(baseParams, params);

3. 重写params中success方法
![](/koala/view/view1/ajax.PNG)

            // 代码示例
			baseParams.success = function(json){
						var validateParamterStr = validateParamter(json);
							if(validateParamterStr == ""){
								var success = json[responseModel.success];
								var code = json[responseModel.code];
								var message =  json[responseModel.message];
								var data =  json[responseModel.data];
								if(success){
									if( code < 400){
										// 请求成功且返回数据成功，对数据进行处理
										if(params.success instanceof Function){
											params.success(data);
										}
									}else{
										// 请求成功但返回数据不成功，给予友好提示
										showWarning(message);
									}
								}else{
									if(code< 200){
										// session过期
										showWarning(message);
									}else if(code< 400){
										// 权限问题
										showWarning(message);
									}else if(code< 600){
										// 服务器异常
										showError(message);
									}else{
										showError("找不到状态码！")
									}
								}
							}else{
								showError(validateParamterStr);
							}
						};

4. 发送请求，调用$.ajax

		$.ajax(baseParams);