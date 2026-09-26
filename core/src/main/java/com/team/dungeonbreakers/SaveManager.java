package com.team.dungeonbreakers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

public class SaveManager {
    private static final String PREF_NAME_PREFIX = "dungeon_breakers_save_";

    public static void save(int slot, PlayerData data) {
        Preferences prefs = Gdx.app.getPreferences(PREF_NAME_PREFIX + slot);

        // 기본 정보
        prefs.putInteger("gold", data.gold);
        prefs.putInteger("gems", data.gems);
        prefs.putString("currentJob", data.currentJob);

        // ★★★ [추가] 해금 정보 저장 ★★★
        prefs.putBoolean("isArcherUnlocked", data.isArcherUnlocked);
        prefs.putInteger("shopSlotCount", data.shopSlotCount);
        prefs.putBoolean("isShopPremium", data.isShopPremium);

        prefs.flush();
        System.out.println("Game Saved to Slot " + slot);
    }

    public static void load(int slot, PlayerData data) {
        Preferences prefs = Gdx.app.getPreferences(PREF_NAME_PREFIX + slot);

        data.gold = prefs.getInteger("gold", 0);
        data.gems = prefs.getInteger("gems", 0);
        data.currentJob = prefs.getString("currentJob", "Knight"); // 기본 기사

        // ★★★ [추가] 해금 정보 로드 ★★★
        data.isArcherUnlocked = prefs.getBoolean("isArcherUnlocked", false);
        data.shopSlotCount = prefs.getInteger("shopSlotCount", 4);
        data.isShopPremium = prefs.getBoolean("isShopPremium", false);

        System.out.println("Game Loaded from Slot " + slot);
    }
}
