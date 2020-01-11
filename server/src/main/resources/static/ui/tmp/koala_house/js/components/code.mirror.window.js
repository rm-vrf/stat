(function($){
    window.iflytek = window.iflytek || {};
    iflytek.titleWindow = iflytek.titleWindow || {};
	iflytek.titleWindow.codeMirror = iflytek.titleWindow.codeMirror  || function(){
		var $this = this;
		this.callback = null;
		var url = "js/components/view/code.mirror.window.html";
		iflytek.utils.load(url, function(response){
			$this.initHtml(response);
			$this.initEventHandler();
		});
	}
	iflytek.titleWindow.codeMirror.prototype.initHtml = function(response){
		$("body").append(response);
	}
	iflytek.titleWindow.codeMirror.prototype.initEventHandler = function(){
		var $this = this;
		$("#code_mirror_window_container .title-window-icon-close").click(function(){
			$this.hide();
		});
		$("#code_mirror_button_save").click(function(){
			if($this.callback instanceof Function){
				$this.hide();
				$this.callback($this.editor.getValue());
			}
		});
	}
	iflytek.titleWindow.codeMirror.prototype.show = function(value, callback){
		this.callback = callback;
		$("#code_mirror_window_container").css({
				top: ($(document).height()/2 - 250)+"px",
				left: ($(document).width()/2 - 350)+"px"
		}).show();
		$("#code_mirror_window_mask").show();

		$("#code_mirror_content").val(value);
		this.editor = this.editor || CodeMirror.fromTextArea(document.getElementById("code_mirror_content"), {
			mode:"application/json",
//        	theme:"eclipse",
            indentUnit : 2,  // 缩进单位，默认2
            smartIndent : true,  // 是否智能缩进
            tabSize : 4,  // Tab缩进，默认4
            readOnly : false,  // 是否只读，默认false
            showCursorWhenSelecting : true,
            lineNumbers : true,  // 是否显示行号
        });
		
		this.editor.setValue(value);
		// 格式化
		var totalLines = this.editor.lineCount();
        this.editor.autoFormatRange({line:0, ch:0}, {line:totalLines});
	}
	iflytek.titleWindow.codeMirror.prototype.hide = function(){
		$("#code_mirror_window_container").hide();
		$("#code_mirror_window_mask").hide();
	}
})(jQuery);
