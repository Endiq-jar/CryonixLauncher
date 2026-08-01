package net.kdt.pojavlaunch.platform.input;

import net.kdt.pojavlaunch.MainActivity;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.prefs.LauncherPreferences;

import git.mojo.sdl.SDLKeyboardCaller;

public class KeyboardCaller implements SDLKeyboardCaller {

    public void acceptKeyboard(int x, int y) {
        if(!LauncherPreferences.PREF_ALLOW_KEYBOARD_AUTOOPEN) return;
        Tools.runOnUiThread(() -> MainActivity.toggleKeyboardState(true, y - 50));
    }

    @Override
    public void hideKeyboard() {
        if(!LauncherPreferences.PREF_ALLOW_KEYBOARD_AUTOOPEN) return;
        Tools.runOnUiThread(() -> MainActivity.toggleKeyboardState(false, 0));
    }
}
