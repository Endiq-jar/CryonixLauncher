package net.kdt.pojavlaunch.platform;

import android.app.Activity;
import android.graphics.Bitmap;
import android.view.Surface;

import net.kdt.pojavlaunch.LauncherGLSurface;
import net.kdt.pojavlaunch.MainActivity;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.lifecycle.ContextExecutor;
import net.kdt.pojavlaunch.platform.backend.GLFWBackend;
import net.kdt.pojavlaunch.platform.backend.DummyBackend;
import net.kdt.pojavlaunch.platform.backend.PlatformBackend;
import net.kdt.pojavlaunch.platform.backend.SDLBackend;
import net.kdt.pojavlaunch.platform.clipboard.AndroidClipboard;
import net.kdt.pojavlaunch.platform.cursor.PlatformCursor;
import net.kdt.pojavlaunch.platform.cursor.PlatformCursorImplementor;
import net.kdt.pojavlaunch.platform.input.PlatformGamepad;
import net.kdt.pojavlaunch.platform.input.PlatformGrabListener;
import net.kdt.pojavlaunch.platform.input.SDLGamepad;

import java.util.ArrayList;
import java.util.List;

import git.artdeell.dnbootstrap.glfw.GLFW;
import git.artdeell.dnbootstrap.glfw.GamepadEnableHandler;
import git.mojo.sdl.SDLActivity;
import git.mojo.sdl.SDLControllerManager;

/**
 * Launcher Platform frontend used to manage different window system & input implementations. Currently supports SDL&GLFW
 */
public class Platform {
    public static PlatformBackend PLATFORM = new DummyBackend(); // Initialize a dummy platform - the game will initialize correct one later
    private static List<PlatformGrabListener> grabListeners = new ArrayList<>();
    private static PlatformCursorImplementor mCursorImplementor = null;
    private static boolean isGrabbing = false;
    public static double cursorX;
    public static double cursorY;
    private static Surface mPendingSurface;
    private static PlatformGamepad mPlatformGamepad = null;
    private static PlatformCursor mPlatformCursor = null;
    private static GamepadEnableHandler mGamepadEnabler;
    private static AndroidClipboard mClipboard;

    // Always reset cursor on grab lost - makes it move to the center as should if the game didn't move it
    private static final boolean RESET_CURSOR_UNGRAB = true;

    /**
     * Initialize Platform, set platform implementations' init callbacks and fire early initializers
     * @param activity activity to bind to
     */
    public static void initialize(Activity activity) {
        mClipboard = new AndroidClipboard(activity.getApplicationContext());
        GLFW.setInitCallback(() -> onInit(new GLFWBackend()));
        SDLActivity.setInitCallback(() -> onInit(new SDLBackend()));
        SDLActivity.setClipboard(mClipboard);
        GLFW.setClipboardImpl(mClipboard);
        // SDL can handle gamepads on its own, so route all events through it
        // if SDL was detected of course (the check is based on detectDevices)
        // Vanilla SDL client shouldn't touch input system and thus cause emulated input to break
        SDLControllerManager.setEnabledCallback(() -> mPlatformGamepad = new SDLGamepad());
        SDLBackend.initialize(activity);
    }

    private static void onInit(PlatformBackend impl) {
        // We probably already initialized at this point. Don't try to initialize again
        if(!(PLATFORM instanceof DummyBackend)) return;
        Platform.setPlatformLibrary(impl);
        ContextExecutor.executeActivity(activity -> ((MainActivity) activity).hideLoadingScreen());
        resetCursorPosition();
    }

    /**
     * Is current platform implementation grabbed the cursor
     * @return grab state
     */
    public static boolean isGrabbing() {
        return isGrabbing;
    }

