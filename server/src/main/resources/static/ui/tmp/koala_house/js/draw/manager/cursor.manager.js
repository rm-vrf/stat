window.iflytek = window.iflytek || {};
iflytek.manager = iflytek.manager || {};
iflytek.manager.cursor = function(){};

iflytek.manager.cursor.prototype.changeCursor = function(type){
	var cursorClass = "";
	if(type > 4000){
		cursorClass = 'url(img/cursor/text.ico),auto';
	}else if(type > 3000){
		cursorClass = 'url(img/cursor/img.ico),auto';
	}else if(type > 1000){
		cursorClass = 'url(img/cursor/chart.ico),auto';
	}
	this.removeCursor();
	$("body")[0].style.cursor= cursorClass;
	
}
iflytek.manager.cursor.prototype.removeCursor = function(container){
	$("body")[0].style.cursor= "default";
}
iflytek.manager.cursor.prototype.setCursor = function(cursor){
	$("body")[0].style.cursor= cursor;
}

