package com.team.dungeonbreakers;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class MapNode {
    public NodeType type;
    public NodeStatus status;
    public int col; // 가로 단계 (0 ~ 14)
    public int row; // 세로 위치 인덱스

    public Vector2 uiPosition = new Vector2(); // 화면에 그려질 좌표

    public Array<MapNode> parents = new Array<>(); // 이전 단계 노드들
    public Array<MapNode> children = new Array<>(); // 다음 단계 노드들

    public MapNode(NodeType type, int col, int row) {
        this.type = type;
        this.col = col;
        this.row = row;
        this.status = NodeStatus.LOCKED;
    }
}
