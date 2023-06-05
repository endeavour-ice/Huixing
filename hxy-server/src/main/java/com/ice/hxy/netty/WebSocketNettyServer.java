package com.ice.hxy.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;

/**
 * @author ice
 * @date 2022/7/22 15:55
 */
@Component
@Slf4j
public class WebSocketNettyServer {

    // 创建Netty服务启动对象
    private final ServerBootstrap serverBootstrap;
    // 创建两个线程池
    // 主 : 处理连接
    private final NioEventLoopGroup bossGroup = new NioEventLoopGroup(1);
    // 从 : 处理业务
    private final NioEventLoopGroup workerGroup = new NioEventLoopGroup(8);

    public void start() {
        // 监听端口
        serverBootstrap.bind(9001);
        log.info("netty-server 启动成功");
    }

    /**
     * 销毁
     */
    @PreDestroy
    public void destroy() {
        bossGroup.shutdownGracefully().syncUninterruptibly();
        workerGroup.shutdownGracefully().syncUninterruptibly();
        log.info("关闭 ws server 成功");
    }

    public WebSocketNettyServer() {
        // 启动类
        serverBootstrap = new ServerBootstrap();
        // 初始化服务器启动对象
        serverBootstrap.
                group(bossGroup, workerGroup)
                // 指定Netty 通道类型 Nio 同步非阻塞io
                .channel(NioServerSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                .option(ChannelOption.SO_BACKLOG, 128) // 设置线程队列得到的连接个数
                .childOption(ChannelOption.SO_KEEPALIVE, true) // 设置保持活动的连接状态
                .childHandler(new WebSocketChannelInitializer());// 初始化handler
    }
}
