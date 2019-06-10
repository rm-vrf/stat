
function requestParameter(name) {
    var vars = [], hash;
    var hashes = window.location.href.slice(window.location.href.indexOf('?') + 1).split('&');
    for(var i = 0; i < hashes.length; i++) {
        hash = hashes[i].split('=');
        vars.push(hash[0]);
        vars[hash[0]] = hash[1];
    }
    return vars;
};

function byteCountToDisplaySize(size) {
	var ONE_KB = 1024;
	var ONE_MB = ONE_KB * ONE_KB;
	var ONE_GB = ONE_KB * ONE_MB;
	var ONE_TB = ONE_KB * ONE_GB;
	var ONE_PB = ONE_KB * ONE_TB;
	var ONE_EB = ONE_KB * ONE_PB;
	var displaySize;
	if (size >= ONE_EB) {
		displaySize = parseInt(size / ONE_EB) + " EB";
	} else if (size >= ONE_PB) {
		displaySize = parseInt(size / ONE_PB) + " PB";
	} else if (size >= ONE_TB) {
		displaySize = parseInt(size / ONE_TB) + " TB";
	} else if (size >= ONE_GB) {
		displaySize = parseInt(size / ONE_GB) + " GB";
	} else if (size >= ONE_MB) {
		displaySize = parseInt(size / ONE_MB) + " MB";
	} else if (size >= ONE_KB) {
		displaySize = parseInt(size / ONE_KB) + " KB";
	} else {
		displaySize = parseInt(size) + " bytes";
	}
	return displaySize;
};

$(document).ready(function() {
	function refreshMessageCount() {
		$.get('/v1/event/_count', function(count, status, xhr) {
			if (count > 0) {
				$('#message_count').text(count >= 1024 ? '1024+' : count);
				$('#message_count').show();
			} else {
				$('#message_count').hide();
			}
				
		});
	}
	refreshMessageCount();
	setInterval(function() {
		refreshMessageCount();
	}, 5000);
});
