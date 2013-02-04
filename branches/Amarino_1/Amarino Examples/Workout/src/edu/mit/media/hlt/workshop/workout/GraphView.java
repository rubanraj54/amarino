/*
  Copyright (c) 2009 Bonifaz Kaufmann. 
  
  This library is free software; you can redistribute it and/or
  modify it under the terms of the GNU Lesser General Public
  License as published by the Free Software Foundation; either
  version 2.1 of the License, or (at your option) any later version.

  This library is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public
  License along with this library; if not, write to the Free Software
  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
*/
package edu.mit.media.hlt.workshop.workout;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public class GraphView extends View implements OnTouchListener {

	private Bitmap  mBitmap;
	private Paint   mPaint = new Paint();
    private Canvas  mCanvas = new Canvas();
    
	private float   mSpeed = 1.0f;
	private float   mLastX;
    private float   mScale;
    private float   mLastValue;
    private float   mYOffset;
    private int     mColor;
    private float   mWidth;
    private float   maxValue = 1024f;
    
    private boolean thresholdVisible = false;
    private int threshold;
    
    public GraphView(Context context) {
        super(context);
        init();
    }
    
    public GraphView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    private void init(){
    	mColor = Color.argb(192, 64, 128, 64);
        mPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        //this.setOnTouchListener(this);
        threshold = Preferences.getThreshold(getContext());
    }
    

	public void addDataPoint(float value){
        final Paint paint = mPaint;
        float newX = mLastX + mSpeed;
        final float v = mYOffset + value * mScale;
        
        paint.setColor(mColor);
        mCanvas.drawLine(mLastX, mLastValue, newX, v, paint);
        mLastValue = v;
        mLastX += mSpeed;
        
		invalidate();
    }
    
    public void setMaxValue(int max){
    	maxValue = max;
    	mScale = - (mYOffset * (1.0f / maxValue));
    }
    
    public void setSpeed(float speed){
    	mSpeed = speed;
    }
    
    
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565);
        mCanvas.setBitmap(mBitmap);
        mCanvas.drawColor(0xFFEEEEEE);
        mYOffset = h;
        mScale = - (mYOffset * (1.0f / maxValue));
        mWidth = w;
        mLastX = mWidth;
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        synchronized (this) {
            if (mBitmap != null) {
            	final Canvas cavas = mCanvas;
                if (mLastX >= mWidth) {
                    mLastX = 0;
                    cavas.drawColor(0xFFEEEEEE);
                    mPaint.setColor(0xFF779977);
                    cavas.drawLine(0, mYOffset, mWidth, mYOffset, mPaint);
                    int x = 20;
                    while (x < mWidth){
                    	mPaint.setColor(0x22779977);
                    	cavas.drawLine(x, mYOffset, x, 0, mPaint);
                    	x+=20;
                    }
                }
                if (thresholdVisible){
                	final Paint paint = mPaint;
                	paint.setColor(0xFF000000);
                	cavas.drawLine(0, threshold, mWidth, threshold, paint);
                }
                canvas.drawBitmap(mBitmap, 0, 0, null);
            }
        } 
    }

	@Override
	public boolean onTouch(View view, MotionEvent event) {
		switch (event.getAction()){
		case MotionEvent.ACTION_DOWN:
			thresholdVisible = true;
			break;
		case MotionEvent.ACTION_UP:
			thresholdVisible = false;
			break;
		case MotionEvent.ACTION_MOVE:
			threshold = (int) event.getY();
			Log.d("GraphView", "threshold:" + threshold);
			break;
		}
		invalidate();
		
		return true;
	}
}
