window.iflytek = window.iflytek || {};
iflytek.manager = iflytek.manager || {};
iflytek.manager.selection = function(){
	this.visible = false;
};
// 预选边界框，单例
iflytek.manager.selection.prototype.showSelectionBound = function (width, height){
	this.getSelectionBound(width, height).show();
	this.visible = true;
}
iflytek.manager.selection.prototype.hideSelectionBound = function (){
	this.getSelectionBound().hide();
	this.visible = false;
}
iflytek.manager.selection.prototype.moveSelectionBound = function (x, y){
	var selection = this.getSelectionBound();
	selection.css({
		top: y + "px",
		left: x + "px"
	});
	if(!this.visible){
		selection.show();
		this.visible = true;
	}
}
iflytek.manager.selection.prototype.getSelectionBound = function (width, height){
	var selection = $("#draw_panel_selection");
	if(selection.length == 0){
		var htmlContent = "<div id=\"draw_panel_selection\" class=\"selection\"></div>";
		$("#draw_panel_content").append(htmlContent);
		selection = $("#draw_panel_selection");
	}
	if(width != undefined){
		selection.css("width", width + "px");
	}
	if(height != undefined){
		selection.css("height", height + "px");
	}
	return selection;
}