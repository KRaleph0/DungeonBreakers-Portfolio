package com.team.dungeonbreakers;

public class BattleStateManager {
    private static BattleStateManager instance;

    public boolean isInCombat = false;
    public int activeEnemyCount = 0;

    public static BattleStateManager getInstance() {
        if (instance == null) {
            instance = new BattleStateManager();
        }
        return instance;
    }

    private BattleStateManager() {
        reset();
    }

    public void reset() {
        isInCombat = false;
        activeEnemyCount = 0;
    }

    // 전투 시작 (몬스터 스폰 시 호출)
    public void startCombat(int enemyCount) {
        if (enemyCount > 0) {
            isInCombat = true;
            activeEnemyCount += enemyCount;
            System.out.println("!!! COMBAT STARTED !!! Enemies: " + activeEnemyCount);
        }
    }

    // 적 사망 시 호출
    public void onEnemyKilled() {
        if (activeEnemyCount > 0) {
            activeEnemyCount--;
            System.out.println("Enemy killed. Remaining: " + activeEnemyCount);

            if (activeEnemyCount <= 0) {
                endCombat();
            }
        }
    }

    private void endCombat() {
        isInCombat = false;
        activeEnemyCount = 0;
        System.out.println("!!! COMBAT ENDED !!! Peace restored.");
    }
}
