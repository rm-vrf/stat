function get_hostname(node) {
    var hostname = node.hostname;
    var add = node.agentAddress;
    var ary = add.split(':');
    if (ary && ary.length > 1) {
        hostname = ary[1].replace('//', '');
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
