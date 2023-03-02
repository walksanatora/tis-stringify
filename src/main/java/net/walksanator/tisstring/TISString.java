package net.walksanator.tisstring;

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

@Mod("tisstring")
public class TISString {

    public static final String MOD_ID = "tisstring";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID);
    static final DeferredRegister<ModuleProvider> MODULES = DeferredRegister.create(ModuleProvider.REGISTRY, MOD_ID);
    static final DeferredRegister<Tab> TABS = DeferredRegister.create(Constants.TAB_REGISTRY, MOD_ID);
    static final DeferredRegister<PathProvider> PATH_PROVIDERS = DeferredRegister.create(Constants.PATH_PROVIDER_REGISTRY, MOD_ID);
    static final DeferredRegister<DocumentProvider> CONTENT_PROVIDERS = DeferredRegister.create(Constants.DOCUMENT_PROVIDER_REGISTRY, MOD_ID);

    public static final RegistryObject<StringModuleItem> STR_ITEM = ITEMS.register("string_module", StringModuleItem::new);

    public TISString() {
        
        MODULES.register("string_module", () -> new SimpleModuleProvider<StringModule>(STR_ITEM, StringModule::new));
        
        MODULES.register(FMLJavaModLoadingContext.get().getModEventBus());
        ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());

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
}
