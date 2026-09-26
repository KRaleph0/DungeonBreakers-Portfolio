package com.team.dungeonbreakers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Box2D;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

import java.util.Iterator;

public class GameScreen implements Screen {

    private final DungeonBreakersGame game;

    private GameMapManager mapManager;
    private EntityManager entityManager;
    private GameUIManager uiManager;
    private CharacterManager characterManager;
    private DamageTextManager damageTextManager;
    private ItemManager itemManager;

    private AbstractPlayer player;
    private OrthographicCamera camera;
    private Viewport viewport;
    private Viewport uiViewport;
    private World world;
    private Box2DDebugRenderer debugRenderer;
    private ShapeRenderer shapeRenderer;
    private GameContactListener contactListener;

    private final int saveSlot;
    private final Vector3 mousePos = new Vector3();
    private boolean isPaused = false;

    // UI Components
    private InventoryUI inventoryUI;
    private ShopUI shopUI;
    private JobChangeUI jobChangeUI;
    private ResultUI resultUI;
    private UnlockUI unlockUI;

    private Array<SpawnTrigger> spawnTriggers;
    private String currentMapPath;

    // System Flags & Variables
    private boolean isTransitioning = false;
    private float accumulator = 0;
    private final float TIME_STEP = 1 / 60f;
    private float gameTime = 0f;

    // Map State
    private boolean isCombatMap = false;
    private boolean combatCleared = false;
    private boolean gameOverTriggered = false;

    public GameScreen(DungeonBreakersGame game, int saveSlot) {
        this(game, saveSlot, "map/lobby.tmx");
    }

    public GameScreen(DungeonBreakersGame game, int saveSlot, String mapPath) {
        this.game = game;
        this.saveSlot = saveSlot;
        this.currentMapPath = mapPath;
        this.characterManager = game.characterManager;
    }

    public GameScreen(DungeonBreakersGame game, int saveSlot, String mapPath, CharacterManager charManager) {
        this(game, saveSlot, mapPath);
    }

    @Override
    public void show() {
        Gdx.app.log("Game", "Load: " + currentMapPath);
        Gdx.graphics.setSystemCursor(Cursor.SystemCursor.None);
        isTransitioning = false;

        // 맵 타입 판별
        isCombatMap = currentMapPath.contains("map") && !currentMapPath.contains("store") &&
            !currentMapPath.contains("fire") && !currentMapPath.contains("box") &&
            !currentMapPath.contains("lobby") && !currentMapPath.contains("boss");
        // 보스맵은 전투맵으로 취급
        if (currentMapPath.contains("boss")) isCombatMap = true;

        combatCleared = false;
        gameOverTriggered = false;

        camera = new OrthographicCamera();
        viewport = new FitViewport(DungeonBreakersGame.V_WIDTH, DungeonBreakersGame.V_HEIGHT, camera);
        uiViewport = new FitViewport(DungeonBreakersGame.V_WIDTH, DungeonBreakersGame.V_HEIGHT);
        shapeRenderer = new ShapeRenderer();
        debugRenderer = new Box2DDebugRenderer();
        Box2D.init();

        world = new World(new Vector2(0, -25f), true);
        itemManager = new ItemManager();
        damageTextManager = new DamageTextManager();

        try {
            mapManager = new GameMapManager(currentMapPath);
        } catch (Exception e) {
            Gdx.app.error("GameScreen", "Map load failed", e);
            game.setScreen(new MainMenuScreen(game));
            return;
        }

        // UI 초기화
        uiManager = new GameUIManager(game.batch, game.uiFont, characterManager);
        inventoryUI = new InventoryUI(characterManager, itemManager, game.uiFont);
        shopUI = new ShopUI(characterManager, itemManager, inventoryUI, game.uiFont);

        // ★★★ [수정] 상점 UI 연결 (이게 없어서 판매가 안 됨) ★★★
        inventoryUI.setShopUI(shopUI);

        jobChangeUI = new JobChangeUI(this, characterManager, game.uiFont);
        resultUI = new ResultUI(this, characterManager, game.uiFont);
        unlockUI = new UnlockUI(characterManager, game.uiFont);

        entityManager = new EntityManager(world, characterManager, damageTextManager, itemManager, inventoryUI);

        contactListener = new GameContactListener();
        contactListener.setDamageTextManager(damageTextManager);
        contactListener.setGameScreen(this);
        contactListener.setInventoryUI(inventoryUI);
        world.setContactListener(contactListener);

        mapManager.createPhysics(world);
        spawnTriggers = mapManager.getSpawnTriggers(world);
        entityManager.spawnLobbyEntities(mapManager);
        entityManager.loadMonsterDataFromMap(mapManager);

        boolean isLobby = currentMapPath.contains("lobby");
        entityManager.spawnDoors(mapManager, isLobby);

        Vector2 spawnPoint = entityManager.getPlayerSpawnPoint(mapManager);
        float startX = (spawnPoint != null) ? spawnPoint.x : 400f;
        float startY = (spawnPoint != null) ? spawnPoint.y : 300f;
        createPlayer(startX, startY);

        // 맵 특수 엔티티 소환
        if (currentMapPath.contains("box")) {
            Vector2 pos = getChestSpawnPosition();
            entityManager.spawnChest(pos.x, pos.y, true); // 고등급 상자
        } else if (currentMapPath.contains("fire")) {
            entityManager.spawnBonfire(800, 300);
        } else if (currentMapPath.contains("boss")) {
            Array<Rectangle> bossSpawns = mapManager.getObjectRects("boss");
            if (bossSpawns.size > 0) {
                Rectangle r = bossSpawns.first();
                entityManager.spawnBoss(r.x + r.width / 2, r.y);
            } else {
                entityManager.spawnBoss(1000, 300); // 기본 위치
            }
        }

        if (inventoryUI != null) inventoryUI.setPlayer(player);
        contactListener.setPlayer(player);

        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        uiViewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
    }

