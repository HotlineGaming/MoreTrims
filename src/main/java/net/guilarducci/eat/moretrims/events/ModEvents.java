package net.guilarducci.eat.moretrims.events;

import com.mojang.datafixers.DataFixer;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.guilarducci.eat.moretrims.items.ModItems;
import net.minecraft.advancements.Advancement;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.world.entity.npc.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.event.village.VillagerTradesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.*;

@Mod.EventBusSubscriber
public class ModEvents {
    public static PlayerAdvancements MakePlayerAdvancements(DataFixer fixer, Player player, MinecraftServer server){
        return new PlayerAdvancements(fixer , server.getPlayerList(), server.getAdvancements(), player.getServer().getWorldPath(LevelResource.PLAYER_ADVANCEMENTS_DIR), server.getPlayerList().getPlayer(player.getUUID()));
    }

    @SubscribeEvent
    public static void GetTrimFromToolsmith(VillagerTradesEvent event) {
        if (event.getType() == VillagerProfession.TOOLSMITH) {
            Int2ObjectMap<List<VillagerTrades.ItemListing>> trades = event.getTrades();
            trades.get(5).add((pTrader, pRandom) -> {
                if (!pTrader.level().isClientSide) {
                    MinecraftServer server = pTrader.getServer();
                    Player player = pTrader.level().getNearestPlayer(pTrader, 1000);
                    ServerPlayer serverPlayer = server.getPlayerList().getPlayer(player.getUUID());
                    PlayerAdvancements playerAdvancements = MakePlayerAdvancements(DataFixers.getDataFixer(), player, server);
                    playerAdvancements.reload(server.getAdvancements());
                    Advancement HERO = server.getAdvancements().getAdvancement(Objects.requireNonNull(ResourceLocation.tryParse("minecraft:adventure/hero_of_the_village")));
                    if (serverPlayer.getAdvancements().getOrStartProgress(HERO).isDone()) {
                        return new MerchantOffer(
                                new ItemStack(ModItems.HEROTRIM.get(), 1),
                                new ItemStack(Items.EMERALD, 7),
                                new ItemStack(ModItems.HEROTRIM.get(), 2),
                                1, 20, 0);
                    } else {
                        return null;
                    }
                } else {
                    return null;
                }
            });
        }
    }
}

