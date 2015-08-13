package io.github.mrblobman.commandlib;

import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Method;

public class CommandRegistry {

	public void register(Class<?> commandHandler) {
		for (Method method : commandHandler.getMethods()) {
			SubCommandHandler handlerAnnotation = method.getAnnotation(SubCommandHandler.class);
			AnnotatedType[] methodParams = method.getAnnotatedParameterTypes();
			
		}
	}
}
