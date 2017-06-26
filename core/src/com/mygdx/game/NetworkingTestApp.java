package com.mygdx.game;

import java.io.IOException;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextArea;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntIntMap;
import com.badlogic.gdx.utils.ObjectMap;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

public class NetworkingTestApp extends InputAdapter implements ApplicationListener {
	// User input things
	IntIntMap keys = new IntIntMap();
	// GUI things
	private OrthographicCamera camera;
	private SpriteBatch batch;
	private Skin skin;
	private Stage stage;
	private Label labelDetails;
	private Label labelMessage;
	private TextButton button;
	private TextArea textIPAddress;
	private ObjectMap<String, Texture> textures;
	// Networking things
	private Kryo kryoClient;
	private Client client;
	// Player data
	Array<Player> players;
	private int thisPlayerIndex;
	// Current scene
	private boolean isConnected = false;
	// Player connection stuff
	Player player;
	AddPlayerRequest connectRequest;

	private void initalizeTextures() {
		textures = new ObjectMap<String, Texture>();
		textures.put("Red.png", new Texture(Gdx.files.internal("Red.png")));
		textures.put("Green.png", new Texture(Gdx.files.internal("Green.png")));
	}

	@Override
	public void create() {
		players = new Array<Player>();
		camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		batch = new SpriteBatch();
		// Textures
		initalizeTextures();
		// Load our UI skin from file. Once again, I used the files included in
		// the tests.
		// Make sure default.fnt, default.png, uiskin.[atlas/json/png] are all
		// added to your assets
		skin = new Skin(Gdx.files.internal("Holo-dark-hdpi.json"));
		stage = new Stage();
		// Wire the stage to receive input, as we are using Scene2d in this
		// example
		Gdx.input.setInputProcessor(stage);

		// The following code loops through the available network interfaces
		// Keep in mind, there can be multiple interfaces per device, for
		// example
		// one per NIC, one per active wireless and the loopback
		// In this case we only care about IPv4 address ( x.x.x.x format )

		labelMessage = new Label("Enter IP to Connect", skin);
		button = new TextButton("Connect", skin);
		textIPAddress = new TextArea("", skin);

		Table table = new Table();
		table.add(labelDetails);
		table.row();
		table.add(labelMessage);
		table.row();
		table.add(button);
		table.row();
		table.add(textIPAddress).width(400);
		table.row();
		table.pack();
		// Add scene to stage
		stage.addActor(table);

		// the client that will connect to the server

		client = new Client();
		client.start();

		kryoClient = client.getKryo();
		Registration.registerClasses(kryoClient);

		client.addListener(new Listener() {
			public void received(Connection connection, Object object) {
				if (object instanceof String) {
					String response = (String) object;
					System.out.println("Response: " + response);
					if (!response.equals("Message Received")) {
						labelMessage.setText(response);
					}
				}
				if (object instanceof AddPlayerRequest) {
					AddPlayerRequest request = (AddPlayerRequest) object;
					players.add(request.player);
					assert (request.player.getIndex() == players.size);
				}
				if (object instanceof PlayerMoveRequest) {
					PlayerMoveRequest request = (PlayerMoveRequest) object;
					players.get(request.index).translate(request.delta);
				}
				if (object instanceof PlayerAddedResponse) {
					PlayerAddedResponse response = (PlayerAddedResponse) object;
					thisPlayerIndex = response.index;
					isConnected = true;
					setInputProcessorToThis();
					System.out.println("Response Recieved");
				}
			}
		});

		// Wire up a click listener to our button
		button.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				// When the button is clicked, connect to the server specified
				// by the ip
				player = new Player("Red.png", -1, 0, 0);
				connectRequest = new AddPlayerRequest(player);
				try {
					client.connect(5000, textIPAddress.getText(), 54555, 54777);
				} catch (IOException e) {
					labelMessage.setText("Connection Failed");
				}
				try {
					int i = client.sendTCP(connectRequest);
					System.out.println("ButtonPressed " + Integer.toString(i));
				} catch (Exception e) {
					e.printStackTrace(System.out);
				}
			}
		});
	}

	@Override
	public void dispose() {
		batch.dispose();
		client.close();
	}

	@Override
	public void render() {
		Gdx.gl.glClearColor(0.5f, 0.5f, 0.5f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		if (isConnected) {
			for (Player p : players) {
				batch.draw(textures.get(p.getTexture()), p.getLocation().x, p.getLocation().y);
			}
		} else {
			stage.draw();
		}
		batch.end();
		if (keys.containsKey(Keys.W)) {
			client.sendTCP(new PlayerMoveRequest(thisPlayerIndex, 0, 5));
		}
		if (keys.containsKey(Keys.S)) {
			client.sendTCP(new PlayerMoveRequest(thisPlayerIndex, 0, -5));
		}
		if (keys.containsKey(Keys.A)) {
			client.sendTCP(new PlayerMoveRequest(thisPlayerIndex, -5, 0));
		}
		if (keys.containsKey(Keys.D)) {
			client.sendTCP(new PlayerMoveRequest(thisPlayerIndex, 5, 0));
		}
	}

	public void setInputProcessorToThis() {
		Gdx.input.setInputProcessor(this);
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
	public boolean keyDown(int keycode) {
		keys.put(keycode, keycode);
		return true;
	}

	@Override
	public boolean keyUp(int keycode) {
		keys.remove(keycode, 0);
		return true;
	}

	@Override
	public boolean keyTyped(char character) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean scrolled(int amount) {
		return false;
	}
}