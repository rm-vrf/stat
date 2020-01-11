(function($){
    window.iflytek = window.iflytek || {};
    iflytek.components = iflytek.components || {};
	iflytek.components.bordersStyleComp = iflytek.components.bordersStyleComp  || function(options){
		this.message =  new iflytek.message("drawPanel");
		this.options = options;
		this.container = options.container;
		this.data = options.data;
		this.borderStyleCachec = {};
		var $this = this;
		var url = "js/components/view/style.borders.component.html";
		iflytek.utils.load(url, function(response){
			 	$this.initHtml(response);
			 	$this.initEventHandler();
		});
	}
	iflytek.components.bordersStyleComp.prototype.initHtml = function(response){
			var $this = this;
			this.container.html(response);
			// 初始化边框样式列表
			var arr = [{label: "无", value: "none"},
			{label: "点状", value: "dotted"},
			{label: "实线", value: "solid"},
			{label: "双线", value: "double"},
			{label: "虚线", value: "dashed"}];
			initSelect(this.container.find(".border-style-select"), arr);
			// 初始化边框大小列表
			var arr1 = [];
			for(var i = 0; i <=10; i++){
				arr1.push({label: i, value: i + "px"});
			}
			initSelect(this.container.find(".border-width-select"), arr1);
			// 初始化圆角大小列表
			var arr2 = [];
			for(var i = 0; i <=10; i++){
				arr2.push({label: i, value: i + "px"});
			}
			initSelect(this.container.find(".border-radius-select"), arr2);
			// 初始化边距
			var arr3 = [];
			for(var i = 0; i <=10; i++){
				arr3.push({label: i, value: i + "px"});
			}
			initSelect(this.container.find(".border-padding-select"), arr3);
			// 初始化边框颜色
			this.initColorHtml(this.container.find("#all_border_color_input"));
			this.initColorHtml(this.container.find("#top_border_color_input"));
			this.initColorHtml(this.container.find("#bottom_border_color_input"));
			this.initColorHtml(this.container.find("#left_border_color_input"));
			this.initColorHtml(this.container.find("#right_border_color_input"));
	}
	iflytek.components.bordersStyleComp.prototype.initEventHandler = function(){
		var $this = this;
		// 显示或隐藏边框设置
		this.container.find(".border-location").off().click(function(){
			$(this).toggleClass("active");
			// 显示或隐藏属性设置区域
			var targetId = $(this).attr("ref");
			$this.container.find("#" + targetId).toggle();
			// 处理数据
			var active = $(this).hasClass("active");
			var type = $(this).attr("data-type");
			if(active){
				$.extend($this.style, $this.borderStyleCachec[type]);
			}else{
				// 存入内存
				$this.borderStyleCachec[type] = $this.getBorderValueByType(type);		
				$this.resetBorderValueByType(type);
			}
			if($this.options.changePropertyHandler instanceof Function){
				$this.options.changePropertyHandler(this.data);
			}
		});
		// 改变边框样式/宽度/边角
		$(".border-select").off().change(function(){
			$this.changeStyle($(this));
		});
		
	}
	iflytek.components.bordersStyleComp.prototype.initColorHtml = function(target){
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
		iflytek.components.bordersStyleComp.prototype.getBorderValueByType = function(type){
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
	iflytek.components.bordersStyleComp.prototype.resetBorderValueByType = function(type){
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
	iflytek.components.bordersStyleComp.prototype.changeStyle = function(target, value){
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
	 $.fn.bordersStyleComp = function (options) {
        this.each(function () {
            options.container = $(this);
            new iflytek.components.bordersStyleComp(options);
        });
    };
})(jQuery);
