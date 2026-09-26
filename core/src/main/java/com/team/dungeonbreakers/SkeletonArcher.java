package com.team.dungeonbreakers;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Timer;

public class SkeletonArcher extends AbstractEnemy {
    private final TextureRegion arrowTexture;
    private final Array<Projectile> projectiles; // 투사체 관리 리스트
    private float attackTimer = 0f;
    private final float attackCooldown = 3.0f;

    public SkeletonArcher(World world, TextureAtlas atlas, float startX, float startY, TextureRegion arrowTexture, Array<Projectile> projectiles) {
        super(world, startX, startY,
            // ★★★ [수정] 아틀라스 파일명에 맞춰 띄어쓰기 및 이름 수정 ★★★
            createAnimation(atlas, "Skeleton Archer-Idle", 6, 0.2f),
            createAnimation(atlas, "Skeleton Archer-Walk", 8, 0.15f),
            createAnimation(atlas, "Skeleton Archer-Attack", 9, 0.1f), // Attack01 -> Attack, 8 -> 9프레임
            createAnimation(atlas, "Skeleton Archer-Hurt", 4, 0.15f),
            createAnimation(atlas, "Skeleton Archer-Death", 4, 0.15f),
            4.0f, 60, 0.1f, 0.2f, 0f, -10f);

        this.arrowTexture = arrowTexture;
        this.projectiles = projectiles;
    }

    private static Animation<TextureRegion> createAnimation(TextureAtlas atlas, String regionName, int frameCount, float frameDuration) {
        if (atlas == null) return null;
        TextureRegion region = atlas.findRegion(regionName);
        if (region == null) return null;

        // 프레임 너비 자동 계산
        int frameWidth = region.getRegionWidth() / frameCount;
        TextureRegion[][] tmp = region.split(frameWidth, region.getRegionHeight());
        return new Animation<>(frameDuration, new Array<>(tmp[0]), Animation.PlayMode.LOOP);
    }

    @Override
    public void update(AbstractPlayer player, Array<Projectile> projectilesList, float deltaTime) {
        super.update(player, projectilesList, deltaTime);
        if (!alive || isHurt) return;

        if (attackTimer > 0) attackTimer -= deltaTime;

        float dist = player.x - this.x;
        if (Math.abs(dist) < 400f) { // 인식 범위
            hasAggro = true;
        }

        if (hasAggro) {
            facingRight = (dist > 0);

            // 공격 사거리 및 Y축 범위 체크
            if (Math.abs(dist) < 350f && Math.abs(player.y - this.y) < 100f) {
                // 공격
                body.setLinearVelocity(0, body.getLinearVelocity().y);
                isWalking = false;

                if (attackTimer <= 0 && !isAttacking) {
                    isAttacking = true;
                    attackAnimationTimer = 0f;
                    attackTimer = attackCooldown;

                    // 애니메이션 중간(약 0.5초)에 화살 발사
                    Timer.schedule(new Timer.Task() {
                        @Override
                        public void run() {
                            if (alive && !isHurt) shoot(player);
                        }
                    }, 0.5f);
                }
            } else {
                // 추격
                float speed = 4f;
                body.setLinearVelocity(facingRight ? speed : -speed, body.getLinearVelocity().y);
                isWalking = true;
            }
        }
    }

    private void shoot(AbstractPlayer player) {
        if (arrowTexture == null) return;

        float startX = this.x + (facingRight ? 20 : -20);
        float startY = this.y + 10;

        // 플레이어 쪽으로 방향 계산
        Vector2 direction = new Vector2(player.x - startX, (player.y + 20) - startY).nor();
        Vector2 velocity = direction.scl(15f); // 화살 속도

        // ★★★ [중요] projectiles 리스트에 추가해야 화면에 나옴 ★★★
        Projectile arrow = new Projectile(body.getWorld(), arrowTexture, startX, startY, velocity, true, 10);
        projectiles.add(arrow);
    }
}
