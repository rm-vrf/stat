iflytek.baseChartsPropertyPanel = iflytek.baseChartsPropertyPanel || function() {
	// 继承父类属性
	iflytek.basePropertyPanel.apply(this, arguments);
}
iflytek.utils.extends(iflytek.basePropertyPanel, iflytek.baseChartsPropertyPanel);
iflytek.baseChartsPropertyPanel.prototype.getTabData = function() {
	var component = this.data;
	var fatherUrl = "";
	var arr = [];
	fatherUrl = "view/type1/";
	arr = [
			{ name: "基本信息", url: fatherUrl + "base_info.html" },
			{ name: "参数信息", url: fatherUrl + "options_info.html" },
			{ name: "数据信息", url: fatherUrl + "data_source.html" },
	];
	for(var i = 0; i < arr.length; i++) {
		arr[i].data = this.data;
	}
	return arr;
}
iflytek.baseChartsPropertyPanel.prototype.fillData = function($container) {
	this.fillChartsData($container);
}
iflytek.baseChartsPropertyPanel.prototype.fillChartsData = function($container) {
	$this = this;
	this.getInputs($container).each(function() {
		$this.fillInputData(this);
	});
	/**-----------1、渲染图标标题--------------**/
	// 1-1填充背景组件
	$container.find(".style-component-background").backgroundStyleComp({
		data: convertTextStyleToStyle(this.data.options.title),
		changePropertyHandler: function(data) {
			changePropertyHandler($container.find(".style-component-background"), data);
		}
	});
	// 1-2填充边框属性组件
	$container.find(".style-component-border").borderStyleComp({
		data: convertTextStyleToStyle(this.data.options.title),
		changePropertyHandler: function(data) {
			changePropertyHandler($container.find(".style-component-border"), data);
		},
		showStyle: false,
		showPadding: false
	});
	// 文字属性设置事件绑定
	$container.find(".text-style-edit").click(function() {
		var textStyle = $this.getPropValue($(this), $this.data);
		var fontStyleWindow = new iflytek.titleWindow.fontStyle({
			data: convertTextStyleToStyle(textStyle),
			confirmHandler: function(data) {
				$.extend(textStyle, convertStyleToTextStyle(data.style));
				$this.dispatchMessage($this.data);
			}
		});
	});
	/**-----------2、填充图例表格组件--------------**/
	var legendTable = $container.find("#charts_legend_table_container").myTable({
		showPager: false,
		columns: getLegendColumns(),
		data: getLegendTableData(),
		events: {
			changeValue: function(rowData) {
				refreshLegend();
			},
			removeRow: function(rowData) {
				legendTable.removeRow(rowData);
				refreshLegend();
			}
		}
	});
	// 处理图例新增事件
	$container.find("#add_legend_row_btn").off().click(function() {
		legendTable.addRow({
			id: uuid(),
			value: "newValue"
		});
		refreshLegend();
	});

	function refreshLegend() {
		var arr = legendTable.data.map(function(item) {
			return item.value;
		});
		$this.data.options.legend.data = arr;
		for(var i = 0; i < arr.length; i++) {
			if($this.data.options.series[i]) {
				$this.data.options.series[i].name = arr[i];
			}
		}
		$this.dispatchMessage($this.data);
	}

	/**-----------3、填充横坐标表格组件--------------**/
	var xAxisTable = $container.find("#charts_xAxis_table_container").myTable({
		showPager: false,
		columns: getXAxisColumns(),
		data: getXAxisTableData(),
		events: {
			changeValue: function(rowData) {
				refreshXAxis();
			},
			removeRow: function(rowData) {
				xAxisTable.removeRow(rowData);
				refreshXAxis();
			}
		}
	});
	// 处理图例新增事件
	$container.find("#add_xAxis_row_btn").off().click(function() {
		xAxisTable.addRow({
			id: uuid(),
			value: "newValue"
		});
		refreshXAxis();
	});

	function refreshXAxis() {
		var arr = xAxisTable.data.map(function(item) {
			return item.value;
		});
		$this.data.options.xAxis.data = arr;
		$this.dispatchMessage($this.data);
	}
	/**-----------4、填充series表格组件--------------**/
	var seriesTable = $container.find("#charts_series_table_container").myTable({
		showPager: false,
		columns: getSeriesColumns(),
		data: getSeriesTableData(),
		events: {
			changeValue: function(rowData) {
				refreshSeries();
			},
			removeRow: function(rowData) {
				seriesTable.removeRow(rowData);
				refreshSeries();
			},
			setSeriesAttr: function(rowData){
				var seriesStyleWindow = new iflytek.titleWindow.seriesStyle({
					data: rowData,
					confirmHandler: function(data) {
						$.extend(rowData, data);
						$this.dispatchMessage($this.data);
					}
				});
			}
		}
	});

	function refreshSeries() {
		$this.data.options.series = seriesTable.data;
		$this.dispatchMessage($this.data);
	}
	// 小函数
	function changePropertyHandler(target, data) {
		var style = $this.getPropValue(target, $this.data);
		$.extend(style, convertStyleToTextStyle(data.style));
		$this.dispatchMessage($this.data);
	}

	function getLegendTableData() {
		var resultArr = [];
		var legendArr = $this.data.options.legend.data;
		if(legendArr) {
			for(var i = 0; i < legendArr.length; i++) {
				resultArr.push({
					id: uuid(),
					value: legendArr[i],
				});
			}
		}
		return resultArr;
	}

	function getXAxisTableData() {
		var resultArr = [];
		var xAxisArr = $this.data.options.xAxis.data;
		if(xAxisArr) {
			for(var i = 0; i < xAxisArr.length; i++) {
				resultArr.push({
					id: uuid(),
					value: xAxisArr[i],
				});
			}
		}
		return resultArr;
	}

	function getSeriesTableData() {
		var resultArr = [];
		var seriesArr = $this.data.options.series;
		for(var i = 0; i < seriesArr.length; i++) {
			seriesArr[i].id = uuid();
		}
		return seriesArr;
	}

}
/**------------------用到的小函数-----------------------**/
function convertTextStyleToStyle(textStyle) {
	var style = {};
	if(textStyle) {
		style["color"] = textStyle.color;
		style["font-style"] = textStyle.fontStyle;
		style["font-weight"] = textStyle.fontWeight;
		style["font-size"] = textStyle.fontSize;
		style["font-family"] = textStyle.fontFamily;
		style["border-width"] = textStyle.borderWidth;
		style["border-color"] = textStyle.borderColor;
		style["border-radius"] = textStyle.borderRadius;
	}
	return { style: style };
}

