package com.ice.hxy.controller;

import com.ice.hxy.common.B;
import com.ice.hxy.service.ScoreService.IUserScoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 积分表 前端控制器
 * </p>
 *
 * @author ice
 * @since 2023-05-14
 */
@RestController
@RequestMapping("/score/order")
public class ScoreController {
    @Autowired
    private IUserScoreService scoreService;

    @GetMapping("/get")
    public B<Double> getScoreByUser() {
        Double score= scoreService.getScore();
        return B.ok(score);
    }

}
