package com.team.dungeonbreakers;

import com.badlogic.gdx.utils.Array;

public class PlayerData {
    private static PlayerData instance;

    public int gold = 0;
    public int gems = 0;

    public boolean isArcherUnlocked = false;
    public int shopSlotCount = 6;
    public boolean isShopPremium = false;

    public Array<Item> inventoryItems;
    public Array<Item> equipmentItems;
    public int currentHp = -1;
    public String currentJob = "Knight";

    // ★★★ [추가] 맵 이동 간 버프 저장용 ★★★
    public Array<AbstractPlayer.Buff> savedBuffs;

    private PlayerData() {
        inventoryItems = new Array<>();
        equipmentItems = new Array<>();
        savedBuffs = new Array<>();

        for (int i = 0; i < 8; i++) {
            equipmentItems.add(null);
        }
    }

    public static PlayerData getInstance() {
        if (instance == null) {
            instance = new PlayerData();
        }
        return instance;
    }

    public void addGold(int amount) { this.gold += amount; }
    public void addGems(int amount) { this.gems += amount; }

    public void resetRun() {
        this.gold = 0;
        this.currentHp = -1;
        this.inventoryItems.clear();
        this.savedBuffs.clear(); // 버프도 초기화
        for (int i = 0; i < this.equipmentItems.size; i++) {
            this.equipmentItems.set(i, null);
        }
    }

    public void save(int slot) { SaveManager.save(slot, this); }
    public void load(int slot) { SaveManager.load(slot, this); }
}
