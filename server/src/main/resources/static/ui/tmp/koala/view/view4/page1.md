#### 1、简介
- 统一了一些常用的正则表达式
- 统一了验证不通过的提示语
- 提供了一个js文件， 解决对象多种验证问题
- 与validate.js相比，侧重于单个值的验证，而非validae.js侧重于表单整体验证
#### 2、表单需要注意的一些问题
![](/koala/view/view4/1.PNG)
#### 3、示例代码    

>   <a href="/koala/view/view4/demo_validate.zip">Download</a>

- validate.html


		 <!DOCTYPE html>
		<html>
			<head>
				<meta charset="UTF-8">
				<title></title>
				<script type="text/javascript" src="js/rely/jquery-3.2.1.min.js" ></script>
				<script type="text/javascript" src="js/util/iflytek.validate.js" ></script>
			</head>
			<body>
				<h4>返回结果是：</h4>
				<div id="result"></div>
			</body>
			<script>
				function validate(value, params){
					return new iflytek.validator().validate(value, params);
				}
				//测试
				var result = validate("1989-09-21  11:11:11", "dateTime");
				var result1 = validate(" ", {
					dateTime:true
				});
				var result2 = validate("真是的啊", {
					required: true,
					chinese: true,
					minLength: 5,
					rangeLength:[1,3]
				});
				console.log("result: " + JSON.stringify(result));   // result: {"result":true,"message":""}
				console.log("result1: " + JSON.stringify(result1)); // result1: {"result":true,"message":""
				console.log("result2: " + JSON.stringify(result2)); // result2: {"result":false,"message":"该值为空"}
			</script>
		</html>

