package com.team.dungeonbreakers;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;

public abstract class AbstractEnemy {
    public float x, y;
    public Body body;
    public float width, height;
    protected float scale;
    protected int hp;
    protected int maxHp; // ★ 추가
    protected boolean alive = true;
    protected Vector2 renderOffset;

    protected Animation<TextureRegion> idleAnimation;
    protected Animation<TextureRegion> walkAnimation;
    protected Animation<TextureRegion> attackAnimation;
    protected Animation<TextureRegion> deathAnimation;
    protected Animation<TextureRegion> hurtAnimation;

    protected float stateTime = 0f;
    protected float attackAnimationTimer = 0f;
    protected float deathAnimationTimer = 0f;
    protected float hurtTimer = 0f;
    protected boolean isHurt = false;
    protected float hurtCooldown = 1.0f;
    protected float hurtCooldownTimer = 0f;
    protected boolean hasAggro = false;
    public boolean droppedLoot = false;
    protected boolean isWalking = false;
    protected boolean facingRight = true;
    protected boolean isAttacking = false;
    protected boolean isDying = false;
    protected boolean physicsBodySetToDead = false;

    // ★★★ [추가] 보스 관련 필드 ★★★
    protected boolean isBoss = false;
    protected Texture hpBarFrame;
    protected Texture hpBar;

    public AbstractEnemy(World world, float startX, float startY,
                         Animation<TextureRegion> idleAnim, Animation<TextureRegion> walkAnim, Animation<TextureRegion> attackAnim,
                         Animation<TextureRegion> hurtAnim, Animation<TextureRegion> deathAnim,
                         float scale, int hp,
                         float hitboxWidthRatio, float hitboxHeightRatio,
                         float renderOffsetX, float renderOffsetY) {
        this.x = startX;
        this.y = startY;
        this.idleAnimation = idleAnim;
        this.walkAnimation = walkAnim;
        this.attackAnimation = attackAnim;
        this.hurtAnimation = hurtAnim;
        this.deathAnimation = deathAnim;
        this.scale = scale;
        this.hp = hp;
        this.maxHp = hp; // 최대 체력 설정

        if (idleAnimation == null || idleAnimation.getKeyFrames().length == 0) {
            System.err.println("Warning: Animation not found for Enemy at " + startX + "," + startY);
            this.width = 32; this.height = 32;
        } else {
            this.width = idleAnimation.getKeyFrame(0).getRegionWidth();
            this.height = idleAnimation.getKeyFrame(0).getRegionHeight();
        }
        this.renderOffset = new Vector2(renderOffsetX, renderOffsetY);

        if (deathAnimation != null) deathAnimation.setPlayMode(Animation.PlayMode.NORMAL);
        if (hurtAnimation != null) hurtAnimation.setPlayMode(Animation.PlayMode.NORMAL);

        createBody(world, hitboxWidthRatio, hitboxHeightRatio);
    }

    // ★★★ [추가] 텍스처 설정 메서드 ★★★
    public void setHpBarTextures(Texture frame, Texture bar) {
        this.hpBarFrame = frame;
        this.hpBar = bar;
    }

    // ★★★ [추가] 보스 설정 (크기 2배) ★★★
    public void setBoss(boolean isBoss) {
        this.isBoss = isBoss;
        if (isBoss) {
            this.scale *= 2.0f; // 기존 스케일의 2배
        }
    }

    private void createBody(World world, float hitboxWidthRatio, float hitboxHeightRatio) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(x / DungeonBreakersGame.PPM, y / DungeonBreakersGame.PPM);
        body = world.createBody(bodyDef);
        body.setFixedRotation(true);
        body.setUserData(this);

