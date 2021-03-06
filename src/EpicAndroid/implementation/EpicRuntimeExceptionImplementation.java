package com.epic.framework.implementation;

public class EpicRuntimeExceptionImplementation extends RuntimeException {
	private static final long serialVersionUID = -1998844813797918422L;
	public final String message;
	public final String className;
	public EpicRuntimeExceptionImplementation(String className, String msg, Throwable cause) {
		super(msg, cause);
		this.className = className;
		this.message = msg;
	}
}

