package com.olv.app.controller;

import com.olv.common.annotation.SystemControllerLog;
import com.olv.common.enums.RespCodeEnum;
import com.olv.common.enums.StateEnum;
import com.olv.common.enums.TransactionTypeEnum;
import com.olv.common.language.AppMessage;
import com.olv.common.po.AppUserPo;
import com.olv.common.resp.RespBody;
import com.olv.common.util.CryptUtils;
import com.olv.common.util.LanguageUtil;
import com.olv.common.util.LogUtils;
import com.olv.common.util.qrcode.QRCodeUtil;
import com.olv.common.vo.app.PaymentVo;
import com.olv.server.app.AppUserService;
import com.olv.server.app.BitcoinService;
import com.olv.server.app.WalletService;
import com.olv.server.common.CommonService;
import com.taobao.api.internal.util.Base64;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;

/**
 * Created by Administrator on 2017/8/21.
 */
@RestController
@RequestMapping("/wallet")
public class WalletController {

    @Resource
    private CommonService commonService;
    @Resource
    private WalletService walletService;
    @Resource
    private AppUserService appUserService;
    @Resource
    private LanguageUtil languageUtil;
    @Resource
    private BitcoinService  bitcoinService;

    /**
     * 生成支付二维码
     */
    @GetMapping(value = "/scan/qrcode")
    public RespBody qrcode() {

        RespBody respBody = new RespBody();
        try {
            //金额
 /*           if (amount == null) {
                respBody.add(RespCodeEnum.ERROR.getCode(),"金额不能为空");
                return respBody;
            }
            //转成bigDecimal类型
            BigDecimal b = new BigDecimal(amount);
            //保留2位小数
            amount = b.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();*/

            Double amount = 0d;
            AppUserPo appUserPo = commonService.checkToken();
            //更新用户币种业务
            bitcoinService.updateUserCoin(appUserPo.getId());
            String content = "amount=" + amount + "&receivedId=" + appUserPo.getId()+"&address="+appUserPo.getAddress();
//            String imgPath = request.getSession().getServletContext().getRealPath("/resources/") + "/css/img/logo.png";
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            QRCodeUtil.encode(content, null, baos);
            String rstr = "data:image/jpg;base64," + Base64.encodeToString(baos.toByteArray(), false);
            baos.close();
            respBody.add(RespCodeEnum.SUCCESS.getCode(), "获取支付二维码成功", rstr);

        } catch (Exception ex) {
            respBody.add(RespCodeEnum.ERROR.getCode(), "获取支付二维码失败");
            LogUtils.error("获取支付二维码失败！", ex);
        }
        return respBody;
    }

