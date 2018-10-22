package io.github.mrblobman.spigotcommandlib.context;

import java.util.UUID;

public interface ContextProvider<T> {
    public CommandContext<T> getContextInstance(UUID owner);

    // The returned context must not be destroy()ed already.
    public CommandContext<T> getOrCreateContextInstance(UUID owner);
}
