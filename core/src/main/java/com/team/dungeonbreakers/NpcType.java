package com.team.dungeonbreakers;

public enum NpcType {
    PRIEST_JOB_CHANGE("Priest", "전직 교관"),
    KNIGHT_UNLOCK("Knight Templar", "기사 단장"),
    // ★★★ [수정] 상인은 이제 Swordsman 이미지를 사용합니다 ★★★
    MERCHANT("Swordsman", "상인");

    public final String atlasKey;
    public final String roleName;

    NpcType(String atlasKey, String roleName) {
        this.atlasKey = atlasKey;
        this.roleName = roleName;
    }
}
