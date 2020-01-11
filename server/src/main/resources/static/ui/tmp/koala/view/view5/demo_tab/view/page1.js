define(function (){
　var init = function(item, options){
	    console.log("call page1.js, the data is " + JSON.stringify(item));
		item.changeHandler("you changed me");
	}
　return {
　　init: init
　};
});　　