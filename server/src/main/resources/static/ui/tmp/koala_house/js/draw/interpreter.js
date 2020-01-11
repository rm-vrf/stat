window.iflytek = window.iflytek || {};
iflytek.page = iflytek.page || {};
iflytek.interpreter = iflytek.interpreter || function(container, layers){
	this.container =container;
	if(!layers){
		layers = [];
	}
	this.init(layers);
};
iflytek.interpreter.prototype.init = function(layers){
	if(layers != undefined){
		this.layers = layers;
	}
	this.container.find(".panel-component").remove();
	for(var i = 0; i< this.layers.length; i++){
		var layer = this.layers[i];
		for(var j = 0; j< layer.components.length; j++){
			this.parseComponent(layer.components[j]);
		}
		
	}	
}

iflytek.interpreter.prototype.parseComponent = function(data){
	// 初始化html
	this.container.append(this.initComponentHtml(data));
	// 绑定事件
	this.initEventHanler();
	// 填充数据
	this.initServiceData(data); 
}
iflytek.interpreter.prototype.fillData = function(component){
	if(component.type < 2000){// 图表
		var id = this.getContentId(component.id);
		var myCharts = iflytek.page[component.id];
		if(!myCharts){
			myCharts = echarts.init(document.getElementById(id));
		}
		myCharts.setOption(component.options);
		myCharts.resize();
	}else if(component.type < 3000){// 表格	
		var id = this.getContentId(component.id);
		var tableId = id +'_table';
		$("#"+id).html('<table id="'+tableId +'"  ></table>');
		$("#"+tableId).bootstrapTable(component.options);
		$("#"+tableId).bootstrapTable("resetView");
	}else if(component.type < 4000){// 图片
				
	}else if(component.type < 5000){// 文字
				
	}
}
iflytek.interpreter.prototype.initServiceData = function(component){
	var currService = component.service[component.serviceType];
	if(currService == null) return;
	var $this = this;
	var resultData = [];
	try{
		switch(component.serviceType){
			case "array":
				resultData = currService;
			break;
			case "local":
			case "remote":
				currService.success = function(result){
					resultData = result.data;
				};
				currService.error = function (request) {
					console.error("Get " + component.serviceType + " error: the component is " + component.name);
				};
				$.ajax(currService);
			break;
		}
		if(resultData.length > 0){
			if(component.type < 2000){// 图表
				for(var i = 0; i< component.options.series.length; i++){
					 component.options.series[i].data = resultData[i];
				}
			}else if(component.type < 3000){// 表格	
					
			}
		}
		// 填充数据
		this.fillData(component);
	}catch(error){
		console.error("Get Service Data error: "+ error.toString);
	}
}
iflytek.interpreter.prototype.initComponentHtml = function(data){
	var styleStr = this.initStyleStr(data.style);
	var htmlContent = "<div id=\""+data.id+"\" class=\"panel-component\" style=\""+styleStr+"\"  draggable=\"true\">";
	htmlContent += "<div class=\"panel-component-content-container\" >";
	if(data.type < 2000){// 图表
		htmlContent += "<div id=\""+this.getContentId(data.id)+"\" class=\"panel-component-content\"></div>";
	}else if(data.type < 3000){// 表格	
		htmlContent += "<div id=\""+this.getContentId(data.id)+"\" class=\"panel-component-content\"></div>";
	}else if(data.type < 4000){// 图片
		htmlContent +="<img id=\""+this.getContentId(data.id)+"\"   src=\""+data.options.url+"\"  class=\"panel-component-content\"  />";
	}else if(data.type < 5000){// 文字
		htmlContent += "<span id=\""+this.getContentId(data.id)+"\" class=\"panel-component-content\">" +data.options.text+"<span/>";
	}
	htmlContent += "</div>";
	htmlContent += "<div class=\"panel-component-mask\" data-id=\""+data.id+"\"><img class=\"tool\" type=\"delete\" src=\"img/component/delete.png\"  />";
	htmlContent +="<img class=\"tool\" type=\"edit\"  src=\"img/component/edit.png\"  /><img class=\" tool-drag\" type=\"drag\"  src=\"img/component/arrow_down_right.png\"  /></div>";
	htmlContent += "</div>";
	return htmlContent;
}
iflytek.interpreter.prototype.getContentId = function(id){
	return "content_" + id ;
}
iflytek.interpreter.prototype.refreshComponent = function(component){
	$("#" + component.id).attr("style", this.initStyleStr(component.style));
	this.initServiceData(component);
}
iflytek.interpreter.prototype.initEventHanler = function(){
	// do something
}
iflytek.interpreter.prototype.initStyleStr = function(style){
	var styleStr = "";
	// 排序，解决覆盖的问题
	var arr = [];
	for(var prop in style){
		arr.push(prop);
	}
	arr.sort();
	for(var i = 0;  i< arr.length; i++){
		var prop = arr[i];
		switch(prop){
			case "width":
			case "height":
			case "top":
			case "left":
				styleStr += prop + ":" + style[prop] + "px;";
			break;
			default:
				styleStr += prop + ":" + style[prop] + ";";
			break;
		}
	}
	return styleStr;
}