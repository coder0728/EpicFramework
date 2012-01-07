package com.epic.framework.common.Ui;

import java.util.Collection;
import java.util.LinkedList;
import com.epic.config.EpicProjectConfig;
import com.epic.framework.common.Ui.EpicMenu.EpicMenuItem;
import com.epic.framework.common.types.Dimension;
import com.epic.framework.common.util.EpicFail;
import com.epic.framework.common.util.EpicLog;
import com.epic.framework.implementation.ArchPlatform;
import com.epic.framework.implementation.EpicPlatformConfig;
import com.epic.framework.implementation.EpicPlatformImplementation;
import com.epic.framework.common.util.EpicSoundManager;
import com.epic.framework.common.util.EpicStopwatch;

public class EpicPlatform {
	public static final int PLATFORM_ANDROID = 0;
	public static final int PLATFORM_BLACKBERRY = 1;
	public static final int PLATFORM_IOS = 2;


	
	public static final int TIMER_HZ = 20;
	public static final boolean RMODE_FULLSCREEN = true;
	public static final boolean RMODE_STRETCH = false;
	public static final boolean RMODE_LETTERBOX = false;
	public static final int letterboxWidth = -99;
	public static final int letterboxHeight = -99;
	static int renderWidth = -99;
	static int renderHeight = -99;
	static int truePlatformWidth = -99;
	static int truePlatformHeight = -99;

	
	
	private static final boolean DEBUG = EpicProjectConfig.isReleaseMode ? false : false;
	private static final int NOT_DISPLAYED = -1;
	public static boolean initialized = false;
	public static EpicScreen currentScreen;
	private static EpicPercentLayout epicPercentLayout;
	private static EpicPlatformInterface epicPlatformInterface;
	public static MouseTrail mouseTrail = new MouseTrail();
	static final int designWidth = EpicProjectConfig.designDimensions.width;
	static final int designHeight = EpicProjectConfig.designDimensions.height;
	private static Object singleThreadingLock = new Object();
	private static long lastInputTime = System.currentTimeMillis();
	public static boolean userDragInputEnabled = true;
	private static int timer_slow_factor = 1;
	
	public static LinkedList<EpicNotification> notifications = new LinkedList<EpicNotification>();
	public static LinkedList<EpicScreen> dialogs = new LinkedList<EpicScreen>();
	public static int timeNotificationDisplayed = -1;

	// DDOPSON-2011-09-03 - this timer does NOT run on the ui thread.  thus when ui thread is hung, we still run
	public static void onKillTimerTick() {
		EpicLog.i("kill timer tick");
		if(DEBUG && System.currentTimeMillis() > lastInputTime + 60000) {
			EpicLog.w("KILL_KILL_KILL - kill timer has expired, so nuking the process.");
			System.exit(1); // this kills off hung processes after a minute
		}
	}

	private static long t0;
	private static long tslow_last = -1;
	private static int n_skipped = 0;
	
	private static EpicTimer epicTimer = new EpicTimer() {
		protected void onTimerTick(int n) {
			if(n == 0) {
				t0 = System.currentTimeMillis();
			}
			if(n % timer_slow_factor == 0) {
				long t = System.currentTimeMillis();
				if(timer_slow_factor > 1) {
					// adjust time to account for the fake slowness we are adding.  this is a debug feature
					if(tslow_last == -1) {
						tslow_last = t;
					} else {
						t0 += (t - tslow_last) * (timer_slow_factor - 1) / timer_slow_factor;
					}
				}
				int n_expected = (int)(t - t0) * TIMER_HZ / 1000;
				if(n + n_skipped < n_expected) {
					// DDOPSON-2011-10-15 - we refuse to skip more than two frames ahead without rendering.  
					// in the case of a brutal GC, we will effectively pause the game to avoid screwing the player.
					int toSkip = n_expected - (n + n_skipped + 1);
					n_skipped += toSkip;
					EpicPlatform.onPlatformTimerTick();
					this.n++;
				}
				EpicPlatform.onPlatformTimerTick();
			}
		}
	};
	public static long timeAtScreenChange = -1;

