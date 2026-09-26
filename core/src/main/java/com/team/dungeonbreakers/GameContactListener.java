package com.team.dungeonbreakers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Timer;

public class GameContactListener implements ContactListener {
    private AbstractPlayer player;
    private DamageTextManager damageTextManager;
    private GameScreen gameScreen;
    private InventoryUI inventoryUI;

    // ★★★ [수정] 누락된 tmpVec 변수 선언 추가 ★★★
    private final Vector2 tmpVec = new Vector2();

    public void setPlayer(AbstractPlayer player) {
        this.player = player;
    }

    public void setDamageTextManager(DamageTextManager manager) {
        this.damageTextManager = manager;
    }

    public void setGameScreen(GameScreen screen) {
        this.gameScreen = screen;
    }

    public void setInventoryUI(InventoryUI inventoryUI) {
        this.inventoryUI = inventoryUI;
    }

    private boolean isPlayerFoot(Fixture fixture) { return fixture != null && "foot".equals(fixture.getUserData()); }

    private boolean isGround(Fixture fixture) {
        if (fixture == null) return false;
        Object userData = fixture.getUserData();
        return "ground".equals(userData) || "wall".equals(userData) || "platform".equals(userData);
    }

    @Override
    public void beginContact(Contact contact) {
        Fixture fa = contact.getFixtureA();
        Fixture fb = contact.getFixtureB();

        if ((isPlayerFoot(fa) && isGround(fb)) || (isPlayerFoot(fb) && isGround(fa))) {
            if (player != null) player.isGrounded = true;
        }

        handleTriggerCollision(fa, fb);

        handleProjectileEnemyCollision(contact);
        handlePlayerEnemyAttackCollision(contact);
        handleProjectilePlayerCollision(contact);
        handlePlayerCoinCollision(contact);
        handlePlayerItemCollision(contact);

        // 화살 vs 지형 충돌 (화살 삭제)
        handleProjectileGroundCollision(contact);
    }

    private void handleProjectileGroundCollision(Contact contact) {
        Fixture fa = contact.getFixtureA();
        Fixture fb = contact.getFixtureB();

        Fixture proj = null;
        if (isProjectile(fa) && isGround(fb)) proj = fa;
        else if (isGround(fa) && isProjectile(fb)) proj = fb;

        if (proj != null) {
            Object userData = proj.getBody().getUserData();
            if (userData instanceof Projectile) {
                ((Projectile) userData).destroy();
            }
        }
    }

    private boolean isProjectile(Fixture f) {
        if (f == null || f.getUserData() == null) return false;
        String data = f.getUserData().toString();
        return "projectile".equals(data) || "enemyProjectile".equals(data);
    }

    private void handleTriggerCollision(Fixture fa, Fixture fb) {
        if (isUserData(fa, "playerBody") && fb.getUserData() instanceof SpawnTrigger) {
            ((SpawnTrigger)fb.getUserData()).trigger();
        }
        else if (isUserData(fb, "playerBody") && fa.getUserData() instanceof SpawnTrigger) {
            ((SpawnTrigger)fa.getUserData()).trigger();
        }
    }