    /**
     * 扫码支付
     */
    @PostMapping(value = "/scan/payment")
    @SystemControllerLog(description = "扫码支付")
//    @RepeatControllerLog
    public RespBody payment(@RequestBody PaymentVo paymentVo) {

        RespBody respBody = new RespBody();
        try {
            Boolean flag = commonService.checkSign(paymentVo);
            if (!flag) {
                respBody.add(RespCodeEnum.ERROR.getCode(), languageUtil.getMsg(AppMessage.INVALID_SIGN, "无效签名"));
                return respBody;
            }

            if (StringUtils.isEmpty(paymentVo.getAmount())) {
                respBody.add(RespCodeEnum.ERROR.getCode(), languageUtil.getMsg(AppMessage.PAYEE_AMOUNT_NOT_NULL, "收款金额不能为空"));
                return respBody;
            }
            if ((new BigDecimal(paymentVo.getAmount())).compareTo(BigDecimal.ZERO) <= 0) {
                respBody.add(RespCodeEnum.ERROR.getCode(), languageUtil.getMsg(AppMessage.PARAM_ERROR, "参数有误"));
                return respBody;
            }
            if (StringUtils.isEmpty(paymentVo.getPayPwd())) {
                respBody.add(RespCodeEnum.ERROR.getCode(), languageUtil.getMsg(AppMessage.PAYPWD_NOT_NULL, "支付密码不能为空"));
                return respBody;
            }
            if (StringUtils.isEmpty(paymentVo.getPayeeId()) && StringUtils.isEmpty(paymentVo.getReceivedId())) {
                respBody.add(RespCodeEnum.ERROR.getCode(), languageUtil.getMsg(AppMessage.PAYEE_NOT_NULL, "收款人不能为空"));
                return respBody;
            }

            //获取当前登录用户
            AppUserPo appUserPo = commonService.checkToken();

            //判断支付密码是否正确
            String payPwd = CryptUtils.hmacSHA1Encrypt(paymentVo.getPayPwd(), appUserPo.getPayStal());
            if (!payPwd.equals(appUserPo.getPayPwd())) {
                respBody.add(RespCodeEnum.ERROR.getCode(), languageUtil.getMsg(AppMessage.PAYPWD_ERROR, "支付密码错误"));
                return respBody;
            }

            //根据用户id查询收款用户
            AppUserPo payeeUser = null;
            if (TransactionTypeEnum.TRANSACTION_PAY.getCode().equals(paymentVo.getTransactionType())) {
                if (paymentVo.getReceivedId().length()>32){
                    payeeUser = appUserService.findUserByAddress(paymentVo.getReceivedId());
                }
                if (payeeUser==null){
                    payeeUser = appUserService.findUserByUid(paymentVo.getReceivedId());
                    if (payeeUser == null) {
                        //如果根据uid查询失败，就根据手机查询
                        payeeUser = appUserService.findUserByMobile(paymentVo.getReceivedId());
                    }
                }
            } else {
                payeeUser = appUserService.findUserById(paymentVo.getPayeeId());
            }
            //超级用户不能做收款人
            if (payeeUser == null || payeeUser.getUid().equals(10000)) {
                respBody.add(RespCodeEnum.ERROR.getCode(), languageUtil.getMsg(AppMessage.PAYEE_INVAIL, "收款人不存在"));
                return respBody;
            }
            //收款人的手机号码最后四位数是否输入正确
            if (TransactionTypeEnum.TRANSACTION_PAY.getCode().equals(paymentVo.getTransactionType()) &&   !StringUtils.isEmpty(paymentVo.getKeyWord()) && !payeeUser.getMobile().endsWith(paymentVo.getKeyWord())) {
                respBody.add(RespCodeEnum.ERROR.getCode(), languageUtil.getMsg(AppMessage.PAYEE_PHONE_INVAIL, "收款人手机最后四位号码错误"));
                return respBody;
            }
            if (appUserPo.getId().equals(payeeUser.getId())) {
                respBody.add(RespCodeEnum.ERROR.getCode(), languageUtil.getMsg(AppMessage.PAYEE_NOT_SELF, "收款人不能是自己"));
                return respBody;
            }
            if (StateEnum.DISABLE.getCode().equals(payeeUser.getState())) {
                respBody.add(RespCodeEnum.ERROR.getCode(), languageUtil.getMsg(AppMessage.PAYEE_DISABLE, "收款人已被禁用"));
                return respBody;
            }
            if (appUserPo.getBirdScore().doubleValue() < Double.valueOf(paymentVo.getAmount())) {
                respBody.add(RespCodeEnum.ERROR.getCode(), languageUtil.getMsg(AppMessage.BALANCE_ERROR, "账户余额不足，请先兑换余额"));
                return respBody;
            }

            //处理转账业务
            walletService.birdtransfer(appUserPo,payeeUser,paymentVo.getAmount(),TransactionTypeEnum.TRANSACTION_PAY);

            respBody.add(RespCodeEnum.SUCCESS.getCode(), "转账成功");

        } catch (Exception ex) {
            respBody.add(RespCodeEnum.ERROR.getCode(), languageUtil.getMsg(AppMessage.EXCHANGE_FAIL, "转账失败"));
            LogUtils.error("转账失败", ex);
        }
        return respBody;
    }
}
