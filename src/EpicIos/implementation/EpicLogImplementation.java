package com.epic.framework.implementation;

import org.xmlvm.iphone.NSLog;

import com.epic.framework.common.util.exceptions.EpicRuntimeException;

public class EpicLogImplementation {
	public static final void log(String tag, String msg, int level, Throwable e) {
		NSLog.log(msg);
		if(e instanceof EpicRuntimeException) {
			EpicRuntimeException ere = (EpicRuntimeException)e;
			NSLog.log(ere.className + ": " + ere.message);
		}
		if(e != null) {
			logStack();
		}
	}

	public static void logStack() {
		NSLog.logStack();
	}
}
