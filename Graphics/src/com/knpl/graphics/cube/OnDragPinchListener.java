package com.knpl.graphics.cube;

public interface OnDragPinchListener {
	public void startDrag(float x, float y);
	public void drag(float sx, float sy, float x, float y);
	public void stopDrag(float sx, float sy, float x, float y);
	
	public void startPinch(float x, float y, float r);
	public void pinch(float x, float y, float sr, float r);
	public void stopPinch(float x, float y, float sr, float r);
}
