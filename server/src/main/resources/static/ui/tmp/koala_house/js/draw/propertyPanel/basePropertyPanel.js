window.iflytek = window.iflytek || {};
iflytek.basePropertyPanel = iflytek.basePropertyPanel || function() {
	this.data = null;
	// 消息
	this.message = new iflytek.message("drawPanel");
	this.dispatchMessageType = "changeProperty";
}

iflytek.basePropertyPanel.prototype.init = function(data) {
	this.$navContainer = $("#right-nav");
	this.$contentContainer = $("#right-content");
	if(this.data == null || data.id !== this.data.id) {
		this.data = data;
		this.initHtml();
	} else {
		this.data = data;
		this.refresh();
	}
}
iflytek.basePropertyPanel.prototype.initHtml = function() {
	$this = this;
	this.$navContainer.empty();
	this.$contentContainer.empty();
	new iflytek.tab({
		tabContainer: "#right-nav", // tab项父容器
		contentContainer: "#right-content", // html显示区父容器
		override: false, // 内容是否覆盖：若为true，则每次加载都清空html再加载；若为false,则已加载过的html内容不会重新加载
		loadCompletedHandler: function(item, index) {
			var container = $this.$contentContainer.find(".tab-content[index=" + index + "]")
			$this.fillData($(container));
			$this.initContainer($(container), item);
		},
		data: this.getTabData() // 数据源
	});
}

iflytek.basePropertyPanel.prototype.getTabData = function() {
	var component = this.data;
	var arr = [{
		name: "基本信息",
		url: "view/type0/base_info.html"
	}];
	for(var i = 0; i < arr.length; i++) {
		arr[i].data = this.data;
	}
	return arr;
}
iflytek.basePropertyPanel.prototype.fillData = function($container) {
	var type = this.data.type;
	this.fillDataByDefault($container);
}
iflytek.basePropertyPanel.prototype.fillDataByDefault = function($container) {
	$this = this;
	this.getInputs($container).each(function() {
		$this.fillInputData(this);
	});
	// 填充背景组件
	$container.find(".style-component-background").backgroundStyleComp({
		data: this.data,
		changePropertyHandler: changePropertyHandler
	});
	// 填充边框属性组件
	$container.find(".style-component-border").bordersStyleComp({
		data: this.data,
		changePropertyHandler: changePropertyHandler
	});
	// 填充字体属性组件
	$container.find(".style-component-font").fontStyleComp({
		data: this.data,
		changePropertyHandler: changePropertyHandler
	});

	function changePropertyHandler(data) {
		$.extend($this.data, data);
		$this.dispatchMessage($this.data);
	}
}

iflytek.basePropertyPanel.prototype.fillInputData = function(input) {
	var value = this.getPropValue($(input), this.data);
	var dataType = $(input).attr("data-type");
	switch(dataType) {
		case "text":
		case "number":
			value = value + "";
			$(input).val(value);
			break;
		case "json":
			value = JSON.stringify(value);
			$(input).val(value);
			break;
		case "boolean": // radio组
			if($(input).val() == value) {
				$(input).checked = true;
			} else {
				$(input).checked = false;
			}
			break;
	}
}
iflytek.basePropertyPanel.prototype.getInputs = function($container) {
	return $container.find("input, textarea");
}
iflytek.basePropertyPanel.prototype.changeValue = function(data, input) {
	var target = $(input);
	var names = target.attr("name").split(".");
	var value = target.val();
	if(target.attr("data-type") == "number") {
		value = parseInt(value);
	} else if(target.attr("data-type") == "json") {
		try {
			value = JSON.parse(value);
		} catch(error) {
			console.error("Options data error: property '" + target.attr("name") + "'is not format json!");
		}
	}
	this.parseProp(data, names, value);
}
iflytek.basePropertyPanel.prototype.initContainer = function($container, item) {
	var $this = this;
	var containerName = $($container.children()[0]).attr("name");
	switch(containerName) {
		case "info_container":
			this.initInfoContainer($container, item);
			break;
		case "options_container":
			this.initOptionsContainer($container, item);
			break;
		case "service_container":
			this.initServiceContainer($container, item);
			break;
	}
	// 双击打开Json编辑窗口
	$container.find("[data-type='json']").dblclick(function(event) {
		var target = $(event.currentTarget);
		window.codeMirrorWindow.show(target.val(), function(newValue) {
			target.val(newValue);
		});
	});

}

