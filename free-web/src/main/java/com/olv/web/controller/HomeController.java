package com.olv.web.controller;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import com.olv.common.vo.pc.HomeUser;
import com.olv.server.app.BitcoinService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.olv.common.enums.BusnessTypeEnum;
import com.olv.common.enums.CurrencyTypeEnum;
import com.olv.common.enums.RespCodeEnum;
import com.olv.common.po.AppUserPo;
import com.olv.common.resp.Paging;
import com.olv.common.resp.RespBody;
import com.olv.common.util.LogUtils;
import com.olv.common.vo.pc.AppBillRecordVo1;
import com.olv.common.vo.pc.AppWithDrawVo;
import com.olv.server.web.AppUserService;
import com.olv.server.web.AppWithDrawService;
import com.olv.server.web.WebBillRecordService;

/**
 * 
 * @author 首页统计
 *	zyc
 *  2018-01-13
 */


@RestController
@RequestMapping("/home")
public class HomeController {
	
	
	@Resource
	private WebBillRecordService webBillRecordService;
	
	@Resource
	private AppWithDrawService appWithDrawService;
	@Resource
	private AppUserService appUserService;

	@Resource
	private BitcoinService bitcoinService;
	/**
	 * 首页记录
	 * @param vo
	 * @param paging
	 * @return
	 */
	@GetMapping("/findAll")
	public RespBody findAll(AppBillRecordVo1 vo,Paging paging){
		RespBody respBody = new RespBody();
		try {
			Map<String,Object> result=new HashMap<String, Object>();
			
			
			/**
			 * 今日统计
			 */
			vo.setStartTime(getStarttime(new Date().getTime()));
			
			//今日充值
			vo.setBusnessType(BusnessTypeEnum.EP_RECHARGE.getCode());
			BigDecimal recharge = webBillRecordService.SUMCount(vo);
			result.put("recharge",recharge);
			
			
			
			
			
			
			
			
			//ep转账
			vo.setBusnessType(BusnessTypeEnum.EP_TRANSFER_IN.getCode());
			BigDecimal ep = webBillRecordService.SUMCount(vo);
			result.put("epTran",ep);
			
			
			//OLV
			
			vo.setBusnessType(BusnessTypeEnum.BIRDSCORE_TRANSFER_OUT.getCode());
			BigDecimal birdScore = webBillRecordService.SUMCount(vo);
			result.put("birdScore",birdScore);
			
			//资产释放
			AppBillRecordVo1 vo2 = new AppBillRecordVo1();
			vo2.setBusnessType(BusnessTypeEnum.E_RELEASE.getCode());
			vo2.setCurrencyType(CurrencyTypeEnum.E_ASSET.getCode());
			vo2.setStartTime(getStarttime(new Date().getTime()));
			BigDecimal epRelease = webBillRecordService.SUMCount(vo2);
			result.put("epRelease",epRelease.abs());
			
			//今日注册用户
			AppUserPo appuser = new AppUserPo();
			appuser.setCreateTime(getStarttime(new Date().getTime()));
			result.put("registerUserCount",appUserService.SUMCount(appuser));
			//今日激活用户
			appuser.setState(20);
			appuser.setCreateTime(getStarttime(new Date().getTime()));
			result.put("activeUserCount",appUserService.SUMCount(appuser));
			
			
			//今日ep转账记录
			AppBillRecordVo1 vo4 = new AppBillRecordVo1();
			vo4.setBusnessType(BusnessTypeEnum.EP_TRANSFER_IN.getCode());
			vo4.setStartTime(getStarttime(new Date().getTime()));
			
			result.put("todayEpTranCount",webBillRecordService.findCount(vo4));
			//今日ep释放记录
			AppBillRecordVo1 vo5 = new AppBillRecordVo1();
			vo5.setBusnessType(BusnessTypeEnum.E_RELEASE.getCode());
			vo5.setCurrencyType(CurrencyTypeEnum.E_ASSET.getCode());
			vo5.setStartTime(getStarttime(new Date().getTime()));
			result.put("todayEpReleaseCount",webBillRecordService.findCount(vo5));
			
			//今日ep充值记录
			AppBillRecordVo1 vo6 = new AppBillRecordVo1();
			
			vo6.setBusnessType(BusnessTypeEnum.EP_RECHARGE.getCode());
			vo6.setStartTime(getStarttime(new Date().getTime()));
			result.put("rechargeCount",webBillRecordService.findCount(vo6));
			

			
			/**
			 * 统计全部
			 */

			//提现笔数
			AppWithDrawVo appWithDrawVo = new AppWithDrawVo();
			appWithDrawVo.setState(10);
			//appWithDrawVo.setStartTime(getStarttime(new Date().getTime()));
			
			result.put("appWidthDraw",appWithDrawService.findCount(appWithDrawVo));
			
			//提现总数
			appWithDrawVo.setState(20);
			result.put("appWidthDrawSUM",appWithDrawService.findSUM(appWithDrawVo));
			
			//用户统计（ep,OLV，e资产，用户数量,影子积分）


			HomeUser homeUser = appUserService.homeSUM();


			//用户tfc总数减掉10000
			homeUser.setBirdScore(appUserService.homeSUM().getBirdScore().subtract(appUserService.findUid("10000").getBirdScore()));

			result.put("homeSUM",homeUser);



			//钱包余额
			//result.put("bitcoinScore",bitcoinService.getBitCoinClient ().getBalance ("10000"));

			
			AppBillRecordVo1 appbillRecordVo2 = new AppBillRecordVo1();
			
			
			//EP充值总计
			appbillRecordVo2.setBusnessType(BusnessTypeEnum.EP_RECHARGE.getCode());
			BigDecimal epRechargeAll = webBillRecordService.SUMCount(appbillRecordVo2);
			result.put("epRechargeAll",epRechargeAll);
			
			
			//资产释放
			
			AppBillRecordVo1 appbillRecordVo3 = new AppBillRecordVo1();
			appbillRecordVo3.setBusnessType(BusnessTypeEnum.E_RELEASE.getCode());
			appbillRecordVo3.setCurrencyType(CurrencyTypeEnum.E_ASSET.getCode());
			BigDecimal epReleaseAll = webBillRecordService.SUMCount(appbillRecordVo3);
			
			result.put("epReleaseAll",epReleaseAll.abs());

			respBody.add(RespCodeEnum.SUCCESS.getCode(), "查询记录数据成功",result);
			//保存分页对象
			
			
			paging.setTotalCount(webBillRecordService.findCount(vo));
			respBody.setPage(paging);
		} catch (Exception ex) {
			respBody.add(RespCodeEnum.ERROR.getCode(), "查询记录数据失败");
			LogUtils.error("查找所有数据失败！",ex);
		}
		return respBody;
	}