	public interface EpicTestHook {
		public void onPaintFinished();
	}

	public static EpicTestHook epicTestHook = null;

	public static Object getSingleThreadingLock() {
		return singleThreadingLock;
	}

	public static float getSecondsSinceScreenChange() {
		return (System.currentTimeMillis() - timeAtScreenChange) / 1000.0f;
	}

	public static void initialize(EpicPlatformInterface platformInterface, String screen, String extra) {
		ArchPlatform.logMemoryStats();
		if(DEBUG) EpicLog.d("EpicPlatform.initialize(" + platformInterface + ")");
		if(initialized) {
			throw EpicFail.framework("EpicPlatform.initialize() is being called twice!!!");
		}
		EpicPlatform.epicPlatformInterface = platformInterface;
		EpicPlatform.epicPercentLayout = new EpicPercentLayout(EpicProjectConfig.getDesignDimensions(), platformInterface);
		EpicPlatform.currentScreen = EpicProjectConfig.getInitialScreenObject(screen, extra);
		currentScreen.onCreateUi(epicPercentLayout);
		currentScreen.onShow();
		epicTimer.scheduleAtFixedRate(1000 / TIMER_HZ);
		EpicLog.d("EpicPlatform.initialize is done.");
		initialized = true;
	}

	public static boolean isBlackberry() {
		return EpicPlatformConfig.platform == PLATFORM_BLACKBERRY;
	}
	public static boolean isAndroid() {
		return EpicPlatformConfig.platform == PLATFORM_ANDROID;
	}
	public static boolean isIos() {
		return EpicPlatformConfig.platform == PLATFORM_IOS;

	}
	
	public static void changeScreen(EpicScreen screen) {
		if(DEBUG) EpicLog.d("EpicPlatform.changeScreen(" + screen + ")");
		EpicFail.assertNotNull(currentScreen);
		EpicFail.assertNotNull(screen);

		EpicPlatformImplementation.dismissNotifications();
		if(EpicProjectConfig.screenTransitionSound != null) {
			EpicSoundManager.playSound(EpicProjectConfig.screenTransitionSound);
		}

		// Destroy old Ui
		currentScreen.onHide();
		currentScreen.onDestroyUi();
		epicPercentLayout.clear();

		timeAtScreenChange = System.currentTimeMillis();

		// Add new Ui
		currentScreen = screen;
		currentScreen.onCreateUi(epicPercentLayout);
		currentScreen.onShow();
		repaintScreen();
		userDragInputEnabled = true;
		
		EpicPlatform.repaintScreen();
		EpicSoundManager.resumeMusic();
	}

	public static void getPlatformSize(Dimension d) {
		EpicFail.assertTrue(renderWidth > 0 && renderHeight > 0, "platformWidth / platformHeight not initialized yet");
		d.set(renderWidth, renderHeight);
	}

	public static int getPlatformWidth() {
		EpicFail.assertTrue(renderWidth > 0, "platformWidth not initialized yet");
		return renderWidth;
	}

	public static int getPlatformHeight() {
		EpicFail.assertTrue(renderHeight > 0, "platformHeight not initialized yet");
		return renderHeight;
	}

	//	static void requestUiRebuild() {
	//		if(!initialized) {
	//			initialize();
	//		}
	//		else {
	//			EpicPercentLayout bgLayout = new EpicPercentLayout(EpicProjectConfig.getDesignDimensions());
	//			currentLayout = bgLayout;
	//			currentScreen.onCreateUi(bgLayout);
	//		}
	//	}

	public static boolean onPlatformKeyPress(int c) {
		lastInputTime = System.currentTimeMillis();
		synchronized (singleThreadingLock) {
			if(DEBUG) EpicLog.d("EpicPlatform.onPlatformKeyPress(" + c + " '" + (char)c + "')");
			if(dialogs.isEmpty()) {
				return currentScreen.onKeyPress(c);
			} else {
				return dialogs.get(0).onKeyPress(c);
			}
		}
	}

