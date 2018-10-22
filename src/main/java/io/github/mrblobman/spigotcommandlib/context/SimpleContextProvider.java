package io.github.mrblobman.spigotcommandlib.context;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class SimpleContextProvider<T> implements ContextProvider<T> {
    private class SimpleCommandContext extends BaseCommandContext<T> {
        public SimpleCommandContext(UUID owner, T data) {
            super(owner, data);
        }

        @Override
        public void destroy() {
            SimpleContextProvider.this.active.remove(this.getOwner(), this);
        }
    }

    private final Supplier<T> dataSupplier;
    private final Map<UUID, SimpleCommandContext> active;

    public SimpleContextProvider(Supplier<T> dataSupplier) {
        this.dataSupplier = dataSupplier;
        this.active = new ConcurrentHashMap<>();
    }

    @Override
    public CommandContext<T> getContextInstance(UUID owner) {
        return this.active.get(owner);
    }

    @Override
    public CommandContext<T> getOrCreateContextInstance(UUID owner) {
        return this.active.computeIfAbsent(owner, id ->
                new SimpleCommandContext(id, dataSupplier.get()));
    }
}
