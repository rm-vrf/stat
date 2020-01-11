(function($){
	/**页面参数**/
	var leftNavIndex = 0;
	var mainNavIndex = 0;
	var currContentMenu;
	
	/**页面初始化**/
	$(document).ready(function(){
		initHtml();
		initEventHandler();
		init();
	});
	/**初始化**/
	function init(){
		showPage();
	}
	/**初始化页面**/
	function initHtml(){
		initMenuHtml("#top-nav", config.menu_top);
		initMenuHtml("#left-nav", config.menu_left);
		
		initContentMenu();
	}
	// 初始化菜单内容
	function initMenuHtml(container, menuArr, field ){
		var field = field || "name";
		var htmlContent = "";
		for(var i = 0; i<menuArr.length; i++){
			if(i == 0){
				htmlContent += '<li  class="active"><a href="#">'+menuArr[i][field]+'</a></li>';
			}else{
				htmlContent += '<li><a href="#">'+menuArr[i][field]+'</a></li>';
			}
		}
		$(container).html(htmlContent);
	}
    /** 绑定事件 **/
	function initEventHandler(){
		// 左侧菜单
		initialLeftMenuEvent();
		initialContentMenuEvent();
	}
	function initialLeftMenuEvent(){
		$("#left-nav li").click(function(){
			$("#left-nav li").removeClass("active");
			$(this).addClass("active");
			leftNavIndex = $(this).index();
			var leftMenu = config.menu_left[leftNavIndex];
			initContentMenu(leftMenu.children);
			initialContentMenuEvent();
			mainNavIndex = 0;
			showPage();
		});
	}
	
	function initialContentMenuEvent(){
		// 中间部分
		$("#main-nav li").click(function(event){
			$("#main-nav li").removeClass("active");
			$(this).addClass("active");
			mainNavIndex = $(this).index();
			showPage();
		});
	}
	function initContentMenu(arr){
		if(arr){
			currContentMenu = arr;
		}else{
			currContentMenu = config.default_menu_content;
		}
		initMenuHtml("#main-nav", currContentMenu);
	}
	function showPage(){
		// 获取展示页面的地址
		var fatherUrl = config.baseUrl +config.menu_left[leftNavIndex].url;
		var url = fatherUrl +currContentMenu[mainNavIndex].url;
		if(url.indexOf(".html")> -1){
			$('#main-content').html(iflytek.utils.getIframeHtml(url));
		}else{
			iflytek.utils.load(url, function(response){
				$('#main-content').html(response);
			});
		}
	}

})(jQuery);
