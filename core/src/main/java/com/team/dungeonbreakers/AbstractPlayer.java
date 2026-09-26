package com.team.dungeonbreakers;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import java.util.Iterator;

public abstract class AbstractPlayer {
    public enum BuffType { HEAL_OVER_TIME, BUFF_ATTACK, BUFF_DEFENSE, BUFF_CRIT_CHANCE }
    public static class Buff {
        public BuffType type; public float value; public float duration; public float maxDuration; public TextureRegion icon; public float tickTimer;
        public Buff(BuffType type, float value, float duration, TextureRegion icon) { this.type = type; this.value = value; this.duration = duration; this.maxDuration = duration; this.icon = icon; this.tickTimer = 0f; }
    }

    public float x, y;
    public final float width, height;
    public float scale = 5.5f;
    public boolean isFacingRight = true;
    public boolean isWalking = false;
    public boolean isGrounded = false;
    public boolean isAttacking = false;
    public boolean isHurt = false;
    public final CharacterType characterType;
    public boolean pendingJumpReset = false;
    public boolean canBeHit = true;
    public boolean isAlive = true;
    public boolean isDying = false;
    public boolean isDead = false;
    protected float deathTimer = 0f;

    protected float stateTime;
    protected float attackAnimationTimer;
    protected float hurtTimer = 0f;
    protected float attackTimer = 0;
    protected int jumpsLeft = 2;
    protected final int maxJumps = 2;
    public boolean isDashing = false;
    protected final float DASH_DURATION = 5 / 60f;
    public final float DASH_SPEED = 100f;
    protected float dashTimer = 0;
    protected int maxDashCharges = 2;
    protected int currentDashCharges = 2;
    protected float dashChargeRegenTime = 1.5f;
    protected float dashChargeRegenTimer = 0;
    protected final AnimationData animationData;
    public Body body;
    protected final TextureRegion shadowTexture;
    protected float shadowY, shadowAlpha;
    protected final float maxShadowDistMeters = 5.0f;

    protected int maxHp = 100;
    public float currentHp;
    protected int attackpoint = 10;
    protected float attackCooldown = 1.0f;
    protected int defense = 0;
    protected float criticalChance = 0.2f;
    protected float criticalDamage = 1.5f;

    protected int baseMaxHp = 100;
    protected int baseAttackPoint = 10;
    protected float baseAttackSpeed = 1.0f;
    protected int baseDefense = 0;
    protected float baseCriticalChance = 0.2f;
    protected float baseDashChargeRegenTime = 1.5f;
    protected int baseMaxDashCharges = 2;

    protected int bonusMaxHp = 0;
    protected int bonusAttackPoint = 0;
    protected int bonusDefense = 0;
    protected float bonusCriticalChance = 0f;
    protected float bonusAttackSpeed = 0f;
    protected float bonusCooldownReduction = 0f;
    protected float bonusDashCooldownReduction = 0f;
    protected int bonusDashMaxCharges = 0;

    public Array<Buff> activeBuffs = new Array<>();

    protected float skillCooldown = 5.0f;
    protected float skillTimer = 0f;
    protected TextureRegion skillIcon;
    protected TextureRegion skillIconCooldown;
    public float flashTimer = 0f;
    protected boolean isCastingSkill = false;
    protected float lastGroundY;
    public boolean isLastHitCritical = false;
    protected final float hitboxHeightRatio = 0.2f;

    public AbstractPlayer(AnimationData data, World world) {
        this.animationData = data;
        this.characterType = data.type;
        this.x = 100;
        this.y = 100;
        this.width = (data.idle != null && data.idle.getKeyFrames().length > 0) ? data.idle.getKeyFrames()[0].getRegionWidth() : 32;
        this.height = (data.idle != null && data.idle.getKeyFrames().length > 0) ? data.idle.getKeyFrames()[0].getRegionHeight() : 32;
        this.shadowTexture = data.shadow;

        recalculateStats(); // 초기 스탯 계산

        // ★ 버프 복구
        PlayerData pd = PlayerData.getInstance();
        if (pd.savedBuffs != null && pd.savedBuffs.size > 0) {
            this.activeBuffs.addAll(pd.savedBuffs);
            recalculateStats();
        }

        // ★ 여기서는 체력 초기화를 하지 않음 (자식 클래스에서 initHp 호출)

        this.lastGroundY = this.y;
        createBody(world);
    }

