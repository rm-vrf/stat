### 	一、表格数据格式
#### 	要注意的问题
![](/koala/view/view5/1.PNG)
#### 	jqGrid的数据格式
1、整体

- 表格边界（width/height/rowNum/滚动条)
- 其它表格操作项的位置(分页、工具栏）
- 可编辑表格对应信息 
- 子表格对应信息
- 数据为空/复选框的显示问题
- 等等

2、表头colNames， 以“列”为单位
- 显示的文本

3、表体colModel

- 唯一性（key)
- 样式问题（width/align/sortable/classes/hidden) 
- 显示的文本(label/formatter/sort字段)
- 事件（点击行/单元格）

4、分页

- 所在容器：page属性
- 与ajax请求参数配合
 
5、 封装的ajax请求
- 请求参数映射jsonReader 
6、 操作列
- 单元格中可以填充html,通过formatter进行填充，事件也包含在html中 

### 二、扩展性较好的数据格式

#### 需要解决的问题
- 表格显示问题/复选框/排序/分页/无数据
- 单元格的内容可扩展
- 单元格的样式/事件可扩展
- 单元格的tooltip自定义
#### 数据格式
   
    // 表格参数格式
	var options = {
			grid: {
				width: 100%,
				height: 100%,
				key: "id", 	// 数据唯一标识符， 默认为id
				showCheckBox: true, 	 // 是否显示复选框
				message: {
					empty:  "此表格无数据"
				},
				headerStyle: "class", 	 // 头部默认样式
				bodyStyle: "class", 
				rowToolTipFun: function(rowData){  
					
				}
				selectRowHandler: function(event){  	// event的data里面包含rowData
					
				}
			},
			header: [{      	// 可为字符串数组["name", "sex", "country", ...]
				name: "name", 
				sortField: "name", 	// 排序时传到后台的字段, 默认与name属性值一致
				style: "width: 100px"  // 每一列自定义样式
			}],   
			columns: [
				{
					type: 0,	 // 类型：0表示span标签，1表示a元素， 2表示按钮...， 后续可扩展
					labelField: "name", 	// 需要展示的文本属性,
					labelFun: function(rowData, labelField){},  	// 可传入html
					toolTipFun: function(rowData, labelField){},  // 默认提示语为当前文本
					handler: function(event){        // 用户操作处理事件，如click、change事件； 
							// event的data里面包含rowData, rowIndex, colIndex, options
					}
				}
			],
			data: []    // 数据数组
		}