package com.team.dungeonbreakers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector3;

public class JobChangeUI {
    private boolean isVisible = false;
    private GameScreen gameScreen;
    private BitmapFont font;
    private GlyphLayout layout;

    private Texture backTexture;
    private TextureRegion slotRegion;
    private TextureRegion knightIcon;
    private TextureRegion archerIcon;

    private final float UI_WIDTH = 1000f;
    private final float UI_HEIGHT = 600f;
    private final float SLOT_SIZE = 200f;

    public JobChangeUI(GameScreen gameScreen, CharacterManager charManager, BitmapFont font) {
        this.gameScreen = gameScreen;
        this.font = font;
        this.layout = new GlyphLayout();

        this.backTexture = charManager.getUIBackTexture();
        this.slotRegion = charManager.getSlotCoverTexture();

        AnimationData kData = charManager.getAnimationData("Knight");
        if (kData != null && kData.idle != null) knightIcon = kData.idle.getKeyFrame(0);

        AnimationData aData = charManager.getAnimationData("Archer");
        if (aData != null && aData.idle != null) archerIcon = aData.idle.getKeyFrame(0);
    }

    public void draw(SpriteBatch batch, Vector3 mousePos) {
        if (!isVisible) return;

        float oldScaleX = font.getData().scaleX;
        float oldScaleY = font.getData().scaleY;
        Color oldColor = font.getColor();

        float centerX = 1600f / 2;
        float centerY = 900f / 2;
        float startX = centerX - UI_WIDTH / 2;
        float startY = centerY - UI_HEIGHT / 2;

        if (backTexture != null) batch.draw(backTexture, startX, startY, UI_WIDTH, UI_HEIGHT);
        else {
            batch.setColor(0.1f, 0.1f, 0.1f, 0.95f);
            if (slotRegion != null) batch.draw(slotRegion, startX, startY, UI_WIDTH, UI_HEIGHT);
            batch.setColor(1, 1, 1, 1);
        }

        font.getData().setScale(1.2f);
        font.setColor(Color.GOLD);
        layout.setText(font, "직업 선택");
        font.draw(batch, "직업 선택", centerX - layout.width / 2, startY + UI_HEIGHT - 50);

        float slotY = centerY - SLOT_SIZE / 2;
        float knightX = centerX - SLOT_SIZE - 50;
        float archerX = centerX + 50;

        // 기사는 항상 해금
        drawJobSlot(batch, "Knight", knightIcon, knightX, slotY, mousePos, true);

        // ★★★ [수정] 궁수 해금 여부 체크 ★★★
        boolean isArcherUnlocked = PlayerData.getInstance().isArcherUnlocked;
        drawJobSlot(batch, "Archer", archerIcon, archerX, slotY, mousePos, isArcherUnlocked);

        font.getData().setScale(oldScaleX, oldScaleY);
        font.setColor(oldColor);
    }

    private void drawJobSlot(SpriteBatch batch, String jobName, TextureRegion icon, float x, float y, Vector3 mousePos, boolean isUnlocked) {
        boolean isSelected = PlayerData.getInstance().currentJob.equals(jobName);
        boolean isHovered = (mousePos.x >= x && mousePos.x <= x + SLOT_SIZE && mousePos.y >= y && mousePos.y <= y + SLOT_SIZE);

        if (slotRegion != null) {
            if (isSelected) batch.setColor(0.5f, 1f, 0.5f, 1f);
            else if (isHovered && isUnlocked) batch.setColor(0.8f, 0.8f, 0.8f, 1f);
            else batch.setColor(0.5f, 0.5f, 0.5f, 1f);

            batch.draw(slotRegion, x, y, SLOT_SIZE, SLOT_SIZE);
            batch.setColor(1, 1, 1, 1);
        }

        if (icon != null) {
            if (!isUnlocked) batch.setColor(0.2f, 0.2f, 0.2f, 1f); // 잠김: 어둡게
            batch.draw(icon, x + 20, y + 20, SLOT_SIZE - 40, SLOT_SIZE - 40);
            batch.setColor(1, 1, 1, 1);
        }

        font.getData().setScale(0.8f);
        font.setColor(Color.WHITE);
        String dispName = jobName.equals("Knight") ? "기사" : "궁수";
        layout.setText(font, dispName);
        font.draw(batch, dispName, x + (SLOT_SIZE - layout.width) / 2, y - 20);

        if (isSelected) {
            font.setColor(Color.GREEN);
            font.getData().setScale(1.0f);
            font.draw(batch, "V", x + SLOT_SIZE - 40, y + SLOT_SIZE - 10);
        }

        // 잠김 표시
        if (!isUnlocked) {
            font.setColor(Color.RED);
            font.getData().setScale(0.6f);
            layout.setText(font, "잠김");
            font.draw(batch, "잠김", x + (SLOT_SIZE - layout.width) / 2, y + SLOT_SIZE / 2);
        }

        if (isUnlocked && isHovered && Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            if (!isSelected) {
                PlayerData.getInstance().currentJob = jobName;
                gameScreen.respawnPlayer();
            }
        }
    }

    public void toggle() { isVisible = !isVisible; }
    public void close() { isVisible = false; }
    public boolean isVisible() { return isVisible; }
}
