(function($){
    window.iflytek = window.iflytek || {};
    iflytek.titleWindow = iflytek.titleWindow || {};
	iflytek.titleWindow.export = iflytek.titleWindow.export  || function(){
		var $this = this;
		var url = "js/components/view/export.window.html";
		iflytek.utils.load(url, function(response){
			 	$this.initHtml(response);
			 	$this.initEventHandler();
		});
	}
	iflytek.titleWindow.export.prototype.initHtml = function(response){
		$("body").append(response);
	
	}
	iflytek.titleWindow.export.prototype.initEventHandler = function(){
		var $this = this;
		$("#export_window .title-window-icon-close").click(function(){
			$this.hide();
		});
	}
	iflytek.titleWindow.export.prototype.show = function(value){
		$("#export_window").css({
				top: ($(document).height()/2 - 250)+"px",
				left: ($(document).width()/2 - 350)+"px"
		}).show();
		$("#export_window_mask").show();
		$("#export_content").val(value);
	}
	iflytek.titleWindow.export.prototype.hide = function(){
		$("#export_window").hide();
		$("#export_window_mask").hide();
	}
})(jQuery);
