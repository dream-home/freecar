package com.olv.common.enums;

/**
 * 响应枚举
 * @author qsy
 * @version v1.0
 * @date 2016年11月25日
 */
public enum RespCodeEnum {
	SUCCESS("0000","成功"),
	NOLOGIN("5000","沒有登录"),
	INVALID_LOGIN("608","登录失效"),
	ERROR("9999","错误");
	private String code;
	private String name;
	
	private RespCodeEnum(String code, String name) {
		this.name = name;
		this.code = code;
	}

	public String getCode() {
		return code;
	}

	public String getName() {
		return name;
	}
	
	public static String getName(String code){
		if (code == null) {
			return "";
		}
		for(RespCodeEnum enums : RespCodeEnum.values()){
			if (enums.getCode().equals(code)) {
				return enums.getName();
			}
		}
		return code;
	}
}
