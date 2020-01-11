### 自定义消息类（发布/订阅模式）
- 适用于单页面
- 适合全局消息传递，父子组件之间的消息传递尽量用事件回调的形式
- 基本原理是在window上面新增一个消息对象（map对象），用于消息的增/删/发布。
#### index.html代码使用如下：

	<!DOCTYPE html>
	<html>
		<head>
			<meta charset="utf-8" />
			<title>message demo</title>
		</head>
		<script type="text/javascript" src="js/rely/jquery-3.2.1.min.js" ></script>
		<script type="text/javascript" src="js/view/iflytek.message.js" ></script>
		<body>
			
		</body>
		<script>
			function onMessage(){
				var msg = new iflytek.message("test001");
				msg.on("updateNode", function(data){
					console.log("msg.on = " + JSON.stringify(data));
				});
			}
			function dispatchMessage(){
				var msg1 = new iflytek.message("test001");
				msg1.dispatch("updateNode", {
					name: "lzg",
					sex: 1
				});
			}
			// 订阅消息
			onMessage();
			// 发布消息
			dispatchMessage();
		</script>
	</html>

#### iflytek.message.js代码使用如下：

	(function($){
		iflytek = window.iflytek || {};
		/**
		 * 消息类，用于解决消息传递的问题
		 * @param target 消息对象，此处为唯一标识字符串
		 */
		iflytek.message = iflytek.message || function(target){
			this.target = target;
			this.data = {};	
		};
		/**
		 * 订阅消息
		 * @param  name 消息名称
		 * @param  callbackFun 回调函数
		 */
		iflytek.message.prototype.messageMap = {};
		iflytek.message.prototype.on = function(name, callbackFun){
			if(!this.messageMap.hasOwnProperty(this.target)){
				this.messageMap[this.target] = { 
					eventMap: {}
				};
			}
			var eventMap = this.messageMap[this.target].eventMap;
			if(!eventMap.hasOwnProperty(name)){
				eventMap[name] = [callbackFun];
			}else{
				eventMap[name].push(callbackFun);
			}
		}
		/**
		 * 取消订阅
		 * @param  name 消息名称
		 */
		iflytek.message.prototype.off = function(name){
			var message = this.messageMap[this.target];
			if(message && message.eventMap.hasOwnProperty(name)){
				delete message.eventMap[name];
			}
		}
		/**
		 * 取消所有订阅
		 */
		iflytek.message.prototype.offAll = function(){
			var message = this.messageMap[this.target];
			if(message){
				delete this.messageMap[this.target];
			}
		}
		/**
		 * 发布消息
		 * @param  name 消息名称
		 * @param  data 消息数据
		 */
		iflytek.message.prototype.dispatch = function(name, data){
			var message = this.messageMap[this.target];
			if(message && message.eventMap.hasOwnProperty(name)){
				var funs = message.eventMap[name];
				if(funs instanceof Array){
					for(var i = 0; i< funs.length; i++){
						if(funs[i] instanceof Function){
							funs[i](data);
						}
					}
				}
			}
		}
	})(jQuery);

