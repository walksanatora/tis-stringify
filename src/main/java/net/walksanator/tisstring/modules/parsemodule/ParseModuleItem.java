package net.walksanator.tisstring.modules.parsemodule;

import li.cil.tis3d.api.API;
import li.cil.tis3d.common.item.ModuleItem;
import li.cil.tis3d.util.EnumUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.util.Tuple;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class ParseModuleItem extends ModuleItem {

    public ParseModuleItem() {
        super(createProperties().stacksTo(1).tab(API.itemGroup));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(final Level level, final Player player, final InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        Tuple<ParseModule.MODE,ParseModule.ERR> data = loadFromStack(stack);

        ParseModule.MODE newMode = data.getA();
        ParseModule.ERR newErr = data.getB();
        if(player.isShiftKeyDown()) {
            newErr = newErr.next();
        } else {
            newMode = newMode.next();
        }

        saveToStack(stack, newMode,newErr);
        if(level.isClientSide()) {
            if(player.isShiftKeyDown()) {
                player.displayClientMessage(new TextComponent(newErr.name()), true);
            } else {
                player.displayClientMessage(new TextComponent(newMode.name()), true);
            }

        }
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        Tuple<ParseModule.MODE,ParseModule.ERR> data = loadFromStack(stack);

        tooltip.add(new TextComponent("Mode: " + data.getA()));
        tooltip.add(new TextComponent("OnError: " + data.getB()));
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
    }

    public static Tuple<ParseModule.MODE,ParseModule.ERR> loadFromTag(@Nullable final CompoundTag tag) {
        if (tag != null) {
            return new  Tuple (
                    EnumUtils.load(ParseModule.MODE.class, ParseModule.TAG_MODE, tag),
                    EnumUtils.load(ParseModule.ERR.class, ParseModule.TAG_ERR, tag)
                    );
        } else {
            return new Tuple(ParseModule.MODE.INT, ParseModule.ERR.NULL);
        }
    }

    /**
     * Load ROM data from the specified item stack.
     *
     * @param stack the item stack to load the data from.
     * @return the data loaded from the stack.
     */
    public static Tuple<ParseModule.MODE,ParseModule.ERR> loadFromStack(final ItemStack stack) {
        return loadFromTag(stack.getTag());
    }

    /**
     * Save the specified ROM data to the specified item stack.
     *
     * @param stack the item stack to save the data to.
     * @param data  the data to save to the item stack.
     */
    public static void saveToStack(final ItemStack stack, final ParseModule.MODE mode, final ParseModule.ERR err) {
        final CompoundTag tag = stack.getOrCreateTag();
        EnumUtils.save(mode, ParseModule.TAG_MODE, tag);
        EnumUtils.save(err, ParseModule.TAG_ERR, tag);
    }
}
