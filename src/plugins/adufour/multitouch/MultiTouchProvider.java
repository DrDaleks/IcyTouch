package plugins.adufour.multitouch;

import icy.gui.dialog.MessageDialog;
import icy.plugin.abstract_.Plugin;
import icy.plugin.interface_.PluginLibrary;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import javax.vecmath.Point2f;
import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;

import com.alderstone.multitouch.mac.touchpad.Finger;
import com.alderstone.multitouch.mac.touchpad.FingerState;
import com.alderstone.multitouch.mac.touchpad.TouchpadObservable;

public class MultiTouchProvider extends Plugin implements PluginLibrary, Observer
{
	/** Rotation threshold used to distinguish pinch or drag from actual rotation */
	public static final float						ROTATE_THRESHOLD			= 0.0008f;
	
	/** Motion threshold used to distinguish finger artifact from actual drag */
	public static final float						DRAG_THRESHOLD				= 0.000005f;
	
	/** Maximum number of detectable fingers. (Current API is limited to 11) */
	public static final int							MAX_FINGER_BLOBS			= 20;
	
	/** Motion threshold used to distinguish finger artifact from actual motion */
	public static final float						MOTION_THRESHOLD			= 0.0003f;
	
	/** Motion threshold used to distinguish finger artifact from actual pinch gesture */
	public static final float						PINCH_THRESHOLD				= 0.003f;
	
	/**
	 * Number of frames (consecutive events) to disregard before calculating motion events. This
	 * threshold helps avoiding motion artifact from the finger when it is being first pressed
	 */
	public static final int							PRESSED_FRAMES_THRESHOLD	= 6;
	
	private final Finger[]							currentFingersState			= new Finger[MAX_FINGER_BLOBS];
	
	private final Finger[]							oldFingersState				= new Finger[MAX_FINGER_BLOBS];
	
	/**
	 * The number of frames each finger was pressed since the last pressed event
	 */
	private final int[]								nbFramesPressed				= new int[MAX_FINGER_BLOBS];
	
	private final TouchpadObservable				tpo;
	
	private final ArrayList<FingerStateListener>	listeners					= new ArrayList<FingerStateListener>();
	
	private final ArrayList<FingerMotionListener>	motionListeners				= new ArrayList<FingerMotionListener>();
	
	private final ArrayList<TwoFingersListener>		twoFingersListeners			= new ArrayList<TwoFingersListener>();
	
	private boolean									enabled						= true;
	
	/**
	 * Creates a new MultiTouch provider, which can be used to listen to MultiTouch events.
	 */
	public MultiTouchProvider()
	{
		TouchpadObservable observable = null;
		
		try
		{
			observable = TouchpadObservable.getInstance();
			observable.addObserver(this);
		}
		catch (UnsupportedOperationException e)
		{
			MessageDialog.showDialog("Error", e.getMessage(), MessageDialog.ERROR_MESSAGE);
		}
		finally
		{
			tpo = observable;
		}
	}
	
	/**
	 * Gets whether the event notification system is enabled
	 * 
	 * @return
	 */
	public boolean isEnabled()
	{
		return enabled;
	}
	
	/**
	 * Enables or disables the event notification system for this provider
	 * 
	 * @param enabled
	 */
	public void setEnabled(boolean enabled)
	{
		this.enabled = enabled;
	}
	
	public void addFingerListener(FingerStateListener listener)
	{
		listeners.add(listener);
	}
	
	public void addFingerMotionListener(FingerMotionListener listener)
	{
		motionListeners.add(listener);
	}
	
	public void addTwoFingersListener(TwoFingersListener listener)
	{
		twoFingersListeners.add(listener);
	}
	
	public void removeFingerListener(FingerStateListener listener)
	{
		listeners.remove(listener);
	}
	
	public void removeFingerMotionListener(FingerMotionListener listener)
	{
		motionListeners.remove(listener);
	}
	
	public void removeTwoFingersListener(TwoFingersListener listener)
	{
		twoFingersListeners.remove(listener);
	}
	
	/**
	 * Returns the current number of fingers in the specified state
	 * 
	 * @param state
	 *            a finger state (see constants in the {@link FingerState} class)
	 * @return
	 */
	public int getCount(FingerState state)
	{
		int cpt = 0;
		for (Finger f : currentFingersState)
		{
			if (f == null)
				continue;
			if (f.getState() == state)
				cpt++;
		}
		return cpt;
	}
	
