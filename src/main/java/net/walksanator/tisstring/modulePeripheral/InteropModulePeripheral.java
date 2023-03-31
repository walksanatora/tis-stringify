package net.walksanator.tisstring.modulePeripheral;

import dan200.computercraft.api.lua.IArguments;
import dan200.computercraft.api.lua.ILuaFunction;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.MethodResult;
import li.cil.tis3d.api.prefab.module.AbstractModule;
import net.walksanator.tisstring.modules.InteropModule.InteropModule;
import net.walksanator.tisstring.util.HalfFloat;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.util.Map;

public class InteropModulePeripheral {
    public static Object newTable(AbstractModule mod) {
        if (mod instanceof InteropModule mod2) {
            return Map.of(
                    "popInt", new popInt(mod2),
                    "popFloat", new popFloat(mod2),
                    "pushNum", new pushNum(mod2)
            );
        } else return new Object[]{};
    }
    private static class popInt implements ILuaFunction {
        InteropModule parent;
        public popInt(InteropModule parent) {
            this.parent = parent;
        }

        @NotNull
        @Override
        public MethodResult call(@NotNull IArguments iArguments) {
            if (parent.inbuf.size() > 0) {
                Short out = parent.inbuf.get(0);
                parent.inbuf.remove(0);
                return MethodResult.of(out);
            }
            return MethodResult.of();
        }
    }
    private static class popFloat implements ILuaFunction {
        InteropModule parent;
        public popFloat(InteropModule parent) {
            this.parent = parent;
        }

        @NotNull
        @Override
        public MethodResult call(@NotNull IArguments iArguments) {
            if (parent.inbuf.size() > 0) {
                Short out = parent.inbuf.get(0);
                parent.inbuf.remove(0);
                return MethodResult.of(HalfFloat.toFloat(out));
            }
            return MethodResult.of();
        }
    }
    private static class pushNum implements ILuaFunction {
        InteropModule parent;
        public pushNum(InteropModule parent) {
            this.parent = parent;
        }

        @NotNull
        @Override
        public MethodResult call(@NotNull IArguments iArguments) throws LuaException {
            double value = iArguments.getDouble(0);
            if (Math.floor(value) == value) {
                if ((int)value < Short.MAX_VALUE) {
                    parent.outbuf.add(((Integer)((int)value)).shortValue());
                } else {
                    parent.outbuf.add(
                            ByteBuffer.allocate(4)
                                    .putInt((int)value)
                                    .getShort(2)
                    );
                }
            } else {
                parent.outbuf.add(HalfFloat.toHalf((float) value));
            }
            return MethodResult.of();
        }
    }

}
