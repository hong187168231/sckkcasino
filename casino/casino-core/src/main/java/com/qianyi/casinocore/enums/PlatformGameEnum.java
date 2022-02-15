package com.qianyi.casinocore.enums;

public enum PlatformGameEnum {

    WM_GAME(1, "WM"),
    PG_GAME(2, "PG"),
    CQ9_GAME(3, "CQ9"),
    ;

    private Integer platformId;

    private String gameName;

    PlatformGameEnum(Integer platformId, String gameName) {
        this.platformId = platformId;
        this.gameName = gameName;
    }

    public Integer getPlatformId() {
        return platformId;
    }

    public String getGameName() {
        return gameName;
    }

}
