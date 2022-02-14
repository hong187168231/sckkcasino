package com.qianyi.casinoadmin.install;

import com.qianyi.casinoadmin.util.ExcelUtils;
import com.qianyi.casinocore.model.AdGame;
import com.qianyi.casinocore.model.PlatformGame;
import com.qianyi.casinocore.service.AdGamesService;
import com.qianyi.casinocore.service.PlatformGameService;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
@Order(5)
public class AdGameInitialization implements CommandLineRunner {

    @Autowired
    private AdGamesService adGamesService;

    @Autowired
    private PlatformGameService platformGameService;

    @Override
    public void run(String... args) throws Exception {
        addPlatformGame();

        long count = adGamesService.fontCount();
        if(count > 0){
            return;
        }
        InputStream inputStream = this.getClass().getResourceAsStream("/excleTemplate/gameList.xlsx");
        XSSFWorkbook xssfSheets = new XSSFWorkbook(inputStream);
        List<List<String>> lists = ExcelUtils.readExcelContentList(xssfSheets);
        List<AdGame> adGameList = new ArrayList<>();
        for (List<String> list : lists) {
            AdGame adGame = new AdGame();
            for (int i = 0; i < list.size(); i++) {
                if(i == 0){
                    adGame.setGamePlatformId(Integer.parseInt(list.get(i)));
                }
                if(i == 1){
                    adGame.setGameName(list.get(i).trim());
                }
                if(i == 2){
                    adGame.setGameCode(list.get(i).trim());
                }
                if(i == 3){
                    adGame.setGamesStatus(Integer.parseInt(list.get(i)));
                }
                if(i == 4){
                    adGame.setGameEnName(list.get(i).trim());
                }
                adGameList.add(adGame);
            }
        }
        adGamesService.saveAll(adGameList);
    }

    /**
     * 添加平台
     */
    private void addPlatformGame() {
        List<PlatformGame> platformGameList = platformGameService.findAll();
        if(platformGameList == null || platformGameList.isEmpty()){
            List<PlatformGame> platformGames = new ArrayList<>();
            platformGames.add(new PlatformGame(1, "PG", 1));
            platformGames.add(new PlatformGame(2, "CQ9", 1));
            platformGameService.saveAll(platformGames);
        }

    }
}
