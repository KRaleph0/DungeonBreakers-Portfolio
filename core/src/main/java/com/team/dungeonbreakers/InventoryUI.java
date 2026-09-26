package com.team.dungeonbreakers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;

public class InventoryUI {
    private boolean isVisible = false;
    private TextureRegion equipSlotRegion, invSlotRegion, tooltipBgRegion, emptySlotCover, arrowUp, arrowDown;
    private CharacterManager characterManager;
    private TextureAtlas itemAtlas;
    private BitmapFont font;
    private GlyphLayout layout;
    private final float UI_SCALE = 3.5f;
    private final float TOOLTIP_SCALE = 3.8f;
    private final float V_WIDTH = 1600f;
    private final float V_HEIGHT = 900f;
    private final int MAX_INVENTORY_SLOTS = 9;
    private final int MAX_EQUIPMENT_SLOTS = 8;
    private Array<Item> inventoryItems;
    private Array<Item> equipmentItems;
    private Item draggedItem = null;
    private int dragSourceIndex = -1;
    private boolean isDragFromEquipment = false;
    private Item hoveredItem = null;
    private boolean isHoveredItemEquipment = false;
    private AbstractPlayer player;
    private ShopUI shopUI;
    private final String[] SLOT_TYPES = { "WPN", "HEL", "PNT", "CONSUMABLE", "SUB", "ARM", "BOT", "CONSUMABLE" };

    public InventoryUI(CharacterManager characterManager, ItemManager itemManager, BitmapFont font) {
        this.characterManager = characterManager;
        TextureAtlas slotAtlas = characterManager.getSlotAtlas();
        this.itemAtlas = characterManager.getItemAtlas();

        if (slotAtlas != null) {
            equipSlotRegion = slotAtlas.findRegion("Equipment_slot");
            invSlotRegion = slotAtlas.findRegion("Inventory_slot");
            tooltipBgRegion = slotAtlas.findRegion("item_ui");
            if (tooltipBgRegion == null) tooltipBgRegion = invSlotRegion;
            emptySlotCover = characterManager.getSlotCoverTexture();
        }
        if (this.itemAtlas != null) {
            arrowUp = itemAtlas.findRegion("up");
            arrowDown = itemAtlas.findRegion("down");
        }
        this.inventoryItems = PlayerData.getInstance().inventoryItems;
        this.equipmentItems = PlayerData.getInstance().equipmentItems;
        this.font = font;
        this.layout = new GlyphLayout();
    }

    public void setShopUI(ShopUI shopUI) { this.shopUI = shopUI; }

    public void setPlayer(AbstractPlayer player) {
        this.player = player;
        for (Item item : equipmentItems) { if (item != null) player.updateItemStats(item, true); }
    }

    public boolean addItem(Item newItem) {
        if (newItem.maxStack > 1) {
            for (Item existingItem : inventoryItems) {
                if (existingItem.itemId.equals(newItem.itemId)) {
                    existingItem.maxStack = newItem.maxStack;
                    if (existingItem.count < existingItem.maxStack) { existingItem.count++; return true; }
                }
            }
            int[] quickSlotIndices = {3, 7};
            for (int i : quickSlotIndices) {
                Item equipped = equipmentItems.get(i);
                if (equipped != null && equipped.itemId.equals(newItem.itemId)) {
                    equipped.maxStack = newItem.maxStack;
                    if (equipped.count < equipped.maxStack) { equipped.count++; return true; }
                }
            }
        }
        if (inventoryItems.size < MAX_INVENTORY_SLOTS) { inventoryItems.add(new Item(newItem)); return true; }
        return false;
    }

    public void draw(SpriteBatch batch, Vector3 mousePos) {
        if (!isVisible) return;
        drawInternal(batch, mousePos, true, 0);
    }
    public void drawInventoryOnly(SpriteBatch batch, Vector3 mousePos) {
        drawInternal(batch, mousePos, false, 400f);
    }

