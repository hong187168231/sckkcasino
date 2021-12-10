package com.qianyi.casinoadmin.install;

import com.qianyi.casinoadmin.install.file.MenuTranslationFile;
import com.qianyi.casinoadmin.install.file.SysPermissionConfigFile;
import com.qianyi.casinoadmin.util.LoginUtil;
import com.qianyi.casinocore.model.SysPermission;
import com.qianyi.casinocore.service.SysPermissionService;
import com.qianyi.casinocore.util.CommonConst;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 菜单翻译初始化
 */
@Component
@Slf4j
@Order(2)
public class MenuTranslationInit  implements CommandLineRunner {
    @Autowired
    private MenuTranslationFile menuTranslationFile;
    @Autowired
    private SysPermissionService sysPermissionService;
    @Override
    public void run(String... args) throws Exception {
        log.info("初始化菜单翻译开始============================================》");
        List<SysPermission> sysPermissions = sysPermissionService.findAll();
        if (LoginUtil.checkNull(sysPermissions) || sysPermissions.size() == CommonConst.NUMBER_0){
            log.info("菜单为空============================================》");
            return;
        }
        this.initializeEnglish(sysPermissions);
        this.initializeCambodian(sysPermissions);
        log.info("初始化菜单翻译结束============================================》");
    }

    private void initializeEnglish(List<SysPermission> sysPermissions){
        Map<String, String> englishNames = menuTranslationFile.getEnglishNames();
        if (LoginUtil.checkNull(englishNames) || englishNames.size() == CommonConst.NUMBER_0){
            return;
        }
        sysPermissions.stream().forEach(sysPermission -> {
            sysPermission.setEnglishName(englishNames.get(sysPermission.getName()));
            sysPermissionService.save(sysPermission);
        });
    }
    private void initializeCambodian(List<SysPermission> sysPermissions){
        Map<String, String> cambodianNames = menuTranslationFile.getCambodianNames();
        if (LoginUtil.checkNull(cambodianNames) || cambodianNames.size() == CommonConst.NUMBER_0){
            return;
        }
        sysPermissions.stream().forEach(sysPermission -> {
            sysPermission.setCambodianName(cambodianNames.get(sysPermission.getName()));
            sysPermissionService.save(sysPermission);
        });
    }
}
