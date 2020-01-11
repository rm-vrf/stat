window.iflytek = window.iflytek || {};
iflytek.model = iflytek.model || function(){};
iflytek.model.prototype.getBaseComponentData = function (type){
		return  {
			id: uuid(),
			name: "component" + type,
			type: type,
			description: "",
			layerId: "",
			options: {},
			serviceType: "array",
			service: this.getDefaultService(type),
			visible: true,
			style:{
				top: 0,
				left: 0,
				width: 300, 
				height: 300,
				background: null,
				border: null
			}	
		};
	}
iflytek.model.prototype.getDefaultService = function(type){
	return {
		array: [[]],
		local: {
			id: uuid(),
			url: "",
			type:"get",
			url:"",
			dataType: "json",
			async: false
		},
		remote: {
			id: uuid(),
			url: "",
			method: "post",
			contentType: "application/json",
			dataType: "json"
		}
	}
}
iflytek.model.prototype.getComponentDataByType = function (componentType){
		var type = componentType.type;
		var data = this.getBaseComponentData(type);
		if(type < 2000){// 图表
			data.options = this.getOptionsByType(type);
		}else if(data.type < 3000){// 表格	
			data.options = this.getOptionsByType(type);
			data.style.width = data.options.width = 600;
			data.options.height = data.style.height= 400;
		}else if(data.type < 4000){// 图片	
			$.extend(data.options, componentType.options);
			data.style.width = 100;
			data.style.height = 100;
		}else if(data.type < 5000){// 文字
			$.extend(data.options, componentType.options);
			data.style.width = 100;
			data.style.height = 30;
		}
		return data;
	}
iflytek.model.prototype.getDefaultLayerData = function (){
		return {
			id: "defaultLayer",
			name: "defaultLayer",
			visible: true,
			key: true,
			style: {
				width: "100%",
				height: "100%"
			},
			components: []
		}
}
iflytek.model.prototype.getBaseLayerData = function (){
		return {
			id: uuid(),
			name: "layer",
			visible: true,
			components: []
		}
}
