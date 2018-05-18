package com.olv.web.controller;


import com.olv.common.enums.RespCodeEnum;
import com.olv.common.po.AppUserMessagePo;
import com.olv.common.po.AppUserPo;
import com.olv.common.resp.Paging;
import com.olv.common.resp.RespBody;
import com.olv.common.util.LogUtils;
import com.olv.common.vo.pc.AppUserMessageVo;
import com.olv.server.web.AppUserMessageService;

import com.olv.server.web.AppUserService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/appUserMessage")
public class AppUserMessageController {


	@Resource
	private AppUserService webAppUserService;
	@Resource
	private AppUserMessageService appUserMessageService;


	@GetMapping("/findAll")
	public RespBody findAll(AppUserMessageVo appUserMessageVo, Paging paging){
		RespBody respBody = new RespBody();
		try {
			//保存返回数据
			List<AppUserMessageVo> list=appUserMessageService.getInfoByTime(appUserMessageVo, paging);
			
			
			
			respBody.add(RespCodeEnum.SUCCESS.getCode(), "查询消息数据成功", list);
			//保存分页对象
			paging.setTotalCount(appUserMessageService.getInfoByTimeCount(appUserMessageVo));
			respBody.setPage(paging);
		} catch (Exception ex) {
			respBody.add(RespCodeEnum.ERROR.getCode(), "查询消息数据失败");
			LogUtils.error("公告消息查找所有数据失败！",ex);
		}
		
		return respBody;
	}
	
	@PostMapping("/add")
	public RespBody add(@RequestBody AppUserMessageVo appUserMessageVo){
		RespBody respBody = new RespBody();
		try{
			AppUserPo appUser =  webAppUserService.findUid(appUserMessageVo.getUid());
			
			
			AppUserMessagePo appUserMessagePo = new AppUserMessagePo();
			appUserMessagePo.setUserId(appUser.getId());
			appUserMessagePo.setTitle(appUserMessageVo.getTitle());
			appUserMessagePo.setContent(appUserMessageVo.getContent());
			appUserMessagePo.setCreateTime(appUserMessageVo.getCreateTime());
			appUserMessagePo.setRemark(appUserMessageVo.getRemark());
			appUserMessagePo.setState(appUserMessageVo.getState());
			
			appUserMessageService.add(appUserMessagePo);
			respBody.add(RespCodeEnum.SUCCESS.getCode(), "消息添加成功");
		}catch (Exception ex) {
			respBody.add(RespCodeEnum.ERROR.getCode(), "消息添加失败");
			LogUtils.error("消息添加失败！",ex);
		}
		
		return respBody;
	}
	
	
	@GetMapping("/findUid")
	public RespBody findUid(AppUserMessageVo appUserMessageVo){
		RespBody respBody = new RespBody();
		try {
			AppUserPo appUser  =   webAppUserService.findUid(appUserMessageVo.getUid());
			if(appUser == null){
				respBody.add(RespCodeEnum.SUCCESS.getCode(), "该用户不存在", appUser);	
			}else{
				respBody.add(RespCodeEnum.SUCCESS.getCode(), "该用户数据查询成功", appUser);
			}
				
			
		} catch (Exception ex) {
			respBody.add(RespCodeEnum.ERROR.getCode(), "该用户数据查询失败");
			LogUtils.error("该用户查找数据失败！",ex);
		}
		
		return respBody;
	}
	
	
	
	@PostMapping("/delete")
	public RespBody delete(@RequestBody AppUserMessageVo appUserMessageVo){
		AppUserMessagePo appUserMessagePo = new AppUserMessagePo();
		appUserMessagePo.setId(appUserMessageVo.getId());
		appUserMessagePo.setUserId(appUserMessageVo.getUserId());
		appUserMessagePo.setTitle(appUserMessageVo.getTitle());
		appUserMessagePo.setContent(appUserMessageVo.getContent());
		appUserMessagePo.setCreateTime(appUserMessageVo.getCreateTime());
		appUserMessagePo.setRemark(appUserMessageVo.getRemark());
		
		RespBody respBody = new RespBody();
		try{
			appUserMessageService.delete(appUserMessagePo);
			respBody.add(RespCodeEnum.SUCCESS.getCode(), "用户消息删除成功");
		}catch (Exception ex) {
			respBody.add(RespCodeEnum.ERROR.getCode(), "用户消息删除失败");
			LogUtils.error("用户消息删除失败！",ex);
		}
		
		return respBody;
	}
	

}
