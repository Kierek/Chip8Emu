package com.kierek.chip8emu.emu;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.kierek.chip8emu.Chip8Emu;

public class InputHandler implements InputProcessor {

    private static final String TAG = "InputHandler";

    private Chip8Emu emu;

    private boolean[] keyState;

    public InputHandler(Chip8Emu emu) {
        this.emu = emu;
        keyState = new boolean[16];
    }

    @Override
    public boolean keyDown(int keycode) {
        int hex = getHex(keycode);
        if (hex != -1)
            keyState[getHex(keycode)] = true;
        return true;
    }

    @Override
    public boolean keyUp(int keycode) {
        if (keycode == Input.Keys.SPACE) {
            emu.reset();
            return true;
        }

        int hex = getHex(keycode);
        if (hex != -1)
            keyState[getHex(keycode)] = false;
        return true;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
    }

    boolean getKeyStatus(int address) {
        return keyState[address];
    }

    private int getHex(int keycode) {
        switch (keycode) {
            case Input.Keys.NUMPAD_1:
                return 0x0;
            case Input.Keys.NUMPAD_2:
                return 0x1;
            case Input.Keys.NUMPAD_3:
                return 0x2;
            case Input.Keys.NUMPAD_4:
                return 0x3;
            case Input.Keys.Q:
                return 0x4;
            case Input.Keys.W:
                return 0x5;
            case Input.Keys.E:
                return 0x6;
            case Input.Keys.R:
                return 0x7;
            case Input.Keys.A:
                return 0x8;
            case Input.Keys.S:
                return 0x9;
            case Input.Keys.D:
                return 0xA;
            case Input.Keys.F:
                return 0xB;
            case Input.Keys.Z:
                return 0xC;
            case Input.Keys.X:
                return 0xD;
            case Input.Keys.C:
                return 0xE;
            case Input.Keys.V:
                return 0xF;
        }

        return -1;
    }
}
