//  Copyright 2009 Wayne Keenan
//
//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//
//  http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.
//
//  Finger.java
//  
//
//  Created by Wayne Keenan on 30/05/2009.
//
//  wayne.keenan@gmail.com
//
package com.alderstone.multitouch.mac.touchpad;

public class Finger {
	
	double timestamp;
	int frame, id;
	FingerState state;
	float size, angle, majorAxis, minorAxis;  
	float x,y,dx,dy;
	
	public Finger(int frame, double timestamp, int id, int state, float size,   float x, float y, float dx, float dy, float angle, float majorAxis, float minorAxis) {
		this.timestamp=timestamp;
		this.frame=frame;
		this.id=id;
		
		this.size=size;
		this.angle=angle;
		this.majorAxis=majorAxis;
		this.minorAxis=minorAxis;
		this.x=x;
		this.y=y;
		this.dx=dx;
		this.dy=dy;
	
		this.state = FingerState.getStateFor(state);
		
	}

	public int getID() { return id; }
	public FingerState getState() { return state; }
	public int getFrame() { return frame; }
	public double getTimestamp() { return timestamp; }
	public float getSize() { return size; }
	public float getAngleInRadians() { return angle; }
	public int   getAngle() { return (int)  ( (angle * 90) / Math.atan2(1,0)); }  // return in Degrees
	public float getMajorAxis() { return majorAxis; }
	public float getMinorAxis() { return minorAxis; }
	public float getX() { return x; }
	public float getY() { return y; }
	public float getXVelocity() { return dx; }
	public float getYVelocity() { return dy; }
	
}
