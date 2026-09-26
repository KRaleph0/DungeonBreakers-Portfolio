package com.team.dungeonbreakers;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

public class AnimationData {
    public final CharacterType type;
    public final Animation<TextureRegion> idle;
    public final Animation<TextureRegion> walk;
    public final Animation<TextureRegion> hurt;
    // ★★★ [추가] 사망 애니메이션 ★★★
    public final Animation<TextureRegion> death;

    public Animation<TextureRegion> attack;
    public Animation<TextureRegion> attack01;
    public Animation<TextureRegion> attack02;
    public Animation<TextureRegion> attack03;

    public final TextureRegion shadow;

    public AnimationData(TextureAtlas atlas, String prefix, CharacterType type) {
        this.type = type;

        this.idle = createAnimationFromStrip(atlas, prefix + "-Idle", 6, 0.2f);
        this.walk = createAnimationFromStrip(atlas, prefix + "-Walk", 8, 0.1f);
        this.hurt = createAnimationFromStrip(atlas, prefix + "-Hurt", 4, 0.1f);
        if (this.hurt != null) this.hurt.setPlayMode(Animation.PlayMode.NORMAL);

        // ★★★ [추가] Death 애니메이션 로드 (4프레임 가정) ★★★
        this.death = createAnimationFromStrip(atlas, prefix + "-Death", 4, 0.15f);
        if (this.death != null) this.death.setPlayMode(Animation.PlayMode.NORMAL);

        this.shadow = atlas.findRegion(prefix + "-Shadow");

        if (type == CharacterType.ARCHER) {
            initArcherAnimations(atlas, prefix);
        } else if (type == CharacterType.KNIGHT) {
            initKnightAnimations(atlas, prefix);
        } else {
            this.attack01 = createAnimationFromStrip(atlas, prefix + "-Attack01", 6, 0.1f);
            this.attack02 = null;
            this.attack03 = null;
            this.attack = this.attack01;
        }
    }

    private void initArcherAnimations(TextureAtlas atlas, String prefix) {
        TextureRegion attackStrip = atlas.findRegion(prefix + "-Attack01");
        if (attackStrip != null) {
            int totalFrames = 9;
            int frameWidth = attackStrip.getRegionWidth() / totalFrames;
            int frameHeight = attackStrip.getRegionHeight();
            TextureRegion[][] tmp = attackStrip.split(frameWidth, frameHeight);
            TextureRegion[] frames = new TextureRegion[] { tmp[0][5], tmp[0][6], tmp[0][7], tmp[0][8] };
            this.attack01 = new Animation<>(0.1f, new Array<>(frames));
            this.attack01.setPlayMode(Animation.PlayMode.NORMAL);
        } else {
            this.attack01 = null;
        }
        this.attack02 = createAnimationFromStrip(atlas, prefix + "-Attack02", 12, 0.08f);
        if (this.attack02 == null && attackStrip != null) {
            this.attack02 = createAnimationFromStrip(atlas, prefix + "-Attack01", 9, 0.08f);
        }
        if (this.attack02 != null) this.attack02.setPlayMode(Animation.PlayMode.NORMAL);

        this.attack03 = null;
        this.attack = this.attack01;
    }

    private void initKnightAnimations(TextureAtlas atlas, String prefix) {
        this.attack01 = createAnimationFromStrip(atlas, prefix + "-Attack01", 7, 0.08f);
        if (this.attack01 != null) this.attack01.setPlayMode(Animation.PlayMode.NORMAL);

        TextureRegion attack02Strip = atlas.findRegion(prefix + "-Attack02");
        if (attack02Strip != null) {
            int totalFrames = 10;
            int frameWidth = attack02Strip.getRegionWidth() / totalFrames;
            TextureRegion[][] tmp = attack02Strip.split(frameWidth, attack02Strip.getRegionHeight());
            Array<TextureRegion> frames = new Array<>();
            for(int i = 0; i < 5; i++) frames.add(tmp[0][i]);
            this.attack02 = new Animation<>(0.08f, frames);
            this.attack02.setPlayMode(Animation.PlayMode.NORMAL);
        } else {
            this.attack02 = this.attack01;
        }

        this.attack03 = createAnimationFromStrip(atlas, prefix + "-Attack03", 11, 0.08f);
        if (this.attack03 != null) this.attack03.setPlayMode(Animation.PlayMode.NORMAL);

        this.attack = this.attack01;
    }

    private Animation<TextureRegion> createAnimationFromStrip(TextureAtlas atlas, String regionName, int frameCount, float frameDuration) {
        TextureRegion region = atlas.findRegion(regionName);
        if (region == null) return null;
        int frameWidth = region.getRegionWidth() / frameCount;
        int frameHeight = region.getRegionHeight();
        TextureRegion[][] tmp = region.split(frameWidth, frameHeight);
        Animation<TextureRegion> animation = new Animation<>(frameDuration, new Array<>(tmp[0]));
        animation.setPlayMode(Animation.PlayMode.LOOP);
        return animation;
    }
}
