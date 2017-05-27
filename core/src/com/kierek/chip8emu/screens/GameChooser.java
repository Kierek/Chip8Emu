package com.kierek.chip8emu.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
import com.kierek.chip8emu.Chip8Emu;

/**
 * Created by kierek on 26.05.17.
 */

public class GameChooser implements Screen, InputProcessor {

    private FileHandle[] games;
    private Chip8Emu emu;

    public GameChooser(Chip8Emu emu) {
        this.emu = emu;
        Gdx.input.setInputProcessor(this);
    }

    @Override
    public void show() {
        System.out.println("Choose game: ");

        games = Gdx.files.internal("roms").list();
        for (int i = 0; i < games.length; i++) {
            FileHandle file = games[i];
            System.out.println(i + ": " + file.name());
        }
    }

    @Override
    public void render(float delta) {

    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {

    }

    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    //it wold be nice to make it work with more than 9 games
    //maybe later

    @Override
    public boolean keyUp(int keycode) {
        // 7 - 16 lub 144-153

        for (int i = 0; i < games.length; i++) {
            if (keycode == 7 + i || keycode == 144 + i) {
                emu.startGame(games[i].path());
            }
        }

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
}
