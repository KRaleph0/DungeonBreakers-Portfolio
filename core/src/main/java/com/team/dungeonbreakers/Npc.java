package com.team.dungeonbreakers;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;

public class Npc {
    public float x, y;
    public Body body;
    public final float width, height;

    private Animation<TextureRegion> idleAnimation;
    private TextureRegion shadowRegion;

    private float stateTime = 0f;

    // ★★★ [수정] 크기 확대 (5.0f -> 7.5f) ★★★
    private float scale = 5.0f;
    private boolean facingRight = true;

    public final NpcType type;
    private GlyphLayout layout = new GlyphLayout();

    public Npc(World world, CharacterManager charManager, NpcType type, float x, float y) {
        this.x = x;
        this.y = y;
        this.type = type;

        TextureAtlas atlas = charManager.getAtlas(type.atlasKey);

        String idleRegionName = type.atlasKey + "-Idle";
        TextureRegion region = atlas.findRegion(idleRegionName);

        if (region != null) {
            int frameCount = 6;
            Array<TextureRegion> frames = new Array<>();
            int frameWidth = region.getRegionWidth() / frameCount;
            for (int i = 0; i < frameCount; i++) {
                frames.add(new TextureRegion(region, i * frameWidth, 0, frameWidth, region.getRegionHeight()));
            }
            idleAnimation = new Animation<>(0.2f, frames, Animation.PlayMode.LOOP);
            this.width = frameWidth;
            this.height = region.getRegionHeight();
        } else {
            this.width = 32;
            this.height = 32;
        }

        String shadowName = type.atlasKey + "-Shadow";
        this.shadowRegion = atlas.findRegion(shadowName);

        createBody(world);
    }

    private void createBody(World world) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set(x / DungeonBreakersGame.PPM, y / DungeonBreakersGame.PPM);

        body = world.createBody(bodyDef);

        PolygonShape shape = new PolygonShape();
        // 히트박스 크기 (이미지 크기에 비례, 약간 작게 설정)
        float hitboxWidth = (width * scale * 0.4f) / 2 / DungeonBreakersGame.PPM;
        float hitboxHeight = (height * scale * 0.4f) / 2 / DungeonBreakersGame.PPM;
        shape.setAsBox(hitboxWidth, hitboxHeight);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;

        // ★★★ [확인] 센서 설정 (true면 통과 가능, false면 충돌) ★★★
        fixtureDef.isSensor = true;

        fixtureDef.filter.categoryBits = DungeonBreakersGame.NPC_BIT;
        fixtureDef.filter.maskBits = DungeonBreakersGame.PLAYER_BIT;

        body.createFixture(fixtureDef).setUserData(this);
        shape.dispose();
    }

    public void update(float deltaTime) {
        stateTime += deltaTime;
    }

    public void draw(SpriteBatch batch, BitmapFont font) {
        float drawWidth = width * scale;
        float drawHeight = height * scale;

        float yOffset = (type == NpcType.KNIGHT_UNLOCK) ? 5f : 0f;
        float drawY = y - (drawHeight / 2) + yOffset;
        float drawX = x - (drawWidth / 2);

        // 그림자
        if (shadowRegion != null) {
            batch.setColor(1f, 1f, 1f, 0.5f);
            batch.draw(shadowRegion, drawX, drawY, drawWidth, drawHeight);
            batch.setColor(1f, 1f, 1f, 1f);
        }

        // 캐릭터
        if (idleAnimation != null) {
            TextureRegion currentFrame = idleAnimation.getKeyFrame(stateTime, true);

            if (!facingRight && !currentFrame.isFlipX()) currentFrame.flip(true, false);
            if (facingRight && currentFrame.isFlipX()) currentFrame.flip(true, false);

            batch.draw(currentFrame, drawX, drawY, drawWidth, drawHeight);

            if (font != null) {
                layout.setText(font, type.roleName);

                // NPC 이름 검은색
                Color oldColor = font.getColor();
                font.setColor(Color.BLACK);

                font.draw(batch, layout, x - layout.width / 2, drawY + drawHeight + 30);

                font.setColor(oldColor); // 원래 색상 복구
            }
        }
    }

    public void interact() {}
}
