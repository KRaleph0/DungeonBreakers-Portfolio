package com.team.dungeonbreakers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;

public class Knight extends AbstractPlayer {
    private int comboStep = 0;
    private boolean comboQueued = false;
    private boolean damageDealt = false;
    private DamageTextManager damageTextManager;
    private CharacterManager charManager;
    private Array<Projectile> projectiles;
    private boolean skillProjectileSpawned = false;

    public Knight(AnimationData data, World world, DamageTextManager damageTextManager, CharacterManager charManager, Array<Projectile> projectiles) {
        super(data, world);
        this.damageTextManager = damageTextManager;
        this.charManager = charManager;
        this.projectiles = projectiles;

        this.baseMaxHp = 150;
        this.baseAttackPoint = 25;
        this.baseAttackSpeed = 1.0f;
        this.baseDefense = 5;
        this.baseCriticalChance = 0.1f;

        recalculateStats();

        // ★★★ [수정] 체력 초기화 호출 ★★★
        initHp();

        this.skillCooldown = 8.0f;
        TextureAtlas skillAtlas = charManager.getSkillAtlas();
        if (skillAtlas != null) {
            this.skillIcon = skillAtlas.findRegion("knight_skillicon");
            this.skillIconCooldown = skillAtlas.findRegion("knight_skillicon_cooldown");
        }
    }

    @Override
    public void update(float deltaTime) {
        if (isHurt) {
            super.update(deltaTime);
            return;
        }

        if (isAttacking && animationData.attack == animationData.attack03) {
            if (!skillProjectileSpawned && attackAnimationTimer > 0.3f) {
                spawnSwordAura();
                skillProjectileSpawned = true;
            }
        }

        if (isAttacking && animationData.attack != animationData.attack03) {
            if (attackAnimationTimer > 0.1f) {
                if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
                    if (comboStep == 1) {
                        comboQueued = true;
                    }
                }
            }
            checkMeleeAttack();
        }

        super.update(deltaTime);

        if (!isAttacking) {
            if (comboStep > 0) {
                boolean shouldCombo = comboStep == 1 && (comboQueued || Gdx.input.isButtonPressed(Input.Buttons.LEFT));
                if (shouldCombo) {
                    startAttack2();
                } else {
                    resetAttackState();
                }
            }
            else if (animationData.attack == animationData.attack03) {
                resetAttackState();
            }
        }
    }

    @Override
    public void attack() {
        if (comboStep == 0 && animationData.attack != animationData.attack03) {
            startAttack1();
        }
    }

    @Override
    protected void performSkillAction() {
        isAttacking = true;
        attackAnimationTimer = 0;
        comboStep = 0;

        if (animationData.attack03 != null) {
            animationData.attack = animationData.attack03;
        } else {
            animationData.attack = animationData.attack01;
        }

        skillProjectileSpawned = false;
    }

    private void spawnSwordAura() {
        TextureAtlas skillAtlas = charManager.getSkillAtlas();
        if (skillAtlas == null) return;

        TextureRegion auraRegion = skillAtlas.findRegion("knight_Sword_aura");
        if (auraRegion == null) return;

        float direction = isFacingRight ? 1 : -1;
        Vector2 velocity = new Vector2(15f * direction, 0);

        int skillDamage = (int)(attackpoint * 1.25f);
        int penetration = 100;

        Projectile aura = new Projectile(body.getWorld(), auraRegion, this.x + (40 * direction), this.y, velocity, false, skillDamage, penetration);
        projectiles.add(aura);
    }

    private void startAttack1() {
        isAttacking = true;
        comboStep = 1;
        comboQueued = false;
        damageDealt = false;
        animationData.attack = animationData.attack02;
        attackAnimationTimer = 0;
    }

    private void startAttack2() {
        isAttacking = true;
        comboStep = 2;
        comboQueued = false;
        damageDealt = false;
        animationData.attack = animationData.attack01;
        attackAnimationTimer = 0;
    }

    private void resetAttackState() {
        isAttacking = false;
        comboStep = 0;
        comboQueued = false;
        damageDealt = false;
        animationData.attack = animationData.attack02;
    }

    @Override
    public void drawDebug(ShapeRenderer renderer) {
        if (isAttacking && animationData.attack != animationData.attack03) {
            float range = 150f;
            float h = 80f;
            float startX = x;
            float startY = y - (h / 2);
            if (!isFacingRight) {
                startX -= range;
            }
            renderer.setColor(Color.RED);
            renderer.rect(startX, startY, range, h);
        }
    }

    private void checkMeleeAttack() {
        if (damageDealt) return;
        float progress = attackAnimationTimer / animationData.attack.getAnimationDuration();
        if (progress < 0.3f || progress > 0.8f) return;
        float ppm = DungeonBreakersGame.PPM;
        float rangeMeters = 150f / ppm;
        float heightMeters = 80f / ppm;
        float startX = body.getPosition().x;
        float startY = body.getPosition().y - (heightMeters / 2);
        float endX = startX + (isFacingRight ? rangeMeters : -rangeMeters);
        float endY = startY + heightMeters;
        float lowerX = Math.min(startX, endX);
        float lowerY = Math.min(startY, endY);
        float upperX = Math.max(startX, endX);
        float upperY = Math.max(startY, endY);
        body.getWorld().QueryAABB(new QueryCallback() {
            @Override
            public boolean reportFixture(Fixture fixture) {
                if (fixture.getUserData() == null || !fixture.getUserData().equals("enemy")) {
                    return true;
                }
                Object userData = fixture.getBody().getUserData();
                if (userData instanceof AbstractEnemy) {
                    AbstractEnemy enemy = (AbstractEnemy) userData;
                    if (enemy.isAlive()) {
                        enemy.takeDamage(calculateDamage());
                        float knockback = isFacingRight ? 3f : -3f;
                        enemy.body.applyLinearImpulse(new Vector2(knockback, 0), enemy.body.getWorldCenter(), true);
                        if (damageTextManager != null) {
                            int dmg = isLastHitCritical ? (int)(attackpoint * criticalDamage) : attackpoint;
                            damageTextManager.createDamageText(dmg, enemy.x, enemy.y + enemy.height);
                        }
                    }
                }
                return true;
            }
        }, lowerX, lowerY, upperX, upperY);
        damageDealt = true;
    }
}
