package com.kierek.chip8emu.emu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.kierek.chip8emu.Chip8Emu;

import java.util.Arrays;

public class Renderer {

    private static final String TAG = "Renderer";

    private OrthographicCamera cam;
    private ShapeRenderer shapeRenderer;
    private FitViewport viewport;

    //screen resolution was 64x32 pixels, they where either on or off
    private static final int WIDTH = 64;
    private static final int HEIGHT = 32;

    private boolean[][] pixels = new boolean[WIDTH][HEIGHT];

    private Chip8Emu emu;

    public Renderer(Chip8Emu emu) {
        this.emu = emu;

        cam = new OrthographicCamera();
        cam.setToOrtho(true, WIDTH, HEIGHT);
        viewport = new FitViewport(WIDTH, HEIGHT, cam);

        shapeRenderer = new ShapeRenderer();
        shapeRenderer.setColor(Color.BLACK);
        shapeRenderer.setProjectionMatrix(cam.combined);

    }

    public void render() {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        for (int x = 0; x < pixels.length; x++) {
            for (int y = 0; y < pixels[x].length; y++) {
                if (pixels[x][y]) {
                    shapeRenderer.setColor(Color.WHITE);
                } else {
                    shapeRenderer.setColor(Color.BLACK);
                }
                shapeRenderer.rect(x, y, 1, 1);
            }
        }

        shapeRenderer.end();
    }

    public void clearDisplay() {
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                pixels[x][y] = false;
            }
        }

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    }

    boolean drawSprite(int x, int y, int[] sprite) {
        boolean wasTurnedOff = false;

        //Sprite is 8pixels wide and up to 15 lines high.
        //Every line is 1 byte long.
        //We need to transform every hex to binary, and then put them into pixels[],

        for (int row = 0; row < sprite.length; row++) {
            int rowInBits = sprite[row];

            //I have no idea why, but sprites seem to be reversed on the X axis
            //so I draw them from right to left
            int tempX = 0;
            for (int col = 7; col >= 0; col--, tempX++) {

                //I have no idea why it crashes without modulo
                //didn't chip8 have no display wrapping?
                int destX = (tempX + x) % WIDTH;
                int destY = (y + row) % HEIGHT;

                boolean previousVal = pixels[destX][destY];
                pixels[destX][destY] ^= ((rowInBits >>> col) & 1) == 1;

                if (previousVal && !pixels[destX][destY]) {
                    wasTurnedOff = true;
                }
            }
        }
        return wasTurnedOff;
    }

    public void resize(int width, int height) {
        viewport.update(width, height);
    }
}
