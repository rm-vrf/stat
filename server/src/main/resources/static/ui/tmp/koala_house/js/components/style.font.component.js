(function($){
    window.iflytek = window.iflytek || {};
    iflytek.components = iflytek.components || {};
	iflytek.components.fontStyleComp = iflytek.components.fontStyleComp  || function(options){
		this.message =  new iflytek.message("drawPanel");
		this.options = options;
		this.container = options.container;
		var $this = this;
		var url = "js/components/view/style.font.component.html";
		iflytek.utils.load(url, function(response){
			 	$this.initHtml(response);
			 	$this.initEventHandler();
			 	if(options.data){
			 		$this.setData(options.data);
			 	}
		});
	}
	iflytek.components.fontStyleComp.prototype.initHtml = function(response){
		this.container.html(response);
		// 初始化字体家族列表
			var arr = [{label: "微软雅黑", value: "Microsoft Yahei"},
			{label: "宋体", value: "SimSun"},
			{label: "Arial", value: "Arial"},
			{label: "Tahoma", value: "Tahoma"},
			{label: "Verdana", value: "Verdana"},
			{label: "serif", value: "serif"},
			{label: "sans-serif", value: "sans-serif"},
			{label: "cursive", value: "cursive"}];
			initSelect(this.container.find("#font_family_list"), arr);
			// 初始化字体大小列表
			var arr1 = [];
			for(var i = 8; i <=20; i++){
				arr1.push({label: i, value: i + "px"});
			}
			initSelect(this.container.find("#font_size_list"), arr1);
			// 初始化字体颜色
			this.initColorHtml(this.container.find("#font_color_input"));
	}
	iflytek.components.fontStyleComp.prototype.initEventHandler = function(){
		var $this = this;
		// 改变字体样式: 加粗/斜体/下划线
		this.container.find(".font-weight,.font-style,.font-underline").off().click(function(){
				$(this).toggleClass("active");
			 	var prop = $(this).attr("style-attr");
				$this.toggleStyle($this.data.style, prop);
				if($this.options.changePropertyHandler instanceof Function){
					$this.options.changePropertyHandler($this.data);
				}
			});
			// 改变字体家族/ 字体大小
		this.container.find(".font-select").off().change(function(){
				var prop = $(this).attr("style-attr");
				$this.data.style[prop] = $(this).val();
				if($this.options.changePropertyHandler instanceof Function){
					$this.options.changePropertyHandler($this.data);
				}
			});
	}
	iflytek.components.fontStyleComp.prototype.toggleStyle = function(target, attribute){
			switch(attribute){
				case "font-weight":
				 	target[attribute] = !target[attribute]? "bold": "";
				break;
				case "text-decoration":
				 	target[attribute] = !target[attribute]? "underline": "";
				break;
				case "font-style":
					target[attribute] = !target[attribute]? "italic": "";
				break;
			}
		}
	iflytek.components.fontStyleComp.prototype.setData = function(data){
		this.data = data? data: {style:{}};
		if(!this.data.style){
			this.data.style = {};
		}
		var style = this.data.style;
		// 初始化字体家族
		if(style["font-family"]){
			this.container.find("#font_family_list").val(style["font-family"]);
		}
		// 初始化字体大小
		if(style["font-family"]){
			this.container.find("#font_size_list").val(parseInt(style["font-size"]));
		}
		// 初始化字体颜色
		if(style["color"]){
			var fontContainer = this.container.find("#font_color_input");
			fontContainer.val(style["color"])
			this.initColorHtml(fontContainer);
		}
		// 初始化字体加粗
		if(style["font-weight"] && style["font-weight"] == "bold"){
			this.container.find(".font-weight").addClass("active");
		}
		// 初始化字体下划线
		if(style["text-decoration"] && style["text-decoration"] == "italic"){
			this.container.find(".text-decoration").addClass("active");
		}
		// 初始化字体样式
		if(style["font-style"] && style["font-style"] == "underline"){
			this.container.find(".font-style").addClass("active");
		}
	}
	iflytek.components.fontStyleComp.prototype.initColorHtml = function(target){
		var $this = this;
		this.container.find(".simpleColorContainer").remove();
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
	iflytek.components.fontStyleComp.prototype.changeStyle = function(target, value){
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

	 $.fn.fontStyleComp = function (options) {
        options.container = $(this);
        return  new iflytek.components.fontStyleComp(options);
    };
})(jQuery);
