package com.team.dungeonbreakers;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Bonfire {
    public float x, y;
    public float width = 64f;
    public float height = 64f;

    private TextureRegion texture;
    private boolean used = false;

    public Bonfire(float x, float y, CharacterManager charManager) {
        this.x = x;
        this.y = y;
        this.texture = charManager.getHeartTexture();
    }

    public void interact(AbstractPlayer player) {
        if (used) return;

        // 최대 체력의 50% 회복
        int healAmount = player.maxHp / 2;
        player.heal(healAmount);
        System.out.println("Rested at bonfire. Healed: " + healAmount);

        used = true;
    }

    public void draw(SpriteBatch batch) {
        if (texture != null) {
            if (used) batch.setColor(0.5f, 0.5f, 0.5f, 0.5f); // 사용 후 흐리게
            batch.draw(texture, x - width/2, y, width, height);
            batch.setColor(1, 1, 1, 1);
        }
    }

    public boolean isUsed() { return used; }
}
