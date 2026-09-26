package com.team.dungeonbreakers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class MainMenuScreen implements Screen {
    private final DungeonBreakersGame game;
    private Viewport viewport;
    private final GlyphLayout layout = new GlyphLayout();
    private Texture slotTexture;
    private Texture starIcon;

    private int selectedSlot = 0; // 0, 1, 2 (Save slots)
    private final Vector3 mousePos = new Vector3();

    // ★★★ [추가] 슬롯 상태 캐싱용 배열 ★★★
    private final boolean[] isSlotEmpty = new boolean[3];
    private final int[] slotGems = new int[3];

    public MainMenuScreen(DungeonBreakersGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        viewport = new FitViewport(1600, 900);
        try {
            slotTexture = new Texture(Gdx.files.internal("img/UI/save_slot_box.png"));

            // 스타 아이콘 가져오기
            if (game.characterManager != null) {
                starIcon = game.characterManager.getStarIcon();
            }
            if (starIcon == null && Gdx.files.internal("img/ect/star.png").exists()) {
                starIcon = new Texture(Gdx.files.internal("img/ect/star.png"));
            }

            // ★★★ [추가] 세이브 데이터 미리 읽기 ★★★
            for (int i = 0; i < 3; i++) {
                // SaveManager와 동일한 이름 규칙 사용 ("dungeon_breakers_save_" + slotNumber)
                Preferences prefs = Gdx.app.getPreferences("dungeon_breakers_save_" + (i + 1));

                // "gems" 키가 있으면 세이브가 있다고 판단 (또는 다른 필수 키 확인)
                if (prefs.contains("gems")) {
                    isSlotEmpty[i] = false;
                    slotGems[i] = prefs.getInteger("gems", 0);
                } else {
                    isSlotEmpty[i] = true;
                    slotGems[i] = 0;
                }
            }

        } catch (Exception e) {
            // 로드 실패 시 무시
        }
    }

    @Override
    public void render(float delta) {
        // 입력 처리
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            // 데이터 로드 후 게임 시작 (없는 슬롯이면 새로 시작됨)
            PlayerData.getInstance().load(selectedSlot + 1);
            game.setScreen(new GameScreen(game, selectedSlot + 1));
            return;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.A) || Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
            selectedSlot = (selectedSlot - 1 + 3) % 3;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.D) || Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
            selectedSlot = (selectedSlot + 1) % 3;
        }

        mousePos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        viewport.unproject(mousePos);

        float startX = 200;
        float gap = 450;
        float y = 250;
        float width = 300;
        float height = 400;

        for (int i = 0; i < 3; i++) {
            float x = startX + (i * gap);

            if (mousePos.x >= x && mousePos.x <= x + width &&
                mousePos.y >= y && mousePos.y <= y + height) {

                selectedSlot = i;

                if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
                    PlayerData.getInstance().load(selectedSlot + 1);
                    game.setScreen(new GameScreen(game, selectedSlot + 1));
                    return;
                }
            }
        }

        Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        game.batch.setProjectionMatrix(viewport.getCamera().combined);
        game.batch.begin();

        if (game.menuFont != null) {
            game.menuFont.setColor(Color.WHITE);
            layout.setText(game.menuFont, "DUNGEON BREAKERS");
            game.menuFont.draw(game.batch, layout, (1600 - layout.width) / 2, 800);
        }

        for (int i = 0; i < 3; i++) {
            float x = startX + (i * gap);

            // 선택된 슬롯 강조
            if (i == selectedSlot) game.batch.setColor(1f, 1f, 1f, 1f);
            else game.batch.setColor(0.5f, 0.5f, 0.5f, 1f);

            if (slotTexture != null) {
                game.batch.draw(slotTexture, x, y, width, height);
            }

            if (game.uiFont != null) {
                game.uiFont.setColor(Color.WHITE);
                String text = "Save Slot " + (i + 1);
                layout.setText(game.uiFont, text);

                float textX = x + (width - layout.width) / 2;
                float textY = y + height - 50;

                game.uiFont.draw(game.batch, text, textX, textY);

                // ★★★ [수정] 슬롯 상태에 따라 표시 내용 변경 ★★★
                if (!isSlotEmpty[i]) {
                    // 세이브가 있는 경우: 보석 표시 + Start
                    if (starIcon != null) {
                        float iconSize = 32;
                        float iconX = x + (width - iconSize) / 2 - 20;
                        float iconY = textY - 50; // 슬롯 이름 아래

                        game.batch.setColor(1, 1, 1, 1);
                        game.batch.draw(starIcon, iconX, iconY, iconSize, iconSize);

                        // 저장된 보석 개수 표시
                        game.uiFont.setColor(Color.YELLOW);
                        game.uiFont.draw(game.batch, String.valueOf(slotGems[i]), iconX + iconSize + 10, iconY + 24);
                        game.uiFont.setColor(Color.WHITE);

                        // 슬롯 색상 복구
                        if (i == selectedSlot) game.batch.setColor(1f, 1f, 1f, 1f);
                        else game.batch.setColor(0.5f, 0.5f, 0.5f, 1f);
                    }

                    String info = "Start";
                    layout.setText(game.uiFont, info);
                    game.uiFont.draw(game.batch, info, x + (width - layout.width) / 2, y + height / 2 - 20);

                } else {
                    // 세이브가 없는 경우: Empty 표시
                    String info = "Empty";
                    game.uiFont.setColor(Color.GRAY);
                    layout.setText(game.uiFont, info);
                    game.uiFont.draw(game.batch, info, x + (width - layout.width) / 2, y + height / 2 - 20);
                    game.uiFont.setColor(Color.WHITE);
                }
            }
        }
        game.batch.setColor(1, 1, 1, 1);

        if (game.uiFont != null) {
            String guide = "Select Slot & Press ENTER (or Click)";
            layout.setText(game.uiFont, guide);
            game.uiFont.draw(game.batch, guide, (1600 - layout.width) / 2, 150);
        }

        game.batch.end();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() { dispose(); }

    @Override
    public void dispose() {
        if (slotTexture != null) slotTexture.dispose();
        if (starIcon != null && (game.characterManager == null || starIcon != game.characterManager.getStarIcon())) {
            starIcon.dispose();
        }
    }
}
