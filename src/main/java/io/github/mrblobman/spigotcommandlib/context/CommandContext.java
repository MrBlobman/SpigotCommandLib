package io.github.mrblobman.spigotcommandlib.context;

import java.util.UUID;

public interface CommandContext<T> {
    public UUID getOwner();

    public T getData();

    public void destroy();
}
