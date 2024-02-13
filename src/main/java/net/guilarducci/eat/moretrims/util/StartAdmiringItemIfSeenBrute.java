package net.guilarducci.eat.moretrims.util;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.monster.piglin.PiglinBrute;
import net.minecraft.world.entity.monster.piglin.StartAdmiringItemIfSeen;
import net.minecraft.world.level.Level;

public class StartAdmiringItemIfSeenBrute {
    public static BehaviorControl<LivingEntity> create(int pAdmireDuration) {
        return BehaviorBuilder.create((plivingentity) -> {
            return plivingentity.group(plivingentity.present(MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM), plivingentity.absent(MemoryModuleType.ADMIRING_ITEM), plivingentity.absent(MemoryModuleType.ADMIRING_DISABLED), plivingentity.absent(MemoryModuleType.DISABLE_WALK_TO_ADMIRE_ITEM)).apply(plivingentity, (p_259343_, p_260195_, p_259697_, p_259511_) -> {
                return (p_260130_, p_259946_, p_259235_) -> {
                    ItemEntity itementity = plivingentity.get(p_259343_);
                    if (!PiglinBruteAiUtilMethods.isLovedItem(itementity.getItem())) {
                        return false;
                    } else {
                        p_260195_.setWithExpiry(true, (long)pAdmireDuration);
                        return true;
                    }
                };
            });
        });
    }
}
