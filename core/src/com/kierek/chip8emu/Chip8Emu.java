package com.kierek.chip8emu;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.kierek.chip8emu.emu.InputHandler;
import com.kierek.chip8emu.emu.Processor;
import com.kierek.chip8emu.emu.Renderer;
import com.kierek.chip8emu.screens.GameChooser;

public class Chip8Emu extends Game {

    private static final String TAG = "Chip8Emu";

    private Processor mProcessor;
    private Renderer mRenderer;
    private InputHandler mInput;

    @Override
    public void create() {
        setScreen(new GameChooser(this));

    }

    @Override
    public void render() {
        if (mRenderer != null && mProcessor != null) {
            mProcessor.emulateCycle(Gdx.graphics.getDeltaTime());
            mRenderer.render();
        }
    }

    @Override
    public void dispose() {

    }

    @Override
    public void resize(int width, int height) {
        if (mRenderer != null)
            mRenderer.resize(width, height);
    }

    public InputHandler getInput() {
        return mInput;
    }

    public Renderer getRenderer() {
        return mRenderer;
    }

    public void startGame(String gameFile) {
        mRenderer = new Renderer(this);
        mProcessor = new Processor(this);
        mInput = new InputHandler(this);

        mProcessor.loadROM(gameFile);

        Gdx.input.setInputProcessor(mInput);
    }

    public void reset() {
        mRenderer.clearDisplay();
        mRenderer = null;
        mProcessor = null;
        mInput = null;
        setScreen(new GameChooser(this));
    }
}
