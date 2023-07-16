package com.ice.hxy;

import com.ice.hxy.mode.entity.Post;
import com.ice.hxy.mode.entity.PostGroup;
import com.ice.hxy.service.PostService.IPostService;
import com.ice.hxy.service.PostService.PostGroupService;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.FastByIDMap;
import org.apache.mahout.cf.taste.impl.model.GenericDataModel;
import org.apache.mahout.cf.taste.impl.model.GenericPreference;
import org.apache.mahout.cf.taste.impl.model.GenericUserPreferenceArray;
import org.apache.mahout.cf.taste.impl.neighborhood.ThresholdUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.PreferenceArray;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.UserBasedRecommender;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @Author ice
 * @Date 2023/6/10 9:57
 * @Description: TODO
 */
@SpringBootTest
public class MahoutTest {
    @Resource
    private IPostService postService;
    @Resource
    private PostGroupService postGroupService;
    @Test
    void test() throws TasteException {
        List<PostGroup> list = postGroupService.lambdaQuery().eq(PostGroup::getGroupId, 0L).select(PostGroup::getPostId).list();
        List<Long> ids = list.stream().map(PostGroup::getPostId).collect(Collectors.toList());
        List<Post> posts = postService.lambdaQuery().in(Post::getId, ids).select(Post::getId,Post::getUserId,Post::getViewNum, Post::getCollectNum, Post::getThumbNum).list();
        FastByIDMap<LinkedList<GenericPreference>> fastByIDMap = new FastByIDMap<>();
        for (Post post : posts) {
            Long postId = post.getId();
            Long userId = post.getUserId();
            Integer viewNum = post.getViewNum();
            Integer thumbNum = post.getThumbNum();
            Integer collectNum = post.getCollectNum();
            float value = viewNum + (thumbNum * 2) + (collectNum * 3);
            GenericPreference genericPreference = new GenericPreference(userId, postId, value);
            LinkedList<GenericPreference> preferences  = fastByIDMap.get(userId);
            if (preferences == null) {
                preferences = new LinkedList<>();
            }
            preferences.add(genericPreference);
            fastByIDMap.put(userId, preferences);
        }
        Set<Map.Entry<Long, LinkedList<GenericPreference>>> entrySet = fastByIDMap.entrySet();
        FastByIDMap<PreferenceArray> arrayFastByIDMap = new FastByIDMap<>();
        for (Map.Entry<Long, LinkedList<GenericPreference>> entry : entrySet) {
            Long key = entry.getKey();
            LinkedList<GenericPreference> value = entry.getValue();
            arrayFastByIDMap.put(key, new GenericUserPreferenceArray(value));
        }
        DataModel dataModel = new GenericDataModel(arrayFastByIDMap);
        //用户相似度模型
        UserSimilarity userSimilarity = new PearsonCorrelationSimilarity(dataModel);
        //构建 近邻对象 threshold 是相似阈值 这个数值越高 推荐精准越高 但是推荐的数据也越少 最高为 你给用户设置的喜好值最高值 也就是preference的最高值
        float threshold = 0f;
        UserNeighborhood neighborhood = new ThresholdUserNeighborhood(threshold, userSimilarity, dataModel);
        UserBasedRecommender recommender = new GenericUserBasedRecommender(dataModel, neighborhood, userSimilarity);
        //开始推荐 一参数为用户ID 二参数为 要推荐Item数量

        //我随便在用户列表里拿一个ID试试

        List<RecommendedItem> recommend = recommender.recommend(581118182418814L, 10);
        System.out.println(recommend);
        for (RecommendedItem recommendedItem : recommend) {
            System.out.println(recommendedItem);
        }

    }
}
