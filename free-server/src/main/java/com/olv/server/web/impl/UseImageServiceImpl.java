package com.olv.server.web.impl;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.olv.common.po.UseImagePo;
import com.olv.server.mapper.UseImageMapper;
import com.olv.server.web.UseImageService;

/**
 * 用户业务层实现类
 * @author yyr
 * @version v1.0
 * @date 2017年6月12日
 */
@Service("webUseImageService")
public class UseImageServiceImpl implements UseImageService {
	@Resource
	private UseImageMapper useImageMapper;

	@Override
	public List<UseImagePo> getByIds(List<String> ids) {
		return useImageMapper.getByIds(ids);
	}

	
}
