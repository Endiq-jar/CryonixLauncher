package net.kdt.pojavlaunch.awt;

import android.view.Surface;

public class AWTWindow {
    public static native void nativeMoveWindow(int xoff, int yoff);
    public static native void beginRendering(Surface surface, int canvasWidth, int canvasHeight);
    public static native void endRendering();
}
