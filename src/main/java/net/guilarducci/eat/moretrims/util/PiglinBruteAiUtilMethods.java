package net.guilarducci.eat.moretrims.util;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.monster.piglin.PiglinBrute;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class PiglinBruteAiUtilMethods extends PiglinAi {

    private static void stopWalking(PiglinBrute pPiglin) {
        pPiglin.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        pPiglin.getNavigation().stop();
    }

    public static boolean isLovedItem(ItemStack p_149966_) {
        return p_149966_.is(Items.WITHER_SKELETON_SKULL);
    }

    private static Vec3 getRandomNearbyPos(PiglinBrute pPiglin) {
        Vec3 vec3 = LandRandomPos.getPos(pPiglin, 4, 2);
        return vec3 == null ? pPiglin.position() : vec3;
    }

    private static void throwItemsTowardPos(PiglinBrute pPiglin, List<ItemStack> pStacks, Vec3 pPos) {
        if (!pStacks.isEmpty()) {
            pPiglin.swing(InteractionHand.OFF_HAND);

            for (ItemStack itemstack : pStacks) {
                BehaviorUtils.throwItem(pPiglin, itemstack, pPos.add(0.0D, 1.0D, 0.0D));
            }
        }

    }

    private static void throwItemsTowardRandomPos(PiglinBrute pPiglin, List<ItemStack> pStacks) {
        throwItemsTowardPos(pPiglin, pStacks, getRandomNearbyPos(pPiglin));
    }

    private static void throwItemsTowardPlayer(PiglinBrute pPiglin, Player pPlayer, List<ItemStack> pStacks) {
        throwItemsTowardPos(pPiglin, pStacks, pPlayer.position());
    }

    private static void throwItems(PiglinBrute pPilgin, List<ItemStack> pStacks) {
        Optional<Player> optional = pPilgin.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_PLAYER);
        if (optional.isPresent()) {
            throwItemsTowardPlayer(pPilgin, optional.get(), pStacks);
        } else {
            throwItemsTowardRandomPos(pPilgin, pStacks);
        }

    }

    private static Optional<? extends LivingEntity> getTargetIfWithinRange(AbstractPiglin pPiglinBrute, MemoryModuleType<? extends LivingEntity> pMemoryType) {
        return pPiglinBrute.getBrain().getMemory(pMemoryType).filter((p_35108_) -> {
            return p_35108_.closerThan(pPiglinBrute, 12.0D);
        });
    }

    public static Optional<? extends LivingEntity> findNearestValidAttackTarget(AbstractPiglin p_35087_) {
        Optional<LivingEntity> optional = BehaviorUtils.getLivingEntityFromUUIDMemory(p_35087_, MemoryModuleType.ANGRY_AT);
        if (optional.isPresent() && Sensor.isEntityAttackableIgnoringLineOfSight(p_35087_, optional.get())) {
            return optional;
        } else {
            Optional<? extends LivingEntity> optional1 = getTargetIfWithinRange(p_35087_, MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER);
            return optional1.isPresent() ? optional1 : p_35087_.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_NEMESIS);
        }
    }

    public static boolean isNotHoldingLovedItemInOffHand(PiglinBrute p_35029_) {
        return p_35029_.getOffhandItem().isEmpty() || !isLovedItem(p_35029_.getOffhandItem());
    }

    private static List<ItemStack> getBarterResponseItems(PiglinBrute pPiglin) {
        LootTable loottable = pPiglin.level().getServer().getLootData().getLootTable(BuiltInLootTables.PIGLIN_BARTERING);
        List<ItemStack> list = loottable.getRandomItems((new LootParams.Builder((ServerLevel) pPiglin.level())).withParameter(LootContextParams.THIS_ENTITY, pPiglin).create(LootContextParamSets.PIGLIN_BARTER));
        return list;
    }

    private static boolean isHoldingItemInOffHand(PiglinBrute pPiglin) {
        return !pPiglin.getOffhandItem().isEmpty();
    }

    private static void holdInOffhand(PiglinBrute pPiglin, ItemStack pStack) {
        if (isHoldingItemInOffHand(pPiglin)) {
            pPiglin.spawnAtLocation(pPiglin.getItemInHand(InteractionHand.OFF_HAND));
        }

        pPiglin.setItemSlot(EquipmentSlot.OFFHAND, pStack);
    }

    private static void admireGoldItem(LivingEntity pPiglin) {
       pPiglin.getBrain().setMemoryWithExpiry(MemoryModuleType.ADMIRING_ITEM, true, 120L);
    }


    private static ItemStack removeOneItemFromItemEntity(ItemEntity pItemEntity) {
        ItemStack itemstack = pItemEntity.getItem();
        ItemStack itemstack1 = itemstack.split(1);
        if (itemstack.isEmpty()) {
            pItemEntity.discard();
        } else {
            pItemEntity.setItem(itemstack);
        }

        return itemstack1;
    }

    private static void putInInventory(PiglinBrute pPiglin, ItemStack pStack) {
        ItemStack itemstack = pStack;
        throwItemsTowardRandomPos(pPiglin, Collections.singletonList(itemstack));
    }

    public static void pickUpItem(PiglinBrute pPiglin, ItemEntity pItemEntity) {
        stopWalking(pPiglin);
        ItemStack itemstack;
        if (pItemEntity.getItem().is(Items.GOLD_NUGGET)) {
            pPiglin.take(pItemEntity, pItemEntity.getItem().getCount());
            itemstack = pItemEntity.getItem();
            pItemEntity.discard();
        } else {
            pPiglin.take(pItemEntity, 1);
            itemstack = removeOneItemFromItemEntity(pItemEntity);
        }

        if (isLovedItem(itemstack)) {
            pPiglin.getBrain().eraseMemory(MemoryModuleType.TIME_TRYING_TO_REACH_ADMIRE_ITEM);
            holdInOffhand(pPiglin, itemstack);
            admireGoldItem(pPiglin);
        } else {
            boolean flag = !pPiglin.equipItemIfPossible(itemstack).equals(ItemStack.EMPTY);
            if (!flag) {
                putInInventory(pPiglin, itemstack);
            }
        }
    }

    public static boolean wantsToPickup(PiglinBrute pPiglin, ItemStack pStack) {
            boolean flag = isNotHoldingLovedItemInOffHand(pPiglin);
            return flag && isLovedItem(pStack);
    }

    public static void stopHoldingOffHandItem(PiglinBrute pPiglin, boolean pShouldBarter) {
        ItemStack itemstack = pPiglin.getItemInHand(InteractionHand.OFF_HAND);
        pPiglin.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
        if (pPiglin.isAdult()) {
            boolean flag = isLovedItem(itemstack);
            if (pShouldBarter && flag) {
                throwItems(pPiglin, getBarterResponseItems(pPiglin));
            }
        }
    }

    private static boolean isAdmiringDisabled(PiglinBrute pPiglin) {
        return pPiglin.getBrain().hasMemoryValue(MemoryModuleType.ADMIRING_DISABLED);
    }

    private static boolean isAdmiringItem(PiglinBrute pPiglin) {
        return pPiglin.getBrain().hasMemoryValue(MemoryModuleType.ADMIRING_ITEM);
    }

    public static boolean canAdmire(PiglinBrute pPiglin, ItemStack pStack) {
        return !isAdmiringDisabled(pPiglin) && !isAdmiringItem(pPiglin) && pPiglin.isAdult() && isLovedItem(pStack);
    }

    public static boolean isPlayerHoldingLovedItem(LivingEntity p_34884_) {
        return p_34884_.getType() == EntityType.PLAYER && p_34884_.isHolding(PiglinAi::isLovedItem);
    }

    public static InteractionResult mobInteract(PiglinBrute pPiglin, Player pPlayer, InteractionHand pHand) {
        ItemStack itemstack = pPlayer.getItemInHand(pHand);
        if (canAdmire(pPiglin, itemstack)) {
            ItemStack itemstack1 = itemstack.split(1);
            holdInOffhand(pPiglin, itemstack1);
            admireGoldItem(pPiglin);
            stopWalking(pPiglin);
            return InteractionResult.CONSUME;
        } else {
            return InteractionResult.PASS;
        }
    }
}
