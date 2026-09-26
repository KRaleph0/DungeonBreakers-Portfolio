package com.team.dungeonbreakers;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ObjectMap;

public class CharacterManager implements Disposable {
    private final AssetManager assetManager;
    private final ObjectMap<String, TextureAtlas> atlasMap;
    private final ObjectMap<String, AnimationData> animationDataMap;

    public Animation<TextureRegion> coinCopperAnim;
    public Animation<TextureRegion> coinSilverAnim;
    public Animation<TextureRegion> coinGoldAnim;

    public TextureRegion arrowRegion;
    public TextureRegion enemyArrowRegion;

    // 파일 경로 상수
    private final String KEY_ATLAS_PATH = "img/UI/key.atlas";
    private final String BOX_ATLAS_PATH = "img/map/box.atlas";
    private final String DOOR_ATLAS_PATH = "img/map/door.atlas";
    private final String MAP_ICON_ATLAS_PATH = "img/map/map_icon.atlas";

    private final String MAP_BG_LEFT = "img/map/map_left.png";
    private final String MAP_BG_CENTER = "img/map/map_center.png";
    private final String MAP_BG_RIGHT = "img/map/map_right.png";
    private final String DOOR_PNG_PATH = "img/map/door.png";
    private final String UI_BACK_PATH = "img/UI/back.png";
    private final String HEART_PNG_PATH = "img/ect/heart.png";
    private final String GOLD_ICON_PATH = "img/ect/gold.png";
    private final String STAR_ICON_PATH = "img/ect/star.png";
    private final String SOLDOUT_PNG_PATH = "img/ect/soldout.png";
    private final String LUCKY_ICON_PATH = "img/ect/lucky.png";
    private final String BOOK_ICON_PATH = "img/ect/book.png";

    // ★★★ [보스 관련 경로] ★★★
    private final String BOSS_ATLAS_PATH = "img/Character/Greatsword Skeleton.atlas";
    private final String BOSS_SKILL_PNG_PATH = "img/Character/boss1_skill.png";
    private final String HP_BAR_FRAME_PATH = "img/Character/HP_bar_frame.png";
    private final String HP_BAR_PATH = "img/Character/HP_bar.png";

    private TextureRegion doorOpenRegion;
    private TextureRegion doorCloseRegion;

    public CharacterManager() {
        this.assetManager = new AssetManager();
        this.atlasMap = new ObjectMap<>();
        this.animationDataMap = new ObjectMap<>();
    }