iflytek.basePropertyPanel.prototype.initInfoContainer = function($container, item) {
	var $this = this;
	this.getInputs($container).change(function(event) {
		$this.changeValue(item.data, event.currentTarget);
		$this.dispatchMessage(item.data);
	});
}
iflytek.basePropertyPanel.prototype.initOptionsContainer = function($container, item) {
	var $this = this;
	$container.find(".change-input").off().change(function(event) {
		$this.changeValue(item.data, event.currentTarget);
		$this.dispatchMessage(item.data);
	});
	$container.find("button").click(function(event) {
		var operateName = $(event.currentTarget).attr("name");
		switch(operateName) {
			case "save":
				$this.getInputs($container).each(function() {
					$this.changeValue(item.data, this);
				});
				$this.dispatchMessage(item.data);
				break;
			case "reset":
				break;
		}
	});
}
iflytek.basePropertyPanel.prototype.initServiceContainer = function($container, item) {
	var $this = this;
	// 初始化页面
	// 填充数据源-数组表格
	var datasourceArrTable = $container.find("#datasource_array_table").myTable({
		showPager: false,
		columns: getDatasourceArrColumns(),
		data: getDatasourceArrTableData(),
		events: {
			changeValue: function(rowData) {
				refreshDatasourceArr();
			},
			removeRow: function(rowData) {
				datasourceArrTable.removeRow(rowData);
				refreshDatasourceArr();
			}
		}
	});
	// 处理数据源-数组新增事件
	$container.find("#add_datasourceArr_row_btn").off().click(function() {
		var cols = $container.find("#datasource_array_cols").val();
		var rowArr = [];
		for(var i = 0; i < cols.length; i++) {
			rowArr.push(0);
		}
		datasourceArrTable.addRow(rowArr);
		refreshDatasourceArr();
	});

	function getDatasourceArrColumns() {
		var cols = $container.find("#datasource_array_cols").val();
		var columns = [];
		for(var i = 0; i < cols; i++) {
			columns.push({
				type: 3,
				headerField: "值" + (i + 1),
				labelField: i
			})
		}
		columns.push({
			type: 2,
			headerField: '<a id="add_datasourceArr_row_btn">+</a>',
			labelData: [{
				label: "--",
				title: "",
				eventType: "removeRow"
			}]
		});
		return columns;
	}

	function getDatasourceArrTableData() {
		return $this.data.service.array;
	}

	function refreshDatasourceArr() {
		$this.data.service.array = datasourceArrTable.data;
		$this.dispatchMessage($this.data);
	}
	// 初始化事件
	$container.find(".change-input").off().change(function(event) {
		$this.changeValue($this.data, event.currentTarget);
		$this.dispatchMessage($this.data);
	});
	$container.find("input[name='serviceType']").change(function(event) {
		var target = $(event.currentTarget);
		item.data.serviceType = target.val();

		$container.find(".data-source-tab").removeClass("active");
		var tabId = target.attr("ref");
		$("#" + tabId).addClass("active");
	});
	$container.find("button").click(function(event) {
		var operateName = $(event.currentTarget).attr("name");
		switch(operateName) {
			case "save":
				$container.find("textarea").each(function(event) {
					$this.changeValue(item.data, this);
				});
				$this.dispatchMessage(item.data);
				break;
			case "reset":
				break;
		}
	});
}
iflytek.basePropertyPanel.prototype.refresh = function() {
	$this = this;
	this.$contentContainer.find(".tab-content input").each(function() {
		$this.fillInputData(this);
	});
}
iflytek.basePropertyPanel.prototype.getPropValue = function(element, data) {
	if(!element.attr("name")) {
		return;
	}
	var names = element.attr("name").split(".");
	var value = data;
	for(var i = 0; i < names.length; i++) {
		// 若当前值不存在，则初始化该值
		if(value[names[i]] == null || value[names[i]] == undefined) {
			value[names[i]] = {};
		}
		value = value[names[i]];
	}
	return value;
}
iflytek.basePropertyPanel.prototype.parseProp = function(target, attrs, value) {
	var currAttr = null;
	if(attrs.length == 1) {
		target[attrs[0]] = value;
	} else {
		for(var i = 0; i < attrs.length; i++) {
			if(i == 0) {
				currAttr = target[attrs[0]];
			} else if(i == attrs.length - 1) {
				currAttr[attrs[i]] = value;
			} else {
				if(!currAttr[attrs[i]]) {
					currAttr[attrs[i]] = {};
				}
				currAttr = currAttr[attrs[i]];
			}
		}
	}
}

iflytek.basePropertyPanel.prototype.dispatchMessage = function(data) {
	var name = this.dispatchMessageType;
	this.message.dispatch(name, data);
}