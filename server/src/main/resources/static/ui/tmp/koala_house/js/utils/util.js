function uuid() {
  var i, random;
  var result = '';

  for (i = 0; i < 32; i++) {
    random = Math.random() * 16 | 0;
    if (i === 8 || i === 12 || i === 16 || i === 20) {
      result += '-';
    }
    result += (i === 12 ? 4 : (i === 16 ? (random & 3 | 8) : random))
      .toString(16);
  }

  return result;
}
function initStyleColorHtml(target, onSelectHandler){
			 target.simpleColor({
					boxWidth: 16,
			    boxHeight: 16,
			    cellWidth: 16,
			    cellHeight: 16,
			    chooserCSS: { 'border': '1px solid #777', 'width': '272px' },
			    displayCSS: { 'border': '1px solid #ddd' },
			    displayColorCode: false,
			    livePreview: true,
			    onSelect:onSelectHandler
			  });
}
function changeStyle(targetObj, element, value){
		var prop = element.attr("style-attr");
		if(value != undefined){
			targetObj[prop] = value;
		}else{
			targetObj[prop] = element.val();
		}
}
function initSelect(targets, data){
			var htmlContent = "";
			for(var i = 0; i< data.length; i++){
			var item = data[i];
				htmlContent += '<option value ="'+item.value+'">'+item.label+'</option>';
			}
			targets.each(function(){
				$(this).html(htmlContent);
			});
}
function copy(target){
	if(target){
		return JSON.parse(JSON.stringify(target));
	}
}
