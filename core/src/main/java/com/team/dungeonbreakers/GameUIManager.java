package com.team.dungeonbreakers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.Viewport;

public class GameUIManager implements Disposable {
    private float slotSize = 84f;
    private float slotGap = 15f;
    private float quickSlotIconPadding = 25f;
    private float skillSlotIconPadding = 23f;

    private final SpriteBatch batch;
    private final BitmapFont uiFont;

    private TextureAtlas uiAtlas, dashAtlas, itemAtlas;
    private TextureRegion hp_100, hp_80, hp_60, hp_50, hp_30, hp_10, hp_0;
    private TextureRegion dash_0, dash_1, dash_2, dash_3, dash_4;

    private TextureRegion slotRegion;
    private TextureRegion key1, key2, keyQ;

    private Texture goldIcon;
    private Texture starIcon;

    private final GlyphLayout glyphLayout = new GlyphLayout();
    private ShapeRenderer shapeRenderer;

    public GameUIManager(SpriteBatch batch, BitmapFont uiFont, CharacterManager charManager) {
        this.batch = batch;
        this.uiFont = uiFont;
        this.shapeRenderer = new ShapeRenderer();
        loadAssets(charManager);
    }

    private void loadAssets(CharacterManager charManager) {
        try {
            uiAtlas = new TextureAtlas("img/UI/hp_bar.atlas");
            hp_100 = uiAtlas.findRegion("hp", 100);
            hp_80 = uiAtlas.findRegion("hp", 80);
            hp_60 = uiAtlas.findRegion("hp", 60);
            hp_50 = uiAtlas.findRegion("hp", 50);
            hp_30 = uiAtlas.findRegion("hp", 30);
            hp_10 = uiAtlas.findRegion("hp", 10);
            hp_0 = uiAtlas.findRegion("hp", 0);
        } catch (Exception e) { }

        try {
            dashAtlas = new TextureAtlas("img/UI/dash.atlas");
            dash_0 = dashAtlas.findRegion("dash", 0);
            dash_1 = dashAtlas.findRegion("dash", 1);
            dash_2 = dashAtlas.findRegion("dash", 2);
            dash_3 = dashAtlas.findRegion("dash", 3);
            dash_4 = dashAtlas.findRegion("dash", 4);
        } catch (Exception e) { }

        this.slotRegion = charManager.getSlotCoverTexture();
        this.itemAtlas = charManager.getItemAtlas();
        this.key1 = charManager.getKeyTexture("1");
        this.key2 = charManager.getKeyTexture("2");
        this.keyQ = charManager.getKeyTexture("q");

        this.goldIcon = charManager.getGoldIcon();
        this.starIcon = charManager.getStarIcon();
    }

    public void draw(Viewport uiViewport, AbstractPlayer player, boolean isPaused, InventoryUI inventoryUI, float gameTime) {
        uiViewport.apply();
        batch.setProjectionMatrix(uiViewport.getCamera().combined);

        batch.begin();

        float uiX = 20;
        float uiY = DungeonBreakersGame.V_HEIGHT - 20;
        float scale = 5.0f;

        if (uiAtlas != null && uiFont != null && player != null) {
            TextureRegion currentHpBar = getHpRegion(player);
            if (currentHpBar != null) {
                float barWidth = currentHpBar.getRegionWidth() * scale;
                float barHeight = currentHpBar.getRegionHeight() * scale;
                uiY -= barHeight;
                batch.draw(currentHpBar, uiX, uiY, barWidth, barHeight);

                String hpText = (int)player.currentHp + " / " + player.maxHp;
                glyphLayout.setText(uiFont, hpText);
                uiFont.draw(batch, glyphLayout, uiX + (barWidth/2) - (glyphLayout.width/2), uiY + (barHeight/2) + (glyphLayout.height/2));
            }
        }

        float dashIconSize = 50;
        float dashIconY = uiY - dashIconSize - 10;
        float dashIconX = uiX;
        if (dashAtlas != null && player != null) {
            batch.setColor(1, 1, 1, 1);
            for (int i = 0; i < player.maxDashCharges; i++) {
                TextureRegion dashRegion = dash_0;
                if (i < player.currentDashCharges) dashRegion = dash_4;
                else if (i == player.currentDashCharges && player.currentDashCharges < player.maxDashCharges) {
                    float progress = player.dashChargeRegenTimer / player.dashChargeRegenTime;
                    if (progress > 0.8f) dashRegion = dash_3;
                    else if (progress > 0.55f) dashRegion = dash_2;
                    else if (progress > 0.25f) dashRegion = dash_1;
                }
                if (dashRegion != null) batch.draw(dashRegion, dashIconX + (i * (dashIconSize + 10)), dashIconY, dashIconSize, dashIconSize);
            }
        }

        if (player != null) {
            drawBuffs(player, dashIconX, dashIconY - 50);
        }

        float rightX = DungeonBreakersGame.V_WIDTH - 30;
        float bottomY = 40f;
        float iconSize = 32f;

        String goldText = PlayerData.getInstance().gold + " G";
        uiFont.getData().setScale(1.0f);
        glyphLayout.setText(uiFont, goldText);
        float goldTextX = rightX - glyphLayout.width;

        if (goldIcon != null) {
            batch.draw(goldIcon, goldTextX - iconSize - 10, bottomY - iconSize/2 - 5, iconSize, iconSize);
        }
        uiFont.setColor(Color.GOLD);
        uiFont.draw(batch, goldText, goldTextX, bottomY);

        if (starIcon != null) {
            String gemText = PlayerData.getInstance().gems + "";
            glyphLayout.setText(uiFont, gemText);
            float gemTextX = rightX - glyphLayout.width;
            float gemY = bottomY + 40f;
            batch.setColor(1, 1, 1, 1);
            batch.draw(starIcon, gemTextX - iconSize - 10, gemY - iconSize/2 - 5, iconSize, iconSize);
            uiFont.setColor(Color.CYAN);
            uiFont.draw(batch, gemText, gemTextX, gemY);
        }
        uiFont.setColor(Color.WHITE);

        int minutes = (int)gameTime / 60;
        int seconds = (int)gameTime % 60;
        String timeText = String.format("%02d:%02d", minutes, seconds);
        uiFont.getData().setScale(1.2f);
        glyphLayout.setText(uiFont, timeText);
        float timeX = (DungeonBreakersGame.V_WIDTH - glyphLayout.width) / 2;
        uiFont.draw(batch, timeText, timeX, 130f);
        uiFont.getData().setScale(1.0f);

        drawBottomSlots(uiViewport, player);

        batch.end();

        if (isPaused && (inventoryUI == null || !inventoryUI.isVisible())) {
            drawPauseScreen(uiViewport);
        }
    }

