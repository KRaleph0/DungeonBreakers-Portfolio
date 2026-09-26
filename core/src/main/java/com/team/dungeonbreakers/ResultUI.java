package com.team.dungeonbreakers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;

public class ResultUI {
    private boolean isVisible = false;
    private GameScreen gameScreen;
    private BitmapFont font;
    private GlyphLayout layout;

    private Texture backTexture;
    private Texture starIcon;

    private final float UI_WIDTH = 800f;
    private final float UI_HEIGHT = 500f;

    private int earnedStars = 0;
    private boolean isVictory = false; // ★ 승리 여부 플래그

    public ResultUI(GameScreen gameScreen, CharacterManager charManager, BitmapFont font) {
        this.gameScreen = gameScreen;
        this.font = font;
        this.layout = new GlyphLayout();

        this.backTexture = charManager.getUIBackTexture();
        this.starIcon = charManager.getStarIcon();
    }

    // ★★★ [수정] isVictory 인자 추가 ★★★
    public void show(int stars, boolean isVictory) {
        this.earnedStars = stars;
        this.isVictory = isVictory;
        this.isVisible = true;

        PlayerData pd = PlayerData.getInstance();
        pd.addGems(stars);

        pd.resetRun();
        pd.save(1);
    }

    public void draw(SpriteBatch batch, Vector3 mousePos) {
        if (!isVisible) return;

        float centerX = 1600f / 2;
        float centerY = 900f / 2;
        float startX = centerX - UI_WIDTH / 2;
        float startY = centerY - UI_HEIGHT / 2;

        batch.setColor(0.1f, 0.1f, 0.1f, 0.95f);
        if (backTexture != null) batch.draw(backTexture, startX, startY, UI_WIDTH, UI_HEIGHT);
        batch.setColor(1, 1, 1, 1);

        // ★★★ [수정] 승리/패배 텍스트 분기 ★★★
        font.getData().setScale(1.5f);
        if (isVictory) {
            font.setColor(Color.YELLOW);
            layout.setText(font, "VICTORY!");
            font.draw(batch, "VICTORY!", centerX - layout.width / 2, startY + UI_HEIGHT - 80);
        } else {
            font.setColor(Color.RED);
            layout.setText(font, "GAME OVER");
            font.draw(batch, "GAME OVER", centerX - layout.width / 2, startY + UI_HEIGHT - 80);
        }

        font.getData().setScale(1.0f);
        font.setColor(Color.WHITE);
        String rewardText = "Rewards:";
        layout.setText(font, rewardText);
        font.draw(batch, rewardText, centerX - layout.width / 2, centerY + 20);

        if (starIcon != null) {
            float iconSize = 48f;
            batch.draw(starIcon, centerX - iconSize - 10, centerY - 50, iconSize, iconSize);
            font.setColor(Color.YELLOW);
            font.draw(batch, "+" + earnedStars, centerX + 10, centerY - 15);
        }

        float btnW = 300f; float btnH = 60f; float btnX = centerX - btnW / 2; float btnY = startY + 50f;
        boolean isHovered = (mousePos.x >= btnX && mousePos.x <= btnX + btnW && mousePos.y >= btnY && mousePos.y <= btnY + btnH);

        if (isHovered) font.setColor(Color.YELLOW); else font.setColor(Color.WHITE);
        layout.setText(font, "Return to Lobby");
        font.draw(batch, "Return to Lobby", centerX - layout.width / 2, btnY + btnH / 2 + 10);

        if (isHovered && Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            returnToLobby();
        }
    }

    private void returnToLobby() {
        gameScreen.goToMainMenu();
    }

    public boolean isVisible() { return isVisible; }
}