    @Override
    public void endContact(Contact contact) {
        Fixture fa = contact.getFixtureA();
        Fixture fb = contact.getFixtureB();

        if ((isPlayerFoot(fa) && isGround(fb)) || (isPlayerFoot(fb) && isGround(fa))) {
            if (player != null) player.isGrounded = false;
        }
    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {
        Fixture fa = contact.getFixtureA();
        Fixture fb = contact.getFixtureB();

        Fixture charFix = null;
        Fixture platFix = null;

        if (isCharacter(fa) && isPlatform(fb)) {
            charFix = fa;
            platFix = fb;
        } else if (isPlatform(fa) && isCharacter(fb)) {
            platFix = fa;
            charFix = fb;
        }

        if (charFix != null && platFix != null) {
            Body charBody = charFix.getBody();

            if (charBody.getLinearVelocity().y > 0) {
                contact.setEnabled(false);
                return;
            }

            if (isPlayerBodyOrFoot(charFix)) {
                if (Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
                    contact.setEnabled(false);
                    return;
                }
            }

            float platformTopY = getShapeMaxY(platFix);
            float charBottomY = getShapeMinY(charFix);

            if (charBottomY < platformTopY - 0.1f) {
                contact.setEnabled(false);
            }
        }
    }

    private boolean isCharacter(Fixture f) {
        if (f == null || f.getUserData() == null) return false;
        String data = f.getUserData().toString();
        return data.equals("playerBody") || data.equals("foot") || data.equals("enemy");
    }

    private boolean isPlayerBodyOrFoot(Fixture f) {
        return "playerBody".equals(f.getUserData()) || "foot".equals(f.getUserData());
    }

    private boolean isPlatform(Fixture f) {
        return "platform".equals(f.getUserData());
    }

    private float getShapeMaxY(Fixture f) {
        float maxY = -Float.MAX_VALUE;
        Shape shape = f.getShape();
        if (shape instanceof PolygonShape) {
            PolygonShape poly = (PolygonShape) shape;
            for(int i=0; i<poly.getVertexCount(); i++) {
                // ★★★ [수정] tmpVec 사용 ★★★
                poly.getVertex(i, tmpVec);
                Vector2 worldPt = f.getBody().getWorldPoint(tmpVec);
                if(worldPt.y > maxY) maxY = worldPt.y;
            }
        } else {
            maxY = f.getBody().getPosition().y;
        }
        return maxY;
    }

    private float getShapeMinY(Fixture f) {
        if (isPlayerBodyOrFoot(f) && player != null) {
            return player.getFeetY();
        }

        float minY = Float.MAX_VALUE;
        Shape shape = f.getShape();
        if (shape instanceof PolygonShape) {
            PolygonShape poly = (PolygonShape) shape;
            for(int i=0; i<poly.getVertexCount(); i++) {
                // ★★★ [수정] tmpVec 사용 ★★★
                poly.getVertex(i, tmpVec);
                Vector2 worldPt = f.getBody().getWorldPoint(tmpVec);
                if(worldPt.y < minY) minY = worldPt.y;
            }
        } else {
            minY = f.getBody().getPosition().y;
        }
        return minY;
    }

    @Override public void postSolve(Contact contact, ContactImpulse impulse) {}

    private void handleProjectileEnemyCollision(Contact contact) {
        Fixture fixtureA = contact.getFixtureA();
        Fixture fixtureB = contact.getFixtureB();
        Fixture projectileFixture = null;
        Fixture enemyFixture = null;

        if (isUserData(fixtureA, "projectile") && isUserData(fixtureB, "enemy")) {
            projectileFixture = fixtureA; enemyFixture = fixtureB;
        } else if (isUserData(fixtureA, "enemy") && isUserData(fixtureB, "projectile")) {
            projectileFixture = fixtureB; enemyFixture = fixtureA;
        }

        if (projectileFixture != null && enemyFixture != null) {
            Projectile projectile = (Projectile) projectileFixture.getBody().getUserData();
            AbstractEnemy enemy = (AbstractEnemy) enemyFixture.getBody().getUserData();

            if (projectile != null && enemy != null && projectile.isActive() && enemy.isAlive()) {
                boolean hit = projectile.onHit(enemy);

                if (hit && damageTextManager != null) {
                    damageTextManager.createDamageText(projectile.damage, enemy.x, enemy.y + enemy.height);
                }
            }
        }
    }

    private void handlePlayerEnemyAttackCollision(Contact contact) {
        Fixture fixtureA = contact.getFixtureA();
        Fixture fixtureB = contact.getFixtureB();
        if (isUserData(fixtureA, "enemyAttack") && isUserData(fixtureB, "playerBody")) {
            hitPlayer((AbstractPlayer) fixtureB.getBody().getUserData());
        } else if (isUserData(fixtureB, "enemyAttack") && isUserData(fixtureA, "playerBody")) {
            hitPlayer((AbstractPlayer) fixtureA.getBody().getUserData());
        }
    }

    private void hitPlayer(AbstractPlayer p) {
        if (p != null && p.canBeHit) {
            p.takeDamage(15);
            p.canBeHit = false;
            Timer.schedule(new Timer.Task() { @Override public void run() { if (p != null) p.canBeHit = true; } }, 0.3f);
            System.out.println("Player hit by enemy attack!");
        }
    }

    private void handleProjectilePlayerCollision(Contact contact) {
        Fixture fixtureA = contact.getFixtureA();
        Fixture fixtureB = contact.getFixtureB();
        Fixture projectileFixture = null;
        Fixture playerFixture = null;
        if (isUserData(fixtureA, "enemyProjectile") && isUserData(fixtureB, "playerBody")) {
            projectileFixture = fixtureA; playerFixture = fixtureB;
        } else if (isUserData(fixtureB, "enemyProjectile") && isUserData(fixtureA, "playerBody")) {
            projectileFixture = fixtureB; playerFixture = fixtureA;
        }
        if (projectileFixture != null && playerFixture != null) {
            Projectile projectile = (Projectile) projectileFixture.getBody().getUserData();
            AbstractPlayer playerHit = (AbstractPlayer) playerFixture.getBody().getUserData();
            if (playerHit != null && playerHit.isDashing) return;
            if (projectile != null && projectile.isActive() && playerHit != null && playerHit.canBeHit) {
                playerHit.takeDamage(10);
                playerHit.canBeHit = false;
                Timer.schedule(new Timer.Task() { @Override public void run() { playerHit.canBeHit = true; } }, 0.5f);
                projectile.destroy();
                System.out.println("Player hit by arrow!");
            }
        }
    }

    private void handlePlayerCoinCollision(Contact contact) {
        Fixture fa = contact.getFixtureA();
        Fixture fb = contact.getFixtureB();
        Fixture coinFix = null;
        if (isUserData(fa, "playerBody") && isUserData(fb, "coin")) coinFix = fb;
        else if (isUserData(fb, "playerBody") && isUserData(fa, "coin")) coinFix = fa;
        if (coinFix != null) {
            Coin coin = (Coin) coinFix.getBody().getUserData();
            if (coin != null && coin.isActive()) {
                PlayerData.getInstance().addGold(coin.value);
                coin.collect();
            }
        }
    }

    private void handlePlayerItemCollision(Contact contact) {
        Fixture fa = contact.getFixtureA();
        Fixture fb = contact.getFixtureB();
        Fixture itemFix = null;

        if (isUserData(fa, "playerBody") && fb.getUserData() instanceof DroppedItem) itemFix = fb;
        else if (isUserData(fb, "playerBody") && fa.getUserData() instanceof DroppedItem) itemFix = fa;

        if (itemFix != null) {
            DroppedItem droppedItem = (DroppedItem) itemFix.getBody().getUserData();
            if (droppedItem != null && droppedItem.isActive()) {
                if (inventoryUI != null) {
                    boolean success = inventoryUI.addItem(droppedItem.item);
                    if (success) {
                        droppedItem.collect();
                        System.out.println("Acquired item: " + droppedItem.item.name);
                    } else {
                        System.out.println("Inventory full!");
                    }
                }
            }
        }
    }

    private boolean isUserData(Fixture f, String data) {
        return f != null && data.equals(f.getUserData());
    }
}
