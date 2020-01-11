(function($){
	iflytek = window.iflytek || {};
	iflytek.tab = iflytek.tab || function(options){
		var initOptions = {
			tabContainer: "",
			contentContainer: "",
			data: [],
			selectedIndex: 0,
			loadCompletedHandler: null
		};
		var options = $.extend(initOptions, options);
		// 初始化
		function init(){
			initMenuHtml();
			initEventHandler();
			showPage(0);
		}
		// 初始化菜单内容
		function initMenuHtml(field ){
			var menuArr = options.data;
			var field = field || "name";
			var htmlContent = "";
			for(var i = 0; i<menuArr.length; i++){
				if(i == 0){
					htmlContent += '<li  class="active"><a href="#">'+menuArr[i][field]+'</a></li>';
				}else{
					htmlContent += '<li><a href="#">'+menuArr[i][field]+'</a></li>';
				}
			}
			$(options.tabContainer).html(htmlContent);
		}
		function initEventHandler(){
			$(options.tabContainer + " li").click(function(){
				// 样式
				$(options.tabContainer + " li").removeClass("active");
				$(this).addClass("active");
				// 事件
				var currIndex =  $(this).index();
				if(options.selectedIndex !=currIndex){
					showPage(currIndex);
				}
			});
		}
		var xhr = null;
		function showPage(index){
			options.selectedIndex = index;
			var currItem = options.data[index];
			var $container = $(options.contentContainer);
			if(options.override){
				// 如果覆盖，则先清空html
				$container.empty();
				loadPage();
			}else{
				// 如果不是覆盖，则先判断该数据是否加载过
				$container.find(".tab-content").hide();
				if(!currItem.completed){
					loadPage();
				}else{
					$container.find(".tab-content[index="+index+"]").show();
				}
			}
			
		}
		function loadPage(callback){
			// 获取展示页面的地址
			var url = options.data[options.selectedIndex].url;
			if(xhr != null){
				xhr.abort();
			}
			xhr = iflytek.utils.load(url, function(response){
				// html
				var currIndex = options.selectedIndex;
				var currItem = options.data[currIndex];
				var resultHtml = "<div class='tab-content'  index="+currIndex+">" + response + "</div>";
				$(options.contentContainer).append(resultHtml);
				// 加载js
				if(currItem.js){
					var moduleName = "iflytek_tab_" + currIndex + "_"+ currItem.name;
					if(options.override){
						moduleName = "iflytek_tab" 
					}
					require([currItem.js], function (tab){
				　　 if(tab.init instanceof Function){
							tab.init(currItem, options);
					    }
				　});
				}
				// 改变状态
				currItem.completed = true;
				// 执行回调
				if(options.loadCompletedHandler instanceof Function){
					options.loadCompletedHandler(currItem, currIndex);
				}
				if(callback instanceof Function){
					callback();
				}
			});
		}
		return init();
	};
	
	
	

	
})(jQuery);
