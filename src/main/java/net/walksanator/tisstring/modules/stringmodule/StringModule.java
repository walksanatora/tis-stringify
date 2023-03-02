package net.walksanator.tisstring.modules.stringmodule;

import com.mojang.blaze3d.vertex.PoseStack;

import net.walksanator.tisstring.TISString;
import net.walksanator.tisstring.util.HalfFloat;
import li.cil.manual.api.render.FontRenderer;
import li.cil.tis3d.api.API;
import li.cil.tis3d.api.machine.Casing;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.machine.HaltAndCatchFireException;
import li.cil.tis3d.api.machine.Pipe;
import li.cil.tis3d.api.machine.Port;
import li.cil.tis3d.api.prefab.module.AbstractModuleWithRotation;
import li.cil.tis3d.api.util.RenderContext;
import li.cil.tis3d.client.renderer.font.NormalFontRenderer;
import li.cil.tis3d.util.Color;
import li.cil.tis3d.util.EnumUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.api.distmarker.Dist;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.ArrayList;
import java.util.List;

public class StringModule extends AbstractModuleWithRotation {
    static final String TAG_MODE = "mode";

    enum STATE {
        AWAITING_INPUT,
        OUTPUTTING,
    }

    enum MODE {
        INT,
        UNIT,
        FLT;

        MODE next() {
            if(this.ordinal() == MODE.values().length - 1) {
                return MODE.values()[0];
            } else {
                return MODE.values()[this.ordinal() + 1];
            }
        }

        MODE prev() {
            if(this.ordinal() == 0) {
                return MODE.values()[MODE.values().length - 1];
            } else {
                return MODE.values()[this.ordinal() - 1];
            }
        }
    }

    private MODE mode;

    private STATE state;

    private static final Charset CP437 = Charset.forName("Cp437");
    private final CharsetEncoder encoder = CP437.newEncoder();

    private List<Short> outbuf;

    public StringModule(Casing casing, Face face) {
        super(casing, face);
        this.mode = MODE.INT;
        this.state = STATE.AWAITING_INPUT;
        this.outbuf = new ArrayList<>();
    }

    @Override
    public void step() {
        try {
            this.stepInput();
            this.stepOutput();
        } catch (CharacterCodingException e) {
            throw new HaltAndCatchFireException();
        }
    }

    private void stepInput() throws CharacterCodingException {
        for(final Port port : Port.VALUES) {
            if (outbuf.size() == 0) {
                state = STATE.AWAITING_INPUT;
            }
            final Pipe receivingPipe = getCasing().getReceivingPipe(getFace(), port);
            if (!receivingPipe.isReading()) {
                receivingPipe.beginRead();
            }
            if (receivingPipe.canTransfer() && (state == STATE.AWAITING_INPUT) ) {
                switch (this.mode) {
                    case INT -> {
                        int val = receivingPipe.read();
                        String outstring = Integer.toString(val);
                        ByteBuffer bbuf = encoder.encode(CharBuffer.wrap(outstring));
                        while (bbuf.hasRemaining()) {
                            outbuf.add((short) (bbuf.get() & 0xFF));
                        }
                        outbuf.add((short) 0);
                        state = STATE.OUTPUTTING;
                    }
                    case UNIT -> {
                        short val = receivingPipe.read();
                        String outstring = Short.toString(val);
                        ByteBuffer bbuf = encoder.encode(CharBuffer.wrap(outstring));
                        while (bbuf.hasRemaining()) {
                            outbuf.add((short) (bbuf.get() & 0xFF));
                        }
                        outbuf.add((short) 0);
                        state = STATE.OUTPUTTING;
                    }
                    case FLT -> {
                        double input = HalfFloat.toFloat(receivingPipe.read());
                        String outstring = Double.toString(input);
                        ByteBuffer bbuf = encoder.encode(CharBuffer.wrap(outstring));
                        while (bbuf.hasRemaining()) {
                            outbuf.add((short) (bbuf.get() & 0xFF));
                        }
                        outbuf.add((short) 0);
                        state = STATE.OUTPUTTING;
                    }
                }
            }
        }
    }

    private void stepOutput() {
        this.cancelWrite();
        if (outbuf.size() > 0) {
            boolean hasWritten = false;
            short val = outbuf.get(0);
            TISString.LOGGER.info("Writing value {} mode {}",val,mode);
            for (final Port port : Port.VALUES) {
                final Pipe sendingPipe = getCasing().getSendingPipe(getFace(), port);
                if (!sendingPipe.isWriting()) {
                    TISString.LOGGER.info("writing {} {}",val,port);
                    sendingPipe.beginWrite(val);
                    hasWritten = true;
                }
            }
            if (hasWritten) {outbuf.remove(0);}
        }
    }

    @Override
    public void onInstalled(final @NotNull ItemStack stack) {
        this.mode = StringModuleItem.loadFromStack(stack);
    }

    @Override
    public void onUninstalled(final ItemStack stack) {
        StringModuleItem.saveToStack(stack, this.mode);
    }

    @Override
    public void onBeforeWriteComplete(final Port port) {
        // If one completes, cancel all other writes to ensure a value is only
        // written once.
        cancelWrite();
    }

    @Override
    public void onWriteComplete(final Port port) {
        // Re-cancel in case step() was called after onBeforeWriteComplete() to
        // ensure we're not writing while waiting for input.
        cancelWrite();
    }

    @Override
    public void load(final CompoundTag tag) {
        super.load(tag);
        this.mode = EnumUtils.load(MODE.class, TAG_MODE, tag);
    }

    @Override
    public void save(final CompoundTag tag) {
        super.save(tag);
        EnumUtils.save(this.mode, TAG_MODE, tag);
    }

    @OnlyIn(Dist.CLIENT)
    public void render(final RenderContext context) {

        context.drawString(NormalFontRenderer.INSTANCE, this.mode.toString(), 0xFFFF);

        final PoseStack matrixStack = context.getMatrixStack();
        matrixStack.pushPose();
        this.rotateForRendering(matrixStack);
        context.drawAtlasQuadUnlit(new ResourceLocation(TISString.MOD_ID,"block/overlay/asic_module"));

        if (context.closeEnoughForDetails(getCasing().getPosition())) {
            drawState(context);
        }

        matrixStack.popPose();
    }

    @OnlyIn(Dist.CLIENT)
    private void drawState(RenderContext context) {
        final PoseStack matrixStack = context.getMatrixStack();
        final FontRenderer font = API.normalFontRenderer;

        matrixStack.translate(3 / 16f, 5 / 16f, 0);
        matrixStack.scale(1 / 64f, 1 / 64f, 1);

        if(this.mode.name().length() == 4) {
            matrixStack.translate(2.5f, 5f, 0);
        } else if(this.mode.name().length() == 3) {
            matrixStack.translate(7.25f, 5f, 0);
        } else if(this.mode.name().length() == 2) {
            matrixStack.translate(12.5f, 5f, 0);
        }

        context.drawString(font, this.mode.name(), Color.WHITE);
    }
}