	static boolean onPlatformNavigationClick() {
		lastInputTime = System.currentTimeMillis();
		synchronized (singleThreadingLock) {
			if(DEBUG) EpicLog.d("EpicPlatform.onPlatformNavigationClick()");
			if(dialogs.isEmpty()) {
				return currentScreen.onKeyPress(EpicKeys.ENTER);
			} else {
				return dialogs.get(0).onKeyPress(EpicKeys.ENTER);
			}
		}
	}

	public static boolean onPlatformNavigationMovement(int x, int y) {
		lastInputTime = System.currentTimeMillis();
		synchronized (singleThreadingLock) {
			if(DEBUG) EpicLog.d("EpicPlatform.onPlatformNavigationMovement(" + x + ", " + y + ")");
			if(dialogs.isEmpty()) {
				return currentScreen.onNavigationMovement(x, y);
			} else {
				return dialogs.get(0).onNavigationMovement(x, y);
			}
		}
	}

	public static void onPlatformShow() {
		synchronized (singleThreadingLock) {
			if(DEBUG) EpicLog.d("EpicPlatform.onPlatformShow()");
			EpicSoundManager.resumeMusic();
			if(dialogs.isEmpty()) {
				currentScreen.onShow();
			} else {
				dialogs.get(0).onShow();
			}
		}
	}

	public static void onPlatformHide() {
		synchronized (singleThreadingLock) {
			if(DEBUG) EpicLog.d("EpicPlatform.onPlatformHide()");

			EpicSoundManager.pauseMusic();

			if(dialogs.isEmpty()) {
				currentScreen.onHide();
			} else {
				dialogs.get(0).onHide();
			}
		}
	}

	public static void onPlatformDestroy() {
		synchronized (singleThreadingLock) {
			if(DEBUG) EpicLog.d("EpicPlatform.onPlatformDestroy()");
			// any finalization to be done???  literally, anything???  this can be called more than once, so what does it even mean?  prob nothing
		}
	}

	public static boolean onPlatformBackKey() {
		synchronized (singleThreadingLock) {
			if(DEBUG) EpicLog.d("EpicPlatform.onPlatformBackKey()");
			if(dialogs.isEmpty()) {
				return currentScreen.onBackKey();
			} else {
				return dialogs.get(0).onBackKey();
			}
		}
	}

	public static void repaintScreen() {
		synchronized (singleThreadingLock) {
			//			if(DEBUG) EpicLog.d("EpicPlatform.repaintScreen()");
			 epicPlatformInterface.requestRepaint();
		}
	}

	public static Collection<EpicMenuItem> getMenuItems(int menuType) {
		if(!dialogs.isEmpty()) {
			// dont support menus in dialogs
			return null;
		}
		
		synchronized (singleThreadingLock) {
			if(DEBUG) EpicLog.d("Platform.getMenuItems(" + menuType + ")");
			EpicMenu menu = new EpicMenu();
			EpicMenu debugMenu = new EpicMenu();
			currentScreen.onMenuOpened(menu, debugMenu);
			if(! EpicProjectConfig.isReleaseMode) {
				debugMenu.addItem("DEBUG Rendering", new EpicMenuItem() {
					public void onClicked() {
						EpicCanvas.toggleDebugRendering();
					}
				});
				debugMenu.addItem("Kill process", new EpicMenuItem() {
					public void onClicked() {
						System.exit(0);
					}
				});
				debugMenu.addItem("1X speed", new EpicMenuItem() {
					public void onClicked() {
						timer_slow_factor = 1;
					}
				});
				debugMenu.addItem("0.3X speed", new EpicMenuItem() {
					public void onClicked() {
						timer_slow_factor = 3;
					}
				});
				debugMenu.addItem("0.1X speed", new EpicMenuItem() {
					public void onClicked() {
						timer_slow_factor = 10;
					}
				});
				debugMenu.addItem("0.05X speed", new EpicMenuItem() {
					public void onClicked() {
						timer_slow_factor = 20;
					}
				});
				debugMenu.addItem("0X speed", new EpicMenuItem() {
					public void onClicked() {
						timer_slow_factor = Integer.MAX_VALUE;
					}
				});

			}

			switch(menuType) {
			case EpicMenu.MENU_DEBUG:
				return debugMenu.items;
			case EpicMenu.MENU_GAME:
				return menu.items;
			case EpicMenu.MENU_ALL:
				if(! EpicProjectConfig.isReleaseMode) {
					for(EpicMenuItem menuItem : debugMenu.items) {
						menu.addItem("DEBUG - " + menuItem.name, menuItem);
					}
				}
				return menu.items;
			default:
				throw EpicFail.unhandled_case();
			}
		}
	}

