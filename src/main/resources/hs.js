j_import("./json2.js");
j_import("./ajax.js");

function j_import(js_url) {
	var id = js_url.replace(/[^\w]/g, "");
	if (document.getElementById(id) == null) {
		var xmlHttp = initAjaxObj();
		xmlHttp.open("GET", js_url, false);
		xmlHttp.send(null);
		if (xmlHttp.readyState == 4) {
			/* 0为访问的本地，200到300代表访问服务器成功，304代表没做修改访问的是缓存 */
			if ((xmlHttp.status >= 200 && xmlHttp.status < 300)
					|| xmlHttp.status == 0 || xmlHttp.status == 304) {
				var myHead = document.getElementsByTagName("HEAD").item(0);
				var script = document.createElement("script");
				script.language = "javascript";
				script.type = "text/javascript";
				script.id = id;
				try {
					/* IE8以及以下不支持这种方式，需要通过text属性来设置 */
					var txt = document.createTextNode(xmlHttp.responseText);
					script.appendChild(txt);
				} catch (ex) {
					script.text = xmlHttp.responseText;
				}
				myHead.appendChild(script);
				return true;
			}
		}
	}
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
	return window._ajax;
}