(function($){
    window.iflytek = window.iflytek || {};
    iflytek.components = iflytek.components || {};
	iflytek.components.backgroundStyleComp = iflytek.components.backgroundStyleComp  || function(options){
		this.options = options;
		this.container = options.container;
		var $this = this;
		var url = "js/components/view/style.background.component.html";
		iflytek.utils.load(url, function(response){
			 	$this.initHtml(response);
			 	$this.initEventHandler();	
		});
	}
	iflytek.components.backgroundStyleComp.prototype.initHtml = function(response){
		var $this = this;
		this.container.html(response);
		// 初始化
		this.setData(this.options.data);
	}
	iflytek.components.backgroundStyleComp.prototype.initEventHandler = function(){
		var $this = this;
	}
	iflytek.components.backgroundStyleComp.prototype.setData = function(data){
		this.data = data;
		// 初始化边框颜色
		var colorContainer = this.container.find(".background-color-input");
		if(this.data && this.data["background-color"]){
			 colorContainer.val(this.data["background-color"]); 
		}
		this.initColorHtml(colorContainer);
	}
	iflytek.components.backgroundStyleComp.prototype.initColorHtml = function(target){
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
	iflytek.components.backgroundStyleComp.prototype.changeStyle = function(target, value){
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
	 $.fn.backgroundStyleComp = function (options) {
        options.container = $(this);
          return  new iflytek.components.backgroundStyleComp(options);
    };
})(jQuery);
