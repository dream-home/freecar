package com.olv.server.app.impl;

import com.olv.common.enums.*;
import com.olv.common.exception.CommException;
import com.olv.common.language.AppMessage;
import com.olv.common.po.AppPerformanceRecordPo;
import com.olv.common.po.AppUserContactPo;
import com.olv.common.po.AppUserPo;
import com.olv.common.service.RedisService;
import com.olv.common.util.ConfUtils;
import com.olv.common.util.CryptUtils;
import com.olv.common.util.LanguageUtil;
import com.olv.common.util.ToolUtils;
import com.olv.common.vo.app.ActiveVo;
import com.olv.common.vo.app.UserInfoVo;
import com.olv.common.vo.app.UserVo;
import com.olv.server.app.AppBillRecordService;
import com.olv.server.app.AppPerformanceRecordService;
import com.olv.server.app.AppUserService;
import com.olv.server.app.BitcoinService;
import com.olv.server.common.CommonService;
import com.olv.server.mapper.AppUserContactMapper;
import com.olv.server.mapper.AppUserMapper;
import com.olv.server.mapper.SysKeyWordsMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.apache.ibatis.ognl.DynamicSubscript.first;

/**
 * Created by Administrator on 2018/1/4 0004.
 */
@Service
public class AppUserServiceImpl implements AppUserService {
    @Resource
    private LanguageUtil msgUtil;
    @Resource
    private AppUserMapper appUserMapper;
    @Resource
    private ConfUtils confUtils;
    @Resource
    private SysKeyWordsMapper sysKeyWordsMapper;
    @Resource
    private RedisService redisService;
    @Resource
    private AppUserContactMapper appUserContactMapper;
    @Resource
    private AppBillRecordService billRecordService;
    @Resource
    private CommonService commonService;

    @Resource
    private AppPerformanceRecordService performanceRecordService;
    @Resource
    private BitcoinService bitcoinService;

    @Override
    public AppUserPo findUserById(String id) throws Exception {
        return appUserMapper.findUserById (id);
    }

    @Override
    public AppUserPo getUserByToken(String token) throws Exception {
        AppUserPo user = null;
        Object obj = redisService.getObj (token);
        if (obj != null && obj instanceof AppUserPo) {
            user = (AppUserPo) obj;
        }
        return user;
    }


    @Override
    public AppUserPo findUserByMobile(String mobile) throws Exception {
        return appUserMapper.findUserByMobile (mobile);
    }

    @Override
    public AppUserPo findUserByNickName(String nickName) throws Exception {
        return appUserMapper.findUserByNickName (nickName);
    }

    /**
     * 是否含有关键词
     *
     * @param nickName 昵称
     * @return >0 表示含有关键词汇
     */
    @Override
    public int findKeyWords(String nickName) {
        return sysKeyWordsMapper.findKeyWords (nickName);
    }

