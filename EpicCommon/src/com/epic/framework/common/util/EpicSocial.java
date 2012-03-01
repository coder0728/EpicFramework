/**
 * @author Derek Johnson
 * 
 * EpicSocial provides for a set of social network features for consumers. The initial
 * implementation focuses on Facebook, and provides simple functions for logging in,
 * requesting friend lists, posting to a users timeline, and sending app requests.
 * 
 * For more advanced users, functions are provided to directly pass commands through 
 * to the Facebook Graph API, and return raw JSON results.
 * 
 */

package com.epic.framework.common.util;

public class EpicSocial {
	/**
	 * Begins the asynchronous login process to Facebook, using Single Sign On (SSO), 
	 * if available. This method should generally not be called directly, as other
	 * Facebook related utility methods in EpicSocial will handle login transparently.
	 * 
	 * For customers without the Facebook client installed, a browser window will appear
	 * to log in. For users with the client, SSO will be used to log in.
	 * 
	 * @param socialDelegate Delegate with a proper implementation of 
	 * EpicSocialDelegate.onLoggedInToFacebook(EpicFacebookUser facebookUser)
	 * 
	 */
	public void beginLoginToFacebook() {}
	
	public void postToFacebookWallDialog() {}
	
	public void sendFacebookAppRequestsDialog() {}
	
	public void getFacebookFriendList() {}
	
	public void sendRawFacebookGraphApiRequest(String action) {}
	
}


