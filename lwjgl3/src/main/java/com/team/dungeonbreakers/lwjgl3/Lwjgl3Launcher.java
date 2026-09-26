// Lwjgl3Launcher.java

package com.team.dungeonbreakers.lwjgl3;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.team.dungeonbreakers.DungeonBreakersGame;

/** Launches the desktop (LWJGL3) application. */
public class Lwjgl3Launcher {
    public static void main(String[] args) {
        createApplication();
    }

    private static Lwjgl3Application createApplication() {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("DungeonBreakers");

        // ★★★ 이 부분을 추가하거나 수정하세요 ★★★
        // 창 모드의 해상도를 1600 x 900 으로 설정합니다.
        config.setWindowedMode(1600, 900);
        // ★★★★★★★★★★★★★★★★★★★★★★★★★

        config.setForegroundFPS(60);
        return new Lwjgl3Application(new DungeonBreakersGame(), config);
    }
}