	public static void onPlatformMenuDismissed() {
		synchronized (singleThreadingLock) {
			if(DEBUG) EpicLog.d("Platform.onPlatformMenuDismissed()");
			currentScreen.onMenuClosed();
		}
	}

	public static void doToastNotification(EpicNotification notification) {
		synchronized (singleThreadingLock) {
			notifications.add(notification);
			EpicLog.i("Toasting for: " + notification.title);
			repaintScreen();
			// EpicPlatformImplementation.doToastNotification(text, duration);
		}
	}

	public static void onPlatformPaint(EpicCanvas epicCanvas) {
		synchronized (singleThreadingLock) {
			//			if(DEBUG) EpicLog.d("Platform.paint()");
			EpicStopwatch.paintStart();
			currentScreen.onPaint(epicCanvas, renderWidth, renderHeight, mouseTrail);
			
			if(!dialogs.isEmpty()) {
				dialogs.get(0).onPaint(epicCanvas, renderWidth, renderHeight, mouseTrail);
			}
			
			EpicPlatformImplementation.dismissNotifications();
			if(!notifications.isEmpty()) {
				EpicNotification n = notifications.peek();
				if(timeNotificationDisplayed == NOT_DISPLAYED) {
					if(DEBUG) EpicLog.i("Displaying notification with title " + n.title);
					timeNotificationDisplayed++;
				} else if(timeNotificationDisplayed >= n.duration * EpicPlatform.TIMER_HZ) {
					notifications.remove(0);
					timeNotificationDisplayed = NOT_DISPLAYED;
				} else {
					int alpha;
					if(timeNotificationDisplayed > n.duration * TIMER_HZ - (TIMER_HZ / 2)) {
						alpha = EpicCanvas.calculateTranslationAnimation(255, 0, n.duration * TIMER_HZ - (TIMER_HZ / 2), TIMER_HZ / 2, timeNotificationDisplayed);
					} else {
						alpha = EpicCanvas.calculateTranslationAnimation(0, 255, 0, TIMER_HZ / 2, timeNotificationDisplayed);
					}
					
					if(EpicProjectConfig.toastBackground != null) {
						epicCanvas.drawBitmapWithGlobalAlpha(EpicProjectConfig.toastBackground, EpicNotification.NOTIFICATION_LEFT_PAD, EpicNotification.NOTIFICATION_TOP_PAD, EpicNotification.NOTIFICATION_WIDTH, EpicNotification.NOTIFICATION_HEIGHT, alpha);
					}
					//					epicCanvas.applyFill(EpicNotification.NOTIFICATION_LEFT_PAD, EpicNotification.NOTIFICATION_TOP_PAD, EpicNotification.NOTIFICATION_WIDTH, EpicNotification.NOTIFICATION_HEIGHT, EpicColor.withAlpha(alpha, EpicColor.BLACK));
//					epicCanvas.drawBorder(EpicNotification.NOTIFICATION_LEFT_PAD, EpicNotification.NOTIFICATION_TOP_PAD, EpicNotification.NOTIFICATION_WIDTH, EpicNotification.NOTIFICATION_HEIGHT, EpicColor.withAlpha(alpha, EpicColor.GREEN), 4);
					
					int textWidth = n.icon == null ? 640 : 510;
					int textLeftPad = n.icon == null ? 20 : 100;
					
					epicCanvas.drawText(n.title, EpicNotification.NOTIFICATION_LEFT_PAD + textLeftPad, EpicNotification.NOTIFICATION_TOP_PAD + 5, textWidth, 30, EpicFont.FONT_MAIN.findBestFittingFont(n.title, textWidth, 30), EpicColor.withAlpha(alpha, EpicColor.WHITE), EpicFont.HALIGN_LEFT, EpicFont.VALIGN_CENTER);
					
					if(n.messages != null && n.messages.length > 0) {
						int whichMessage = timeNotificationDisplayed / (n.duration * TIMER_HZ / n.messages.length);
						if(whichMessage >= n.messages.length) whichMessage = n.messages.length - 1;
						epicCanvas.drawTextBox(n.messages[whichMessage], EpicNotification.NOTIFICATION_LEFT_PAD + textLeftPad, EpicNotification.NOTIFICATION_TOP_PAD + 40, textWidth, 30, EpicFont.FONT_MAIN.withSize(24), EpicColor.withAlpha(alpha, EpicColor.LTGRAY));
					}
					
					if(n.icon != null) {
						epicCanvas.drawBitmapWithGlobalAlpha(n.icon, EpicNotification.NOTIFICATION_LEFT_PAD + 20, EpicNotification.NOTIFICATION_TOP_PAD + 10, 64, 64, alpha);
					}
				}
			}
			
			EpicStopwatch.paintFinish();
			if(epicTestHook != null) {
				epicTestHook.onPaintFinished();
			}
		}
	}

