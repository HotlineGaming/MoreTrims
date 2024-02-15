package net.guilarducci.eat.moretrims.mixin.client;

import com.google.common.collect.ImmutableList;
import net.guilarducci.eat.moretrims.MoreTrims;
import net.minecraft.client.renderer.texture.atlas.SpriteResourceLoader;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.client.renderer.texture.atlas.sources.PalettedPermutations;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(SpriteResourceLoader.class)
public abstract class SpriteResourceLoaderMixin {

    //Copy and pasted alex cave's code here, rlly should look more on how it works

    @Inject(method = "load",
            at = @At("RETURN"))
    private static void ac_load(ResourceManager resourceManager, ResourceLocation location, CallbackInfoReturnable<SpriteResourceLoader> cir) {
        if (location.getPath().equals("armor_trims")) {
            SpriteResourceLoader ret = cir.getReturnValue();
            for (SpriteSource source : ((SpriteResourceLoaderMixin) (Object) ret).getSources()) {
                if (source instanceof PalettedPermutationsAccessor permutations && permutations.getPaletteKey().getPath().equals("trims/color_palettes/trim_palette")) {
                    ResourceLocation trimLocationHero = new ResourceLocation(MoreTrims.MODID, "trims/models/armor/hero");
                    ResourceLocation leggingsTrimLocationHero = new ResourceLocation(MoreTrims.MODID, "trims/models/armor/hero").withSuffix("_leggings");
                    ResourceLocation trimLocationWar = new ResourceLocation(MoreTrims.MODID, "trims/models/armor/war");
                    ResourceLocation leggingsTrimLocationWar = new ResourceLocation(MoreTrims.MODID, "trims/models/armor/war").withSuffix("_leggings");
                    permutations.setTextures(ImmutableList.<ResourceLocation>builder().addAll(permutations.getTextures()).add(
                            trimLocationHero, leggingsTrimLocationHero,
                            trimLocationWar, leggingsTrimLocationWar).build());
                }
            }
        }
    }

    @Accessor("sources")
    abstract List<SpriteSource> getSources();

    @Mixin(PalettedPermutations.class)
    private interface PalettedPermutationsAccessor {

        @Accessor
        List<ResourceLocation> getTextures();

        @Accessor("textures")
        @Mutable
        void setTextures(List<ResourceLocation> value);

        @Accessor
        ResourceLocation getPaletteKey();
    }

}
