package com.olv.app.controller;

import com.olv.common.annotation.SystemControllerLog;
import com.olv.common.enums.RespCodeEnum;
import com.olv.common.language.AppMessage;
import com.olv.common.po.AppUserPo;
import com.olv.common.resp.RespBody;
import com.olv.common.util.LanguageUtil;
import com.olv.common.util.LogUtils;
import com.olv.common.vo.app.UserInfoVo;
import com.olv.server.app.AppUserService;
import com.olv.server.common.CommonService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 查询树形结构
 */
@RestController
@RequestMapping("/tree")
public class TreeController {
    @Resource
    private CommonService commonService;
    @Resource
    private AppUserService appUserService;
    @Resource
    private LanguageUtil msgUtil;

    @GetMapping("/info")
    @SystemControllerLog(description = "查询接点网络")
    public RespBody info(String mobile) {
        RespBody respBody = new RespBody ();
        try {
            AppUserPo userPo = commonService.checkToken ();
            UserInfoVo loginUserConcatPo = appUserService.findUserByContactUserId (userPo.getId ());
            if (StringUtils.isEmpty (mobile)) {
                respBody.add (RespCodeEnum.ERROR.getCode (), msgUtil.getMsg (AppMessage.PHONE_NOT_NULL, "手机号不能为空"));
                return respBody;
            }
            AppUserPo userMobilePo = appUserService.findUserByMobile (mobile);
            if (userMobilePo == null) {
                respBody.add (RespCodeEnum.ERROR.getCode (), msgUtil.getMsg (AppMessage.USER_INVALID, "用户不存在"));
                return respBody;
            }
            List<UserInfoVo> resUsers = new ArrayList<> ();
            UserInfoVo first = appUserService.findUserByContactUserId (userMobilePo.getId ());
            mobile = mobile.trim ();
            Boolean flag = true;
            if (!userPo.getMobile ().equals (mobile)) {
                flag = appUserService.underTree (userPo.getId (), userMobilePo.getId ());
            }
            if (!flag) {
                respBody.add (RespCodeEnum.ERROR.getCode (), msgUtil.getMsg (AppMessage.INSUFFICIENT_PERMISSIONS, "查询权限不够"));
                return respBody;
            }
            //root
            resUsers.add (first);
            List<UserInfoVo> userInfoVoList = appUserService.findUserByContactParentId (userMobilePo.getId ());
            if (!CollectionUtils.isEmpty (userInfoVoList)) {
                for (UserInfoVo userInfoVo : userInfoVoList) {
                    //第二层
                    resUsers.add (userInfoVo);
                    List<UserInfoVo> threeLists = appUserService.findUserByContactParentId (userInfoVo.getId ());
                    if (!CollectionUtils.isEmpty (threeLists)) {
                        for (UserInfoVo threeVo : threeLists) {
                            //第三层
                            resUsers.add (threeVo);
                        }
                    }
                }
            }
            respBody.add (RespCodeEnum.SUCCESS.getCode (), "", resUsers);
        } catch (Exception ex) {
            respBody.add (RespCodeEnum.ERROR.getCode (), msgUtil.getMsg (AppMessage.QUERY_ERROR, "查询失败"));
            LogUtils.error ("查询失败！", ex);
        }
        return respBody;
    }
}
