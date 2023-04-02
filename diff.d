diff --git a/src/main/java/net/walksanator/tisstring/CasingPeripheral.java b/src/main/java/net/walksanator/tisstring/CasingPeripheral.java
index 50b89fe..703c5fb 100644
--- a/src/main/java/net/walksanator/tisstring/CasingPeripheral.java
+++ b/src/main/java/net/walksanator/tisstring/CasingPeripheral.java
@@ -6,6 +6,8 @@ import dan200.computercraft.api.peripheral.IPeripheral;
 import dan200.computercraft.shared.util.CapabilityUtil;
 import li.cil.tis3d.api.machine.Face;
 import li.cil.tis3d.api.prefab.module.AbstractModule;
+import li.cil.tis3d.common.module.RandomAccessMemoryModule;
+import li.cil.tis3d.common.module.ReadOnlyMemoryModule;
 import li.cil.tis3d.common.tileentity.CasingTileEntity;
 import li.cil.tis3d.common.tileentity.ControllerTileEntity;
 import net.minecraft.core.Direction;
@@ -18,6 +20,8 @@ import net.minecraftforge.event.AttachCapabilitiesEvent;
 import net.minecraftforge.eventbus.api.SubscribeEvent;
 import net.minecraftforge.fml.common.Mod;
 import net.walksanator.tisstring.modulePeripheral.InteropModulePeripheral;
+import net.walksanator.tisstring.modulePeripheral.RAMModulePeripheral;
+import net.walksanator.tisstring.modulePeripheral.ROMModulePeripheral;
 import net.walksanator.tisstring.modules.InteropModule.InteropModule;
 import org.jetbrains.annotations.NotNull;
 import org.jetbrains.annotations.Nullable;
