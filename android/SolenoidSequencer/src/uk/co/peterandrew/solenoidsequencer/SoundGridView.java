package uk.co.peterandrew.solenoidsequencer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.View;

public class SoundGridView extends View {
	private final int mNumSteps = 16;
	private final int mNumInstruments = 4;
	
	private float mControlWidth;
	private float mControlHeight;
	
	private Paint paintOff = new Paint();
	private Paint paintOn = new Paint();
	
	private Boolean mStates[][] = new Boolean[4][16];
	private GridClickListener gridClickListener;

	
    public SoundGridView(Context context) {
        super(context);
        this.resetStates();
        
    	paintOff.setColor(Color.BLUE);
    	paintOn.setColor(Color.RED);	
    }
    
    private void resetStates() {
        for (int instrument = 0; instrument < this.mNumInstruments; instrument++) {
        	for (int step = 0; step < this.mNumSteps; step++) {
        		this.mStates[instrument][step] = false;
        	}
        }    	
    }
    
    public void setState(int instrument, int step, boolean state) {
    	this.mStates[instrument][step] = state;
    	invalidate();
    }

    public void setGridClickListener(GridClickListener gridClickListener) {
    	this.gridClickListener = gridClickListener;
    }
    
    @SuppressLint("DrawAllocation")
	protected void onDraw(Canvas canvas) {
        mControlWidth = this.getWidth() / this.mNumSteps;
        mControlHeight = this.getHeight() / this.mNumInstruments;
        RectF controlRect = new RectF(0, 0, mControlWidth - 2, mControlHeight - 2);
        
      	for (int instrument = 0; instrument < this.mNumInstruments; instrument++) {
    		canvas.translate(1, 1);
    		for (int step = 0; step < this.mNumSteps; step++) {
    			Paint currPaint;
    			if (!this.mStates[instrument][step]) {
    				currPaint = paintOff;
    			} else {
    				currPaint = paintOn;
    			}
    			canvas.drawRoundRect(controlRect, 0.1f, 0.1f, currPaint);
    			canvas.translate(mControlWidth, 0);
    		}
    		canvas.translate(-(canvas.getWidth() + 1), mControlHeight);
    	}
    }
    
    public boolean onTouchEvent(MotionEvent event) {
    	if (event.getAction() == MotionEvent.ACTION_DOWN) {
    		int step = (int)(event.getX() / this.mControlWidth);
    		int instrument = (int)(event.getY() / this.mControlHeight);
    		
    		mStates[instrument][step] = !mStates[instrument][step];
    		
    		gridClickListener.onGridClick(step + 1, instrument + 1, mStates[instrument][step]);
    		
    		invalidate();
    	}
    	return true;
    }
    
    
	public interface GridClickListener {
		public void onGridClick(int step, int instrument, boolean state);
	}

}
