(function($){
	iflytek = window.iflytek || {};
	iflytek.utils = iflytek.utils || {};
	/**
	 * 继承
	 * #示例
	 * var Chinese = {nation:'中国'};
	 * var Doctor ={career:'医生'};
	 * var Doctor = iflytek.utils.extends(Chinese);
	 * alert(Doctor.nation); //中国
	 */
	iflytek.utils.extends = function(o){
		function F() {}
　　　　F.prototype = o;
　　　　return new F();
	};
	/**
	 * 浅拷贝
	 */
	iflytek.utils.extendCopy = function(p) {
　　　　var c = {};
　　　　for (var i in p) { 
　　　　　　c[i] = p[i];
　　　　}
　　　　c.uber = p;
　　　　return c;
　};
	/**
	 * 深拷贝
	 */
    iflytek.utils.deepCopy = function(p, c) {
　　　　var c = c || {};
　　　　for (var i in p) {
　　　　　　if (typeof p[i] === 'object') {
　　　　　　　　c[i] = (p[i].constructor === Array) ? [] : {};
　　　　　　　　deepCopy(p[i], c[i]);
　　　　　　} else {
　　　　　　　　　c[i] = p[i];
　　　　　　}
　　　　}
　　　　return c;
　};
	iflytek.utils.extends = function(fatherClass, childClass){
		// 创建一个没有实例方法的类
		var Super = function(){};
		Super.prototype = fatherClass.prototype;
		//将实例作为子类的原型
		childClass.prototype = new Super();
	};
	iflytek.utils.load =	function(url, callback){
		return $.ajax({
			url: url, 
			type: "GET",
			success: function(data){	
				if(callback instanceof Function){
					var htmlText = "";
					if(url.indexOf(".html") > -1){
						htmlText = iflytek.utils.getHtmlBody(data);
					}else if(url.indexOf(".md") > -1){
						htmlText = iflytek.utils.getMarkDownContent(data);
					}
					callback(htmlText);
				}
			},
			error: function(e){
				callback("<h3>Error: No html exit！</h3>");
			}
		});
	}
	iflytek.utils.getIframeHtml = function(url){
		var html = "<iframe src=\""+url+"\" width=\"100%\" height=\"100%\"></iframe>";
		return html;
	}
	iflytek.utils.getHtmlBody = function(htmlText){
		var result = "";
		var re =/<body>[\w\W]*<\/body>/;
		var result=  re.exec(htmlText); 
		if(result && result.length > 0){
			var bodyText = result[0];
			result = bodyText.slice(6, bodyText.length-7);
		}
		return result.trim();
	}
	iflytek.utils.getMarkDownContent = function(md){
		var converter = new showdown.Converter({extensions: function() {
		  function htmlunencode(text) {
		    return (
		      text
		        .replace(/&amp;/g, '&')
		        .replace(/&lt;/g, '<')
		        .replace(/&gt;/g, '>')
		      );
		  }
		  return [
		    {
		      type: 'output',
		      filter: function (text, converter, options) {
		        // use new shodown's regexp engine to conditionally parse codeblocks
		        var left  = '<pre><code\\b[^>]*>',
		            right = '</code></pre>',
		            flags = 'g',
		            replacement = function (wholeMatch, match, left, right) {
		              // unescape match to prevent double escaping
		              match = htmlunencode(match);
		              return left + hljs.highlightAuto(match).value + right;
		            };
		        return showdown.helper.replaceRecursiveRegExp(text, replacement, left, right, flags);
		      }
		    }
		  ];
		}()});
		return converter.makeHtml(md);
	}
})(jQuery);
