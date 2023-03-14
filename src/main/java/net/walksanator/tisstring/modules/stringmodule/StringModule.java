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

import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

public class StringModule extends AbstractModuleWithRotation {
    static final String TAG_MODE = "mode";
    static final String TAG_OUTBUF = "outbuf";

    enum STATE {
        AWAITING_INPUT,
        OUTPUTTING,
    }

    enum MODE {
        INT,
        UINT,
        FLT,
        HEX,
        UHEX;

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

    private StringBuilder outbuf;

    public StringModule(Casing casing, Face face) {
        super(casing, face);
        this.mode = MODE.INT;
        this.state = STATE.AWAITING_INPUT;
        this.outbuf = new StringBuilder();
    }

    @Override
    public void step() {
        try {
            this.stepInput();
            //this.stepOutput();
        } catch (CharacterCodingException e) {
            throw new HaltAndCatchFireException();
        }
    }

    private void stepInput() throws CharacterCodingException {
        for(final Port port : Port.VALUES) {
            final Pipe receivingPipe = getCasing().getReceivingPipe(getFace(), port);
            if (!receivingPipe.isReading()) {
                receivingPipe.beginRead();
            }
            if (receivingPipe.canTransfer() && (state == STATE.AWAITING_INPUT) ) {
                switch (this.mode) {
                    case INT -> {
                        outbuf.append(receivingPipe.read());
                    }
                    case UINT -> {
                        outbuf.append(Short.toUnsignedInt(receivingPipe.read()));
                    }
                    case FLT -> {
                        outbuf.append(HalfFloat.toFloat(receivingPipe.read()));
                    }
                    case HEX -> {
                        outbuf.append(String.format("%040x",receivingPipe.read()));
                    }
                    case UHEX -> {
                        outbuf.append(String.format("%04X",receivingPipe.read()));
                    }
                }
                outbuf.append('\0');
                outbuf.reverse();
                state = STATE.OUTPUTTING;
                this.stepOutput();
            }
        }

    }

    private void stepOutput() {
        if (outbuf.length() > 0) {
            short val = (short) outbuf.charAt(outbuf.length() - 1);;
            //TISString.LOGGER.info("Writing value {} mode {}",val,mode);
            for (final Port port : Port.VALUES) {
                final Pipe sendingPipe = getCasing().getSendingPipe(getFace(), port);
                if (!sendingPipe.isWriting()) {
                    //TISString.LOGGER.info("writing {} {}", val, port);
                    sendingPipe.beginWrite(val);
                }
            }
        }
    }

    @Override
    public void onBeforeWriteComplete(final Port port) {
        // Pop the value (that was being written).
        outbuf.setLength(outbuf.length() - 1);

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
        if (outbuf.length() > 0) {
            stepOutput();
        } else {
            state = STATE.AWAITING_INPUT;
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
    public void load(final CompoundTag tag) {
        super.load(tag);
        this.mode = EnumUtils.load(MODE.class, TAG_MODE, tag);
        this.outbuf.setLength(0);
        this.outbuf.append(tag.getString(TAG_OUTBUF));
    }

    @Override
    public void save(final CompoundTag tag) {
        super.save(tag);
        EnumUtils.save(this.mode, TAG_MODE, tag);
        tag.putString(TAG_OUTBUF, outbuf.toString());
    }

    @OnlyIn(Dist.CLIENT)
    public void render(final RenderContext context) {

        context.drawString(NormalFontRenderer.INSTANCE, this.mode.toString(), 0xFFFF);

        final PoseStack matrixStack = context.getMatrixStack();
        matrixStack.pushPose();
        this.rotateForRendering(matrixStack);
        context.drawAtlasQuadUnlit(new ResourceLocation(TISString.MOD_ID,"block/overlay/string_module"));

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