	public static void onPlatformTouchEvent(int _x, int _y) {
		lastInputTime = System.currentTimeMillis();
		int x = _x * EpicPlatform.designWidth / EpicPlatform.truePlatformWidth;
		int y = _y * EpicPlatform.designHeight / EpicPlatform.truePlatformHeight;
		
		if(timeNotificationDisplayed > TIMER_HZ * 1 && notifications.peek() != null &&
				x >= EpicNotification.NOTIFICATION_LEFT_PAD && x <= EpicNotification.NOTIFICATION_LEFT_PAD + EpicNotification.NOTIFICATION_WIDTH &&
				y >= EpicNotification.NOTIFICATION_TOP_PAD && y <= EpicNotification.NOTIFICATION_HEIGHT) {
			timeNotificationDisplayed = (notifications.peek().duration * TIMER_HZ) - (TIMER_HZ / 3);
		}
		
		
		if(userDragInputEnabled) {
			synchronized (singleThreadingLock) {
				if(DEBUG) EpicLog.d("Platform.onPlatformTouchEvent(" + _x + ", " + _y + ") => (" + x + ", " + y + ")");
				mouseTrail.add(x, y);
			}
		}
	}
	
	public static void showDialog(EpicScreen dialog) {
		dialogs.add(dialog);
		repaintScreen();
	}
	
	public static void dismissDialog() {
		// Dismiss current dialog
		dialogs.removeLast();
		repaintScreen();
	}

	public static void onPlatformTouchFinished(int _x, int _y) {
		lastInputTime = System.currentTimeMillis();

		int x = _x * EpicPlatform.designWidth / EpicPlatform.truePlatformWidth;
		int y = _y * EpicPlatform.designHeight / EpicPlatform.truePlatformHeight;

		if(timeNotificationDisplayed > TIMER_HZ * 1 && notifications.peek() != null &&
				x >= EpicNotification.NOTIFICATION_LEFT_PAD && x <= EpicNotification.NOTIFICATION_LEFT_PAD + EpicNotification.NOTIFICATION_WIDTH &&
				y >= EpicNotification.NOTIFICATION_TOP_PAD && y <= EpicNotification.NOTIFICATION_HEIGHT) {
			if(notifications.peek().clickCallback != null) {
				notifications.peek().clickCallback.onClick();
			}
			
			return;
		}
		
		synchronized (singleThreadingLock) {
			if(DEBUG) EpicLog.d("Platform.onPlatformTouchFinished(" + x + ", " + y + ")");
			if(mouseTrail.size() > 5) {
				if(userDragInputEnabled) {
					if(dialogs.isEmpty()) {
						currentScreen.onDragFinished(mouseTrail);
					} else {
						dialogs.get(0).onDragFinished(mouseTrail);
					}
				}
			}
			else {
				if(dialogs.isEmpty()) {
					currentScreen.onClick(x, y);
				} else {
					dialogs.get(0).onClick(x, y);
				}
			}
			if(userDragInputEnabled) {
				mouseTrail.clear();
			}
		}
	}

