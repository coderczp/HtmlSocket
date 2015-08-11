package com.nice.czp.htmlsocket.api;


/**
 * 
 * 消息的编解码接口
 * 
 * @author coder_czp@126.com-2015年8月8日
 */
public interface ICodec {

    /**
     * 从文本构建消息对象,主要针对非socket模式的消息传输
     * 
     * @param textMessage
     *            前端提交的文本消息
     * @return
     *         IMessage
     * 
     */
    IMessage decode(String textMessage);

    /**
     * 从byte构建消息对象,主要针对socket模式的消息传输
     * 
     * @param byteMessage
     *            前端提交的二进制消息
     * @return
     *         IMessage
     * 
     */
    IMessage decode(byte[] byteMessage);

    /**
     * 将消息对象编码为文本,主要用于非socket传输
     * 
     * @param message
     *            IMessage消息对象
     * @return String
     *         编码后的字符串
     */
    String endcodeToText(IMessage message);

    /**
     * 将消息消息编码为bytes,主要用于socket传输
     * 
     * @param message
     *            IMessage消息对象
     * @return byte[]
     *         编码后的bytes
     */
    byte[] endcodeToBytes(IMessage message);

}
