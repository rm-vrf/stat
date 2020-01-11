window.iflytek = window.iflytek || {};
iflytek.componentTypes =  [
			{groupName: "图表", children: [
				{id:"1001", name: "折线图", type: 1001, icon: "img/componentType/line_chart.png"},
				{id:"1002", name: "柱状图", type: 1002, icon: "img/componentType/graph_chart.jpg"},
				{id:"1003", name: "饼状图", type: 1003, icon: "img/componentType/pie_chart.png"}]
			},
			{groupName: "组件", children: [
				{id:"2001", name: "表格", type: 2001, icon: "img/componentType/component_table.png"}]
			},
			{groupName: "图片", children: [	
				 {id:"1004", name: "图片_dog", type: 3001, icon: "repository/img/animal1.gif", 
					options: {
						url:"repository/img/animal1.gif"
					}},
				{id:"1005", name: "图片_cat", type: 3002, icon: "repository/img/animal4.gif", 
					options: {
						url:"repository/img/animal4.gif"
					}},
				{id:"1006", name: "图片_kitty", type: 3003, icon: "repository/img/animal5.gif", 
					options: {
						url:"repository/img/animal5.gif"
					}}
				]
			},
			{groupName: "文字", children: [		
				 {id:"1007", name: "文字", type: 4001, icon: "img/componentType/text.png", 
					 options: {
						text: "请添加文本"
					}
				}
			]}
]