- iflytek.validate.js   (<font color="red">validate中的参数params可以为正则表达式，正则字符串，type字符串validator.regExps中的属性)、普通字符串</font>)

		(function($){
			iflytek = window.iflytek || {};
			iflytek.validator = iflytek.validator || function(){};
			/**
			 * 正则表达式示例
			 */
			iflytek.validator.prototype.regExps = {
				qq:  /^[1-9]\d{4,14}$/ , // QQ
				email:  /^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\.[a-zA-Z0-9_-]+)+$/,  // 邮编
				cardId: /(^[1-9]\d{5}(18|19|([23]\d))\d{2}((0[1-9])|(10|11|12))(([0-2][1-9])|10|20|30|31)\d{3}[0-9Xx]$)|(^[1-9]\d{5}\d{2}((0[1-9])|(10|11|12))(([0-2][1-9])|10|20|30|31)\d{2}$)/, // 15位或18位身份证号
				url: /^http:\/\/[A-Za-z0-9]+\.[A-Za-z0-9]+[\/=\?%\-&_~`@[\]\':+!]*([^<>\"\"])*$/,
				telephone:  /^((\(\d{2,3}\))|(\d{3}\-))?(\(0\d{2,3}\)|0\d{2,3}-)?[1-9]\d{6,7}(\-\d{1,4})?$/, // 电话号码，格式为：XXXX-XXXXXXX，XXXX-XXXXXXXX，XXX-XXXXXXX，XXX-XXXXXXXX，XXXXXXX，XXXXXXXX。 
				cellphone:  /^0?(13[0-9]|15[012356789]|18[0236789]|14[57])[0-9]{8}$/,  // 手机
				zip: /^[1-9][0-9]{5}$/, // 邮编
				ip: /^(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])(\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])){3}$/, // IP地址
				date:  /^\d{4}-(0?[1-9]|1[0-2])-(0?[1-9]|[1-2]\d|3[0-1])$/, // 日期
				time: /^([0-1]\d|2[0-3]):[0-5]\d:[0-5]\d$/, // 时间
				dateTime: /^\d{4}-(0?[1-9]|1[0-2])-(0?[1-9]|[1-2]\d|3[0-1])\s{1,2}([0-1]\d|2[0-3]):[0-5]\d:[0-5]\d$/, // 日期和时间，中间1~2个空格
				year: /^\d{4}$/, // 年份
				month: /^(0?[1-9]|1[0-2])$/  , // 月份,正确格式为：“01”-“09”和“1”“12”
				day: /^((0?[1-9])|((1|2)[0-9])|30|31)$/,  //天数，正确格式为：01、09和1、31
				integer: /^[-+]?\d*$/ , // 整数
				double: /^[-\+]?\d+(\.\d+)?$/, // 小数
				chinese: /^[\u0391-\uFFE5]+$/, // 中文
			}
			/**
			 * 限制条件示例
			 */
			iflytek.validator.prototype.limists = {
				// 文本类
				length: 10,
				minLength: 5, 
				maxLength: 10, 
				rangeLength: [5, 10],
				// 数字类
				min: 5,
				max: 10,
				range: [5, 10]
			}
			/**
			 * 验证失败提示字符串
			 */
			iflytek.validator.prototype.message = {
				qq: "QQ号码不正确" , // QQ
				email:  "邮箱地址不正确",  // 邮编
				cardId: "身份证号码不正确", // 15位或18位身份证号
				url: "URL地址不正确",
				telephone:  "电话号码不正确", // 电话号码
				cellphone:  "手机号码不正确",  // 手机
				zip: "邮编地址不正确", // 邮编
				ip: "IP地址不正确", // IP地址
				date:  "日期格式不正确", // 日期
				time: "时间格式不正确", // 时间
				dateTime: "日期时间格式不正确", // 日期和时间
				year: "年份不正确", // 年份
				month: "月份不正确"  , // 月份
				day: "天数不正确",  //天数
				integer: "该值不为整数" , // 整数
				double: "该值不为小数" , // 小数
				chinese: "该值不为中文", // 中文
				// 非正则类
				isNull: "值为null",
				isEmpty: "该值为空",
				required: "文本不能为空", // 非空判断
				// 文本类
				length: "字符串长度必须是{0}",
				minLength: "字符串最小长度为{0}",
				maxLength: "字符串最大长度为{0}",
				rangeLength: "字符串长度必须介于 {0} 和 {1} 之间",
				// 数字类
				min: "值最小为{0}",
				max: "值最大为{0}",
				range: "值必须介于 {0} 和 {1} 之间",
			}
			/**
			 * 判断是否为Null
			 * @param {Object} value
			 */
			iflytek.validator.prototype.isNull = function(value){
				 if (value === undefined || value === null) {
				    return true;
				  } else {
				   return false;
				  }
			}
			/**
			 * 判断是否为空
			 * @param {Object} value 
			 */
			iflytek.validator.prototype.isEmpty = function(value){
				 if (value === undefined || value === null) {
				    return true;
				  } else {
				    if (value instanceof Array && value.length === 0) {
				      return true;
				    }else if (typeof value === "string" && value.trim() === "") {
				      return true;
				    }
				  }
				  return false;
			}
			/**
			 * 占位符转换函数
			 */
			iflytek.validator.prototype.format = function() {  
				  if(arguments.length == 0) return "";  
				  if(arguments.length == 1) return arguments[0];  
				  var s = arguments[0];
				  var arr = [];
				  for(var i = 1;i< arguments.length; i++){
				  	arr = arr.concat(arguments[i]);
				  }
				  for(var i=0; i<arr.length; i++)  
				    s=s.replace(new RegExp("\\{"+i+"\\}","g"), arr[i]);  
				  return s;  
			};  
			/** 
			 * 参数为对象，多个校验, 返回params中第一个不匹配的错误提示
			 *	参数为 var params = {
			 *			required: true, 
			 *				qq: true, 
			 *				email: true
			 *			}
			 */
			iflytek.validator.prototype.validateEmpty = function(value, params){
				if(params.required){
					if(this.isEmpty(value)){
						return {
							result: false,
							message: this.message.isEmpty
						};
					}else{
						return null;
					}
				}else{
					if(this.isNull(value)){
						return {
							result: false,
							message: this.message.isNull
						};
					}else{
						return {
							result: true,
							message: ""
						};
					}
				}
			}
			/**
			 * 根据限制参数进行验证
			 * @param {Object} value 待验证的字符串
			 * @param {Object} params  validator.limits相关参数设置
			 */
			iflytek.validator.prototype.validateByLimits = function(value, params){
				var validateEmptyResult = this.validateEmpty(value, params);
				if(validateEmptyResult != null){
					return validateEmptyResult;
				}
				if(params.hasOwnProperty("length")){
					if(value.length != params.length){
						return {
							result: false, 
							message: this.format(this.message.length, params.length)
						};
					}
				}
				if(params.hasOwnProperty("minLength")){
					if(value.length < params.minLength){
						return {
							result: false, 
							message: this.format(this.message.minLength, params.minLength)
						};
					}
				}
				if(params.hasOwnProperty("maxLength")){
					if(value.length > params.maxLength){
						return {
							result: false, 
							message: this.format(this.message.maxLength, params.maxLength)
						};
					}
				}
				if(params.hasOwnProperty("rangeLength")){
					if(value.length < params.rangeLength[0] || value.length > params.rangeLength[1]){
						return {
							result: false, 
							message:  this.format(this.message.rangeLength, params.rangeLength)
						};
					}
				}
				if(params.hasOwnProperty("min")){
					if(value < params.min){
						return {
							result: false, 
							message: this.format(this.message.min, this.params.min)
						};
					}
				}
				if(params.hasOwnProperty("max")){
					if(value > params.max){
						return {
							result: false, 
							message: this.format(this.message.max, this.params.max)
						};
					}
				}
				if(params.hasOwnProperty("range")){
					if(value < params.range[0] || value > params.range[1]){
						return {
							result: false, 
							message:  this.format(this.message.range, params.range)
						};
					}
				}
				return {
					result: true,
					message: ""
				};
			}
			/**
			 * 根据code码进行验证， 为了节省代码设置的函数，待开发
			 * @param {Object} value  待验证的字符串
			 * @param {Object} code  code值
			 */
			iflytek.validator.prototype.validateByCode = function(value, code){
				// 待开发
				return this.validate(value.toString(), params);
			}
			/**
			 * 根据json对象进行验证
			 * var params = {
					required: true,
					dateTime:true
				}
			 * @param {Object} value  待验证的字符串
			 * @param {Object} params 验证参数
			 */
			iflytek.validator.prototype.validateByParams = function(value, params){
				var validateEmptyResult = this.validateEmpty(value, params);
				if(validateEmptyResult != null){
					return validateEmptyResult;
				}
				for(var property in params){
						if(params[property] && this.regExps[property]){
							if(!this.regExps[property].test(value)){
								return {
									result: false,
									message: this.message[property]
								};
							}
						}
				}
				return {
					result: true,
					message: ""
				};
			}
			/**
			 * 验证字符串， 返回参数为
		 	 * result = {
			 *		result: true,  // 是否验证通过
			 *		message: ""  // 若不为空，则为验证不通过提示字符串
			 * 	}
			 * @param {Object} value  待验证的字符串
			 * @param {Object} params  
			 *  
			 */
			iflytek.validator.prototype.validate = function(value, params){
				// 非空判断
				var validateEmptyResult = this.validateEmpty(value, params);
				if(validateEmptyResult != null){
					return validateEmptyResult;
				}	
				// 初始化返回结果
				var validateResult = {
					result: true,
					message: ""
				};
				// 参数分类进行判断
				if(params instanceof RegExp){
					// 参数为正则表达式， 如/^[-+]?\d*$/
					reg = params;
				}else if(typeof  params == "string"){
					if(this.regExps.hasOwnProperty(params)){
						// 参数为正则表达式类型，如dateTime
						validateResult.result =  this.regExps[params].test(value);
						if(!validateResult.result){
							validateResult.message = this.message[params];
						}
					}else{
						// 参数为正则表达式字符串, 如"^([0-1]\\d|2[0-3]):[0-5]\\d:[0-5]\\d$"
						validateResult.result = new RegExp(params).test(value);
					}
				}else if(typeof  params == "number"){
					// 参数为code码， 为了节省代码
					var codeValidateResult =  this.validateByCode(value, params);
		             if(!codeValidateResult.result){
		            	validateResult = codeValidateResult;
		            }
				}else{
		            var paramsValidateResult =  this.validateByParams(value, params);
		            if(!paramsValidateResult.result){
		            	validateResult =  paramsValidateResult;
		            }
		            var limitsValidateResult =  this.validateByLimits(value, params);
		             if(!limitsValidateResult.result){
		            	validateResult = limitsValidateResult;
		            }
				}
				return validateResult;
			}
		})(jQuery);
