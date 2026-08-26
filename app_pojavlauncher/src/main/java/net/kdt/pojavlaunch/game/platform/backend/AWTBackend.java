package net.kdt.pojavlaunch.game.platform.backend;


import android.view.Surface;

import net.kdt.pojavlaunch.CallbackBridge;
import net.kdt.pojavlaunch.game.platform.Platform;

public class AWTBackend implements PlatformBackend {
    static {
        System.loadLibrary("pojavexec_awt");
    }

    @Override
    public void surfaceCreated(Surface surface) {
        Platform.grabStateChanged(false);
        // AWT requires us to manually draw on the screen
        nativeBeginRendering(surface, CallbackBridge.windowWidth, CallbackBridge.windowHeight);
    }

    @Override
    public void surfaceUpdated() {
        nativeResize(CallbackBridge.windowWidth, CallbackBridge.windowHeight);
        // There's no need of updating AWT Surface... for now
    }

    @Override
    public void surfaceDestroyed() {
        nativeEndRendering();
    }

    @Override
    public void sendMousePosition() {
        nativeSendCursorPos((int) Platform.cursorX, (int) Platform.cursorY);
    }

    @Override
    public void sendMouseEvent(int button, int state, int mods) {
        nativeSendMouseEvent(button, state, mods);
    }

    @Override
    public boolean sendKeyEvent(int key, int state, int mods, char codepoint) {
        return nativeSendKeyEvent(key, state, mods, codepoint);
    }

    @Override
    public boolean sendKeyEvent(int key, int state, int mods) {
        return nativeSendKeyEvent(key, state, mods, 0);
    }

    @Override
    public boolean sendKeyEvent(int key, boolean state, int mods) {
        return nativeSendKeyEvent(key, state ? 1 : 0, mods, 0);
    }

    @Override
    public void sendScrollEvent(double x, double y) {
        // Unsupported
    }

    @Override
    public void sendBulkUnicodeEvent(String text, int mods) {
        nativeTypeChars(text);
    }

    @Override
    public String backendName() {
        return "AWT";
    }

    @Override
    public void setHovered(boolean hovered) {
        // Unsupported
    }

    @Override
    public void setVisible(boolean visible) {
        // Unsupported
    }

    public static native void nativeMoveWindow(int xoff, int yoff);
    private static native void nativeBeginRendering(Surface surface, int bridgeWidth, int bridgeHeight);
    private static native void nativeEndRendering();
    private static native void nativeSendCursorPos(int x, int y);
    private static native boolean nativeSendKeyEvent(int keycode, int state, int mods, int codepoint);
    private static native void nativeSendMouseEvent(int button, int state, int mods);
    private static native void nativeResize(int bridgeWidth, int bridgeHeight);
    private static native void nativeTypeChars(String chars);
}
