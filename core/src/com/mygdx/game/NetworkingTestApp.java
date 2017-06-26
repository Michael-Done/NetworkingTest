package com.mygdx.game;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
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
import com.esotericsoftware.kryonet.*;
import com.esotericsoftware.kryo.Kryo;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

public class NetworkingTestApp implements ApplicationListener {
	private OrthographicCamera camera;
	private SpriteBatch batch;
	private Skin skin;
	private Stage stage;
	private Label labelDetails;
	private Label labelMessage;
	private TextButton button;
	private TextArea textIPAddress;
	private Texture red;
	private Texture green;
	// Networking things

	private Kryo kryoClient;
	private Client client;
	// Player data
	Array<Player> players;
	private int thisPlayerIndex;

	@Override
	public void create() {
		players = new Array<Player>();
		camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		batch = new SpriteBatch();
		// Textures
		red = new Texture(Gdx.files.internal("Red.png"));
		green = new Texture(Gdx.files.internal("Green.png"));
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

		labelMessage = new Label("Hello world", skin);
		button = new TextButton("Send message", skin);
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

		// Now we create a thread that will listen for incoming socket
		// connections

		client = new Client();
		client.start();
		try {
			client.connect(5000, "127.0.0.1", 54555, 54777);
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		kryoClient = client.getKryo();
		kryoClient.register(String.class);
		kryoClient.register(Player.class);
		kryoClient.register(PlayerMoveRequest.class);
		kryoClient.register(AddPlayerRequest.class);
		kryoClient.register(PlayerAddedResponse.class);

		Player player = new Player(red, -1, 0, 0);
		AddPlayerRequest connectRequest = new AddPlayerRequest(player);
		client.sendTCP(connectRequest);

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
				}
			}
		});

		// Wire up a click listener to our button
		button.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				// When the button is clicked, get the message text or create a
				// default string value
				// String textToSend = new String();
				// if (textMessage.getText().length() == 0)
				// textToSend = "Doesn't say much but likes clicking buttons";
				// else
				// textToSend = textMessage.getText();
				// try {
				// client.connect(5000, textIPAddress.getText(), 54555, 54777);
				// } catch (IOException e) {
				// e.printStackTrace();
				// }
				// client.sendTCP(textToSend);

			}
		});
	}

	@Override
	public void dispose() {
		batch.dispose();
	}

	@Override
	public void render() {
		Gdx.gl.glClearColor(0.5f, 0.5f, 0.5f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		for (Player p : players) {
			batch.draw(p.getTexture(), p.getLocation().x, p.getLocation().y);
		}
		// stage.draw();
		batch.end();
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
}