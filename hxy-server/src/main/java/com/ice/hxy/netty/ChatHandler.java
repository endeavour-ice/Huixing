package com.ice.hxy.netty;

import com.google.gson.Gson;

import com.ice.hxy.mode.chat.ChatType;
import com.ice.hxy.mode.chat.SocketReq;
import com.ice.hxy.util.GsonUtils;
import com.ice.hxy.service.WebSocketService;
import com.ice.hxy.util.SpringUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

/**
 * @author ice
 * @date 2022/7/22 16:27
 */
@Slf4j
public class ChatHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    // 用来保存所有的客服端连接
    private static final WebSocketService webSocketService = SpringUtil.getBean(WebSocketService.class);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) {
        // 当接收到数据后自动调用
        Gson gson = GsonUtils.getGson();
        SocketReq socketReq = gson.fromJson(msg.text(), SocketReq.class);
        ChatType chatType = ChatType.of(socketReq.getType());
        Channel channel = ctx.channel();
        switch (chatType) {
            case XT:
                break;
            case CT:
                webSocketService.auth(String.valueOf(socketReq.getData()),channel);
                break;
            default:
                log.warn("未知类型: {}",chatType);
        }

    }

    // 新的客服端连接时调用
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        webSocketService.handlerAdded(ctx.channel());

    }

    // 出现异常时调用
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.warn("异常发生,异常消息: {}",cause.getMessage());
        Channel channel = ctx.channel();
        channel.close();


    }
    // 注册的ChannelHandlerContext的Channel现在处于非活动状态，并且已达到其生存期结束。
    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        userOff(ctx);
    }

    // channel 处于活动状态调用
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
    }

    private void userOff(ChannelHandlerContext ctx) {
        Channel channel = ctx.channel();
        webSocketService.close(channel);
        channel.close();

    }
    // 用户断开连接调用
    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        userOff(ctx);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if(evt instanceof IdleStateEvent) {
            IdleStateEvent idleStateEvent = (IdleStateEvent)evt;

            if(idleStateEvent.state() == IdleState.READER_IDLE) {
//                log.info("读空闲事件触发...");
            }
            else if(idleStateEvent.state() == IdleState.WRITER_IDLE) {
//                log.info("写空闲事件触发...");
            }
            else if(idleStateEvent.state() == IdleState.ALL_IDLE) {
                userOff(ctx);

            }
        }
        super.userEventTriggered(ctx, evt);
    }
}
