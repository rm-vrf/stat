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
		console.log("message.on = " + name);
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
		return this;
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
		console.log("message.dispatch = " + name);
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
