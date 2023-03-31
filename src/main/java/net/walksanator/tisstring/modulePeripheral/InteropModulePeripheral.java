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

public class InteropModulePeripheral {
    public static Object newTable(AbstractModule mod) throws LuaException {
        if (mod instanceof InteropModule mod2) {
            return new Object[]{
                    new popInt(mod2),
                    new popFloat(mod2),
                    new pushNum(mod2)
            };
        } else {
            throw new LuaException("Invalid Module for PeripheralImpl (internal code broken, file a issue)");
        }
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
            Object[] values = iArguments.getAll();
            if (values[0].getClass().equals(Double.class)) {
                parent.outbuf.add(HalfFloat.toHalf(((Double)values[0]).floatValue()));
            } else if (values[0].getClass().equals(Integer.class)) {
                if ((int)values[0] < Short.MAX_VALUE) {
                    parent.outbuf.add(((Integer)values[0]).shortValue());
                } else if ((int)values[0] > Short.MAX_VALUE) {
                    parent.outbuf.add(
                            ByteBuffer.allocate(4)
                                    .putInt((int)values[0])
                                    .getShort(2)
                    );
                }
            } else {
                throw new LuaException("Invalid input, expected Number");
            }
            return MethodResult.of();
        }
    }

}
