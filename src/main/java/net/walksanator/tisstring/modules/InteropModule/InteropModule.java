package net.walksanator.tisstring.modules.InteropModule;

import com.mojang.blaze3d.vertex.PoseStack;
import li.cil.manual.api.render.FontRenderer;
import li.cil.tis3d.api.API;
import li.cil.tis3d.api.machine.*;
import li.cil.tis3d.api.prefab.module.AbstractModuleWithRotation;
import li.cil.tis3d.api.util.RenderContext;
import li.cil.tis3d.client.renderer.font.NormalFontRenderer;
import li.cil.tis3d.util.Color;
import li.cil.tis3d.util.EnumUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.walksanator.tisstring.TISString;
import net.walksanator.tisstring.modules.parsemodule.ParseModuleItem;
import net.walksanator.tisstring.util.HalfFloat;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.ArrayList;

public class InteropModule extends AbstractModuleWithRotation {
    static final String TAG_OUTBUF = "outbuf";
    static final String TAG_INBUF = "inbuf";

    public ArrayList<Short> outbuf;
    public ArrayList<Short> inbuf;


    public InteropModule(Casing casing, Face face) {
        super(casing, face);
        this.outbuf = new ArrayList<>();
        this.inbuf = new ArrayList<>();
    }

    @Override
    public void step() {
        try {
            this.stepInput();
            this.stepOutput();
        } catch (NumberFormatException e) {
            TISString.LOGGER.error(e);
        }
    }

    private void stepInput() throws NumberFormatException {
        for(final Port port : Port.VALUES) {
            final Pipe receivingPipe = getCasing().getReceivingPipe(getFace(), port);
            if (!receivingPipe.isReading()) {
                receivingPipe.beginRead();
            }
            if (receivingPipe.canTransfer()) {
                inbuf.add(receivingPipe.read());
            }
        }
    }

    private void stepOutput() {
        if (outbuf.size() > 0) {
            //TISString.LOGGER.info("Writing value {} mode {}",val,mode);
            for (final Port port : Port.VALUES) {
                final Pipe sendingPipe = getCasing().getSendingPipe(getFace(), port);
                if (!sendingPipe.isWriting()) {
                    //TISString.LOGGER.info("writing {} {}", val, port);
                    sendingPipe.beginWrite(outbuf.get(0));
                }
            }
        }
    }

    @Override
    public void onBeforeWriteComplete(final Port port) {
        // Pop the value (that was being written).
        outbuf.remove(0);

        // If one completes, cancel all other writes to ensure a value is only
        // written once.
        cancelWrite();
    }

    @Override
    public void onWriteComplete(final Port port) {
        // Re-cancel in case step() was called after onBeforeWriteComplete() to
        // ensure all our writes are in sync.
        cancelWrite();

        // If we're done, tell clients we can input again.
        if (outbuf.size() > 0) {
            stepOutput();
        }
    }

    @Override
    public void load(final CompoundTag tag) {
        super.load(tag);
        for (int val : tag.getIntArray(TAG_INBUF)) {
            this.inbuf.add((short) val);
        };
        for (int val : tag.getIntArray(TAG_OUTBUF)) {
            this.outbuf.add((short) val);
        };
    }

    @Override
    public void save(final CompoundTag tag) {
        super.save(tag);
        ArrayList<Integer> temp = new ArrayList<Integer>();
        for (int val : this.outbuf) {
            temp.add( val);
        };
        tag.putIntArray(TAG_OUTBUF, temp);
        temp.clear();
        for (int val : this.inbuf) {
            temp.add( val);
        };
        tag.putIntArray(TAG_INBUF, temp);
    }

    @OnlyIn(Dist.CLIENT)
    public void render(final RenderContext context) {
        if (!getCasing().isEnabled() || !this.isVisible()) {return;};
        final PoseStack matrixStack = context.getMatrixStack();
        matrixStack.pushPose();
        this.rotateForRendering(matrixStack);
        context.drawAtlasQuadUnlit(new ResourceLocation(TISString.MOD_ID,"block/overlay/interop_module"));

        matrixStack.popPose();
    }
}
