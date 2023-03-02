package net.walksanator.tisstring.modules.stringmodule;

import java.util.List;

import javax.annotation.Nullable;

import li.cil.tis3d.api.API;
import li.cil.tis3d.common.item.ModuleItem;
import li.cil.tis3d.util.EnumUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

// import net.minecraft.world.level.block.entity.BedBlockEntity;

public class StringModuleItem extends ModuleItem {

    public StringModuleItem() {
        super(createProperties().stacksTo(1).tab(API.itemGroup));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(final Level level, final Player player, final InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        StringModule.MODE mode = loadFromStack(stack);

        StringModule.MODE nextMode;
        if(player.isShiftKeyDown()) {
            nextMode = mode.prev();
        } else {
            nextMode = mode.next();
        }

        saveToStack(stack, nextMode);
        if(level.isClientSide()) {
            player.displayClientMessage(new TextComponent(nextMode.name()), true);
        }
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        StringModule.MODE mode = loadFromStack(stack);

        tooltip.add(new TextComponent("Mode: " + mode.toString()));
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
    }

    public static StringModule.MODE loadFromTag(@Nullable final CompoundTag tag) {
        if (tag != null) {
            return EnumUtils.load(StringModule.MODE.class, StringModule.TAG_MODE, tag);
        } else {
            return StringModule.MODE.INT;
        }
    }

    /**
     * Load ROM data from the specified item stack.
     *
     * @param stack the item stack to load the data from.
     * @return the data loaded from the stack.
     */
    public static StringModule.MODE loadFromStack(final ItemStack stack) {
        return loadFromTag(stack.getTag());
    }

    /**
     * Save the specified ROM data to the specified item stack.
     *
     * @param stack the item stack to save the data to.
     * @param data  the data to save to the item stack.
     */
    public static void saveToStack(final ItemStack stack, final StringModule.MODE mode) {
        final CompoundTag tag = stack.getOrCreateTag();
        EnumUtils.save(mode, StringModule.TAG_MODE, tag);
    }
}
