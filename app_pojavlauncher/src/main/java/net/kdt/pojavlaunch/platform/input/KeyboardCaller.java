package net.kdt.pojavlaunch.platform.input;

import net.kdt.pojavlaunch.MainActivity;
import net.kdt.pojavlaunch.Tools;

import git.mojo.sdl.SDLKeyboardCaller;

public class KeyboardCaller implements SDLKeyboardCaller {

    public void acceptKeyboard(int x, int y) {
        // TODO: panning support
        Tools.runOnUiThread(() -> MainActivity.toggleKeyboardState(true, false  ));
    }

    @Override
    public void hideKeyboard() {
        Tools.runOnUiThread(() -> MainActivity.toggleKeyboardState(false, false));
    }
}
