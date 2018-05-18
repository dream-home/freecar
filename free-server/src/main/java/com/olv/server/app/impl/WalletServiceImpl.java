package com.olv.server.app.impl;

import com.olv.common.enums.BusnessTypeEnum;
import com.olv.common.enums.CurrencyTypeEnum;
import com.olv.common.enums.PerformanceTypeEnum;
import com.olv.common.enums.TransactionTypeEnum;
import com.olv.common.exception.CommException;
import com.olv.common.language.AppMessage;
import com.olv.common.po.AppPerformanceParamPo;
import com.olv.common.po.AppUserContactPo;
import com.olv.common.po.AppUserPo;
import com.olv.common.util.LanguageUtil;
import com.olv.common.util.LogUtils;
import com.olv.common.util.ToolUtils;
import com.olv.server.app.*;
import com.olv.server.common.CommonService;
import com.olv.server.mapper.AppUserMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 转账业务类
 * Created by Administrator on 2017/8/22.
 */
@Service
public class WalletServiceImpl implements WalletService {
    @Resource
    private LanguageUtil msgUtil;
    @Resource
    private AppUserMapper appUserMapper;
    @Resource
    private AppUserContactService appUserContactService;
    @Resource
    private AppBillRecordService appBillRecordService;
    @Resource
    private AppPerformanceParamService appPerformanceParamService;
    @Resource
    private AppUserService appUserService;
    @Resource
    private CommonService commonService;
    @Resource
    private BitcoinService bitcoinService;
    @Value ("${walletpassword}")
    private String password;

