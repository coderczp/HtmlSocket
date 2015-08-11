/*******************************************************************************
 * this is long polling and websocket js<br>
 * author:coder_czp<br>
 * date:2015/8/8 <br>
 * copyright:coder_czp 2015
 ******************************************************************************/

function HtmlSockect(settings) {
	"use strict";

	var _sockect;
	var _set = settings;

	this.listen = function() {
		_sockect.listen();
	}
	this.sendTo = function(targetId, msg) {
		if (!targetId || !msg) {
			alert('targetId msg is required');
		} else {
			var message = settings.encode(msg);
			if (typeof (message) == "object") {
				message.to = targetId;
				message.from = _set.id;
				_sockect.write(JSON.stringify(message));
				return;
			}
			alert('function encode must be return JSON');
		}
	}
	this.broadcastGroup = function(subject, msg) {
		if (!subject || !msg) {
			alert('subject msg is required');
			return;
		}
		var message = settings.encode(msg);
		if (typeof (message) == "object") {
			message.subject = subject;
			message.from = _set.id;
			_sockect.write(JSON.stringify(message));
			return;
		}
		alert('function encode must be return JSON');

	}
	this.close = function() {
		_sockect.shutdown();
	}

	function send(message) {
		_sockect.write(message);
	}

	if (!_set || !_set.id || !_set.topic || !_set.onmessage) {
		alert("Id topic onmessage is required");
	} else {
		_sockect = HtmlSockect.createSocket(_set);
	}
}

