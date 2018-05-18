package com.olv.app.controller;

import com.olv.common.annotation.SystemControllerLog;
import com.olv.common.enums.RespCodeEnum;
import com.olv.common.exception.CommException;
import com.olv.common.language.AppMessage;
import com.olv.common.po.AppUserMessagePo;
import com.olv.common.po.AppUserPo;
import com.olv.common.po.SysNoticePo;
import com.olv.common.resp.Paging;
import com.olv.common.resp.RespBody;
import com.olv.common.util.LanguageUtil;
import com.olv.common.util.LogUtils;
import com.olv.server.app.SettingService;
import com.olv.server.common.CommonService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 消息
 */
@RestController
@RequestMapping("/message")
public class MessageController {
    @Resource
    private CommonService commonService;
    @Resource
    private LanguageUtil msgUtil;
    @Resource
    private LanguageUtil languageUtil;
    @Resource
    private SettingService settingService;


    /**
     * 获取用户个人消息列表
     *
     * @return 响应对象
     */
    @GetMapping("/list")
    public RespBody message(Paging paging) {
        // 创建返回对象
        RespBody respBody = new RespBody();
        try {
            int total = settingService.msgCount();
            List<AppUserMessagePo> list = null;
            if (total > 0) {
                list = settingService.msgList(paging);
            }
            paging.setTotalCount(total);
            respBody.setPage(paging);
            respBody.add(RespCodeEnum.SUCCESS.getCode(), "加载个人消息列表成功", list);
        } catch (CommException ex) {
            respBody.add(RespCodeEnum.ERROR.getCode(), ex.getMessage());
            LogUtils.error("加载个人消息列表失败！", ex);
        } catch (Exception ex) {
            respBody.add(RespCodeEnum.ERROR.getCode(), "加载个人消息列表失败");
            LogUtils.error("加载个人消息列表失败！", ex);
        }
        return respBody;
    }

    /**
     * 批量删除个人消息
     *
     * @return 响应对象
     */
    @PostMapping("/message/del")
    @SystemControllerLog(description = "批量删除个人消息")
//    @RepeatControllerLog
    public RespBody delMessage(@RequestBody List<AppUserMessagePo> ids) {
        // 创建返回对象
        RespBody respBody = new RespBody();
        try {
            AppUserPo user = commonService.checkToken();
            if (ids == null || ids.size() <= 0) {
                respBody.add(RespCodeEnum.ERROR.getCode(), languageUtil.getMsg(AppMessage.NOT_SELECT_DELBANK, "未选择要删除的消息"));
                return respBody;
            }
            //调用dao删除银行卡
            settingService.delMessage(ids);
            respBody.add(RespCodeEnum.SUCCESS.getCode(), "批量删除个人消息成功");
        } catch (CommException ex) {
            respBody.add(RespCodeEnum.ERROR.getCode(), ex.getMessage());
            LogUtils.error("批量删除个人消息失败！", ex);
        } catch (Exception ex) {
            respBody.add(RespCodeEnum.ERROR.getCode(), "批量删除个人消息失败");
            LogUtils.error("批量删除个人消息失败！", ex);
        }
        return respBody;
    }

    /**
     * 获取系统公告消息列表
     *
     * @return 响应对象
     */
    @GetMapping("/notice")
    public RespBody notice(Paging paging) {
        // 创建返回对象
        RespBody respBody = new RespBody();
        try {
            int total = settingService.noticeCount();
            List<SysNoticePo> list = null;
            if (total > 0) {
                list = settingService.noticeList(paging);
            }
            paging.setTotalCount(total);
            respBody.setPage(paging);
            respBody.add(RespCodeEnum.SUCCESS.getCode(), "加载公告消息列表成功", list);
        } catch (CommException ex) {
            respBody.add(RespCodeEnum.ERROR.getCode(), msgUtil.getMsg(AppMessage.LOAD_MESSAGE_FAIL, "加载个人消息列表失败"));
            LogUtils.error("加载公告消息列表失败！", ex);
        } catch (Exception ex) {
            respBody.add(RespCodeEnum.ERROR.getCode(),  msgUtil.getMsg(AppMessage.LOAD_MESSAGE_FAIL, "加载个人消息列表失败"));
            LogUtils.error("加载公告消息列表失败！", ex);
        }
        return respBody;
    }

    /**
     * 用户个人消息未读消息数量
     *
     * @return 响应对象
     */
    @GetMapping("/newMessageCount")
    public RespBody newMessage() {
        // 创建返回对象
        RespBody respBody = new RespBody();
        try {
            respBody.add(RespCodeEnum.SUCCESS.getCode(), "用户个人消息未读消息数量成功", settingService.newMsg());
        } catch (CommException ex) {
            respBody.add(RespCodeEnum.ERROR.getCode(),  msgUtil.getMsg(AppMessage.LOAD_MESSAGE_COUNT_FAIL, "加载用户个人消息未读消息数量失败"));
            LogUtils.error("用户个人消息未读消息数量失败！", ex);
        } catch (Exception ex) {
            respBody.add(RespCodeEnum.ERROR.getCode(),  msgUtil.getMsg(AppMessage.LOAD_MESSAGE_COUNT_FAIL, "加载用户个人消息未读消息数量失败"));
            LogUtils.error("用户个人消息未读消息数量失败！", ex);
        }
        return respBody;
    }

    /**
     * 用户已消息
     *
     * @return 响应对象
     */
    @PostMapping("/readMsg")
    public RespBody readMsg(@RequestBody AppUserMessagePo model) {
        // 创建返回对象
        RespBody respBody = new RespBody();
        try {
            //验签
            Boolean flag = commonService.checkSign(model);
            if (!flag) {
                respBody.add(RespCodeEnum.ERROR.getCode(), languageUtil.getMsg(AppMessage.INVALID_SIGN, "无效签名"));
                return respBody;
            }

            if (model == null || StringUtils.isEmpty(model.getId())) {
                respBody.add(RespCodeEnum.ERROR.getCode(), "id不能为空");
                return respBody;
            }

            respBody.add(RespCodeEnum.SUCCESS.getCode(), "用户已读消息成功", settingService.readMsg(model.getId()));
        } catch (CommException ex) {
            respBody.add(RespCodeEnum.ERROR.getCode(),"");
            LogUtils.error("用户已读消息失败！", ex);
        } catch (Exception ex) {
            respBody.add(RespCodeEnum.ERROR.getCode(), "");
            LogUtils.error("用户已读消息失败！", ex);
        }
        return respBody;
    }
}
