package com.team.dungeonbreakers;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.*;

public class DroppedItem {
    public Body body;
    public Item item;
    public float x, y;
    private float stateTime;
    private boolean active;
    private CharacterManager charManager;
    private TextureRegion texture;

    public DroppedItem(World world, float x, float y, Item item, CharacterManager charManager) {
        this.x = x;
        this.y = y;
        this.item = item;
        this.charManager = charManager;
        this.active = true;
        this.stateTime = 0;

        // 아이템 이미지 가져오기
        if (charManager.getItemAtlas() != null) {
            this.texture = charManager.getItemAtlas().findRegion(item.textureRegionName);
        }

        createBody(world);
    }

    private void createBody(World world) {
        BodyDef bdef = new BodyDef();
        bdef.position.set(x / DungeonBreakersGame.PPM, y / DungeonBreakersGame.PPM);
        bdef.type = BodyDef.BodyType.DynamicBody;
        body = world.createBody(bdef);

        FixtureDef fdef = new FixtureDef();
        CircleShape shape = new CircleShape();
        shape.setRadius(10 / DungeonBreakersGame.PPM); // 반지름 10픽셀

        fdef.shape = shape;
        fdef.isSensor = true; // 통과 가능 (센서)
        fdef.filter.categoryBits = DungeonBreakersGame.COIN_BIT; // 코인 비트 재사용 (또는 별도 ITEM_BIT 추가)
        fdef.filter.maskBits = DungeonBreakersGame.PLAYER_BIT | DungeonBreakersGame.GROUND_BIT;

        body.createFixture(fdef).setUserData(this); // UserData에 자신(DroppedItem)을 넣음
        shape.dispose();
    }

    public void update(float dt) {
        stateTime += dt;
        // 둥둥 떠다니는 효과
        float floatOffset = (float) Math.sin(stateTime * 3) * 0.1f;
        // 위치 갱신
        if (body != null) {
            this.x = body.getPosition().x * DungeonBreakersGame.PPM;
            this.y = (body.getPosition().y + floatOffset) * DungeonBreakersGame.PPM;
        }
    }

    public void draw(SpriteBatch batch) {
        if (!active || texture == null) return;
        float size = 24f; // 아이템 크기
        batch.draw(texture, x - size / 2, y - size / 2, size, size);
    }

    public void collect() {
        active = false;
    }

    public boolean isActive() {
        return active;
    }
}
