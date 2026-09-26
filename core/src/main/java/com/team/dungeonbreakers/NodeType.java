package com.team.dungeonbreakers;

public enum NodeType {
    MONSTER("battle"),      // 전투
    SHOP("shop"),           // 상점
    CHEST("compensation"),  // 상자
    REST("rest"),           // 휴식
    BOSS("boss");           // 보스

    public final String iconName;

    NodeType(String iconName) {
        this.iconName = iconName;
    }
}