	public void update(Observable obj, Object arg)
	{
		if (!enabled)
			return;
		
		Finger newFingerState = (Finger) arg;
		
		// the finger ID starts at 1, not 0
		final int id = newFingerState.getID() - 1;
		
		Finger oldFingerState = currentFingersState[id];
		
		if (oldFingerState == null || oldFingersState[id] == null)
		{
			currentFingersState[id] = newFingerState;
			oldFingersState[id] = newFingerState;
			return;
		}
		
		if (newFingerState.getState() == FingerState.PRESSED)
		{
			// count the number of frames the finger was pressed
			nbFramesPressed[id]++;
			
			// consider the event "valid" only after a number of time frames
			// => this prevents artifact gestures
			if (nbFramesPressed[id] > PRESSED_FRAMES_THRESHOLD)
			{
				// the event is valid => reset the counter
				nbFramesPressed[id] = 0;
				
				processMultiTouchEvent(oldFingerState, newFingerState);
			}
		}
		else if (newFingerState.getState() == FingerState.RELEASED)
		{
			nbFramesPressed[id] = 0;
			
			// state listeners
			if (newFingerState.getState() != oldFingerState.getState())
				for (FingerStateListener l : listeners)
					l.fingerReleased(this, newFingerState);
			
		}
		else if (newFingerState.getState() == FingerState.HOVER)
		{
			// state listeners
			if (newFingerState.getState() != oldFingerState.getState())
				for (FingerStateListener l : listeners)
					l.fingerHover(this, newFingerState);
		}
		
		oldFingersState[id] = currentFingersState[id];
		currentFingersState[id] = newFingerState;
	}
	
	private void processMultiTouchEvent(Finger oldFingerState, Finger newFingerState)
	{
		// state listeners
		if (newFingerState.getState() != oldFingerState.getState())
			for (FingerStateListener l : listeners)
				l.fingerPressed(this, newFingerState);
		
		Point2f f1 = new Point2f(newFingerState.getX(), newFingerState.getY());
		Point2f f1old = new Point2f(oldFingerState.getX(), oldFingerState.getY());
		Vector3f df1 = new Vector3f(f1.x - f1old.x, f1.y - f1old.y, 0);
		
		// motion listeners
		if (motionListeners.size() > 0)
		{
			if (Math.abs(df1.x) > MOTION_THRESHOLD || Math.abs(df1.y) > MOTION_THRESHOLD)
				for (FingerMotionListener motionListener : motionListeners)
					motionListener.fingerMoved(this, newFingerState, df1.x, df1.x);
		}
		
		// multi-touch gestures
		if (twoFingersListeners.size() > 0)
		{
			int nbFingers = getCount(FingerState.PRESSED);
			
			if (nbFingers == 2)
			{
				Finger curF2 = null;
				// find the second finger
				for (int i = 0; i < MAX_FINGER_BLOBS; i++)
				{
					curF2 = currentFingersState[i];
					if (curF2 != null && curF2.getState() == FingerState.PRESSED && curF2.getID() != newFingerState.getID())
						break;
				}
				
				int id2 = curF2.getID() - 1;
				
				Point2f f2 = new Point2f(curF2.getX(), curF2.getY());
				Point2f f2old = new Point2f(oldFingersState[id2].getX(), oldFingersState[id2].getY());
				Vector3f df2 = new Vector3f(f2.x - f2old.x, f2.y - f2old.y, 0);
				
				float dotv1v2 = df1.dot(df2);
				
				if (dotv1v2 > DRAG_THRESHOLD)
				{
					// drag
					
					float delta = df1.length();
					df1.normalize(); // WARNING: df1 is destroyed from now on
					
					for (TwoFingersListener l : twoFingersListeners)
						l.drag(this, new Vector2f(df1.x, df1.y), delta);
				}
				else
				{
					// pinch
					
					Vector3f f1f2 = new Vector3f(f2.x - f1.x, f2.y - f1.y, 0);
					Vector3f f1f2Old = new Vector3f(f2old.x - f1old.x, f2old.y - f1old.y, 0);
					
					float dDistance = f1f2Old.length() - f1f2.length();
					
					if (Math.abs(dDistance) > PINCH_THRESHOLD)
					{
						for (TwoFingersListener l : twoFingersListeners)
							l.pinch(this, dDistance);
					}
					
					// rotate
					
					float angle = f1f2Old.angle(f1f2);
					f1f2.cross(f1f2Old, f1f2); // WARNING: f1f2 is destroyed from now on
					
					if (Math.abs(f1f2.z) > ROTATE_THRESHOLD)
					{
						for (TwoFingersListener l : twoFingersListeners)
							l.rotate(this, Math.signum(f1f2.z) * angle);
					}
				}
			}
		}
		
	}
	
	public void shutDown()
	{
		tpo.deleteObservers();
	}
	
	@Override
	protected void finalize() throws Throwable
	{
		shutDown();
		super.finalize();
	}
}
