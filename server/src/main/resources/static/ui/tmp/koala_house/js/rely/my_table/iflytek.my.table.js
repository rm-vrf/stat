window.iflytek = window.iflytek || {};
iflytek.components = iflytek.components || {};
iflytek.components.myTable = function(options){
	var defaultOptions = {
		 container: "",
		 pager: "",
		 width: "100%",
         height: "100%",
         showPager: true,
		 currPageNum: 1,
         key: "id",  // 数据唯一标识符， 默认为id
         showCheckBox: true,      // 是否显示复选框
         message: {
            empty:  "此表格无数据"
         },
        columns: [
        	 {
                type: 0,     // 类型： 0表示checkBox , 1表示span标签，2表示a元素，
                headerField: "",
                style: "",
       			sortField: "",
       			labelData: [], 
                labelField: "name",     // 需要展示的文本属性,
                labelFunction: function(rowData, labelField){},      // 可传入html
                toolTipFunction: function(rowData, labelField){},  // 默认提示语为当前文本
                handler: function(event){        // 用户操作处理事件，如click、change事件； 
                        // event的data里面包含rowData, rowIndex, colIndex, options
                }
            }
        ],
        data: {},
        total: 0,
        events: {},
	};
	$.extend(defaultOptions, options);
	this.options = defaultOptions;
	this.container = this.options.container;
	this.data = options.data;
	// 若行数据没有id，则自动构建
	if(this.data && this.data.length > 0 && !this.data[0][this.options.key]){
		for(var i = 0; i< this.data.length; i++){
			this.data[i][this.options.key] = uuid();
		}
	}
	this.total = options.total;
	this.dataMap = {};
	this.condition = {
       pageSize: 10, 
        pageNum: 1,
        sort: "",
        order: ""
	 };
	 this.requestHandler = this.options.requestHandler;
	this.init();
};
iflytek.components.myTable.prototype.init = function(){
	this.initHtml();
	if(this.data.length > 0){
		this.initEventHandler();
		this.initPagination();
	}else{
		this.showNoData();
	}
};
iflytek.components.myTable.prototype.initPagination = function(){
	if(this.options.showPager){
		var $this = this;
		this.pagination = new iflytek.components.myPagination({
			container: this.options.pager,
            currPageNum: this.options.currPageNum,
			changePageHandler: function(data){
				$this.condition.pageSize = data.pageSize;
				$this.condition.pageNum = data.pageNum;
				$this.condition.sort = "";
				$this.condition.order = "";
				$this.doQuery();
			}
		});
		this.pagination.setTotal(this.total);
	}
};
iflytek.components.myTable.prototype.initHtml = function(){
	var tableHtml = '<table class="table table-hover table-bordered  my-table">';
	tableHtml += this.getHeaderHtml();
	tableHtml += this.getBodyHtml();
	tableHtml += '</table>';
	this.container.html(tableHtml);
	// 初始化页面默认值
	this.container.find("select").each(function(){
		$(this).val($(this).attr("value"));
	});
};
iflytek.components.myTable.prototype.getHeaderHtml = function(){
	var headerHtml = '<thead>';
	var cols = this.options.columns;
	for(var i = 0; i< cols.length; i++){
		var col = cols[i];
		var columnStyle = this.getColumnStyle(col.style);
		headerHtml += '<th class="mytable-th" style="'+columnStyle+'">';
		switch(col.type){
			case 0: // checkBox
				headerHtml += '<input  type="checkbox" class="checkbox th-checkbox" />';
			break;
			default: // span
				headerHtml += '<span>'+col.headerField+'</span> ';
				headerHtml += this.getSortFieldHtml(col);
			break;
		}
		headerHtml += "</th>";
	}
	headerHtml += '</tr></thead>';
	return headerHtml;
};
iflytek.components.myTable.prototype.getSortFieldHtml = function(col){
	var htmlContent = "";
	if(col.sortField && col.sortField != ""){
		htmlContent = '<div class="sort-container">'
			+ '<a class="sort" type="asc" data="'+col.sortField+'"><span class="glyphicon glyphicon-triangle-top" style="block" ></span></a>'
			+ '<a class="sort" type="desc"  data="'+col.sortField+'"><span class="glyphicon glyphicon-triangle-bottom" style="block" ></span></a>'
			+'</div>';
	}
	return htmlContent;
}
iflytek.components.myTable.prototype.getColumnStyle = function(headerStyle){
		var defaultHeaderStyle = {"text-align": "center"};
		$.extend(defaultHeaderStyle, headerStyle);
		var style = "";
		try{
			var arr = [];
			for(var prop in defaultHeaderStyle){
				arr.push(prop + ":" + defaultHeaderStyle[prop]);
			}
			style = arr.join(";");
		}catch(err){
			console.error("parse header style error：" + err.toString());
		}
		return style;
}
iflytek.components.myTable.prototype.getBodyHtml = function(){
	var $this = this;
	var bodyHtml = '<tbody>';
	var arr = this.data;
	var cols = this.options.columns;
	var key = this.options.key;
	for(var i = 0; i< arr.length; i++){
		var item = arr[i];
		$this.dataMap[item[key]] = item;
		// 渲染tr的html
		var rowHtml = ' <tr data-id="'+item[key]+'">';
		for(var j = 0; j < cols.length; j++ ){
			// 渲染td的html
			var col = cols[j];
			var coumnStyle = this.getColumnStyle(col.style);
			rowHtml += '<td style="'+coumnStyle+'">';
			var text = item[col.labelField];
			// 空处理
            text = text == null? "": text;
			var title = text;
			if(col.labelFunction instanceof Function){
				text  = col.labelFunction(item, col.labelField, i);
			}
			if(col.toolTipFunction instanceof Function){
				title  = col.toolTipFunction(item, col.labelField, i);
			}
			switch(col.type){
				case 0: // checkBox
					rowHtml += '<input  type="checkbox" class="checkbox tr-checkbox" /> ';
				break;
				case 2: // 操作列a元素
					for(var k = 0; k< col.labelData.length; k++){
						var labelItem = col.labelData[k];
						rowHtml += '<a class="tr-tool" event-type="'+labelItem.eventType+'"><span title="'+labelItem.title+'">'+labelItem.label+'</span> </a>';
					}
				break;
				case 3: // 文本框
					rowHtml += '<input class="tr-tool-input" event-type="changeValue" label-field="'+col.labelField+'" type="text" value="'+text+'" />';
				break;
				case 4: // 下拉框
					rowHtml += '<select class="tr-tool-select" event-type="changeValue" label-field="'+col.labelField+'"  value="'+text+'" >';
					for(var k = 0; k< col.labelData.length; k++){
						var labelItem = col.labelData[k];
						rowHtml += '<option value="'+labelItem.value+'" >'+labelItem.label+'</option>';
					}
					rowHtml += '</select>';
				break;
				default: // span
					rowHtml += '<span title="'+title+'">'+text+'</span>';
				break;
			}	
			rowHtml += '</td>';
		}
		rowHtml += '</tr>';
		bodyHtml += rowHtml;
	}
	bodyHtml += '</tbody>';
	return bodyHtml;
};
iflytek.components.myTable.prototype.initEventHandler = function(){
	var $this = this;
	// checkBox
	this.container.find(".th-checkbox").off().change(function(event){
		var value = $(this).prop("checked");
		// 处理数据
		for(var i  = 0; i< $this.data.length; i++){
			$this.data[i].selected = value;
		}
		// 处理页面显示
		$this.container.find(".tr-checkbox").prop("checked", value);
	});
	$this.container.find(".tr-checkbox").off().change(function(event){
		var value = $(this).prop("checked");
		var rowData = getRowData(this);
		// 处理数据
		rowData.selected = value;
		// 处理页面显示
		var allSeleted  =$this.isAllSeclected();
		$this.container.find(".th-checkbox").prop("checked", allSeleted);
	});
	// 编辑列
	$this.container.find(".tr-tool").off().click(function(event){
		var eventType = $(this).attr("event-type");
		var data = getRowData(this);
		// 分派事件
		var events = $this.options.events;
		 if(events[eventType] instanceof Function){
		 	events[eventType](data);
		 }
	});
	// 排序
	$this.container.find(".sort").off().click(function(event){
		// 调整样式
		$this.container.find(".sort").removeClass("active");
		$(this).addClass("active");
		// 传递事件
		$this.condition.sort =  $(this).attr("data");
		$this.condition.order =  $(this).attr("type");
		$this.doQuery();
	});
	// 文本框change处理事件
	$this.container.find(".tr-tool-select,.tr-tool-input").off().change(function(event){
		var eventType = $(this).attr("event-type");
		var labelField = $(this).attr("label-field");
		var data = getRowData(this);
		data[labelField] = $(this).val();
		var events = $this.options.events;
		 if(events[eventType] instanceof Function){
		 	events[eventType](data);
		 }
	});
	function getRowData(target){
		var tr = $(target).parents("tr")[0];
		var data_id = $(tr).attr("data-id");
		return $this.dataMap[data_id];
	}
}
iflytek.components.myTable.prototype.setData = function(data){
	this.options.data  = this.data =data;
	this.container.find("tbody").empty();
	if(data.length > 0){
		this.container.find("table").append(this.getBodyHtml());
		this.initEventHandler();
		this.initPagination();
	}else{
		this.showNoData();
	}
}
// 无数据
iflytek.components.myTable.prototype.showNoData = function(){
	var noDataHtml = '<tr><td colspan="'+this.options.columns.length+'"><span style="padding: 10px; height:60px;"> 数据为空</span></td></tr>';
	this.container.find("tbody").html(noDataHtml);
	if(this.options.pager){
		this.options.pager.empty();
	}
}
iflytek.components.myTable.prototype.isAllSeclected = function(){
	for(var i = 0; i < this.data.length; i++){
		if(!this.data[i].selected){
				return false;
		}
	}
	return true;
}
iflytek.components.myTable.prototype.removeRow = function(rowData){
	var key = this.options.key;
	this.container.find("tr[data-id='"+rowData[key]+"']").remove();
	this.data = this.data.filter(function(item){
		return item[key] != rowData[key];
	});
}
iflytek.components.myTable.prototype.addRow = function(rowData){
	this.data.push(rowData);
	this.setData(this.data);
}
iflytek.components.myTable.prototype.doQuery = function(type){
	if(this.options.requestHandler  instanceof Function){
		this.options.requestHandler(this.condition);
	}
}
 $.fn.myTable = function (options) {
       options.container = $(this);
        return new iflytek.components.myTable(options);
    };

