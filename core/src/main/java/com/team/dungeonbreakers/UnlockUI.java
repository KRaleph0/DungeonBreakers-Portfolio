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

public class UnlockUI {
    // ... (기존 필드 유지)
    private boolean isVisible = false;
    private BitmapFont font;
    private GlyphLayout layout;
    private Texture backTexture;
    private TextureRegion slotRegion;
    private Texture starIcon;
    private TextureRegion archerIcon;
    private Texture luckyIcon;
    private Texture bookIcon;
    private final float UI_WIDTH = 1000f;
    private final float UI_HEIGHT = 500f;
    private final float ITEM_WIDTH = 250f;
    private final int COST_ARCHER = 50;
    private final int COST_SLOT = 200;
    private final int COST_PREMIUM = 300;

    public UnlockUI(CharacterManager charManager, BitmapFont font) {
        this.font = font;
        this.layout = new GlyphLayout();
        this.backTexture = charManager.getUIBackTexture();
        this.slotRegion = charManager.getSlotCoverTexture();
        this.starIcon = charManager.getStarIcon();

        // ★★★ [수정] 아이콘 로드 ★★★
        this.luckyIcon = charManager.getLuckyIcon();
        this.bookIcon = charManager.getBookIcon();

        AnimationData aData = charManager.getAnimationData("Archer");
        if (aData != null && aData.idle != null) archerIcon = aData.idle.getKeyFrame(0);
    }

    public void draw(SpriteBatch batch, Vector3 mousePos) {
        if (!isVisible) return;

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

        font.getData().setScale(1.0f);
        font.setColor(Color.GOLD);
        layout.setText(font, "해금 상점");
        font.draw(batch, "해금 상점", centerX - layout.width / 2, startY + UI_HEIGHT - 40);

        font.getData().setScale(0.6f);
        font.setColor(Color.CYAN);
        String gems = "보유: " + PlayerData.getInstance().gems + " Stars";
        layout.setText(font, gems);
        font.draw(batch, gems, startX + UI_WIDTH - layout.width - 30, startY + UI_HEIGHT - 40);

        float itemY = centerY - 50;
        float gap = 50f;
        float startItemX = centerX - (ITEM_WIDTH * 3 + gap * 2) / 2 + ITEM_WIDTH/2;

        // ★★★ [수정] 아이콘 전달 ★★★
        drawUnlockItem(batch, startItemX, itemY, "궁수 직업", archerIcon, 1, mousePos);
        // 상점 확장은 책 아이콘
        TextureRegion bookReg = (bookIcon != null) ? new TextureRegion(bookIcon) : slotRegion;
        drawUnlockItem(batch, startItemX + ITEM_WIDTH + gap, itemY, "상점 확장", bookReg, 2, mousePos);
        // 고급 상점은 럭키 아이콘
        TextureRegion luckyReg = (luckyIcon != null) ? new TextureRegion(luckyIcon) : slotRegion;
        drawUnlockItem(batch, startItemX + (ITEM_WIDTH + gap) * 2, itemY, "고급 상점", luckyReg, 3, mousePos);

        font.getData().setScale(0.5f);
        font.setColor(Color.LIGHT_GRAY);
        font.draw(batch, "[ESC] 닫기", startX + 20, startY + 30);
    }

    private void drawUnlockItem(SpriteBatch batch, float x, float y, String title, TextureRegion icon, int type, Vector3 mousePos) {
        // ... (기본 로직 동일) ...
        PlayerData pd = PlayerData.getInstance();
        boolean isUnlocked = false;
        int cost = 0;
        String desc = "";

        if (type == 1) { isUnlocked = pd.isArcherUnlocked; cost = COST_ARCHER; desc = "새로운 직업 '궁수'를\n해금합니다."; }
        else if (type == 2) { if (pd.shopSlotCount >= 8) { isUnlocked = true; desc = "최대 확장됨"; } else { cost = COST_SLOT; desc = "상점의 장비 칸을\n2개 더 늘립니다."; } }
        else if (type == 3) { isUnlocked = pd.isShopPremium; cost = COST_PREMIUM; desc = "상점에서 높은 등급의\n아이템이 등장합니다."; }

        float halfW = ITEM_WIDTH / 2; float halfH = 150f;
        boolean isHovered = (mousePos.x >= x - halfW && mousePos.x <= x + halfW && mousePos.y >= y - halfH && mousePos.y <= y + halfH);

        if (slotRegion != null) {
            batch.setColor(isHovered ? Color.DARK_GRAY : Color.BLACK);
            batch.draw(slotRegion, x - halfW, y - halfH, ITEM_WIDTH, 300f);
            batch.setColor(Color.WHITE);
        }

        if (icon != null) {
            // ★★★ [수정] 궁수(type 1)인 경우 2배 확대 ★★★
            float size = (type == 1) ? 160f : 80f;
            batch.draw(icon, x - size/2, y + 40 - (size-80)/2, size, size);
        }

        font.getData().setScale(0.7f); font.setColor(Color.YELLOW); layout.setText(font, title); font.draw(batch, title, x - layout.width / 2, y + 30);
        font.getData().setScale(0.5f); font.setColor(Color.LIGHT_GRAY);
        String[] lines = desc.split("\n"); float descY = y - 10;
        for (String line : lines) { layout.setText(font, line); font.draw(batch, line, x - layout.width / 2, descY); descY -= 20; }

        float btnY = y - 120;
        if (isUnlocked) { font.setColor(Color.GREEN); layout.setText(font, "해금됨"); font.draw(batch, "해금됨", x - layout.width / 2, btnY); }
        else {
            boolean canAfford = pd.gems >= cost;
            if (starIcon != null) batch.draw(starIcon, x - 40, btnY - 10, 24, 24);
            font.setColor(canAfford ? Color.WHITE : Color.RED); font.draw(batch, cost + "", x - 10, btnY + 10);
            if (isHovered && Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) { if (canAfford) purchase(type, cost); else System.out.println("Not enough gems!"); }
        }
    }

    private void purchase(int type, int cost) {
        PlayerData pd = PlayerData.getInstance();
        pd.gems -= cost;
        if (type == 1) pd.isArcherUnlocked = true;
        else if (type == 2) pd.shopSlotCount = 8;
        else if (type == 3) pd.isShopPremium = true;
        pd.save(1);
    }

    public void toggle() { isVisible = !isVisible; }
    public void close() { isVisible = false; }
    public boolean isVisible() { return isVisible; }
}
