package com.nice.czp.htmlsocket.push;

import com.nice.czp.htmlsocket.api.ICodec;

/**
 * 
 * @author coder_czp@126.com-2015年8月8日
 * 
 */
public class PushSerConfig {

    private int port;
    private ICodec codec;
    private String resourceBase = "./";
    /* Access-Control-Allow-Origin 默认允许所有站点跨域访问 */
    private String corsControl = "*";
    private long connTimeout = 60 * 1000;

    public PushSerConfig(int port, ICodec codec) {
        this.port = port;
        this.codec = codec;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public ICodec getCodec() {
        return codec;
    }

    public void setCodec(ICodec codec) {
        this.codec = codec;
    }

    public long getConnTimeout() {
        return connTimeout;
    }

    public void setConnTimeout(long connTimeout) {
        this.connTimeout = connTimeout;
    }

    public String getCorsControl() {
        return corsControl;
    }

    public void setCorsControl(String corsControl) {
        this.corsControl = corsControl;
    }

    public String getResourceBase() {
        return resourceBase;
    }

    public void setResourceBase(String resourceBase) {
        this.resourceBase = resourceBase;
    }

}
