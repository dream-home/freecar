package com.olv.web.controller;

import java.math.BigDecimal;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.olv.common.annotation.SystemControllerLog;
import com.olv.common.enums.BusnessTypeEnum;
import com.olv.common.enums.CurrencyTypeEnum;
import com.olv.common.enums.RespCodeEnum;
import com.olv.common.enums.WithDrawEnum;
import com.olv.common.po.AppBillRecordPo;
import com.olv.common.po.AppUserPo;
import com.olv.common.po.AppWithDrawPo;
import com.olv.common.resp.Paging;
import com.olv.common.resp.RespBody;
import com.olv.common.util.LogUtils;
import com.olv.common.vo.pc.AppWithDrawVo;

import com.olv.server.web.AppUserService;
import com.olv.server.web.AppWithDrawService;
import com.olv.server.web.WebBillRecordService;

@RestController
@RequestMapping(value = "/appWithDraw")
public class AppWithDrawController {
	@Resource
	private	  AppWithDrawService appWithDrawService;
	
	@Resource
	private AppUserService appUserService;
	@Resource
	private WebBillRecordService webBillRecordService;
	
	@GetMapping("/findAll")
	public RespBody findAllRerocd(AppWithDrawVo vo  ,Paging paging){
		RespBody respBody = new RespBody();
		try {
			//保存返回数据 
			List <AppWithDrawVo> list = appWithDrawService.findAll(vo,paging);
			for(AppWithDrawVo vo1:list){
				vo1.setTypeName(WithDrawEnum.getName(vo1.getState()));
			}
			respBody.add(RespCodeEnum.SUCCESS.getCode(), "查找所有提现记录数据成功", list);
			//保存分页对象
			paging.setTotalCount(appWithDrawService.findCount(vo));
			respBody.setPage(paging);
		} catch (Exception ex) {
			respBody.add(RespCodeEnum.ERROR.getCode(), "查找所有提现记录数据失败");
			LogUtils.error("查找所有提现记录信息数据失败！",ex);
		}
		return respBody;
	}
	
	
	@PostMapping("/successState")
	@SystemControllerLog(description="确认打款")
	public RespBody successState(@RequestBody AppWithDrawPo po){
		RespBody respBody = new RespBody();
		try {
			
		
			po.setState(20);
			appWithDrawService.update(po);
			/**
			 * 修改用户冻结资金（冻结资金-提现金额）
			 */
			AppUserPo appuser = appUserService.findUserById(po.getUserId());
			appuser.setBlockedEpBalance(appuser.getBlockedEpBalance().subtract(po.getAmount()));
			appUserService.updateById(appuser,appuser.getId());
		
			
			respBody.add(RespCodeEnum.SUCCESS.getCode(), "操作成功");
		} catch (Exception ex) {
			respBody.add(RespCodeEnum.ERROR.getCode(), "操作成功");
			LogUtils.error("操作成功！",ex);
		}
		return respBody;
	}

	
	@PostMapping("/errorState")
	@SystemControllerLog(description="驳回打款")
	public RespBody erroeState(@RequestBody AppWithDrawPo po){
		RespBody respBody = new RespBody();
		try {
			
			
			po.setState(30);
			appWithDrawService.update(po);
			/**
			 * 修改用户冻结资金（冻结资金 = 冻结资金-提现金额   用户ep余额 = 用户ep余额 +提现金额 ）
			 */
			AppUserPo appuser = appUserService.findUserById(po.getUserId());
			BigDecimal before = appuser.getEpBalance();
			appuser.setBlockedEpBalance(appuser.getBlockedEpBalance().subtract(po.getAmount()));
			appuser.setEpBalance(appuser.getEpBalance().add(po.getAmount()));
			appUserService.updateById(appuser,appuser.getId());
			
			
			AppBillRecordPo appBillRecordPo = new AppBillRecordPo();
			
			appBillRecordPo.setUserId(po.getUserId());
			appBillRecordPo.setBalance(po.getAmount());
			appBillRecordPo.setBusinessNumber(po.getId());
			appBillRecordPo.setBeforeBalance(before);
			appBillRecordPo.setAfterBalance(appuser.getEpBalance());
			
			appBillRecordPo.setCurrencyType(CurrencyTypeEnum.EP_BALANCE.getCode());
			appBillRecordPo.setBusnessType(BusnessTypeEnum.EP_FREE_FORZEN.getCode());
			appBillRecordPo.setRemark("提现驳回记录");
			webBillRecordService.add(appBillRecordPo);
				
			respBody.add(RespCodeEnum.SUCCESS.getCode(), "操作成功");
		} catch (Exception ex) {
			respBody.add(RespCodeEnum.ERROR.getCode(), "操作成功");
			LogUtils.error("操作成功！",ex);
		}
		return respBody;
	}
	
	
	
	
	

}