    /**
     * 注册用户业务
     *
     * @param userVo
     * @return
     * @throws Exception
     */
    @Transactional
    @Override
    public Boolean add(UserVo userVo) throws Exception {

        AppUserPo appUserPo = new AppUserPo ();
        //获取盐
        String pwdStal = ToolUtils.getUUID ();
        String payStal = ToolUtils.getUUID ();
        //设置登录密码
        String loginPw = CryptUtils.hmacSHA1Encrypt (userVo.getLoginPwd (), pwdStal);
        //设置支付密码
        String payPwd = CryptUtils.hmacSHA1Encrypt (userVo.getPayPwd (), payStal);

        appUserPo.setMobile (userVo.getMobile ());
        appUserPo.setNickName (userVo.getNickName ());
        appUserPo.setParentId (userVo.getParentId ());
        appUserPo.setLoginPwd (loginPw);
        appUserPo.setPayPwd (payPwd);
        appUserPo.setPwdStal (pwdStal);
        appUserPo.setPayStal (payStal);
        appUserPo.setId (ToolUtils.getUUID ());
        appUserPo.setCreateTime (new Date ());
        appUserPo.setState (StateEnum.NO_ACTIVE.getCode ());
        appUserPo.setLevel (userVo.getLevel ());
        appUserPo.setEpBalance (new BigDecimal (0));
        appUserPo.setBlockedEpBalance (new BigDecimal (0));
        appUserPo.setBirdScore (new BigDecimal (0));
        appUserPo.setAssets (new BigDecimal (0));
        appUserPo.setAreaNum (userVo.getAreaNum ().trim ());
        appUserPo.setActiveNo (0);
        appUserPo.setState (10);
        appUserPo.setIsAllowed (20);
        appUserPo.setShadowScore (BigDecimal.ZERO);


        //生成接点人信息
        AppUserContactPo appUserContactPo = new AppUserContactPo ();
        appUserContactPo.setId (ToolUtils.getUUID ());
        appUserContactPo.setCurrentArea (userVo.getCurrentArea ());
        AppUserContactPo appUserContactParent = appUserContactMapper.findUserByUserId (userVo.getContactId ());
        if (appUserContactParent == null) {
            throw new CommException ("接点人不存在");
        }
        appUserContactPo.setLevel (appUserContactParent.getLevel ().intValue () + 1);
        appUserContactPo.setParentId (appUserContactParent.getUserId ());
        appUserContactPo.setUserId (appUserPo.getId ());
        appUserContactPo.setPerformanceA (new BigDecimal (0));
        appUserContactPo.setPerformanceB (new BigDecimal (0));

        int count = appUserContactMapper.insert (appUserContactPo);
        if (count < 1) {
            throw new CommException ("保存注册用户接点人信息失败");
        }

        count = appUserMapper.insert (appUserPo);

        if (count > 0) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public String login(AppUserPo appUserPo) throws Exception {

        //获取token
        String token = RedisKeyEnum.TOKEN_API.getKey () + ToolUtils.getUUID ();
        // 登录成功,将用户信息存储到redis中
        String msg = redisService.putObj (token, appUserPo, confUtils.getSessionTimeout ());
        if (!msg.equalsIgnoreCase ("ok")) {
            // 缓存用户信息失败
            throw new CommException ("设置token到redis失败");
        } else {
            //redis是否存在
            String token_key = redisService.getString (appUserPo.getId ());
            if (!StringUtils.isEmpty (token_key)) {
                //删除token_key值
                redisService.del (token_key);
            }
            //用另外一个redis去保存token_key,保证用户登录只有一个token_key
            redisService.putString (appUserPo.getId (), token, confUtils.getSessionTimeout ());
            //登录成功，修改登录时间
            AppUserPo model = new AppUserPo ();
            model.setLoginTime (new Date ());
            appUserMapper.updateById (model, appUserPo.getId ());

            //更新用户币种业务
            bitcoinService.updateUserCoin (appUserPo.getId ());

            return token;
        }
    }

    @Override
    @Transactional
    public int delUser(String userId) throws Exception {
        if (StringUtils.isEmpty (userId)) {
            return 0;
        }
        AppUserContactPo model = new AppUserContactPo ();
        model.setUserId (userId);
        int rows = appUserContactMapper.delete (model);
        if (rows < 1) {
            throw new CommException (msgUtil.getMsg (AppMessage.DELUSER_FAILURE, "删除用户失败"));
        }
        rows = appUserMapper.deleteByPrimaryKey (userId);
        if (rows < 1) {
            throw new CommException (msgUtil.getMsg (AppMessage.DELUSER_FAILURE, "删除用户失败"));
        }
        return rows;
    }

    /**
     * 根据用户id修改用户信息
     *
     * @param userPo
     * @param userId
     * @return
     * @throws Exception
     */
    @Override
    public int updateById(AppUserPo userPo, String userId) throws Exception {
        return appUserMapper.updateById (userPo, userId);
    }

    @Override
    @Transactional
    public Boolean active(AppUserPo userPo, ActiveVo vo) throws Exception {
        String uuid = ToolUtils.getUUID ();

        AppUserPo activeUserPo = this.findUserById (vo.getActiveUserId ());
        if (activeUserPo == null) {
            throw new CommException (msgUtil.getMsg (AppMessage.USER_INVALID, "用户信息异常"));
        }

        String activeNoLimit = commonService.findParameter ("activeNoLimit");
        if (StringUtils.isEmpty (activeNoLimit)) {
            throw new CommException (msgUtil.getMsg (AppMessage.PARAM_ERROR, "参数有误"));
        }
        Integer activeNoLimitInt = Integer.valueOf (activeNoLimit);
        Integer count = null;
        Integer usedInt = 0;
        if (activeNoLimitInt > 0) {
            count = appUserMapper.updateActiveNoById (userPo.getId (), -activeNoLimitInt);
            if (count == null || count < 1) {
                throw new CommException (msgUtil.getMsg (AppMessage.ACTIVENO_NOT_ENOUGH, "激活次数不够"));
            }
            usedInt = -activeNoLimitInt;
        }
        billRecordService.saveBillRecord (uuid, userPo.getId (), BusnessTypeEnum.ACCOUNT_ACTIVE.getCode (), CurrencyTypeEnum.ACTIVATION_TIMES.getCode (), new BigDecimal (usedInt), new BigDecimal (userPo.getActiveNo ()), new BigDecimal ((userPo.getActiveNo () - activeNoLimitInt)), "为" + vo.getMobile () + "【" + activeUserPo.getUid () + "】" + "激活账号", vo.getMobile ());
        appUserMapper.updateUserStateById (vo.getActiveUserId (), StateEnum.NORMAL.getCode ());
        String activeAddE = commonService.findParameter ("activeAddE");
        String activeAddPerformance = commonService.findParameter ("activeAddPerformance");
        BigDecimal activeAddEAmount = new BigDecimal (activeAddE);
        BigDecimal activeAddactiveAddPerformanceEAmount = new BigDecimal (activeAddPerformance);
        if (BigDecimal.ZERO.compareTo (activeAddEAmount) == -1) {
            appUserMapper.updateAssetsById (new BigDecimal (activeAddE), activeUserPo.getId ());
            BigDecimal beforActiveAddEAmount = activeUserPo.getAssets ();
            BigDecimal afterActiveAddEAmount = activeUserPo.getAssets ().add (activeAddEAmount).setScale (4, BigDecimal.ROUND_HALF_EVEN);
            billRecordService.saveBillRecord (uuid, activeUserPo.getId (), BusnessTypeEnum.E_ASSET_ACTIVE.getCode (), CurrencyTypeEnum.E_ASSET.getCode (), activeAddEAmount, beforActiveAddEAmount, afterActiveAddEAmount, "激活增加资产", "");
        }
        if (BigDecimal.ZERO.compareTo (activeAddactiveAddPerformanceEAmount) == -1) {
            updatePerformance (vo.getActiveUserId (), uuid, activeAddactiveAddPerformanceEAmount, PerformanceTypeEnum.ACTIVE);
        }
        return true;
    }

    /**
     * 激活
     *
     * @param userId 被激活人的userId
     * @param uuid
     */
    @Override
    public void updatePerformance(String userId, String uuid, BigDecimal amount, PerformanceTypeEnum performanceTypeEnum) {
        String level = commonService.findParameter ("levelNo");
        Integer levelNo = Integer.valueOf (level);
        List<String> alist = new ArrayList<> ();
        List<String> blist = new ArrayList<> ();
        List<AppPerformanceRecordPo> poList = new ArrayList<> ();
        AppUserContactPo contactPo = null;
        AppUserContactPo contactParentPo = null;
        for (int i = 0; i < levelNo; i++) {
            AppPerformanceRecordPo recordPo = new AppPerformanceRecordPo ();
            if (i == 0) {
                contactPo = appUserContactMapper.findUserByUserId (userId);
            }

            if (org.apache.commons.lang3.StringUtils.isEmpty (contactPo.getParentId ())) {
                break;
            }
            contactParentPo = appUserContactMapper.findUserByUserId (contactPo.getParentId ());
            if (contactParentPo == null) {
                break;
            }
            if (AreaEnum.DEPARTMENT_A.getCode ().equals (contactPo.getCurrentArea ())) {
                alist.add (contactParentPo.getId ());
                recordPo.setDepartment (AreaEnum.DEPARTMENT_A.getCode ());
                poList.add (recordPo);
            } else if (AreaEnum.DEPARTMENT_B.getCode ().equals (contactPo.getCurrentArea ())) {
                blist.add (contactParentPo.getId ());
                recordPo.setDepartment (AreaEnum.DEPARTMENT_B.getCode ());
                poList.add (recordPo);
            }
            recordPo.setId (ToolUtils.getUUID ());
            recordPo.setOrderId (uuid);
            recordPo.setAmount (amount);
            recordPo.setUserId (contactParentPo.getUserId ());
            recordPo.setCreateTime (new Date ());
            recordPo.setType (performanceTypeEnum.getCode ());
            contactPo = appUserContactMapper.findUserByUserId (contactPo.getParentId ());
        }
        if (!CollectionUtils.isEmpty (alist)) {
            appUserContactMapper.updatePerformanceListA (alist, amount);
        }
        if (!CollectionUtils.isEmpty (blist)) {
            appUserContactMapper.updatePerformanceListB (blist, amount);
        }
        if (!CollectionUtils.isEmpty (poList)) {
            performanceRecordService.insertPerformanceRecordList (poList);
        }
    }

    @Override
    public List<UserInfoVo> findUserByContactParentId(String partentId) {
        List<UserInfoVo> list = appUserMapper.findUserByContactParentId (partentId);
        if (list == null) {
            list = Collections.emptyList ();
        }
        return list;
    }

    @Override
    public UserInfoVo findUserByContactUserId(String userId) {
        return appUserMapper.findUserByContactUserId (userId);
    }


    @Override
    public AppUserPo findUserByUid(String uid) throws Exception {
        if (StringUtils.isEmpty (uid) || uid.length () >= 32) {
            return null;
        }
        return appUserMapper.findUserByUid (uid);
    }

    @Override
    public AppUserPo findUserByAddress(String address) throws Exception {
        if (StringUtils.isEmpty (address) || address.length () < 32) {
            return null;
        }
        return appUserMapper.findUserByAddress (address);
    }


    @Override
    public int updateAddress(String address, String id) throws Exception {
        if (StringUtils.isEmpty (address) || StringUtils.isEmpty (id)) {
            return 0;
        }
        return appUserMapper.updateAddress (address, id);
    }

    /**
     * 不在一棵树下返回false
     * @param loginUserId
     * @param concatUserId
     * @return
     * @throws Exception
     */
    @Override
    public Boolean underTree(String loginUserId, String concatUserId) throws Exception {
        UserInfoVo loginUserContact=this.findUserByContactUserId (loginUserId);
        UserInfoVo first=this.findUserByContactUserId (concatUserId);
        //非root || 查询以上层次，返回false
        Boolean flag= (first.getLevel ()==loginUserContact.getLevel () && !loginUserContact.getMobile ().equals (first.getMobile ())) || first.getLevel ()<loginUserContact.getLevel ();
        if (flag){
            return false;
        }
        UserInfoVo parent=first;
        int levelNo=9;
        int no=first.getLevel ()-loginUserContact.getLevel ();
        String levelNoStr =commonService.findParameter ("levelQueryNo");
        if (!StringUtils.isEmpty (levelNoStr)){
            levelNo=Integer.valueOf (levelNoStr);
        }
        if (no>levelNo || no<0){
            return false;
        }
        for (int i=0;i<levelNo+1;i++){
            if (parent==null){
                return false;
            }
            if (parent.getLevel ()==loginUserContact.getLevel () && parent.getId ().equals (loginUserContact.getId ())){
                return true;
            }
            parent=this.findUserByContactUserId (parent.getParentId ());
        }
        return false;
    }
}