    // 상자 소환 위치 (문 옆)
    private Vector2 getChestSpawnPosition() {
        if (entityManager.doors.size > 0) {
            DungeonDoor door = entityManager.doors.first();
            return new Vector2(door.x - 80f, door.y);
        }
        if (player != null) return new Vector2(player.x + 100f, player.y);
        return new Vector2(400f, 300f);
    }

    public void goToMainMenu() {
        isTransitioning = true;
        game.setScreen(new MainMenuScreen(game));
    }

    public void triggerTransition() {
        // 버프 및 체력 저장
        if (player != null) {
            PlayerData.getInstance().currentHp = (int) player.currentHp;
            PlayerData.getInstance().savedBuffs.clear();
            PlayerData.getInstance().savedBuffs.addAll(player.activeBuffs);
        }

        isTransitioning = true;
        if (currentMapPath.contains("lobby")) {
            game.setScreen(new MapScreen(game, saveSlot));
        } else {
            DungeonMapManager.getInstance().completeCurrentNode();
            game.setScreen(new MapScreen(game, saveSlot));
        }
    }

    private void createPlayer(float x, float y) {
        String charName = PlayerData.getInstance().currentJob;
        if (charName == null || charName.isEmpty()) charName = "Archer";

        AnimationData data = characterManager.getAnimationData(charName);
        if ("Knight".equals(charName)) {
            player = new Knight(data, world, damageTextManager, characterManager, entityManager.projectiles);
        } else {
            player = new Archer(data, world, entityManager.projectiles, camera, characterManager);
        }

        player.body.setTransform(x / DungeonBreakersGame.PPM, y / DungeonBreakersGame.PPM, 0);
        player.body.setUserData(player);
    }

    public void respawnPlayer() {
        float cx = player.x;
        float cy = player.y;
        if (player.body != null) world.destroyBody(player.body);
        createPlayer(cx, cy);
        contactListener.setPlayer(player);
        inventoryUI.setPlayer(player);
    }

    public boolean isCombatActive() {
        if (entityManager == null || entityManager.enemies == null) return false;
        for (AbstractEnemy enemy : entityManager.enemies) {
            if (!(enemy instanceof Dummy)) return true;
        }
        return false;
    }

