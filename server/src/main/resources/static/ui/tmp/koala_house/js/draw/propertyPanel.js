window.iflytek = window.iflytek || {};
iflytek.propertyPanel = iflytek.propertyPanel || function() {
	// 消息
	this.message = new iflytek.message("drawPanel");
	this.initMessage();
}
iflytek.propertyPanel.prototype.initProperty = function(component){
	var currPropertyPanel ;
	if(component.type < 1000) { // 画布图层
		currPropertyPanel = new iflytek.basePropertyPanel();
	} else if(component.type < 2000) { // 图表
		currPropertyPanel = new iflytek.lineChartsPropertyPanel();
	} else if(component.type < 3000) { // 表格
		currPropertyPanel = new iflytek.basePropertyPanel();
	} else if(component.type < 4000) { // 图片
		currPropertyPanel = new iflytek.basePropertyPanel();
	} else if(component.type < 5000) { // 文字
		currPropertyPanel = new iflytek.basePropertyPanel();
	}
	if(currPropertyPanel){
		currPropertyPanel.dispatchMessageType = this.dispatchMessageType;
		currPropertyPanel.init(component);
	}
}

iflytek.propertyPanel.prototype.initMessage = function() {
	var $this = this;
	this.message.on("showProperty", function(component) {
		$this.dispatchMessageType = "changeProperty";
		$this.initProperty(component);
	}).on("refreshProperty", function(component) {
		$this.dispatchMessageType = "changeProperty";
		$this.initProperty(component);
	}).on("showPanelProperty", function(drawPanel) {
		$this.dispatchMessageType = "changePanelProperty";
		$this.initProperty(drawPanel);
	});
}
