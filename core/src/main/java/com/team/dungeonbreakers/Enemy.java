package com.team.dungeonbreakers;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

public class Enemy {
    public float x, y;
    public Body body;
    private TextureRegion texture;
    public float width, height;

    private int hp = 1000;
    private boolean alive = true;
    private float scale = 5.0f;

    public Enemy(World world, TextureAtlas atlas, String characterName, float startX, float startY) {
        this.x = startX;
        this.y = startY;

        String idleRegionName = characterName + "-Idle";
        TextureRegion idleStrip = atlas.findRegion(idleRegionName);
        if (idleStrip == null) {
            throw new NullPointerException("Region not found in atlas: " + idleRegionName);
        }

        int frameWidth = idleStrip.getRegionWidth() / 6;
        this.texture = idleStrip.split(frameWidth, idleStrip.getRegionHeight())[0][0];

        this.width = texture.getRegionWidth();
        this.height = texture.getRegionHeight();
        createBody(world);
    }

    private void createBody(World world) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(x / DungeonBreakersGame.PPM, y / DungeonBreakersGame.PPM);
        body = world.createBody(bodyDef);
        body.setFixedRotation(true);
        body.setUserData(this);

        // --- 메인 히트박스 생성 (플레이어와 유사하게) ---
        PolygonShape bodyShape = new PolygonShape();
        float hitboxWidthRatio = 0.1f;
        float hitboxHeightRatio = 0.2f;
        float bodyWidthMeters = (width * scale * hitboxWidthRatio) / 2 / DungeonBreakersGame.PPM;
        float bodyHeightMeters = (height * scale * hitboxHeightRatio) / 2 / DungeonBreakersGame.PPM;
        bodyShape.setAsBox(bodyWidthMeters, bodyHeightMeters);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = bodyShape;
        fixtureDef.density = 1.0f;

        // ★★★ 적 충돌 규칙 설정 ★★★
        fixtureDef.filter.categoryBits = DungeonBreakersGame.ENEMY_BIT; // "나는 적이다"
        fixtureDef.filter.maskBits = DungeonBreakersGame.GROUND_BIT | DungeonBreakersGame.PROJECTILE_BIT; // "나는 땅과 투사체하고만 충돌하겠다"

        body.createFixture(fixtureDef).setUserData("enemy");
        bodyShape.dispose();

        // (적은 점프를 하지 않으므로 발 센서는 일단 생략)
    }

    // ... (takeDamage, isAlive, update, draw 메소드는 그대로)
    public void takeDamage(int damage) {
        if (!alive) return;
        this.hp -= damage;
        if (this.hp <= 0) {
            alive = false;
        }
    }

    public boolean isAlive() {
        return alive;
    }

    public void update() {
        if (alive) {
            this.x = body.getPosition().x * DungeonBreakersGame.PPM;
            this.y = body.getPosition().y * DungeonBreakersGame.PPM;
        }
    }

    public void draw(SpriteBatch batch) {
        if (alive) {
            float yOffset = -10f;
            float xOffset = -10f;
            batch.draw(texture, x - (width * scale / 2)+xOffset, y - (height * scale / 2)+yOffset, width * scale, height * scale);
        }
    }
}