HtmlSockect.Settings = function(url, id, topic) {

	this.id = id;
	this.url = url;
	this.topic = topic;

	this.onopen = function(msg) {
		var str = JSON.stringify(msg);
		if (window.console) {
			console.log(str);
		} else {
			alert(str);
		}
	}
	this.decode = function(data) {
		return data;
	}
	this.encode = this.decode;
	this.onerror = this.onopen;
	this.onclose = this.onopen;
	this.onmessage = this.onopen;

}
/** Sockect factory */
HtmlSockect.createSocket = function(set) {

	if (window.WebSocket)
		return WebSock(set);
	else
		return LongConnect(set);

	function LongConnect(set) {

		var param = "topic=" + set.topic + "&id=" + set.id;
		var realUrl = set.url + 'poll/?' + param;
		var ajax = new Ajax();

		function doResult(data) {
			try {
				if (!data)
					return;
				/* server error */
				if (true == data._serr || /_serr/.test(data)) {
					set.onerror(data);
				} else {
					set.onmessage(set.decode(data));
				}
			} catch (e) {
				set.onerror(e);
			}
		}
		this.listen = function() {
			ajax.post({
				url : realUrl,
				dataType : 'jsonp',
				success : function(data) {
					doResult(data);
					listen();
				},
				error : function(req, text, err) {
					set.onerror(err);
					listen();
				}
			});
		}
		this.write = function(message) {
			ajax.post({
				success : function(data) {
					alert(data);
				},
				url : realUrl + '&hold=false',
				data : {
					'data' : message
				},
				error : set.onerror,
				dataType : 'jsonp'
			});
		}
		this.shutdown = set.onclose;
		return this;
	}

	function WebSock(set) {

		this.listen = function() {
			var param = "topic=" + set.topic + "&id=" + set.id;
			var url = set.url.replace("http", "ws");
			var realUrl = url + 'ws/?' + param;
			var ws = new WebSocket(realUrl);
			ws.binaryType = "arraybuffer";
			ws.onerror = set.onerror;
			ws.onopen = set.onopen;
			var server_error = 1011;
			ws.onclose = function(evt) {
				var reason = evt.reason;
				if (server_error == evt.code) {
					/* server close connect */
					set.onerror(reason);
					return;
				}
				/* reconn if not close by user */
				if ('user close' != evt) {
					this._sock = null;
					listen();
				} else {
					this._sock.onclose = null;
					this._sock.close();
				}
			};
			ws.onmessage = function(evt) {
				try {
					set.onmessage(set.decode(evt.data));
				} catch (e) {
					set.onerror(e);
				}
			};
			this._sock = ws;
		}
		this.shutdown = function() {
			_sock.close('user close');
			set.onclose();
		}
		this.write = function(msg) {
			_sock.send(msg);
		}
		return this;
	}
}
/** *********flow is Ajax*********************** */
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
	_ajax = function() {
		var xmlHttp;
		if (window.XMLHttpRequest) {
			xmlHttp = new XMLHttpRequest();
			/* cors can cross domain but can't open 2+ connect */
			xmlHttp._cors = false;
			return xmlHttp;
		}
		var names = [ "MSXML2.XMLHTTP", "Microsoft.XMLHTTP" ];
		for (var i = 0; i < names.length; i++) {
			if (xmlHttp = new ActiveXObject(names[i]))
				return xmlHttp;
		}
		var err = "sorry,your browser does not support ajax!";
		alert(err);
		throw err;
	}();
}
/** *******************json2.js support for IE 6/7/8************** */
if (typeof JSON !== 'object') {
	JSON = {}
}
(function() {
	'use strict';
	var rx_one = /^[\],:{}\s]*$/, rx_two = /\\(?:["\\\/bfnrt]|u[0-9a-fA-F]{4})/g;
	var rx_three = /"[^"\\\n\r]*"|true|false|null|-?\d+(?:\.\d*)?(?:[eE][+\-]?\d+)?/g;
	var rx_four = /(?:^|:|,)(?:\s*\[)+/g, rx_escapable = /[\\\"\u0000-\u001f\u007f-\u009f\u00ad\u0600-\u0604\u070f\u17b4\u17b5\u200c-\u200f\u2028-\u202f\u2060-\u206f\ufeff\ufff0-\uffff]/g;
	var rx_dangerous = /[\u0000\u00ad\u0600-\u0604\u070f\u17b4\u17b5\u200c-\u200f\u2028-\u202f\u2060-\u206f\ufeff\ufff0-\uffff]/g;
	function f(n) {
		return n < 10 ? '0' + n : n
	}
	function this_value() {
		return this.valueOf()
	}
	if (typeof Date.prototype.toJSON !== 'function') {
		Date.prototype.toJSON = function() {
			return isFinite(this.valueOf()) ? this.getUTCFullYear() + '-'
					+ f(this.getUTCMonth() + 1) + '-' + f(this.getUTCDate())
					+ 'T' + f(this.getUTCHours()) + ':'
					+ f(this.getUTCMinutes()) + ':' + f(this.getUTCSeconds())
					+ 'Z' : null
		};
		Boolean.prototype.toJSON = this_value;
		Number.prototype.toJSON = this_value;
		String.prototype.toJSON = this_value
	}
	var gap, indent, meta, rep;
	function quote(string) {
		rx_escapable.lastIndex = 0;
		return rx_escapable.test(string) ? '"'
				+ string.replace(rx_escapable, function(a) {
					var c = meta[a];
					return typeof c === 'string' ? c : '\\u'
							+ ('0000' + a.charCodeAt(0).toString(16)).slice(-4)
				}) + '"' : '"' + string + '"'
	}
	function str(key, holder) {
		var i, k, v, length, mind = gap, partial, value = holder[key];
		if (value && typeof value === 'object'
				&& typeof value.toJSON === 'function') {
			value = value.toJSON(key)
		}
		if (typeof rep === 'function') {
			value = rep.call(holder, key, value)
		}
		switch (typeof value) {
		case 'string':
			return quote(value);
		case 'number':
			return isFinite(value) ? String(value) : 'null';
		case 'boolean':
		case 'null':
			return String(value);
		case 'object':
			if (!value) {
				return 'null'
			}
			gap += indent;
			partial = [];
			if (Object.prototype.toString.apply(value) === '[object Array]') {
				length = value.length;
				for (i = 0; i < length; i += 1) {
					partial[i] = str(i, value) || 'null'
				}
				v = partial.length === 0 ? '[]' : gap ? '[\n' + gap
						+ partial.join(',\n' + gap) + '\n' + mind + ']' : '['
						+ partial.join(',') + ']';
				gap = mind;
				return v
			}
			if (rep && typeof rep === 'object') {
				length = rep.length;
				for (i = 0; i < length; i += 1) {
					if (typeof rep[i] === 'string') {
						k = rep[i];
						v = str(k, value);
						if (v) {
							partial.push(quote(k) + (gap ? ': ' : ':') + v)
						}
					}
				}
			} else {
				for (k in value) {
					if (Object.prototype.hasOwnProperty.call(value, k)) {
						v = str(k, value);
						if (v) {
							partial.push(quote(k) + (gap ? ': ' : ':') + v)
						}
					}
				}
			}
			v = partial.length === 0 ? '{}' : gap ? '{\n' + gap
					+ partial.join(',\n' + gap) + '\n' + mind + '}' : '{'
					+ partial.join(',') + '}';
			gap = mind;
			return v
		}
	}
	if (typeof JSON.stringify !== 'function') {
		meta = {
			'\b' : '\\b',
			'\t' : '\\t',
			'\n' : '\\n',
			'\f' : '\\f',
			'\r' : '\\r',
			'"' : '\\"',
			'\\' : '\\\\'
		};
		JSON.stringify = function(value, replacer, space) {
			var i;
			gap = '';
			indent = '';
			if (typeof space === 'number') {
				for (i = 0; i < space; i += 1) {
					indent += ' '
				}
			} else if (typeof space === 'string') {
				indent = space
			}
			rep = replacer;
			if (replacer
					&& typeof replacer !== 'function'
					&& (typeof replacer !== 'object' || typeof replacer.length !== 'number')) {
				throw new Error('JSON.stringify');
			}
			return str('', {
				'' : value
			})
		}
	}
	if (typeof JSON.parse !== 'function') {
		JSON.parse = function(text, reviver) {
			var j;
			function walk(holder, key) {
				var k, v, value = holder[key];
				if (value && typeof value === 'object') {
					for (k in value) {
						if (Object.prototype.hasOwnProperty.call(value, k)) {
							v = walk(value, k);
							if (v !== undefined) {
								value[k] = v
							} else {
								delete value[k]
							}
						}
					}
				}
				return reviver.call(holder, key, value)
			}
			text = String(text);
			rx_dangerous.lastIndex = 0;
			if (rx_dangerous.test(text)) {
				text = text.replace(rx_dangerous, function(a) {
					return '\\u'
							+ ('0000' + a.charCodeAt(0).toString(16)).slice(-4)
				})
			}
			if (rx_one.test(text.replace(rx_two, '@').replace(rx_three, ']')
					.replace(rx_four, ''))) {
				j = eval('(' + text + ')');
				return typeof reviver === 'function' ? walk({
					'' : j
				}, '') : j
			}
			throw new SyntaxError('JSON.parse');
		}
	}
}());