    @Override
    public void render(float delta) {
        if (isTransitioning) return;

        if (!isPaused) {
            if (world != null && player != null) {
                // 사망 (패배) 체크
                if (player.isDead && !gameOverTriggered) {
                    gameOverTriggered = true;
                    resultUI.show(3, false); // 패배
                }

                if (!player.isDead) {
                    gameTime += delta;

                    // 전투 클리어 체크
                    if (isCombatMap && !combatCleared) {
                        if (entityManager.enemies.size == 0 && entityManager.pendingMonsters.size == 0) {
                            combatCleared = true;

                            if (currentMapPath.contains("boss")) {
                                gameOverTriggered = true;
                                resultUI.show(10, true);
                            } else {
                                // 일반 전투 -> 상자 소환 (플레이어 위치)
                                entityManager.spawnChest(player.x, player.y, false);
                                System.out.println("Stage Cleared! Chest Spawned at player.");
                            }
                        }
                    }

                    // 물리 연산 (고정 스텝)
                    float frameTime = Math.min(delta, 0.25f);
                    accumulator += frameTime;
                    while (accumulator >= TIME_STEP) {
                        world.step(TIME_STEP, 6, 2);
                        accumulator -= TIME_STEP;
                    }

                    handleGameInput();
                    if (isTransitioning) return;

                    player.update(delta);
                    entityManager.update(delta, player, mapManager.mapPixelWidth, mapManager.mapPixelHeight);
                    updateTriggers();
                    damageTextManager.update(delta);
                    updateCamera(delta);
                    for (Npc npc : entityManager.npcs) npc.update(delta);
                } else {
                    player.update(delta);
                }
            }
        } else {
            handlePauseInput();
        }

        Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        mapManager.render(camera);

        game.batch.setProjectionMatrix(camera.combined);
        game.batch.begin();
        entityManager.draw(game.batch, game.uiFont, player);
        player.draw(game.batch);
        damageTextManager.draw(game.batch);
        game.batch.end();

        uiManager.draw(uiViewport, player, isPaused, inventoryUI, gameTime);

        // UI Overlay
        if (shopUI != null && shopUI.isVisible()) {
            uiViewport.apply();
            game.batch.setProjectionMatrix(uiViewport.getCamera().combined);
            game.batch.begin();
            mousePos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            uiViewport.unproject(mousePos);
            shopUI.draw(game.batch, mousePos);
            inventoryUI.drawInventoryOnly(game.batch, mousePos);
            game.batch.end();
        } else if (inventoryUI != null && inventoryUI.isVisible()) {
            uiViewport.apply();
            game.batch.setProjectionMatrix(uiViewport.getCamera().combined);
            game.batch.begin();
            mousePos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            uiViewport.unproject(mousePos);
            inventoryUI.draw(game.batch, mousePos);
            game.batch.end();
        }

        if (jobChangeUI != null && jobChangeUI.isVisible()) {
            uiViewport.apply();
            game.batch.setProjectionMatrix(uiViewport.getCamera().combined);
            game.batch.begin();
            mousePos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            uiViewport.unproject(mousePos);
            jobChangeUI.draw(game.batch, mousePos);
            game.batch.end();
        }

        if (unlockUI != null && unlockUI.isVisible()) {
            uiViewport.apply();
            game.batch.setProjectionMatrix(uiViewport.getCamera().combined);
            game.batch.begin();
            mousePos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            uiViewport.unproject(mousePos);
            unlockUI.draw(game.batch, mousePos);
            game.batch.end();
        }

        if (resultUI != null && resultUI.isVisible()) {
            uiViewport.apply();
            game.batch.setProjectionMatrix(uiViewport.getCamera().combined);
            game.batch.begin();
            mousePos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            uiViewport.unproject(mousePos);
            resultUI.draw(game.batch, mousePos);
            game.batch.end();
        }

        drawAdaptiveCursor();
    }

    private void updateTriggers() {
        for (Iterator<SpawnTrigger> iter = spawnTriggers.iterator(); iter.hasNext(); ) {
            SpawnTrigger trigger = iter.next();
            if (trigger.isTriggered) {
                entityManager.spawnAllPendingMonsters();
                if (entityManager.enemies.size > 0) {
                    BattleStateManager.getInstance().startCombat(entityManager.enemies.size);
                }
                world.destroyBody(trigger.body);
                iter.remove();
            }
        }
    }

    private void updateCamera(float delta) {
        if (camera == null || player == null) return;
        float lerp = 5f * delta;
        camera.position.x += (player.x - camera.position.x) * lerp;
        camera.position.y += (player.y - camera.position.y) * lerp;

        float halfW = viewport.getWorldWidth() / 2;
        float halfH = viewport.getWorldHeight() / 2;
        float minX = halfW;
        float maxX = Math.max(halfW, mapManager.mapPixelWidth - halfW);
        float minY = halfH;
        float maxY = Math.max(halfH, mapManager.mapPixelHeight - halfH);

        camera.position.x = MathUtils.clamp(camera.position.x, minX, maxX);
        camera.position.y = MathUtils.clamp(camera.position.y, minY, maxY);
        camera.update();
    }