    private void drawInternal(SpriteBatch batch, Vector3 mousePos, boolean showEquipment, float xOffset) {
        float equipWidth = equipSlotRegion.getRegionWidth() * UI_SCALE;
        float equipHeight = equipSlotRegion.getRegionHeight() * UI_SCALE;
        float invWidth = invSlotRegion.getRegionWidth() * UI_SCALE;
        float invHeight = invSlotRegion.getRegionHeight() * UI_SCALE;
        float gap = 50f;
        float totalWidth = showEquipment ? (equipWidth + gap + invWidth) : invWidth;
        float startX = (V_WIDTH - totalWidth) / 2f + xOffset;
        float centerY = V_HEIGHT / 2f;

        float equipY = centerY - (equipHeight / 2f);
        float invX = showEquipment ? (startX + equipWidth + gap) : startX;
        float invY = centerY - (invHeight / 2f);

        if (showEquipment && equipSlotRegion != null) batch.draw(equipSlotRegion, startX, equipY, equipWidth, equipHeight);
        if (invSlotRegion != null) batch.draw(invSlotRegion, invX, invY, invWidth, invHeight);

        handleInput(mousePos, startX, equipY, equipHeight, invX, invY, invHeight, showEquipment);

        hoveredItem = null;
        if (showEquipment) {
            for (int i = 0; i < MAX_EQUIPMENT_SLOTS; i++) {
                Item item = equipmentItems.get(i);
                boolean isDraggingThis = (isDragFromEquipment && dragSourceIndex == i && draggedItem != null);
                int col = i % 4; int row = i / 4;
                if (item != null && !isDraggingThis) {
                    drawSlotCover(batch, startX, equipY, equipHeight, row, col);
                    drawItemInSlot(batch, item, startX, equipY, equipHeight, row, col, mousePos, true, i);
                } else { checkSlotHover(startX, equipY, equipHeight, row, col, mousePos, true, i); }
            }
        }
        for (int i = 0; i < MAX_INVENTORY_SLOTS; i++) {
            Item item = (i < inventoryItems.size) ? inventoryItems.get(i) : null;
            boolean isDraggingThis = (!isDragFromEquipment && dragSourceIndex == i && draggedItem != null);
            int col = i % 3; int row = i / 3;
            if (item != null && !isDraggingThis) {
                drawItemInSlot(batch, item, invX, invY, invHeight, row, col, mousePos, false, i);
            } else { checkSlotHover(invX, invY, invHeight, row, col, mousePos, false, i); }
        }

        if (draggedItem != null) {
            TextureRegion region = characterManager.findTextureRegion(draggedItem.textureRegionName);
            if (region != null) {
                float size = 50f;
                batch.draw(region, mousePos.x - size/2, mousePos.y - size/2, size, size);
            }
        }

        if (hoveredItem != null && draggedItem == null && tooltipBgRegion != null) {
            drawTooltip(batch, hoveredItem, mousePos.x, mousePos.y);
        }
    }

