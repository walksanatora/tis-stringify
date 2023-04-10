package net.walksanator.tisstring.manual;

import java.util.Objects;

import li.cil.manual.api.ManualModel;
import li.cil.manual.api.prefab.provider.NamespaceDocumentProvider;
import li.cil.tis3d.client.manual.Manuals;
import org.jetbrains.annotations.NotNull;

public class TISStringContentProvider extends NamespaceDocumentProvider {

    public TISStringContentProvider(String namespace, String basePath) {
        super(namespace, basePath);
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
