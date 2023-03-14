package net.walksanator.tisstring;

import li.cil.tis3d.common.item.ModuleItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.walksanator.tisstring.modules.InteropModule.InteropModule;
import net.walksanator.tisstring.modules.parsemodule.ParseModule;
import net.walksanator.tisstring.modules.parsemodule.ParseModuleItem;
import net.walksanator.tisstring.modules.stringmodule.StringModule;
import net.walksanator.tisstring.modules.stringmodule.StringModuleItem;
import net.walksanator.tisstring.manual.TISStringContentProvider;
import net.walksanator.tisstring.manual.TISStringPathProvider;
import net.walksanator.tisstring.manual.TISStringTab;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import li.cil.manual.api.ManualModel;
import li.cil.manual.api.Tab;
import li.cil.manual.api.provider.DocumentProvider;
import li.cil.manual.api.provider.PathProvider;
import li.cil.manual.api.util.Constants;
import li.cil.tis3d.api.module.ModuleProvider;
import li.cil.tis3d.common.provider.module.SimpleModuleProvider;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

@Mod("tisstring")
public class TISString {

    public static final String MOD_ID = "tisstring";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID);
    static final DeferredRegister<ModuleProvider> MODULES = DeferredRegister.create(ModuleProvider.REGISTRY, MOD_ID);
    static final DeferredRegister<Tab> TABS = DeferredRegister.create(Constants.TAB_REGISTRY, MOD_ID);
    static final DeferredRegister<PathProvider> PATH_PROVIDERS = DeferredRegister.create(Constants.PATH_PROVIDER_REGISTRY, MOD_ID);
    static final DeferredRegister<DocumentProvider> CONTENT_PROVIDERS = DeferredRegister.create(Constants.DOCUMENT_PROVIDER_REGISTRY, MOD_ID);
    static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS,TISString.MOD_ID);

    public static final RegistryObject<StringModuleItem> STR_ITEM = ITEMS.register("string_module", StringModuleItem::new);
    public static final RegistryObject<ParseModuleItem> NUM_ITEM = ITEMS.register("parse_module", ParseModuleItem::new);
    public static RegistryObject<ModuleItem> INTEROP_ITEM = null;

    static {
        if (ModList.get().isLoaded("computercraft")) {
            INTEROP_ITEM = ITEMS.register("interop_module", ModuleItem::new);
            MODULES.register("interop_module", () -> new SimpleModuleProvider<InteropModule>(INTEROP_ITEM,InteropModule::new));
        }
    }

    public TISString() {
        
        MODULES.register("string_module", () -> new SimpleModuleProvider<StringModule>(STR_ITEM, StringModule::new));
        MODULES.register("parse_module", () -> new SimpleModuleProvider<ParseModule>(NUM_ITEM, ParseModule::new));

        IEventBus evBus = FMLJavaModLoadingContext.get().getModEventBus();
        MODULES.register(evBus);
        ITEMS.register(evBus);
        BLOCKS.register(evBus);

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            CONTENT_PROVIDERS.register("content_provider", () -> new TISStringContentProvider(MOD_ID,"doc"));
            PATH_PROVIDERS.register("path_provider", () -> new TISStringPathProvider(MOD_ID));
            TABS.register(MOD_ID, () -> {
                return new TISStringTab(ManualModel.LANGUAGE_KEY + "/tisstring.md", new TranslatableComponent("tisstring.manual.tab"));
            });

            CONTENT_PROVIDERS.register(FMLJavaModLoadingContext.get().getModEventBus());
            PATH_PROVIDERS.register(FMLJavaModLoadingContext.get().getModEventBus());
            TABS.register(FMLJavaModLoadingContext.get().getModEventBus());
        });

        MinecraftForge.EVENT_BUS.register(this);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            FMLJavaModLoadingContext.get().getModEventBus().register(ClientSetup.class);
        });
    }

    private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block, CreativeModeTab tab) {
        RegistryObject<T> toReturn = BLOCKS.register(name, block);
        ITEMS.register(name, () -> new BlockItem(toReturn.get(),
                new Item.Properties().tab(tab)));
        return toReturn;
    }

}
