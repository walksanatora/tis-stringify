package net.walksanator.tisstring;

import java.util.Objects;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@OnlyIn(Dist.CLIENT)
public class ClientSetup {
    @SubscribeEvent
    public static void handleTextureStitchEvent(final TextureStitchEvent.Pre event) {
        if (Objects.equals(event.getAtlas().location(), InventoryMenu.BLOCK_ATLAS)) {
            event.addSprite(new ResourceLocation(net.walksanator.tisstring.TISString.MOD_ID,"block/overlay/asic_module"));
        }
    }
}
