window.iflytek = window.iflytek || {};
iflytek.drawPanel = iflytek.drawPanel || function(container){
	// 状态对象
	this.status = "initial";
	// 属性对象
	this.layers = [];
	this.currLayer = null;
	this.currComponent = null;
	this.currComponentType = null;
	// 操作对象
	this.selectionManager = new iflytek.manager.selection();
	this.cursorManager = new iflytek.manager.cursor();
	this.model = new iflytek.model();
	this.interpreter = null;
	this.layerPanel = null;
	this.offsetX = -25;
	this.offsetY = -15;
	this.panelOffsetX = 0;
	this.panelOffsetY = 0;
	this.componentOffsetX = 0;
	this.componentOffsetY = 0;
	this.options = {
		type: 0,
		width: 1024,
		height:768,
		scaleX: 1,
		scaleY: 1,
		top: 40,
		left: 0,
		style: {}
	};
	this.type = 0;
	// 页面对象
	this.$container = container;
	this.$mask = null;
	this.$drawPanel = null;
	this.layerCount = 1;
	this.componentCount = 1;
	// 消息
	this.message =  new iflytek.message("drawPanel");
};
iflytek.drawPanel.prototype.init = function(options){
	$.extend(this.options, options);
	this.initHtml();
	this.initData();
	this.initGrid();
	this.initEventHandler();
	this.initMessage();
}

iflytek.drawPanel.prototype.initHtml = function(){
	var htmlContent ="<div class=\"content\"><div id='draw_panel_content'></div><div class=\"mask\" style=\"display: none;\"></div></div>";
	this.$container.html(htmlContent);
	
	this.$mask = this.$container.find(".mask");
	this.$drawPanel = $("#draw_panel_content");
}
iflytek.drawPanel.prototype.initData = function(){
	var $this = this;
	// 添加默认层级
	this.currLayer  = this.model.getDefaultLayerData();
	this.layers.push(this.currLayer);
	// 添加层级html
	this.interpreter = new iflytek.interpreter(this.$drawPanel, this.layers);
	this.layerPanel= new iflytek.layerPanel({
		container: $("#layer_components_panel"),
		data: this.layers
	});
}
iflytek.drawPanel.prototype.initEventHandler = function(){
	// 绑定事件
	var $this = this;
	$(document).mouseup(document_mouseup).keydown(document_keydown);
	this.$drawPanel.mouseenter(panel_mouseenter).mousedown(panel_mousedown).mousemove(panel_mousemove).mouseup(panel_mouseup).click(panel_mouseclick).bind('mousewheel', panel_mousewheel);
	this.$mask.mousemove(mask_mousemove).mouseup(mask_mouseup);
	// 事件声明
	function document_mouseup(event){
		switch($this.status){
			case $this.statusFactory().addComponent:
			case $this.statusFactory().dragComponent:
				$this.reset();
			break;
			case $this.statusFactory().dragPanel:
				$this.reset();
				$this.options.left =parseInt($this.$drawPanel.css("left"));
				$this.options.top  =parseInt($this.$drawPanel.css("top"));
			break;
			case $this.statusFactory().sizeComponent:
				$this.dispatchMessage("refreshProperty", $this.currComponent);
				$this.reset();
			break;
			
		}
	}
	function document_keydown(event){
		var offsetX, offsetY = 0; 
		switch(event.keyCode){
			case 37: // 左
				offsetX = -1;
				$this.moveComponent(offsetX, offsetY);
			break;
			case 38: // 上
				offsetY = -1;
				$this.moveComponent(offsetX, offsetY);
			break;
			case 39: // 右
				offsetX = 1;
				$this.moveComponent(offsetX, offsetY);
			break;
			case 40: // 下
				offsetY = 1;
				$this.moveComponent(offsetX, offsetY);
			break;
		}
	}
	function panel_mouseenter(event){
		switch($this.status){
			case $this.statusFactory().addComponent:			
				$this.$mask.show();
				$this.currComponent = $this.model.getComponentDataByType( $this.currComponentType);
				$this.currComponent.layerId = $this.currLayer.id;
				$this.selectionManager.getSelectionBound($this.currComponent.style.width, $this.currComponent.style.height);
			break;
			
		}	
	}
	function panel_mousedown(event){
		switch($this.status){
			case $this.statusFactory().initial:
				$this.panelOffsetX = event.pageX;
				$this.panelOffsetY = event.pageY;
				$this.setStatus($this.statusFactory().dragPanel);
			break;
		}
	}
	function  panel_mouseup(event){
	}
	function panel_mouseclick(event){
		$this.dispatchMessage("showPanelProperty", $this.options);
	}
	function panel_mousewheel(event){
		var scaleX, scaleY = 0;
		if(event.originalEvent.wheelDelta > 0){ // 向上
			scaleX = $this.options.scaleX + 0.2;
			scaleY = $this.options.scaleY + 0.2;
		}else{
			scaleX = $this.options.scaleX - 0.2;
			scaleY = $this.options.scaleY - 0.2;
		}
		$this.setScale(scaleX, scaleY, event.offsetX, event.offsetY);
		$this.dispatchMessage("changePanelProperty", $this.options);
	}
	function  panel_mousemove(event){
		switch($this.status){
			case $this.statusFactory().dragPanel:
				var left = parseInt($this.$drawPanel.css("left")) + event.pageX - $this.panelOffsetX;
				var top =parseInt($this.$drawPanel.css("top"))+ event.pageY - $this.panelOffsetY;
				$this.$drawPanel.css({
					left: left + "px",
					top:  top + "px"
				});
				$this.$mask.css({
					left: left + "px",
					top:  top + "px"
				});
				$this.panelOffsetX = event.pageX;
				$this.panelOffsetY = event.pageY;
			break;
			case $this.statusFactory().sizeComponent:
				$this.sizeComponentByOffset(event.pageX - $this.componentOffsetX, event.pageY - $this.componentOffsetY);
				
				$this.componentOffsetX = event.pageX;
				$this.componentOffsetY = event.pageY;
			break;
		}
	}
	
	function mask_mousemove(event){
		var offsetX = event.offsetX + $this.offsetX;
		var offsetY = event.offsetY + $this.offsetY;
		switch($this.status){
			case $this.statusFactory().addComponent:
			case $this.statusFactory().dragComponent:
				$this.selectionManager.moveSelectionBound(offsetX, offsetY);
			break;
		}	
	}
	function mask_mouseup(event){
		var offsetX = event.offsetX + $this.offsetX;
		var offsetY = event.offsetY + $this.offsetY;
		event.stopImmediatePropagation();
		switch($this.status){ 
			case $this.statusFactory().addComponent:
				$this.locationComponent(offsetX, offsetY);
				$this.addComponentByType();
			break;
			case $this.statusFactory().dragComponent:
				$this.locationComponent(offsetX, offsetY);
				$this.dispatchMessage("refreshProperty", $this.currComponent);
			break;
		}
		$this.highlightComponent($this.currComponent);
		$this.reset();
	}
}


