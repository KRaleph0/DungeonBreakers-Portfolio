package com.team.dungeonbreakers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.Array;

public class ArmoredSkeleton extends AbstractEnemy {

    private final float attackCooldown = 2.0f;
    private float attackTimer = 0f;
    private final float attackWindUp = 0.5f;
    private final float attackActiveTime = 0.4f;
    private Fixture attackHitbox = null;

    // ★★★ [수정] 점프력 상향 및 쿨타임 감소 ★★★
    private float jumpCooldown = 2.0f; // 3.0f -> 2.0f (더 자주 점프)
    private float jumpTimer = 0f;
    private float jumpForce =60f; // 22f -> 35f (플레이어와 비슷한 높이로 점프)

    public ArmoredSkeleton(World world, TextureAtlas atlas, float startX, float startY) {
        super(world, startX, startY,
            createAnimationFromStrip(atlas, "Armored_Skeleton-Idle", 6, 0.2f),
            createAnimationFromStrip(atlas, "Armored_Skeleton-Walk", 8, 0.15f),
            createAnimationFromStrip(atlas, "Armored_Skeleton-Attack01", 8, 0.1f),
            createAnimationFromStrip(atlas, "Armored_Skeleton-Hurt", 4, 0.15f),
            createAnimationFromStrip(atlas, "Armored_Skeleton-Death", 4, 0.15f),
            4.0f, 120, 0.1f, 0.2f, 0f, -10f);

        if (this.attackAnimation != null) this.attackAnimation.setPlayMode(Animation.PlayMode.NORMAL);
    }

    private static Animation<TextureRegion> createAnimationFromStrip(TextureAtlas atlas, String regionName, int frameCount, float frameDuration) {
        TextureRegion region = atlas.findRegion(regionName);
        if (region == null) throw new NullPointerException("Region not found in atlas: " + regionName);
        TextureRegion[] frames = new TextureRegion[frameCount];
        int frameWidth = region.getRegionWidth() / frameCount;
        for (int i = 0; i < frameCount; i++) {
            frames[i] = new TextureRegion(region, i * frameWidth, 0, frameWidth, region.getRegionHeight());
        }
        Animation<TextureRegion> animation = new Animation<>(frameDuration, frames);
        animation.setPlayMode(Animation.PlayMode.LOOP);
        return animation;
    }

    @Override
    public void update(AbstractPlayer player, Array<Projectile> projectiles, float deltaTime) {
        if (isHurt) {
            super.update(player, projectiles, deltaTime);
            return;
        }

        super.update(player, projectiles, deltaTime);
        if (!alive) return;

        if (attackTimer > 0) attackTimer -= deltaTime;
        if (jumpTimer > 0) jumpTimer -= deltaTime;

        if (isAttacking) {
            if (attackAnimation != null && attackAnimation.isAnimationFinished(attackAnimationTimer)) {
                isAttacking = false;
                destroyAttackHitbox();
            }
        } else {
            float distanceToPlayerX = player.x - this.x;
            float distanceToPlayerY = player.y - this.y;
            float attackRange = 250f;
            float detectRange = 500f;
            float moveSpeed = 5f;

            if (!hasAggro && Math.abs(distanceToPlayerX) < detectRange) {
                hasAggro = true;
            }

            if (hasAggro) {
                facingRight = (distanceToPlayerX > 0);

                if (Math.abs(distanceToPlayerX) < attackRange && Math.abs(distanceToPlayerY) < 50) {
                    isAttacking = true;
                    attackAnimationTimer = 0f;
                    attackTimer = attackCooldown;
                    body.setLinearVelocity(0, body.getLinearVelocity().y);
                    isWalking = false;
                    Timer.schedule(new Timer.Task() { @Override public void run() { if (isAttacking) createAttackHitbox(); }}, attackWindUp);
                } else {
                    float velocityX = Math.signum(distanceToPlayerX) * moveSpeed;

                    // ★★★ [수정] 점프 조건: 플레이어가 더 높은 곳에 있을 때 (Y차이 > 80f)
                    if (distanceToPlayerY > 80f && Math.abs(body.getLinearVelocity().y) < 0.1f && jumpTimer <= 0) {
                        body.applyLinearImpulse(new Vector2(0, jumpForce), body.getWorldCenter(), true);
                        jumpTimer = jumpCooldown;
                    }

                    body.setLinearVelocity(velocityX, body.getLinearVelocity().y);
                    isWalking = true;
                }
            } else {
                body.setLinearVelocity(0, body.getLinearVelocity().y);
                isWalking = false;
            }
        }
    }

