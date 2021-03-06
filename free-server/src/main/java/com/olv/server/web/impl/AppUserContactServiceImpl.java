package com.olv.server.web.impl;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.olv.common.vo.pc.AppUserContactVo;
import com.olv.server.mapper.AppUserContactMapper;
import com.olv.server.web.AppUserContactService;

/**
 * 
 * @author 用户接点人
 *	zyc 2018-01-17
 */


@Service("AppUserContactService")
public class AppUserContactServiceImpl implements AppUserContactService {

	@Resource
	 private  AppUserContactMapper appUserContactMapper; 
		
	@Override
	public List<AppUserContactVo> webFindUserByParentId(String parentId) {
		// TODO Auto-generated method stub
		return appUserContactMapper.webFindUserByParentId(parentId);
	}

	@Override
	public AppUserContactVo webfindUserByUserId(String userId) {
		// TODO Auto-generated method stub
		return appUserContactMapper.webfindUserByUserId(userId);
	}

}
