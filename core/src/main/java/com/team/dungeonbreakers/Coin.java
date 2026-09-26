// core/src/main/java/com/team/dungeonbreakers/Coin.java
package com.team.dungeonbreakers;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.physics.box2d.*;

public class Coin {
    public enum CoinType { COPPER, SILVER, GOLD }

    public Body body;
    public float x, y;
    private Animation<TextureRegion> animation;
    private float stateTime;
    private boolean active;
    public int value;

    private final float width = 16f;
    private final float height = 16f;
    private final float scale = 2.0f;

    public Coin(World world, float x, float y, CoinType type, CharacterManager charManager) {
        this.x = x;
        this.y = y;
        this.active = true;
        // 애니메이션이 동시에 시작하지 않도록 랜덤 시작점 설정
        this.stateTime = MathUtils.random(0f, 1f);

        switch (type) {
            case COPPER:
                this.animation = charManager.coinCopperAnim;
                this.value = 1;
                break;
            case SILVER:
                this.animation = charManager.coinSilverAnim;
                this.value = 10;
                break;
            case GOLD:
                this.animation = charManager.coinGoldAnim;
                this.value = 100;
                break;
        }

        createBody(world);
    }

    private void createBody(World world) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(x / DungeonBreakersGame.PPM, y / DungeonBreakersGame.PPM);

        // ★★★ [수정] 감속 설정 변경 ★★★
        // linearDamping: 이동 속도 감소 (너무 미끄러지지 않게 적당히 유지)
        bodyDef.linearDamping = 0.5f;
        // angularDamping: 회전 속도 감소 (제거하여 계속 물리적으로 구르게 함)
        // bodyDef.angularDamping = 0.5f; // <--- 삭제 또는 0으로 설정

        body = world.createBody(bodyDef);

        CircleShape shape = new CircleShape();
        shape.setRadius(6 / DungeonBreakersGame.PPM);

        // 획득용 센서 픽스쳐
        FixtureDef sensorDef = new FixtureDef();
        sensorDef.shape = shape;
        sensorDef.isSensor = true;
        sensorDef.filter.categoryBits = DungeonBreakersGame.COIN_BIT;
        sensorDef.filter.maskBits = DungeonBreakersGame.PLAYER_BIT;
        body.createFixture(sensorDef).setUserData("coin");

        // 물리 충돌용 픽스쳐 (바닥 튕기기)
        FixtureDef physicsDef = new FixtureDef();
        physicsDef.shape = shape;
        physicsDef.isSensor = false;
        physicsDef.restitution = 0.6f; // 조금 더 잘 튀게 상향
        physicsDef.friction = 0.4f;    // 바닥 마찰력과 상호작용
        physicsDef.filter.categoryBits = DungeonBreakersGame.COIN_BIT;
        physicsDef.filter.maskBits = DungeonBreakersGame.GROUND_BIT;
        body.createFixture(physicsDef).setUserData("coin_physics");

        body.setUserData(this);
        shape.dispose();

        // ★★★ [수정] 넓게 퍼지며 튀어오르기 + 초기 회전력 ★★★
        // X축 힘의 범위를 넓혀서 (-5 ~ 5) 더 넓게 퍼지게 함
        float impulseX = MathUtils.random(-5.0f, 5.0f);
        // Y축 힘은 위로 솟구치게 (3 ~ 6)
        float impulseY = MathUtils.random(3.0f, 6.0f);
        body.applyLinearImpulse(impulseX, impulseY, body.getWorldCenter().x, body.getWorldCenter().y, true);

        // 초기 물리적 회전력(Torque) 적용 -> 생성되자마자 데굴데굴 구름
        float randomTorque = MathUtils.random(-2.0f, 2.0f);
        body.applyTorque(randomTorque, true);
    }

    public void update(float deltaTime) {
        stateTime += deltaTime;
        x = body.getPosition().x * DungeonBreakersGame.PPM;
        y = body.getPosition().y * DungeonBreakersGame.PPM;
    }

    public void draw(SpriteBatch batch) {
        if (!active || animation == null) return;
        // 두 번째 인자 true는 루핑(반복)을 의미하므로 계속 회전하는 애니메이션이 재생됨
        TextureRegion currentFrame = animation.getKeyFrame(stateTime, true);

        batch.draw(currentFrame,
            x - (width * scale) / 2, y - (height * scale) / 2,
            width * scale, height * scale);
    }

    public boolean isActive() { return active; }
    public void collect() { active = false; }
}
