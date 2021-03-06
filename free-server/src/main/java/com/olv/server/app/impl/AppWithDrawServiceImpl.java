package com.olv.server.app.impl;

import com.olv.server.app.AppUserService;
import com.olv.server.app.AppWithDrawService;
import com.olv.server.common.CommonService;
import com.olv.server.mapper.AppWithDrawMapper;
import com.olv.common.enums.BusnessTypeEnum;
import com.olv.common.enums.CurrencyTypeEnum;
import com.olv.common.enums.WithDrawEnum;
import com.olv.common.po.AppUserPo;
import com.olv.common.po.AppWithDrawPo;
import com.olv.common.resp.Paging;
import com.olv.common.util.ToolUtils;
import com.olv.common.vo.app.DrawRecordVo;
import com.olv.server.app.AppBillRecordService;
import com.olv.server.mapper.AppUserMapper;
import org.apache.ibatis.session.RowBounds;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * 提现
 * Created by Administrator on 2018/1/4 0004.
 */
@Service
public class AppWithDrawServiceImpl implements AppWithDrawService {

    @Resource
    private AppUserService userService;
    @Resource
    private AppBillRecordService billRecordService;
    @Resource
    private CommonService commonService;

    @Resource
    private AppUserMapper appUserMapper;

    @Resource
    private AppWithDrawMapper withDrawMapper;

    @Override
    public Boolean save(AppWithDrawPo model) {
        model.setId(ToolUtils.getUUID());
        model.setState(WithDrawEnum.PENDING.getCode());
        model.setCreateTime(new Date());
        model.setUpdateTime(new Date());
        withDrawMapper.insertSelective(model);
        return true;
    }

    @Override
    @Transactional
    public Boolean epWithDraw(String userId, String bankId, BigDecimal amount) throws Exception {
        BigDecimal withdrawFee = new BigDecimal(commonService.findParameter("withdrawFee"));
        AppUserPo userPo = userService.findUserById(userId);
        appUserMapper.updateEpBalanceById(amount.multiply(new BigDecimal("-1")), userId);
        appUserMapper.updateEpBlockBalanceById(amount, userId);
        BigDecimal am=amount.multiply(new BigDecimal("-1")).setScale(4,BigDecimal.ROUND_HALF_EVEN);
        BigDecimal before=userPo.getEpBalance();
        BigDecimal fee=amount.multiply(withdrawFee).setScale(4,BigDecimal.ROUND_HALF_EVEN);
        BigDecimal after=userPo.getEpBalance().subtract(amount).setScale(4,BigDecimal.ROUND_HALF_EVEN);
        AppWithDrawPo model = new AppWithDrawPo();
        model.setFee(fee);
        model.setBeforeBalance(before);
        model.setAmount(amount);
        model.setUserId(userId);
        model.setBankCardId(bankId);
        this.save(model);
        billRecordService.saveBillRecord(ToolUtils.getUUID(),userId, BusnessTypeEnum.EP_WITHDRAWALS.getCode(), CurrencyTypeEnum.EP_BALANCE.getCode(),am,before,after,"用户提现",userPo.getNickName());
        return true;
    }


    /**
     * 查看用户交易流水记录总数
     */
    @Override
    public Integer drawRecordTotal(String userId) {
        Integer count =withDrawMapper.drawRecordTotal(userId);
        if (count==null){
            count=0;
        }
        return count;
    }


    /**
     * 查看用户交易流水记录
     */
    @Override
    public List<DrawRecordVo> drawRecordList(String userId, Paging paging) {
        RowBounds rowBounds = new RowBounds(paging.getPageNumber(), paging.getPageSize());
        List<DrawRecordVo> list = withDrawMapper.drawRecordList(userId, rowBounds);
        if (list==null|| CollectionUtils.isEmpty(list)){
            list= Collections.emptyList();
        }
        return list;
    }
}
