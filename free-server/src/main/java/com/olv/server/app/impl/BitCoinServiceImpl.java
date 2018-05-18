package com.olv.server.app.impl;


import com.olv.common.enums.BusnessTypeEnum;
import com.olv.common.enums.CurrencyTypeEnum;
import com.olv.common.exception.CommException;
import com.olv.common.language.AppMessage;
import com.olv.common.po.AppUserPo;
import com.olv.common.service.RedisService;
import com.olv.common.thread.ThreadPoolManager;
import com.olv.common.util.BitCoinUtil;
import com.olv.common.util.LanguageUtil;
import com.olv.common.util.LogUtils;
import com.olv.common.util.ToolUtils;
import com.olv.server.app.BitcoinService;
import com.olv.server.common.CommonService;
import com.olv.server.mapper.AppCountryCodeMapper;
import com.olv.server.mapper.AppUserMapper;
import net.sf.json.JSONArray;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.paradoxs.bitcoin.client.BitcoinClient;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;

/**
 * 数字货币业务层实现类
 *
 * @author qsy
 * @version V1.0
 * @date 2017年8月17日
 */
@Service
public class BitCoinServiceImpl implements BitcoinService {
    @Resource
    private BitCoinUtil bitCoinUtil;
    private BitcoinClient bitcoinClient;
    @Resource
    private CommonService commonService;
    @Resource
    private AppUserMapper userMapper;
    @Resource
    private AppCountryCodeMapper appCountryCodeMapper;
    @Resource
    private LanguageUtil languageUtil;
    @Resource
    private RedisService redisService;
    @Resource
    private HttpServletRequest request;

    @Override
    public String getAccountAddress(String accountName) {
        return getBitCoinClient().getAccountAddress(accountName);
    }

    @Override
    public BigDecimal loadBalance(String accountName) {
        return getBitCoinClient().getBalance(accountName);
    }


    @Override
    public Boolean validAddress(String address) {
        if (StringUtils.isEmpty(address) || address.length() < 32) {
            return false;
        }
        return this.getBitCoinClient().validateAddress(address).getIsValid();
    }

    /**
     * 创建访问钱包对象
     *
     * @return 钱包对象
     */

    @Override
    public BitcoinClient getBitCoinClient() {
        if (bitcoinClient == null) {
            return new BitcoinClient(bitCoinUtil.getHost(), bitCoinUtil.getLogin(), bitCoinUtil.getPassword(), bitCoinUtil.getPort());
        } else {
            return bitcoinClient;
        }
    }

    @Override
    public BigDecimal loadWalletBalance() {
        return getBitCoinClient().getBalance();
    }

    /**
     * 更新用户币种资产
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserCoin(String userid) throws Exception {

        //30秒更新一次
        String key = redisService.getString("getUserVirtualCoin_" + userid);
        if (!StringUtils.isEmpty(key)) {
            return;
        }
        redisService.putString("getUserVirtualCoin_" + userid, userid, 60);
        //开启多线程去更新,不影响用户体验
        ThreadPoolManager.exec(new Runnable() {
            @Override
            @Transactional(rollbackFor = Exception.class)
            public void run() {
                Integer uid = null;
                try {
                    //获取最新用户的信息
                    AppUserPo userPo = userMapper.findUserById(userid);
                    uid = userPo.getUid();
                    //更新用户钱包地址
                    if (org.springframework.util.StringUtils.isEmpty(userPo.getAddress())) {
                        try {
                            String address = getAccountAddress(userPo.getUid().toString());
                            if (!org.springframework.util.StringUtils.isEmpty(address)) {
                                userMapper.updateAddress(address, userPo.getId());
                            }
                        } catch (Exception e) {
                            LogUtils.error("获取钱包地址失败", e);
                        }
                    }
                    //从钱包服务器获取用户的TFC数额
                    BigDecimal balance = loadBalance(userPo.getUid().toString());
                    //只要与钱包服务器的数量大于0,就更新数据库的瑞波币
                    if (balance.compareTo(new BigDecimal(0.01)) == 1) {
                        int rows = userMapper.updateBirdScoreById(balance, userid);
                        commonService.saveBillRecord(userPo.getId(), balance, BusnessTypeEnum.COIN_SYNCHRO.getCode(), CurrencyTypeEnum.BIRDSCORE.getCode(), userPo.getBirdScore(), userPo.getBirdScore().add(balance), "同步钱包服务器", ToolUtils.getUUID());
                        //将当前帐号的钱包余额扣除（转到10000账号上）
                        if (!getBitCoinClient().move(userPo.getUid().toString(), "10000", balance, 1, "")) {
                            throw new CommException(languageUtil.getMsg(AppMessage.COIN_DEBIT_FAIL, "钱包扣款失败"));
                        }
                        LogUtils.info("用户:" + uid + "更新TFC成功！此次更新额度：" + balance);
                    }
                } catch (Exception ex) {
                    //记录用户更新瑞波币数额的错误信息
                    LogUtils.error("用户：" + uid + "更新TFC额度失败", ex);
                }
            }
        });
    }

    public static void main(String[] args) {
        JSONArray parameters = new JSONArray().element("aaaa").element (10);
        System.out.println (parameters.toString ());
    }

}