    public void drawTooltip(SpriteBatch batch, Item item, float x, float y) {
        float originalScaleX = font.getData().scaleX;
        float originalScaleY = font.getData().scaleY;
        Color originalColor = font.getColor();

        // 내용 높이 계산
        float contentHeight = 150f;
        if (item.attackPower > 0) contentHeight += 24f;
        if (item.defense > 0) contentHeight += 24f;
        if (item.criticalChance > 0) contentHeight += 24f;
        // ★★★ [추가] 치명타 피해 표시 ★★★
        if (item.criticalDamage > 0) contentHeight += 24f;
        if (item.maxHp > 0) contentHeight += 24f;
        if (item.attackSpeed > 0) contentHeight += 24f;
        if (item.cooldown > 0) contentHeight += 24f;
        if (item.dashCooldownReduction > 0) contentHeight += 24f;
        if (item.dashMaxCharges > 0) contentHeight += 24f;
        if (item.description != null && !item.description.isEmpty()) contentHeight += 60f;
        contentHeight += 24f; // 판매가

        float bgW = tooltipBgRegion.getRegionWidth() * TOOLTIP_SCALE;
        float bgH = Math.max(tooltipBgRegion.getRegionHeight() * TOOLTIP_SCALE, contentHeight);

        float tooltipX = x + 20; float tooltipY = y - bgH;
        if (tooltipX + bgW > V_WIDTH) tooltipX = x - bgW - 10;
        if (tooltipY < 0) tooltipY = y + 10;

        if (tooltipBgRegion != null) batch.draw(tooltipBgRegion, tooltipX, tooltipY, bgW, bgH);

        float padding = 25f;
        float iconSize = 64f * 0.9f;
        float iconX = tooltipX + padding + 10f;
        float iconY = tooltipY + bgH - padding - iconSize - 10f;
        TextureRegion icon = characterManager.findTextureRegion(item.textureRegionName);
        if (icon != null) batch.draw(icon, iconX, iconY, iconSize, iconSize);

        float textX = iconX + iconSize + 20f;
        float textY = iconY + iconSize;
        float maxW = bgW - (textX - tooltipX) - 30f;

        font.getData().setScale(0.66f); font.setColor(getGradeColor(item.grade));
        layout.setText(font, item.name, Color.WHITE, maxW, Align.left, true);
        font.draw(batch, item.name, textX, textY, maxW, Align.left, true);

        float nameH = layout.height;
        font.getData().setScale(0.5f); font.setColor(Color.LIGHT_GRAY);
        String typeText = getGradeText(item.grade) + " " + getTypeText(item.itemId);
        font.draw(batch, typeText, textX, textY - nameH - 12f);

        float contentY = iconY - 40f;
        Item equipped = null;

        // ★★★ [수정] 비교 대상 장비 찾기 ★★★
        // 인벤토리에 있는 아이템이거나 상점 아이템인 경우에만 비교
        if (!isHoveredItemEquipment) {
            int slotIdx = findEquipmentSlot(item);
            if (slotIdx != -1) {
                equipped = equipmentItems.get(slotIdx);
            }
        }

        // ★★★ [수정] 비교 수행 여부 (착용 장비가 있으면 true) ★★★
        boolean doCompare = (equipped != null);

        float lineSpace = 24f;
        // 스탯 그리기
        if (item.attackPower > 0) { drawStatLine(batch, "공격력", (float)item.attackPower, (float)(equipped != null ? equipped.attackPower : 0), tooltipX+padding, contentY, "", doCompare); contentY -= lineSpace; }
        if (item.defense > 0) { drawStatLine(batch, "방어력", (float)item.defense, (float)(equipped != null ? equipped.defense : 0), tooltipX+padding, contentY, "", doCompare); contentY -= lineSpace; }
        if (item.criticalChance > 0) { drawStatLine(batch, "치명타 확률", item.criticalChance * 100, (equipped != null ? equipped.criticalChance * 100 : 0), tooltipX+padding, contentY, "%", doCompare); contentY -= lineSpace; }
        // ★★★ [추가] 치명타 피해 표시 ★★★
        if (item.criticalDamage > 0) { drawStatLine(batch, "치명타 피해", item.criticalDamage * 100, (equipped != null ? equipped.criticalDamage * 100 : 0), tooltipX+padding, contentY, "%", doCompare); contentY -= lineSpace; }

        if (item.maxHp > 0) { drawStatLine(batch, "최대 체력", (float)item.maxHp, (float)(equipped != null ? equipped.maxHp : 0), tooltipX+padding, contentY, "", doCompare); contentY -= lineSpace; }
        if (item.attackSpeed > 0) { drawStatLine(batch, "공격 속도", item.attackSpeed * 100, (equipped != null ? equipped.attackSpeed * 100 : 0), tooltipX+padding, contentY, "%", doCompare); contentY -= lineSpace; }
        if (item.cooldown > 0) { drawStatLine(batch, "쿨타임 감소", item.cooldown * 100, 0, tooltipX+padding, contentY, "%", false); contentY -= lineSpace; }

        if (item.dashCooldownReduction > 0) {
            drawStatLine(batch, "대쉬 쿨타임 감소", item.dashCooldownReduction * 100, (equipped != null ? equipped.dashCooldownReduction * 100 : 0), tooltipX+padding, contentY, "%", doCompare);
            contentY -= lineSpace;
        }
        if (item.dashMaxCharges > 0) {
            drawStatLine(batch, "대쉬 횟수 증가", (float)item.dashMaxCharges, (float)(equipped != null ? equipped.dashMaxCharges : 0), tooltipX+padding, contentY, "", doCompare);
            contentY -= lineSpace;
        }

        // 판매가
        int price = calculatePrice(item);
        int sellPrice = price / 2;
        font.setColor(Color.GOLD);
        font.draw(batch, "판매가: " + sellPrice + " G", tooltipX + padding, contentY);
        contentY -= lineSpace;

        font.setColor(Color.WHITE);
        if (item.description != null && !item.description.isEmpty()) {
            contentY -= 10f;
            font.draw(batch, item.description, tooltipX + padding, contentY, bgW - padding*2, Align.left, true);
        }

        font.getData().setScale(originalScaleX, originalScaleY);
        font.setColor(originalColor);
    }

