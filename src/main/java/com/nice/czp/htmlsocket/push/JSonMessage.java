package com.nice.czp.htmlsocket.push;

import com.alibaba.fastjson.JSONObject;
import com.nice.czp.htmlsocket.api.ICodec;
import com.nice.czp.htmlsocket.api.IMessage;

public class JSonMessage implements ICodec, IMessage {

    private static final long serialVersionUID = 1L;
    private String subject;
    private Object data;
    private long from = -1;
    private long to = -1;

    @Override
    public IMessage decode(String textMessage) {
        JSONObject json = JSONObject.parseObject(textMessage);
        return JSONObject.toJavaObject(json, JSonMessage.class);
    }

    @Override
    public IMessage decode(byte[] byteMessage) {
        return decode(new String(byteMessage));
    }

    @Override
    public String endcodeToText(IMessage obj) {
        return JSONObject.toJSONString(obj);
    }

    @Override
    public byte[] endcodeToBytes(IMessage obj) {
        return endcodeToText(obj).getBytes();
    }

    @Override
    public String getSubject() {
        return subject;
    }

    @Override
    public Object getData() {
        return data;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setData(Object data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "JSonMessage [subject=" + subject + "]";
    }

    @Override
    public long getFrom() {
        return from;
    }

    @Override
    public long getTo() {
        return to;
    }

    public void setFrom(long from) {
        this.from = from;
    }

    public void setTo(long to) {
        this.to = to;
    }

}
