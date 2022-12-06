package com.qianyi.casinoadmin.controller;

import com.qianyi.casinoadmin.util.LoginUtil;
import com.qianyi.casinocore.model.IpBlack;
import com.qianyi.casinocore.service.IpBlackService;
import com.qianyi.casinocore.util.BillThreadPool;
import com.qianyi.casinocore.util.CommonConst;
import com.qianyi.modulecommon.Constants;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import com.qianyi.modulespringcacheredis.util.RedisUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("ipBlack")
@Api(tags = "运营中心")
@Slf4j
public class IpBlackController {

    @Autowired
    private IpBlackService ipBlackService;

    @Autowired
    private RedisUtil redisUtil;

    private static final BillThreadPool threadPool = new BillThreadPool(CommonConst.NUMBER_1);

    /**
     * 分页查询ip黑名单
     *
     * @param pageSize 每页大小
     * @param pageCode 当前页
     * @param ip ip
     * @param status 状态
     * @return
     */
    @ApiOperation("分页查询ip黑名单")
    @GetMapping("/findIpBlackPag")
    @ApiImplicitParams({@ApiImplicitParam(name = "pageSize", value = "每页大小(默认10条)", required = false),
        @ApiImplicitParam(name = "pageCode", value = "当前页(默认第一页)", required = false),
        @ApiImplicitParam(name = "ip", value = "ip", required = false),
        @ApiImplicitParam(name = "status", value = "状态/** 0:未禁用 1：禁用 */", required = false),})
    public ResponseEntity<IpBlack> findIpBlackPag(Integer pageSize, Integer pageCode, String ip, Integer status) {
        IpBlack ipBlack = new IpBlack();
        Sort sort = Sort.by("id").descending();
        ipBlack.setIp(ip);
        if (!LoginUtil.checkNull(ip)) {
            threadPool.execute(() -> this.asynDeleRedis(ip));
        }
        ipBlack.setStatus(status);
        Pageable pageable = LoginUtil.setPageable(pageCode, pageSize, sort);
        Page<IpBlack> ipBlackPag = ipBlackService.findIpBlackPag(ipBlack, pageable);
        return ResponseUtil.success(ipBlackPag);
    }

    @GetMapping("/disable")
    @ApiOperation("ip黑名单删除")
    @ApiImplicitParams({@ApiImplicitParam(name = "id", value = "id", required = true),})
    public ResponseEntity disable(Long id) {
        IpBlack byId = ipBlackService.findById(id);
        if (LoginUtil.checkNull(byId)) {
            return ResponseUtil.custom("找不到这个ip");
        }
        String ip = byId.getIp();
        ipBlackService.delete(byId);
        if (!LoginUtil.checkNull(ip)) {
            threadPool.execute(() -> this.asynDeleRedis(ip));
        }
        return ResponseUtil.success();
    }

    private void asynDeleRedis(String ip) {
        log.info("ip黑名单异步删除缓存{}开始", ip);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            log.error("ip黑名单异步删除缓存异常", ex);
        }
        Boolean b = redisUtil.delete(RedisUtil.IP_BLACK_LIST_KEY + ip);
        log.info("ip黑名单异步删除缓存{}结束{}", ip, b);
    }
}
