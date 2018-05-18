package com.olv.server.app.impl;

import com.olv.server.app.AppBankCardService;
import com.olv.server.mapper.AppBankCardMapper;
import com.olv.common.vo.app.BankCardVo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 银行卡相关业务
 * Created by Administrator on 2018/1/4 0004.
 */
@Service
public class AppBankCardServiceImpl implements AppBankCardService {

    @Resource
    private AppBankCardMapper appBankCardMapper;
    @Override
    public BankCardVo findKey(String id) {
        return appBankCardMapper.findKey(id);
    }
}
