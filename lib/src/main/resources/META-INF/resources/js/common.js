$(document).ready(function () {
});

function get_hostname(node, prefer_ip_address) {
    var hostname = '';
    if (!prefer_ip_address) {
        hostname = node.hostname;
    } else {
        $.each(node.networks, function(i, network) {
            if (network.siteLocal) {
                hostname = network.address;
            }
        });
    }
    
    if (hostname == '') {
        hostname = node.hostname;
    }
    return hostname;
}

function get_param(name) {
    var vars = [], hash;
    var hashes = window.location.href.slice(window.location.href.indexOf('?') + 1).split('&');
    for(var i = 0; i < hashes.length; i++) {
        hash = hashes[i].split('=');
        vars.push(hash[0]);
        vars[hash[0]] = hash[1];
    }
    return vars;
};
