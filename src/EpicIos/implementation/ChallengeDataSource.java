package com.epic.framework.implementation;

import org.xmlvm.iphone.CGAffineTransform;
import org.xmlvm.iphone.CGRect;
import org.xmlvm.iphone.NSIndexPath;
import org.xmlvm.iphone.UIColor;
import org.xmlvm.iphone.UIImage;
import org.xmlvm.iphone.UIImageView;
import org.xmlvm.iphone.UILabel;
import org.xmlvm.iphone.UITableView;
import org.xmlvm.iphone.UITableViewCell;
import org.xmlvm.iphone.UITableViewDataSource;

import com.epic.framework.common.Ui.EpicBitmap;

class ChallengeDataSource extends UITableViewDataSource {
	private String[] titles;
	private String[] subtitles;
	private EpicBitmap[] images;
	private boolean[] complete;

	public ChallengeDataSource(String[] titles, String[] subtitles, boolean[] complete, EpicBitmap[] images) {
		this.titles = titles;
		this.subtitles = subtitles;
		this.images = images;
		this.complete = complete;
	}
	
	public UITableViewCell cellForRowAtIndexPath(UITableView table, NSIndexPath idx) {
		int padding = 5;
		int imageDims = (int)table.getRowHeight() - (padding * 2);
		int textWidth = (int) table.getFrame().size.width - imageDims - (padding * 3);
		UITableViewCell cell = new UITableViewCell();
		
		// if(complete[idx.getRow()]) cell.getContentView().setBackgroundColor(completedBgColor);
		
		UILabel label = new UILabel(new CGRect(imageDims + (2*padding), padding, textWidth, (int) table.getRowHeight() / 3));
		label.setText(titles[idx.getRow()]);
		label.setFont(label.getFont().fontWithSize(26));
		label.setSize(label.getBounds().size.width, 28);
//		UILabel subtitle = cell.getDetailTextLabel();
//		subtitle.setText(subtitles[idx.getRow()]);
		
		// if(complete[idx.getRow()]) label.setBackgroundColor(UIColor.clearColor);
		cell.addSubview(label);
		
		UIImageView img = cell.getImageView();
		img.setTransform(CGAffineTransform.makeScale(1, -1));
		img.setLocation(padding, padding);
		img.setImage((UIImage) images[idx.getRow()].getPlatformObject(imageDims, imageDims));
		
		UILabel subtitle = new UILabel(new CGRect(imageDims + (2*padding), padding + label.getBounds().origin.y + label.getBounds().size.height, textWidth, ((int) 2 * table.getRowHeight() / 3) - padding));
		subtitle.setText(subtitles[idx.getRow()]);
		subtitle.setTextColor(UIColor.lightGrayColor);
		subtitle.setNumberOfLines(2);
		subtitle.setFont(subtitle.getFont().fontWithSize(20));
		// if(complete[idx.getRow()]) subtitle.setBackgroundColor(UIColor.clearColor);
		cell.addSubview(subtitle);
		
		return cell;
	}

	public int numberOfRowsInSection(UITableView table, int section) {
		return titles.length;
	}
}