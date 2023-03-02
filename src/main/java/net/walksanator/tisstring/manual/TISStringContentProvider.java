package net.walksanator.tisstring.manual;

import java.util.Objects;

import li.cil.manual.api.ManualModel;
import li.cil.manual.api.prefab.provider.NamespaceDocumentProvider;
import li.cil.tis3d.client.manual.Manuals;

public class TISStringContentProvider extends NamespaceDocumentProvider {

    public TISStringContentProvider(String namespace, String basePath) {
        super(namespace, basePath);
    }

    public TISStringContentProvider(final String namespace) {
        super(namespace);
    }

    @Override
    public boolean matches(ManualModel manual) {
        return Objects.equals(manual, Manuals.MANUAL.get());
    }

    @Override
    public int sortOrder() {
        return Integer.MAX_VALUE;
    }
}
