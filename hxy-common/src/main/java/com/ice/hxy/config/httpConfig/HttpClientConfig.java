package com.ice.hxy.config.httpConfig;

import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * @Author ice
 * @Date 2023/5/8 22:46
 * @Description: TODO
 */
@Configuration
public class HttpClientConfig {
    private Integer maxTotal = 300;// 最大连接

    private Integer defaultMaxPerRoute =50;// 每个host的最大连接

    private Integer connectTimeout=1000;// 连接超时时间

    private Integer connectionRequestTimeout=5000;// 请求超时时间

    private Integer socketTimeout=5000;// 响应超时时间
    /**
     * HttpClient连接池
     * @return
     */
    @Bean
    public HttpClientConnectionManager httpClientConnectionManager() {
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(maxTotal);
        connectionManager.setDefaultMaxPerRoute(defaultMaxPerRoute);
        return connectionManager;
    }

    /**
     * 注册RequestConfig
     * @return
     */
    @Bean
    public RequestConfig requestConfig() {
        return RequestConfig.custom().setConnectTimeout(connectTimeout)
                .setConnectionRequestTimeout(connectionRequestTimeout).setSocketTimeout(socketTimeout)
                .build();
    }

    /**
     * 注册HttpClient
     * @param manager
     * @param config
     * @return
     */
    @Bean
    public HttpClient httpClient(HttpClientConnectionManager manager, RequestConfig config) {
        return HttpClientBuilder.create().setConnectionManager(manager).setDefaultRequestConfig(config)
                .build();
    }
    /**
     * 使用连接池管理连接
     * @param httpClient
     * @return
     */
    @Bean
    public ClientHttpRequestFactory requestFactory(HttpClient httpClient) {
        return new HttpComponentsClientHttpRequestFactory(httpClient);
    }
    /**
     * 使用HttpClient来初始化一个RestTemplate
     * @param requestFactory
     * @return
     */
    @Bean
    public RestTemplate restTemplate(ClientHttpRequestFactory requestFactory) {
        RestTemplate template = new RestTemplate(requestFactory);
        List<HttpMessageConverter<?>> list = template.getMessageConverters();
        for (HttpMessageConverter<?> mc : list) {
            if (mc instanceof StringHttpMessageConverter) {
                ((StringHttpMessageConverter) mc).setDefaultCharset(StandardCharsets.UTF_8);
            }
        }
        return template;
    }
}
