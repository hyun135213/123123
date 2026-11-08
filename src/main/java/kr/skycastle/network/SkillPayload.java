package kr.skycastle.network;

import kr.skycastle.SkyCastle;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/** 클라이언트 -> 서버: "스킬 사용" 요청 */
public record SkillPayload() implements CustomPayload {
    public static final Id<SkillPayload> ID = new Id<>(Identifier.of(SkyCastle.MOD_ID, "use_skill"));
    public static final PacketCodec<RegistryByteBuf, SkillPayload> CODEC = PacketCodec.unit(new SkillPayload());

    @Override
    public Id<? extends CustomPayload> getId() { return ID; }
}