@@ -43,6 +47,8 @@ public class CasingPeripheral implements IPeripheral, ICapabilityProvider {
 
     static {
         ModulePeripheralImpls.put(InteropModule.class, InteropModulePeripheral::newTable);
+        ModulePeripheralImpls.put(RandomAccessMemoryModule.class, RAMModulePeripheral::newTable);
+        ModulePeripheralImpls.put(ReadOnlyMemoryModule.class, ROMModulePeripheral::newTable);
     }
 
     @LuaFunction
diff --git a/src/main/java/net/walksanator/tisstring/modulePeripheral/RAMModulePeripheral.java b/src/main/java/net/walksanator/tisstring/modulePeripheral/RAMModulePeripheral.java
index 691c21f..d5bc1fb 100644
--- a/src/main/java/net/walksanator/tisstring/modulePeripheral/RAMModulePeripheral.java
+++ b/src/main/java/net/walksanator/tisstring/modulePeripheral/RAMModulePeripheral.java
@@ -1,2 +1,45 @@
-package net.walksanator.tisstring.modulePeripheral;public class RAMModulePeripheral {
+package net.walksanator.tisstring.modulePeripheral;
+
+import dan200.computercraft.api.lua.IArguments;
+import dan200.computercraft.api.lua.ILuaFunction;
+import dan200.computercraft.api.lua.LuaException;
+import dan200.computercraft.api.lua.MethodResult;
+import li.cil.tis3d.api.prefab.module.AbstractModule;
+import li.cil.tis3d.common.module.RandomAccessMemoryModule;
+import org.jetbrains.annotations.NotNull;
+
+import java.lang.reflect.Field;
+import java.util.Map;
+
+public class RAMModulePeripheral {
+
+    public static Object newTable(AbstractModule mod) {
+        if (mod instanceof RandomAccessMemoryModule ramm) {
+            return Map.of(
+                    "peek", new peek(ramm)
+            );
+        } else return new Object[]{};
+    }
+
+    private static class peek implements ILuaFunction {
+
+        private final RandomAccessMemoryModule parent;
+        public peek(RandomAccessMemoryModule parent) {
+            this.parent = parent;
+        }
+
+        @NotNull
+        @Override
+        public MethodResult call(@NotNull IArguments iArguments) throws LuaException {
+            try {
+                Field memoryField = RandomAccessMemoryModule.class.getDeclaredField("memory");
+                memoryField.setAccessible(true);
+                byte[] memory = (byte[]) memoryField.get(parent);
+                int idx = iArguments.getInt(0);
+                return MethodResult.of(memory[idx & 255] & 255);
+            } catch (NoSuchFieldException | IllegalAccessException e) {
+                throw new LuaException("Failed to reflect value from ram module");
+            }
+        }
+    }
 }
diff --git a/src/main/java/net/walksanator/tisstring/modulePeripheral/ROMModulePeripheral.java b/src/main/java/net/walksanator/tisstring/modulePeripheral/ROMModulePeripheral.java
index d5bc1fb..29b34d6 100644
--- a/src/main/java/net/walksanator/tisstring/modulePeripheral/ROMModulePeripheral.java
+++ b/src/main/java/net/walksanator/tisstring/modulePeripheral/ROMModulePeripheral.java
@@ -6,25 +6,26 @@ import dan200.computercraft.api.lua.LuaException;
 import dan200.computercraft.api.lua.MethodResult;
 import li.cil.tis3d.api.prefab.module.AbstractModule;
 import li.cil.tis3d.common.module.RandomAccessMemoryModule;
+import li.cil.tis3d.common.module.ReadOnlyMemoryModule;
 import org.jetbrains.annotations.NotNull;
 
 import java.lang.reflect.Field;
 import java.util.Map;
 
-public class RAMModulePeripheral {
+public class ROMModulePeripheral {
 
     public static Object newTable(AbstractModule mod) {
-        if (mod instanceof RandomAccessMemoryModule ramm) {
+        if (mod instanceof ReadOnlyMemoryModule romm) {
             return Map.of(
-                    "peek", new peek(ramm)
+                    "peek", new peek(romm)
             );
         } else return new Object[]{};
     }
 
     private static class peek implements ILuaFunction {
 
-        private final RandomAccessMemoryModule parent;
-        public peek(RandomAccessMemoryModule parent) {
+        private final ReadOnlyMemoryModule parent;
+        public peek(ReadOnlyMemoryModule parent) {
             this.parent = parent;
         }
 
@@ -38,7 +39,7 @@ public class RAMModulePeripheral {
                 int idx = iArguments.getInt(0);
                 return MethodResult.of(memory[idx & 255] & 255);
             } catch (NoSuchFieldException | IllegalAccessException e) {
-                throw new LuaException("Failed to reflect value from ram module");
+                throw new LuaException("Failed to reflect value from rom module");
             }
         }
     }
diff --git a/src/main/java/net/walksanator/tisstring/modules/parsemodule/ParseModuleItem.java b/src/main/java/net/walksanator/tisstring/modules/parsemodule/ParseModuleItem.java
index 42f4450..61e42fc 100644
--- a/src/main/java/net/walksanator/tisstring/modules/parsemodule/ParseModuleItem.java
+++ b/src/main/java/net/walksanator/tisstring/modules/parsemodule/ParseModuleItem.java
@@ -13,6 +13,7 @@ import net.minecraft.world.entity.player.Player;
 import net.minecraft.world.item.ItemStack;
 import net.minecraft.world.item.TooltipFlag;
 import net.minecraft.world.level.Level;
+import org.jetbrains.annotations.NotNull;
 
 import javax.annotation.Nullable;
 import java.util.List;
@@ -24,7 +25,7 @@ public class ParseModuleItem extends ModuleItem {
     }
 
     @Override
-    public InteractionResultHolder<ItemStack> use(final Level level, final Player player, final InteractionHand hand) {
+    public @NotNull InteractionResultHolder<ItemStack> use(final @NotNull Level level, final Player player, final @NotNull InteractionHand hand) {
         ItemStack stack = player.getItemInHand(hand);
         Tuple<ParseModule.MODE,ParseModule.ERR> data = loadFromStack(stack);
 
diff --git a/src/main/resources/assets/tisstring/doc/en_us/modules/interop_module.md b/src/main/resources/assets/tisstring/doc/en_us/modules/interop_module.md
index d96298f..0aeed21 100644
--- a/src/main/resources/assets/tisstring/doc/en_us/modules/interop_module.md
+++ b/src/main/resources/assets/tisstring/doc/en_us/modules/interop_module.md
@@ -4,15 +4,7 @@
 (Warnind: this module may not exist, do not be afraid it just means you dont have ComputerCraft installed)
 
 The interopmodule allows you to interact with TIS-3D within CC
-just wrap a casing with this module installed via `peripheral.find("casing")`
-and enjoy the functions, the module is always reading/writing
-the CC-side functions provided are
-`casing.popInt(): Number|nil` pops a number from the interop module (signed integer) returns nil if empty
-`casing.popUint(): Number|nil` pops a unsinged number from the interop module (unsingned int) returns nil if empty
-`casing.popFloat(): Number|nil` pops a 16-bit float from the interop module, returns nil if empty
-`casing.pushInt(Number)` pushes a Int, determines signed or not if it is positive above Short.MAX_VALUE
-`casing.pushFloat(Number)` pushes a Float, expect loss in prescition
-`casing.getLen():Number` returns the current number of values avaliable to be popped from the interop module
-
-Notice: installing mutiple interop modules on the same casing is useless, it will only pick one 
-(and it is "random" as to which)
\ No newline at end of file
+just use the correct face and put it into
+eg:
+`peripheral.call("casing","getFace","Y_POS")`
+this will get you the functions defined in the [peripheral](../peripheral/interop_module.md)
diff --git a/src/main/resources/assets/tisstring/doc/en_us/tisstring.md b/src/main/resources/assets/tisstring/doc/en_us/tisstring.md
index 096b8dd..0e053af 100644
--- a/src/main/resources/assets/tisstring/doc/en_us/tisstring.md
+++ b/src/main/resources/assets/tisstring/doc/en_us/tisstring.md
@@ -6,5 +6,24 @@ TIS Stringify features include the following modules
 * [Parse Module](modules/parse_module.md)
 * [Interop Module](modules/interop_module.md)
 
+casings can be wrapped as CC peripherals
+you get the following functions
+* getFace(string face) -> Table
+  gets you the functions (and other data) associated with that face
+* getFaces() -> [face]
+  returns all valid faces on the casing
+* getModule(string face) -> string
+  gets the module java class (todo: make this itemid)
+* getState() -> string
+  gets the state of the controller managing the casing
+
+functions will return `nil, string` when a error occurs
+the `string` will be the error message
+
+the following modules have Lua functions
+* [Interop Module](peripheral/interop_module.md)
+* [RAM Module](peripheral/ram.md)
+* [ROM Module](peripheral/rom.md)
+
 # TODO: 
 None! suggest something on github
\ No newline at end of file
