var config = {
	baseUrl : "/koala/view/",
	menu_top: [
	{name: "JQuery/Bootstrap"},
	{name: "FlyUI"},
	{name: "Vue"}],
	default_menu_content:[
		{name: "简介",  url: "page1.md"},
		{name: "流程图",  url: "page2.md"},
		{name: "代码示例",   url: "page3.md"}],
	menu_left: [
		{name: "前后端交互", url: "view1/"},
		{name: "前后端分离",  url: "view2/"},
		{name: "跨域", url: "view3/", children: [
			{name: "简介",  url: "page1.md"},
			{name: "小知识",  url: "page2.md"}
			]},
		{name: "表单验证",  url: "view4/", children: [
			{name: "简介",  url: "page1.md"},
			{name: "小知识",  url: "page2.md"}
			]},
		{name: "组件数据标准化",  url: "view5/", children: [
			{name: "表格数据",  url: "page1.md"},
			{name: "菜单数据",  url: "demo_menu/demo_menu.html"},
			{name: "Tab页数据",  url: "demo_tab/demo_tab.html"}
			]},
		{name: "组件之间消息传递",  url: "view6/", children: [
			{name: "自定义消息类",  url: "page1.md"},
			{name: "cookie",  url: "page2.md"},
			{name: "localStorage",  url: "page3.md"}
			]},
		{name: "项目标准化",  url: "view7/", children: [
			{name: "脚手架工程",  url: "page1.md"}
			]}
		]
};
