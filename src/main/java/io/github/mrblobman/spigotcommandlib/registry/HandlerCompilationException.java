package io.github.mrblobman.spigotcommandlib.registry;

public class HandlerCompilationException extends RuntimeException {
    private final MethodDescriptor method;

    public HandlerCompilationException(MethodDescriptor method, String message) {
        super(message);
        this.method = method;
    }

    public HandlerCompilationException(MethodDescriptor method, String messageFormat, Object... args) {
        super(String.format(messageFormat, args));
        this.method = method;
    }

    public HandlerCompilationException(MethodDescriptor method, String message, Throwable cause) {
        super(message, cause);
        this.method = method;
    }

    @Override
    public String getMessage() {
        return "Error registering " + method.getName() + " in " + method.getDeclaringClass().getSimpleName() + ". Reason: " + super.getMessage();
    }
}
