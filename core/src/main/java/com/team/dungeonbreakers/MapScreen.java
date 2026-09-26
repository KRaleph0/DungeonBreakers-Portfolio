package com.team.dungeonbreakers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Cursor; // ★ 수정: Cursor 임포트 추가
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class MapScreen implements Screen {
    private final DungeonBreakersGame game;
    private final int saveSlot;

    private OrthographicCamera camera;
    private FitViewport viewport;
    private ShapeRenderer shapeRenderer;
    private SpriteBatch batch;
    private BitmapFont font;

    private DungeonMapManager mapManager;
    private CharacterManager charManager;

    private final float NODE_SIZE = 48f;
    private final float MAP_START_X = 100f;
    private final float GAP_X = 120f;
    private final float GAP_Y = 80f;

    private float scrollX = 0;
    private final float VIEWPORT_WIDTH = 1600f;
    private final float TOTAL_MAP_WIDTH;

    private Vector3 mousePos = new Vector3();
    private Texture bgLeft, bgCenter, bgRight;
    private TextureAtlas mapIconAtlas;

    // ★ 화면 전환 중인지 체크하는 플래그 (크래시 방지)
    private boolean isTransitioning = false;

    public MapScreen(DungeonBreakersGame game, int saveSlot) {
        this(game, saveSlot, game.characterManager);
    }

    public MapScreen(DungeonBreakersGame game, int saveSlot, CharacterManager charManager) {
        this.game = game;
        this.saveSlot = saveSlot;
        this.charManager = charManager;
        this.mapManager = DungeonMapManager.getInstance();

        this.batch = game.batch;
        this.font = game.uiFont;

        this.TOTAL_MAP_WIDTH = MAP_START_X + (mapManager.STAGES * GAP_X) + 200f;
    }

    @Override
    public void show() {
        // 시스템 커서 숨김
        Gdx.graphics.setSystemCursor(Cursor.SystemCursor.None);

        camera = new OrthographicCamera();
        viewport = new FitViewport(1600, 900, camera);
        shapeRenderer = new ShapeRenderer();

        bgLeft = charManager.getMapBgLeft();
        bgCenter = charManager.getMapBgCenter();
        bgRight = charManager.getMapBgRight();
        mapIconAtlas = charManager.getMapIconAtlas();

        calculateNodePositions();

        if (mapManager.currentNode != null) {
            scrollX = mapManager.currentNode.uiPosition.x - VIEWPORT_WIDTH / 2;
        } else {
            scrollX = 0;
        }
        clampScroll();

        isTransitioning = false;
    }

    private void calculateNodePositions() {
        for (Array<MapNode> layer : mapManager.mapLayers) {
            for (MapNode node : layer) {
                float x = MAP_START_X + (node.col * GAP_X);
                float y = 450f + (node.row - 3) * GAP_Y + MathUtils.random(-10f, 10f);
                node.uiPosition.set(x, y);
            }
        }
    }

    @Override
    public void render(float delta) {
        // ★ 화면 전환 중이면 렌더링 중단 (크래시 방지)
        if (isTransitioning) return;

        handleInput(delta);

        // 입력 처리 중 전환되었을 수 있으므로 다시 체크
        if (isTransitioning) return;

        Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.position.set(VIEWPORT_WIDTH / 2 + scrollX, 450, 0);
        camera.update();
        batch.setProjectionMatrix(camera.combined);
        shapeRenderer.setProjectionMatrix(camera.combined);

        batch.begin();
        drawBackground(batch);
        batch.end();

        Gdx.gl.glEnable(GL20.GL_BLEND);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        for (Array<MapNode> layer : mapManager.mapLayers) {
            for (MapNode node : layer) {
                for (MapNode child : node.children) {
                    if (node.status == NodeStatus.COMPLETED && child.status == NodeStatus.AVAILABLE) shapeRenderer.setColor(1, 1, 0.5f, 0.8f);
                    else if (node.status == NodeStatus.COMPLETED && child.status == NodeStatus.COMPLETED) shapeRenderer.setColor(0.5f, 0.5f, 0.5f, 0.5f);
                    else shapeRenderer.setColor(0.3f, 0.3f, 0.3f, 0.3f);
                    shapeRenderer.line(node.uiPosition, child.uiPosition);
                }
            }
        }
        shapeRenderer.end();

        batch.begin();
        for (Array<MapNode> layer : mapManager.mapLayers) {
            for (MapNode node : layer) drawNode(batch, node);
        }
        batch.end();

        drawAdaptiveCursor();
    }

    private void drawAdaptiveCursor() {
        batch.begin();
        mousePos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        viewport.unproject(mousePos);
        if (game.normalCursorTexture != null) {
            batch.draw(game.normalCursorTexture, mousePos.x, mousePos.y - game.normalCursorTexture.getHeight());
        }
        batch.end();
    }

    private void drawBackground(SpriteBatch batch) {
        if (bgLeft == null) return;
        float x = 0;
        batch.draw(bgLeft, x, 0, bgLeft.getWidth() * 2, 900);
        x += bgLeft.getWidth() * 2;
        float centerW = bgCenter != null ? bgCenter.getWidth() * 2 : 100;
        int repeat = (int)((TOTAL_MAP_WIDTH - x) / centerW) + 1;
        for(int i=0; i<repeat; i++) { if (bgCenter != null) batch.draw(bgCenter, x, 0, centerW, 900); x += centerW; }
        if (bgRight != null) batch.draw(bgRight, x, 0, bgRight.getWidth() * 2, 900);
    }

    private void drawNode(SpriteBatch batch, MapNode node) {
        TextureRegion icon = null;
        if (mapIconAtlas != null) icon = mapIconAtlas.findRegion(node.type.iconName);
        float size = (node.type == NodeType.BOSS) ? NODE_SIZE * 1.8f : NODE_SIZE;
        float drawX = node.uiPosition.x - size / 2;
        float drawY = node.uiPosition.y - size / 2;
        if (node.status == NodeStatus.LOCKED) batch.setColor(0.4f, 0.4f, 0.4f, 0.7f);
        else if (node.status == NodeStatus.AVAILABLE) {
            float alpha = 0.7f + 0.3f * (float)Math.sin(System.currentTimeMillis() / 150.0);
            batch.setColor(1f, 1f, 1f, alpha);
            size *= 1.2f; drawX = node.uiPosition.x - size / 2; drawY = node.uiPosition.y - size / 2;
        } else if (node.status == NodeStatus.COMPLETED) batch.setColor(0.3f, 0.3f, 0.3f, 0.5f);
        else batch.setColor(1, 1, 1, 1);

        if (icon != null) batch.draw(icon, drawX, drawY, size, size);
        else font.draw(batch, node.type.name().substring(0,1), drawX, drawY + size);
        batch.setColor(1, 1, 1, 1);
    }

    private void handleInput(float delta) {
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D)) { scrollX += 800 * delta; clampScroll(); }
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A)) { scrollX -= 800 * delta; clampScroll(); }
        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            mousePos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            viewport.unproject(mousePos);
            for (Array<MapNode> layer : mapManager.mapLayers) {
                for (MapNode node : layer) {
                    if (node.status == NodeStatus.AVAILABLE && node.uiPosition.dst(mousePos.x, mousePos.y) < NODE_SIZE * 1.5f) {
                        selectNode(node); return;
                    }
                }
            }
        }
    }

    private void clampScroll() {
        if (scrollX < 0) scrollX = 0;
        if (scrollX > TOTAL_MAP_WIDTH - VIEWPORT_WIDTH) scrollX = TOTAL_MAP_WIDTH - VIEWPORT_WIDTH;
    }

    private void selectNode(MapNode node) {
        System.out.println("Selected Node: " + node.type + " (Stage " + node.col + ")");
        mapManager.currentNode = node;

        // 맵 비활성화 및 경로 설정
        mapManager.disableSiblings(node);

        String mapPath = "map/map1.tmx";
        switch (node.type) {
            case SHOP: mapPath = "map/store.tmx"; break;
            case BOSS: mapPath = "map/boss.tmx"; break;
            case REST: mapPath = "map/fire.tmx"; break;
            case CHEST: mapPath = "map/box.tmx"; break;
            case MONSTER: int r = MathUtils.random(1, 10); mapPath = "map/map" + r + ".tmx"; break;
            default: mapPath = "map/map1.tmx"; break;
        }

        // ★ 화면 전환 플래그 설정 및 GameScreen 이동
        isTransitioning = true;
        game.setScreen(new GameScreen(game, saveSlot, mapPath));
    }

    @Override public void resize(int width, int height) { viewport.update(width, height); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() { dispose(); }

    @Override public void dispose() {
        // ★ 전환 중이면 리소스 해제 방지
        if (isTransitioning) return;
        if (shapeRenderer != null) shapeRenderer.dispose();
        // characterManager는 Game에서 관리하므로 dispose하지 않음
    }
}