    private void drawAdaptiveCursor() {
        uiViewport.apply();
        game.batch.setProjectionMatrix(uiViewport.getCamera().combined);
        game.batch.begin();
        mousePos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        uiViewport.unproject(mousePos);

        boolean isUIOpen = isPaused ||
            (inventoryUI != null && inventoryUI.isVisible()) ||
            (shopUI != null && shopUI.isVisible()) ||
            (jobChangeUI != null && jobChangeUI.isVisible()) ||
            (unlockUI != null && unlockUI.isVisible()) ||
            (resultUI != null && resultUI.isVisible());

        if (isUIOpen) {
            if (game.normalCursorTexture != null) {
                game.batch.draw(game.normalCursorTexture, mousePos.x, mousePos.y - game.normalCursorTexture.getHeight());
            }
        } else {
            if (game.cursorTexture != null) {
                game.batch.draw(game.cursorTexture, mousePos.x - game.cursorTexture.getWidth() / 2f, mousePos.y - game.cursorTexture.getHeight() / 2f);
            }
        }
        game.batch.end();
    }

    private void handleGameInput() {
        if (gameOverTriggered) return;

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            if (unlockUI != null && unlockUI.isVisible()) { unlockUI.close(); return; }
            if (jobChangeUI != null && jobChangeUI.isVisible()) { jobChangeUI.close(); return; }
            if (shopUI != null && shopUI.isVisible()) { shopUI.close(); if (inventoryUI != null) inventoryUI.close(); return; }
            else if (inventoryUI != null && inventoryUI.isVisible()) { inventoryUI.close(); return; }
            else isPaused = !isPaused;
            return;
        }

        boolean isUIOpen = (shopUI != null && shopUI.isVisible()) ||
            (inventoryUI != null && inventoryUI.isVisible()) ||
            (jobChangeUI != null && jobChangeUI.isVisible()) ||
            (unlockUI != null && unlockUI.isVisible());

        if (isUIOpen) {
            player.body.setLinearVelocity(0, player.body.getLinearVelocity().y);
            player.isWalking = false;
            return;
        }

