package io.github.mrblobman.spigotcommandlib.registry;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;

public class MethodDescriptor {
    public static MethodDescriptor fromMethod(Method method) {
        return new MethodDescriptor(
                method.getName(),
                method.getDeclaringClass(),
                Arrays.asList(method.getParameters())
        );
    }

    private final String name;
    private final Class declaringClass;
    private final List<Parameter> params;

    public MethodDescriptor(String name, Class declaringClass, List<Parameter> params) {
        this.name = name;
        this.declaringClass = declaringClass;
        this.params = params;
    }

    public String getName() {
        return name;
    }

    public Class getDeclaringClass() {
        return declaringClass;
    }

    public List<Parameter> getParameters() {
        return params;
    }

    public Parameter getParameter(int idx) {
        if (idx < 0 || idx >= this.params.size())
            return null;

        return this.params.get(idx);
    }
}
