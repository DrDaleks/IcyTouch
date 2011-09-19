package plugins.adufour.multitouch;

import com.alderstone.multitouch.mac.touchpad.Finger;

/**
 * Interface used to received finger events
 * @author adufour
 *
 */
public interface FingerStateListener {
	/**
	 * Fired when a finger presses the pad
	 * 
	 * @param multiTouchProvider
	 * @param f
	 */
	void fingerPressed(MultiTouchProvider multiTouchProvider, Finger f);

	/**
	 * Fired when a finger is released
	 * 
	 * @param multiTouchProvider
	 * @param f
	 */
	void fingerReleased(MultiTouchProvider multiTouchProvider, Finger f);

	/**
	 * Fired when a finger hovers the pad (without actually touching it)
	 * 
	 * @param multiTouchProvider
	 * @param f
	 */
	void fingerHover(MultiTouchProvider multiTouchProvider, Finger f);
}
