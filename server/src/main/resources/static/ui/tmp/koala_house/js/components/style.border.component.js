(function($){
    window.iflytek = window.iflytek || {};
    iflytek.components = iflytek.components || {};
	iflytek.components.borderStyleComp = iflytek.components.borderStyleComp  || function(options){
		this.message =  new iflytek.message("drawPanel");
		var defaultOptions = {
			showStyle: true,
			showWidth: true,
			showColor: true,
			showRadius: true,
			showPadding: true
		};
		$.extend(defaultOptions, options);
		this.options = defaultOptions;
		this.container = options.container;
		this.data = options.data;
		this.borderStyleCachec = {};
		var $this = this;
		var url = "js/components/view/style.border.component.html";
		iflytek.utils.load(url, function(response){
			 	$this.initHtml(response);
			 	$this.initEventHandler();
		});
	}
	iflytek.components.borderStyleComp.prototype.initHtml = function(response){
			var $this = this;
			this.container.html(response);
			// 初始化边框样式列表
			if(this.options.showStyle){
				var arr = [{label: "无", value: "none"},
				{label: "点状", value: "dotted"},
				{label: "实线", value: "solid"},
				{label: "双线", value: "double"},
				{label: "虚线", value: "dashed"}];
				initSelect(this.container.find(".border-style-select"), arr);
			}else{
				this.container.find(".border-style-select").hide();
			}
			
			// 初始化边框大小列表
			if(this.options.showWidth){
				var arr1 = [];
				for(var i = 0; i <=10; i++){
					arr1.push({label: i, value: i + "px"});
				}
				initSelect(this.container.find(".border-width-select"), arr1);
			}else{
				this.container.find(".border-width-select").hide();
			}
			// 初始化圆角大小列表
			if(this.options.showRadius){
				var arr2 = [];
				for(var i = 0; i <=10; i++){
					arr2.push({label: i, value: i + "px"});
				}
				initSelect(this.container.find(".border-radius-select"), arr2);
			}else{
				this.container.find(".border-radius-select").hide();
			}
			// 初始化边距
			if(this.options.showPadding){
				var arr3 = [];
				for(var i = 0; i <=10; i++){
					arr3.push({label: i, value: i + "px"});
				}
				initSelect(this.container.find(".border-padding-select"), arr3);
			}else{
				this.container.find(".border-padding-select-label").hide();
				this.container.find(".border-padding-select").hide();
			}
			// 初始化边框颜色
			this.initColorHtml(this.container.find("#border_color_input"));
	}
	iflytek.components.borderStyleComp.prototype.initEventHandler = function(){
		var $this = this;
		// 改变边框样式/宽度/边角
		$(".border-select").off().change(function(){
			$this.changeStyle($(this));
		});
		
	}
	iflytek.components.borderStyleComp.prototype.setData = function(data){
		
	}
	iflytek.components.borderStyleComp.prototype.initColorHtml = function(target){
		var $this = this;
		target.simpleColor({
			boxWidth: 16,
			boxHeight: 16,
			cellWidth: 16,
			cellHeight: 16,
			chooserCSS: { 'border': '1px solid #777', 'width': '272px' },
			displayCSS: { 'border': '1px solid #ddd' },
			displayColorCode: false,
			livePreview: true,
			onSelect: function(hex, element) {
			   	$this.changeStyle(target, "#"+hex);
			}
		 });
	}
		iflytek.components.borderStyleComp.prototype.getBorderValueByType = function(type){
			var prex = "border-";
			if(type != "all"){
				prex = "border-" + type + "-";
			}
			var resultObj = {};
			var borderStyle = this.data.style;
			resultObj[prex + "style"] = borderStyle[prex + "style"];
			resultObj[prex + "color"] = borderStyle[prex + "color"];
			resultObj[prex + "width"] = borderStyle[prex + "width"];
			resultObj[prex + "radius"] = borderStyle[prex + "radius"];
			return resultObj;
		}
	iflytek.components.borderStyleComp.prototype.resetBorderValueByType = function(type){
			var prex = "border-";
			if(type != "all"){
				prex = "border-" + type + "-";
			}
			var borderStyle = this.data.style;
			borderStyle[prex + "style"] ="none";
			borderStyle[prex + "color"] = "none";
			borderStyle[prex + "width"] = "none";
			borderStyle[prex + "radius"] = "none";
		}
	iflytek.components.borderStyleComp.prototype.changeStyle = function(target, value){
			var prop = target.attr("style-attr");
			if(value != undefined){
				this.data.style[prop] = value;
			}else{
				this.data.style[prop] = target.val();
			}
			if(this.options.changePropertyHandler instanceof Function){
				this.options.changePropertyHandler(this.data);
			}
	}
	 $.fn.borderStyleComp = function (options) {
       options.container = $(this);
       return new iflytek.components.borderStyleComp(options);
    };
})(jQuery);
