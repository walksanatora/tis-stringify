package net.walksanator.tisstring.modules.parsemodule;

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
import net.walksanator.tisstring.util.HalfFloat;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

public class ParseModule extends AbstractModuleWithRotation {
    static final String TAG_MODE = "mode";
    static final String TAG_ERR = "err";
    static final String TAG_OUTBUF = "outbuf";
    static final String TAG_INBUF = "inbuf";


    enum MODE {
        INT,
        UINT,
        FLT,
        HEX;

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

    enum ERR {
        NULL,
        HCF;

        ERR next() {
            if(this.ordinal() == ERR.values().length - 1) {
                return ERR.values()[0];
            } else {
                return ERR.values()[this.ordinal() + 1];
            }
        }

        ERR prev() {
            if(this.ordinal() == 0) {
                return ERR.values()[MODE.values().length - 1];
            } else {
                return ERR.values()[this.ordinal() - 1];
            }
        }
    }

    private MODE mode;

    private ERR on_error;

    private static final Charset CP437 = Charset.forName("Cp437");
    private final CharsetEncoder encoder = CP437.newEncoder();

    private StringBuilder outbuf;
    private StringBuilder inbuf;


    public ParseModule(Casing casing, Face face) {
        super(casing, face);
        this.mode = MODE.INT;
        this.on_error = ERR.NULL;
        this.outbuf = new StringBuilder();
        this.inbuf = new StringBuilder();
    }

    @Override
    public void step() {
        try {
            this.stepInput();
        } catch (NumberFormatException e) {
            TISString.LOGGER.error(e);
            inbuf.setLength(0);
            switch (this.on_error) {
                case HCF -> throw new HaltAndCatchFireException();
                case NULL -> outbuf.append('\0');
            }
        }
    }

    private void stepInput() throws NumberFormatException {
        for(final Port port : Port.VALUES) {
            final Pipe receivingPipe = getCasing().getReceivingPipe(getFace(), port);
            if (!receivingPipe.isReading()) {
                receivingPipe.beginRead();
            }
            if (receivingPipe.canTransfer()) {
                short inval = receivingPipe.read();
                if (inval == 0) {
                    switch (this.mode) {
                        case INT -> outbuf.append((char) Short.parseShort(inbuf.toString()));
                        case UINT -> outbuf.append((char) (Integer.parseInt(inbuf.toString())& 0xffff));
                        case FLT -> outbuf.append((char) HalfFloat.toHalf(Float.parseFloat(inbuf.toString())));
                        case HEX -> outbuf.append((char)
                                ByteBuffer.allocate(4)
                                        .putInt(Integer.parseInt(inbuf.toString(),16)&0xffff)
                                        .getShort(2));
                    }

                    inbuf.setLength(0);
                    this.stepOutput();
                } else {
                    inbuf.append((char)inval);
                }
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
        }
    }

    @Override
    public void onInstalled(final @NotNull ItemStack stack) {
        Tuple<MODE, ERR> data = ParseModuleItem.loadFromStack(stack);
        this.mode = data.getA();
        this.on_error = data.getB();
    }

    @Override
    public void onUninstalled(final ItemStack stack) {
        ParseModuleItem.saveToStack(stack, this.mode,this.on_error);
    }

    @Override
    public void load(final CompoundTag tag) {
        super.load(tag);
        this.mode = EnumUtils.load(MODE.class, TAG_MODE, tag);
        this.on_error = EnumUtils.load(ERR.class, TAG_ERR, tag);

        this.inbuf.setLength(0);
        this.inbuf.append(tag.getString((TAG_INBUF)));
        this.outbuf.setLength(0);
        this.outbuf.append(tag.getString(TAG_OUTBUF));
    }

    @Override
    public void save(final CompoundTag tag) {
        super.save(tag);
        EnumUtils.save(this.mode, TAG_MODE, tag);
        EnumUtils.save(this.on_error, TAG_ERR, tag);
        tag.putString(TAG_OUTBUF, outbuf.toString());
        tag.putString(TAG_INBUF, inbuf.toString());
    }

    @OnlyIn(Dist.CLIENT)
    public void render(final RenderContext context) {
        if (!getCasing().isEnabled() || !this.isVisible()) {return;};

        context.drawString(NormalFontRenderer.INSTANCE, this.mode.toString(), 0xFFFF);

        final PoseStack matrixStack = context.getMatrixStack();
        matrixStack.pushPose();
        this.rotateForRendering(matrixStack);
        context.drawAtlasQuadUnlit(new ResourceLocation(TISString.MOD_ID,"block/overlay/parse_module"));

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
        matrixStack.scale(1 / 64f, 1 / 64f, 1f);

        if(this.mode.name().length() == 4) {
            matrixStack.translate(2.5f, 10f, 0);
        } else if(this.mode.name().length() == 3) {
            matrixStack.translate(7.25f, 10f, 0);
        } else if(this.mode.name().length() == 2) {
            matrixStack.translate(12.5f, 10f, 0);
        }

        context.drawString(font, this.mode.name(), Color.WHITE);

        if(this.mode.name().length() == 4) {
            matrixStack.translate(-2.5f, -5f, 0);
        } else if(this.mode.name().length() == 3) {
            matrixStack.translate(-7.25f, -5f, 0);
        } else if(this.mode.name().length() == 2) {
            matrixStack.translate(-12.5f, -5f, 0);
        }

        if(this.on_error.name().length() == 4) {
            matrixStack.translate(2.5f, -6f, 0);
        } else if(this.on_error.name().length() == 3) {
            matrixStack.translate(7.25f, -6f, 0);
        } else if(this.on_error.name().length() == 2) {
            matrixStack.translate(12.5f, -6f, 0);
        }

        context.drawString(font,this.on_error.name(),Color.RED);
    }
}
