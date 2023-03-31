package net.walksanator.tisstring.modules.InteropModule;

import dan200.computercraft.api.lua.*;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.shared.util.CapabilityUtil;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.module.Module;
import li.cil.tis3d.common.tileentity.CasingTileEntity;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.Mod;
import net.walksanator.tisstring.TISString;
import net.walksanator.tisstring.util.HalfFloat;
import org.jetbrains.annotations.NotNull;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

import java.nio.ByteBuffer;

import static dan200.computercraft.shared.Capabilities.CAPABILITY_PERIPHERAL;

@Mod.EventBusSubscriber
public class CasingPeripheral implements IPeripheral, ICapabilityProvider {
    private static final ResourceLocation CAP_ID = new ResourceLocation( TISString.MOD_ID, "casing" );
    private final CasingTileEntity casing;
    private LazyOptional<IPeripheral> self;
    public CasingPeripheral( CasingTileEntity commandBlock ) {this.casing = commandBlock;}

    @LuaFunction
    public final Short popInt() throws LuaException {
        for (Face face : Face.values()) {
            Module mod = casing.getModule(face);
            if (mod instanceof InteropModule mod2) {
                if (mod2.inbuf.size() > 0) {
                    Short out = mod2.inbuf.get(0);
                    mod2.inbuf.remove(0);
                    return out;
                }
                return null;
            }
        }
        throw new LuaException("Could not find a InteropModule on casing");
    }

    @LuaFunction
    public final Integer popUint() throws LuaException {
        for (Face face : Face.values()) {
            Module mod = casing.getModule(face);
            if (mod instanceof InteropModule mod2) {
                if (mod2.inbuf.size() > 0) {
                    Short out = mod2.inbuf.get(0);
                    mod2.inbuf.remove(0);
                    return Short.toUnsignedInt(out);
                }
                return null;
            }
        }
        throw new LuaException("Could not find a InteropModule on casing");
    }

    @LuaFunction
    public final testLuaFn test(IArguments args) throws LuaException {
        LuaTable<?, ?> argv = args.getTableUnsafe(0);
        Object argc = args.getAll();
        TISString.LOGGER.info(argv);
        TISString.LOGGER.info(argc);
        return new testLuaFn(3.14);
    }

    private static class testLuaFn implements ILuaFunction {

        double test;
        public testLuaFn(double d) {
            this.test = d;
        }

        @NotNull
        @Override
        public MethodResult call(@NotNull IArguments iArguments) throws LuaException {
            TISString.LOGGER.info("test function return was called");
            return MethodResult.of();
        }
    }

    @LuaFunction
    public final Float popFloat() throws LuaException {
        for (Face face : Face.values()) {
            Module mod = casing.getModule(face);
            if (mod instanceof InteropModule mod2) {
                if (mod2.inbuf.size() > 0) {
                    Short out = mod2.inbuf.get(0);
                    mod2.inbuf.remove(0);
                    return HalfFloat.toFloat(out);
                }
                return null;
            }
        }
        throw new LuaException("Could not find a InteropModule on casing");
    }

    @LuaFunction
    public final void pushDouble(IArguments args) throws LuaException {
        Double value = args.optDouble(0,0.0);
        for (Face face : Face.values()) {
            Module mod = casing.getModule(face);
            if (mod instanceof InteropModule mod2) {
                mod2.outbuf.add(HalfFloat.toHalf(value.floatValue()));
                return;
            }
        }
        throw new LuaException("Could not find a InteropModule on casing");
    }

    @LuaFunction
    public final void pushInt(IArguments args) throws LuaException {
        Integer value = args.optInt(0,0);
        for (Face face : Face.values()) {
            Module mod = casing.getModule(face);
            if (mod instanceof InteropModule mod2) {
                if (value < Short.MAX_VALUE) {
                    mod2.outbuf.add(value.shortValue());
                } else if (value > Short.MAX_VALUE) {
                    mod2.outbuf.add(
                            ByteBuffer.allocate(4)
                            .putInt(value)
                            .getShort(2)
                    );
                }
                return;
            }
        }
        throw new LuaException("Could not find a InteropModule on casing");
    }

    @LuaFunction
    public final int getLen() throws LuaException {
        for (Face face : Face.values()) {
            Module mod = casing.getModule(face);
            if (mod instanceof InteropModule) {
                return ((InteropModule) mod).inbuf.size();
            }
        }
        throw new LuaException("Could not find a InteropModule on casing");
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
