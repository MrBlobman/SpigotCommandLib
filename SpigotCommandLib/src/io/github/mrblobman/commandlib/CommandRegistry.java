package io.github.mrblobman.commandlib;

import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Method;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.plugin.Plugin;

public class CommandRegistry {
	Plugin pl;
	public void register(Class<?> commandHandler) {
		MethodLoop:
			for (Method method : commandHandler.getMethods()) {
				SubCommandHandler handlerAnnotation = method.getAnnotation(SubCommandHandler.class);
				AnnotatedType[] methodParams = method.getAnnotatedParameterTypes();
				ArgumentFormatter<?>[] formatters = new ArgumentFormatter<?>[methodParams.length-1];
				for (int i = 1; i < methodParams.length-(method.isVarArgs() ? 2 : 1); i++) {
					Class<?> paramType = methodParams[i].getType().getClass();
					if (paramType.equals(String.class)) {
						formatters[i-1] = ArgumentFormatter.STRING;
					} else if (paramType.equals(Integer.class) || paramType.equals(Integer.TYPE)) {
						formatters[i-1] = ArgumentFormatter.INTEGER;
					} else if (paramType.equals(Double.class) || paramType.equals(Double.TYPE)) {
						formatters[i-1] = ArgumentFormatter.DOUBLE;
					} else if (paramType.equals(Long.class) || paramType.equals(Long.TYPE)) {
						formatters[i-1] = ArgumentFormatter.LONG;
					} else if (paramType.equals(Short.class) || paramType.equals(Short.TYPE)) {
						formatters[i-1] = ArgumentFormatter.SHORT;
					} else if (paramType.equals(Float.class) || paramType.equals(Float.TYPE)) {
						formatters[i-1] = ArgumentFormatter.FLOAT;
					} else if (paramType.equals(Color.class)) {
						formatters[i-1] = ArgumentFormatter.COLOR;
					} else {
						Bukkit.getLogger().log(Level.WARNING, "Cannot register method "+method.getName()+". Unknown parameter parse type ("+paramType.getName()+"). Accepted types are String, Integer, Long, Short, Double, Float and org.bukkit.Color.");
						continue MethodLoop;
					}
				}
				if (method.isVarArgs()) {
					//We need to handle the last arg differently because it is a var arg
					Class<?> paramType = methodParams[methodParams.length-1].getType().getClass();
					if (paramType.equals(String[].class)) {
						formatters[formatters.length-1] = ArgumentFormatter.STRING;
					} else if (paramType.equals(Integer[].class) || paramType.equals(Integer.TYPE)) {
						formatters[formatters.length-1] = ArgumentFormatter.INTEGER;
					} else if (paramType.equals(Double.class) || paramType.equals(Double.TYPE)) {
						formatters[formatters.length-1] = ArgumentFormatter.DOUBLE;
					} else if (paramType.equals(Long.class) || paramType.equals(Long.TYPE)) {
						formatters[formatters.length-1] = ArgumentFormatter.LONG;
					} else if (paramType.equals(Short.class) || paramType.equals(Short.TYPE)) {
						formatters[formatters.length-1] = ArgumentFormatter.SHORT;
					} else if (paramType.equals(Float.class) || paramType.equals(Float.TYPE)) {
						formatters[formatters.length-1] = ArgumentFormatter.FLOAT;
					} else if (paramType.equals(Color.class)) {
						formatters[formatters.length-1] = ArgumentFormatter.COLOR;
					} else {
						Bukkit.getLogger().log(Level.WARNING, "Cannot register method "+method.getName()+". Unknown parameter parse type ("+paramType.getName()+"). Accepted types are String, Integer, Long, Short, Double, Float and org.bukkit.Color.");
						continue MethodLoop;
					}
				}
			}
	}
}
