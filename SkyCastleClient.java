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
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class SkyCastleClient implements ClientModInitializer {
    private static KeyBinding skillKey;

    // ---- SAO 풍 색상 팔레트 ----
    private static final int FRAME   = 0xFFE4ECF4; // 밝은 은백색 테두리
    private static final int PANEL   = 0xD0101820; // 반투명 짙은 남색
    private static final int WHITE   = 0xFFFFFFFF;
    private static final int GRAY    = 0xFFAFC0CF;
    private static final int ACCENT  = 0xFFFFB347; // 주황 포인트

    @Override
    public void onInitializeClient() {
        KeyBinding.Category category = KeyBinding.Category.create(Identifier.of(SkyCastle.MOD_ID, "main"));
        skillKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.skycastle.skill", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_R, category));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (skillKey.wasPressed()) {
                ClientPlayNetworking.send(new SkillPayload());
            }
        });

        // 바닐라 체력/배고픔 UI 숨기기 (커스텀 HUD가 대체)
        HudElementRegistry.replaceElement(VanillaHudElements.HEALTH_BAR, original -> (ctx, tick) -> {});
        HudElementRegistry.replaceElement(VanillaHudElements.FOOD_BAR, original -> (ctx, tick) -> {});

        HudElementRegistry.attachElementAfter(VanillaHudElements.HOTBAR,
            Identifier.of(SkyCastle.MOD_ID, "status_hud"), SkyCastleClient::renderHud);
    }

    // ================= HUD =================
    private static void renderHud(DrawContext ctx, RenderTickCounter tick) {
        MinecraftClient mc = MinecraftClient.getInstance();
        PlayerEntity p = mc.player;
        if (p == null || mc.options.hudHidden) return;

        int x = 14, y = 12;

        // 이름 + 레벨
        Text name = p.getName();
        ctx.drawText(mc.textRenderer, name, x, y, WHITE, true);
        int nameW = mc.textRenderer.getWidth(name);
        ctx.drawText(mc.textRenderer, Text.translatable("hud.skycastle.level", p.experienceLevel),
            x + nameW + 8, y, ACCENT, true);

        // HP 바
        float hp = clamp(p.getHealth() / p.getMaxHealth());
        int hpTop = hp > 0.5f ? 0xFF9BF0A0 : hp > 0.2f ? 0xFFFFD66B : 0xFFFF8A80;
        int hpBot = hp > 0.5f ? 0xFF2FA84F : hp > 0.2f ? 0xFFE08A00 : 0xFFD32F2F;
        int barW = 160, barH = 10;
        slantBar(ctx, x, y + 13, barW, barH, 5, hp, hpTop, hpBot, true);
        Text hpText = Text.literal((int) Math.ceil(p.getHealth()) + " / " + (int) p.getMaxHealth());
        ctx.drawText(mc.textRenderer, hpText,
            x + barW - mc.textRenderer.getWidth(hpText) - 8, y, GRAY, true);

        // SP(배고픔) 바 - 얇게
        float sp = clamp(p.getHungerManager().getFoodLevel() / 20f);
        slantBar(ctx, x, y + 27, 112, 4, 2, sp, 0xFF9BD8FF, 0xFF2E86DE, false);

        // 스킬 슬롯
        ItemStack stack = p.getMainHandStack();
        int sy = y + 38;
        if (stack.getItem() instanceof SkillWeaponItem weapon) {
            float cd = p.getItemCooldownManager().getCooldownProgress(stack, tick.getTickProgress(false));
            ctx.fill(x - 1, sy - 1, x + 29, sy + 29, FRAME);
            ctx.fill(x, sy, x + 28, sy + 28, PANEL);
            ctx.drawItem(stack, x + 6, sy + 6);
            int h = (int) (28 * cd);
            if (h > 0) ctx.fill(x, sy + 28 - h, x + 28, sy + 28, 0x99FFFFFF);
            // 우하단 키 표시
            ctx.fill(x + 17, sy + 18, x + 28, sy + 28, 0xE0000000);
            ctx.drawText(mc.textRenderer, skillKey.getBoundKeyLocalizedText(), x + 20, sy + 19, WHITE, false);

            ctx.drawText(mc.textRenderer, Text.translatable(weapon.getSkill().key), x + 36, sy + 4, WHITE, true);
            int lineW = 100;
            ctx.fill(x + 36, sy + 16, x + 36 + lineW, sy + 18, 0xFF2A3440);
            ctx.fill(x + 36, sy + 16, x + 36 + (int) (lineW * (1 - cd)), sy + 18, cd > 0 ? GRAY : 0xFF7FD8FF);
            ctx.drawText(mc.textRenderer, Text.translatable(cd > 0 ? "hud.skycastle.cooldown" : "hud.skycastle.ready"),
                x + 36, sy + 21, cd > 0 ? GRAY : 0xFF7FD8FF, false);
            sy += 36;
        }

        // 파티원 (주변 다른 플레이어, 최대 3명)
        int shown = 0;
        if (mc.world != null) {
            for (PlayerEntity o : mc.world.getPlayers()) {
                if (o == p || shown >= 3) continue;
                float r = clamp(o.getHealth() / o.getMaxHealth());
                ctx.drawText(mc.textRenderer, o.getName(), x + 2, sy, WHITE, true);
                slantBar(ctx, x, sy + 11, 96, 5, 2, r,
                    r > 0.5f ? 0xFF9BF0A0 : r > 0.2f ? 0xFFFFD66B : 0xFFFF8A80,
                    r > 0.5f ? 0xFF2FA84F : r > 0.2f ? 0xFFE08A00 : 0xFFD32F2F, false);
                sy += 22;
                shown++;
            }
        }
    }

    // ================= 그리기 도우미 =================
    /** 오른쪽 아래가 비스듬히 잘린 SAO 풍 게이지 */
    private static void slantBar(DrawContext ctx, int x, int y, int w, int h, int slant,
                                 float ratio, int topColor, int botColor, boolean ticks) {
        shape(ctx, x - 1, y - 1, w + 2, h + 2, slant, FRAME);   // 테두리
        shape(ctx, x, y, w, h, slant, 0xFF0B1118);               // 바닥
        int fw = (int) (w * ratio);
        for (int i = 0; i < h; i++) {
            int trim = h > 1 ? slant * i / (h - 1) : 0;
            int end = Math.min(x + fw, x + w - trim);
            if (end > x) {
                int c = lerp(topColor, botColor, h > 1 ? i / (float) (h - 1) : 0f);
                ctx.fill(x, y + i, end, y + i + 1, c);
            }
        }
        // 윗면 하이라이트
        int hl = Math.min(x + fw, x + w);
        if (hl > x) ctx.fill(x, y, hl, y + 1, 0x66FFFFFF);
        if (ticks) {
            for (int k = 1; k < 10; k++) {
                int px = x + w * k / 10;
                if (px < x + w - slant) ctx.fill(px, y, px + 1, y + h, 0x40000000);
            }
        }
    }

    private static void shape(DrawContext ctx, int x, int y, int w, int h, int slant, int color) {
        for (int i = 0; i < h; i++) {
            int trim = h > 1 ? slant * i / (h - 1) : 0;
            ctx.fill(x, y + i, x + w - trim, y + i + 1, color);
        }
    }

    private static float clamp(float v) { return Math.max(0f, Math.min(1f, v)); }

    private static int lerp(int a, int b, float t) {
        int r = (int) (((a >> 16) & 255) * (1 - t) + ((b >> 16) & 255) * t);
        int g = (int) (((a >> 8) & 255) * (1 - t) + ((b >> 8) & 255) * t);
        int bl = (int) ((a & 255) * (1 - t) + (b & 255) * t);
        return 0xFF000000 | (r << 16) | (g << 8) | bl;
    }
}
