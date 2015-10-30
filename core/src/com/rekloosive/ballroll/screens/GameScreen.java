package com.rekloosive.ballroll.screens;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Peripheral;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.rekloosive.ballroll.BallRoll;
import com.rekloosive.ballroll.utils.Constants;

public class GameScreen extends InputAdapter implements Screen, ContactListener {

	private BallRoll game;

	private OrthographicCamera fontCamera;
	private Viewport fontViewport;

	private Viewport viewport;
	private Vector3 point = new Vector3();
	private SpriteBatch batch;
	private Texture levelTexture;
	private Texture ballTexture;
	private Sound bounce;

	// General Box2D
	private Box2DDebugRenderer debugRenderer;
	private BodyDef defaultDynamicBodyDef;
	private World world;

	// Circles
	private Body ball;
	private CircleShape circle;

	private List<Body> groundBodies = new ArrayList<Body>();

	private List<float[]> levelFloorWidthConifgs = new ArrayList<float[]>();
	private List<float[]> levelFloorPositionConifgs = new ArrayList<float[]>();

	private float VIRTUAL_WIDTH = Constants.SCENE_WIDTH * Constants.SCREEN_TO_WORLD;
	private float VIRTUAL_HEIGHT = Constants.SCENE_HEIGHT * Constants.SCREEN_TO_WORLD;

	private boolean useAccelerometer = false;
	private boolean gameOver = false;
	private BitmapFont font;
	private float gameTimeElapsed = 0f;
	private float gameOverTimeElapsed = 0f;

	public GameScreen(BallRoll game) {

		this.game = game;
		this.useAccelerometer = Gdx.input.isPeripheralAvailable(Peripheral.Accelerometer);
		createFloorConfigs();

		viewport = new FitViewport(Constants.SCENE_WIDTH, Constants.SCENE_HEIGHT);
		viewport.getCamera().position.set(viewport.getCamera().position.x + Constants.SCENE_WIDTH * 0.5f, viewport.getCamera().position.y + Constants.SCENE_HEIGHT * 0.5f, 0);
		viewport.getCamera().update();
		
		levelTexture = new Texture(Gdx.files.internal(Constants.LEVEL_IMAGE_PATH));
		ballTexture = new Texture(Gdx.files.internal(Constants.BALL_IMAGE_PATH));
		bounce = Gdx.audio.newSound(Gdx.files.internal(Constants.BALL_SOUND_PATH));
		
		fontCamera = new OrthographicCamera();
		fontCamera.position.set(VIRTUAL_WIDTH * 0.5f, VIRTUAL_HEIGHT * 0.5f, 0.0f);
		fontViewport = new FitViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, fontCamera);

		batch = new SpriteBatch();
		font = new BitmapFont(Gdx.files.internal("impact.fnt"));

		Gdx.input.setInputProcessor(this);

		// Create Physics World
		world = new World(new Vector2(0, -9.8f), true);

		// Tweak debug information
		debugRenderer = new Box2DDebugRenderer(true, /* draw bodies */
				false, /* don't draw joints */
				true, /* draw aabbs */
				true, /* draw inactive bodies */
				false, /* don't draw velocities */
				true /* draw contacts */);

		// Creates a ground to avoid objects falling forever
		createInitialFloors();

		// Get the ball rolling...
		defaultDynamicBodyDef = new BodyDef();
		defaultDynamicBodyDef.type = BodyType.DynamicBody;
		circle = new CircleShape();
		circle.setRadius(Constants.BALL_RADIUS);
		FixtureDef circleFixtureDef = new FixtureDef();
		circleFixtureDef.shape = circle;
		circleFixtureDef.density = 0.7f;
		circleFixtureDef.friction = 0.4f;
		circleFixtureDef.restitution = 0.4f;
		defaultDynamicBodyDef.position.set(Constants.SCENE_WIDTH * 0.5f, Constants.SCENE_HEIGHT);
		ball = world.createBody(defaultDynamicBodyDef);
		ball.createFixture(circleFixtureDef);
		