function convertStyleToTextStyle(style) {
	var textStyle = {};
	if(style) {
		textStyle.color = style["color"];
		textStyle.fontStyle = style["font-style"];
		textStyle.fontWeight = style["font-weight"];
		if(style["font-size"]) {
			textStyle.fontSize = parseInt(style["font-size"]);
		}
		textStyle.fontFamily = style["font-family"];
		textStyle.backgroundColor = style["background-color"];
		if(style["border-width"]) {
			textStyle.borderWidth = parseInt(style["border-width"]);
		}
		if(style["border-color"]) {
			textStyle.borderColor = style["border-color"];
		}
		if(style["border-radius"]) {
			textStyle.borderRadius = parseInt(style["border-radius"]);
		}
	}
	return textStyle;
}

function getLegendColumns() {
	return [
		{ type: 3, headerField: "数据项", labelField: "value" },
		{
			type: 2,
			headerField: '<a id="add_legend_row_btn">+</a>',
			style: { width: "60px" },
			labelData: [{ label: "删除", title: "", eventType: "removeRow" }]
		}
	];
}

function getXAxisColumns() {
	return [
		{ type: 3, headerField: "数据项", labelField: "value" },
		{
			type: 2,
			headerField: '<a id="add_xAxis_row_btn">+</a>',
			style: { width: "60px" },
			labelData: [{ label: "删除", title: "", eventType: "removeRow" }]
		}
	];
}

function getSeriesColumns() {
	return [
		{ type: 4, headerField: "类型", labelField: "type", labelData: getSeriesTypes() },
		{
			type: 2,
			headerField: "数据属性",
			style: { width: "200px" },
			labelData: [{ label: "系列属性", title: "", eventType: "setSeriesAttr" },
				{ label: "删除", title: "", eventType: "removeRow" }
			]
		}
	];
}

function getSeriesTypes() {
	return [{ label: "line", value: "line" },
		{ label: "bar", value: "bar" },
		{ label: "pie", value: "pie" },
		{ label: "graph", value: "graph" }
	]
}