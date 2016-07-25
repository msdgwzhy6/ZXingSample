package com.zxing;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Handler;

import com.google.zxing.Result;
import com.zxing.view.ViewfinderView;

public abstract class CaptureBaseActivity extends Activity {
	
	public void handleDecode(Result result, Bitmap barcode) {
		
	}
	
	public void drawViewfinder(){}
	
	public Handler getHandler() {
		return null;
	}

	public ViewfinderView getViewfinderView() {
		return null;
	}

}
