package net.walksanator.tisstring.manual;

import java.util.Objects;

import li.cil.manual.api.ManualModel;
import li.cil.manual.api.prefab.provider.NamespacePathProvider;
import li.cil.tis3d.client.manual.Manuals;
import org.jetbrains.annotations.NotNull;

public class TISStringPathProvider extends NamespacePathProvider {

    public TISStringPathProvider(String namespace) {
        this(namespace, false);
    }

    public TISStringPathProvider(String namespace, boolean keepNamespaceInPath) {
        super(namespace, keepNamespaceInPath);
    }

    @Override
    public boolean matches(@NotNull ManualModel manual) {
        return Objects.equals(manual, Manuals.MANUAL.get());
    }

    @Override
    public int sortOrder() {
        return Integer.MAX_VALUE;
    }
}
