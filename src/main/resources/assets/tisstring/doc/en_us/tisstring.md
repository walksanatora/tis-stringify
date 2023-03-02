# TIS Advanced

TIS Advanced features include the [ASIC Module](modules/string_module.md), as well as the following additional instructions for the Execution Module:

## Floating Point Arithmetic Instructions

`ADDF`, `SUBF`, `MULF`, and `DIVF` behave like their integer counterparts, however, they operate on `ACC` as though the data contained within represents an IEEE-754 compliant half-precision floating point number. 

Note that it is possible to store an integer value in `ACC` and attempt floating point calculations on it, and vice versa. This is likely to produce incorrect values. It is the user's responsibility to ensure that operations are only performed on the correct data representation.

## Floating Point Flow Control Instructions

The `JEZF`, `JNZF`, `JGZF`, and `JLZF` instructions behave like their integer counterparts `JEZ`, `JNZ`, `JGZ`, and `JLZ`, however, like the floating point arithmetic operations above, they operate on `ACC` as though the value contained within is an IEEE-754 half-precision float.

## Floating Point Conversion Instructions

The `FLT` instruction will convert an integer value stored in `ACC` to a IEEE-754 half-precision floating point representation. The `INT` instruction will convert a floating point value stored in `ACC` to a 16-bit signed two's complement integer representation; the same representation used by standard TIS-3D arithmetic operations. Converting to integer representation will round the floating point value to the nearest whole number, with ties rounding upwards.