package plugins.adufour.multitouch;

import com.alderstone.multitouch.mac.touchpad.Finger;

public interface FingerMotionListener
{
    /**
     * Fired when a finger is moved on the pad
     * 
     * @param multiTouchProvider
     * @param f
     */
    void fingerMoved(MultiTouchProvider multiTouchProvider, Finger f, float dX, float dY);
}
