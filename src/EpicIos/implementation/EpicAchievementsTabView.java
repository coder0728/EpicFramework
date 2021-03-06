package com.epic.framework.implementation;

import java.util.ArrayList;

import org.xmlvm.iphone.NSIndexPath;
import org.xmlvm.iphone.UIColor;
import org.xmlvm.iphone.UIImage;
import org.xmlvm.iphone.UITabBarController;
import org.xmlvm.iphone.UITableView;
import org.xmlvm.iphone.UITableViewController;
import org.xmlvm.iphone.UITableViewDelegate;
import org.xmlvm.iphone.UITableViewStyle;
import org.xmlvm.iphone.UIViewController;

import com.epic.framework.common.Ui.EpicBitmap;
import com.epic.framework.common.Ui.EpicImageBuffer;
import com.epic.framework.common.Ui.EpicPlatform;
import com.epic.resources.EpicImages;
import com.realcasualgames.words.Challenge;
import com.realcasualgames.words.PlayerState;

public class EpicAchievementsTabView extends UITabBarController {
	UITableViewController completed;
	UITableViewController incomplete;
	
	ArrayList<UIViewController> list = new ArrayList<UIViewController>();
	private int iconSize = 30;
	public EpicAchievementsTabView() {
		// Get achievements, and split into 2 lists
		ArrayList<Challenge> completedChallenges = new ArrayList<Challenge>();
		ArrayList<Challenge> incompleteChallenges = new ArrayList<Challenge>();
		
		for(int i = 0; i < Challenge.challenges.length; ++i) {
			if(PlayerState.isChallengeComplete(i)) {
				completedChallenges.add(Challenge.challenges[i]);
			} else {
				incompleteChallenges.add(Challenge.challenges[i]);
			}
		}
		
		ChallengeDataSource ds = getDataSource(completedChallenges, true);
		ChallengeDataSource ids = getDataSource(incompleteChallenges, false);
		
		
		completed = new UITableViewController(UITableViewStyle.Plain);
        UITableView table = completed.getTableView();
        
        table.setDataSource(ds);
        table.setRowHeight(64);
        //table.setSeparatorColor(UIColor.clearColor);
        table.setDelegate(
        		new UITableViewDelegate() {
        			public void didSelectRowAtIndexPath(UITableView tableview, NSIndexPath indexPath) {
        				// noop
        			}
        		});
        
        completed.setTitle("Completed");
        completed.getTabBarItem().setImage((UIImage) EpicImages.game_tomato_gray_tab.getInstance(iconSize , iconSize).platformObject);
                
        incomplete = new UITableViewController(UITableViewStyle.Plain);
        UITableView table2 = incomplete.getTableView();
        
        table2.setDataSource(ids);
        //table2.setSeparatorColor(UIColor.clearColor);
        table2.setRowHeight(64);
        table2.setDelegate(
        		new UITableViewDelegate() {
        			public void didSelectRowAtIndexPath(UITableView tableview, NSIndexPath indexPath) {
        				// noop
        			}
        		});
        
        incomplete.setTitle("Incomplete");
        incomplete.getTabBarItem().setImage((UIImage) EpicImages.close_button.getInstance(iconSize , iconSize).platformObject);
        list.add(incomplete);
        list.add(completed);

        this.setViewControllers(list);
	}
	
	
	private ChallengeDataSource getDataSource(ArrayList<Challenge> challenges, boolean completedList) {
		String[] titles = new String[challenges.size()];
		String[] subtitles = new String[challenges.size()];
		EpicBitmap[] images = new EpicBitmap[challenges.size()];
		boolean[] completed = new boolean[challenges.size()];
		
		for(int i = 0; i < challenges.size(); ++i) {
			titles[i] = challenges.get(i).getTitle() + (!completedList && challenges.get(i).getProgress().length() > 0 ? " (" + challenges.get(i).getProgress() + ")" : "");
			subtitles[i] = challenges.get(i).getDescription();
			images[i] = challenges.get(i).getImage();
			completed[i] = challenges.get(i).isComplete();
		}
		
		return new ChallengeDataSource(titles, subtitles, completed, images);
	}


	@Override
	public boolean shouldAutorotateToInterfaceOrientation(int uiInterfaceOrientation) {
		return false;
	}
}
