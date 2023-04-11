package net.walksanator.tisstring.instructions;

import li.cil.tis3d.common.module.execution.Machine;
import li.cil.tis3d.common.module.execution.MachineState;
import li.cil.tis3d.common.module.execution.instruction.Instruction;
import li.cil.tis3d.common.module.execution.target.Target;

public class LoadInstruction implements Instruction {
    public static final String NAME = "LDD";
    public static final Instruction INSTANCE = new LoadInstruction();

    @Override
    public void step(Machine machine) {
        MachineState state = machine.getState();
        state.acc = state.bak;
        ++state.pc;
    }
    public String toString() {
        return "LDD";
    }
}
