package com.team.dungeonbreakers;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

public class DungeonMapManager {
    private static DungeonMapManager instance;

    public Array<Array<MapNode>> mapLayers;
    public final int STAGES = 12;
    public final int ROWS = 3;

    public MapNode currentNode = null;

    private DungeonMapManager() {
        generateMap();
    }

    public static DungeonMapManager getInstance() {
        if (instance == null) {
            instance = new DungeonMapManager();
        }
        return instance;
    }

    public void generateMap() {
        mapLayers = new Array<>();
        currentNode = null;

        for (int c = 0; c < STAGES; c++) {
            Array<MapNode> layer = new Array<>();
            int nodeCount = (c == 0 || c == STAGES - 1 || c == STAGES - 2) ?
                MathUtils.random(1, 2) : MathUtils.random(2, 3);
            if (c == STAGES - 1) nodeCount = 1;

            Array<Integer> indices = new Array<>();
            while (indices.size < nodeCount) {
                int idx = MathUtils.random(0, ROWS - 1);
                if (!indices.contains(idx, false)) indices.add(idx);
            }
            indices.sort();

            for (int row : indices) {
                NodeType type = getRandomType(c);
                MapNode node = new MapNode(type, c, row);
                if (c == 0) node.status = NodeStatus.AVAILABLE;
                layer.add(node);
            }
            mapLayers.add(layer);
        }

        for (int c = 0; c < STAGES - 1; c++) {
            Array<MapNode> currentLayer = mapLayers.get(c);
            Array<MapNode> nextLayer = mapLayers.get(c + 1);

            for (MapNode parent : currentLayer) {
                MapNode closest = null;
                float minDst = Float.MAX_VALUE;

                for (MapNode child : nextLayer) {
                    float dst = Math.abs(child.row - parent.row);
                    if (dst < minDst) {
                        minDst = dst;
                        closest = child;
                    }
                }
                if (closest != null) connect(parent, closest);

                for (MapNode child : nextLayer) {
                    if (child == closest) continue;
                    float dst = Math.abs(child.row - parent.row);
                    if (dst <= 1 && MathUtils.randomBoolean(0.3f)) {
                        connect(parent, child);
                    }
                }
            }

            for (MapNode child : nextLayer) {
                if (child.parents.size == 0) {
                    MapNode closestParent = null;
                    float minDst = Float.MAX_VALUE;
                    for (MapNode p : currentLayer) {
                        float dst = Math.abs(child.row - p.row);
                        if (dst < minDst) {
                            minDst = dst;
                            closestParent = p;
                        }
                    }
                    if (closestParent != null) connect(closestParent, child);
                }
            }
        }
    }

    private void connect(MapNode parent, MapNode child) {
        if (!parent.children.contains(child, true)) {
            parent.children.add(child);
            child.parents.add(parent);
        }
    }

    private NodeType getRandomType(int col) {
        if (col == STAGES - 1) return NodeType.BOSS;
        if (col == 0) return NodeType.MONSTER;
        if (col == STAGES - 2) return NodeType.REST;

        float r = MathUtils.random();
        if (r < 0.60f) return NodeType.MONSTER; // 60% 전투
        if (r < 0.80f) return NodeType.CHEST;   // 20% 상자
        // ★★★ [수정] 휴식(REST) 랜덤 등장 제거 -> 나머지 20%는 상점 ★★★
        return NodeType.SHOP;
    }

    // ★★★ [신규] 선택되지 않은 형제 노드들 비활성화 ★★★
    public void disableSiblings(MapNode selectedNode) {
        Array<MapNode> layer = mapLayers.get(selectedNode.col);
        for (MapNode node : layer) {
            if (node != selectedNode) {
                node.status = NodeStatus.UNREACHABLE; // 또는 LOCKED
            }
        }
    }

    public void completeCurrentNode() {
        if (currentNode != null) {
            currentNode.status = NodeStatus.COMPLETED;
            for (MapNode child : currentNode.children) {
                child.status = NodeStatus.AVAILABLE;
            }
        }
    }
}