iflytek.drawPanel.prototype.addComponentByType = function(){
	if(this.currComponent != null){
		this.currComponent.name = "component" + this.componentCount;
		this.currComponent.layerId = this.currLayer.id;
		this.currLayer.components.push(this.currComponent);
		this.interpreter.init(this.layers);
		this.initPanelEventHandler();
		this.layerPanel.init();
		
		 this.componentCount++;
	}else{
		console.error("currComponent is null");
	}	
}
iflytek.drawPanel.prototype.initPanelEventHandler = function(){
	var $this = this;
	this.$drawPanel.find(".panel-component").off().mouseenter(function(event){
		$(this).find(".panel-component-mask").show();
	}).mouseleave(function(event){
		event.preventDefault();
		event.stopPropagation();
		$(this).find(".panel-component-mask").hide();
	});
	this.$drawPanel.find(".panel-component-mask").off().mousedown(function(event){
		event.preventDefault();
		event.stopPropagation();
		var id = $(event.currentTarget).attr("data-id");
		$this.currComponent = $this.getComponentById(id);
		// 画布事件
		$this.offsetX = -event.offsetX;
		$this.offsetY = -event.offsetY;
		$this.$mask.show();
		$this.setStatus($this.statusFactory().dragComponent);
		$this.selectionManager.getSelectionBound($this.currComponent.style.width, $this.currComponent.style.height);
	});

	this.$drawPanel.find(".panel-component-mask .tool-drag").off().mousedown(function(event){
		event.preventDefault();
		event.stopPropagation();
		$this.componentOffsetX = event.pageX;
		$this.componentOffsetY = event.pageY;
		var id = $(event.currentTarget).parent().attr("data-id");
		var component = $this.getComponentById(id);
		$this.highlightComponent(component);
		$this.setStatus($this.statusFactory().sizeComponent);
	}).click(function(event){
		event.preventDefault();
		event.stopPropagation();
	});
	this.$drawPanel.find(".panel-component-mask .tool").off().mousedown(function(event){
		event.preventDefault();
		event.stopPropagation();
	}).mouseup(function(event){
		event.preventDefault();
		event.stopPropagation();
	}).click(function(event){
		event.preventDefault();
		event.stopPropagation();
		var id = $(event.currentTarget).parent().attr("data-id");
		var type = $(event.currentTarget).attr("type");
		var component = $this.getComponentById(id);
		switch(type){
			case "delete":
				// 删除对象数据
				$this.removeComponent(component);
				$this.layerPanel.removeComponent(component);
				$this.dispatchMessage("hideProperty");
			break;
			case "edit":
				$this.dispatchMessage("showProperty", component);
			break;
			case "drag":
				$this.panelOffsetX = event.pageX;
				$this.panelOffsetY = event.pageY;
				$this.setStatus($this.statusFactory().sizeComponent);
			break;
		}
		
	});
}
iflytek.drawPanel.prototype.getComponentById = function(id){
	for(var i = 0; i< this.layers.length; i++){
		var layer = this.layers[i];
		for(var j = 0; j< layer.components.length; j++){
			if(layer.components[j].id == id){
				return layer.components[j];
			}
		}
	}
}