        if (player == null || player.isHurt || isPaused) return;

        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) useQuickSlot(0);
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) useQuickSlot(1);

        if (Gdx.input.isKeyJustPressed(Input.Keys.I)) {
            if (inventoryUI != null) {
                if (!isCombatActive()) inventoryUI.toggle();
                else System.out.println("전투 중 불가");
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.E)) checkNpcInteraction();
        if (Gdx.input.isKeyJustPressed(Input.Keys.Q)) player.castSkill();

        mousePos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        camera.unproject(mousePos);
        player.isFacingRight = (mousePos.x > player.x);

        if (Gdx.input.isKeyJustPressed(Input.Keys.W) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) player.jump();
        if (Gdx.input.isButtonJustPressed(Input.Buttons.RIGHT)) player.dash(new Vector2(mousePos.x - player.x, mousePos.y - player.y));

        if (!player.isDashing) {
            int force = 0;
            if (Gdx.input.isKeyPressed(Input.Keys.A)) force = -1;
            if (Gdx.input.isKeyPressed(Input.Keys.D)) force = 1;
            player.body.setLinearVelocity(force * 20.0f, player.body.getLinearVelocity().y);
            player.isWalking = force != 0;
        }
        if (Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
            if (player.canAttack()) player.attack();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.P)) player.attackpoint += 10;
    }

    private void useQuickSlot(int slotIndex) {
        int equipIndex = (slotIndex == 0) ? 3 : 7;
        Array<Item> equipment = PlayerData.getInstance().equipmentItems;
        if (equipIndex < equipment.size) {
            Item item = equipment.get(equipIndex);
            if (item != null) {
                boolean used = false;
                if ("HEAL_INSTANT".equals(item.effectType)) {
                    player.heal(item.value);
                    used = true;
                } else if (item.effectType != null && (item.effectType.startsWith("BUFF") || "HEAL_OVER_TIME".equals(item.effectType))) {
                    String iconName = item.textureRegionName;
                    if ("BUFF_ATTACK".equals(item.effectType)) iconName = "BUFF_ATTACK";
                    else if ("BUFF_DEFENSE".equals(item.effectType)) iconName = "BUFF_DEFENSE";
                    else if ("BUFF_CRIT_CHANCE".equals(item.effectType)) iconName = "BUFF_CRIT_CHANCE";
                    else if ("HEAL_OVER_TIME".equals(item.effectType)) iconName = "HEAL_OVER_TIME";

                    TextureRegion icon = characterManager.findTextureRegion(iconName);
                    if (icon == null) icon = characterManager.findTextureRegion(item.textureRegionName);

                    AbstractPlayer.BuffType type = null;
                    if ("HEAL_OVER_TIME".equals(item.effectType)) type = AbstractPlayer.BuffType.HEAL_OVER_TIME;
                    else if ("BUFF_ATTACK".equals(item.effectType)) type = AbstractPlayer.BuffType.BUFF_ATTACK;
                    else if ("BUFF_DEFENSE".equals(item.effectType)) type = AbstractPlayer.BuffType.BUFF_DEFENSE;
                    else if ("BUFF_CRIT_CHANCE".equals(item.effectType)) type = AbstractPlayer.BuffType.BUFF_CRIT_CHANCE;

                    if (type != null) {
                        player.addBuff(type, item.value, item.duration, icon);
                        used = true;
                    }
                }
                if (used) {
                    item.count--;
                    if (item.count <= 0) equipment.set(equipIndex, null);
                }
            }
        }
    }

    private void checkNpcInteraction() {
        float interactRange = 100f;
        boolean interacted = false;
        for (Npc npc : entityManager.npcs) {
            if (Vector2.dst(player.x, player.y, npc.x, npc.y) <= interactRange) {
                npc.interact();
                if (npc.type == NpcType.MERCHANT) {
                    if (shopUI != null) {
                        shopUI.toggle();
                        if (inventoryUI != null && !inventoryUI.isVisible()) inventoryUI.toggle();
                    }
                } else if (npc.type == NpcType.PRIEST_JOB_CHANGE) {
                    if (jobChangeUI != null) jobChangeUI.toggle();
                } else if (npc.type == NpcType.KNIGHT_UNLOCK) {
                    if (unlockUI != null) unlockUI.toggle();
                }
                interacted = true;
                break;
            }
        }
        if (!interacted && entityManager.chests != null) {
            for (Chest chest : entityManager.chests) {
                if (!chest.isRemoved() && Vector2.dst(player.x, player.y, chest.x, chest.y) <= interactRange) {
                    chest.interact(player);
                    interacted = true;
                    break;
                }
            }
        }
        if (!interacted && entityManager.bonfires != null) {
            for (Bonfire fire : entityManager.bonfires) {
                if (!fire.isUsed() && Vector2.dst(player.x, player.y, fire.x, fire.y) <= 100f) {
                    fire.interact(player);
                    interacted = true;
                    break;
                }
            }
        }
        if (!interacted && entityManager.doors != null) {
            for (DungeonDoor door : entityManager.doors) {
                if (door.isOpen() && Vector2.dst(player.x, player.y, door.x, door.y) <= interactRange) {
                    triggerTransition();
                    interacted = true;
                    break;
                }
            }
        }
    }

    private void giveReward() {
        if (itemManager != null && inventoryUI != null) {
            Item item = itemManager.getRandomItem();
            if (item != null && inventoryUI.addItem(item)) System.out.println("Reward: " + item.name);
        }
    }

    private void handlePauseInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) isPaused = !isPaused;
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        uiViewport.update(width, height, true);
    }

    @Override public void pause() {}
    @Override public void resume() {}

    @Override
    public void hide() {
        dispose();
    }

    @Override
    public void dispose() {
        if (isTransitioning) return;

        if (mapManager != null) mapManager.dispose();
        if (entityManager != null) entityManager.dispose();
        if (uiManager != null) uiManager.dispose();
        if (world != null) world.dispose();
        if (debugRenderer != null) debugRenderer.dispose();
        if (damageTextManager != null) damageTextManager.dispose();
        if (shapeRenderer != null) shapeRenderer.dispose();
        if (inventoryUI != null) inventoryUI.dispose();
    }
}
