package com.rekloosive.ballroll.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.rekloosive.ballroll.utils.Constants;

public class TestScreen extends InputAdapter implements Screen {
	
	//private static final int VIRTUAL_WIDTH = 1280;
	//private static final int VIRTUAL_HEIGHT = 720;
	
	private float VIRTUAL_WIDTH = Constants.SCENE_WIDTH*Constants.SCREEN_TO_WORLD;
	private float VIRTUAL_HEIGHT = Constants.SCENE_HEIGHT*Constants.SCREEN_TO_WORLD;
	
	private OrthographicCamera camera;
	private Viewport viewport;
	private SpriteBatch batch;
	private BitmapFont font;
	
	
	public TestScreen() {
		camera = new OrthographicCamera();
		camera.position.set(VIRTUAL_WIDTH  * 0.5f, VIRTUAL_HEIGHT * 0.5f, 0.0f);
		
		viewport = new FitViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera);
		batch = new SpriteBatch();
		
		font = new BitmapFont(Gdx.files.internal("impact.fnt"));
		font.setColor(Color.WHITE);
	}
	

	@Override
	public void show() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(0.6f, 0.6f, 0.6f, 1.0f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		batch.setProjectionMatrix(camera.combined);
		
		batch.begin();
		font.setColor(Color.WHITE);
		font.draw(batch, "This is a one line string", 0.0f, 50.0f);
		
		batch.end();
	}

	@Override
	public void resize(int width, int height) {
		viewport.update(width, height);
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

	@Override
	public void dispose() {
		batch.dispose();
		font.dispose();
	}

}
