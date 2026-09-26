package com.team.dungeonbreakers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.math.MathUtils;

import java.util.HashMap;
import java.util.Map;

public class ItemManager {
    public Array<Item> allItems = new Array<>();
    private Map<String, Item> itemMap = new HashMap<>();
    private Map<String, Array<Item>> itemsByGrade = new HashMap<>();

    public ItemManager() {
        loadAllItems();
    }

    private void loadAllItems() {
        String[] jsonFiles = {
            "item/armors.json", "item/boots.json", "item/helmets.json",
            "item/knignt_weapon.json", "item/Archer_weapon.json",
            "item/shields.json", "item/quivers.json", "item/consumables_all.json"
        };

        JsonReader jsonReader = new JsonReader();

        for (String fileName : jsonFiles) {
            try {
                JsonValue root = jsonReader.parse(Gdx.files.internal(fileName));
                JsonValue items = root.get("items");

                for (JsonValue itemVal : items) {
                    Item item = new Item();
                    item.itemId = itemVal.getString("itemId");
                    item.name = itemVal.getString("name");
                    item.grade = itemVal.getString("grade", "Common");
                    item.description = itemVal.getString("description", "");

                    item.defense = itemVal.getInt("defense", 0);
                    item.attackPower = itemVal.getInt("attackPower", 0);
                    item.criticalDamage = itemVal.getFloat("criticalDamage", 0);
                    item.maxHp = itemVal.getInt("maxHp", 0);
                    item.attackSpeed = itemVal.getFloat("attackSpeed", 0);
                    item.cooldown = itemVal.getFloat("cooldown", 0);
                    item.criticalChance = itemVal.getFloat("criticalChance", 0);
                    item.dashCooldownReduction = itemVal.getFloat("dashCooldownReduction", 0);
                    item.dashMaxCharges = itemVal.getInt("dashMaxCharges", 0);

                    item.effectType = itemVal.getString("effectType", null);
                    item.value = itemVal.getFloat("value", 0);
                    item.duration = itemVal.getFloat("duration", 0);

                    item.textureRegionName = findTextureRegionName(item.itemId, item.grade);

                    if (item.itemId.startsWith("POT") || item.itemId.startsWith("CON")) {
                        item.maxStack = 5;
                    } else {
                        item.maxStack = 1;
                    }
                    item.count = 1;

                    allItems.add(item);
                    itemMap.put(item.itemId, item);

                    String gradeKey = item.grade.toUpperCase();
                    if (!itemsByGrade.containsKey(gradeKey)) {
                        itemsByGrade.put(gradeKey, new Array<>());
                    }
                    itemsByGrade.get(gradeKey).add(item);
                }
            } catch (Exception e) {
                Gdx.app.error("ItemManager", "Error loading " + fileName, e);
            }
        }
        Gdx.app.log("ItemManager", "Loaded " + allItems.size + " items.");
    }

    // ★★★ [수정] 장비 아이템만 반환 (소모품 제외) ★★★
    public Item getDropItem(String grade, CharacterType playerClass) {
        String key = grade.toUpperCase();
        if (!itemsByGrade.containsKey(key)) return null;

        Array<Item> candidates = itemsByGrade.get(key);
        if (candidates.size == 0) return null;

        Array<Item> filtered = new Array<>();
        for (Item item : candidates) {
            if (isItemUsableByClass(item, playerClass)) {
                filtered.add(item);
            }
        }

        if (filtered.size == 0) return null;
        return new Item(filtered.get(MathUtils.random(0, filtered.size - 1)));
    }

    // ★★★ [신규] 소모품 아이템만 반환 (등급별) ★★★
    public Item getDropConsumable(String grade) {
        String key = grade.toUpperCase();

        // 해당 등급에 아이템이 없으면 Common으로 폴백
        if (!itemsByGrade.containsKey(key) || itemsByGrade.get(key).size == 0) {
            key = "COMMON";
        }

        if (!itemsByGrade.containsKey(key)) return null;

        Array<Item> candidates = itemsByGrade.get(key);
        Array<Item> consumables = new Array<>();

        for (Item item : candidates) {
            if (item.itemId.startsWith("POT") || item.itemId.startsWith("CON")) {
                consumables.add(item);
            }
        }

        // 해당 등급에 소모품이 없으면 Common에서 찾기
        if (consumables.size == 0 && !key.equals("COMMON")) {
            return getDropConsumable("COMMON");
        }

        if (consumables.size == 0) return null;
        return new Item(consumables.get(MathUtils.random(0, consumables.size - 1)));
    }