    // ★★★ [신규] 체력 초기화 및 동기화 메서드 (자식 클래스 생성자 마지막에 호출) ★★★
    public void initHp() {
        PlayerData pd = PlayerData.getInstance();
        if (pd.currentHp == -1) {
            // 게임 처음 시작
            this.currentHp = this.maxHp;
            pd.currentHp = this.maxHp;
        } else {
            // 맵 이동 후: 저장된 체력 불러오기
            // 현재 maxHp(장비/버프 포함)를 넘지 않도록 조정
            this.currentHp = Math.min(pd.currentHp, this.maxHp);
            if (this.currentHp <= 0) this.currentHp = 1; // 최소 1
        }
        System.out.println("Player HP Initialized: " + (int)currentHp + " / " + maxHp);
    }

    public void addBuff(BuffType type, float value, float duration, TextureRegion icon) { for (Buff b : activeBuffs) { if (b.type == type) { b.duration = duration; b.maxDuration = duration; b.value = value; b.icon = icon; recalculateStats(); return; } } activeBuffs.add(new Buff(type, value, duration, icon)); recalculateStats(); }
    private void updateBuffs(float delta) { Iterator<Buff> iter = activeBuffs.iterator(); boolean statChanged = false; while (iter.hasNext()) { Buff b = iter.next(); b.duration -= delta; if (b.type == BuffType.HEAL_OVER_TIME) { b.tickTimer += delta; if (b.tickTimer >= 0.5f) { heal(1); b.tickTimer = 0f; } } if (b.duration <= 0) { iter.remove(); statChanged = true; } } if (statChanged) { recalculateStats(); } }
    public void heal(float amount) { if (!isAlive) return; currentHp = Math.min(maxHp, currentHp + amount); PlayerData.getInstance().currentHp = (int)currentHp; }
    public void updateItemStats(Item item, boolean isEquipping) { if (item == null) return; int sign = isEquipping ? 1 : -1; this.bonusAttackPoint += item.attackPower * sign; this.bonusDefense += item.defense * sign; this.bonusMaxHp += item.maxHp * sign; this.bonusCriticalChance += item.criticalChance * sign; this.bonusAttackSpeed += item.attackSpeed * sign; this.bonusCooldownReduction += item.cooldown * sign; this.bonusDashCooldownReduction += item.dashCooldownReduction * sign; this.bonusDashMaxCharges += item.dashMaxCharges * sign; recalculateStats(); }
    public void recalculateStats() { float buffAtkMulti = 0f; int buffDef = 0; float buffCrit = 0f; for (Buff b : activeBuffs) { switch (b.type) { case BUFF_ATTACK: buffAtkMulti += b.value; break; case BUFF_DEFENSE: buffDef += (int)b.value; break; case BUFF_CRIT_CHANCE: buffCrit += b.value; break; } } this.maxHp = this.baseMaxHp + this.bonusMaxHp; this.attackpoint = (int)((this.baseAttackPoint + this.bonusAttackPoint) * (1.0f + buffAtkMulti)); this.defense = this.baseDefense + this.bonusDefense + buffDef; this.criticalChance = this.baseCriticalChance + this.bonusCriticalChance + buffCrit; this.maxDashCharges = this.baseMaxDashCharges + this.bonusDashMaxCharges; float totalCooldownMod = MathUtils.clamp(bonusAttackSpeed + bonusCooldownReduction, -0.5f, 0.8f); this.attackCooldown = 1.0f * (1.0f - totalCooldownMod); this.dashChargeRegenTime = this.baseDashChargeRegenTime * (1.0f - MathUtils.clamp(bonusDashCooldownReduction, -0.5f, 0.8f)); if (this.currentHp > this.maxHp) this.currentHp = this.maxHp; }
    public int calculateDamage() { if (MathUtils.random() < criticalChance) { isLastHitCritical = true; return (int) (attackpoint * criticalDamage); } isLastHitCritical = false; return attackpoint; }
    private void createBody(World world) { BodyDef bodyDef = new BodyDef(); bodyDef.type = BodyDef.BodyType.DynamicBody; bodyDef.position.set(x / DungeonBreakersGame.PPM, y / DungeonBreakersGame.PPM); bodyDef.bullet = true; body = world.createBody(bodyDef); body.setFixedRotation(true); body.setUserData(this); PolygonShape shape = new PolygonShape(); float hitboxWidthRatio = 0.1f; float w = (width * scale * hitboxWidthRatio) / 2 / DungeonBreakersGame.PPM; float h = (height * scale * hitboxHeightRatio) / 2 / DungeonBreakersGame.PPM; shape.setAsBox(w, h, new Vector2(0, 0), 0); FixtureDef fixtureDef = new FixtureDef(); fixtureDef.shape = shape; fixtureDef.density = 0.3f; fixtureDef.friction = 0.0f; fixtureDef.restitution = 0.0f; fixtureDef.filter.categoryBits = DungeonBreakersGame.PLAYER_BIT; fixtureDef.filter.maskBits = DungeonBreakersGame.GROUND_BIT | DungeonBreakersGame.ENEMY_PROJECTILE_BIT | DungeonBreakersGame.COIN_BIT | DungeonBreakersGame.TRIGGER_BIT | DungeonBreakersGame.PLATFORM_BIT | DungeonBreakersGame.NPC_BIT; body.createFixture(fixtureDef).setUserData("playerBody"); shape.dispose(); PolygonShape footSensorShape = new PolygonShape(); footSensorShape.setAsBox(w * 0.9f, h / 4, new Vector2(0, -h - (h/4)), 0); FixtureDef footDef = new FixtureDef(); footDef.shape = footSensorShape; footDef.isSensor = true; footDef.filter.categoryBits = DungeonBreakersGame.PLAYER_BIT; footDef.filter.maskBits = DungeonBreakersGame.GROUND_BIT | DungeonBreakersGame.PLATFORM_BIT; body.createFixture(footDef).setUserData("foot"); footSensorShape.dispose(); }
    public float getFeetY() { float h = (height * scale * hitboxHeightRatio) / 2 / DungeonBreakersGame.PPM; return body.getPosition().y - h; }
    public void update(float dt) { if (!isDead) { this.x = body.getPosition().x * DungeonBreakersGame.PPM; this.y = body.getPosition().y * DungeonBreakersGame.PPM; } stateTime += dt; updateBuffs(dt); if (isDying) { deathTimer += dt; if (animationData.death != null && animationData.death.isAnimationFinished(deathTimer)) { isDead = true; isDying = false; } return; } if (isGrounded) lastGroundY = this.y; updateShadow(); if (isHurt) { hurtTimer += dt; if (animationData.hurt != null && animationData.hurt.isAnimationFinished(hurtTimer)) { isHurt = false; hurtTimer = 0; } } if (attackTimer > 0) attackTimer -= dt; if (isAttacking) { attackAnimationTimer += dt; if (animationData.attack != null && animationData.attack.isAnimationFinished(attackAnimationTimer)) { isAttacking = false; } } if (!isGrounded) pendingJumpReset = true; else if (pendingJumpReset && !isAttacking) { resetJumpCount(); pendingJumpReset = false; } if (isDashing) { dashTimer -= dt; if (dashTimer <= 0) stopDash(); } if (currentDashCharges < maxDashCharges) { dashChargeRegenTimer += dt; if (dashChargeRegenTimer >= dashChargeRegenTime) { currentDashCharges++; dashChargeRegenTimer = 0; } } if (skillTimer > 0) { skillTimer -= dt; if (skillTimer <= 0) { skillTimer = 0; flashTimer = 0.2f; } } if (flashTimer > 0) flashTimer -= dt; }
    private void updateShadow() { World world = body.getWorld(); Vector2 rayStart = body.getPosition(); Vector2 rayEnd = new Vector2(rayStart.x, rayStart.y - maxShadowDistMeters); shadowAlpha = 0f; shadowY = -1000f; world.rayCast(new RayCastCallback() { @Override public float reportRayFixture(Fixture fixture, Vector2 point, Vector2 normal, float fraction) { short cat = fixture.getFilterData().categoryBits; if ((cat & DungeonBreakersGame.GROUND_BIT) != 0 || (cat & DungeonBreakersGame.PLATFORM_BIT) != 0) { shadowY = point.y * DungeonBreakersGame.PPM; float maxAlpha = 0.8f; shadowAlpha = maxAlpha * (1.0f - fraction); return fraction; } return -1; } }, rayStart, rayEnd); }
    public void jump() { if (jumpsLeft > 0) { body.setLinearVelocity(body.getLinearVelocity().x, 0); body.applyLinearImpulse(0, 25f, body.getWorldCenter().x, body.getWorldCenter().y, true); jumpsLeft--; isGrounded = false; pendingJumpReset = false; } }
    public void resetJumpCount() { jumpsLeft = maxJumps; }
    public boolean canAttack() { return !isAttacking && attackTimer <= 0 && !isDying && !isDead; }
    public void attack() { if (!canAttack()) return; isAttacking = true; attackAnimationTimer = 0; attackTimer = attackCooldown; }
    public void dash(Vector2 direction) { if (currentDashCharges <= 0 || isDashing || isDying) return; isDashing = true; dashTimer = DASH_DURATION; currentDashCharges--; direction.nor(); Vector2 finalVelocity = direction.scl(DASH_SPEED); float maxVerticalDashSpeed = 15f; finalVelocity.y = MathUtils.clamp(finalVelocity.y, -maxVerticalDashSpeed, maxVerticalDashSpeed); body.setLinearVelocity(finalVelocity); }
    protected void stopDash() { isDashing = false; float velX = 0; body.setLinearVelocity(velX, body.getLinearVelocity().y); }
    public void draw(SpriteBatch batch) { TextureRegion currentFrame; if (isDying || isDead) { if (animationData.death != null) { currentFrame = animationData.death.getKeyFrame(deathTimer); } else { currentFrame = animationData.idle.getKeyFrame(0); } } else if (isHurt && animationData.hurt != null) { currentFrame = animationData.hurt.getKeyFrame(hurtTimer); } else if (isAttacking && animationData.attack != null) { currentFrame = animationData.attack.getKeyFrame(attackAnimationTimer); } else if (isWalking) { currentFrame = animationData.walk.getKeyFrame(stateTime, true); } else { currentFrame = animationData.idle.getKeyFrame(stateTime, true); } float w = width * scale; float h = height * scale; float dx = x - w / 2; float dy = y - h / 2 - 10f; if ((!isFacingRight && !currentFrame.isFlipX()) || (isFacingRight && currentFrame.isFlipX())) { currentFrame.flip(true, false); } if (shadowTexture != null && shadowAlpha > 0.05f && !isDead) { batch.setColor(1, 1, 1, shadowAlpha); float sw = shadowTexture.getRegionWidth() * scale; float sh = shadowTexture.getRegionHeight() * scale; batch.draw(shadowTexture, x - sw/2, shadowY - sh/2 + 43f, sw, sh); } batch.setColor(1, 1, 1, 1); batch.draw(currentFrame, dx, dy, w, h); }
    public void takeDamage(int damage) { if (!isAlive || isDashing || !canBeHit || isDying) return; float mult = 100f / (100f + Math.max(0, defense)); int actualDamage = Math.max(1, (int)(damage * mult)); this.currentHp -= actualDamage; PlayerData.getInstance().currentHp = (int)this.currentHp; System.out.println("Hit: " + actualDamage + " HP: " + (int)currentHp); canBeHit = false; if (this.currentHp <= 0) { isAlive = false; this.currentHp = 0; PlayerData.getInstance().currentHp = 0; isDying = true; deathTimer = 0f; } }
    public void castSkill() { if (skillTimer > 0 || isAttacking || isHurt || isDying) return; attackTimer = attackCooldown; skillTimer = skillCooldown; performSkillAction(); }
    protected abstract void performSkillAction();
    public float getSkillTimer() { return skillTimer; }
    public float getSkillCooldown() { return skillCooldown; }
    public TextureRegion getSkillIcon() { return skillIcon; }
    public TextureRegion getSkillIconCooldown() { return skillIconCooldown; }
    public void drawDebug(ShapeRenderer renderer) {}
}
