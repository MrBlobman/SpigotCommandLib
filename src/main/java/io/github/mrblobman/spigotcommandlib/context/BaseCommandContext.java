package io.github.mrblobman.spigotcommandlib.context;

import java.util.UUID;

public abstract class BaseCommandContext<T> implements CommandContext<T> {
    private final UUID owner;
    private final T data;

    public BaseCommandContext(UUID owner, T data) {
        this.owner = owner;
        this.data = data;
    }

    @Override
    public UUID getOwner() {
        return this.owner;
    }

    @Override
    public T getData() {
        return this.data;
    }
}
