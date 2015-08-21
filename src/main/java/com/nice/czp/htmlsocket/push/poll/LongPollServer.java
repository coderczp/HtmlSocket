package com.nice.czp.htmlsocket.push.poll;

import java.io.IOException;
import java.io.Writer;
import java.util.concurrent.TimeUnit;

import org.glassfish.grizzly.EmptyCompletionHandler;
import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;
import org.glassfish.grizzly.http.server.TimeoutHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nice.czp.htmlsocket.api.IMessage;
import com.nice.czp.htmlsocket.api.IRemoteSubcriber;
import com.nice.czp.htmlsocket.api.PushError;
import com.nice.czp.htmlsocket.push.PushContext;

/**
 * comet长轮询接入类
 * 
 * @author coder_czp@126.com-2015年8月15日
 * 
 */
public class LongPollServer extends HttpHandler implements TimeoutHandler{

	private EmptyCompletionHandler<Response> handler = new EmptyCompletionHandler<Response>();
	private static Logger logger = LoggerFactory.getLogger(LongPollServer.class);
	private PushContext context;

	public LongPollServer(PushContext wsContext) {
		this.context = wsContext;
	}

	@Override
	public void service(Request request, Response response) throws Exception {
		try {
			int port = request.getRemotePort();
			String address = request.getRemoteAddr();
			String holdConn = request.getParameter("hold");

			if ("false".equals(holdConn)) {
				logger.info("recv short request:[{}:{}]", address, port);
				String messageJson = request.getParameter("data");
				IMessage msg = context.getCodec().decode(messageJson);
				context.getMessageCenter().send(msg);
			} else {
				logger.info("recv long-poll request:[{}:{}]", address, port);
				PushError erro = context.beAddSub(request.getParameterMap());
				if (erro != null) {
					writeInfo(response, erro);
					return;
				}
				long timeout = context.getConfig().getConnTimeout();
				IRemoteSubcriber sub = new PollSubcriber(response, context);
				response.suspend(timeout, TimeUnit.MILLISECONDS, handler, this);
				context.getMessageCenter().addSubscriber(sub);
				request.setAttribute("sub", sub);
				
				logger.info("create long-poll client:[{}:{}]", address, port);
			}
		} catch (Exception e) {
			logger.error("occur error when process request", e);
		}
	}

	@Override
	public boolean onTimeout(Response response) {
		if (response.isSuspended()) {
			response.resume();
		}
		writeInfo(response, PushError.TIMEOUT);
		Request req = response.getRequest();
		String ip = req.getRemoteAddr();
		int port = req.getRemotePort();
		logger.info("long-poll client:[{}:{}] timeout", ip, port);
		return true;
	}

	private void writeInfo(Response response, Object info) {
		try {
			Writer writer = response.getWriter();
			Request request = response.getRequest();
			String jsonp = request.getParameter("callback");
			if (jsonp != null) {
				writer.write(String.format("%s('%s')", jsonp, info));
			} else {
				writer.write(info.toString());
			}
			writer.flush();
		} catch (IOException e) {
			logger.error("occur error when response", e);
		}
	}


}