iflytek.drawPanel.prototype.moveComponent = function(offsetX, offsetY){
	if(this.currComponent != null){
		var style = this.currComponent.style;
		this.locationComponent(style.left + offsetX, style.top + offsetY);
	}
}
iflytek.drawPanel.prototype.locationComponent = function(x, y){
	if(this.currComponent != null){
		this.currComponent.style.top = y;
		this.currComponent.style.left = x;
		this.interpreter.refreshComponent(this.currComponent);
	}else{
		console.error("locationComponent: currComponent is null");
	}	
}
iflytek.drawPanel.prototype.sizeComponentByOffset = function(offsetWidth, offsetHeight){
	console.log("offsetWidth = " + offsetWidth + "; offsetHeight = " + offsetHeight);
	if(this.currComponent != null){
		this.currComponent.style.width += offsetWidth;
		this.currComponent.style.height += offsetHeight;
		this.interpreter.refreshComponent(this.currComponent);
	}
}
iflytek.drawPanel.prototype.highlightComponent = function(component){
	if(component){
		this.currComponent = component;
	}
	if(this.currComponent != null){
		// 高亮显示
		$(".panel-component.active").removeClass("active");
		$("#" + component.id).addClass("active");
		this.layerPanel.selectComponent(this.currComponent);
	}else{
		console.error("selectedComponent: currComponent is null");
	}	
}
iflytek.drawPanel.prototype.cancelHighlight = function(){
	$(".panel-component .active").removeClass("active");
}
iflytek.drawPanel.prototype.setComponentType = function(componentType){
	this.currComponentType = componentType;
}
iflytek.drawPanel.prototype.removeLayer = function(layer){
	// 删除组件
	for(var i = 0; i<layer.components.length; i++){
		$("#" + layer.components[i].id).remove();
	}
	// 删除层级
	this.layers = this.layers.filter(function(item){
		return item.id != layer;
	});
	this.currLayer = this.layers[0];
	this.reset();
}
iflytek.drawPanel.prototype.cleanLayerKey = function(){
	for(var i = 0; i<this.layers.length; i++){
		this.layers[i].key = false;
	}
}
iflytek.drawPanel.prototype.addLayer = function(){
	this.cleanLayerKey();
	var layer = this.model.getBaseLayerData();
	layer.name = "layer" + this.layerCount;
	layer.key = true;
	this.layers.push(layer);
	
	this.currLayer = layer;
	this.layerCount ++;
	
	this.layerPanel.init();
}
iflytek.drawPanel.prototype.showLayer = function(layer){
	for(var i = 0; i< layer.components.length; i++){
		var $component = $("#" + layer.components[i].id);
		if(layer.visible){
			$("#" + layer.components[i].id).show();
		}else{
			$("#" + layer.components[i].id).hide();
		}
	}
}
iflytek.drawPanel.prototype.changeKeyLayer = function(layer){
	this.cleanLayerKey();
	layer.key = true;
	this.currLayer = layer;
}

