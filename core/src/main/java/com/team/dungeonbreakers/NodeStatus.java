package com.team.dungeonbreakers;

public enum NodeStatus {
    LOCKED,     // 갈 수 없음 (잠김)
    AVAILABLE,  // 현재 갈 수 있음 (반짝임 효과)
    COMPLETED,  // 이미 클리어함
    UNREACHABLE // 지나친 길 (선택 불가)
}
