package com.example.HelloUIv2;

import com.epic.framework.common.Ui.EpicCanvas;
import com.epic.framework.common.Ui.EpicColor;
import com.epic.framework.common.Ui2.EpicWidget;

public class SampleWidget extends EpicWidget {

	@Override
	public void onPaint(EpicCanvas canvas) {
		canvas.applyFullscreenFill(EpicColor.YELLOW);
		canvas.drawCircle(EpicColor.RED, 255, 400, 200, 150);
	}

}
