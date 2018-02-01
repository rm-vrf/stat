$(document).ready(function () {
    $.get('/v1/node', function(node, status, xhr) {
        $('#hostname').text(node.hostname);
        $('#address').text(get_hostname(node, true));
        var os = node.os;
        if (os) {
            $('#cpucore').text(os.cpu);
        }
        var disk_total = 0;
        $.each(node.disks, function(i, disk) {
            disk_total += disk.total / 1024 / 1024 / 1024;
        });
        $('#diskspace').text(parseInt(disk_total));
        var memory = node.memory;
        if (memory) {
            memory = memory.total / 1024 / 1024 / 1024;
            $('#memory').text(parseInt(memory));
        } else {
            $('#memory').text(0);
        }
    });
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
