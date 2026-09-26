package com.team.dungeonbreakers;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import java.util.Iterator;

public class EntityManager implements com.badlogic.gdx.utils.Disposable {
    private final World world;
    private final CharacterManager charManager;
    private final DamageTextManager damageTextManager;
    private ItemManager itemManager;
    private InventoryUI inventoryUI;

    public Array<AbstractEnemy> enemies = new Array<>();
    public Array<Npc> npcs = new Array<>();
    public Array<Projectile> projectiles = new Array<>();
    public Array<Coin> coins = new Array<>();
    public Array<DroppedItem> droppedItems = new Array<>();
    public Array<Chest> chests = new Array<>();
    public Array<DungeonDoor> doors = new Array<>();
    public Array<Bonfire> bonfires = new Array<>();

    public static class PendingMonster {
        float x, y;
        String type;
        public PendingMonster(float x, float y, String type) { this.x = x; this.y = y; this.type = type; }
    }
    public Array<PendingMonster> pendingMonsters = new Array<>();
    private TextureRegion keyPromptTexture;
    private final float INTERACTION_RANGE = 100f;

    public EntityManager(World world, CharacterManager charManager, DamageTextManager damageTextManager, ItemManager itemManager, InventoryUI inventoryUI) {
        this.world = world;
        this.charManager = charManager;
        this.damageTextManager = damageTextManager;
        this.itemManager = itemManager;
        this.inventoryUI = inventoryUI;
        this.keyPromptTexture = charManager.getKeyPromptTexture();
    }

    // ★★★ [수정] 보스 소환 메서드 ★★★
    public void spawnBoss(float x, float y) {
        com.badlogic.gdx.graphics.g2d.TextureAtlas bossAtlas = charManager.getAtlas("Greatsword Skeleton");
        TextureRegion skillTex = charManager.getBossSkillTexture();

        if (bossAtlas != null && skillTex != null) {
            Boss boss = new Boss(world, bossAtlas, x, y, this.projectiles, skillTex);

            // ★ HP 바 텍스처 주입
            boss.setHpBarTextures(charManager.getHpBarFrameTexture(), charManager.getHpBarTexture());

            // ★ 보스 모드 활성화 (크기 2배, HP바 ON)
            boss.setBoss(true);

            enemies.add(boss);
            System.out.println("BOSS Spawned at " + x + ", " + y);
        } else {
            System.out.println("Failed to spawn Boss: Assets missing.");
        }
    }