    /**
     * Change grab state of a platform. Called from implementation-specific grab listeners. Safe to call from non-UI threads.
     * @param grabbing new grab state
     */
    public static void grabStateChanged(boolean grabbing) {
        boolean wasGrabbing = isGrabbing;
        isGrabbing = grabbing;
        Tools.runOnUiThread(() -> {
            if(RESET_CURSOR_UNGRAB && wasGrabbing && !isGrabbing) resetCursorPosition();
            if(mCursorImplementor != null) mCursorImplementor.onGrabState(grabbing);
            for(PlatformGrabListener listener : grabListeners) {
                listener.onGrabState(grabbing);
            }
        });
    }

    /**
     * Get Platform gamepad implementation
     * @return Platform gamepad object
     */
    public static PlatformGamepad getPlatformGamepad() {
        return mPlatformGamepad;
    }

    /**
     * Get Platform custom cursor
     * @return cursor object
     */
    public static PlatformCursor getCursor() {
        return mPlatformCursor;
    }

    /**
     * Set Platform custom cursor
     * @param bitmap Custom cursor bitmap
     * @param xhot x offset of the cursor hotspot
     * @param yhot y offset of the cursor hotspot
     */
    public static void setCursor(Bitmap bitmap, int xhot, int yhot){
        mPlatformCursor = bitmap == null ? null : new PlatformCursor(bitmap, xhot, yhot);
        mCursorImplementor.onCursorChanged();
    }

    /**
     * Set cursor implementor for Platform
     * @param implementor cursor implementor
     */
    public static void setCursorImplementor(PlatformCursorImplementor implementor) {
        mCursorImplementor = implementor;
    }

    /**
     * Get currently used cursor implementor
     * @return Cursor implementor
     */
    public static PlatformCursorImplementor getCursorImplementor() {
        return mCursorImplementor;
    }

    /**
     * Set GLFW gamepad handler.
     * TODO: Make GLFW gamepad handling same as SDL
     * @param handler gamepad enable handler
     */
    public static void setGamepadEnableHandler(GamepadEnableHandler handler) {
        mGamepadEnabler = handler;
    }

    /**
     * Get GLFW gamepad enable handler
     * @return gamepad enable handler
     */
    public static GamepadEnableHandler getGamepadEnableHandler() {
        return mGamepadEnabler;
    }

    /**
     * Set current cursor position
     * @param x Cursor X
     * @param y Cursor Y
     */
    public static void setCursorPosition(double x, double y) {
        cursorX = x;
        cursorY = y;
        clampCursorPosition();
        mCursorImplementor.onCursorPosition();
    }

    /**
     * Clamp cursor position on the screen. Prevents the cursor from moving outside the game window
     */
    public static void clampCursorPosition(){
        cursorX = Math.clamp(cursorX, 0, LauncherGLSurface.getWindowWidth());
        cursorY = Math.clamp(cursorY, 0f, LauncherGLSurface.getWindowHeight());
    }

    /**
     * Reset current cursor position and set it to the center of a window
     */
    public static void resetCursorPosition(){
        cursorX = (double) LauncherGLSurface.getWindowWidth() / 2;
        cursorY = (double) LauncherGLSurface.getWindowHeight() / 2;
    }

    /**
     * Send current cursor position to the implementation after clamping and updating its view position.
     * Prefer using this over {@link PlatformBackend#sendMousePosition()}
     */
    public static void sendCursorPosition(){
        mCursorImplementor.onCursorPosition();
        if(!isGrabbing) clampCursorPosition();
        PLATFORM.sendMousePosition();
    }

    /**
     * Register Platform grab listener
     * @param pgl Grab listener
     */
    public static void addGrabListener(PlatformGrabListener pgl) {
        grabListeners.add(pgl);
    }

    /**
     * Trigger surface recreate on implementation. Needs to be called each time a surface object becomes invalid
     * @param surface Surface object
     */
    public static void updateSurface(Surface surface){
        mPendingSurface = surface;
        PLATFORM.surfaceCreated(surface);
    }

    /**
     * Set platform implementation backend
     * @param backend implementation backend
     */
    public static void setPlatformLibrary(PlatformBackend backend) {
        PLATFORM = backend;
        // To be picked by platform library
        if(mPendingSurface != null)
            PLATFORM.surfaceCreated(mPendingSurface);
    }
}
