package net.walksanator.tisstring.mixin;

import li.cil.tis3d.common.module.execution.compiler.instruction.UnaryInstructionEmitter;
import net.walksanator.tisstring.TISString;
import net.walksanator.tisstring.instructions.LoadInstruction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.google.common.collect.ImmutableMap;
import com.llamalad7.mixinextras.injector.ModifyReceiver;

import li.cil.tis3d.common.module.execution.compiler.Compiler;
import li.cil.tis3d.common.module.execution.compiler.instruction.InstructionEmitter;

@Mixin(Compiler.class)
public class MixinCompiler {
    @ModifyReceiver(
            method = "<clinit>",
            at = @At(value = "INVOKE", target = "Lcom/google/common/collect/ImmutableMap$Builder;build()Lcom/google/common/collect/ImmutableMap;")
    )
    private static @SuppressWarnings("all") ImmutableMap.Builder<String,InstructionEmitter> tisAdvanced$instructionMapModifier(ImmutableMap.Builder<String,InstructionEmitter> builder) {
        TISString.LOGGER.info("Started reg custom ops");

        builder.put(LoadInstruction.NAME, new UnaryInstructionEmitter(()->LoadInstruction.INSTANCE));

        //// Floating point arithmetic operations
        //builder.put(FloatAddInstruction.NAME, new TargetOrImmediateInstructionEmitter(FloatAddInstruction::new, FloatAddImmediateInstruction::new));
        //builder.put(FloatSubInstruction.NAME, new TargetOrImmediateInstructionEmitter(FloatSubInstruction::new, FloatSubImmediateInstruction::new));
        //builder.put(FloatMulInstruction.NAME, new TargetOrImmediateInstructionEmitter(FloatMulInstruction::new, FloatMulImmediateInstruction::new));
        //builder.put(FloatDivInstruction.NAME, new TargetOrImmediateInstructionEmitter(FloatDivInstruction::new, FloatDivImmediateInstruction::new));

        //// Floating point control flow
        //builder.put(FloatJumpEqualZeroInstruction.NAME, new LabelInstructionEmitter(FloatJumpEqualZeroInstruction::new));
        //builder.put(FloatJumpNotZeroInstruction.NAME, new LabelInstructionEmitter(FloatJumpNotZeroInstruction::new));
        //builder.put(FloatJumpGreaterThanZeroInstruction.NAME, new LabelInstructionEmitter(FloatJumpGreaterThanZeroInstruction::new));
        //builder.put(FloatJumpLessThanZeroInstruction.NAME, new LabelInstructionEmitter(FloatJumpLessThanZeroInstruction::new));

        //// Float <-> Int conversion operations
        //builder.put(IntToFloatInstruction.NAME, new UnaryInstructionEmitter(() -> IntToFloatInstruction.INSTANCE));
        //builder.put(FloatToIntInstruction.NAME, new UnaryInstructionEmitter(() -> FloatToIntInstruction.INSTANCE));
        TISString.LOGGER.info("Finished reg custom ops");
        return builder;
    }
}