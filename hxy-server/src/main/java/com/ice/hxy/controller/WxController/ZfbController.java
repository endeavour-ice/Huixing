package com.ice.hxy.controller.WxController;


import com.ice.hxy.common.B;
import com.ice.hxy.service.ScoreService.IScoreOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @Author ice
 * @Date 2023/5/14 16:04
 * @Description: 支付宝
 */
@RestController
@RequestMapping("/zfb")
@Slf4j
public class ZfbController {
    @Autowired
    private IScoreOrderService orderService;

    /**
     * 网页
     *
     * @param subject     内容
     * @param totalAmount 金额
     * @param score       积分
     * @param response
     */
    @GetMapping("/pay/{subject}/{totalAmount}/{score}")
    public B<String> pay(@PathVariable("subject") String subject,
                         @PathVariable("totalAmount") String totalAmount,
                         HttpServletResponse response, @PathVariable("score") String score) {

        return B.ok(orderService.saveByZFBWY(subject, totalAmount, score, response));
    }

    @PostMapping("/notify")
    public void notifyS(HttpServletRequest request) {
        Boolean is = orderService.notifyS(request);


    }

    /**
     * 手机网站支付
     */
    @GetMapping("/wapPay/{subject}/{totalAmount}/{score}")
    public void doPost(@PathVariable("subject") String subject,
                       @PathVariable("totalAmount") String totalAmount,
                       HttpServletResponse response, @PathVariable("score") String score) {
        orderService.saveByZFBSJ(subject, totalAmount, score, response);
    }
}