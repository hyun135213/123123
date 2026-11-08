package kr.skycastle.client;

import kr.skycastle.SkyCastle;
import kr.skycastle.item.SkillWeaponItem;
import kr.skycastle.network.SkillPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class SkyCastleClient implements ClientModInitializer {
    private static KeyBinding skillKey;

    @Override
    public void onInitializeClient() {
        // 키 설정: 설정 > 조작 에서 "스카이 캐슬" 항목으로 표시됨
        KeyBinding.Category category = KeyBinding.Category.create(Identifier.of(SkyCastle.MOD_ID, "main"));
        skillKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.skycastle.skill", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_R, category));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (skillKey.wasPressed()) {
                ClientPlayNetworking.send(new SkillPayload());
            }
        });

        // HUD: 핫바 뒤에 SAO 느낌의 HP 바 + 스킬 쿨타임 표시
        HudElementRegistry.attachElementAfter(VanillaHudElements.HOTBAR,
            Identifier.of(SkyCastle.MOD_ID, "status_hud"), (ctx, tickCounter) -> {
                MinecraftClient mc = MinecraftClient.getInstance();
                PlayerEntity p = mc.player;
                if (p == null || mc.options.hudHidden) return;

                // HP 바 (좌상단)
                int x = 10, y = 10, w = 120, h = 8;
                float ratio = Math.max(0, Math.min(1, p.getHealth() / p.getMaxHealth()));
                int color = ratio > 0.5f ? 0xFF4CD964 : ratio > 0.2f ? 0xFFFFCC00 : 0xFFFF3B30;
                ctx.fill(x - 1, y - 1, x + w + 1, y + h + 1, 0xFF000000);
                ctx.fill(x, y, x + w, y + h, 0xFF303030);
                ctx.fill(x, y, x + (int) (w * ratio), y + h, color);
                ctx.drawText(mc.textRenderer, p.getName(), x, y - 10, 0xFFFFFFFF, true);
                ctx.drawText(mc.textRenderer,
                    Text.literal((int) p.getHealth() + " / " + (int) p.getMaxHealth()),
                    x + w + 6, y, 0xFFFFFFFF, true);

                // 스킬 표시 (손에 든 무기가 스킬 무기일 때)
                ItemStack stack = p.getMainHandStack();
                if (stack.getItem() instanceof SkillWeaponItem weapon) {
                    float cd = p.getItemCooldownManager()
                        .getCooldownProgress(stack, tickCounter.getTickProgress(false));
                    int sy = y + 22;
                    ctx.drawText(mc.textRenderer, Text.translatable(weapon.getSkill().key),
                        x, sy, 0xFFFFE066, true);
                    ctx.fill(x, sy + 11, x + w, sy + 15, 0xFF303030);
                    ctx.fill(x, sy + 11, x + (int) (w * (1 - cd)), sy + 15,
                        cd > 0 ? 0xFF8E8E93 : 0xFF5AC8FA);
                    ctx.drawText(mc.textRenderer, Text.translatable("hud.skycastle.key_hint",
                        skillKey.getBoundKeyLocalizedText()), x, sy + 18, 0xFFAAAAAA, true);
                }
            });
    }
}
