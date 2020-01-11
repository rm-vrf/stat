window.iflytek = window.iflytek || {};
iflytek.components = iflytek.components || {};
iflytek.components.myPagination = function(options){
	var defaultOptions = {
		pageSize: 10, 
		pageNum: 1,
		currPageNum: 1,
		totalPage: 0,
		total: 0,
		pageSizeList: [10, 20, 50],
		changePageHandler: function(data){}
	}
	$.extend(defaultOptions, options);
	this.options = defaultOptions;
	this.container = this.options.container;
	this.pageSize = this.options.pageSize;
	this.pageNum = this.options.pageNum; // 页码从1开始
	this.currPageNum = this.options.currPageNum;
	this.totalPage = this.options.totalPage;
}
iflytek.components.myPagination.prototype.setTotal = function(total){
	this.total = total;
	this.totalPage = Math.ceil(total/this.pageSize);
	this.setCurrPage(this.currPageNum, false);
}
iflytek.components.myPagination.prototype.setCurrPage = function(page, isDispatchEvent){
	this.currPageNum = page;
	if(page < 1 || page > this.totalPage){
		return;
	}else{
		this.currPageNum = page;
		this.init();
		if(isDispatchEvent){
			this.dispatchEvent();
		}
	}
}
iflytek.components.myPagination.prototype.init = function(){
	this.initHtml();
	this.initEventHandler();
}
iflytek.components.myPagination.prototype.initHtml = function(){
	var htmlContent = '<nav aria-label="Page navigation">';
	htmlContent += ' <div class="pagination-info" style="display:inline-block; float: left; margin: 20px 5px; line-height: 31px"><span >'+this.getInfoText()+'</span></div>'
	htmlContent += '<ul class="pagination" style="float: right" >';
	if(this.totalPage == 1){
		// 不显示分页
	}else if(this.totalPage <= 6){
		// 显示Previous，前N页，Next
		htmlContent +=  this.getPreviousBtnHtml();
		for(var i = 1; i<= this.totalPage; i++){
			htmlContent += this.getPageBtnHtml(i);
		}
		htmlContent +=  this.getNextBtnHtml();
	}else{
		if(this.currPageNum <= 4){
			// 显示Previous，前5页，...，最后一页，Next
			htmlContent +=  this.getPreviousBtnHtml();
			for(var i = 1; i<= 5; i++){
				htmlContent += this.getPageBtnHtml(i);
			}
			htmlContent +=  this.getOtherPageBtnHtml();
			htmlContent += this.getPageBtnHtml(this.totalPage);
			htmlContent +=  this.getNextBtnHtml();
		}else if(this.currPageNum >= this.totalPage-3){
			// 显示Previous，第一页，...，最后5页，Next
			htmlContent +=  this.getPreviousBtnHtml();
			htmlContent += this.getPageBtnHtml(1);
			htmlContent +=  this.getOtherPageBtnHtml();
			for(var i = this.totalPage-4; i<= this.totalPage; i++){
				htmlContent += this.getPageBtnHtml(i);
			}
			htmlContent +=  this.getNextBtnHtml();
		}else{
			// 显示Previous，第一页，...，中间3页，...，最后1页，Next
			htmlContent +=  this.getPreviousBtnHtml();
			htmlContent += this.getPageBtnHtml(1);
			htmlContent +=  this.getOtherPageBtnHtml();
			for(var i = this.currPageNum-1; i<= this.currPageNum+1; i++){
				htmlContent += this.getPageBtnHtml(i);
			}
			htmlContent +=  this.getOtherPageBtnHtml();
			htmlContent += this.getPageBtnHtml(this.totalPage);
			htmlContent +=  this.getNextBtnHtml();
		}
	}
	htmlContent += '</ul></nav>';
	this.container.html(htmlContent);
}
iflytek.components.myPagination.prototype.getPreviousBtnHtml = function(){
	return '<li class="pager-pre"><a  aria-label="Previous"><span aria-hidden="true">&laquo;</span></a></li>';
}
iflytek.components.myPagination.prototype.getPageBtnHtml = function(page){
	var className = "";
	if(page == this.currPageNum){
		className = "active";
	}
	return ' <li class="pager-page '+className+'" data="'+page+'"><a >'+page+'</a></li>';
}
iflytek.components.myPagination.prototype.getOtherPageBtnHtml = function(page){
	return ' <li ><a >...</a></li>';
}
iflytek.components.myPagination.prototype.getNextBtnHtml = function(){
	return '<li  class="pager-next"><a  aria-label="Next"> <span aria-hidden="true">&raquo;</span></a></li>';
}
iflytek.components.myPagination.prototype.initEventHandler = function(){
	var $this = this;
	// 前一页
	this.container.find(".pager-pre").off().click(function(event){
		$this.setCurrPage($this.currPageNum -1, true);
	});
	// 后一页
	this.container.find(".pager-next").off().click(function(event){
		$this.setCurrPage($this.currPageNum +1, true);
	});
	this.container.find(".pager-page").off().click(function(event){
		var page = parseInt($(this).attr("data"));
		$this.setCurrPage(page, true);
	});
}
iflytek.components.myPagination.prototype.getInfoText = function(){
	var from = this.pageSize * (this.currPageNum - 1) + 1;
	var to = this.currPageNum * this.pageSize;
	if(to > this.total){
		to = this.total;
	}
	return "第" + from + " 到 " + to + "  条，共 " + this.total + "  条";
}

iflytek.components.myPagination.prototype.dispatchEvent = function(){
	if(this.options.changePageHandler  instanceof Function){
		this.options.changePageHandler({
			pageSize: this.pageSize,
			pageNum: this.currPageNum
		});
	}
}
 $.fn.myPagination = function (options) {
        this.each(function () {
            options.container = $(this);
            new iflytek.components.myPagination(options);
        });
   };
