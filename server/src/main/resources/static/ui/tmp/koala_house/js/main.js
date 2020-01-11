var drawPanel = new iflytek.drawPanel($(".middle"));
var rightPanel = new iflytek.propertyPanel();
var toolContainer =null;
var componentTypePanel = null;
var resourcePanel = null;
var exportWindow = null;
window.codeMirrorWindow= null;
var rightPanelVisible = false;
var message =  new iflytek.message("drawPanel");
function init(){
	initSize();
	initMessage();
	initPanel();
	initEventHandler();
	
}
function initSize(){
	var contentHeight =getContentHeight();

	$(".left,.middle,.right").css("height", contentHeight + "px");
	$("right-arrow-container").css("top", -(contentHeight/2 - 15)+"px");
	$("#right-content").css("height", (contentHeight - 44)+ "px");
	
	var compTypeContanerHeight = contentHeight * 0.6 - 45;
	$("#left_content_theme").css("height", compTypeContanerHeight + "px");
	$("#left_content_user").css("height", compTypeContanerHeight + "px");
}
function getContentHeight(){
	var contentHeight =  $(document).height()-70;
	return contentHeight < 600? 600: contentHeight;
}
function getContentWidth(){
	return  $(document).width()-250;
}
function initPanel(){
	// 初始化画布
	drawPanel.init({
		width: getContentWidth() - 3,
		height: getContentHeight() - 3
	});
	// 左侧框
	componentTypePanel =  new iflytek.componentTypePanel({
		container: $("#left_content_theme"),
		data: iflytek.componentTypes
	});
	resourcePanel =  new iflytek.resourcePanel({
		container: $("#left_content_user"),
		data:[]
	});
	// 工具栏
	toolContainer = new iflytek.container.tool({
		container: $("#tool_container"),
		data: drawPanel.options
	});
	// 弹出框
	exportWindow =  new 	iflytek.titleWindow.export();
	codeMirrorWindow = new iflytek.titleWindow.codeMirror();
}
function initMessage(){
	message.on("showProperty", function(){
		showPropertyPanel();
	}).on("showPanelProperty", function(){
		showPropertyPanel();
	}).on("hideProperty", function(){
		hidePropertyPanel();
	}).on("export", function(){
		var value = JSON.stringify(drawPanel.layers);
		exportWindow.show(value);
	});
}

function propertyChangeHandler(data){
	drawPanel.interpreter.refreshComponent(data);
}
function initEventHandler(){
	$(".right .right-arrow").click(function(){
		var right = $(".right").css("right");
		if(!rightPanelVisible){
			showPropertyPanel();
		}else{
			hidePropertyPanel();
		}	
	});
}

function showPropertyPanel(){
	rightPanelVisible = true;
	$(".right").animate({ 
    	"right": "0px"
  	}, 300 , "swing", function(){
  		$(".right-arrow").attr("src", "img/view/right_arrow.png");
  	});
}
function hidePropertyPanel(){	
	rightPanelVisible = false;
	$(".right").animate({ 
    	"right": "-400px"
  	}, 300 , "swing", function(){
  		$(".right-arrow").attr("src", "img/view/left_arrow.png");
  	});
}
