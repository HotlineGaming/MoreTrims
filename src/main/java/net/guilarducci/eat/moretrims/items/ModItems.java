package net.guilarducci.eat.moretrims.items;

import net.guilarducci.eat.moretrims.MoreTrims;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SmithingTemplateItem;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.*;


public class ModItems {

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MoreTrims.MODID);

    public static final RegistryObject<Item> HEROTRIM = ITEMS.register("hero_armor_trim_smithing_template", () -> SmithingTemplateItem.createArmorTrimTemplate(new ResourceLocation("moretrims:hero")));
    public static final RegistryObject<Item> WARTRIM = ITEMS.register("war_armor_trim_smithing_template", () -> SmithingTemplateItem.createArmorTrimTemplate(new ResourceLocation("moretrims:war")));


    public static void register(IEventBus eventBus){
        ITEMS.register(eventBus);
    }
}