    public void loadAssets() {
        // 캐릭터
        loadAtlasSafe("img/Character/Archer.atlas");
        loadAtlasSafe("img/Character/Armored_Skeleton.atlas");
        loadAtlasSafe("img/Character/skeleton_Archer.atlas");
        loadAtlasSafe("img/Character/knight.atlas");
        loadAtlasSafe("img/Character/slime.atlas");
        loadAtlasSafe("img/Character/Knight Templar.atlas");
        loadAtlasSafe("img/Character/Priest.atlas");
        loadAtlasSafe("img/Character/Swordsman.atlas");
        loadAtlasSafe(BOSS_ATLAS_PATH);

        // UI
        loadAtlasSafe("img/UI/dropcoin.atlas");
        loadAtlasSafe("img/UI/slot.atlas");
        loadAtlasSafe("img/ect/item_img.atlas");
        loadAtlasSafe("img/ect/skill.atlas");

        loadTextureSafe("img/ect/arrow.png");
        loadTextureSafe("img/ect/Enemyarrow.png");
        loadTextureSafe("img/ect/Inventory_Slot_1.png");

        loadAtlasSafe(KEY_ATLAS_PATH);
        loadAtlasSafe(BOX_ATLAS_PATH);
        loadAtlasSafe(DOOR_ATLAS_PATH);
        loadAtlasSafe(MAP_ICON_ATLAS_PATH);

        loadTextureSafe(MAP_BG_LEFT);
        loadTextureSafe(MAP_BG_CENTER);
        loadTextureSafe(MAP_BG_RIGHT);
        loadTextureSafe(DOOR_PNG_PATH);
        loadTextureSafe(UI_BACK_PATH);
        loadTextureSafe(HEART_PNG_PATH);
        loadTextureSafe(GOLD_ICON_PATH);
        loadTextureSafe(STAR_ICON_PATH);
        loadTextureSafe(SOLDOUT_PNG_PATH);
        loadTextureSafe(LUCKY_ICON_PATH);
        loadTextureSafe(BOOK_ICON_PATH);

        // ★★★ [보스 리소스 로드] ★★★
        loadTextureSafe(BOSS_SKILL_PNG_PATH);
        loadTextureSafe(HP_BAR_FRAME_PATH);
        loadTextureSafe(HP_BAR_PATH);

        assetManager.finishLoading();

        // 맵핑
        atlasMap.put("Archer", getAtlasFromAsset("img/Character/Archer.atlas"));
        atlasMap.put("Armored_Skeleton", getAtlasFromAsset("img/Character/Armored_Skeleton.atlas"));
        atlasMap.put("Skeleton_Archer", getAtlasFromAsset("img/Character/skeleton_Archer.atlas"));
        atlasMap.put("Knight", getAtlasFromAsset("img/Character/knight.atlas"));
        atlasMap.put("Slime", getAtlasFromAsset("img/Character/slime.atlas"));
        atlasMap.put("Knight Templar", getAtlasFromAsset("img/Character/Knight Templar.atlas"));
        atlasMap.put("Priest", getAtlasFromAsset("img/Character/Priest.atlas"));
        atlasMap.put("Swordsman", getAtlasFromAsset("img/Character/Swordsman.atlas"));
        atlasMap.put("Greatsword Skeleton", getAtlasFromAsset(BOSS_ATLAS_PATH));

        TextureAtlas coinAtlas = getAtlasFromAsset("img/UI/dropcoin.atlas");
        if (coinAtlas != null) {
            coinCopperAnim = createCoinAnimation(coinAtlas, "dropcoin_copper", 16, 16);
            coinSilverAnim = createCoinAnimation(coinAtlas, "dropcoin_silver", 16, 16);
            coinGoldAnim = createCoinAnimation(coinAtlas, "dropcoin_gold", 16, 16);
        }

        // 필터
        setTextureFilter(getAtlasFromAsset("img/UI/slot.atlas"));
        setTextureFilter(getAtlasFromAsset("img/ect/item_img.atlas"));
        setTextureFilter(getAtlasFromAsset("img/ect/skill.atlas"));
        setTextureFilter(getAtlasFromAsset(BOX_ATLAS_PATH));
        setTextureFilter(getAtlasFromAsset(DOOR_ATLAS_PATH));
        setTextureFilter(getAtlasFromAsset(MAP_ICON_ATLAS_PATH));
        setTextureFilter(getAtlasFromAsset(BOSS_ATLAS_PATH));

        if (assetManager.isLoaded("img/ect/arrow.png"))
            arrowRegion = new TextureRegion(assetManager.get("img/ect/arrow.png", Texture.class));
        if (assetManager.isLoaded("img/ect/Enemyarrow.png"))
            enemyArrowRegion = new TextureRegion(assetManager.get("img/ect/Enemyarrow.png", Texture.class));

        if (assetManager.isLoaded(DOOR_PNG_PATH)) {
            Texture doorTex = assetManager.get(DOOR_PNG_PATH, Texture.class);
            doorOpenRegion = new TextureRegion(doorTex, 2, 2, 32, 26);
            doorCloseRegion = new TextureRegion(doorTex, 36, 2, 32, 26);
        }
    }

