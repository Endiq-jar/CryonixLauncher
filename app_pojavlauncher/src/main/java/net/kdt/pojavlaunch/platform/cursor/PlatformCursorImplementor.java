package net.kdt.pojavlaunch.platform.cursor;

import net.kdt.pojavlaunch.platform.input.PlatformGrabListener;

/**
 * Platform cursor implementor. Receives cursor updates
 */
public interface PlatformCursorImplementor extends PlatformGrabListener {
    /**
     * Update cursor position on the screen
     */
    void onCursorPosition();

    /**
     * Update cursor drawable on the screen
     */
    void onCursorChanged();

    /**
     * Update screen ratio between screen/view size and game window size
     * @param ratioX X ratio
     * @param ratioY Y ratio
     */
    void onCursorRatioUpdate(double ratioX, double ratioY);
}
