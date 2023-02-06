package com.qianyi.casinocore.business;

import com.qianyi.casinocore.model.UserWashCodeConfig;
import com.qianyi.casinocore.model.WashCodeConfig;
import com.qianyi.casinocore.service.UserWashCodeConfigService;
import com.qianyi.casinocore.service.WashCodeConfigService;
import com.qianyi.modulecommon.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class UserWashCodeBusiness {

    @Autowired
    private UserWashCodeConfigService userWashCodeConfigService;

    @Autowired
    private WashCodeConfigService washCodeConfigService;

    public List<WashCodeConfig> getWashCodeConfig(String platform,Long userId) {
        //先查询用户级的洗码配置
        List<UserWashCodeConfig> codeConfigs = userWashCodeConfigService.findByUserIdAndPlatform(userId, platform);
        if (!CollectionUtils.isEmpty(codeConfigs)) {
            List<WashCodeConfig> list = new ArrayList<>();
            WashCodeConfig config = null;
            for (UserWashCodeConfig codeConfig : codeConfigs) {
                if (codeConfig.getState() != Constants.open) {
                    continue;
                }
                config = new WashCodeConfig();
                BeanUtils.copyProperties(codeConfig, config);
                list.add(config);
            }
            return list;
        }
        //先查询全局洗码配置
        List<WashCodeConfig> configs = washCodeConfigService.findByPlatformAndState(platform,Constants.open);
        return configs;
    }
}
