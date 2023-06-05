package com.ice.hxy.util;

import cn.hutool.http.HttpRequest;
import com.google.gson.Gson;
import com.ice.hxy.common.ErrorCode;
import com.ice.hxy.config.initConfig.ConstantProperties;
import com.ice.hxy.service.commService.HttpService;
import com.ice.hxy.exception.GlobalException;
import com.ice.hxy.mode.entity.vo.ChatGptMessageVo;
import com.ice.hxy.mode.gpt.CUser;
import com.ice.hxy.mode.gpt.ChatMessage;
import com.ice.hxy.mode.gpt.ChatResponse;
import com.ice.hxy.mode.gpt.Choices;

import com.unfbx.chatgpt.OpenAiClient;
import com.unfbx.chatgpt.entity.common.Choice;
import com.unfbx.chatgpt.entity.completions.CompletionResponse;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @Author ice
 * @Date 2023/2/12 14:04
 * @Description: TODO
 */
@Slf4j
public class ChatGptUtils {
    private static final OpenAiClient openAiClient;
    private static final String token = "Bearer " + ConstantProperties.CG_TOKEN;
    private static final HttpService httpService = SpringUtil.getBean(HttpService.class);


    static {
        try {
            openAiClient = new OpenAiClient(ConstantProperties.CG_TOKEN);
        } catch (Exception e) {
            log.error("token 失效 Error={}", e.getMessage());
            throw new GlobalException(ErrorCode.SYSTEM_EXCEPTION);
        }
    }

    public static String sendChatGpt(String id, String content) {
        String message = "服务器限流，请稍后再试 | Server was limited, please try again later";
        if (!StringUtils.hasText(id) || !StringUtils.hasText(content)) {
            message = "请输入内容!";
            return message;
        }
        Map<String, String> map = new HashMap<>(3);
        map.put("apiKey", ConstantProperties.CG_TOKEN);
        map.put("sessionId", id);
        map.put("content", content);
        try {
            Gson gson = GsonUtils.getGson();
            String json = gson.toJson(map);
            String resp = HttpRequest.post("https://api.openai-proxy.com/v1/chat/completions")
                    .body(json)
                    .execute().body();
            ChatGptMessageVo messageVo = gson.fromJson(resp, ChatGptMessageVo.class);
            Integer code = messageVo.getCode();
            if (code == 200 || "执行成功".equals(messageVo.getMessage())) {
                message = messageVo.getData();
            } else {
                log.error(resp);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return message;
    }


    public static String sendChatP(String content) {
        if (!StringUtils.hasText(content)) {
            return "请先输入内容!";
        }
        String message = "服务器限流，请稍后再试 | Server was limited, please try again later";

        try {
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.add("Content-Type", "application/json");
            httpHeaders.add("Authorization", token);
            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setModel("gpt-3.5-turbo");
            CUser user = CUser.builder().content(content).role("user").build();
            chatMessage.getMessages().add(user);
            ResponseEntity<String> post = httpService.post("https://api.openai-proxy.com/v1/chat/completions",
                    httpHeaders, chatMessage.toString(), String.class);
            String body = post.getBody();
            ChatResponse chatResponse = GsonUtils.getGson().fromJson(body, ChatResponse.class);
            for (Choices choice : chatResponse.getChoices()) {
                CUser cUser = choice.getMessage();
                message = cUser.getContent();
            }
        } catch (Exception e) {
            log.error(" sendChatP 错误: {}", e.getMessage());
        }

        return message;
    }


    public static String sendChatG(String sendTex) {
        StringBuilder stringBuilder = new StringBuilder();
        CompletionResponse completions = openAiClient.completions(sendTex);
        if (completions == null) {
            return stringBuilder.toString();
        }
        Choice[] choices = completions.getChoices();
        for (Choice choice : choices) {
            String choiceText = choice.getText();
            stringBuilder.append(choiceText);
        }
        String txt = stringBuilder.toString();
        txt = Pattern.compile("\\s*|\t|\r|\n").matcher(txt).replaceAll("");
        return txt;
    }


}
