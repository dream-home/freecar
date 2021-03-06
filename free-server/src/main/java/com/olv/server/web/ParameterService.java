package com.olv.server.web;


import java.util.List;
import java.util.Map;

import com.olv.common.po.SysParameterPo;
import com.olv.common.resp.Paging;
import com.olv.common.vo.pc.SysParameterVo;

/**
 * 参数设置业务层接口
 *
 * @author qsy
 * @version v1.0
 * @date 2017年6月15日
 */
public interface ParameterService {

    /**
     * 查找所有数据
     *
     * @param paging 分页
     * @return 集合
     * @throws Exception
     */
    public List<SysParameterVo> findAll(Paging paging) throws Exception;
    
    
    public List<String> findGrouptype() throws Exception;
    /**
     * 新增
     *
     * @param parameterVo
     * @throws Exception
     */
    public void add(SysParameterVo parameterVo) throws Exception;

    /**
     * 修改
     *
     * @param parameterVo
     * @throws Exception
     */
    public void update(SysParameterVo parameterVo) throws Exception;

    /**
     * 删除
     *
     * @param parameterVo
     */
    public void delete(SysParameterVo parameterVo);

    /**
     * 查找总记录数
     *
     * @return
     */
    public long findCount();

    String findParameter(String paraName);

    Map<String,String> findParameter(String... paraName);
    
    /**
     * 根据条件查询
     * @param SysParameterVo
     * @param paging
     * @return
     */
    public List<SysParameterVo> getInfoByTime(SysParameterVo sysParameterVo, Paging paging);

}