    // ★★★ [수정] 비교 로직 메서드 (doCompare 플래그 사용) ★★★
    private void drawStatLine(SpriteBatch batch, String label, float val, float equipVal, float x, float y, String suffix, boolean doCompare) {
        font.setColor(Color.WHITE);
        String valStr = (val == (int)val) ? String.valueOf((int)val) : String.format("%.1f", val);
        String baseText = label + ": " + valStr + suffix;
        font.draw(batch, baseText, x, y);
        layout.setText(font, baseText);

        // doCompare가 true일 때만(착용 장비가 있을 때만) 비교 수행
        if (doCompare) {
            float diff = val - equipVal;
            // 아주 작은 오차는 무시 (부동소수점 이슈 방지)
            if (Math.abs(diff) > 0.01f) {
                float diffX = x + layout.width + 10f;
                float arrowSize = 12f;
                float arrowY = y - arrowSize + 2f;
                String diffStr = (Math.abs(diff) == (int)Math.abs(diff)) ? String.valueOf((int)Math.abs(diff)) : String.format("%.1f", Math.abs(diff));

                if (diff > 0) {
                    font.setColor(Color.GREEN);
                    if (arrowUp != null) { batch.setColor(0f, 1f, 0f, 1f); batch.draw(arrowUp, diffX, arrowY, arrowSize, arrowSize); batch.setColor(1f, 1f, 1f, 1f); diffX += arrowSize + 2f; }
                    font.draw(batch, " " + diffStr + suffix, diffX, y);
                } else {
                    font.setColor(Color.RED);
                    if (arrowDown != null) { batch.setColor(1f, 0f, 0f, 1f); batch.draw(arrowDown, diffX, arrowY, arrowSize, arrowSize); batch.setColor(1f, 1f, 1f, 1f); diffX += arrowSize + 2f; }
                    font.draw(batch, " " + diffStr + suffix, diffX, y);
                }
            }
        }
    }

    private int calculatePrice(Item item) {
        int basePrice = 100;
        String g = item.grade != null ? item.grade.toUpperCase() : "COMMON";
        if ("UNCOMMON".equals(g)) basePrice = 500;
        else if ("RARE".equals(g)) basePrice = 1500;
        else if ("EPIC".equals(g)) basePrice = 3000;
        else if ("LEGENDARY".equals(g)) basePrice = 5000;
        return basePrice;
    }

