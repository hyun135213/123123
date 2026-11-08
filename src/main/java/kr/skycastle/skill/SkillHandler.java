package kr.skycastle.skill;

import kr.skycastle.item.SkillWeaponItem;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public final class SkillHandler {
    private SkillHandler() {}

    /** 서버에서 실행: 손에 든 무기의 스킬을 발동 */
    public static void tryUse(ServerPlayerEntity player) {
        ItemStack stack = player.getMainHandStack();
        if (!(stack.getItem() instanceof SkillWeaponItem weapon)) return;
        if (player.getItemCooldownManager().isCoolingDown(stack)) return;

        SwordSkill skill = weapon.getSkill();
        ServerWorld world = (ServerWorld) player.getEntityWorld();
        Vec3d look = player.getRotationVec(1.0f).multiply(1, 0, 1).normalize();

        // 돌진
        if (skill.dash > 0) {
            player.setVelocity(look.x * skill.dash, 0.1, look.z * skill.dash);
            player.velocityModified = true;
        }

        // 타격 범위
        Box box = skill.aoe
            ? player.getBoundingBox().expand(skill.reach)
            : player.getBoundingBox().stretch(look.multiply(skill.reach)).expand(1.0);

        List<LivingEntity> targets = world.getEntitiesByClass(LivingEntity.class, box,
            e -> e != player && e.isAlive());
        for (LivingEntity t : targets) {
            t.damage(world, world.getDamageSources().playerAttack(player), skill.damage);
            t.takeKnockback(0.6, -look.x, -look.z);
        }

        // 연출
        world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP,
            SoundCategory.PLAYERS, 1.0f, 1.0f);
        world.spawnParticles(ParticleTypes.SWEEP_ATTACK, player.getX() + look.x * 1.5,
            player.getBodyY(0.5), player.getZ() + look.z * 1.5, 3, 0.5, 0.3, 0.5, 0.0);

        player.getItemCooldownManager().set(stack, skill.cooldown);
    }
}
