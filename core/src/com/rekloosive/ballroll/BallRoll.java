package com.rekloosive.ballroll;

import com.badlogic.gdx.Game;
import com.rekloosive.ballroll.screens.GameScreen;
import com.rekloosive.ballroll.screens.SplashScreen;

public class BallRoll extends Game {
	
	private SplashScreen splashScreen;
	
	
	public BallRoll() {
		super();
	}
	
	@Override
	public void create() {
		splashScreen = new SplashScreen(this);
		setScreen(splashScreen);
	}
	
	public void showSplashScreen() {
		setScreen(splashScreen);
	}
	
	public void showGameScreen() {
		setScreen(new GameScreen(this));
		//setScreen(new TestScreen());
	}
}