        PolygonShape shape = new PolygonShape();
        float w = Math.max(10, width * scale * hitboxWidthRatio) / 2 / DungeonBreakersGame.PPM;
        float h = Math.max(10, height * scale * hitboxHeightRatio) / 2 / DungeonBreakersGame.PPM;
        shape.setAsBox(w, h);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 1.0f;
        fixtureDef.filter.categoryBits = DungeonBreakersGame.ENEMY_BIT;
        fixtureDef.filter.maskBits = DungeonBreakersGame.GROUND_BIT | DungeonBreakersGame.PROJECTILE_BIT | DungeonBreakersGame.PLATFORM_BIT;
        body.createFixture(fixtureDef).setUserData("enemy");
        shape.dispose();
    }

    public void update(AbstractPlayer player, Array<Projectile> projectiles, float deltaTime) {
        if (body != null) {
            this.x = body.getPosition().x * DungeonBreakersGame.PPM;
            this.y = body.getPosition().y * DungeonBreakersGame.PPM;
        }
        stateTime += deltaTime;
        if (hurtCooldownTimer > 0) hurtCooldownTimer -= deltaTime;
        if (isHurt) {
            hurtTimer += deltaTime;
            if (hurtAnimation != null && hurtAnimation.isAnimationFinished(hurtTimer)) {
                isHurt = false; hurtTimer = 0f;
            }
        }
        if (isDying) deathAnimationTimer += deltaTime;
        else if (isAttacking) attackAnimationTimer += deltaTime;
    }

    public void takeDamage(int damage) {
        if (!alive) return;
        this.hp -= damage;
        this.hasAggro = true;
        if (this.hp > 0) {
            if (hurtCooldownTimer <= 0) {
                isHurt = true; hurtTimer = 0f; hurtCooldownTimer = hurtCooldown; isAttacking = false;
            }
        } else {
            alive = false; isDying = true; deathAnimationTimer = 0f; isHurt = false;
            BattleStateManager.getInstance().onEnemyKilled();
        }
    }

    public void setPhysicsBodyToDead() {
        if (body != null && !physicsBodySetToDead) {
            physicsBodySetToDead = true;
            for (Fixture f : body.getFixtureList()) f.setSensor(true);
            body.setLinearVelocity(0, 0);
            body.setType(BodyDef.BodyType.StaticBody);
        }
    }

    public boolean isAlive() { return alive; }

    public void draw(SpriteBatch batch) {
        if (!alive && !isDying) return;

        if (isDying && deathAnimation != null && deathAnimation.isAnimationFinished(deathAnimationTimer)) {
            isDying = false;
            return;
        }

        TextureRegion currentFrame = null;
        if (isDying && deathAnimation != null) currentFrame = deathAnimation.getKeyFrame(deathAnimationTimer);
        else if (isHurt && hurtAnimation != null) currentFrame = hurtAnimation.getKeyFrame(hurtTimer);
        else if (isAttacking && attackAnimation != null && !attackAnimation.isAnimationFinished(attackAnimationTimer)) currentFrame = attackAnimation.getKeyFrame(attackAnimationTimer);
        else if (isWalking && walkAnimation != null) currentFrame = walkAnimation.getKeyFrame(stateTime, true);
        else if (idleAnimation != null) {
            currentFrame = idleAnimation.getKeyFrame(stateTime, true);
            if (isAttacking && attackAnimation != null && attackAnimation.isAnimationFinished(attackAnimationTimer)) isAttacking = false;
        }

        if (currentFrame == null) return;

        float drawX = x - (width * scale / 2) + renderOffset.x;
        float drawY = y - (height * scale / 2) + renderOffset.y;
        float drawW = width * scale;
        float drawH = height * scale;

        if ((!facingRight && !currentFrame.isFlipX()) || (facingRight && currentFrame.isFlipX())) {
            currentFrame.flip(true, false);
        }
        batch.draw(currentFrame, drawX, drawY, drawW, drawH);

        // ★★★ [추가] 보스 HP 바 그리기 ★★★
        if (isBoss && alive) {
            drawHpBar(batch, drawX, drawY, drawW, drawH);
        }
    }

    private void drawHpBar(SpriteBatch batch, float entityX, float entityY, float entityW, float entityH) {
        if (hpBarFrame == null || hpBar == null) return;

        float barWidth = 150f * (scale / 4.0f);
        float barHeight = 25f * (scale / 4.0f);
        float barX = entityX + (entityW - barWidth) / 2;
        float barY = entityY + entityH + 20f;

        batch.draw(hpBarFrame, barX, barY, barWidth, barHeight);

        float hpPercent = (float) hp / maxHp;
        float currentBarWidth = barWidth * hpPercent;

        // 프레임 내부 마진 (약간의 오차 보정)
        float margin = 2f;
        if (hpPercent > 0) {
            batch.draw(hpBar, barX + margin, barY + margin, Math.max(0, currentBarWidth - margin * 2), barHeight - margin * 2);
        }
    }

    public boolean isReadyForRemoval() { return !alive && !isDying; }
    public void drawDebug(ShapeRenderer renderer) {}
}
