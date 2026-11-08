package kr.skycastle.item;

import kr.skycastle.SkyCastle;
import kr.skycastle.skill.SwordSkill;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ToolMaterial;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public final class ModItems {
    public static final Item ONE_HANDED_SWORD = weapon("one_handed_sword",
        new Item.Settings().sword(ToolMaterial.IRON, 3.0f, -2.4f), SwordSkill.VERTICAL);
    public static final Item RAPIER = weapon("rapier",
        new Item.Settings().sword(ToolMaterial.IRON, 2.0f, -1.8f), SwordSkill.LINEAR);
    public static final Item DAGGER = weapon("dagger",
        new Item.Settings().sword(ToolMaterial.IRON, 1.0f, -1.2f), SwordSkill.FLURRY);
    public static final Item GREATSWORD = weapon("greatsword",
        new Item.Settings().sword(ToolMaterial.DIAMOND, 6.0f, -3.2f), SwordSkill.WHIRLWIND);

    private static Item weapon(String name, Item.Settings settings, SwordSkill skill) {
        RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(SkyCastle.MOD_ID, name));
        return Registry.register(Registries.ITEM, key, new SkillWeaponItem(settings.registryKey(key), skill));
    }

    public static void register() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT).register(entries -> {
            entries.add(ONE_HANDED_SWORD);
            entries.add(RAPIER);
            entries.add(DAGGER);
            entries.add(GREATSWORD);
        });
    }
}