    private void loadAtlasSafe(String path) { try { assetManager.load(path, TextureAtlas.class); } catch (Exception e) {} }
    private void loadTextureSafe(String path) { try { assetManager.load(path, Texture.class); } catch (Exception e) {} }
    private TextureAtlas getAtlasFromAsset(String path) { return assetManager.isLoaded(path) ? assetManager.get(path, TextureAtlas.class) : null; }
    public TextureAtlas getAtlas(String atlasName) { return atlasMap.get(atlasName); }
    private void setTextureFilter(TextureAtlas atlas) { if(atlas==null)return; for(Texture t:atlas.getTextures()) t.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest); }
    private Animation<TextureRegion> createCoinAnimation(TextureAtlas atlas, String regionName, int tileWidth, int tileHeight) { TextureRegion region = atlas.findRegion(regionName); if (region == null) return null; TextureRegion[][] tmp = region.split(tileWidth, tileHeight); return new Animation<>(0.1f, new Array<>(tmp[0]), Animation.PlayMode.LOOP); }

    public AnimationData getAnimationData(String characterName) {
        if (animationDataMap.containsKey(characterName)) return animationDataMap.get(characterName);
        TextureAtlas atlas = atlasMap.get(characterName);
        if (atlas == null) return null;
        CharacterType type;
        try { type = CharacterType.valueOf(characterName.toUpperCase().replace(" ", "_")); }
        catch (IllegalArgumentException e) { type = CharacterType.ARCHER; }
        AnimationData data = new AnimationData(atlas, characterName, type);
        animationDataMap.put(characterName, data);
        return data;
    }

    public TextureAtlas getSlotAtlas() { return getAtlasFromAsset("img/UI/slot.atlas"); }
    public TextureAtlas getItemAtlas() { return getAtlasFromAsset("img/ect/item_img.atlas"); }
    public TextureAtlas getSkillAtlas() { return getAtlasFromAsset("img/ect/skill.atlas"); }
    public TextureRegion getSlotCoverTexture() {
        if (assetManager.isLoaded("img/ect/Inventory_Slot_1.png")) return new TextureRegion(assetManager.get("img/ect/Inventory_Slot_1.png", Texture.class));
        return null;
    }
    public Texture getUIBackTexture() { if (assetManager.isLoaded(UI_BACK_PATH)) return assetManager.get(UI_BACK_PATH, Texture.class); return null; }
    public TextureRegion getKeyTexture(String keyName) { TextureAtlas atlas = getAtlasFromAsset(KEY_ATLAS_PATH); return atlas != null ? atlas.findRegion(keyName) : null; }
    public TextureRegion getKeyPromptTexture() { return getKeyTexture("e"); }
    public Animation<TextureRegion> getChestAnimation(String grade) {
        TextureAtlas atlas = getAtlasFromAsset(BOX_ATLAS_PATH); if (atlas == null) return null;
        String regionName = "c_box";
        if (grade != null) { switch (grade.toUpperCase()) { case "UNCOMMON": regionName = "u_box"; break; case "RARE": regionName = "r_box"; break; case "EPIC": regionName = "e_box"; break; case "LEGENDARY": regionName = "l_box"; break; } }
        TextureRegion region = atlas.findRegion(regionName); if (region == null) return null;
        TextureRegion[][] tmp = region.split(16, 16); return new Animation<>(0.1f, new Array<>(tmp[0]), Animation.PlayMode.NORMAL);
    }
    public TextureRegion findTextureRegion(String name) {
        if (name == null) return null;
        TextureAtlas itemAtlas = getItemAtlas(); if (itemAtlas != null) { TextureRegion region = itemAtlas.findRegion(name); if (region != null) return region; }
        TextureAtlas boxAtlas = getAtlasFromAsset(BOX_ATLAS_PATH); if (boxAtlas != null) { TextureRegion region = boxAtlas.findRegion(name); if (region != null) { TextureRegion[][] tmp = region.split(16, 16); if (tmp != null && tmp.length > 0 && tmp[0].length > 0) return tmp[0][0]; return region; } }
        return null;
    }
    public TextureRegion getDoorTexture(boolean isOpen) {
        TextureAtlas atlas = getAtlasFromAsset(DOOR_ATLAS_PATH); if (atlas != null) return atlas.findRegion(isOpen ? "door_open" : "door_close");
        return isOpen ? doorOpenRegion : doorCloseRegion;
    }
    public TextureRegion getHeartTexture() { if (assetManager.isLoaded(HEART_PNG_PATH)) return new TextureRegion(assetManager.get(HEART_PNG_PATH, Texture.class)); return null; }
    public Texture getGoldIcon() { if (assetManager.isLoaded(GOLD_ICON_PATH)) return assetManager.get(GOLD_ICON_PATH, Texture.class); return null; }
    public Texture getStarIcon() { if (assetManager.isLoaded(STAR_ICON_PATH)) return assetManager.get(STAR_ICON_PATH, Texture.class); return null; }
    public TextureRegion getBossSkillTexture() { if (assetManager.isLoaded(BOSS_SKILL_PNG_PATH)) return new TextureRegion(assetManager.get(BOSS_SKILL_PNG_PATH, Texture.class)); return null; }
    public Texture getSoldOutTexture() { if (assetManager.isLoaded(SOLDOUT_PNG_PATH)) return assetManager.get(SOLDOUT_PNG_PATH, Texture.class); return null; }
    public Texture getLuckyIcon() { if (assetManager.isLoaded(LUCKY_ICON_PATH)) return assetManager.get(LUCKY_ICON_PATH, Texture.class); return null; }
    public Texture getBookIcon() { if (assetManager.isLoaded(BOOK_ICON_PATH)) return assetManager.get(BOOK_ICON_PATH, Texture.class); return null; }

    // ★★★ [추가] HP Bar 텍스처 Getter ★★★
    public Texture getHpBarFrameTexture() { if (assetManager.isLoaded(HP_BAR_FRAME_PATH)) return assetManager.get(HP_BAR_FRAME_PATH, Texture.class); return null; }
    public Texture getHpBarTexture() { if (assetManager.isLoaded(HP_BAR_PATH)) return assetManager.get(HP_BAR_PATH, Texture.class); return null; }

    public TextureAtlas getMapIconAtlas() { return getAtlasFromAsset(MAP_ICON_ATLAS_PATH); }
    public Texture getMapBgLeft() { return assetManager.isLoaded(MAP_BG_LEFT) ? assetManager.get(MAP_BG_LEFT, Texture.class) : null; }
    public Texture getMapBgCenter() { return assetManager.isLoaded(MAP_BG_CENTER) ? assetManager.get(MAP_BG_CENTER, Texture.class) : null; }
    public Texture getMapBgRight() { return assetManager.isLoaded(MAP_BG_RIGHT) ? assetManager.get(MAP_BG_RIGHT, Texture.class) : null; }

    @Override
    public void dispose() {
        assetManager.dispose();
        atlasMap.clear();
        animationDataMap.clear();
    }
}
