package net.walksanator.tisstring.manual;

import java.util.Objects;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector4f;

import net.walksanator.tisstring.TISString;
import li.cil.manual.api.ManualModel;
import li.cil.manual.api.Tab;
import li.cil.manual.api.prefab.tab.AbstractTab;
import li.cil.manual.api.util.MarkdownManualRegistryEntry;
import li.cil.tis3d.client.manual.Manuals;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class TISStringTab extends AbstractTab {

    public TISStringTab(String path, Component tooltip) {
        super(path, tooltip);
    }

    @Override
    public void renderIcon(final PoseStack matrixStack) {

        final Vector4f position = new Vector4f(0, 0, 0, 1);
        position.transform(matrixStack.last().pose());

        final PoseStack renderSystemPoseStack = RenderSystem.getModelViewStack();
        renderSystemPoseStack.pushPose();
        renderSystemPoseStack.translate(position.x(), position.y(), 0);

        Minecraft.getInstance().getItemRenderer().renderGuiItem(new ItemStack(TISString.STR_ITEM.get()), 0, 0);

        renderSystemPoseStack.popPose();
        RenderSystem.applyModelViewMatrix();

        // Unfuck GL state.
        RenderSystem.enableBlend();
    }

    @Override
    public boolean matches(final ManualModel manual) {
        return Objects.equals(manual, Manuals.MANUAL.get());
    }

    @Override
    public int sortOrder() {
        return Integer.MAX_VALUE;
    }

    @Override
    public int compareTo(final MarkdownManualRegistryEntry<Tab> other) {
        return Integer.compare(this.sortOrder(), other.sortOrder());
    }
    
}
