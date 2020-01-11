(function($) {
	window.iflytek = window.iflytek || {};
	iflytek.titleWindow = iflytek.titleWindow || {};
	iflytek.titleWindow.seriesStyle = iflytek.titleWindow.seriesStyle || function(options) {
		this.options = options;
		this.data = options.data;
		var $this = this;
		var url = this.getHtmlUrlBytype();
		iflytek.utils.load(url, function(response) {
			$this.initHtml(response);
			$this.initEventHandler();
		});
	}
	iflytek.titleWindow.seriesStyle.prototype.getHtmlUrlBytype = function() {
		return "js/components/view/charts.style.series.line.window.html";
	}
	iflytek.titleWindow.seriesStyle.prototype.initHtml = function(response) {
		$("body").append(response);
		var $this = this;
		this.container = $("#style_series_window");
		if(this.data) {
			this.show(this.data, this.options.confirmHandler);
		}
	}
	iflytek.titleWindow.seriesStyle.prototype.initEventHandler = function() {
		var $this = this;
		this.container.find(".title-window-icon-close").click(function() {
			$this.hide();
		});
		this.container.find("input[type='text'], select").off().change(function(){
			var names = $(this).attr("name").split(".");
			$this.parseProp($this.data, names, $(this).val());
		});
		this.container.find("input[type='checkbox'], select").off().change(function(){
			var names = $(this).attr("name").split(".");
			$this.parseProp($this.data, names, $(this).prop("checked"));
		});
		this.container.find("button").off().click(function() {
			var type = $(this).attr("name");
			switch(type) {
				case "confirm":
					$this.hide();
					if($this.confirmHandler instanceof Function) {
						$this.confirmHandler($this.data);
					}
					break;
				case "cancel":
					$this.hide();
					break;
			}
		});
	}
	iflytek.titleWindow.seriesStyle.prototype.getPropValue = function(element, data) {
		if(!element.attr("name")) {
			return;
		}
		var names = element.attr("name").split(".");
		var value = data;
		for(var i = 0; i < names.length; i++) {
			// 若当前值不存在，则初始化该值
			if(!value[names[i]]) {
				value[names[i]] = {};
			}
			value = value[names[i]];
			if(!value) {
				return undefined;
			}
		}
		return value;
	}
	iflytek.titleWindow.seriesStyle.prototype.parseProp = function(target, attrs, value) {
		var currAttr = null;
		if(attrs.length == 1) {
			target[attrs[0]] = value;
		} else {
			for(var i = 0; i < attrs.length; i++) {
				if(i == 0) {
					currAttr = target[attrs[0]];
				} else if(i == attrs.length - 1) {
					currAttr[attrs[i]] = value;
				} else {
					if(!currAttr[attrs[i]]) {
						currAttr[attrs[i]] = {};
					}
					currAttr = currAttr[attrs[i]];
				}
			}
		}
	}
	iflytek.titleWindow.seriesStyle.prototype.show = function(data, confirmHandler) {
		this.data = copy(data);
		this.confirmHandler = confirmHandler;
		// 显示窗口
		$("#style_series_window").css({
			top: ($(document).height() / 2 - 150) + "px",
			left: ($(document).width() / 2 - 200) + "px"
		}).show();
		$("#style_series_window_mask").show();
		$("#style_series_window").show();
		// 初始化select值
		this.container.find("input[type='text'], select").each(function(){
			var value = $this.getPropValue($(this), $this.data);
			$(this).val(value);
		});
		this.container.find("input[type='checkbox']").each(function(){
			var value = $this.getPropValue($(this), $this.data);
			$(this).prop("checked", value);
		});
	}
	iflytek.titleWindow.seriesStyle.prototype.hide = function() {
		$("#style_series_window").remove();
		$("#style_series_window_mask").remove();
	}
})(jQuery);