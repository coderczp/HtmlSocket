/*******************************************************************************
 * this is ajax js
 ******************************************************************************/
function Ajax() {
	"use strict";
	var _ajax;

	/* encodeURIComponent(key) */
	function joinData(data) {
		var str = "";
		for ( var k in data) {
			str += (k + '=' + data[k] + '&');
		}
		return str;
	}

	function processResult(ajax, cfg) {
		var result = ajax.responseText;
		if ("json" == ajax.responseType) {
			result = JSON.parse(result);
		}
		if (ajax.status == 200) {
			cfg.success(result);
		} else if (cfg.error) {
			cfg.error(ajax, ajax.statusText, ajax.response);
		}
		if (cfg.complete)
			cfg.complete(ajax);
	}

	function preProcessArgs(cfg, success) {
		if (!cfg || !cfg.url)
			throw 'please set ajax param[url is required]';

		var now = new Date().getTime();
		var url = cfg.url.replace(/#.*$/, "");
		var end = (/\?/.test(url) ? "&" : "?");
		var turl = url + end + '_=' + now;

		cfg.url = turl;
		cfg.data = cfg.data || {};
		cfg.async = cfg.async || true;
		cfg.method = cfg.method || 'post';
		cfg.success = success || cfg.success;
		return cfg;
	}
	this.post = function(jsoncfg, success) {
		jsoncfg.method = 'post';
		this.ajax(jsoncfg, success);
	}

	this.get = function(jsoncfg, success) {
		jsoncfg.method = 'get';
		if (jsoncfg.data && jsoncfg.data != {})
			throw "can't set data when use get,please append to url";
		this.ajax(jsoncfg, success);
	}

	this.ajax = function(jsoncfg, success) {
		var cfg = preProcessArgs(jsoncfg, success);
		if ("jsonp" == cfg.dataType && (!_ajax._cors)) {
			jsonp(cfg.url, cfg.data, cfg.error, cfg.success);
			return;
		} else if ("get" == cfg.method) {
			_ajax.open(cfg.method, cfg.url, cfg.async);
		} else {
			var cType = "application/x-www-form-urlencoded";
			_ajax.open(cfg.method, cfg.url, cfg.async);
			_ajax.setRequestHeader("Content-type", cType);
		}
		if (cfg.async) {
			_ajax.onreadystatechange = function() {
				if (_ajax.readyState == 4)
					processResult(_ajax, cfg);
			}
		}
		_ajax.send(joinData(cfg.data));
		if (cfg.async == false)
			processResult(_ajax, cfg);
	}

	function jsonp(url, params, error, callback) {

		var jurl = url + (/\?/.test(url) ? "&" : "?");
		var jsonp = "_json" + (new Date().getTime());
		var script = document.createElement('script');
		var head = document.getElementsByTagName('head')[0];
		script.src = jurl + joinData(params) + "cors=jsonp&callback=" + jsonp;
		script.onload = script.onreadystatechange = function() {
			if (!script.readyState || /loaded|complete/.test(script.readyState)) {
				script.onload = script.onreadystatechange = null;
				if (script && script.parentNode) {
					script.parentNode.removeChild(script);
				}
			}
		}
		script.onerror = function(err) {
			error(null, null, 'jsonp load error');
		}
		script.async = true;
		window[jsonp] = function(data) {
			script.onerror = null;
			window[jsonp] = null;
			callback(data);
		};
		head.appendChild(script);
	}
	_ajax = initAjaxObj();
}

function initAjaxObj() {

	if (typeof (window._ajax) === 'object')
		return window._ajax;

	if (window.XMLHttpRequest)
		window._ajax = new XMLHttpRequest();
	else
		window._ajax = new ActiveXObject("MSXML2.XMLHTTP")
				|| new ActiveXObject("Microsoft.XMLHTTP");

	if (!window._ajax) {
		alert("sorry,your browser does not support ajax!");
		throw "sorry,your browser does not support ajax!"
	}
	window._ajax._cors = false;
	return window._ajax;
}