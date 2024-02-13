package net.guilarducci.eat.moretrims.mixin;

import com.google.common.collect.ImmutableList;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.guilarducci.eat.moretrims.util.PiglinBruteAiUtilMethods;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.monster.piglin.PiglinArmPose;
import net.minecraft.world.entity.monster.piglin.PiglinBrute;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PiglinBrute.class)
public abstract class PiglinBruteMixin extends AbstractPiglin {

    public PiglinBruteMixin(EntityType<? extends AbstractPiglin> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public void pickUpItem(ItemEntity pItemEntity) {
        this.onItemPickup(pItemEntity);
        PiglinBruteAiUtilMethods.pickUpItem(((PiglinBrute) (Object) this), pItemEntity);
    }

    @Shadow
    protected static ImmutableList<MemoryModuleType<?>> MEMORY_TYPES;

    static{
        MEMORY_TYPES = ImmutableList.<MemoryModuleType<?>>builder()
                .addAll(MEMORY_TYPES)
                .addAll(ImmutableList.of(MemoryModuleType.ADMIRING_ITEM, MemoryModuleType.TIME_TRYING_TO_REACH_ADMIRE_ITEM, MemoryModuleType.ADMIRING_DISABLED, MemoryModuleType.DISABLE_WALK_TO_ADMIRE_ITEM, MemoryModuleType.NEAREST_PLAYER_HOLDING_WANTED_ITEM, MemoryModuleType.INTERACTION_TARGET, MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM))
                .build();
    }


    @Override
    public boolean canHunt() {
        return false;
    }

    @WrapOperation(method = "wantsToPickUp", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/world/item/Item;)Z"))
    private boolean makehimpickupskulls(ItemStack instance, Item pItem, Operation<Boolean> original) {
        return original.call(instance, pItem) || PiglinBruteAiUtilMethods.wantsToPickup(((PiglinBrute) (Object) this), instance);
    }

    @Override
    public PiglinArmPose getArmPose() {
        if(this.isAggressive() && this.isHoldingMeleeWeapon()){
            return PiglinArmPose.ATTACKING_WITH_MELEE_WEAPON;
        } else if(PiglinBruteAiUtilMethods.isLovedItem(this.getOffhandItem())){
            return PiglinArmPose.ADMIRING_ITEM;
        } else {
            return PiglinArmPose.DEFAULT;
        }
    }

    @Override
    protected void playConvertedSound() {
        this.playSound(SoundEvents.PIGLIN_BRUTE_CONVERTED_TO_ZOMBIFIED, 1.0F, this.getVoicePitch());
    }

    public InteractionResult mobInteract(Player pPlayer, InteractionHand pHand) {
        InteractionResult interactionresult = super.mobInteract(pPlayer, pHand);
        if (interactionresult.consumesAction()) {
            return interactionresult;
        } else if (!this.level().isClientSide) {
            return PiglinBruteAiUtilMethods.mobInteract(((PiglinBrute) (Object) this), pPlayer, pHand);
        } else {
            boolean flag = PiglinBruteAiUtilMethods.canAdmire(((PiglinBrute) (Object) this), pPlayer.getItemInHand(pHand)) && this.getArmPose() != PiglinArmPose.ADMIRING_ITEM;
            return flag ? InteractionResult.SUCCESS : InteractionResult.PASS;
        }
    }
}
