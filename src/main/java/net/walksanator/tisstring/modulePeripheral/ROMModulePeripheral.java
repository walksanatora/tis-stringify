package net.walksanator.tisstring.modulePeripheral;

import dan200.computercraft.api.lua.IArguments;
import dan200.computercraft.api.lua.ILuaFunction;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.MethodResult;
import li.cil.tis3d.api.prefab.module.AbstractModule;
import li.cil.tis3d.common.module.RandomAccessMemoryModule;
import li.cil.tis3d.common.module.ReadOnlyMemoryModule;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.Map;

public class ROMModulePeripheral {
    public static Object newTable(AbstractModule mod) {
        if (mod instanceof ReadOnlyMemoryModule ramm) {
            return Map.of("peek", new peek(ramm)
            );
        } else return new Object[]{};
    }
    private static class peek implements ILuaFunction {
        private final ReadOnlyMemoryModule parent;
        public peek(ReadOnlyMemoryModule parent) {
            this.parent = parent;
        }
        @Override
        @NotNull
        public MethodResult call(@NotNull IArguments iArguments) throws LuaException {
            try {
                Field memoryField = RandomAccessMemoryModule.class.getDeclaredField("memory");
                memoryField.setAccessible(true);
                byte[] memory = (byte[]) memoryField.get(parent);
                int idx = iArguments.getInt(0);
                return MethodResult.of(memory[idx & 255] & 255);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new LuaException("Failed to reflect value from ram module");
            }
        }
    }
}