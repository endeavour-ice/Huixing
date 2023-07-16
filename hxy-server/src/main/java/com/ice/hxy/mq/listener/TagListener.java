package com.ice.hxy.mq.listener;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ice.hxy.mode.entity.Tags;
import com.ice.hxy.service.TagService.TagsService;
import com.ice.hxy.util.GsonUtils;
import com.ice.hxy.util.LongUtil;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author ice
 * @Date 2023/5/28 10:55
 * @Description: 标签mq监听
 */
@Component
@Slf4j
public class TagListener {
    @Autowired
    private TagsService tagsService;

    @RabbitListener(queues = "tag_queue")
    public void tagNum(Message message, Channel channel,String json) {
        try {
            Gson gson = GsonUtils.getGson();
            Map<String, Object> map = gson.fromJson(json, new TypeToken<HashMap<String, Object>>() {
            }.getType());
            Double uid = (Double) map.get("userId");
            Double gid = (Double) map.get("groupId");
            long userId = (long) uid.doubleValue();
            long groupId = (long) gid.doubleValue();
            String tag = (String) map.get("tag");
            if (LongUtil.isEmpty(userId) || LongUtil.isEmpty(groupId) || !StringUtils.hasText(tag)) {
                return;
            }
            List<Tags> list = tagsService.lambdaQuery().eq(Tags::getCategory, groupId).list();
            Map<String, Tags> tagsMap = list.stream().collect(Collectors.toMap(Tags::getTag, t -> t));
            List<String> jsonTag = GsonUtils.getGson().fromJson(tag, new TypeToken<List<String>>() {
            }.getType());
            List<Tags> newTags = new ArrayList<>();
            List<Tags> upTags = new ArrayList<>();
            for (String g : jsonTag) {
                Tags tags = tagsMap.get(g);
                if (tags == null) {
                    Tags s = new Tags();
                    s.setTag(g);
                    s.setCreatorId(userId);
                    s.setCategory(groupId);
                    s.setTagNum(1);
                    newTags.add(s);
                } else {
                    tags.setTagNum((tags.getTagNum() + 1));
                    upTags.add(tags);
                }
            }
            tagsService.saveBatch(newTags);
            tagsService.updateBatchById(upTags);

        } catch (Exception e) {
            log.error("TagListener tagNum error:{}", e.getMessage());
        }
    }
}
