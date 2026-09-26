package com.team.dungeonbreakers;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;

public class Chest {
    public float x, y;
    public float width = 64f;
    public float height = 64f;

    private boolean isRemoved = false;
    private float stateTime = 0f;

    private Animation<TextureRegion> animation;
    private InventoryUI inventoryUI;
    private ItemManager itemManager;
    private String grade;

    public Chest(float x, float y, String grade, CharacterManager charManager, InventoryUI inventoryUI, ItemManager itemManager) {
        this.x = x;
        this.y = y;
        this.grade = grade;
        this.inventoryUI = inventoryUI;
        this.itemManager = itemManager;

        this.animation = charManager.getChestAnimation(grade);
    }

    public void interact(AbstractPlayer player) {
        if (isRemoved) return;

        System.out.println("Opening " + grade + " Chest!");

        // 아이템 지급 및 제거
        giveRewards(player.characterType);
        isRemoved = true;
    }

    public void update(float delta) {
        if (!isRemoved) {
            stateTime += delta;
        }
    }

    private void giveRewards(CharacterType playerClass) {
        if (itemManager == null || inventoryUI == null) return;

        int successCount = 0;
        System.out.println("--- " + grade + " Box Rewards (" + playerClass + ") ---");

        // 1. 장비 아이템 지급 (1~2개 랜덤)
        int equipCount = MathUtils.random(1, 2);
        for (int i = 0; i < equipCount; i++) {
            Item item = itemManager.getDropItem(grade, playerClass);
            if (item != null) {
                if (inventoryUI.addItem(item)) {
                    System.out.println("Acquired Gear: " + item.name);
                    successCount++;
                } else {
                    System.out.println("Inventory Full! Lost: " + item.name);
                }
            }
        }

        // 2. 소비 아이템 추가 지급 (50% 확률)
        if (MathUtils.randomBoolean(0.5f)) {
            Item consumable = itemManager.getDropConsumable(grade);
            if (consumable != null) {
                if (inventoryUI.addItem(consumable)) {
                    System.out.println("Acquired Consumable: " + consumable.name);
                    successCount++;
                }
            }
        }

        // 아이템을 하나라도 먹었으면 인벤토리 오픈
        if (successCount > 0 && !inventoryUI.isVisible()) {
            inventoryUI.toggle();
        }
    }

    public void draw(SpriteBatch batch) {
        if (isRemoved || animation == null) return;

        // 계속 반복 재생
        TextureRegion region = animation.getKeyFrame(stateTime, true);

        if (region != null) {
            batch.draw(region, x - width/2, y, width, height);
        }
    }

    public boolean isRemoved() {
        return isRemoved;
    }
}
