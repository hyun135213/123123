package kr.skycastle.item;

import kr.skycastle.skill.SwordSkill;
import net.minecraft.item.Item;

public class SkillWeaponItem extends Item {
    private final SwordSkill skill;

    public SkillWeaponItem(Settings settings, SwordSkill skill) {
        super(settings);
        this.skill = skill;
    }

    public SwordSkill getSkill() { return skill; }
}