iflytek.drawPanel.prototype.removeComponent = function(component){
	var layer = this.getLayerById(component.layerId);
	$("#"+component.id).remove();
	layer.components = layer.components.filter(function(item){
		return item.id != component.id;
	});
	this.reset();
}
iflytek.drawPanel.prototype.upComponent = function(component){
	this.$drawPanel.append($("#" + component.id));
}
iflytek.drawPanel.prototype.downComponent = function(component){
	this.$drawPanel.prepend($("#" + component.id));
}
iflytek.drawPanel.prototype.selectedComponent = function(component){
	this.currComponent = component;
	this.highlightComponent(component);
}
iflytek.drawPanel.prototype.getLayerById = function(id){
	var arr = this.layers.filter(function(item){
		return item.id == id;
	});
	if(arr.length > 0){
		return arr[0];
	}else{
		return null;
	}
}
iflytek.drawPanel.prototype.changePanelProperty = function(data){
	$.extend(this.options, data);
	$("#draw_panel_content").css({
		width: data.width + "px",
		height: data.height + "px"
	});
	this.$drawPanel.css({
		top: this.options.top + "px",
		left: this.options.left + "px"
	});
	this.$mask.css({
		top: this.options.top + "px",
		left: this.options.left + "px"
	});
		// 缩放
	var transform = "scale("+this.options.scaleX+","+this.options.scaleY+")";
	this.$drawPanel.css("transform",transform);
	this.$mask.css("transform",transform);
	// 背景、字体、边框样式
	var styleStr = "";
	// 排序，解决覆盖的问题
	var arr = [];
	for(var prop in data.style){
		arr.push(prop);
	}
	arr.sort();
	var style = {};
	for(var i = 0;  i< arr.length; i++){
		style[arr[i]] = data.style[arr[i]];
	}
	data.style = style;
	$("#draw_panel_content").css(style);
}
iflytek.drawPanel.prototype.setScale = function(scaleX, scaleY, offsetX, offsetY){
	if(scaleX > 1 || scaleX < -0.4 || scaleY > 1 || scaleY < 0.4){
		return;
	}
	if(!offsetX)  offsetX = 0;
	if(!offsetY)  offsetY = 0;
	var offsetScaleX  = scaleX - this.options.scaleX;
	var offsetScaleY  = scaleY - this.options.scaleY;
	this.options.scaleX	 = scaleX;
	this.options.scaleY = scaleY;
		
		// 鼠标带来的偏移量
	var offsetLeft = this.options.width * offsetScaleX * 0.5 -  offsetX * offsetScaleX;
	var offsetTop = this.options.height * offsetScaleY * 0.5 - offsetY * offsetScaleY;
	this.options.top += offsetTop;
	this.options.left += offsetLeft;
	
}
iflytek.drawPanel.prototype.initGrid = function(){
	var count_w = Math.ceil(this.options.width / 100);
	var count_h = Math.ceil(this.options.height / 100);
	var htmlContent = '<div class="grid-cell-container">';
	for(var i = 0; i< count_w * count_h; i++){
		htmlContent +='<div class="grid-cell"></div>';
	}
	htmlContent += '</div>';
	this.$drawPanel.append(htmlContent);
}
iflytek.drawPanel.prototype.showGrid = function(){
	this.$drawPanel.find(".grid-cell").show();
}
iflytek.drawPanel.prototype.showGrid = function(){
	this.$drawPanel.find(".grid-cell").hide();
}
iflytek.drawPanel.prototype.reset = function(){
	this.setStatus(this.statusFactory().initial);
//	this.currComponent = null;
	this.selectionManager.hideSelectionBound();
	this.$mask.hide();
}
iflytek.drawPanel.prototype.setStatus = function(status){
	this.status = status;
	switch(this.status){ 
		case this.statusFactory().addComponent:
			this.cursorManager.changeCursor(this.currComponentType.type);
		break;
		case this.statusFactory().initial:
		case this.statusFactory().dragComponent:
			this.cursorManager.removeCursor();
		break;
		case this.statusFactory().sizeComponent:
			this.cursorManager.setCursor("se-resize");
		break;
	}
}

iflytek.drawPanel.prototype.statusFactory = function(){
	return {
		initial: "initial",
		addComponent: "addComponent",
		dragComponent: "dragComponent",
		sizeComponent: "sizeComponent",
		dragPanel: "dragPanel"
	};
}
iflytek.drawPanel.prototype.initMessage = function(){
	var $this = this;
	this.message.on("addLayer", function(){
		$this.addLayer();
	}).on("showLayer", function(layer){
		$this.showLayer(layer);
	}).on("changeKeyLayer",  function(layer){
		$this.changeKeyLayer(layer);
	}).on("deleteLayer", function(layer){
		$this.removeLayer(layer);
	}).on("upComponent", function(component){
		$this.upComponent(component);
	}).on("downComponent", function(component){
		$this.downComponent(component);
	}).on("selectComponent", function(component){
		$this.selectedComponent(component);
	}).on("deleteComponent", function(component){
		$this.removeComponent(component);
	}).on("selectedComponentType", function(componentType){
		$this.setComponentType(componentType);	
		$this.setStatus("addComponent");
	}).on("changePanelProperty", function(data){
		$this.changePanelProperty(data);
	}).on("changeProperty", function(data){
		$this.interpreter.refreshComponent($this.currComponent);
	});
}
iflytek.drawPanel.prototype.dispatchMessage = function(name, data){
	this.message.dispatch(name, data);
}
