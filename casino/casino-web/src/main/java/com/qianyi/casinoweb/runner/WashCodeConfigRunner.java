package com.qianyi.casinoweb.runner;

import com.qianyi.casinocore.model.WashCodeConfig;
import com.qianyi.casinocore.service.WashCodeConfigService;
import com.qianyi.modulecommon.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 第一次启动项目初始化游戏洗码配置
 */
@Component
public class WashCodeConfigRunner implements CommandLineRunner {

    @Autowired
    private WashCodeConfigService washCodeConfigService;

    @Override
    public void run(String... args) throws Exception {
        List<WashCodeConfig> list = washCodeConfigService.findByPlatform(Constants.PLATFORM);
        if (!CollectionUtils.isEmpty(list)) {
            return;
        }
        initData();
    }

    private void initData() {
        List<WashCodeConfig> list = new ArrayList<>();
        list.add(setData("101", "百家乐","Baccarat"));
        list.add(setData("102", "龙虎","Dragon Tiger"));
        list.add(setData("103", "轮盘","Roulette"));
        list.add(setData("104", "骰宝","SicBo"));
        list.add(setData("105", "牛牛","Niu Niu"));
        list.add(setData("106", "三公","Three Face"));
        list.add(setData("107", "番摊","Fantan"));
        list.add(setData("108", "色碟","Se Die"));
        list.add(setData("110", "鱼虾蟹","Fish-Prawn-Crab"));
        list.add(setData("111", "炸金花","Golden Flower"));
        list.add(setData("112", "温州牌九","Wenzhou Pai Gow"));
        list.add(setData("113", "二八杠","Mahjong tiles"));
        list.add(setData("128", "安達巴哈","AndarBahar"));
        washCodeConfigService.saveAll(list);
    }

    private WashCodeConfig setData(String gameId, String gameName,String gameEnName) {
        WashCodeConfig codeConfig = new WashCodeConfig();
        codeConfig.setPlatform("wm");
        codeConfig.setGameId(gameId);
        codeConfig.setGameName(gameName);
        codeConfig.setGameEnName(gameEnName);
        codeConfig.setState(Constants.open);
        return codeConfig;
    }
}
