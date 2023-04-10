package net.walksanator.tisstring;

import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.shared.util.CapabilityUtil;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.prefab.module.AbstractModule;
import li.cil.tis3d.common.module.RandomAccessMemoryModule;
import li.cil.tis3d.common.module.ReadOnlyMemoryModule;
import li.cil.tis3d.common.tileentity.CasingTileEntity;
import li.cil.tis3d.common.tileentity.ControllerTileEntity;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.walksanator.tisstring.modulePeripheral.InteropModulePeripheral;
import net.walksanator.tisstring.modulePeripheral.RAMModulePeripheral;
import net.walksanator.tisstring.modulePeripheral.ROMModulePeripheral;
import net.walksanator.tisstring.modules.InteropModule.InteropModule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static dan200.computercraft.shared.Capabilities.CAPABILITY_PERIPHERAL;



@Mod.EventBusSubscriber
public class CasingPeripheral implements IPeripheral, ICapabilityProvider {
    private static final ResourceLocation CAP_ID = new ResourceLocation( TISString.MOD_ID, "casing" );
    private final CasingTileEntity casing;
    private LazyOptional<IPeripheral> self;
    public CasingPeripheral(CasingTileEntity commandBlock ) {this.casing = commandBlock;}

    public static Map<Class<? extends AbstractModule>, Function<AbstractModule,Object>> ModulePeripheralImpls = new HashMap<>();

    static {
        ModulePeripheralImpls.put(InteropModule.class, InteropModulePeripheral::newTable);
        ModulePeripheralImpls.put(RandomAccessMemoryModule.class, RAMModulePeripheral::newTable);
        ModulePeripheralImpls.put(ReadOnlyMemoryModule.class, ROMModulePeripheral::newTable);
    }

    @LuaFunction
    public Object getFaces() {
        return Arrays.stream(Face.values()).map(Enum::toString).toArray();
    }
    @LuaFunction
    public Object getFace(String face) {
        AbstractModule mod = (AbstractModule) casing.getModule(Face.valueOf(face));
        if (mod == null) {return new Object[]{null,"Face has no module"};}
        Function<AbstractModule,Object> fn = ModulePeripheralImpls.get(mod.getClass());
        if (fn != null) {
            return fn.apply(mod);
        }
        return new Object[]{null,"Module does not have a Peripheral Implementation"};
    }

    @LuaFunction
    public Object getModule(String face) {
        AbstractModule mod = (AbstractModule) casing.getModule(Face.valueOf(face));
        if (mod == null) {return new Object[]{null,"Face has no module"};}
        return mod.getClass().getName();
    }

    @LuaFunction
    public Object getState() {
        ControllerTileEntity casingte = casing.getController();
        if (casingte == null) {return new Object[]{null,"Casing does not have a controller attached"};}
        return casingte.getState().toString();
    }

    @Nonnull
    @Override
    public String getType() {return "casing";}

    @Override
    public boolean equals(@Nullable IPeripheral other) {
        return other != null && other.getClass() == getClass();
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if( cap == CAPABILITY_PERIPHERAL )
        {
            if( self == null ) self = LazyOptional.of( () -> this );
            return self.cast();
        }
        return LazyOptional.empty();
    }

    @Nonnull
    @Override
    public Object getTarget() {return casing;}

    private void invalidate() { self = CapabilityUtil.invalidate( self );}

    @SubscribeEvent
    public static void onCapability( AttachCapabilitiesEvent<BlockEntity> event )
    {
        BlockEntity tile = event.getObject();
        if( tile instanceof CasingTileEntity casing )
        {
            CasingPeripheral peripheral = new CasingPeripheral( casing );
            event.addCapability( CAP_ID, peripheral );
            event.addListener( peripheral::invalidate );
        }
    }
}
