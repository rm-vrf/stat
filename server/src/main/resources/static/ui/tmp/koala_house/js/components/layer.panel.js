window.iflytek = window.iflytek || {};
iflytek.layerMessages = {
	addLayer: "addLayer",
	showLayer: "showLayer",
	changeKeyLayer: "changeKeyLayer",
	deleteLayer: "deleteLayer",
	upComponent: "upComponent",
	downComponent: "downComponent",
	selectComponent: "selectComponent",
	deleteComponent: "deleteComponent",
	showProperty: "showProperty",
	hideProperty: "hideProperty"
}
iflytek.layerPanel = iflytek.layerPanel || function(options){
	this.message =  new iflytek.message("drawPanel");
	this.container = options.container;
	this.layers =options.data;
	var $this = this;
	var url = "js/components/view/layer.panel.html";
	iflytek.utils.load(url, function(response){
		$this.container.html(response);
		$this.init(response);
	});
}
iflytek.layerPanel.prototype.init = function(){
	this.initHtml(this.layers);
	this.initEventHandler();
	this.initMessage();
}
iflytek.layerPanel.prototype.initHtml = function(layers){
	var htmlContent = "";
	for(var i = 0; i< layers.length; i++){
		htmlContent  += this.initLayerHtml(layers[i]);
	}
	$("#layers_panel").html(htmlContent);
	
	// 初始化图层状态
}
iflytek.layerPanel.prototype.initLayerHtml = function(layer){
	var visibleClass = layer.visible? "active": "";
	var keyClass = layer.key? "active": "";
	var htmlContent = '<div class="layer-panel" data-id="'+layer.id+'"><div class="layer-header"  role="button" data-toggle="collapse" href="#'+layer.id+'" aria-expanded="true" aria-controls="'+layer.id+'">'
			+ '<a class="layer-tool layer-tool-visible '+visibleClass+'"><span class="glyphicon glyphicon-eye-open" aria-hidden="true"></span></a>'
			+ '<a class="layer-tool  layer-tool-key '+keyClass+'"><span class="glyphicon glyphicon-check" aria-hidden="true"></span></a>'
			+ '<span class="layer-title">'+layer.name+'</span>'
			+ '<a class="layer-tool  layer-tool-delete  pull-right"><span class="glyphicon  glyphicon-remove" aria-hidden="true"></span></a>'
		    + '</div>'
		    +'<div class="collapse in" id="'+layer.id+'">'
			+'<div class="layer-content" >'
			+'<ul>';
	var components = layer.components;
	for(var i = 0; i< components.length; i++){
		htmlContent += this.initComponentHtml(components[i]);
	}
	htmlContent +='</ul></div></div></div>';
	return htmlContent;
}
iflytek.layerPanel.prototype.initComponentHtml = function(component){
	var componentHtml = '<li class="layer-component" data-id="'+component.id+'">'
				 +'<span>'+component.name+'</span>'
				 +'<a class="layer-tool  layer-component-delete pull-right" title="删除"><span class="glyphicon  glyphicon-remove" aria-hidden="true"></span></a>'
				+'<a class="layer-tool  layer-component-down  pull-right" title="置底"><span class="glyphicon   glyphicon-arrow-down" aria-hidden="true"></span></a>'
				+'<a class="layer-tool  layer-component-up  pull-right" title="置顶"><span class="glyphicon   glyphicon-arrow-up" aria-hidden="true"></span></a>'
				 +'</li>';
	return componentHtml;
}
iflytek.layerPanel.prototype.initEventHandler = function(){
	var $this = this;
	// 添加
	this.container.find(".layer-panel-add").off().click(function(event){
		$this.dispatchMessage(iflytek.layerMessages.addLayer);
	});
	// 显示/隐藏
	this.container.find(".layer-tool-visible").off().click(function(event){
		event.stopPropagation();
		var target = $(event.currentTarget);
		var currLayer = $this.getLayerByTarget(target);
		currLayer.visible = !currLayer.visible;
		target.toggleClass("active");
		$this.dispatchMessage(iflytek.layerMessages.showLayer, currLayer);
	});
	// 主操作层级
	this.container.find(".layer-tool-key").off().click(function(event){
		event.stopPropagation();
		var target = $(event.currentTarget);
		var currLayer = $this.getLayerByTarget(target);
		if(!currLayer.key){
			currLayer.key = true;
			$this.container.find(".layer-tool-key").removeClass("active");
			target.addClass("active");
			$this.dispatchMessage(iflytek.layerMessages.changeKeyLayer, currLayer);
		}
	});
	// 删除层级
	this.container.find(".layer-tool-delete ").off().click(function(event){
		if($this.layers.length == 1){
			alert("至少保留一个图层！")
			return;
		}
		if(!confirm("是否确认删除？")){
			return;
		}
		var target = $(event.currentTarget);
		var currLayer = $this.getLayerByTarget(target);
		$(target.parents(".layer-panel")).remove();
		$this.dispatchMessage(iflytek.layerMessages.deleteLayer, currLayer);
	});
	this.container.find(".layer-component").off().mouseenter(function(event){
		$(this).children(".layer-tool").show();
	}).mouseleave(function(event){
		$(this).children(".layer-tool").hide();
	}).click(function(event){
		var componentId = $(event.currentTarget).attr("data-id");
		var currComponent = $this.getComponentById(componentId);
		$this.dispatchMessage(iflytek.layerMessages.selectComponent, currComponent);
		$this.dispatchMessage(iflytek.layerMessages.showProperty, currComponent);
	});
	// 组件移动到上一层
	this.container.find(".layer-component-up").off().click(function(event){
		var target = $(event.currentTarget);
		var currComponent = $this.getComponentByTarget(target);
		
//		var componentContainer = $(target.parents(".layer-component"));
//		$(componentContainer.prev()).before(componentContainer);
		$this.dispatchMessage(iflytek.layerMessages.upComponent, currComponent);
	});
	// 组件移动到下一层
	this.container.find(".layer-component-down").off().click(function(event){
		var target = $(event.currentTarget);
		var currComponent = $this.getComponentByTarget(target);
//		
//		var componentContainer = $(target.parents(".layer-component"));
//		$(componentContainer.next()).after(componentContainer);
		
		$this.dispatchMessage(iflytek.layerMessages.downComponent, currComponent);
	});
	// 删除组件
	this.container.find(".layer-component-delete").off().click(function(event){
		event.stopPropagation();
		if(!confirm("是否确认删除？")){
			return;
		}
		var target = $(event.currentTarget);
		var currComponent = $this.getComponentByTarget(target);
		
		$this.removeComponent(currComponent);
		$this.dispatchMessage(iflytek.layerMessages.deleteComponent, currComponent);
		$this.dispatchMessage(iflytek.layerMessages.hideProperty);
	});
}
iflytek.layerPanel.prototype.selectComponent = function(component){
	this.container.find(".layer-component").removeClass("active");
	this.container.find("[data-id='"+component.id+"']").addClass("active");
}
iflytek.layerPanel.prototype.removeComponent = function(component){
	this.container.find("[data-id='"+component.id+"']").remove();
}
iflytek.layerPanel.prototype.getLayerByTarget = function(target){
	var panel = $(target.parents(".layer-panel"));
	return  this.getLayerById(panel.attr("data-id"));
}
iflytek.layerPanel.prototype.getComponentByTarget = function(target){
	var layer = this.getLayerByTarget(target);
	var componentId = $(target.parents(".layer-component")).attr("data-id");
	return this.getComponentById(componentId);
}
iflytek.layerPanel.prototype.getComponentById = function(id){
	for(var i = 0; i<this.layers.length; i++){
		var layer = this.layers[i];
		var arr = layer.components.filter(function(item){
			return item.id == id;
		})
		if(arr.length > 0){
			return arr[0];
		}
	}
}
iflytek.layerPanel.prototype.getLayerById = function(id){
	var arr = this.layers.filter(function(item){
		return item.id == id;
	});
	if(arr.length > 0){
		return arr[0];
	}else{
		return null;
	}
}
iflytek.layerPanel.prototype.initMessage = function(){
	var $this = this;
	this.message.on("selectedComponent", this.selectComponent);
}
iflytek.layerPanel.prototype.dispatchMessage = function(name, data){
	this.message.dispatch(name, data);
}
