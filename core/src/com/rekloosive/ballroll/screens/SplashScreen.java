package com.rekloosive.ballroll.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.rekloosive.ballroll.BallRoll;
import com.rekloosive.ballroll.utils.Constants;

public class SplashScreen extends InputAdapter implements Screen {

	private BallRoll game;
	private OrthographicCamera camera;
	private Viewport viewport;
	private SpriteBatch batch;
	private Texture backgroundTexture;

	
	public SplashScreen(BallRoll game) {
		this.game = game;
		camera = new OrthographicCamera();
		viewport = new FitViewport(Constants.SCENE_WIDTH, Constants.SCENE_HEIGHT, camera);
		batch = new SpriteBatch();
		backgroundTexture = new Texture(Gdx.files.internal(Constants.SPLASH_IMAGE_PATH));
	}

	
	public BallRoll getGame() {
		return game;
	}

	
	@Override
	public void dispose() {
		batch.dispose();
		backgroundTexture.dispose();
	}

	
	@Override
	public void resize(int width, int height) {
		viewport.update(width, height, false);
	}

	
	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(Color.WHITE.r, Color.WHITE.g, Color.WHITE.b, Color.WHITE.a);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		batch.setProjectionMatrix(camera.combined);

		batch.begin();

		int width = backgroundTexture.getWidth();
		int height = backgroundTexture.getHeight();
		float originX = width * 0.5f;
		float originY = height * 0.5f;

		// Render caveman centered on the screen
		batch.draw(backgroundTexture, // Texture
				-originX, -originY, // x, y
				originX, originY, // originX, originY
				width, height, // width, height
				Constants.WORLD_TO_SCREEN, Constants.WORLD_TO_SCREEN, // scaleX, scaleY
				0.0f, // rotation
				0, 0, // srcX, srcY
				width, height, // srcWidth, srcHeight
				false, false); // flipX, flipY

		batch.end();
		
		if(Gdx.input.justTouched()) {
            game.showGameScreen();
		}
	}


	@Override
	public void show() {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void pause() {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void resume() {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void hide() {
		// TODO Auto-generated method stub
		
	}

}