//package com.epic.framework.common.util;
//
//import com.epic.framework.common.Ui.EpicClickListener;
//import com.epic.framework.common.Ui.EpicNotification;
//import com.epic.framework.common.Ui.EpicPlatform;
//import com.epic.framework.common.Ui.EpicScreen;
//import com.epic.framework.implementation.EpicSocialImplementation;
//import com.epic.resources.EpicImages;
//import com.realcasualgames.words.PlayerState;
//import com.realcasualgames.words.ScreenMainMenu;
//import com.realcasualgames.words.WordsHttp;
//
//public class EpicSocial {
//	public static String getIdentity() {
//		return PlayerState.getIdentity();
//	}
//
//	public interface EpicSocialSignInCompletionHandler {
//		void onSignedIn(String identity);
//	}
//	
//	public static boolean supportsFacebookPost() {
//		return EpicSocialImplementation.supportsFacebookPost();
//	}
//	
//	public static void postToFacebook(String title, String url, String caption, String imageUrl, EpicClickListener callback) {
//		if(supportsFacebookPost()) EpicSocialImplementation.postToFacebook(title, url, caption, imageUrl, callback);
//	}
//
//	public static void signIn(final EpicSocialSignInCompletionHandler doAfter) {
//		if(PlayerState.getIdentity() == null) {
//			EpicLog.i("PLAYER IDENTITY REQUESTED");
//			EpicSocialImplementation.beginLogin(new EpicSocialSignInCompletionHandler() {
//				public void onSignedIn(String identity) {
//					onSignInComplete(identity, null, null);
//					
//					if(doAfter != null) {
//						doAfter.onSignedIn(identity);
//					}
//				}
//
//			});
//		}
//		else {
//			doAfter.onSignedIn(PlayerState.getIdentity());
//		}
//	}
//	
//	public static void onSignInComplete(String identity, String displayName, String fbid) {
//		EpicLog.i("PLAYER IDENTITY CHOSEN: '" + identity + "'");
//		String un = "";
//		if(displayName != null) {
//			un = displayName;
//		} else if(identity.contains("@")) {
//			un = identity.split("@")[0];
//		} else {
//			un = identity;
//		}
//		
//		EpicNotification n = new EpicNotification("Welcome to Word Farm!", new String[] { "You are now logged in." }, EpicImages.icon_cow);
//		// EpicPlatform.doToastNotification("Welcome to Word Farm, " + identity + "!", 3000);
//		EpicPlatform.doToastNotification(n);
//		PlayerState.setIdentityWithFacebookId(identity, displayName, fbid);
//		if(fbid != null) {
//			PlayerState.setFBID(fbid);
//			EpicLog.v("Set FBID: " + fbid);
//		}
//		
//		WordsHttp.syncAccount(new EpicHttpResponseHandler() {
//			public void handleResponse(EpicHttpResponse response) {	
//				EpicLog.i("Account sync complete.");
//				EpicPlatform.repaintScreen();
//			}
//
//			public void handleFailure(Exception e) {
//				EpicLog.w("Failure to call syncAccount on the remote service");
//			}
//		});
//		
//		if(EpicSocialImplementation.friendList != null) {
//			EpicSocialImplementation.searchFriendList(EpicSocialImplementation.friendList);
//		} else {
//			EpicLog.i("Friends list still null when signing in...");
//		}
//	}
//	
//	public static void switchUser(final EpicSocialSignInCompletionHandler doAfter) {
//		EpicLog.i("PLAYER IDENTITY SWITCH REQUESTED");
//		EpicSocialImplementation.beginLogin(new EpicSocialSignInCompletionHandler() {
//			public void onSignedIn(String identity) {
//				EpicLog.i("NEW PLAYER IDENTITY CHOSEN: '" + identity + "'");
//				EpicNotification n = new EpicNotification("Welcome to Word Farm!", new String[] { "You are now logged in." }, EpicImages.icon_cow);
//				//EpicPlatform.doToastNotification("Welcome to Word Farm, " + identity + "!", 3000);
//				EpicPlatform.doToastNotification(n);
//				PlayerState.setIdentity(identity);
//				if(doAfter != null) {
//					doAfter.onSignedIn(identity);
//				}
//			}
//		});
//	
//	}
//	
//	public static boolean isLoggedIn() {
//		return PlayerState.getIdentity() != null;
//	}
//
//	public static String getEmailList() {
//		return EpicSocialImplementation.getEmailList();
//	}
//
//	public static String getPlatformId() {
//		return EpicSocialImplementation.getPlatformId();
//	}
//	
//	public static String getDisplayNameFromEmail(String email) {
//		return EpicSocialImplementation.getDisplayNameFromEmail(email);
//	}
//
//	public static String[] getDisplayNamesFromEmails(String[] names_to_lookup) {
//		return names_to_lookup;
//		// return EpicSocialImplementation.getDisplayNamesFromEmails(names_to_lookup);
//	}
//
////	public static String chooseContact() {
////		return EpicSocialImplementation.chooseContact();
////	}
//
//	public static void onContactEmailReturned(String[] strings) {
//		EpicSocialImplementation.selectFromEmailList(strings);
//	}
//
//	public static void signOut() {
//		if(PlayerState.canLogOut()) {
//			PlayerState.logOut();
//			EpicLog.i("PLAYER LOGGED OUT");
//			EpicPlatform.doToastNotification(new EpicNotification("Logged Out", new String[] { "You have successfully logged out." }, EpicImages.icon_cow));
//			EpicPlatform.repaintScreen();
//		} else {
//			EpicPlatform.doToastNotification(new EpicNotification("Problem Logging Out", new String[] { "You cannot log out until you sync your latest scores.", "Please complete a game and try again." }, EpicImages.icon_cow, 5));
//		}
//	}
//
//	public static void togglePush(boolean pushEnabled) {
//		EpicSocialImplementation.togglePush(pushEnabled);
//	}
//
//	public static void getFacebookFriendList() {
//		EpicSocialImplementation.getFacebookFriendList();
//	}
//
//	public static void showAchievements() {
//		EpicSocialImplementation.showAchievements();
//	}
//
//	public static void viewChallenges(int defaultListLength, String response) {
//		EpicSocialImplementation.viewChallenges(defaultListLength, response);
//	}
//
//	public static void viewChallenges(int defaultListLength) {
//		EpicSocialImplementation.viewChallenges(defaultListLength);
//	}
//
//	public static void promptFacebookLogin(EpicScreen screen) {
//		EpicSocialImplementation.promptFacebookLogin(screen);
//	}
//}
