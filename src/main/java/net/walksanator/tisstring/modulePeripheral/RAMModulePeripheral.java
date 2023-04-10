package net.walksanator.tisstring.modulePeripheral;

import dan200.computercraft.api.lua.IArguments;
import dan200.computercraft.api.lua.ILuaFunction;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.MethodResult;
import li.cil.tis3d.api.prefab.module.AbstractModule;
import li.cil.tis3d.common.module.RandomAccessMemoryModule;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.Map;
public class RAMModulePeripheral {
    public static Object newTable(AbstractModule mod) {
        if (mod instanceof RandomAccessMemoryModule ramm) {
            return Map.of("peek", new peek(ramm)
            );
        } else return new Object[]{};
    }

    private record peek(RandomAccessMemoryModule parent) implements ILuaFunction {
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