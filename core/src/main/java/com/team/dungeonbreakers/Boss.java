package com.team.dungeonbreakers;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;

public class Boss extends AbstractEnemy {
    private final Array<Projectile> projectiles;
    private final TextureRegion shockwaveTexture;

    private float attackTimer = 0f;
    private float attackCooldown = 2.5f;
    private boolean isPhaseTwo = false;
    private boolean shockwaveSpawned = false; // 충격파 중복 생성 방지

    // 애니메이션 필드
    private Animation<TextureRegion> animSlash; // Attack01
    private Animation<TextureRegion> animStab;  // Attack02
    private Animation<TextureRegion> animSmash; // Attack03

    public Boss(World world, TextureAtlas atlas, float startX, float startY, Array<Projectile> projectiles, TextureRegion shockwaveTexture) {
        super(world, startX, startY,
            createAnimation(atlas, "Greatsword Skeleton-Idle", 6, 0.15f),
            createAnimation(atlas, "Greatsword Skeleton-Walk", 8, 0.15f),
            null, // 기본 attack은 null
            createAnimation(atlas, "Greatsword Skeleton-Hurt", 4, 0.1f),
            createAnimation(atlas, "Greatsword Skeleton-Death", 4, 0.15f),
            6.0f, 500, 0.15f, 0.3f, 0f, -20f);

        this.projectiles = projectiles;
        this.shockwaveTexture = shockwaveTexture;

        // 공격 애니메이션 초기화
        this.animSlash = createAnimation(atlas, "Greatsword Skeleton-Attack01", 9, 0.1f);
        this.animStab  = createAnimation(atlas, "Greatsword Skeleton-Attack02", 12, 0.08f);
        this.animSmash = createAnimation(atlas, "Greatsword Skeleton-Attack03", 8, 0.12f);
    }

    private static Animation<TextureRegion> createAnimation(TextureAtlas atlas, String regionName, int frameCount, float frameDuration) {
        if (atlas == null) return null;
        TextureRegion region = atlas.findRegion(regionName);
        if (region == null) return null;
        TextureRegion[][] tmp = region.split(region.getRegionWidth() / frameCount, region.getRegionHeight());
        return new Animation<>(frameDuration, new Array<>(tmp[0]), Animation.PlayMode.NORMAL);
    }

    @Override
    public void update(AbstractPlayer player, Array<Projectile> projectilesList, float deltaTime) {
        super.update(player, projectilesList, deltaTime);
        if (!alive) return;

        // 2페이즈 체크
        if (!isPhaseTwo && hp < 250) {
            isPhaseTwo = true;
            attackCooldown = 1.5f;
        }

        if (attackTimer > 0) attackTimer -= deltaTime;

        float dist = player.x - this.x;

        // 공격 중이 아닐 때만 방향 전환
        if (!isAttacking) {
            facingRight = (dist > 0);
        }

        if (isAttacking) {
            body.setLinearVelocity(0, body.getLinearVelocity().y);

            // ★★★ 내려찍기(Attack03) 충격파 생성 로직 ★★★
            if (attackAnimation == animSmash) {
                // 애니메이션이 절반 정도 진행됐을 때 (내려찍는 순간)
                if (!shockwaveSpawned && attackAnimationTimer >= attackAnimation.getAnimationDuration() * 0.5f) {
                    spawnShockwaves();
                    shockwaveSpawned = true;
                }
            }

            if (attackAnimation != null && attackAnimation.isAnimationFinished(attackAnimationTimer)) {
                isAttacking = false;
                isWalking = true;
                attackTimer = attackCooldown;
            }
        } else if (!isHurt && attackTimer <= 0) {
            if (Math.abs(dist) < 200f) { // 근거리
                isWalking = false;
                startAttack(MathUtils.randomBoolean() ? animSlash : animStab); // 베기 or 찌르기
            } else if (Math.abs(dist) < 600f) { // 중거리
                isWalking = false;
                startAttack(animSmash); // 내려찍기
            } else { // 원거리 추격
                isWalking = true;
                float speed = isPhaseTwo ? 6f : 4f;
                body.setLinearVelocity(facingRight ? speed : -speed, body.getLinearVelocity().y);
            }
        }
    }

    private void startAttack(Animation<TextureRegion> anim) {
        isAttacking = true;
        attackAnimationTimer = 0;
        attackAnimation = anim;
        shockwaveSpawned = false; // 충격파 플래그 초기화

        // 공격 시작 시 바로 방향 고정
        body.setLinearVelocity(0, body.getLinearVelocity().y);
    }

    private void spawnShockwaves() {
        if (shockwaveTexture == null) return;

        float startY = this.y - (height * scale / 2) + 10f; // 발 밑
        float speed = 15f;
        int damage = 30;

        // 왼쪽 충격파
        Projectile leftWave = new Projectile(body.getWorld(), shockwaveTexture, x - 50, startY, new Vector2(-speed, 0), true, damage, 999); // 관통력 높게
        // 오른쪽 충격파
        Projectile rightWave = new Projectile(body.getWorld(), shockwaveTexture, x + 50, startY, new Vector2(speed, 0), true, damage, 999);

        projectiles.add(leftWave);
        projectiles.add(rightWave);
        System.out.println("Boss Smash! Shockwaves spawned.");
    }
}
