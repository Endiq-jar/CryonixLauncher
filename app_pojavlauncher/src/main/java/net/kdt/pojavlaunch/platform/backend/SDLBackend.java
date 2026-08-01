package net.kdt.pojavlaunch.platform.backend;

import android.app.Activity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Surface;

import net.kdt.pojavlaunch.LauncherGLSurface;
import net.kdt.pojavlaunch.platform.Platform;

import git.mojo.sdl.SDL;
import git.mojo.sdl.SDLActivity;

import git.mojo.sdl.SDLControllerManager;
import git.mojo.sdl.SDLInputConnection;

public class SDLBackend implements PlatformBackend{

    private static void handleGrabStateChange(boolean isGrabbing){
        // SDL really expects cursor to be at 0x0 position when relative mode (grabbing = true) is enabled
        // This caused weird jumps when gaining grab because Platform cursor position values contain stale non-zero values at that point.
        // Reset position to 0x0 when gaining grab state
        Platform.cursorX = 0;
        Platform.cursorY = 0;
        Platform.grabStateChanged(isGrabbing);
    }
    public SDLBackend(){
        SDLActivity.setGrabListener(SDLBackend::handleGrabStateChange);
        SDLActivity.setCursorCallback(cursor -> {
            if(cursor != null) Platform.setCursor(cursor.getBitmap(), cursor.getXhot(), cursor.getYhot());
            else Platform.setCursor(null, 0, 0);
        });
        SDLActivity.setKeyboardCaller(Platform.getKeyboardCaller());
    }
    public static void initialize(Activity activity) {
        // TODO: check what can be moved to the initialize point
        // we need to setup enough SDL for the game to not crash to initialize it later
        SDL.initialize();
        SDL.setContext(activity);
        SDL.setupJNI();
        SDLControllerManager.initializeDeviceListener();
    }

    @Override
    public void surfaceCreated(Surface surface) {
        SDLActivity.setNativeSurface(surface);
        SDLActivity.onNativeSurfaceCreated();
        this.surfaceUpdated(); // Update initial size
    }

    @Override
    public void surfaceUpdated() {
        int w = LauncherGLSurface.getWindowWidth();
        int h = LauncherGLSurface.getWindowHeight();
        float r = LauncherGLSurface.getWindowRate();
        SDLActivity.nativeSetScreenResolution(w, h, w, h, 1.0f, r);
        SDLActivity.onNativeResize();
        SDLActivity.onNativeSurfaceChanged();
    }

    @Override
    public void surfaceDestroyed() {
        SDLActivity.onNativeSurfaceDestroyed();
    }

    @Override
    public void sendMousePosition() {
        if(!Platform.isGrabbing()) Platform.clampCursorPosition();
        SDLActivity.onNativeMouse(0, MotionEvent.ACTION_MOVE, (float) Platform.cursorX, (float) Platform.cursorY, Platform.isGrabbing());
        if(Platform.isGrabbing()){
            // SDL in relative mode expects these to be reset to 0 or it will freak out (classic:tm: way)
            Platform.cursorX = 0;
            Platform.cursorY = 0;
        }
        Platform.getCursorImplementor().onCursorPosition();
    }


    @Override
    public void sendMouseEvent(int key, int state, int mods) {
        SDLActivity.onNativeMouseButton(key, state, (float) Platform.cursorX, (float) Platform.cursorY, Platform.isGrabbing());
    }

    @Override
    public void sendKeyEvent(int key, int state, int mods, char codepoint) {
        if(state == KeyEvent.ACTION_DOWN) {
            SDLActivity.onNativeKeyDown(key);
            if(codepoint != 0) SDLInputConnection.nativeCommitText(String.valueOf(codepoint), 0);
        }
        else SDLActivity.onNativeKeyUp(key);
    }

    @Override
    public void sendKeyEvent(int key, int state, int mods) {
        if(state == 1) SDLActivity.onNativeKeyDown(key);
        else SDLActivity.onNativeKeyUp(key);
    }

    @Override
    public void sendKeyEvent(int key, boolean state, int mods) {
        if(state) SDLActivity.onNativeKeyDown(key);
        else SDLActivity.onNativeKeyUp(key);
    }

    @Override
    public void sendScrollEvent(double x, double y) {
        SDLActivity.onNativeMouse(0, MotionEvent.ACTION_SCROLL, (float) x, (float) y, false);
    }

    @Override
    public void sendBulkUnicodeEvent(String text, int mods) {
        SDLInputConnection.nativeCommitText(text, 0);
    }

    @Override
    public String backendName() {
        return "SDL";
    }
}
