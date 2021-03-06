package com.epic.framework.implementation;

import com.epic.framework.common.util.exceptions.EpicNativeMethodMissingImplementation;

public abstract class EpicPlatformImplementationNative {	
	public static void setupDebugHandlers() {
		throw new EpicNativeMethodMissingImplementation("EpicPlatformImplementationNative");
	}
	public static String getUniqueDeviceId() {
		throw new EpicNativeMethodMissingImplementation("EpicPlatformImplementationNative");
	}
	public static String getDeviceName() {
		throw new EpicNativeMethodMissingImplementation("EpicPlatformImplementationNative");
	}
	public static void runOnUiThread(Runnable runnable) {
		throw new EpicNativeMethodMissingImplementation("EpicPlatformImplementationNative");
	}
	public static void loginToFacebook() {
		throw new EpicNativeMethodMissingImplementation("EpicPlatformImplementationNative");
	}
	public static int isNetworkAvailable() {
		throw new EpicNativeMethodMissingImplementation("EpicPlatformImplementationNative");
	}
	public static void postToFacebook(String fbMessage) {
		throw new EpicNativeMethodMissingImplementation("EpicPlatformImplementationNative");
	}
	public static void setAppBadge(int newCount) {
		throw new EpicNativeMethodMissingImplementation("EpicPlatformImplementationNative");		
	}
	public static void launchBrowserTo(String string) {
		throw new EpicNativeMethodMissingImplementation("EpicPlatformImplementationNative");				
	}
	public static void requestFacebookFriends(String message) {
		throw new EpicNativeMethodMissingImplementation("EpicPlatformImplementationNative");				
	}
	public static void requestPurchase(String whichItem) {
		throw new EpicNativeMethodMissingImplementation("EpicPlatformImplementationNative");
	}
}
