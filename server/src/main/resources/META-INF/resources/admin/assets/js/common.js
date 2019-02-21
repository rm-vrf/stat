var ONE_KB = 1024;
var ONE_MB = ONE_KB * ONE_KB;
var ONE_GB = ONE_KB * ONE_MB;
var ONE_TB = ONE_KB * ONE_GB;
var ONE_PB = ONE_KB * ONE_TB;
var ONE_EB = ONE_KB * ONE_PB;

function byteCountToDisplaySize(size) {
    var displaySize;
    if (size / ONE_EB > 1) {
        displaySize = parseInt(size / ONE_EB) + " EB";
    } else if (size / ONE_PB > 1) {
        displaySize = parseInt(size / ONE_PB) + " PB";
    } else if (size / ONE_TB > 1) {
        displaySize = parseInt(size / ONE_TB) + " TB";
    } else if (size / ONE_GB > 1) {
        displaySize = parseInt(size / ONE_GB) + " GB";
    } else if (size / ONE_MB > 1) {
        displaySize = parseInt(size / ONE_MB) + " MB";
    } else if (size / ONE_KB > 1) {
        displaySize = parseInt(size / ONE_KB) + " KB";
    } else {
        displaySize = parseInt(size) + " bytes";
    }
    return displaySize;
}

function getParameter(name) {
    var vars = [], hash;
    var hashes = window.location.href.slice(window.location.href.indexOf('?') + 1).split('&');
    for(var i = 0; i < hashes.length; i++) {
        hash = hashes[i].split('=');
        vars.push(hash[0]);
        vars[hash[0]] = hash[1];
    }
    return vars;
}

var topbar = new Vue({
	el : '#topbar-collapse',
	data : {
		messageCount: ''
	},
	methods: {
		refresh: function() {
			Vue.http.get('/v1/event/count').then(function(ret) {
				var count = ret.body;
				topbar.messageCount = count == 0 ? '' : count > 1024 ? count + '+' : count;
			});
		}
	}
});

$(document).ready(function () {
	topbar.refresh();
	setInterval(function() {
		topbar.refresh();
	}, 5000);
});
