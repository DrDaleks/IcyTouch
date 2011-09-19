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
//  FingerState.java
//
//  Created by Wayne Keenan on 30/05/2009.
//
//  wayne.keenan@gmail.com
//
package com.alderstone.multitouch.mac.touchpad;

public class FingerState {
	
	private final String name;
	
	private FingerState(String name) { this.name=name;}
	
	public String toString() { return name; }
	
	public static final FingerState PRESSED   = new FingerState("PRESSED");
	public static final FingerState RELEASED  = new FingerState("RELEASED");
	public static final FingerState HOVER     = new FingerState("HOVER");
	public static final FingerState PRESSING  = new FingerState("PRESSING");
	public static final FingerState RELEASING = new FingerState("RELEASING");
	public static final FingerState TAP		  = new FingerState("TAP");
	public static final FingerState UNKNOWN_1 = new FingerState("UNKNOWN_1");
	public static final FingerState UNKNOWN   = new FingerState("UNKNOWN_?");
	
	public static FingerState getStateFor(int stateId) {
		FingerState state;
		switch (stateId) {
			case 1:		state = FingerState.UNKNOWN_1;		break;
			case 2:		state = FingerState.HOVER;		break;
			case 3:		state = FingerState.TAP;		break;
			case 4:		state = FingerState.PRESSED;	break;
			case 5:		state = FingerState.PRESSING;	break;
			case 6:		state = FingerState.RELEASING;	break;
			case 7:		state = FingerState.RELEASED;	break;
			default:	state = FingerState.UNKNOWN;	break;
		}
		
		return state;
	}
}
