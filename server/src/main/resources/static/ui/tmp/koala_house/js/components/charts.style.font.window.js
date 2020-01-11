(function($){
    window.iflytek = window.iflytek || {};
    iflytek.titleWindow = iflytek.titleWindow || {};
	iflytek.titleWindow.fontStyle = iflytek.titleWindow.fontStyle  || function(options){
		this.options = options;
		this.data = options.data;
		var $this = this;
		var url = "js/components/view/charts.style.font.window.html";
		iflytek.utils.load(url, function(response){
			 	$this.initHtml(response);
			 	$this.initEventHandler();
		});
	}
	iflytek.titleWindow.fontStyle.prototype.initHtml = function(response){
		$("body").append(response);
		this.container = $("#style_font_window");
		var $this = this;
		// 填充字体属性组件
		this.fontStyleComp = this.container.find(".style-component-font").fontStyleComp({
			data: {style: {}},
			changePropertyHandler: function(data){
				$.extend($this.data, data);
			}
		});
		if(this.data){
			this.show(this.data, this.options.confirmHandler);
		}
	}
	iflytek.titleWindow.fontStyle.prototype.initEventHandler = function(){
		var $this = this;
		this.container.find(".title-window-icon-close").click(function(){
			$this.hide();
		});
		this.container.find("button").off().click(function(){
			var type = $(this).attr("name");
			switch(type){
				case "confirm":
					$this.hide();
					if($this.confirmHandler instanceof Function){
						$this.confirmHandler($this.data);
					}
				break;
				case "cancel":
					$this.hide();
				break;
			}
		});
	}
	iflytek.titleWindow.fontStyle.prototype.show = function(data, confirmHandler){
		this.data = copy(data);
		this.confirmHandler = confirmHandler;
		// 显示窗口
		$("#style_font_window").css({
				top: ($(document).height()/2 - 250)+"px",
				left: ($(document).width()/2 - 200)+"px"
		}).show();
		$("#style_font_window_mask").show();
		// 初始化
		this.fontStyleComp.setData(this.data);
	}
	iflytek.titleWindow.fontStyle.prototype.hide = function(){
		$("#style_font_window").remove();
		$("#style_font_window_mask").remove();
	}
})(jQuery);