    private void createAttackHitbox() {
        if (body == null || !alive) return;

        destroyAttackHitbox();

        PolygonShape attackShape = new PolygonShape();
        float hitboxWidthMeters = 200f / DungeonBreakersGame.PPM;
        float hitboxHeightMeters = 40f / DungeonBreakersGame.PPM;

        float enemyBodyHalfWidthMeters = (this.width * this.scale * 0.1f) / 2 / DungeonBreakersGame.PPM;
        float offsetX = (facingRight ? 1 : -1) * (enemyBodyHalfWidthMeters + hitboxWidthMeters / 2);

        attackShape.setAsBox(hitboxWidthMeters / 2, hitboxHeightMeters / 2, new Vector2(offsetX, 0), 0);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = attackShape;
        fixtureDef.isSensor = true;
        fixtureDef.filter.categoryBits = DungeonBreakersGame.ENEMY_BIT;
        fixtureDef.filter.maskBits = DungeonBreakersGame.PLAYER_BIT;

        attackHitbox = body.createFixture(fixtureDef);
        attackHitbox.setUserData("enemyAttack");

        attackShape.dispose();

        checkImmediatePlayerCollision();

        body.setAwake(true);
        Array<Body> bodies = new Array<>();
        body.getWorld().getBodies(bodies);
        for (Body b : bodies) {
            b.setAwake(true);
        }

        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                destroyAttackHitbox();
            }
        }, Math.max(attackActiveTime, 0.1f));
    }

    private void checkImmediatePlayerCollision() {
        if (attackHitbox == null || body == null || !alive) return;

        World world = body.getWorld();
        Array<Body> bodies = new Array<>();
        world.getBodies(bodies);

        float hitboxWidthMeters = 200f / DungeonBreakersGame.PPM;
        float hitboxHeightMeters = 40f / DungeonBreakersGame.PPM;

        Vector2 basePos = body.getPosition();
        float enemyBodyHalfWidthMeters = (this.width * this.scale * 0.1f) / 2 / DungeonBreakersGame.PPM;
        float offsetX = (facingRight ? 1 : -1) * (enemyBodyHalfWidthMeters + hitboxWidthMeters / 2);

        float attackCenterX = basePos.x + offsetX;
        float attackCenterY = basePos.y;

        float attackLeft = attackCenterX - hitboxWidthMeters / 2;
        float attackRight = attackCenterX + hitboxWidthMeters / 2;
        float attackTop = attackCenterY + hitboxHeightMeters / 2;
        float attackBottom = attackCenterY - hitboxHeightMeters / 2;

        for (Body b : bodies) {
            for (Fixture f : b.getFixtureList()) {
                if ("playerBody".equals(f.getUserData())) {
                    Vector2 playerPos = b.getPosition();
                    float playerHalfWidth = 30f / DungeonBreakersGame.PPM;
                    float playerHalfHeight = 50f / DungeonBreakersGame.PPM;

                    float playerLeft = playerPos.x - playerHalfWidth;
                    float playerRight = playerPos.x + playerHalfWidth;
                    float playerTop = playerPos.y + playerHalfHeight;
                    float playerBottom = playerPos.y - playerHalfHeight;

                    boolean overlapX = attackRight > playerLeft && attackLeft < playerRight;
                    boolean overlapY = attackTop > playerBottom && attackBottom < playerTop;

                    if (overlapX && overlapY) {
                        AbstractPlayer player = (AbstractPlayer) b.getUserData();
                        if (player != null && player.canBeHit) {
                            player.takeDamage(15);
                            player.canBeHit = false;
                            com.badlogic.gdx.utils.Timer.schedule(new com.badlogic.gdx.utils.Timer.Task() {
                                @Override
                                public void run() {
                                    player.canBeHit = true;
                                }
                            }, 0.3f);
                            System.out.println("Immediate player hit detected!");
                        }
                    }
                }
            }
        }
    }

    private void destroyAttackHitbox() {
        if (attackHitbox != null && body != null) {
            Gdx.app.postRunnable(() -> {
                if (attackHitbox != null && body != null && !body.getWorld().isLocked()) {
                    try { body.destroyFixture(attackHitbox); } catch (Exception e){}
                    attackHitbox = null;
                } else if (attackHitbox != null) {
                    Timer.schedule(new Timer.Task() { @Override public void run() { destroyAttackHitbox(); } }, Gdx.graphics.getDeltaTime());
                }
            });
        }
    }
}
