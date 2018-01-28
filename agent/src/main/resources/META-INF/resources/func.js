
function _get_param(name) {
    var vars = [], hash;
    var hashes = window.location.href.slice(window.location.href.indexOf('?') + 1).split('&');
    for(var i = 0; i < hashes.length; i++) {
        hash = hashes[i].split('=');
        vars.push(hash[0]);
        vars[hash[0]] = hash[1];
    }
    return vars;
};

function _get_app_list(callback) {
    $.get('/v1/app', callback);
}

function _get_app(name, callback) {
    $.get('/v1/app/' + name, callback);
}

function _stop_app(name, callback) {
    $.post('/v1/app/' + name + '/_stop', callback);
}

function _start_app(name, callback) {
    $.post('/v1/app/' + name + '/_start', callback);
}

function _put_scale(app, num, callback) {
    var url = '/v1/app/' + app + '/_scale?num=' + num;
    $.post(url, callback);
}

function _get_proc_list(callback) {
    $.get('/v1/proc', callback);
}

function _get_app_proc_list(app, callback) {
    var url = '/v1/app/' + app + '/proc';
    $.get(url, callback);
}

function _kill_proc(pid, callback) {
    var url = '/v1/proc/' + pid + '/_kill';
    $.post(url, callback);
}

