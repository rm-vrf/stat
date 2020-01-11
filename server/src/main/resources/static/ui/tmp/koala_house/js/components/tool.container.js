(function($){
    window.iflytek = window.iflytek || {};
    iflytek.container = iflytek.container || {};
	iflytek.container.tool = iflytek.container.tool  || function(options){
		this.message =  new iflytek.message("drawPanel");
		this.container  = options.container;
		var $this = this;
		var url = "js/components/view/tool.container.html";
		iflytek.utils.load(url, function(response){
			$this.container.html(response);
			$this.init(options.data);
		});	
	}
	iflytek.container.tool.prototype.init = function(data){
		this.initData(data);
		this.initEventHandler();
		this.initMessage();
	}
	iflytek.container.tool.prototype.initData = function(data){
		this.data = {
				width: 1024,
				height: 768,
				scaleX: 1,
				scaleY: 1
		};
		$.extend(this.data, data);
		for(var prop in this.data){
			this.container.find("input[name='"+prop+"']").val(this.data[prop]);
		}
	}
	
	iflytek.container.tool.prototype.initEventHandler = function(){
		var $this = this;
		this.container.find("input").change(function(event){
			var target = $(event.currentTarget);
			var type = target.attr("name");
			var value = target.val();
			switch(target.attr("data-type")){
				case "number":
					value = parseFloat(value);
				break;
			}
			$this.data[type] = value;
			$this.dispatchMessage("changePanelProperty", $this.data);
		});
		this.container.find("button").click(function(event){
			event.stopImmediatePropagation();
			var type = $(event.currentTarget).attr("name");
			switch(type){
				case "export":
				break;
				case "style":
					$this.container.find(".panel-style-container").toggle();
				break;
			}
			$this.dispatchMessage(type);
		});
	}
	iflytek.container.tool.prototype.initMessage = function(){
		var $this = this;
		this.message.on("changePanelProperty", function(options){
			$this.initData(options);
		});
	}
	iflytek.container.tool.prototype.dispatchMessage = function(name, data){
		this.message.dispatch(name, data);
	}
})(jQuery);