    // ... (나머지 update, draw, spawnCoins 등등 메서드 그대로 유지 - 변경 없음)
    public void update(float delta, AbstractPlayer player, float mapPixelWidth, float mapPixelHeight) { for (Iterator<AbstractEnemy> iter = enemies.iterator(); iter.hasNext(); ) { AbstractEnemy enemy = iter.next(); enemy.update(player, projectiles, delta); if (!enemy.isAlive()) { if (!enemy.physicsBodySetToDead) enemy.setPhysicsBodyToDead(); if (enemy.isDying && !enemy.droppedLoot) { if (!(enemy instanceof Dummy)) spawnCoins(enemy.x, enemy.y); enemy.droppedLoot = true; } } if (enemy.isReadyForRemoval()) { if (enemy.body != null) { world.destroyBody(enemy.body); enemy.body = null; } iter.remove(); } } for (Npc npc : npcs) npc.update(delta); for (Iterator<Chest> iter = chests.iterator(); iter.hasNext(); ) { Chest chest = iter.next(); chest.update(delta); if (chest.isRemoved()) iter.remove(); } boolean isCombatActive = false; for (AbstractEnemy enemy : enemies) { if (!(enemy instanceof Dummy) && enemy.isAlive()) { isCombatActive = true; break; } } for (DungeonDoor door : doors) door.update(isCombatActive); for (Iterator<Projectile> iter = projectiles.iterator(); iter.hasNext(); ) { Projectile proj = iter.next(); proj.update(); if (!proj.isActive() || proj.x < 0 || proj.x > mapPixelWidth || proj.y < 0 || proj.y > mapPixelHeight) { if (proj.body != null) world.destroyBody(proj.body); proj.dispose(); iter.remove(); } } for (Iterator<Coin> iter = coins.iterator(); iter.hasNext(); ) { Coin coin = iter.next(); coin.update(delta); if (!coin.isActive()) { if (coin.body != null) world.destroyBody(coin.body); iter.remove(); } } for (Iterator<DroppedItem> iter = droppedItems.iterator(); iter.hasNext(); ) { DroppedItem item = iter.next(); item.update(delta); if (!item.isActive()) { if (item.body != null) world.destroyBody(item.body); iter.remove(); } } }
    public void draw(SpriteBatch batch, BitmapFont font, AbstractPlayer player) { for (DungeonDoor door : doors) { door.draw(batch); if (player != null && keyPromptTexture != null && door.isOpen()) { if (Vector2.dst(player.x, player.y, door.x, door.y) <= INTERACTION_RANGE) drawKeyPrompt(batch, door.x, door.y + 60f); } } for (Bonfire fire : bonfires) { fire.draw(batch); if (!fire.isUsed() && player != null && keyPromptTexture != null) { if (Vector2.dst(player.x, player.y, fire.x, fire.y) <= INTERACTION_RANGE) drawKeyPrompt(batch, fire.x, fire.y + 60f); } } for (Npc npc : npcs) { npc.draw(batch, font); if (player != null && keyPromptTexture != null) { if (Vector2.dst(player.x, player.y, npc.x, npc.y) <= INTERACTION_RANGE) drawKeyPrompt(batch, npc.x, npc.y + 80f); } } for (Chest chest : chests) { chest.draw(batch); if (player != null && keyPromptTexture != null && !chest.isRemoved()) { if (Vector2.dst(player.x, player.y, chest.x, chest.y) <= INTERACTION_RANGE) drawKeyPrompt(batch, chest.x, chest.y + 60f); } } for (AbstractEnemy enemy : enemies) { enemy.draw(batch); if (enemy instanceof Dummy) ((Dummy) enemy).drawStats(batch, font); } for (Coin coin : coins) coin.draw(batch); for (Projectile proj : projectiles) proj.draw(batch); for (DroppedItem item : droppedItems) item.draw(batch); }
    private void drawKeyPrompt(SpriteBatch batch, float x, float y) { float iconWidth = keyPromptTexture.getRegionWidth() * 2.0f; float iconHeight = keyPromptTexture.getRegionHeight() * 2.0f; batch.draw(keyPromptTexture, x - (iconWidth / 2f), y, iconWidth, iconHeight); }
    public void spawnCoins(float x, float y) { int totalGold = MathUtils.random(100, 200); int goldCount = totalGold / 100; int remainder = totalGold % 100; int silverCount = remainder / 10; int copperCount = remainder % 10; for (int i = 0; i < goldCount; i++) coins.add(new Coin(world, x, y + 20f, Coin.CoinType.GOLD, charManager)); for (int i = 0; i < silverCount; i++) coins.add(new Coin(world, x, y + 20f, Coin.CoinType.SILVER, charManager)); for (int i = 0; i < copperCount; i++) coins.add(new Coin(world, x, y + 20f, Coin.CoinType.COPPER, charManager)); }
    public void spawnRandomItem(float x, float y) { if (itemManager == null) return; Item randomItem = itemManager.getRandomItem(); if (randomItem != null) droppedItems.add(new DroppedItem(world, x, y + 20f, randomItem, charManager)); }
    public void spawnChest(float x, float y, boolean isHighGrade) { String grade = "COMMON"; float r = MathUtils.random(); if (isHighGrade) { if (r < 0.05f) grade = "LEGENDARY"; else if (r < 0.25f) grade = "EPIC"; else if (r < 0.60f) grade = "RARE"; else if (r < 0.90f) grade = "UNCOMMON"; else grade = "COMMON"; } else { if (r < 0.02f) grade = "LEGENDARY"; else if (r < 0.10f) grade = "EPIC"; else if (r < 0.30f) grade = "RARE"; else if (r < 0.60f) grade = "UNCOMMON"; else grade = "COMMON"; } chests.add(new Chest(x, y, grade, charManager, inventoryUI, itemManager)); System.out.println("Spawned Chest (" + grade + ") at " + x + ", " + y); }
    public void spawnBonfire(float x, float y) { bonfires.add(new Bonfire(x, y, charManager)); }
    public void spawnAllPendingMonsters() { if (pendingMonsters.size == 0) return; for (PendingMonster pm : pendingMonsters) { if ("Skeleton_Archer".equals(pm.type)) { enemies.add(new SkeletonArcher(world, charManager.getAtlas("Skeleton_Archer"), pm.x, pm.y, charManager.enemyArrowRegion, this.projectiles)); } else { enemies.add(new ArmoredSkeleton(world, charManager.getAtlas("Armored_Skeleton"), pm.x, pm.y)); } } pendingMonsters.clear(); }
    public void spawnDoors(GameMapManager mapManager, boolean isLobby) { Array<com.badlogic.gdx.math.Rectangle> doorRects = mapManager.getObjectRects("dungeon_entrance"); for (com.badlogic.gdx.math.Rectangle rect : doorRects) { float doorX = rect.x + rect.width / 2; float doorY = rect.y; doors.add(new DungeonDoor(doorX, doorY, isLobby, charManager)); } }
    public void spawnLobbyEntities(GameMapManager mapManager) { for (com.badlogic.gdx.math.Rectangle rect : mapManager.getObjectRects("dummy")) enemies.add(new Dummy(world, charManager, rect.x, rect.y)); for (com.badlogic.gdx.math.Rectangle rect : mapManager.getObjectRects("npc1")) npcs.add(new Npc(world, charManager, NpcType.PRIEST_JOB_CHANGE, rect.x, rect.y)); for (com.badlogic.gdx.math.Rectangle rect : mapManager.getObjectRects("npc2")) npcs.add(new Npc(world, charManager, NpcType.KNIGHT_UNLOCK, rect.x, rect.y)); for (com.badlogic.gdx.math.Rectangle rect : mapManager.getObjectRects("trader")) npcs.add(new Npc(world, charManager, NpcType.MERCHANT, rect.x, rect.y + 50f)); }
    public void loadMonsterDataFromMap(GameMapManager mapManager) { for (com.badlogic.gdx.math.Rectangle rect : mapManager.getObjectRects("armored_skeleton_spawn")) pendingMonsters.add(new PendingMonster(rect.x, rect.y, "Armored_Skeleton")); for (com.badlogic.gdx.math.Rectangle rect : mapManager.getObjectRects("skeleton_archer_spawn")) pendingMonsters.add(new PendingMonster(rect.x, rect.y, "Skeleton_Archer")); }
    public Vector2 getPlayerSpawnPoint(GameMapManager mapManager) { Array<com.badlogic.gdx.math.Rectangle> spawns = mapManager.getObjectRects("player_spawn"); if (spawns.size > 0) return new Vector2(spawns.get(0).x, spawns.get(0).y); spawns = mapManager.getObjectRects("player"); if (spawns.size > 0) return new Vector2(spawns.get(0).x, spawns.get(0).y); return null; }
    @Override public void dispose() { for (Projectile proj : projectiles) proj.dispose(); projectiles.clear(); coins.clear(); droppedItems.clear(); chests.clear(); doors.clear(); bonfires.clear(); }
}
