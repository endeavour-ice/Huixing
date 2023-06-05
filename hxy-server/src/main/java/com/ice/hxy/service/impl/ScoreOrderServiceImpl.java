package com.ice.hxy.service.impl;

import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradeAppPayModel;
import com.alipay.api.request.AlipayTradeAppPayRequest;
import com.alipay.api.request.AlipayTradePrecreateRequest;
import com.alipay.api.response.AlipayTradeAppPayResponse;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.ice.hxy.common.ErrorCode;
import com.ice.hxy.config.initConfig.ConstantProperties;
import com.ice.hxy.exception.GlobalException;
import com.ice.hxy.extend.zfb.ZFBConfig;
import com.ice.hxy.mapper.ScoreOrderMapper;
import com.ice.hxy.mode.entity.ScoreOrder;
import com.ice.hxy.mode.entity.ScorePayment;
import com.ice.hxy.mode.entity.User;
import com.ice.hxy.service.ScoreService.IScoreOrderService;
import com.ice.hxy.service.ScoreService.IScorePaymentService;
import com.ice.hxy.util.GsonUtils;
import com.ice.hxy.util.SnowFlake;
import com.ice.hxy.util.UserUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * 订单表 服务实现类
 * </p>
 *
 * @author ice
 * @since 2023-05-14
 */
@Service
@Slf4j
public class ScoreOrderServiceImpl extends ServiceImpl<ScoreOrderMapper, ScoreOrder> implements IScoreOrderService {
    @Autowired
    private IScorePaymentService paymentService;

    @Override
    public String saveByZFBWY(String subject, String totalAmount, String score, HttpServletResponse response) {
        return saveByZFB(subject, totalAmount, score, response);
    }

