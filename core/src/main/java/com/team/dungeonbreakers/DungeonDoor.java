package com.team.dungeonbreakers;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class DungeonDoor {
    public float x, y;

    // ★★★ [수정] 문 크기 확대 (기존 96/78 -> 144/117, 약 1.5배) ★★★
    public float width = 144f;
    public float height = 117f;

    private boolean isOpen = true;
    private boolean isLobbyDoor;

    private CharacterManager charManager;

    public DungeonDoor(float x, float y, boolean isLobbyDoor, CharacterManager charManager) {
        this.x = x;
        this.y = y;
        this.isLobbyDoor = isLobbyDoor;
        this.charManager = charManager;
    }

    public void update(boolean combatActive) {
        if (isLobbyDoor) {
            isOpen = true;
        } else {
            isOpen = !combatActive;
        }
    }

    public void draw(SpriteBatch batch) {
        TextureRegion region = charManager.getDoorTexture(isOpen);
        if (region != null) {
            // 좌표는 중앙 하단 기준이므로 그리기 좌표 보정
            batch.draw(region, x - width/2, y, width, height);
        }
    }

    public boolean isOpen() {
        return isOpen;
    }
}
