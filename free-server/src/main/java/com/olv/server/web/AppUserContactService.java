package com.olv.server.web;

import java.util.List;

import com.olv.common.vo.pc.AppUserContactVo;

public interface AppUserContactService {
	
	
	public List<AppUserContactVo> webFindUserByParentId(String parentId);
	
	
	public AppUserContactVo webfindUserByUserId(String userId);

}
