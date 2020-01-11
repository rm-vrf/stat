>   <a href="/koala/view/view1/demo_ajax.zip">Download</a>

### iflytek.ajax.js 
```javascript
(function($){
	   /**
		 *  显示错误，根据项目进行修改,如弹出提示框
		 */
		function showError(message){
			console.error("showError:" + message);
		}
		/**
		 *  显示警告，根据项目进行修改,如弹出提示框
		 */
		function showWarning(message){
			console.warn("showWarning:" + message);
		}
		/**
		 *  显示记录消息，根据项目进行修改,如弹出提示框
		 */
		function showInfo(message){
			console.log("showInfo:" + message);
		}
	   /**###########定义函数########################**/
		window.iflytek = window.iflytek || {};
	  
		window.iflytek.ajax = function(params){
				/**
			   * 后台返回的参数模型，此处表示必须包含success/code/data/message字段，其值可以修改
			   */
			var responseModel = {
		    	success: "success", 
		    	code: "code", 
		    	data: "data" , 
		    	message: "message"};
		    /**
			 *  参数校验
			 */
		   function validateParamter(data){
				var noContainFields = [];
				for(property in responseModel){
					if(!data.hasOwnProperty(responseModel[property])){
						noContainFields.push(responseModel[property]);
					}
				}
				var result = "";
				if(noContainFields.length > 0){
					result =  "接口错误:返回结果中不包含属性" + noContainFields.join(",");
				}
				return result;
			};
			return (function(params){
					// 初始化ajax参数
					var baseParams = {
						type: "post",
						beforeSend:function(xmlrequest){
							 // this为调用本次AJAX请求时传递的options参数
						},
//						complete: function (xmlrequest) {
//						    // this为调用本次AJAX请求时传递的options参数
//						},
						error: function(){
							showError("调用服务出错！");
						}	
					};
					$.extend(baseParams, params);
					// 重写成功回调方法
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
									}
								}
							}else{
								showError(validateParamterStr);
							}
						};
					// 发送请求
					$.ajax(baseParams);
				})(params);
		}
		
})(jQuery);



```
