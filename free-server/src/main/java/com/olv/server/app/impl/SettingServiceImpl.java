package com.olv.server.app.impl;

import com.olv.common.enums.BankEnum;
import com.olv.common.enums.YNEnum;
import com.olv.common.exception.CommException;
import com.olv.common.language.AppMessage;
import com.olv.common.po.*;
import com.olv.common.resp.Paging;
import com.olv.common.util.LanguageUtil;
import com.olv.common.util.MyBeanUtils;
import com.olv.common.util.ToolUtils;
import com.olv.common.vo.app.BankCardVo;
import com.olv.server.app.SettingService;
import com.olv.server.common.CommonService;
import com.olv.server.mapper.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.RowBounds;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;

/**
 * 设置业务层接口
 *
 * @version v1.0
 * @date 2017年8月18日
 */
@Service
public class SettingServiceImpl implements SettingService {

    @Resource
    private AppBankCardMapper bankCardMapper;

    @Resource
    private AppUserMessageMapper appUserMessageMapper;

    @Resource
    private SysNoticeMapper sysNoticeMapper;
    @Resource
    private AppUserMapper userMapper;
    @Resource
    private UseImageMapper imageMapper;
    @Resource
    private AppBankTypeMapper bankTypeMapper;
    @Resource
    private LanguageUtil msgUtil;
    @Resource
    private CommonService commonService;

    /**
     * 新增|修改银行卡
     *
     * @param bank   银行卡信息
     * @param action true 新增 false 修改
     * @return
     * @throws Exception
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void callBankCard(BankCardVo bank, boolean action) throws Exception {
        //重新生成一个Po类
        AppBankCardPo model = MyBeanUtils.copyProperties(bank, AppBankCardPo.class);
        //查找用户已绑定的银行卡数量
        int count = this.getUserCardCount(bank.getUserId());
        //若无绑定过则新增此银行卡位默认银行卡
        if (count == 0) {
            model.setDefaultState(BankEnum.NORMAL.getCode());
            model.setState(YNEnum.YES.getCode());
        } else {
            //若设置为默认银行卡，则其他已绑定的银行统统改成非默认
            if (BankEnum.NORMAL.getCode().toString().equals(bank.getDefaultState())) {
                bankCardMapper.updUserDefualt(bank.getUserId());
                model.setDefaultState(BankEnum.NORMAL.getCode());
            }
        }
        //新增银行卡
        if (action) {
            model.setId(ToolUtils.getUUID());
            model.setState(YNEnum.YES.getCode());
            //调用dao保存数据
            bankCardMapper.insert(model);
            //获取用户信息
            AppUserPo userPo = userMapper.selectByPrimaryKey(model.getUserId());
            //第一次新增银行卡时用户要保存持卡人姓名
            if (StringUtils.isEmpty(userPo.getName())) {
                AppUserPo userModel = new AppUserPo();
                userModel.setName(model.getName());
                userMapper.updateById(userModel, model.getUserId());
            }
        } else { //修改银行卡
            //查找默认银行卡
            AppBankCardPo bankCard = this.findUserDefualtCard(bank.getUserId());
            //若正在修改默认银行指定为不默认，则强制为默认银行卡
            if (bankCard != null && bankCard.getId().equals(bank.getId()) && BankEnum.DISABLE.getCode().toString().equals(bank.getDefaultState())) {
                model.setDefaultState(BankEnum.NORMAL.getCode());
            }
            //调用dao保存数据
            model.setState(YNEnum.YES.getCode());
            bankCardMapper.updateByPrimaryKey(model);
        }
    }

    /**
     * 设置默认银行银行卡
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void defaultBankCard(String bankId, String userId) throws Exception {

        //修改所有银行卡为不默认
        bankCardMapper.updUserDefualt(userId);
        //设置默认银行卡
        AppBankCardPo model = new AppBankCardPo();
        model.setId(bankId);
        model.setDefaultState(BankEnum.NORMAL.getCode());
        model.setState(YNEnum.YES.getCode());
        bankCardMapper.updateByPrimaryKeySelective(model);
    }

    /**
     * 查找用户绑定的银行卡数量
     */
    private int getUserCardCount(String userId) throws Exception {
        AppBankCardPo model = new AppBankCardPo();
        model.setUserId(userId);
        model.setDefaultState(BankEnum.NORMAL.getCode());
        model.setState(YNEnum.YES.getCode());
        return bankCardMapper.selectCount(model);
    }

    /**
     * 查询用户所有银行卡信息
     */
    @Override
    public List<AppBankCardPo> findAll(String userId) throws Exception {
        AppBankCardPo model = new AppBankCardPo();
        model.setUserId(userId);
        model.setState(YNEnum.YES.getCode());
        return bankCardMapper.select(model);
    }

    /**
     * 查询用户默认银行卡
     */
    @Override
    public AppBankCardPo findUserDefualtCard(String userId) throws Exception {
        AppBankCardPo model = new AppBankCardPo();
        model.setUserId(userId);
        model.setDefaultState(BankEnum.NORMAL.getCode());
        model.setState(YNEnum.YES.getCode());
        return bankCardMapper.selectOne(model);
    }

