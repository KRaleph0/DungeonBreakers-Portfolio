package com.team.dungeonbreakers;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;

public class Dummy extends AbstractEnemy {
    private float damageTakenTimer = 0f;
    private int totalDamageTaken = 0;
    private float dps = 0f;
    private float timeSinceLastHit = 0f;

    public Dummy(World world, CharacterManager charManager, float startX, float startY) {
        // ★★★ [수정] Slime 아틀라스 사용 ★★★
        // (만약 Slime 아틀라스가 없거나 이름이 다르면 AbstractEnemy에서 안전하게 처리됨)
        super(world, startX, startY,
            createAnimation(charManager.getAtlas("Slime"), "Slime-Idle", 6, 0.2f),
            createAnimation(charManager.getAtlas("Slime"), "Slime-Walk", 8, 0.1f),
            null,
            createAnimation(charManager.getAtlas("Slime"), "Slime-Hurt", 4, 0.1f),
            createAnimation(charManager.getAtlas("Slime"), "Slime-Death", 4, 0.15f),
            4.0f, 999999, 0.1f, 0.2f, 0f, -10f);

        this.hasAggro = false;
    }

    private static Animation<TextureRegion> createAnimation(TextureAtlas atlas, String regionName, int frameCount, float frameDuration) {
        if (atlas == null) return null;
        TextureRegion region = atlas.findRegion(regionName);
        if (region == null) return null; // 리전이 없으면 null 반환

        TextureRegion[] frames = new TextureRegion[frameCount];
        int frameWidth = region.getRegionWidth() / frameCount;
        for (int i = 0; i < frameCount; i++) {
            frames[i] = new TextureRegion(region, i * frameWidth, 0, frameWidth, region.getRegionHeight());
        }
        return new Animation<>(frameDuration, frames);
    }

    @Override
    public void update(AbstractPlayer player, Array<Projectile> projectiles, float deltaTime) {
        super.update(player, projectiles, deltaTime);

        body.setLinearVelocity(0, body.getLinearVelocity().y);

        if (totalDamageTaken > 0) {
            damageTakenTimer += deltaTime;
            timeSinceLastHit += deltaTime;
            if (damageTakenTimer > 0) {
                dps = totalDamageTaken / damageTakenTimer;
            }
            if (timeSinceLastHit > 5.0f) {
                totalDamageTaken = 0;
                damageTakenTimer = 0f;
                dps = 0f;
            }
        }

        if (hp < 999900) hp = 999999;
    }

    @Override
    public void takeDamage(int damage) {
        super.takeDamage(damage);
        totalDamageTaken += damage;
        timeSinceLastHit = 0f;
        if (hp <= 0) {
            hp = 999999;
            alive = true;
            isDying = false;
        }
    }

    public void drawStats(SpriteBatch batch, BitmapFont font) {
        if (totalDamageTaken > 0) {
            String dpsText = "DPS: " + (int)dps;
            String dmgText = "Total: " + totalDamageTaken;
            font.draw(batch, dpsText, x - 20, y + 120);
            font.draw(batch, dmgText, x - 20, y + 100);
        }
    }
}