    // ★★★ [수정] 직업별 장비 필터링 (소모품 제외) ★★★
    private boolean isItemUsableByClass(Item item, CharacterType playerClass) {
        String id = item.itemId;

        // 소모품은 여기서 제외 (별도 메서드로 획득)
        if (id.startsWith("POT") || id.startsWith("CON")) {
            return false;
        }

        // 공용 방어구
        if (id.startsWith("ARM") || id.startsWith("HEL") || id.startsWith("PNT") || id.startsWith("BOT")) {
            return true;
        }

        // 전사: 검, 방패
        if (playerClass == CharacterType.KNIGHT) {
            if (id.startsWith("WPN_WC") || id.startsWith("WPN_WP") || id.startsWith("SUB_SH")) return true;
        }

        // 궁수: 활, 화살통
        if (playerClass == CharacterType.ARCHER) {
            if (id.startsWith("WPN_AS") || id.startsWith("WPN_AB") || id.startsWith("SUB_QV")) return true;
        }

        return false;
    }

    private String findTextureRegionName(String id, String grade) {
        if (id.startsWith("POT_HP_S")) return "POT_HP_S";
        if (id.startsWith("POT_HP_M")) return "POT_HP_M";
        if (id.startsWith("POT_HP_L")) return "POT_HP_L";
        if (id.startsWith("CON_HOT_BANDAGE")) return "bread";
        if (id.startsWith("CON_HOT_TROLL")) return "toast";
        if (id.startsWith("CON_BUFF_ATK")) return "CON_BUFF_ATK";
        if (id.startsWith("CON_BUFF_DEF")) return "CON_BUFF_DEF";
        if (id.startsWith("CON_BUFF_CRIT")) return "CON_BUFF_CRIT";
        if (id.startsWith("CON_UTIL_DASH")) return "CON_UTIL_DASH";

        String prefix = "";
        if (grade.equalsIgnoreCase("Common")) prefix = "common";
        else if (grade.equalsIgnoreCase("Uncommon")) prefix = "Uncommon";
        else if (grade.equalsIgnoreCase("Rare")) prefix = "Rare";
        else if (grade.equalsIgnoreCase("Epic")) prefix = "Epic";
        else if (grade.equalsIgnoreCase("Legendary")) prefix = "legendary";

        if (id.startsWith("SUB_SH")) {
            if (grade.equalsIgnoreCase("Common")) return "commonshield";
            if (grade.equalsIgnoreCase("Uncommon")) return "UncommonShield";
            if (grade.equalsIgnoreCase("Rare")) return "RareShield";
            if (grade.equalsIgnoreCase("Epic")) return "Epicshield";
            if (grade.equalsIgnoreCase("Legendary")) return "legendaryshield";
        }
        if (id.startsWith("SUB_QV")) {
            if (grade.equalsIgnoreCase("Uncommon")) return "UcommonQuiver";
            return prefix + "Quiver";
        }
        if (id.startsWith("WPN_WC")) {
            if (grade.equalsIgnoreCase("Common")) return "CommonCriticalBlade";
            if (grade.equalsIgnoreCase("Uncommon")) return "UnCommonCriticalBlade";
            return prefix + "CriticalBlade";
        }
        if (id.startsWith("WPN_AS")) {
            if (grade.equalsIgnoreCase("Epic") || grade.equalsIgnoreCase("Legendary"))
                return prefix + "Criticalshortbow";
            return prefix + "CriticalShortbow";
        }
        if (id.startsWith("ARM")) {
            if (grade.equalsIgnoreCase("Common")) return "commonarmor";
            return prefix + "Armor";
        }
        if (id.startsWith("HEL")) return prefix + "helmet";
        if (id.startsWith("PNT")) return prefix + "pants";
        if (id.startsWith("BOT")) return prefix + "boots";
        if (id.startsWith("WPN_WP")) return prefix + "PowerSword";
        if (id.startsWith("WPN_AB")) return prefix + "Longbow";

        return "bread";
    }

    public Item getItem(String itemId) {
        Item item = itemMap.get(itemId);
        if (item != null) {
            return new Item(item);
        }
        return null;
    }

    public Item getRandomItem() {
        if (allItems.size == 0) return null;
        return new Item(allItems.get(MathUtils.random(0, allItems.size - 1)));
    }
}