    private void drawSlotCover(SpriteBatch batch, float pX, float pY, float pH, int row, int col) { if (emptySlotCover == null) return; float currentGap = 23f * UI_SCALE; float currentOffsetX = 17f * UI_SCALE; float currentOffsetY = 17f * UI_SCALE; float centerX = pX + currentOffsetX + (col * currentGap); float topY = pY + pH; float centerY = topY - currentOffsetY - (row * currentGap); float size = currentGap * 1.4f; batch.draw(emptySlotCover, centerX - size / 2f, centerY - size / 2f, size, size); }
    private void handleInput(Vector3 mousePos, float eqX, float eqY, float eqH, float invX, float invY, float invH, boolean showEquipment) { boolean leftPressed = Gdx.input.isButtonPressed(Input.Buttons.LEFT); boolean rightJustPressed = Gdx.input.isButtonJustPressed(Input.Buttons.RIGHT); boolean leftJustPressed = Gdx.input.isButtonJustPressed(Input.Buttons.LEFT); if (leftJustPressed && draggedItem == null) { if (showEquipment) { int eqSlot = getHoveredSlotIndex(eqX, eqY, eqH, mousePos, true); if (eqSlot != -1 && equipmentItems.get(eqSlot) != null) { draggedItem = equipmentItems.get(eqSlot); dragSourceIndex = eqSlot; isDragFromEquipment = true; return; } } int invSlot = getHoveredSlotIndex(invX, invY, invH, mousePos, false); if (invSlot != -1 && invSlot < inventoryItems.size) { draggedItem = inventoryItems.get(invSlot); dragSourceIndex = invSlot; isDragFromEquipment = false; } } if (!leftPressed && draggedItem != null) { int targetEqIdx = showEquipment ? getHoveredSlotIndex(eqX, eqY, eqH, mousePos, true) : -1; int targetInvIdx = getHoveredSlotIndex(invX, invY, invH, mousePos, false); if (targetEqIdx != -1) tryEquipItem(targetEqIdx); else if (targetInvIdx != -1) handleInventoryDrop(targetInvIdx); draggedItem = null; dragSourceIndex = -1; } if (rightJustPressed && draggedItem == null) { if (!showEquipment) { int invSlot = getHoveredSlotIndex(invX, invY, invH, mousePos, false); if (invSlot != -1 && invSlot < inventoryItems.size) { Item item = inventoryItems.get(invSlot); if (shopUI != null) { shopUI.sellItem(item); return; } } } if (showEquipment) { int eqSlot = getHoveredSlotIndex(eqX, eqY, eqH, mousePos, true); if (eqSlot != -1 && equipmentItems.get(eqSlot) != null) { unequipItem(eqSlot); return; } int invSlot = getHoveredSlotIndex(invX, invY, invH, mousePos, false); if (invSlot != -1 && invSlot < inventoryItems.size) { Item item = inventoryItems.get(invSlot); int targetSlot = findEquipmentSlot(item); if (targetSlot != -1) { draggedItem = item; dragSourceIndex = invSlot; isDragFromEquipment = false; tryEquipItem(targetSlot); draggedItem = null; } } } } }
    private void tryEquipItem(int targetSlotIdx) { String reqType = SLOT_TYPES[targetSlotIdx]; boolean isMatch = false; if (reqType.equals("CONSUMABLE")) { if (draggedItem.itemId.startsWith("POT") || draggedItem.itemId.startsWith("CON")) isMatch = true; } else { if (draggedItem.itemId.startsWith(reqType)) isMatch = true; } if (!isMatch) return; Item prevEquip = equipmentItems.get(targetSlotIdx); if (isDragFromEquipment) { equipmentItems.set(dragSourceIndex, prevEquip); equipmentItems.set(targetSlotIdx, draggedItem); } else { equipmentItems.set(targetSlotIdx, draggedItem); if (player != null) player.updateItemStats(draggedItem, true); if (prevEquip != null) { inventoryItems.set(dragSourceIndex, prevEquip); if (player != null) player.updateItemStats(prevEquip, false); } else { inventoryItems.removeIndex(dragSourceIndex); } } }
    private void handleInventoryDrop(int targetIdx) { if (!isDragFromEquipment) { if (targetIdx >= inventoryItems.size) { inventoryItems.removeIndex(dragSourceIndex); inventoryItems.add(draggedItem); } else { Item temp = inventoryItems.get(targetIdx); inventoryItems.set(targetIdx, draggedItem); inventoryItems.set(dragSourceIndex, temp); } } else { unequipItemTo(targetIdx); } }
    private void unequipItem(int equipSlotIdx) { if (inventoryItems.size < MAX_INVENTORY_SLOTS) { Item item = equipmentItems.get(equipSlotIdx); if (item != null) { equipmentItems.set(equipSlotIdx, null); inventoryItems.add(item); if (player != null) player.updateItemStats(item, false); } } }
    private void unequipItemTo(int invIdx) { if (invIdx < inventoryItems.size) { Item invItem = inventoryItems.get(invIdx); String reqType = SLOT_TYPES[dragSourceIndex]; boolean isMatch = false; if (reqType.equals("CONSUMABLE")) { if (invItem.itemId.startsWith("POT") || invItem.itemId.startsWith("CON")) isMatch = true; } else { if (invItem.itemId.startsWith(reqType)) isMatch = true; } if (isMatch) { inventoryItems.set(invIdx, draggedItem); equipmentItems.set(dragSourceIndex, invItem); if (player != null) { player.updateItemStats(draggedItem, false); player.updateItemStats(invItem, true); } } } else { equipmentItems.set(dragSourceIndex, null); inventoryItems.add(draggedItem); if (player != null) player.updateItemStats(draggedItem, false); } }
    private int findEquipmentSlot(Item item) { if (item.itemId.startsWith("POT") || item.itemId.startsWith("CON")) { if (equipmentItems.get(3) == null) return 3; if (equipmentItems.get(7) == null) return 7; return 3; } for(int i=0; i<SLOT_TYPES.length; i++) { if (item.itemId.startsWith(SLOT_TYPES[i])) return i; } return -1; }
    private int getHoveredSlotIndex(float pX, float pY, float pH, Vector3 mPos, boolean isEquip) { int cols = isEquip ? 4 : 3; int rows = isEquip ? 2 : 3; float scaleGap = 23f * UI_SCALE; float scaleOffX = 17f * UI_SCALE; float scaleOffY = 17f * UI_SCALE; float hitSize = scaleGap * 0.9f; for (int r = 0; r < rows; r++) { for (int c = 0; c < cols; c++) { float cx = pX + scaleOffX + (c * scaleGap); float cy = (pY + pH) - scaleOffY - (r * scaleGap); if (mPos.x >= cx - hitSize/2 && mPos.x <= cx + hitSize/2 && mPos.y >= cy - hitSize/2 && mPos.y <= cy + hitSize/2) { return r * cols + c; } } } return -1; }
    private void checkSlotHover(float pX, float pY, float pH, int row, int col, Vector3 mPos, boolean isEquip, int index) { float currentGap = 23f * UI_SCALE; float currentOffsetX = 17f * UI_SCALE; float currentOffsetY = 17f * UI_SCALE; float centerX = pX + currentOffsetX + (col * currentGap); float topY = pY + pH; float centerY = topY - currentOffsetY - (row * currentGap); float iconSize = currentGap * 0.65f; if (draggedItem == null) { float half = iconSize / 2f; if (mPos.x >= centerX - half && mPos.x <= centerX + half && mPos.y >= centerY - half && mPos.y <= centerY + half) { Item item = isEquip ? equipmentItems.get(index) : (index < inventoryItems.size ? inventoryItems.get(index) : null); if (item != null) { hoveredItem = item; isHoveredItemEquipment = isEquip; } } } }
    private void drawItemInSlot(SpriteBatch batch, Item item, float pX, float pY, float pH, int row, int col, Vector3 mPos, boolean isEquip, int index) { float currentGap = 23f * UI_SCALE; float currentOffsetX = 17f * UI_SCALE; float currentOffsetY = 17f * UI_SCALE; float centerX = pX + currentOffsetX + (col * currentGap); float topY = pY + pH; float centerY = topY - currentOffsetY - (row * currentGap); float iconSize = currentGap * 0.65f; if (item != null && item.textureRegionName != null) { TextureRegion region = characterManager.findTextureRegion(item.textureRegionName); if (region != null) { batch.draw(region, centerX - iconSize / 2f, centerY - iconSize / 2f, iconSize, iconSize); } if (item.maxStack > 1) { drawCount(batch, item.count, centerX, centerY, iconSize); } } if (draggedItem == null) { float half = iconSize / 2f; if (mPos.x >= centerX - half && mPos.x <= centerX + half && mPos.y >= centerY - half && mPos.y <= centerY + half) { if (item != null) { hoveredItem = item; isHoveredItemEquipment = isEquip; } } } }
    private void drawCount(SpriteBatch batch, int count, float cx, float cy, float size) { String text = String.valueOf(count); float sX = font.getData().scaleX; float sY = font.getData().scaleY; font.getData().setScale(0.5f); font.setColor(Color.WHITE); layout.setText(font, text); font.draw(batch, text, cx + size/2 - layout.width, cy - size/2 + layout.height + 2f); font.getData().setScale(sX, sY); }
    private String getGradeText(String grade) { if (grade == null) return ""; switch (grade.toUpperCase()) { case "COMMON": return "일반"; case "UNCOMMON": return "고급"; case "RARE": return "희귀"; case "EPIC": return "영웅"; case "LEGENDARY": return "전설"; default: return grade; } }
    private String getTypeText(String itemId) { if (itemId.startsWith("WPN")) return "무기"; if (itemId.startsWith("HEL")) return "투구"; if (itemId.startsWith("ARM")) return "갑옷"; if (itemId.startsWith("PNT")) return "하의"; if (itemId.startsWith("BOT")) return "신발"; if (itemId.startsWith("SUB")) return "보조무기"; if (itemId.startsWith("POT") || itemId.startsWith("CON")) return "소모품"; return "장비"; }
    private Color getGradeColor(String grade) { if (grade == null) return Color.WHITE; switch (grade.toUpperCase()) { case "LEGENDARY": return Color.ORANGE; case "EPIC": return Color.PURPLE; case "RARE": return new Color(0.3f, 0.3f, 1f, 1f); case "UNCOMMON": return Color.GREEN; default: return Color.WHITE; } }
    public void toggle() { isVisible = !isVisible; }
    public void close() { isVisible = false; }
    public boolean isVisible() { return isVisible; }
    public void dispose() { }
}
