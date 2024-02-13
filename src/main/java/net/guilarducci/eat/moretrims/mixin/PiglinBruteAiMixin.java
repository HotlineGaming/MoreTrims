package net.guilarducci.eat.moretrims.mixin;

import com.google.common.collect.ImmutableList;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.guilarducci.eat.moretrims.util.PiglinBruteAiUtilMethods;
import net.guilarducci.eat.moretrims.util.StartAdmiringItemIfSeenBrute;
import net.guilarducci.eat.moretrims.util.StopHoldingItemIfNoLongerAdmiringBrute;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.*;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.piglin.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.schedule.Activity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

import static net.guilarducci.eat.moretrims.util.PiglinBruteAiUtilMethods.isHoldingItemInOffHand;
import static net.guilarducci.eat.moretrims.util.PiglinBruteAiUtilMethods.stopHoldingOffHandItem;

@Mixin(PiglinBruteAi.class)
public class PiglinBruteAiMixin {

    @Unique
    private static void moreTrims$initCoreAdmiring(Brain<PiglinBrute> pBrain) {
        pBrain.addActivity(Activity.CORE, 0, ImmutableList.of(new LookAtTargetSink(45, 90), new MoveToTargetSink(), StopHoldingItemIfNoLongerAdmiringBrute.create(), StartAdmiringItemIfSeenBrute.create(120)));
    }

    @Unique
    private static void moreTrims$initAdmireItemActivity(Brain<PiglinBrute> pBrain) {
        pBrain.addActivityAndRemoveMemoryWhenStopped(Activity.ADMIRE_ITEM, 10, ImmutableList.of(GoToWantedItem.create(PiglinBruteAiUtilMethods::isNotHoldingLovedItemInOffHand, 0.6F, true, 9), StopAdmiringIfItemTooFarAway.create(9), StopAdmiringIfTiredOfTryingToReachItem.create(200, 200)), MemoryModuleType.ADMIRING_ITEM);
    }

    @Inject(method = "wasHurtBy", at = @At("HEAD"))
    private static void wasHurtByPlayer(PiglinBrute pPiglinBrute, LivingEntity pTarget, CallbackInfo ci) {
        Brain<PiglinBrute> brain = pPiglinBrute.getBrain();
        brain.eraseMemory(MemoryModuleType.ADMIRING_ITEM);

        if (isHoldingItemInOffHand(pPiglinBrute)) {
            stopHoldingOffHandItem(pPiglinBrute, false);
        }

        if (pTarget instanceof Player) {
            brain.setMemoryWithExpiry(MemoryModuleType.ADMIRING_DISABLED, true, 400L);
        }
    }

    @WrapOperation(method = "updateActivity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ai/Brain;setActiveActivityToFirstValid(Ljava/util/List;)V"))
    private static void AddAdmireActivity(Brain instance, List<Activity> activities, Operation<Void> original){
        original.call(instance, ImmutableList.of(Activity.ADMIRE_ITEM, activities.get(0), activities.get(1)));
    }

    @Inject(method = "makeBrain", at = @At(value = "HEAD"))
    private static void makeBrain(PiglinBrute pPiglinBrute, Brain<PiglinBrute> pBrain, CallbackInfoReturnable<Brain<?>> cir) {
        moreTrims$initAdmireItemActivity(pBrain);
        moreTrims$initCoreAdmiring(pBrain);
    }


}
