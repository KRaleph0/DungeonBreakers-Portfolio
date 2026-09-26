package com.team.dungeonbreakers;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.*;

public class SpawnTrigger {
    public Body body;
    public boolean isTriggered = false;

    public SpawnTrigger(World world, Rectangle rect) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set((rect.x + rect.width / 2) / DungeonBreakersGame.PPM, (rect.y + rect.height / 2) / DungeonBreakersGame.PPM);

        body = world.createBody(bodyDef);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox((rect.width / 2) / DungeonBreakersGame.PPM, (rect.height / 2) / DungeonBreakersGame.PPM);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.isSensor = true; // 센서로 설정 (통과 가능)

        // ★★★ [수정] 충돌 필터 명시적 설정 ★★★
        fixtureDef.filter.categoryBits = DungeonBreakersGame.TRIGGER_BIT;
        fixtureDef.filter.maskBits = DungeonBreakersGame.PLAYER_BIT; // 플레이어만 감지

        body.createFixture(fixtureDef).setUserData(this); // UserData에 자기 자신(SpawnTrigger) 저장
        shape.dispose();
    }

    public void trigger() {
        if (!isTriggered) {
            isTriggered = true;
            System.out.println("Spawn Trigger Activated!");
        }
    }
}
