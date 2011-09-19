package plugins.adufour.multitouch;

import javax.vecmath.Vector2f;

public interface TwoFingersListener
{
	/**
	 * Called when the distance between two fingers varies. The variation (delta) is negative if the
	 * fingers are brought closer to each other, and positive otherwise
	 * 
	 * @param source
	 * @param f1
	 * @param f2
	 * @param delta
	 */
	void pinch(MultiTouchProvider source, float delta);
	
	/**
	 * Called when the two fingers are dragged in a parallel direction.
	 * 
	 * @param source
	 * @param direction
	 * @param delta
	 */
	void drag(MultiTouchProvider source, Vector2f direction, float delta);
	
	/**
	 * Called when the axis between the two fingers is rotating. This corresponds to 2 use cases:
	 * either the fingers are dragged in an anti-parallel direction, or one finger describes a
	 * circle around the other static finger. The angle value is positive if the rotation is
	 * clockwise
	 * 
	 * @param source
	 * @param angle
	 *            the rotation angle in radians
	 */
	void rotate(MultiTouchProvider source, float angle);
}