	public static void onPlatformTimerTick() {
		synchronized (singleThreadingLock) {
			if(dialogs.isEmpty()) {
				currentScreen.onTimerTick();
			} else {
				dialogs.get(0).onTimerTick();
			}
			if(!notifications.isEmpty()) {
				timeNotificationDisplayed++;
				repaintScreen();
			}
		}
	}

	public static void onPlatformLayoutRequest(int width, int height, boolean invertWidgetOrder) {
		// DDOPSON-2011-10-15 - this is a ghetto hack to deal with some BB phones that inexplicably load us first as profile and then switch to landscape.  
		// This hack avoids resizing all the images dynamically and then throwing them away (big perf hit).
		if(height > width) {
			EpicLog.w("SCREEN_ORIENTATION_IS_PROFILE - common bug on BB, so preparing for landscape anyways w=" + width + ", h=" + height);
			int t = width;
			width = height;
			height = t;
		}
		synchronized (singleThreadingLock) {
			if(DEBUG) EpicLog.d("EpicPlatform.onPlatformLayoutRequest(" + width + ", " + height + ", " + invertWidgetOrder + ")");
			if(!initialized) {
				throw EpicFail.framework("EpicPlatform has NOT been initialized!!!");
			}
			if(RMODE_FULLSCREEN) {
				renderWidth = width;
				renderHeight = height;
				truePlatformWidth = width;
				truePlatformHeight = height;
			} else if(RMODE_STRETCH) {
				renderWidth = letterboxWidth;
				renderHeight = letterboxHeight;
				truePlatformWidth = width;
				truePlatformHeight = height;
			} else if(RMODE_LETTERBOX) {
				renderWidth = letterboxWidth;
				renderHeight = letterboxHeight;
				truePlatformWidth = letterboxWidth;
				truePlatformHeight = letterboxHeight;				
			}
			epicPercentLayout.doTheLayout(width, height, invertWidgetOrder);
		}
	}

	public static void runOnUiThread(Runnable runnable) {
		EpicPlatformImplementation.runOnUiThread(runnable);
	}

	public static void runInBackground(Runnable runnable) {
		EpicPlatformImplementation.runInBackground(runnable);
	}

	public static final int scaleLogicalToRenderX(int x) {
		return x * renderWidth / designWidth;
	}
	public static final int scaleLogicalToRenderY(int y) {
		return y * renderHeight / designHeight;
	}
	public static final int scaleLogicalToRealX(int x) {
		return x * truePlatformWidth / designWidth;
	}
	public static final int scaleLogicalToRealY(int y) {
		return y * truePlatformHeight / designHeight;
	}
	
	public static final int scaleRenderToLogicalX(int x) {
		return x * designWidth / renderWidth;
	}
	public static final int scaleRenderToLogicalY(int y) {
		return y * designHeight / renderHeight;
	}
	public static final int scaleRealToLogicallX(int x) {
		return x * designWidth / truePlatformWidth;
	}
	public static final int scaleRealToLogicalY(int y) {
		return y * designHeight / truePlatformHeight;
	}

	public static void pauseTimer() {
		epicTimer.pause();
	}
	
	public static void resumeTimer() {
		epicTimer.resume();
	}

	public static void clearNotifications() {
		timeNotificationDisplayed = NOT_DISPLAYED;
		notifications.clear();
	}

	public static boolean isTouchEnabledDevice() {
		return EpicPlatformImplementation.isTouchEnabledDevice();
	}

	public static boolean isFunkySmallNonTouchDevice() {
		// wtf?  - some strange hacks Derek uses
		return getPlatformWidth() < 800 && !EpicPlatform.isTouchEnabledDevice();
	}

	public static void androidLaunchMarketplace(String string) {
		// TODO Auto-generated method stub
		
	}

	public static String getApplicationVersion() {
		return EpicPlatformImplementation.getApplicationVersion();
	}

	public static String getListingId() {
		return EpicPlatformImplementation.getListingId();
	}

	public static String getUniqueDeviceId() {
		return EpicPlatformImplementation.getUniqueDeviceId();
	}
}