		if(!useAccelerometer) {
			ball.applyLinearImpulse(new Vector2(0.1f, 0f), ball.getPosition(), true);
		}
		
		world.setContactListener(this);
		
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		if(!Gdx.input.isPeripheralAvailable(Peripheral.Accelerometer)) {
			if (button == Input.Buttons.LEFT && !gameOver) {
				viewport.getCamera().unproject(point.set(screenX, screenY, 0));
				if (point.x < ball.getPosition().x) {
					ball.applyLinearImpulse(new Vector2(0.2f, 0f), ball.getPosition(), true);
				} else {
					ball.applyLinearImpulse(new Vector2(-0.2f, 0f), ball.getPosition(), true);
				}
				return true;
			}
		}
		return false;
	}

	
	public BallRoll getGame() {
		return game;
	}

	@Override
	public void show() {
		bounce.play();
	}

	@Override
	public void render(float delta) {
		
		checkGameOver();

		Gdx.gl.glClearColor(Color.WHITE.r, Color.WHITE.g, Color.WHITE.b, Color.WHITE.a);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		if (gameOver) {
			gameOverTimeElapsed = gameOverTimeElapsed + delta;
			batch.setProjectionMatrix(fontViewport.getCamera().combined);
			batch.begin();
			font.setColor(Color.BLACK);
			font.draw(batch, "GAME OVER", 0.0f, VIRTUAL_HEIGHT * 0.5f, VIRTUAL_WIDTH, Align.center, true);
			font.draw(batch, "FINAL SCORE: " + Integer.toString(Math.round(gameTimeElapsed * 10f)), 50f, VIRTUAL_HEIGHT - 25f);
			batch.end();
			if (gameOverTimeElapsed > 3f) {
				game.showSplashScreen();
			}
		} else {
			gameTimeElapsed = gameTimeElapsed + delta;
			batch.setProjectionMatrix(fontViewport.getCamera().combined);
			batch.begin();
			font.setColor(Color.BLACK.r, Color.BLACK.g, Color.BLACK.b, 0.3f);
			font.getData().setScale(1.5f);
			font.draw(batch, Integer.toString(Math.round(gameTimeElapsed * 10f)), 50f, VIRTUAL_HEIGHT - 25f);
			batch.end();
		}
		

		// If the game doesn't render at 60fps, the physics will go mental.
		// That'll be covered in Box2DFixedTimeStepSample
		world.step(1 / 60f, 6, 2);

		float angle = 0.05f;
		for (Body body : groundBodies) {
			
			float bodyWidth = (Float)body.getUserData();
			float xPos = body.getPosition().x - bodyWidth*0.5f;
			float yPos = body.getPosition().y - Constants.GROUND_HEIGHT*0.5f;
			batch.setProjectionMatrix(viewport.getCamera().combined);
			batch.begin();
			batch.draw(levelTexture, 
					xPos, yPos,  
					bodyWidth*0.5f, Constants.GROUND_HEIGHT*0.5f, 
					bodyWidth, Constants.GROUND_HEIGHT,  
					1f, 1f, 
					(float) Math.toDegrees(body.getAngle()), 
					0,0, 
					levelTexture.getWidth(), levelTexture.getHeight(),
					false,false);
			batch.end();
			 
			if (gameOver) {
				angle = angle * -1f;
				body.setType(BodyType.DynamicBody);
				body.applyAngularImpulse(angle, true);
				body.setGravityScale(1f);
			} else {
				float newVelocity = 0.4f + gameTimeElapsed/100f;
				body.setLinearVelocity(0f, newVelocity);
			}
		}
		
		float bodyWidth = Constants.BALL_RADIUS*2f;
		float xPos = ball.getPosition().x - Constants.BALL_RADIUS;
		float yPos = ball.getPosition().y - Constants.BALL_RADIUS;
		batch.setProjectionMatrix(viewport.getCamera().combined);
		batch.begin();
		batch.draw(ballTexture, 
				xPos, yPos,  
				Constants.BALL_RADIUS, Constants.BALL_RADIUS, 
				bodyWidth, bodyWidth,  
				1f, 1f, 
				(float) Math.toDegrees(ball.getAngle()), 
				0,0, 
				ballTexture.getWidth(), ballTexture.getHeight(),
				false,false);
		batch.end();

		freeLevels();
		
		if(useAccelerometer) {
			float yacc = Gdx.input.getAccelerometerY();
			float ballVelocity = yacc * (1f + gameTimeElapsed/100f);
			ball.setLinearVelocity(ballVelocity, ball.getLinearVelocity().y);
		}

		//debugRenderer.render(world, viewport.getCamera().combined);

	}
	

	@Override
	public void resize(int width, int height) {
		viewport.update(width, height);
		fontViewport.update(width, height);
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
		bounce.dispose();
		ballTexture.dispose();
		levelTexture.dispose();
		debugRenderer.dispose();
		batch.dispose();
		circle.dispose();
		world.dispose();
		font.dispose();
	}

	
	private void createFloorConfigs() {

		float[] widths = new float[] { 5.8f, 5.8f };
		levelFloorWidthConifgs.add(widths);
		float[] positions = new float[] { 2.4f, 10.4f };
		levelFloorPositionConifgs.add(positions);

		widths = new float[] { 11f };
		levelFloorWidthConifgs.add(widths);
		positions = new float[] { 5.5f };
		levelFloorPositionConifgs.add(positions);

		widths = new float[] { 11f };
		levelFloorWidthConifgs.add(widths);
		positions = new float[] { 7.5f };
		levelFloorPositionConifgs.add(positions);

		widths = new float[] { 3f, 3f, 4.4f };
		levelFloorWidthConifgs.add(widths);
		positions = new float[] { 1.5f, 5.7f, 10.6f };
		levelFloorPositionConifgs.add(positions);

		widths = new float[] { 2.5f, 2.5f, 2.5f };
		levelFloorWidthConifgs.add(widths);
		positions = new float[] { 2f, 6f, 10f };
		levelFloorPositionConifgs.add(positions);

		widths = new float[] { 3f, 2.5f, 4f };
		levelFloorWidthConifgs.add(widths);
		positions = new float[] { 1.5f, 6f, 10.8f };
		levelFloorPositionConifgs.add(positions);

		widths = new float[] { 7f, 4.6f };
		levelFloorWidthConifgs.add(widths);
		positions = new float[] { 3.5f, 10.5f };
		levelFloorPositionConifgs.add(positions);

		widths = new float[] { 4.6f, 7f };
		levelFloorWidthConifgs.add(widths);
		positions = new float[] { 2.3f, 9.3f };
		levelFloorPositionConifgs.add(positions);

	}

	
	private void createRandomFloor(float initialYPos) {
		int ran = MathUtils.random(0, levelFloorWidthConifgs.size() - 1);
		// System.out.println("Making level " + ran);
		makeLevel(initialYPos, levelFloorWidthConifgs.get(ran), levelFloorPositionConifgs.get(ran));
	}

	
	private void createInitialFloors() {
		float position = Constants.SCENE_HEIGHT - Constants.LEVEL_HEIGHT * 3f;
		float lowest = 0f - Constants.LEVEL_HEIGHT * 3f;
		while (position >= lowest) {
			createRandomFloor(position);
			position = position - Constants.LEVEL_HEIGHT;
		}
	}

	
	private void makeLevel(float initialYPos, float[] floorWidths, float[] floorXPositions) {

		float halfGroundHeight = Constants.GROUND_HEIGHT * 0.5f;

		for (int i = 0; i < floorWidths.length; i++) {

			float floorWidth = floorWidths[i];
			float floorXPosition = floorXPositions[i];

			/*
			 * BodyDef groundBodyDef = new BodyDef(); groundBodyDef.type =
			 * BodyType.KinematicBody; //groundBodyDef.gravityScale = 0f;
			 * groundBodyDef.position.set(floorXPosition, initialYPos);
			 * 
			 * Body groundBody = world.createBody(groundBodyDef);
			 * //groundBody.setGravityScale(1f);x PolygonShape groundBox = new
			 * PolygonShape(); groundBox.setAsBox(floorWidth*0.5f,
			 * halfGroundHeight);
			 * 
			 * FixtureDef groundBoxFixtureDef = new FixtureDef();
			 * groundBoxFixtureDef.shape = groundBox;
			 * groundBoxFixtureDef.density = 0.5f; groundBoxFixtureDef.friction
			 * = 0.6f; groundBoxFixtureDef.restitution = 0.5f;
			 * 
			 * groundBody.createFixture(groundBoxFixtureDef);
			 * groundBox.dispose(); groundBodies.add(groundBody);
			 */

			BodyDef groundBodyDef = new BodyDef();
			groundBodyDef.type = BodyType.KinematicBody;

			PolygonShape groundBox = new PolygonShape();
			groundBox.setAsBox(floorWidth * 0.5f, halfGroundHeight);

			FixtureDef groundBoxFixtureDef = new FixtureDef();
			groundBoxFixtureDef.shape = groundBox;
			groundBoxFixtureDef.density = 1f;
			groundBoxFixtureDef.friction = 0.6f;
			groundBoxFixtureDef.restitution = 0f;

			groundBodyDef.position.set(floorXPosition, initialYPos);
			Body groundBody = world.createBody(groundBodyDef);
			groundBody.setGravityScale(0f);
			groundBody.createFixture(groundBoxFixtureDef);
			groundBody.setUserData(floorWidth);

			groundBox.dispose();
			groundBodies.add(groundBody);
		}

	}

	private void freeLevels() {
		if (gameOver) {
			if (!groundBodies.isEmpty()) {
				// levels will be falling off the screen
				List<Body> removeMe = new ArrayList<Body>();
				for (Body groundBody : groundBodies) {
					if ((groundBody.getPosition().y + Constants.GROUND_HEIGHT * 0.5f) < 0f) {
						removeMe.add(groundBody);
					}
				}
				for (Body groundBody : removeMe) {
					groundBodies.remove(groundBody);
					world.destroyBody(groundBody);
				}
			}
		} else {
			float lowest = Constants.SCENE_HEIGHT - Constants.GROUND_HEIGHT * 0.5f;
			List<Body> removeMe = new ArrayList<Body>();
			for (Body groundBody : groundBodies) {
				if (groundBody.getPosition().y < lowest) {
					lowest = groundBody.getPosition().y;
				}
				if ((groundBody.getPosition().y - Constants.GROUND_HEIGHT * 0.5f) > Constants.SCENE_HEIGHT) {
					removeMe.add(groundBody);
				}
			}
			boolean newFloor = false;
			for (Body groundBody : removeMe) {
				groundBodies.remove(groundBody);
				world.destroyBody(groundBody);
				newFloor = true;
			}
			if (newFloor) {
				createRandomFloor(lowest - Constants.LEVEL_HEIGHT);
			}
		}

	}

	private void checkGameOver() {
		if (((ball.getPosition().y - Constants.BALL_RADIUS) > Constants.SCENE_HEIGHT) || // Top limit
				(ball.getPosition().y + Constants.BALL_RADIUS) < 1.0f || // Bottom limit
				(ball.getPosition().x - Constants.BALL_RADIUS) > Constants.SCENE_WIDTH || // Right limit
				(ball.getPosition().x + Constants.BALL_RADIUS) < 0) { // Left limit
			gameOver = true;
		}
	}

	@Override
	public void beginContact(Contact contact) {
		Fixture fixtureA = contact.getFixtureA();
		Fixture fixtureB = contact.getFixtureB();
		Body bodyA = fixtureA.getBody();
		Body bodyB = fixtureB.getBody();
		if(ball.equals(bodyA) || ball.equals(bodyB)) {
			bounce.play(0.25f);
		}
	}

	@Override
	public void endContact(Contact contact) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void preSolve(Contact contact, Manifold oldManifold) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void postSolve(Contact contact, ContactImpulse impulse) {
		// TODO Auto-generated method stub
		
	}

}
