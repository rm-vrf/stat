window.iflytek = window.iflytek || {};
iflytek.componentTypePanel = iflytek.componentTypePanel || function(options){
	this.message =  new iflytek.message("drawPanel");
	this.container = options.container;
	this.data =options.data;
	this.typeMap = {};
	var $this = this;
	var url = "js/components/view/component.type.panel.html";
	iflytek.utils.load(url, function(response){
		$this.initHtml(response);
		$this.initEventHandler();
	});
}
iflytek.componentTypePanel.prototype.initHtml = function(response){
	this.container.html(response);
	// 初始化html
	var htmlContent = '<div class="list-group">';	
	for(var i = 0; i<this.data.length; i++ ){
		var group = this.data[i];
		htmlContent += '<h4 class="group-title">'+group.groupName+'</h4>';
		htmlContent += '<div class="list-group-item">';
		for(var j = 0; j< group.children.length; j++){
			var item = group.children[j];
			this.typeMap[item.id] = item;
			htmlContent  += "<a class=\"img-container\" data-id=\""+item.id+"\"><img  type="+item.type+"  src=\""+item.icon+"\" class=\"type-icon\ alt=\""+item.name+"\" /></a>";
		}
		htmlContent += '</div>';
	}
	htmlContent += "</div>"
	this.container.find(".list-content").html(htmlContent);
}
iflytek.componentTypePanel.prototype.initEventHandler = function(){
	var $this = this;
	this.container.find(".img-container").mousedown(function(event){
		event.preventDefault();
		var typeId = $(event.currentTarget).attr("data-id");
		var componentType = $this.typeMap[typeId];
		$this.dispatchMessage("selectedComponentType", componentType);	
	});	
}
iflytek.componentTypePanel.prototype.initMessage = function(){
	
}
iflytek.componentTypePanel.prototype.dispatchMessage = function(name, data){
		this.message.dispatch(name, data);
}