    private void drawBuffs(AbstractPlayer player, float x, float y) {
        float iconSize = 40f;
        float gap = 10f;

        // 마우스 좌표
        float mouseX = Gdx.input.getX();
        float mouseY = Gdx.graphics.getHeight() - Gdx.input.getY(); // Y좌표 반전

        for (int i = 0; i < player.activeBuffs.size; i++) {
            AbstractPlayer.Buff buff = player.activeBuffs.get(i);
            if (buff.icon != null) {
                float drawX = x + (i * (iconSize + gap));

                batch.setColor(0.3f, 0.3f, 0.3f, 0.5f);
                batch.draw(buff.icon, drawX, y, iconSize, iconSize);

                float percent = buff.duration / buff.maxDuration;
                TextureRegion region = buff.icon;
                float v = region.getV(); float v2 = region.getV2();
                float vNew = v + (v2 - v) * (1 - percent);

                batch.setColor(1f, 1f, 1f, 1f);
                batch.draw(region.getTexture(), drawX, y, iconSize, iconSize * percent, region.getU(), v2, region.getU2(), vNew);

                String timeStr = String.format("%.1f", buff.duration);
                uiFont.getData().setScale(0.5f);
                glyphLayout.setText(uiFont, timeStr);
                uiFont.draw(batch, timeStr, drawX + (iconSize - glyphLayout.width) / 2, y - 5);
                uiFont.getData().setScale(1.0f);

                // ★★★ [추가] 버프 툴팁 표시 ★★★
                if (mouseX >= drawX && mouseX <= drawX + iconSize && mouseY >= y && mouseY <= y + iconSize) {
                    String tooltip = "";
                    switch(buff.type) {
                        case BUFF_ATTACK: tooltip = "공격력 " + (int)(buff.value * 100) + "% 증가"; break;
                        case BUFF_DEFENSE: tooltip = "방어력 " + (int)buff.value + " 증가"; break;
                        case BUFF_CRIT_CHANCE: tooltip = "치명타 " + (int)(buff.value * 100) + "% 증가"; break;
                        case HEAL_OVER_TIME: tooltip = "0.5초당 체력 1 회복"; break;
                    }
                    uiFont.getData().setScale(0.6f);
                    glyphLayout.setText(uiFont, tooltip);
                    uiFont.draw(batch, tooltip, mouseX + 10, mouseY + 20);
                    uiFont.getData().setScale(1.0f);
                }
            }
        }
        batch.setColor(1, 1, 1, 1);
    }

    private void drawBottomSlots(Viewport uiViewport, AbstractPlayer player) {
        float startX = (DungeonBreakersGame.V_WIDTH - (slotSize * 3 + slotGap * 2)) / 2f;
        float startY = 20f;

        Item quick1 = getEquipmentItem(3);
        Item quick2 = getEquipmentItem(7);

        drawQuickSlot(startX, startY, slotSize, key1, "1", quick1);
        drawQuickSlot(startX + slotSize + slotGap, startY, slotSize, key2, "2", quick2);

        float skillX = startX + (slotSize + slotGap) * 2;
        drawSkillSlot(skillX, startY, slotSize, keyQ, "Q", player);
    }