    @Override
    public void saveByZFBSJ(String subject, String totalAmount, String score, HttpServletResponse response) {
        User loginUser = UserUtils.getLoginUser();
        if (!StringUtils.hasText(subject)
                || !StringUtils.hasText(totalAmount) || !StringUtils.hasText(score)) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR);

        }
        try {
            double parseDouble = Double.parseDouble(totalAmount);
            double sc = Double.parseDouble(score);
            if (parseDouble < 0.01 || sc < 0) {
                throw new GlobalException(ErrorCode.PARAMS_ERROR, "金额错误");
            }
            DefaultAlipayClient alipayClient = ZFBConfig.getClient();
            AlipayTradeAppPayRequest appRequest = new AlipayTradeAppPayRequest();
            appRequest.setNotifyUrl(ConstantProperties.NOTIFYURL);
            appRequest.setReturnUrl(ConstantProperties.RETURNURL);
            AlipayTradeAppPayModel model = new AlipayTradeAppPayModel();
            model.setSubject(subject);
            String snowString = SnowFlake.getSnowString();
            model.setOutTradeNo(snowString);
            model.setTimeoutExpress("30m");
            model.setTotalAmount(totalAmount);
            model.setProductCode("QUICK_MSECURITY_PAY");
            appRequest.setBizModel(model);
            AlipayTradeAppPayResponse appResponse = alipayClient.sdkExecute(appRequest);
            String body = appResponse.getBody();

            if (appResponse.isSuccess()) {
                ScoreOrder scoreOrder = new ScoreOrder();
                scoreOrder.setUserId(loginUser.getId());
                scoreOrder.setOrderId(snowString);
                scoreOrder.setScore(BigDecimal.valueOf(sc));
                scoreOrder.setScoreAmount(BigDecimal.valueOf(parseDouble));
                scoreOrder.setOrderStatus((byte) 0);
                this.save(scoreOrder);

                // 调用SDK生成表单
                response.setContentType("text/html;charset=utf-8");
                response.getWriter().write(body);
                // 直接将完整的表单html输出到页面
                response.getWriter().flush();
                response.getWriter().close();
            } else {
                log.error("pay payResponse.isSuccess() 失败; body:{}", body);
                throw new GlobalException(ErrorCode.ERROR);
            }
        } catch (Exception e) {
            log.error("pay 错误 message:{}", e.getMessage());
            throw new GlobalException(ErrorCode.SYSTEM_EXCEPTION);
        }

    }

    @Transactional(rollbackFor = Exception.class)
    public String saveByZFB(String subject, String totalAmount, String score, HttpServletResponse response) {
        User loginUser = UserUtils.getLoginUser();
        if (!StringUtils.hasText(subject)
                || !StringUtils.hasText(totalAmount) || !StringUtils.hasText(score)) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR);
        }
        try {
            double parseDouble = Double.parseDouble(totalAmount);
            double sc = Double.parseDouble(score);
            if (parseDouble < 0.01 || sc < 0) {
                throw new GlobalException(ErrorCode.PARAMS_ERROR, "金额错误");
            }
            //实例化客户端,填入所需参数
            DefaultAlipayClient alipayClient = ZFBConfig.getClient();
            AlipayTradePrecreateRequest alipayRequest = new AlipayTradePrecreateRequest();
            alipayRequest.setNotifyUrl(ConstantProperties.NOTIFYURL);
            alipayRequest.setReturnUrl(ConstantProperties.RETURNURL);
            String snowString = SnowFlake.getSnowString();
            String product_code = "FAST_INSTANT_TRADE_PAY";
            Gson gson = GsonUtils.getGson();
            Map<String, Object> map = new HashMap<>();
            //商户订单号，商家自定义，保持唯一性
            map.put("out_trade_no", snowString);
            //支付金额，最小值0.01元
            map.put("total_amount", parseDouble);
            //订单标题，不可使用特殊符号
            map.put("subject", subject);
            map.put("store_id", "天衡");
            //手机网站支付默认传值FAST_INSTANT_TRADE_PAY; QUICK_WAP_WAY

            //map.put("product_code", product_code);
            String s = gson.toJson(map);
            alipayRequest.setBizContent(s);


            AlipayTradePrecreateResponse payResponse = alipayClient.execute(alipayRequest);
            String body = payResponse.getBody();
            if (payResponse.isSuccess()) {
                ScoreOrder scoreOrder = new ScoreOrder();
                scoreOrder.setUserId(loginUser.getId());
                scoreOrder.setOrderId(snowString);
                scoreOrder.setScore(BigDecimal.valueOf(sc));
                scoreOrder.setScoreAmount(BigDecimal.valueOf(parseDouble));
                scoreOrder.setOrderStatus((byte) 0);
                this.save(scoreOrder);
                // 调用SDK生成表单
                return payResponse.getQrCode();
                //response.setContentType("text/html;charset=utf-8");
                //response.getWriter().write(body);
                //// 直接将完整的表单html输出到页面
                //response.getWriter().flush();
                //response.getWriter().close();
            } else {
                log.error("pay payResponse.isSuccess() 失败; body:{}", body);
                throw new GlobalException(ErrorCode.ERROR);
            }

        } catch (Exception e) {
            log.error("pay 错误 message:{}", e.getMessage());
            throw new GlobalException(ErrorCode.SYSTEM_EXCEPTION);
        }
    }

    @Override
    public Boolean notifyS(HttpServletRequest request) {
        HashMap<String, String> paramMap = new HashMap<>();
        Map<String, String[]> parameterMap = request.getParameterMap();
        for (String s : parameterMap.keySet()) {
            paramMap.put(s, request.getParameter(s));
        }
        String out_trade_no = paramMap.get("out_trade_no");
        QueryWrapper<ScoreOrder> wrapper = new QueryWrapper<>();
        wrapper.eq("order_id", out_trade_no);
        ScoreOrder scoreOrder = baseMapper.selectOne(wrapper);
        //if (scoreOrder == null) {
        //    throw new GlobalException(ErrorCode.PARAMS_ERROR, "订单不存在");
        //}
        scoreOrder.setOrderStatus((byte) 1);
        ScorePayment scorePayment = new ScorePayment();
        scorePayment.setOrderId(Long.valueOf(out_trade_no));
        scorePayment.setPaymentId(SnowFlake.getSnowLong());
        scorePayment.setSubject(paramMap.get("subject"));
        scorePayment.setScoreAmount(scoreOrder.getScoreAmount());
        scorePayment.setTradeNo(paramMap.get("trade_no"));
        scorePayment.setBuyerAmount(BigDecimal.valueOf(Long.parseLong(paramMap.get("buyer_pay_amount"))));
        scorePayment.setTotalAmount(BigDecimal.valueOf(Long.parseLong(paramMap.get("total_amount"))));
        scorePayment.setPaymentStatus(paramMap.get("trade_status"));
        scorePayment.setCallbackContent(paramMap.toString());
        scorePayment.setGmtCreate(paramMap.get("gmt_create"));
        scorePayment.setPaymentType((byte) 1);
        System.out.println("id" + paramMap.get("buyer_id"));
        //baseMapper.updateById(scoreOrder);
        return true;
    }
}
