package com.team.dungeonbreakers;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

public class GameMapManager implements Disposable {
    public TiledMap tiledMap;
    private OrthogonalTiledMapRenderer mapRenderer;
    private final float MAP_SCALE = 4.0f;

    public float mapPixelWidth;
    public float mapPixelHeight;

    public GameMapManager(String mapPath) {
        try {
            tiledMap = new TmxMapLoader().load(mapPath);
            mapRenderer = new OrthogonalTiledMapRenderer(tiledMap, MAP_SCALE);

            MapProperties prop = tiledMap.getProperties();
            int mapWidth = prop.get("width", Integer.class);
            int mapHeight = prop.get("height", Integer.class);
            int tilePixelWidth = prop.get("tilewidth", Integer.class);
            int tilePixelHeight = prop.get("tileheight", Integer.class);

            mapPixelWidth = mapWidth * tilePixelWidth * MAP_SCALE;
            mapPixelHeight = mapHeight * tilePixelHeight * MAP_SCALE;
        } catch (Exception e) {
            System.err.println("맵 로드 실패: " + mapPath);
            e.printStackTrace();
        }
    }

    public void render(OrthographicCamera camera) {
        if (mapRenderer != null) {
            mapRenderer.setView(camera);
            mapRenderer.render();
        }
    }

    private MapLayer getLayerIgnoreCase(String layerName) {
        if (tiledMap == null) return null;
        MapLayer layer = tiledMap.getLayers().get(layerName);
        if (layer != null) return layer;
        for (MapLayer l : tiledMap.getLayers()) {
            if (l.getName().equalsIgnoreCase(layerName)) {
                return l;
            }
        }
        return null;
    }

    public void createPhysics(World world) {
        String[] targetLayers = {"floor", "FLOOR", "wall", "Collision", "Layer 1"};

        for (String layerName : targetLayers) {
            MapLayer layer = getLayerIgnoreCase(layerName);
            if (layer == null) continue;

            String userData = "ground";
            if (layerName.equalsIgnoreCase("wall")) userData = "wall";

            for (MapObject object : layer.getObjects()) {
                if (object instanceof RectangleMapObject) {
                    Rectangle rect = ((RectangleMapObject) object).getRectangle();
                    createStaticBody(world, rect, userData, DungeonBreakersGame.GROUND_BIT, false);
                }
            }
        }

        MapLayer platformLayer = getLayerIgnoreCase("platforms");
        if (platformLayer != null) {
            for (MapObject object : platformLayer.getObjects()) {
                if (object instanceof RectangleMapObject) {
                    Rectangle rect = ((RectangleMapObject) object).getRectangle();
                    createStaticBody(world, rect, "platform", DungeonBreakersGame.PLATFORM_BIT, false);
                }
            }
        }
    }

    public void createDungeonEntrance(World world) {
        // 물리적 입구 생성 (이동 로직은 GameScreen에서 E키로 처리)
        // 대소문자 무시 검색
        MapLayer layer = getLayerIgnoreCase("dungeon_entrance");

        if (layer != null) {
            for (MapObject object : layer.getObjects()) {
                if (object instanceof RectangleMapObject) {
                    createStaticBody(world, ((RectangleMapObject) object).getRectangle(), "dungeon_entrance", DungeonBreakersGame.TRIGGER_BIT, true);
                }
            }
        } else {
            // 레이어 없으면 오브젝트 이름으로 검색
            for (MapLayer l : tiledMap.getLayers()) {
                for (MapObject object : l.getObjects()) {
                    if (object instanceof RectangleMapObject && "dungeon_entrance".equalsIgnoreCase(object.getName())) {
                        createStaticBody(world, ((RectangleMapObject) object).getRectangle(), "dungeon_entrance", DungeonBreakersGame.TRIGGER_BIT, true);
                    }
                }
            }
        }
    }

    private void createStaticBody(World world, Rectangle rect, String userData, short categoryBit, boolean isSensor) {
        float scaledX = rect.x * MAP_SCALE;
        float scaledY = rect.y * MAP_SCALE;
        float scaledWidth = rect.width * MAP_SCALE;
        float scaledHeight = rect.height * MAP_SCALE;

        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set((scaledX + scaledWidth / 2) / DungeonBreakersGame.PPM, (scaledY + scaledHeight / 2) / DungeonBreakersGame.PPM);
        Body body = world.createBody(bodyDef);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(scaledWidth / 2 / DungeonBreakersGame.PPM, scaledHeight / 2 / DungeonBreakersGame.PPM);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.friction = 0.6f;
        fixtureDef.isSensor = isSensor;
        fixtureDef.filter.categoryBits = categoryBit;

        body.createFixture(fixtureDef).setUserData(userData);
        shape.dispose();
    }

    public Array<SpawnTrigger> getSpawnTriggers(World world) {
        Array<SpawnTrigger> triggers = new Array<>();
        MapLayer layer = getLayerIgnoreCase("spawn area");
        if (layer != null) {
            for (MapObject object : layer.getObjects()) {
                if (object instanceof RectangleMapObject) {
                    Rectangle rect = ((RectangleMapObject) object).getRectangle();
                    Rectangle scaledRect = new Rectangle(rect.x * MAP_SCALE, rect.y * MAP_SCALE, rect.width * MAP_SCALE, rect.height * MAP_SCALE);
                    triggers.add(new SpawnTrigger(world, scaledRect));
                }
            }
        }
        return triggers;
    }

    // ★★★ [수정] 레이어+오브젝트 전수 조사 ★★★
    public Array<Rectangle> getObjectRects(String targetName) {
        Array<Rectangle> rects = new Array<>();

        // 1. 레이어 검색
        MapLayer layer = getLayerIgnoreCase(targetName);
        if (layer != null) {
            for (MapObject object : layer.getObjects()) {
                if (object instanceof RectangleMapObject) {
                    Rectangle rect = ((RectangleMapObject) object).getRectangle();
                    rects.add(new Rectangle(rect.x * MAP_SCALE, rect.y * MAP_SCALE, rect.width * MAP_SCALE, rect.height * MAP_SCALE));
                }
            }
        }

        // 2. 오브젝트 이름 검색 (추가 보완)
        for (MapLayer l : tiledMap.getLayers()) {
            if (l == layer) continue; // 이미 위에서 처리함
            for (MapObject object : l.getObjects()) {
                if (object instanceof RectangleMapObject && targetName.equalsIgnoreCase(object.getName())) {
                    Rectangle rect = ((RectangleMapObject) object).getRectangle();
                    rects.add(new Rectangle(rect.x * MAP_SCALE, rect.y * MAP_SCALE, rect.width * MAP_SCALE, rect.height * MAP_SCALE));
                }
            }
        }

        if (rects.size == 0) {
            System.out.println("[GameMapManager] Object/Layer '" + targetName + "' not found.");
        } else {
            System.out.println("[GameMapManager] Found " + rects.size + " rects for '" + targetName + "'");
        }

        return rects;
    }

    @Override
    public void dispose() {
        if (tiledMap != null) tiledMap.dispose();
        if (mapRenderer != null) mapRenderer.dispose();
    }
}
