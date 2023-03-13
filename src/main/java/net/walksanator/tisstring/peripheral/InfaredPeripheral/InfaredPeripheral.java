package net.walksanator.tisstring.peripheral.InfaredPeripheral;

import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.walksanator.tisstring.TISString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class InfaredPeripheral implements IPeripheral {
    private final Level level;
    private final BlockPos pos;

    public InfaredPeripheral(Level level, BlockPos worldPosition) {
        this.level = level;
        this.pos = worldPosition;
    }

    @NotNull
    @Override
    public String getType() {
        return "infrared";
    }

    @Override
    public boolean equals(@Nullable IPeripheral iPeripheral) {
        return level.getBlockState(pos).is(TISString.IR_BLOCK.get());
    }
}