    /**
     * ep余额转账
     *
     * @param payUser   付款人
     * @param payeeUser 收款人
     * @param amount    转账金额
     * @throws Exception
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void payment(AppUserPo payUser, AppUserPo payeeUser, String amount) throws Exception {
        //一个业务产生所有流水都共用一个uuid,方便管理后端查询
        String uuid = ToolUtils.getUUID();
        //设置收款金额为BigDecimal
        BigDecimal bigDecimalMoney = new BigDecimal(amount);
        //付款人付款前金额
        BigDecimal payUser_beforeAmout = payUser.getEpBalance();
        //付款人付款后金额
        BigDecimal payUser_afterAmout = payUser_beforeAmout.subtract(bigDecimalMoney).setScale(4, BigDecimal.ROUND_HALF_EVEN);
        //收款人收款前金额
        BigDecimal payeeUser_beforeAmout = payeeUser.getEpBalance();
        //收款人收款后金额
        BigDecimal payeeUser_afterAmout = payeeUser_beforeAmout.add(bigDecimalMoney).setScale(4, BigDecimal.ROUND_HALF_EVEN);

        //减少付款人的余额
        int rows = appUserMapper.updateEpBalanceById(bigDecimalMoney.multiply(new BigDecimal(-1)), payUser.getId());
        //数据更新行数为0 ，抛出异常
        if (rows == 0) {
            LogUtils.error("用户付款失败,数据库更新行数为0");
            throw new Exception();
        }
        //付款人的余额流水记录
        Map payMap = new HashMap();
        payMap.put("phone", payeeUser.getMobile());
        payMap.put("nickName", payeeUser.getNickName());
        String payExtend = ToolUtils.toJson(payMap);
        appBillRecordService.saveBillRecord(uuid, payUser.getId(), BusnessTypeEnum.EP_TRANSFER_OUT.getCode(), CurrencyTypeEnum.EP_BALANCE.getCode(), bigDecimalMoney.multiply(new BigDecimal(-1)), payUser_beforeAmout, payUser_afterAmout, "用户转账-付款方减少ep余额", payExtend);

        //增加收款人的余额
        rows = appUserMapper.updateEpBalanceById(bigDecimalMoney, payeeUser.getId());
        //数据更新行数为0 ，抛出异常
        if (rows == 0) {
            LogUtils.error("用户收款失败,数据库更新行数为0");
            throw new Exception();
        }
        //收款人的余额流水记录
        Map receviceMap = new HashMap();
        receviceMap.put("phone", payUser.getMobile());
        receviceMap.put("nickName", payUser.getNickName());
        String receviceExtend = ToolUtils.toJson(receviceMap);
        appBillRecordService.saveBillRecord(uuid, payeeUser.getId(), BusnessTypeEnum.EP_TRANSFER_IN.getCode(), CurrencyTypeEnum.EP_BALANCE.getCode(), bigDecimalMoney, payeeUser_beforeAmout, payeeUser_afterAmout, "用户转账-收款方增加ep余额", receviceExtend);
    }

    /**
     * TFC对外部地址转账
     *
     * @param payUser   付款人
     * @param amount    转账金额
     * @throws Exception
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void birdtransferToOutAddress(AppUserPo payUser, String  outAddress, BigDecimal amount, TransactionTypeEnum transactionTypeEnum) throws Exception {
        if (!bitcoinService.validAddress(outAddress)) {
            throw new CommException(msgUtil.getMsg(AppMessage.COIN_ADDRESS_INVAIL, "输入钱包地址无效"));
        }

        //一个业务产生所有流水都共用一个uuid,方便管理后端查询
        String uuid = ToolUtils.getUUID();
        //付款人付款前TFC
        BigDecimal payUser_beforeScore = payUser.getBirdScore();
        //速通宝对外转出手续费
        Double tfcTransferPoundage = ToolUtils.parseDouble(commonService.findParameter("tfcTransferPoundage"));
        //增加对外手续费
        BigDecimal coinBalance = amount.add(new BigDecimal (tfcTransferPoundage)).setScale(4, RoundingMode.HALF_EVEN);
        //付款人付款后TFC
        BigDecimal payUser_afterScore = payUser_beforeScore.subtract(coinBalance).setScale(4, BigDecimal.ROUND_HALF_EVEN);
        //再次判断用户TFC是否足够
        if (payUser.getBirdScore().compareTo(coinBalance) == -1) {
            //转账价格大于当前用户价格
            throw new CommException(msgUtil.getMsg(AppMessage.BIRDSCORE_LESS, "转账TFC过大"));
        }
        //速通宝对外转出最小额度
        Double outAddressMin = ToolUtils.parseDouble(commonService.findParameter("outAddressMin"));
        if (coinBalance.compareTo (new BigDecimal (outAddressMin))==-1) {
            throw new CommException(msgUtil.getMsg(AppMessage.TRANSFER_FAILED, "转账失败"));
        }
        //速通宝对外转出最大额度
        Double outAddressMax = ToolUtils.parseDouble(commonService.findParameter("outAddressMax"));
        //再次判断用户速通宝是否足够
        if (new BigDecimal(outAddressMax).compareTo(coinBalance) == -1) {
            //转账价格大于当前用户价格
            throw new CommException(msgUtil.getMsg(AppMessage.BIRDSCORE_LESS, "转账TFC过大"));
        }
        //减少付款人的TFC
        int rows = appUserMapper.updateBirdScoreById(coinBalance.multiply(new BigDecimal(-1)), payUser.getId());
        //数据更新行数为0 ，抛出异常
        if (rows == 0) {
            LogUtils.error("用户付款失败,数据库更新行数为0");
            throw new Exception();
        }

        Map payMap = new HashMap();
        payMap.put("address", outAddress);
        String payExtend = ToolUtils.toJson(payMap);
        //付款人的TFC流水记录
        appBillRecordService.saveBillRecord(uuid, payUser.getId(), BusnessTypeEnum.BIRDSCORE_TRANSFER_OUT_ADDRESS.getCode(), CurrencyTypeEnum.BIRDSCORE.getCode(), coinBalance.multiply(new BigDecimal(-1)), payUser_beforeScore, payUser_afterScore, "用户转账-付款方减少TFC", payExtend);



        //先保存数据库交易数据
//        appBillRecordService.saveBillRecord(uuid, outAddress, BusnessTypeEnum.BIRDSCORE_TRANSFER_OUT_ADDRESS.getCode(), CurrencyTypeEnum.BIRDSCORE.getCode(), amount, BigDecimal.ZERO, BigDecimal.ZERO, "对外转钱包地址-收款记录", payExtend);
        //地址转账（所有内部用户对外转账都通过10000号转出）
        if (StringUtils.isEmpty (password)){
            LogUtils.error ("密码错误");
            throw new CommException(msgUtil.getMsg(AppMessage.TRANSFER_FAILED, "转账失败"));
        }
        bitcoinService.getBitCoinClient ().walletpassphrase (password);
        String transOrderId = bitcoinService.getBitCoinClient().sendFrom("10000", outAddress, amount, 3, "", "");
        if (transOrderId == null) {
            throw new CommException(msgUtil.getMsg(AppMessage.TRANSFER_FAILED, "转账失败"));
        }
    }


    /**
     * TFC转账
     *
     * @param payUser   付款人
     * @param payeeUser 收款人
     * @param amount    转账金额
     * @throws Exception
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void birdtransfer(AppUserPo payUser, AppUserPo payeeUser, String amount, TransactionTypeEnum transactionTypeEnum) throws Exception {

        //一个业务产生所有流水都共用一个uuid,方便管理后端查询
        String uuid = ToolUtils.getUUID();
        //设置收款金额为BigDecimal
        BigDecimal bigDecimalMoney = new BigDecimal(amount);
        //付款人付款前TFC
        BigDecimal payUser_beforeScore = payUser.getBirdScore();
        //付款人付款后TFC
        BigDecimal payUser_afterScore = payUser_beforeScore.subtract(bigDecimalMoney).setScale(4, BigDecimal.ROUND_HALF_EVEN);
        //收款人收款前TFC
        BigDecimal payeeUser_beforeScore = payeeUser.getBirdScore();
        //收款人收款后TFC
        BigDecimal payeeUser_afterScore = payeeUser_beforeScore.add(bigDecimalMoney).setScale(4, BigDecimal.ROUND_HALF_EVEN);

        //减少付款人的TFC
        int rows = appUserMapper.updateBirdScoreById(bigDecimalMoney.multiply(new BigDecimal(-1)), payUser.getId());
        //数据更新行数为0 ，抛出异常
        if (rows == 0) {
            LogUtils.error("用户付款失败,数据库更新行数为0");
            throw new Exception();
        }
        //付款人的TFC流水记录
        Map payMap = new HashMap();
        payMap.put("phone", payeeUser.getMobile());
        payMap.put("nickName", payeeUser.getNickName());
        payMap.put("code", transactionTypeEnum.getCode());
        payMap.put("name", transactionTypeEnum.getName());
        payMap.put("headImg", payeeUser.getImgPath());
        payMap.put("uid", payeeUser.getUid());
        String payExtend = ToolUtils.toJson(payMap);
        appBillRecordService.saveBillRecord(uuid, payUser.getId(), BusnessTypeEnum.BIRDSCORE_TRANSFER_OUT.getCode(), CurrencyTypeEnum.BIRDSCORE.getCode(), bigDecimalMoney.multiply(new BigDecimal(-1)), payUser_beforeScore, payUser_afterScore, "用户转账-付款方减少TFC", payExtend);

        //增加收款人的TFC
        rows = appUserMapper.updateBirdScoreById(bigDecimalMoney, payeeUser.getId());
        //数据更新行数为0 ，抛出异常
        if (rows == 0) {
            LogUtils.error("用户收款失败,数据库更新行数为0");
            throw new Exception();
        }
        //付款人的TFC流水记录
        Map receviceMap = new HashMap();
        receviceMap.put("phone", payUser.getMobile());
        receviceMap.put("nickName", payUser.getNickName());
        String receviceExtend = ToolUtils.toJson(receviceMap);
        appBillRecordService.saveBillRecord(uuid, payeeUser.getId(), BusnessTypeEnum.BIRDSCORE_TRANSFER_IN.getCode(), CurrencyTypeEnum.BIRDSCORE.getCode(), bigDecimalMoney, payeeUser_beforeScore, payeeUser_afterScore, "用户转账-收款方增加TFC", receviceExtend);
    }


    @Override
    @Transactional
    public Boolean epExchange(String userId, BigDecimal amount) throws Exception {
        String bunessNo = ToolUtils.getOrderNo();
        BigDecimal performance = BigDecimal.ZERO;
        AppUserPo userPo = appUserMapper.findUserById(userId);
        if (userPo.getEpBalance().compareTo(amount) == -1) {
            throw new CommException(msgUtil.getMsg(AppMessage.BALANCE_ERROR, "账户EP余额不足"));
        }
        AppUserContactPo contactPo = appUserContactService.findUserByUserId(userId);
        if (contactPo.getPerformanceA().compareTo(contactPo.getPerformanceB()) < 1) {
            performance = contactPo.getPerformanceA();
        } else {
            performance = contactPo.getPerformanceB();
        }
        AppPerformanceParamPo paramPo = appPerformanceParamService.findOneAppPerformanceParamPo(performance, 10);
        if (paramPo == null) {
            throw new CommException(msgUtil.getMsg(AppMessage.PARAM_ERROR, "参数有误"));
        }
        BigDecimal assertE = amount.multiply(paramPo.getRate()).setScale(4, BigDecimal.ROUND_HALF_EVEN);
        BigDecimal am = amount.multiply(new BigDecimal("-1"));
        BigDecimal beforeEP = userPo.getEpBalance();
        BigDecimal afterEP = userPo.getEpBalance().subtract(amount).setScale(4, BigDecimal.ROUND_HALF_EVEN);
        BigDecimal beforeAssert = userPo.getAssets();
        BigDecimal afterAssert = userPo.getAssets().add(assertE).setScale(4, BigDecimal.ROUND_HALF_EVEN);
        Integer row = appUserMapper.updateEpBalanceById(am, userId);
        if (row == null || row < 1) {
            throw new Exception("更新失败");
        }
        row = 0;
        row = appUserMapper.updateAssetsById(assertE, userId);
        if (row == null || row < 1) {
            throw new Exception("更新失败");
        }
        appBillRecordService.saveBillRecord(bunessNo, userId, BusnessTypeEnum.EP_EXCHANGE.getCode(), CurrencyTypeEnum.EP_BALANCE.getCode(), am, beforeEP, afterEP, "EP兑换,EP减少", assertE.toString());
        appBillRecordService.saveBillRecord(bunessNo, userId, BusnessTypeEnum.EP_EXCHANGE.getCode(), CurrencyTypeEnum.E_ASSET.getCode(), assertE, beforeAssert, afterAssert, "EP兑换,资产增加", am.toString());
        appUserService.updatePerformance(userId, bunessNo, amount, PerformanceTypeEnum.EXCHANGE);
        return true;
    }

    /**
     * 用户签到
     *
     * @param releaseAmount
     * @param epAmount
     * @param birdScore
     * @param appUserPo
     * @throws Exception
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void openRedPacket(BigDecimal releaseAmount, BigDecimal epAmount, BigDecimal birdScore, AppUserPo appUserPo,BigDecimal shadowAmount) throws Exception {
        //计算用户释放前e资产数量
        BigDecimal beforeAssets = appUserPo.getAssets();
        //计算用户释放后e资产数量
        BigDecimal afterAssets = beforeAssets.subtract(releaseAmount);
        //计算用户释放前ep余额
        BigDecimal beforeEpAmount = appUserPo.getEpBalance();
        //计算用户释放后ep余额
        BigDecimal afterEpAmount = beforeEpAmount.add(epAmount);
        //计算用户释放前TFC
        BigDecimal beforeBirdScore = appUserPo.getBirdScore();
        //计算用户释放后TFC
        BigDecimal afterBirdScore = beforeBirdScore.add(birdScore);
        //计算用户释放影子积分
        BigDecimal beforeShadowAmount = appUserPo.getShadowScore();
        //计算用户释放后影子积分
        BigDecimal afterShadowAmount = beforeShadowAmount.subtract(shadowAmount);

        //一个业务产生所有流水都共用一个uuid,方便后端查询
        String uuid = ToolUtils.getUUID();

        //更新释放时间
        AppUserPo model = new AppUserPo();
        model.setReleaseTime(new Date());//更新释放时间
        appUserService.updateById(model, appUserPo.getId());

        //更新用户资产数量
        int rows = appUserMapper.updateAssetsById(releaseAmount.multiply(new BigDecimal(-1)), appUserPo.getId());
        if (rows < 0) {
            LogUtils.error("签到中，更新用户资产数量失败");
            throw new CommException("签到中，更新用户资产数量失败");
        }

        //更新用户影子积分数量
        rows = appUserMapper.updateShadowScoreById(shadowAmount.multiply(new BigDecimal(-1)), appUserPo.getId());
        if (rows < 0) {
            LogUtils.error("签到中，更新用户影子积分失败");
            throw new CommException("签到中，更新用户影子积分失败");
        }

        //增加用户资产流水
        appBillRecordService.saveBillRecord(uuid, appUserPo.getId(), BusnessTypeEnum.E_RELEASE.getCode(), CurrencyTypeEnum.E_ASSET.getCode(), releaseAmount.multiply(new BigDecimal(-1)), beforeAssets, afterAssets, "用户签到，释放" + releaseAmount + "资产", appUserPo.getNickName());

        //保存影子积分流水记录
        if (shadowAmount.doubleValue() > 0.1d) {
            appBillRecordService.saveBillRecord(uuid, appUserPo.getId(), BusnessTypeEnum.E_REWARD_SHADOWSCORE.getCode(), CurrencyTypeEnum.SHADOWSCORE.getCode(), shadowAmount.multiply(new BigDecimal(-1)), beforeShadowAmount, afterShadowAmount, "用户签到，释放" + shadowAmount + "影子积分", appUserPo.getNickName());
        }

        //更新用户Ep余额数量
        rows = appUserMapper.updateEpBalanceById(epAmount, appUserPo.getId());
        if (rows < 0) {
            LogUtils.error("签到中，更新用户EP余额数量失败");
            throw new CommException("签到中，更新用户EP余额数量失败");
        }
        //增加用户EP余额流水
        appBillRecordService.saveBillRecord(uuid, appUserPo.getId(), BusnessTypeEnum.E_RELEASE.getCode(), CurrencyTypeEnum.EP_BALANCE.getCode(), epAmount, beforeEpAmount, afterEpAmount, "用户签到，增加" + epAmount + "EP余额", appUserPo.getNickName());

        //更新用户TFC
        rows = appUserMapper.updateBirdScoreById(birdScore, appUserPo.getId());
        if (rows < 0) {
            LogUtils.error("签到中，更新用户TFC失败");
            throw new CommException("签到中，更新用户TFC失败");
        }
        //增加用户TFC流水
        appBillRecordService.saveBillRecord(uuid, appUserPo.getId(), BusnessTypeEnum.E_RELEASE.getCode(), CurrencyTypeEnum.BIRDSCORE.getCode(), birdScore, beforeBirdScore, afterBirdScore, "用户签到，增加" + birdScore + "TFC", appUserPo.getNickName());

        //计算用户当前推荐人的获取收益
        BigDecimal pushParam = new BigDecimal(0);
        BigDecimal rewardAmout = new BigDecimal(0);
        //BigDecimal beforeRewardAmount = new BigDecimal(0);
        //BigDecimal afterRewardAmount = new BigDecimal(0);
        BigDecimal beforeRewardShadowscore = new BigDecimal(0);
        BigDecimal afterRewardShadowscore = new BigDecimal(0);
        String pushRewardScale1 = commonService.findParameter("pushRewardScale1");
        String pushRewardScale2 = commonService.findParameter("pushRewardScale2");
        String pushRewardScale3 = commonService.findParameter("pushRewardScale3");
        String pushRewardScale4 = commonService.findParameter("pushRewardScale4");
        String pushRewardScale5 = commonService.findParameter("pushRewardScale5");
        AppUserContactPo appUserContactPo = appUserContactService.findUserByUserId (appUserPo.getId ());
        AppUserPo parentUser = appUserPo;
        AppUserContactPo parentContactPo = appUserContactPo;
        //计算赏金释放
        //BigDecimal releaseOnlyEpFromAsserts = epAmount.subtract(shadowAmount);
        //根据用户id查询上级用户
        for (int i = 0; i < 5; i++) {
            parentContactPo = appUserContactService.findUserByUserId (parentContactPo.getParentId ());
            parentUser = appUserMapper.findUserById (parentContactPo.getUserId ());
            if (parentUser == null) {
                //如果用户为空，跳出循环
                break;
            }
            if (parentUser.getUid() == 10000) {
                break;
            }
            //判断是第几代
            if (i == 0) {
                pushParam = new BigDecimal(pushRewardScale1);
            } else if (i == 1) {
                pushParam = new BigDecimal(pushRewardScale2);
            } else if (i == 2) {
                pushParam = new BigDecimal(pushRewardScale3);
            } else if (i == 3) {
                pushParam = new BigDecimal(pushRewardScale4);
            } else if (i == 4) {
                pushParam = new BigDecimal(pushRewardScale5);
            }

            //计算直推推荐人收益
            rewardAmout = epAmount.multiply (pushParam);
            //直推奖励，如果影子积分数量小于0.1，就不分奖励了
            if (new BigDecimal (0.1).compareTo (rewardAmout) > 0) {
                continue;
            }

            //计算推荐人收益前影子积分
            beforeRewardShadowscore = parentUser.getShadowScore();
            //计算推荐人收益后影子积分
            afterRewardShadowscore = parentUser.getShadowScore().add(rewardAmout);

            //计算推荐人收益前ep余额
            //beforeRewardAmount = parentUser.getEpBalance();
            //计算推荐人收益后ep余额
            //afterRewardAmount = parentUser.getEpBalance().add(rewardAmout);

            //更新用户Ep余额数量
//            rows = appUserMapper.updateEpBalanceById(rewardAmout,parentUser.getId());
//            if(rows < 0){
//                LogUtils.error("用户获取收益赏金中，更新用户EP余额数量失败");
//                throw new CommException("用户获取收益赏金中，更新用户EP余额数量失败");
//            }

            //增加当前用户的影子积分
            rows = appUserMapper.updateShadowScoreById(rewardAmout,parentUser.getId());
            if(rows < 0){
                LogUtils.error("用户获取收益赏金中，增加上级用户对等的影子积分失败");
                throw new CommException("用户获取收益赏金中，增加上级用户对等的影子积分失败");
            }

            //增加用户EP余额流水
            //appBillRecordService.saveBillRecord(uuid,parentUser.getId(),BusnessTypeEnum.E_REWARD.getCode(),CurrencyTypeEnum.EP_BALANCE.getCode(),rewardAmout,beforeRewardAmount,afterRewardAmount,"用户签到，当前用户第"+(i+1)+"代直推人获取"+rewardAmout+"Ep余额收益",parentUser.getNickName());

            //增加当前用户影子积分增加流水
            appBillRecordService.saveBillRecord(uuid,parentUser.getId(),BusnessTypeEnum.E_REWARD_SHADOWSCORE.getCode(),CurrencyTypeEnum.SHADOWSCORE.getCode(),rewardAmout,beforeRewardShadowscore,afterRewardShadowscore,"用户"+"【"+appUserPo.getUid()+"】"+"签到，当前用户第"+(i+1)+"代节点人增加"+rewardAmout+"影子积分",parentUser.getNickName());
        }
    }


}
