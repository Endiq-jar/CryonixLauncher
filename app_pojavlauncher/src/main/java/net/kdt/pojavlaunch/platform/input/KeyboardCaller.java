package net.kdt.pojavlaunch.platform.input;

import net.kdt.pojavlaunch.MainActivity;

import git.mojo.sdl.SDLKeyboardCaller;

public class KeyboardCaller implements SDLKeyboardCaller {

    public void acceptKeyboard(int x, int y) {
        // TODO: panning support
        MainActivity.switchKeyboardState(false);
    }
}
