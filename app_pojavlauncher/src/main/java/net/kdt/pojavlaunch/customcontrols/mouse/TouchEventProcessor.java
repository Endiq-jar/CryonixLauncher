package net.kdt.pojavlaunch.customcontrols.mouse;

import static net.kdt.pojavlaunch.platform.Platform.PLATFORM;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import net.kdt.pojavlaunch.LauncherGLSurface;
import net.kdt.pojavlaunch.platform.Platform;
import net.kdt.pojavlaunch.prefs.LauncherPreferences;

public abstract class TouchEventProcessor {
    private final View mHostView;
    private double ratioX, ratioY;
    public TouchEventProcessor(View hostView) {
        mHostView = hostView;
    }
    public void updateCursorRatio(double ratioX, double ratioY){
        this.ratioX = ratioX;
        this.ratioY = ratioY;
    }

    protected void sendTouchCoordinates(float x, float y) {
        Platform.cursorX = x / ratioX;
        Platform.cursorY = y / ratioY;
        Platform.sendCursorPosition();
    }

    protected void applyMoveVector(float[] vector) {
        applyMoveVector(vector[0], vector[1]);
    }

    protected void applyMoveVector(float x, float y) {
        Platform.cursorX += x * LauncherPreferences.PREF_MOUSESPEED;
        Platform.cursorY += y * LauncherPreferences.PREF_MOUSESPEED;
        Platform.sendCursorPosition();
    }

    abstract public boolean processTouchEvent(MotionEvent motionEvent);
    abstract public void cancelPendingActions();
}