	@GetMapping("/findBitCoin")
	public RespBody findBitCoin(AppBillRecordVo1 vo,Paging paging){
		RespBody respBody = new RespBody();
		try {
			Map<String,Object> result=new HashMap<String, Object>();




			//钱包余额统计


			BigDecimal bitcoinScore =bitcoinService.getBitCoinClient ().getBalance ();
			if(bitcoinScore.equals(0) || bitcoinScore.equals("") ){
				result.put("bitcoinScore","--");

			}else{
				result.put("bitcoinScore",bitcoinScore);
			}




			respBody.add(RespCodeEnum.SUCCESS.getCode(), "查询记录数据成功",result);
			//保存分页对象


			paging.setTotalCount(webBillRecordService.findCount(vo));
			respBody.setPage(paging);
		} catch (Exception ex) {

			respBody.add(RespCodeEnum.ERROR.getCode(), "查询记录数据失败");
			LogUtils.error("查找所有数据失败！",ex);
		}
		return respBody;
	}
	
	
	//开始时间处理
	public static Date getStarttime(Long startTime){
		Date endTime = new Date(startTime);
		try {
			SimpleDateFormat dft = new SimpleDateFormat("yyyy-MM-dd");
			Calendar date = Calendar.getInstance();
			date.setTime(endTime);
			date.set(Calendar.DATE, date.get(Calendar.DATE));
			endTime = dft.parse(dft.format(date.getTime()));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return endTime;
	}				
		
	

}
