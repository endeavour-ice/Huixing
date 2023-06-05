package com.ice.hxy.service.ScoreService;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ice.hxy.mode.entity.ScoreOrder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * 订单表 服务类
 * </p>
 *
 * @author ice
 * @since 2023-05-14
 */
public interface IScoreOrderService extends IService<ScoreOrder> {

    String saveByZFBWY(String subject, String totalAmount, String score, HttpServletResponse response);

    void saveByZFBSJ(String subject, String totalAmount, String score, HttpServletResponse response);

    Boolean notifyS(HttpServletRequest request);
}
