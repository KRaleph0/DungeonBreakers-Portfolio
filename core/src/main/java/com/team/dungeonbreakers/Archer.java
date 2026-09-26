package com.team.dungeonbreakers;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;

public class Archer extends AbstractPlayer {
    private final Array<Projectile> projectiles;
    private final OrthographicCamera camera;
    private final CharacterManager charManager;

    public Archer(AnimationData data, World world, Array<Projectile> projectiles, OrthographicCamera camera, CharacterManager charManager) {
        super(data, world);
        this.projectiles = projectiles;
        this.camera = camera;
        this.charManager = charManager;

        // 아처 스탯 설정
        this.baseMaxHp = 80;
        this.baseAttackPoint = 15;
        this.baseAttackSpeed = 1.0f;
        this.baseDefense = 2;
        this.baseCriticalChance = 0.3f;

        recalculateStats();

        // ★★★ [수정] 모든 스탯 설정 후 체력 초기화 호출 ★★★
        initHp();

        this.skillCooldown = 6.0f;
        TextureAtlas skillAtlas = charManager.getSkillAtlas();
        if (skillAtlas != null) {
            this.skillIcon = skillAtlas.findRegion("Archer_skillicon");
            this.skillIconCooldown = skillAtlas.findRegion("Archer_skilliocn_cooldown");
        }
    }

    @Override
    public void attack() {
        if (!canAttack()) return;
        if (animationData.attack01 != null) animationData.attack = animationData.attack01;
        super.attack();
        shootArrow(false);
    }

    @Override
    protected void performSkillAction() {
        isAttacking = true;
        attackAnimationTimer = 0;
        if (animationData.attack02 != null) animationData.attack = animationData.attack02;
        shootArrow(true);
    }

    private void shootArrow(boolean isSkill) {
        if (charManager.arrowRegion == null) return;
        Vector3 mousePos = new Vector3(com.badlogic.gdx.Gdx.input.getX(), com.badlogic.gdx.Gdx.input.getY(), 0);
        camera.unproject(mousePos);
        float startX = this.x; float startY = this.y + 20;
        Vector2 direction = new Vector2(mousePos.x - startX, mousePos.y - startY).nor();
        float speed = 20f;
        Vector2 velocity = new Vector2(direction.x * speed, direction.y * speed);
        int dmg = calculateDamage(); int pen = 1;
        TextureRegion region = charManager.arrowRegion;
        if (isSkill) { dmg = (int)(dmg * 1.5f); pen = 3; if (charManager.getSkillAtlas() != null) { TextureRegion skillArrow = charManager.getSkillAtlas().findRegion("Archer_skillarrow"); if (skillArrow != null) region = skillArrow; } }
        Projectile arrow = new Projectile(body.getWorld(), region, startX, startY, velocity, false, dmg, pen);
        projectiles.add(arrow);
    }
}
