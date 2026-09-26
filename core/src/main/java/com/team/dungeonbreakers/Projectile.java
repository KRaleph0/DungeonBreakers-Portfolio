package com.team.dungeonbreakers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import java.util.HashSet;
import java.util.Set;

public class Projectile {
    public Body body;
    private TextureRegion textureRegion;
    public float x, y;
    private float width, height;
    private boolean isEnemy;
    private boolean active;
    private float stateTime;

    public int damage;
    public int penetration;
    private Set<AbstractEnemy> hitEnemies;

    public Projectile(World world, TextureRegion textureRegion, float startX, float startY, Vector2 velocity, boolean isEnemy, int damage) {
        this(world, textureRegion, startX, startY, velocity, isEnemy, damage, 1);
    }

    public Projectile(World world, TextureRegion textureRegion, float startX, float startY, Vector2 velocity, boolean isEnemy, int damage, int penetration) {
        this.textureRegion = textureRegion;
        this.x = startX;
        this.y = startY;
        this.isEnemy = isEnemy;
        this.active = true;
        this.stateTime = 0;
        this.damage = damage;
        this.penetration = penetration;
        this.hitEnemies = new HashSet<>();

        // ★★★ [수정] 화살 크기 확대 (64f -> 96f) ★★★
        if (textureRegion != null) {
            float aspect = (float)textureRegion.getRegionHeight() / textureRegion.getRegionWidth();
            this.width = 192f;  // 기존 64f에서 1.5배 확대
            this.height = 192f * aspect;
        } else {
            this.width = 48; // 기본값도 약간 상향
            this.height = 16;
        }

        createBody(world, velocity);
    }

    private void createBody(World world, Vector2 velocity) {
        BodyDef bdef = new BodyDef();
        bdef.position.set(x / DungeonBreakersGame.PPM, y / DungeonBreakersGame.PPM);
        bdef.type = BodyDef.BodyType.DynamicBody;
        bdef.bullet = true; // 고속 이동 물체 충돌 감지

        body = world.createBody(bdef);
        body.setGravityScale(0f); // 중력 영향 없음
        body.setUserData(this);

        FixtureDef fdef = new FixtureDef();
        PolygonShape shape = new PolygonShape();

        // 히트박스 크기 (이미지 크기에 비례하여 자동 조정됨)
        float hitW = (width / 2 / DungeonBreakersGame.PPM) * 0.8f;
        float hitH = (height / 2 / DungeonBreakersGame.PPM) * 0.4f;
        shape.setAsBox(hitW, hitH);

        fdef.shape = shape;
        fdef.isSensor = true; // 물리적 충돌 없이 통과 (감지만 함)

        if (isEnemy) {
            fdef.filter.categoryBits = DungeonBreakersGame.ENEMY_PROJECTILE_BIT;
            // PLATFORM_BIT 제외 (플랫폼 통과)
            fdef.filter.maskBits = DungeonBreakersGame.PLAYER_BIT | DungeonBreakersGame.GROUND_BIT;
            body.createFixture(fdef).setUserData("enemyProjectile");
        } else {
            fdef.filter.categoryBits = DungeonBreakersGame.PROJECTILE_BIT;
            // PLATFORM_BIT 제외 (플랫폼 통과)
            fdef.filter.maskBits = DungeonBreakersGame.ENEMY_BIT | DungeonBreakersGame.GROUND_BIT;
            body.createFixture(fdef).setUserData("projectile");
        }

        body.setLinearVelocity(velocity);

        float angle = velocity.angleDeg();
        body.setTransform(body.getPosition(), (float)Math.toRadians(angle));

        shape.dispose();
    }

    public boolean onHit(AbstractEnemy enemy) {
        if (!active) return false;
        if (hitEnemies.contains(enemy)) return false;

        enemy.takeDamage(this.damage);
        hitEnemies.add(enemy);

        penetration--;
        if (penetration <= 0) {
            destroy();
        }
        return true;
    }

    public void update() {
        if (active && body != null) {
            x = body.getPosition().x * DungeonBreakersGame.PPM;
            y = body.getPosition().y * DungeonBreakersGame.PPM;
            stateTime += Gdx.graphics.getDeltaTime();
        }
    }

    public void draw(SpriteBatch batch) {
        if (active && textureRegion != null) {
            float originX = width / 2;
            float originY = height / 2;
            float rotation = body.getAngle() * MathUtils.radiansToDegrees;

            batch.draw(textureRegion, x - width / 2, y - height / 2, originX, originY, width, height, 1, 1, rotation);
        }
    }

    public boolean isActive() { return active; }
    public void destroy() { active = false; }
    public void dispose() {}
}
