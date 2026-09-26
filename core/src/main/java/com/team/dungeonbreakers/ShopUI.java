package com.team.dungeonbreakers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

public class ShopUI {
    // ... (기존 필드들)
    private boolean isVisible = false;
    private Texture backTexture;
    private TextureRegion slotRegion;
    private Texture soldOutTexture;
    private TextureAtlas itemAtlas;
    private BitmapFont font;
    private GlyphLayout layout;
    private final float SCREEN_WIDTH = 1600f;
    private final float SCREEN_HEIGHT = 900f;
    private Array<Item> shopItems;
    private InventoryUI inventoryUI;

    public ShopUI(CharacterManager charManager, ItemManager itemManager, InventoryUI inventoryUI, BitmapFont font) {
        this.inventoryUI = inventoryUI;
        this.font = font;
        this.layout = new GlyphLayout();
        this.itemAtlas = charManager.getItemAtlas();
        this.backTexture = charManager.getUIBackTexture();
        this.slotRegion = charManager.getSlotCoverTexture();
        this.soldOutTexture = charManager.getSoldOutTexture();
        this.shopItems = new Array<>();
        refreshShop(itemManager);
    }

    // ... (refreshShop, calculatePrice 동일) ...
    public void refreshShop(ItemManager itemManager) { shopItems.clear(); PlayerData pd = PlayerData.getInstance(); int totalSlots = pd.shopSlotCount; boolean isHighQuality = pd.isShopPremium; int consumableSlots = 2; int equipmentSlots = totalSlots - consumableSlots; for (int i = 0; i < totalSlots; i++) { String grade = "COMMON"; float r = MathUtils.random(); if (isHighQuality) { if (r < 0.05f) grade = "LEGENDARY"; else if (r < 0.25f) grade = "EPIC"; else if (r < 0.60f) grade = "RARE"; else if (r < 0.90f) grade = "UNCOMMON"; } else { if (r < 0.02f) grade = "LEGENDARY"; else if (r < 0.10f) grade = "EPIC"; else if (r < 0.30f) grade = "RARE"; else if (r < 0.60f) grade = "UNCOMMON"; } Item item; if (i >= equipmentSlots) { item = itemManager.getDropConsumable(grade); if (item != null) item.count = 3; } else { item = itemManager.getDropItem(grade, null); if (item != null) item.count = 1; } if (item != null) { item.price = calculatePrice(item); shopItems.add(item); } } }
    private int calculatePrice(Item item) { int basePrice = 100; String g = item.grade != null ? item.grade.toUpperCase() : "COMMON"; if ("UNCOMMON".equals(g)) basePrice = 500; else if ("RARE".equals(g)) basePrice = 1500; else if ("EPIC".equals(g)) basePrice = 3000; else if ("LEGENDARY".equals(g)) basePrice = 5000; return basePrice; }

    // ★★★ [수정] 툴팁 호출 및 렌더링 분리 ★★★
    public void draw(SpriteBatch batch, Vector3 mousePos) {
        if (!isVisible) return;
        float bgWidth = 600f; float bgHeight = 400f;
        float startX = (SCREEN_WIDTH / 4f) - (bgWidth / 2f); float startY = (SCREEN_HEIGHT - bgHeight) / 2f;

        if (backTexture != null) batch.draw(backTexture, startX, startY, bgWidth, bgHeight);
        else { batch.setColor(0.2f, 0.2f, 0.2f, 0.9f); if (slotRegion != null) batch.draw(slotRegion, startX, startY, bgWidth, bgHeight); batch.setColor(1, 1, 1, 1); }

        font.getData().setScale(0.8f); font.setColor(Color.GOLD); layout.setText(font, "상 점"); font.draw(batch, "상 점", startX + bgWidth / 2 - layout.width / 2, startY + bgHeight - 20);

        float slotSize = 60f; float gap = 20f;
        float initialX = startX + 50f;
        float currentX = initialX;
        float currentY = startY + bgHeight - 100f;

        // 1. 아이템 슬롯
        for (int i = 0; i < shopItems.size; i++) {
            Item item = shopItems.get(i);
            if (slotRegion != null) batch.draw(slotRegion, currentX, currentY, slotSize, slotSize);
            if (itemAtlas != null) {
                TextureRegion icon = itemAtlas.findRegion(item.textureRegionName);
                if (icon != null) batch.draw(icon, currentX + 5, currentY + 5, slotSize - 10, slotSize - 10);
            }
            if (item.count <= 0) {
                if (soldOutTexture != null) batch.draw(soldOutTexture, currentX, currentY, slotSize, slotSize);
                else { font.setColor(Color.RED); font.draw(batch, "SOLD OUT", currentX, currentY + slotSize/2); }
            } else {
                font.getData().setScale(0.4f); font.setColor(Color.YELLOW);
                String info = item.price + " G"; if (item.maxStack > 1) info += " (" + item.count + ")";
                layout.setText(font, info); font.draw(batch, info, currentX + (slotSize - layout.width) / 2, currentY - 10);
            }
            currentX += slotSize + gap;
            if ((i + 1) % 4 == 0) { currentX = initialX; currentY -= (slotSize + gap + 30f); }
        }

        // 2. 툴팁 (맨 위)
        currentX = initialX;
        currentY = startY + bgHeight - 100f;
        for (int i = 0; i < shopItems.size; i++) {
            Item item = shopItems.get(i);
            if (item.count > 0) {
                if (mousePos.x >= currentX && mousePos.x <= currentX + slotSize &&
                    mousePos.y >= currentY && mousePos.y <= currentY + slotSize) {
                    if (inventoryUI != null) inventoryUI.drawTooltip(batch, item, mousePos.x, mousePos.y);
                    if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) buyItem(item);
                }
            }
            currentX += slotSize + gap;
            if ((i + 1) % 4 == 0) { currentX = initialX; currentY -= (slotSize + gap + 30f); }
        }
        font.getData().setScale(0.6f); font.setColor(Color.WHITE); String myGold = "보유 골드: " + PlayerData.getInstance().gold + " G"; layout.setText(font, myGold); font.draw(batch, myGold, startX + 30, startY + 40);
    }
    // ... (buyItem, sellItem, toggle, close 등 동일) ...
    private void buyItem(Item item) { if (item.count <= 0) return; PlayerData playerData = PlayerData.getInstance(); if (playerData.gold >= item.price) { Item boughtItem = new Item(item); boughtItem.count = 1; if (inventoryUI.addItem(boughtItem)) { playerData.gold -= item.price; item.count--; System.out.println("구매 성공: " + item.name); } else { System.out.println("인벤토리가 가득 찼습니다!"); } } else { System.out.println("골드가 부족합니다!"); } }
    public void sellItem(Item item) { if (item == null) return; int sellPrice = calculatePrice(item) / 2; PlayerData.getInstance().addGold(sellPrice); item.count--; if (item.count <= 0) { PlayerData.getInstance().inventoryItems.removeValue(item, true); } System.out.println("Sold: " + item.name + " for " + sellPrice + " G"); }
    public void toggle() { isVisible = !isVisible; }
    public void close() { isVisible = false; }
    public boolean isVisible() { return isVisible; }
}
