package kr.skycastle;

import kr.skycastle.item.ModItems;
import kr.skycastle.network.SkillPayload;
import kr.skycastle.skill.SkillHandler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public class SkyCastle implements ModInitializer {
    public static final String MOD_ID = "skycastle";

    @Override
    public void onInitialize() {
        ModItems.register();
        PayloadTypeRegistry.playC2S().register(SkillPayload.ID, SkillPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(SkillPayload.ID,
            (payload, context) -> SkillHandler.tryUse(context.player()));
    }
}
