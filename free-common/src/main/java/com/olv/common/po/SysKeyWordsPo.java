package com.olv.common.po;

import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * URL记录Po类
 * @author qsy
 * @version v1.0
 * @date 2016年12月15日
 */
@Table(name="sys_key_words")
public class SysKeyWordsPo implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * 主键编号
	 */
	@Id
	private String id;
	
	/**
	 * 关键词
	 */
	private String name;
	
	/**
	 * 是否有效
	 */
	private String state;
	
	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the state
	 */
	public String getState() {
		return state;
	}

	/**
	 * @param state the state to set
	 */
	public void setState(String state) {
		this.state = state;
	}

}
