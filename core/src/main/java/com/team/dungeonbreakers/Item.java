package com.team.dungeonbreakers;

public class Item {
    public String itemId;
    public String name;
    public String type; // WEAPON, ARMOR, CONSUMABLE, ETC
    public String grade; // COMMON, RARE, LEGENDARY ...
    public String description;

    // 장비 스탯
    public int defense;
    public int attackPower;
    public float criticalDamage; // ★ 복구됨
    public int maxHp;
    public float criticalChance;
    public float attackSpeed; // 공격 속도 증가량 (예: 0.1 = 10%)
    public float cooldown; // 쿨타임 감소량
    public float dashCooldownReduction;
    public int dashMaxCharges;

    // 소모품 효과 및 기타
    public String effectType; // HEAL, BUFF_ATK, BUFF_SPD 등
    public float value;       // ★ 복구됨 (회복량, 버프 수치 등)
    public float duration;

    // 이미지
    public String textureRegionName;

    // 인벤토리 상태
    public int count = 1;
    public int maxStack = 1;

    // 상점
    public int price = 0; // ★ 유지됨

    public Item() {}

    // 복사 생성자 (모든 필드 복사)
    public Item(Item other) {
        this.itemId = other.itemId;
        this.name = other.name;
        this.type = other.type;
        this.grade = other.grade;
        this.description = other.description;

        this.defense = other.defense;
        this.attackPower = other.attackPower;
        this.criticalDamage = other.criticalDamage;
        this.maxHp = other.maxHp;
        this.criticalChance = other.criticalChance;
        this.attackSpeed = other.attackSpeed;
        this.cooldown = other.cooldown;
        this.dashCooldownReduction = other.dashCooldownReduction;
        this.dashMaxCharges = other.dashMaxCharges;

        this.effectType = other.effectType;
        this.value = other.value;
        this.duration = other.duration;

        this.textureRegionName = other.textureRegionName;

        this.count = other.count;
        this.maxStack = other.maxStack;

        this.price = other.price;
    }
}