    private Item getEquipmentItem(int index) {
        if (PlayerData.getInstance().equipmentItems.size > index) return PlayerData.getInstance().equipmentItems.get(index);
        return null;
    }

    private void drawQuickSlot(float x, float y, float size, TextureRegion keyImg, String keyName, Item item) {
        if (slotRegion != null) batch.draw(slotRegion, x, y, size, size);
        if (item != null && itemAtlas != null) {
            TextureRegion icon = itemAtlas.findRegion(item.textureRegionName);
            if (icon != null) {
                float iconSize = size - (quickSlotIconPadding * 2);
                batch.draw(icon, x + quickSlotIconPadding, y + quickSlotIconPadding, iconSize, iconSize);
            }
            if (item.count > 1) {
                String countText = String.valueOf(item.count);
                uiFont.getData().setScale(0.5f); uiFont.setColor(Color.WHITE);
                glyphLayout.setText(uiFont, countText);
                uiFont.draw(batch, countText, x + size - glyphLayout.width - 23, y + glyphLayout.height + 23);
                uiFont.getData().setScale(1f);
            }
        }
        if (keyImg != null) batch.draw(keyImg, x - 10, y + size - 25, 32, 32);
        else {
            uiFont.getData().setScale(0.7f); uiFont.setColor(Color.YELLOW);
            uiFont.draw(batch, keyName, x + 5, y + size - 5);
            uiFont.getData().setScale(1f); uiFont.setColor(Color.WHITE);
        }
    }

    private void drawSkillSlot(float x, float y, float size, TextureRegion keyImg, String keyName, AbstractPlayer player) {
        if (slotRegion != null) batch.draw(slotRegion, x, y, size, size);
        if (player != null && player.getSkillIcon() != null) {
            float iconPadding = skillSlotIconPadding;
            float iconSize = size - (iconPadding * 2);
            float drawX = x + iconPadding; float drawY = y + iconPadding;
            TextureRegion icon = player.getSkillIcon();
            batch.draw(icon.getTexture(), drawX, drawY, iconSize, iconSize, icon.getRegionX(), icon.getRegionY(), icon.getRegionWidth(), icon.getRegionHeight(), false, false);
            if (player.getSkillTimer() > 0 && player.getSkillIconCooldown() != null) {
                float cooldownPercent = player.getSkillTimer() / player.getSkillCooldown();
                TextureRegion cdIcon = player.getSkillIconCooldown();
                float visibleHeight = iconSize * cooldownPercent;
                float u = cdIcon.getU(); float u2 = cdIcon.getU2(); float v = cdIcon.getV(); float v2 = cdIcon.getV2();
                float vNew = v + (v2 - v) * (1 - cooldownPercent);
                batch.setColor(1f, 1f, 1f, 1f);
                batch.draw(cdIcon.getTexture(), drawX, drawY, iconSize, visibleHeight, u, v2, u2, vNew);
            }
            if (player.flashTimer > 0) {
                batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
                batch.setColor(1f, 1f, 1f, player.flashTimer * 5f);
                batch.draw(icon.getTexture(), drawX, drawY, iconSize, iconSize, icon.getRegionX(), icon.getRegionY(), icon.getRegionWidth(), icon.getRegionHeight(), false, false);
                batch.setColor(1f, 1f, 1f, 1f);
                batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            }
        }
        if (keyImg != null) batch.draw(keyImg, x - 10, y + size - 25, 32, 32);
        else {
            uiFont.getData().setScale(0.7f); uiFont.setColor(Color.YELLOW);
            uiFont.draw(batch, keyName, x + 5, y + size - 5);
            uiFont.getData().setScale(1f); uiFont.setColor(Color.WHITE);
        }
    }

    private void drawPauseScreen(Viewport uiViewport) {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        shapeRenderer.setProjectionMatrix(uiViewport.getCamera().combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0, 0, 0, 0.7f);
        shapeRenderer.rect(0, 0, DungeonBreakersGame.V_WIDTH, DungeonBreakersGame.V_HEIGHT);
        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
        batch.begin();
        String pauseText = "PAUSED";
        glyphLayout.setText(uiFont, pauseText);
        uiFont.draw(batch, glyphLayout, (DungeonBreakersGame.V_WIDTH - glyphLayout.width) / 2, DungeonBreakersGame.V_HEIGHT / 2 + 50);
        batch.end();
    }

    private TextureRegion getHpRegion(AbstractPlayer player) {
        if (uiAtlas == null || player == null) return null;
        float percent = (float) player.currentHp / (float) player.maxHp * 100f;
        if (percent > 90) return hp_100; if (percent > 70) return hp_80; if (percent > 50) return hp_60; if (percent > 40) return hp_50; if (percent > 20) return hp_30; if (percent > 0) return hp_10; return hp_0;
    }

    @Override
    public void dispose() {
        if (uiAtlas != null) uiAtlas.dispose();
        if (dashAtlas != null) dashAtlas.dispose();
        if (shapeRenderer != null) shapeRenderer.dispose();
    }
}
