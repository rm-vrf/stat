(function($){
	iflytek = window.iflytek || {};
	iflytek.menu = iflytek.menu || function(){
		this.keyField = "id";
		this.labelField = "name";
		this.urlField = "url";
		this.className = "iflytek_menu_top";
		this.itemClick = function(item){
			console.log("item=" + JSON.stringify(item));
		}
	};
	iflytek.menu.prototype.init = function(options){
		$.extend(this, options);
		this.$container = $(this.container);
		this.setClassNameByType(this.type);
		var html = "<ul class='iflytek_menu "+this.className+"'>";
		for(var i = 0; i< this.data.length; i++){
			html += this.initItemHtml(this.data[i]);
		}
		html += "</ul>";
		this.$container.html(html);
		
		this.initEventHandler(this.data);
	}
	iflytek.menu.prototype.setClassNameByType = function(type){
		switch(type){
			case "top":
			this.className = "iflytek_menu_top";
			break;
			case "left":
			this.className = "iflytek_menu_left";
			break;
		}
	}
	iflytek.menu.prototype.initItemHtml = function(item){
		var html = "<li  data_id=\""+item[this.keyField]+"\" ><a  href=\""+item[this.urlField]+"\">"+item[this.labelField]+"</a>";
		if(item.children){
			html += "<ul>";
			for(var i = 0; i< item.children.length; i++){
			   	html += this.initItemHtml(item.children[i]);
			}
			html += "</ul>";
		}	 
		html += "</li>";
		return html;
	}
	iflytek.menu.prototype.initEventHandler = function(data){
		   
	}
	iflytek.menu.prototype.traverseToFindItem = function(arr, id){
			for(var i = 0; i<arr.length; i++){
				var item = arr[i];
				if(item[this.keyField] == id){
					return  item;
				}else{
					if(item.children){
						var childResult = this.traverseToFindItem(item.children, id);
						if(childResult){
							return childResult;
						}
					}
				}
			}
			return null;
		}
	
	/**
	 * 顶部菜单类， 继承iflytek.menu菜单类
	 */
	iflytek.menu_top = iflytek.menu_top || function(){
		iflytek.menu.call(this);
		this.type = "top";
	}
	// 继承
	iflytek.utils.extends(iflytek.menu, iflytek.menu_top);
	iflytek.menu_top.prototype.initEventHandler = function(data){
		   var _self = this;
			this.$container.find("li").mouseover(function(){
				 $(this).children("ul").css("display", "block");
			}).mouseout(function(){
				$(this).children("ul").css("display", "none");
			}).click(function(event){
				event.stopImmediatePropagation();
				var id = $(this).attr("data_id");
				var currItem = _self.traverseToFindItem(_self.data, id);
				if(_self.itemClick instanceof Function){
					_self.itemClick(currItem);
				}
			});
		}
	/**
	 * 顶部菜单类， 继承iflytek.menu菜单类
	 */
	iflytek.menu_left = iflytek.menu_left || function(){
		iflytek.menu.call(this);
		this.type = "left";
	}
	// 继承
	iflytek.utils.extends(iflytek.menu, iflytek.menu_left);
	iflytek.menu_left.prototype.initEventHandler = function(data){
		   var _self = this;
			this.$container.find("li").click(function(event){
				event.stopImmediatePropagation();
				var id = $(this).attr("data_id");
				// 同级节点隐藏， 当前的显示或隐藏
				var parent = $(this).parent();
				$(parent).find("li").each(function(){
					if($(this).attr("data_id") != id){
						$(this).children("ul").hide();
					}else{
						$(this).children("ul").toggle();
					}
				});
				// 抛出点击事件
				var currItem = _self.traverseToFindItem(_self.data, id);
				if(_self.itemClick instanceof Function){
					_self.itemClick(currItem);
				}
			});
		}
	// 构造器
	iflytek.generator = iflytek.generator || {};
	iflytek.generator.menu = iflytek.generator.menu || function(type, options){
		var menu; 
		switch(type){
			case "top":
				menu = new iflytek.menu_top();
				break;
			case "left":
				menu = new iflytek.menu_left();
				break;
		}
		menu.init(options);
	};
})(jQuery);