    @Override
    @Transactional
    public int deleteBankCardById(String cardId) throws Exception {
        Integer count = 0;
        Boolean isUpdate = false;
        if (StringUtils.isEmpty(cardId)) {
            return 0;
        }
        AppBankCardPo delCard = bankCardMapper.selectByPrimaryKey(cardId);
        if (delCard == null) {
            return 0;
        }
        if (BankEnum.NORMAL.getCode().toString().equals(delCard.getDefaultState())) {
            isUpdate = true;
        }
        AppBankCardPo updateModel = MyBeanUtils.copyProperties(delCard, AppBankCardPo.class);
        updateModel.setState(YNEnum.NO.getCode());
        bankCardMapper.updateByPrimaryKey(updateModel);
//        count = bankCardMapper.delete(model);
        if (count < 0) {
            throw new CommException(msgUtil.getMsg(AppMessage.DEL_FAILURE, "删除失败"));
        }
        if (isUpdate) {
            AppBankCardPo selectModel = new AppBankCardPo();
            selectModel.setDefaultState(BankEnum.DISABLE.getCode());
            selectModel.setUserId(commonService.findAppUser().getId());
            selectModel.setState(YNEnum.YES.getCode());
            List<AppBankCardPo> defaultCardList = bankCardMapper.select(selectModel);
            if (defaultCardList != null && !CollectionUtils.isEmpty(defaultCardList)) {
                AppBankCardPo defaultCard = defaultCardList.get(0);
                if (defaultCard != null) {
                    AppBankCardPo updateModel1 = MyBeanUtils.copyProperties(defaultCard, AppBankCardPo.class);
                    updateModel1.setDefaultState(BankEnum.NORMAL.getCode());
                    bankCardMapper.updateByPrimaryKey(updateModel1);
                }
            }

        }
        return count;
    }

    /**
     * 删除用户银行卡
     *
     * @param ids
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteBankCard(List<AppBankCardPo> ids) throws Exception {
        int count = 0;
        for (AppBankCardPo po : ids) {
            AppBankCardPo model = new AppBankCardPo();
            model.setId(po.getId());
            //是否绑定另一张银行卡位默认
            defaultBankCard(po.getId());
            count += bankCardMapper.delete(model);
        }
        return count;
    }

    /**
     * 删除默认银行卡后需重新指定一张新卡为默认银行卡
     */
    private int defaultBankCard(String bankId) throws Exception {
        //根据ID查询要删除的银行卡是否是默认银行卡
        AppBankCardPo bankCardPo = bankCardMapper.selectByPrimaryKey(bankId);
        if (BankEnum.NORMAL.getCode().equals(bankCardPo.getDefaultState())) {
            //查询一张用户剩余的银行卡
            AppBankCardPo model = new AppBankCardPo();
            model.setDefaultState(BankEnum.DISABLE.getCode());
            model.setUserId(commonService.findAppUser().getId());
            AppBankCardPo bank = bankCardMapper.selectOne(model);
            //若有则更改为默认银行卡
            if (bank != null) {
                AppBankCardPo updateModel = MyBeanUtils.copyProperties(bank, AppBankCardPo.class);
                updateModel.setDefaultState(BankEnum.NORMAL.getCode());
                bankCardMapper.updateByPrimaryKey(updateModel);
            }
        }
        return 0;
    }

    /**
     * 获取开户银行信息
     */
    @Override
    public AppBankTypePo findBankType(String typeId) throws Exception {
        return bankTypeMapper.selectByPrimaryKey(typeId);
    }

    /**
     * 获取所有开户银行类型
     */
    @Override
    public List<AppBankTypePo> typeList() throws Exception {
        return bankTypeMapper.selectAll();
    }


    /**
     * 个人信息总数
     */
    @Override
    public int msgCount() throws Exception {
        AppUserMessagePo model = new AppUserMessagePo();
        model.setUserId(commonService.findAppUser().getId());
        return appUserMessageMapper.selectCount(model);
    }

    /**
     * 获取个人消息
     */
    @Override
    public List<AppUserMessagePo> msgList(Paging paging) throws Exception {
        AppUserMessagePo model = new AppUserMessagePo();
        model.setUserId(commonService.findAppUser().getId());
        return appUserMessageMapper.msgList(model, new RowBounds(paging.getPageNumber(), paging.getPageSize()));
    }

    /**
     * 公告消息总数
     */
    @Override
    public int noticeCount() throws Exception {
        return sysNoticeMapper.noticeCount();
    }

    /**
     * 公告消息列表
     */
    @Override
    public List<SysNoticePo> noticeList(Paging paging) {
        return sysNoticeMapper.noticeList(new RowBounds(paging.getPageNumber(), paging.getPageSize()));
    }

    /**
     * 批量删除个人消息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int delMessage(List<AppUserMessagePo> ids) {
        int count = 0;
        for (AppUserMessagePo po : ids) {
            AppUserMessagePo model = new AppUserMessagePo();
            model.setId(po.getId());
            count += appUserMessageMapper.delete(model);
        }
        return count;
    }

    /**
     * 未读的新消息
     */
    @Override
    public int newMsg() throws Exception {
        AppUserMessagePo model = new AppUserMessagePo();
        model.setUserId(commonService.findAppUser().getId());
        model.setState(YNEnum.YES.getCode());//未读
        int total = appUserMessageMapper.selectCount(model);
        return total;
    }

    /**
     * 已读消息
     */
    @Override
    public AppUserMessagePo readMsg(String id) throws Exception {
        AppUserMessagePo model = appUserMessageMapper.selectByPrimaryKey(id);
        model.setState(YNEnum.NO.getCode());//已读
        appUserMessageMapper.updateByPrimaryKey(model);
        return model;
    }


}
