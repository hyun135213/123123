package kr.skycastle.skill;

/** 무기별 스킬 정의. 이름은 lang 파일의 키로 표시됩니다. */
public enum SwordSkill {
    //            키,           쿨타임(tick), 돌진, 사거리, 피해, 광역
    VERTICAL  ("skill.skycastle.vertical",  60, 1.1, 3.5,  8f, false),
    LINEAR    ("skill.skycastle.linear",    50, 1.8, 6.0,  7f, false),
    FLURRY    ("skill.skycastle.flurry",    40, 0.8, 2.5,  6f, false),
    WHIRLWIND ("skill.skycastle.whirlwind", 90, 0.0, 4.0, 11f, true);

    public final String key;
    public final int cooldown;
    public final double dash, reach;
    public final float damage;
    public final boolean aoe;

    SwordSkill(String key, int cooldown, double dash, double reach, float damage, boolean aoe) {
        this.key = key; this.cooldown = cooldown; this.dash = dash;
        this.reach = reach; this.damage = damage; this.aoe = aoe;
    }
}
