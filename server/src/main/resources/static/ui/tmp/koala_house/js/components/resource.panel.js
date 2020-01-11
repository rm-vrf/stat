window.iflytek = window.iflytek || {};
iflytek.resourcePanel = iflytek.resourcePanel || function(options){
	this.message =  new iflytek.message("drawPanel");
	this.container = options.container;
	this.data =options.data;
	this.components = [];
	this.selectedComponentType = options.selectedComponentTypeHandler;
	var $this = this;
	var url = "js/components/view/resource.panel.html";
	iflytek.utils.load(url, function(response){
		$this.initHtml(response);
		$this.initEventHandler();
	});
}
iflytek.resourcePanel.prototype.initHtml = function(response){
	this.container.html(response);
}
iflytek.resourcePanel.prototype.initEventHandler = function(){
	var $this = this;
	$("#file_tool_input").change(function(event){
		$this.chageFileHandler(event.currentTarget);
	});
	$("#file_tool_select").click(function(){
		$("#file_tool_input").trigger("click");
	});
	this.container.find(".img-container").mousedown(function(event){
		event.preventDefault();
		if($this.selectedComponentType instanceof Function){
			var fileId = $(event.currentTarget).attr("file-id");
			var file = $this.fileMap[fileId];
			$this.selectedComponentType(componentType);	
		}
	});	
}
iflytek.resourcePanel.prototype.chageFileHandler = function(fileInput){
	var files = fileInput.files;
	var currComponents = [];
	for(var i = 0; i< files.length; i++){
		var file = files[i];
		file.url = iflytek.utils.getFileUrl(file);
		var component = this.changeFileToComponentType(file);
		currComponents.push(component);
		this.components.push(component);
	}
	// 添加到文件展示面板
	var fileContainer = $(this.container.find(".file-container"));
	this.refreshFileContainerHtml(fileContainer, currComponents);
	this.refreshFileContainerEventHandler(fileContainer);
}
iflytek.resourcePanel.prototype.refreshFileContainerHtml = function(container, components){
	var htmlContent = "";
	for(var i = 0; i<components.length; i++){
		var component = components[i];
		htmlContent += '<div class="file-box" data-id="'+component.id+'">';
		htmlContent += '<img class="file-tool-delete"  src="img/component/delete.png"  />';
		htmlContent += '<a class="img-container"><img src="'+component.options.url+'"   /></a>'
		htmlContent += '<span title="'+component.name+'">'+component.name+'</span>';
		htmlContent += '</div>';
	}
	container.append(htmlContent);
}
iflytek.resourcePanel.prototype.refreshFileContainerEventHandler =function (container){
	var $this = this;
	container.find(".file-box").off().mouseenter(function(event){
		$(this).find(".file-tool-delete").show();
	}).mouseleave(function(event){
		$(this).find(".file-tool-delete").hide();
	}).mousedown(function(event){
		event.preventDefault();
		var id = $(event.currentTarget).attr("data-id");
		var componentType = $this.getComponentTypeById(id);
		$this.dispatchMessage("selectedComponentType", componentType);
	});	
	container.find(".file-tool-delete").click(function(event){
		var fileBox = $(event.currentTarget).parent(".file-box");
		var id = $(fileBox).attr("data-id");
		this.components = this.components.filter( function(item){
			return item.id != id;
		});
		$(fileBox).remove();
	});
}
iflytek.resourcePanel.prototype.getComponentTypeById = function(id){
	var arr = this.components.filter( function(item){
			return item.id == id;
	});
	return arr.length>0? arr[0]: null;
}
iflytek.resourcePanel.prototype.changeFileToComponentType = function (file){
			return {
				id: uuid(),
				name: file.name,
				type: 3001,
				icon: file.url,
				options: {
					url: file.url
				}
			};
		}
iflytek.resourcePanel.prototype.initMessage = function(){
	
}
iflytek.resourcePanel.prototype.dispatchMessage = function(name, data){
	this.message.dispatch(name, data);
}