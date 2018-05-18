package com.olv.app.controller;


import com.olv.common.service.RedisService;
import com.olv.common.util.LanguageUtil;
import com.olv.server.app.AppUserService;
import com.olv.server.app.BitcoinService;
import com.olv.server.common.CommonService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 数字资产控制器
 *
 * @author qsy
 * @version V1.0
 * @date 2017年8月17日
 */
@RestController
@RequestMapping("/coin")
public class CoinController {
    @Resource
    private BitcoinService bitcoinService;
    @Resource
    private CommonService commonService;
    @Resource
    private LanguageUtil languageUtil;
    @Resource
    private RedisService redisService;
    @Resource
    private HttpServletRequest request;
    @Resource
    private AppUserService appUserService;


}
