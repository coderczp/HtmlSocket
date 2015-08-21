/*******************************************************************************
 * this is long polling and websocket js<br>
 * author:coder_czp<br>
 * date:2015/8/8 <br>
 * copyright:coder_czp 2015
 ******************************************************************************/

function HtmlSockect(settings) {
	"use strict";

	if (!settings || !settings.id || !settings.topic || !settings.onmessage) {
		alert("Id topic onmessage is required");
		return;
	}

	this.settings = settings;
	this.sockect = HtmlSockect.createSocket(settings);

	this.listen = function() {
		this.sockect.listen();
	}
	this.close = function() {
		this.sockect.shutdown();
	}

	function send(message) {
		this.sockect.write(message);
	}
}

String.fmt = function() {
	if (arguments.length == 0)
		return null;
	var format = arguments[0];
	var size = arguments.length;
	for (var i = 1; i < size; i++) {
		format = format.replace("%s", arguments[i]);
	}
	return format;
}

HtmlSockect.prototype.sendTo = function(toId, message) {
	if (!toId || !message) {
		alert('toId msg is required');
		return;
	}
	var set = this.settings;
	var msg = set.encode(message);
	if (typeof (msg) != "string")
		return alert('function encode must be return string');

	var fmt = '{"to":%s,"from":%s,"data":"%s"}';
	this.sockect.write(String.fmt(fmt, toId, set.id, msg));
}

HtmlSockect.prototype.broadcastGroup = function(subject, message) {
	if (!subject || !message) {
		alert('subject message is required');
		return;
	}
	var set = this.settings;
	var msg = set.encode(message);
	if (typeof (msg) != "string")
		return alert('function encode must be return string');

	var fmt = '{"subject":%s,"from":%s,"data":"%s"}';
	this.sockect.write(String.fmt(fmt, subject, set.id, msg));
}

HtmlSockect.Settings = function(url, id, topic) {

	this.id = id;
	this.url = url;
	this.topic = topic;

	this.onopen = function(msg) {
		if (window.console) {
			console.log(msg);
		} else {
			alert(msg);
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
	if (window.xWebSocket)
		return new WebSock(set);
	else
		return new LongPolling(set);
}
function handleResult(data, set) {
	try {
		if (true == data._serr || /_serr/.test(data))
			return !handleError(data);
		set.onmessage(set.decode(data));
	} catch (e) {
		alert('call onmessage error:' + data)
	}
	return true;
}

/* 返回false,终止重连 返回true,继续重连 */
function handleError(err, set) {
	try {
		if ('user close' == err)
			return set.onclose(err) && false;

		if (err.code == 1011 || /1011/.test(err))
			return set.onclose(err.reason||err) && false;

		return set.onerror(err) || true;
	} catch (e) {
		alert('call onerror err:' + err);
		return true;
	}
}

function LongPolling(set) {
	var param = "topic=" + set.topic + "&id=" + set.id;
	this.realUrl = set.url + 'poll/?' + param;
	this.shutdown = set.onclose;
	this.ajax = new Ajax();
	this.set = set;
}

LongPolling.prototype.listen = function() {
	var that = this;
	this.ajax.post({
		url : that.realUrl,
		dataType : 'jsonp',
		success : function(data) {
			if (handleResult(data, that.set))
				that.listen();
		},
		error : function(req, text, err) {
			that.set.onerror(err, that.set);
			that.listen();
		}
	});
}
LongPolling.prototype.write = function(message) {
	var that = this;
	this.ajax.post({
		url : that.realUrl + '&hold=false',
		success : handleResult,
		data : {
			'data' : message
		},
		error : that.set.onerror,
		dataType : 'jsonp'
	});
}

function WebSock(set) {
	this.set = set;
	this.shutdown = function() {
		this.sock.close('user close');
		this.set.onclose();
	}
	this.write = function(msg) {
		this.sock.send(msg);
	}
}
WebSock.prototype.listen = function() {
	var that = this;
	var param = "topic=" + that.set.topic + "&id=" + that.set.id;
	var url = that.set.url.replace("http", "ws");
	var ws = new WebSocket(url + 'ws/?' + param);
	var server_error = 1011;
	var proto_error = 1002;

	ws.binaryType = "arraybuffer";
	ws.onerror = this.set.onerror;
	ws.onopen = this.set.onopen;
	ws.onclose = function(evt) {
		if (handleError(evt, that.set)) {
			/* reconn if exception */
			this.onclose = null;
			this.close();
			that.sock = null;
			that.listen();
		}/* else proto_error or server_error don't reconn */
	};

	ws.onmessage = function(evt) {
		handleResult(evt.data, that.set);
	};
	that.sock = ws;
}
