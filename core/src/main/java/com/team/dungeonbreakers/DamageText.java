package com.team.dungeonbreakers;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class DamageText {
    private String text;
    private float x, y;
    private float timer;
    private float maxTime = 1.0f; // 텍스트가 유지되는 시간 (초)
    private float velocityY = 50f; // 위로 떠오르는 속도

    public DamageText(String text, float x, float y) {
        this.text = text;
        this.x = x;
        this.y = y;
        this.timer = 0f;
    }

    public void update(float deltaTime) {
        // 위로 천천히 올라감
        y += velocityY * deltaTime;

        // 시간 증가
        timer += deltaTime;
    }

    public boolean isFinished() {
        // 수명이 다했는지 확인
        return timer >= maxTime;
    }

    public void draw(SpriteBatch batch, BitmapFont font) {
        // 시간이 지날수록 투명해지게 설정 (Fade Out)
        float alpha = 1.0f - (timer / maxTime);
        if (alpha < 0) alpha = 0;

        // 기존 폰트 색상을 가져와서 알파값만 변경
        Color oldColor = font.getColor();
        font.setColor(oldColor.r, oldColor.g, oldColor.b, alpha);

        font.draw(batch, text, x, y);

        // 다음 텍스트를 위해 색상(투명도) 원상복구
        font.setColor(oldColor.r, oldColor.g, oldColor.b, 1.0f);
    }
}
