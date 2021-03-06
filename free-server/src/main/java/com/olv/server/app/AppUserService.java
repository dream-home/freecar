package com.olv.server.app;

import com.olv.common.enums.PerformanceTypeEnum;
import com.olv.common.po.AppUserPo;
import com.olv.common.vo.app.ActiveVo;
import com.olv.common.vo.app.UserInfoVo;
import com.olv.common.vo.app.UserVo;

import java.math.BigDecimal;
import java.util.List;

/**
 * 用户相关业务
 * Created by Administrator on 2018/1/4 0004.
 */
public interface AppUserService {


    /**
     * 根据用户id查询用户
     * @param id
     * @return
     * @throws Exception
     */
    public AppUserPo findUserById(String id) throws Exception;

    /**
     * 根据token查询用户信息
     * @param token
     * @return
     * @throws Exception
     */
    public AppUserPo getUserByToken(String token) throws Exception;

    /**
     * 根据手机号查询用户
     *
     * @param mobile
     * @return
     * @throws Exception
     */
    public AppUserPo findUserByMobile(String mobile) throws Exception;

    /**
     * 根据address查询用户
     *
     * @param address
     * @return
     * @throws Exception
     */
    public AppUserPo findUserByAddress(String address) throws Exception;

    /**
     * 根据昵称查询用户
     *
     * @param nickName
     * @return
     * @throws Exception
     */
    public AppUserPo findUserByNickName(String nickName) throws Exception;

    /**
     * 根据UID查询用户
     *
     * @param uid
     * @return
     * @throws Exception
     */
    public AppUserPo findUserByUid(String uid) throws Exception;

    /**
     * 是否是关键词汇
     * */
    int findKeyWords(String nickName);

    /**
     * 注册用户
     *
     * @param userVo
     * @throws Exception
     */
    public Boolean add(UserVo userVo) throws Exception;

    /**
     * 用户登录
     *
     * @param appUserPo
     * @throws Exception
     */
    public String login(AppUserPo appUserPo) throws Exception;


    /**
     * 激活用户
     * @param userPo
     * @param vo
     * @return
     * @throws Exception
     */
    public Boolean active(AppUserPo userPo,ActiveVo vo) throws Exception;

    /**
     * 删除用户
     */
    public int delUser(String userId) throws Exception;


    public List<UserInfoVo> findUserByContactParentId(String partentId);

    public UserInfoVo findUserByContactUserId(String userId);

    /**
     * 根据用户id修改用户信息
     * @param userPo
     * @param userId
     * @return
     * @throws Exception
     */
    public int updateById(AppUserPo userPo, String userId) throws Exception;


    void updatePerformance(String userId , String uuid, BigDecimal amount, PerformanceTypeEnum performanceTypeEnum) throws Exception;

    int updateAddress(String address,  String id)  throws Exception;

    Boolean  underTree(String loginUserId,String concatUserId)  throws Exception;
}
