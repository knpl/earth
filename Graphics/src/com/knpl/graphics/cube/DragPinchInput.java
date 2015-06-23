package com.knpl.graphics.cube;

import android.view.MotionEvent;
import android.view.View;
import android.view.MotionEvent.PointerCoords;
import android.view.View.OnTouchListener;

public class DragPinchInput implements OnTouchListener {
	
	private final ExploreState exploreState;
	private final DragState dragState;
	private final ZoomState zoomState;
	private final InvalidState invalidState;
	
	private State state;
	private final OnDragPinchListener listener;
	
	public DragPinchInput(OnDragPinchListener listener) {
		exploreState = new ExploreState();
		dragState = new DragState();
		zoomState = new ZoomState();
		invalidState = new InvalidState();
		
		this.listener = listener;
		setState(exploreState);
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		state.onTouch(event);
		if (event.getActionMasked() == MotionEvent.ACTION_UP){
			v.performClick();
		}
		return true;
	}
	
	public void setState(State state) {
		this.state = state;
	}
	
	private interface State {
		public void onTouch(MotionEvent event);
	}
	
	private class ExploreState implements State {
		@Override
		public void onTouch(MotionEvent event) {
			if (event.getActionMasked() != MotionEvent.ACTION_DOWN) {
				setState(invalidState);
				return;
			}
			float x = event.getX();
			float y = event.getY();
			listener.startDrag(x, y);
			dragState.setStart(x, y);
			setState(dragState);
		}
	}
	
	private class DragState implements State {
		
		private float x0, y0;
		
		public DragState() {
			x0 = y0 = 0f;
		}
		
		public void setStart(float x0, float y0) {
			this.x0 = x0;
			this.y0 = y0;
		}
		
		@Override
		public void onTouch(MotionEvent event) {
			int action = event.getActionMasked();
			
			float x = event.getX();
			float y = event.getY();
			
			switch (action) {
				
			case MotionEvent.ACTION_POINTER_DOWN:
				actionPointerDown(event);
				setState(zoomState);
				break;
			
			case MotionEvent.ACTION_UP:
				listener.stopDrag(x0, y0, x, y);
				setState(exploreState);
				break;
				
			case MotionEvent.ACTION_MOVE:
				listener.drag(x0, y0, x, y);
				break;
				
			case MotionEvent.ACTION_CANCEL:
				setState(exploreState);
				break;
				
			default:
				setState(invalidState);
			}
		}
		
		public void actionPointerDown(MotionEvent event) {
			int newIndex = event.getActionIndex();
			PointerCoords pOld = new PointerCoords(),
						  pNew = new PointerCoords();
			event.getPointerCoords(newIndex, pNew);
			event.getPointerCoords(1 - newIndex, pOld);

			listener.stopDrag(x0, y0, pOld.x, pOld.y);
			x0 = y0 = 0f;
			
			float dx = (pNew.x - pOld.x),
				  dy = (pNew.y - pOld.y);
			
			float cx = pOld.x + .5f*dx;
			float cy = pOld.y + .5f*dy;
			float r  = (float)(.5 * Math.sqrt(dx*dx + dy*dy));
			
			listener.startPinch(cx, cy, r);
			zoomState.setStartCircle(cx, cy, r);
		}
	}
	
	private class ZoomState implements State {

		float cx, cy, cr;
		
		public ZoomState() {
			cr = 1;
			cx = 0;
			cy = 0;
		}
		
		public void setStartCircle(float cx, float cy, float cr) {
			this.cx = cx;
			this.cy = cy;
			this.cr = cr;
		}
		
		@Override
		public void onTouch(MotionEvent event) {
			int action = event.getActionMasked();
			
			switch (action) {
			case MotionEvent.ACTION_POINTER_UP:
				actionPointerUp(event);
				setState(dragState);
				break;
				
			case MotionEvent.ACTION_MOVE:
				actionMove(event);
				break;
				
			case MotionEvent.ACTION_CANCEL:
				setState(exploreState);
				break;
				
			default:
				setState(invalidState);
			}
		}

		private void actionPointerUp(MotionEvent event) {
			PointerCoords p0 = new PointerCoords(),
						  p1 = new PointerCoords();
			event.getPointerCoords(0, p0);
			event.getPointerCoords(1, p1);
		
			float dx = (p1.x - p0.x),
				  dy = (p1.y - p0.y),
				  r  = (float)Math.sqrt(dx*dx+dy*dy)/2f;
			
			listener.stopPinch(cx, cy, cr, r);
			
			PointerCoords notReleased = (1 - event.getActionIndex() == 0) ? p0 : p1;
			dragState.setStart(notReleased.x, notReleased.y);
		}

		private void actionMove(MotionEvent event) {
			PointerCoords p0 = new PointerCoords(),
						  p1 = new PointerCoords();
			event.getPointerCoords(0, p0);
			event.getPointerCoords(1, p1);
		
			float dx = (p1.x - p0.x),
				  dy = (p1.y - p0.y),
				  r  = (float)Math.sqrt(dx*dx+dy*dy)/2f;
			
			listener.pinch(cx, cy, cr, r);
		}
	}
	
	private class InvalidState implements State {
		@Override
		public void onTouch(MotionEvent event) {
			android.util.Log.d("DragPinchInput", "invalid state");
		}
	}

}
