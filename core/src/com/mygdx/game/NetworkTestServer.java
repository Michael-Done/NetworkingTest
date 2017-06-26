package com.mygdx.game;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;

public class NetworkTestServer implements ApplicationListener {
	private OrthographicCamera camera;
	private SpriteBatch batch;
	private Skin skin;
	private Stage stage;
	private Server server;
	private Kryo kryo;
	private Label labelDetails;
	Array<Player> players;
	Table table;
	@Override
	public void create() {
		players = new Array<Player>();
		camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		batch = new SpriteBatch();
		// Load our UI skin from file. Once again, I used the files included in
		// the tests.
		// Make sure default.fnt, default.png, uiskin.[atlas/json/png] are all
		// added to your assets
		skin = new Skin(Gdx.files.internal("Holo-dark-hdpi.json"));
		stage = new Stage();
		// Wire the stage to receive input, as we are using Scene2d in this
		// example
		Gdx.input.setInputProcessor(stage);

		List<String> addresses = new ArrayList<String>();
		try {
			Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
			for (NetworkInterface ni : Collections.list(interfaces)) {
				for (InetAddress address : Collections.list(ni.getInetAddresses())) {
					if (address instanceof Inet4Address) {
						addresses.add(address.getHostAddress());
					}
				}
			}
		} catch (SocketException e) {
			e.printStackTrace();
		}

		// Print the contents of our array to a string. Yeah, should have used
		// StringBuilder
		String ipAddress = new String("");
		for (String str : addresses) {
			ipAddress = ipAddress + str + "\n";
		}

		// Now setup our scene UI
		// Create our controls
		labelDetails = new Label(ipAddress, skin);

		server = new Server();
		server.start();
		try {
			server.bind(54555, 54777);
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		kryo = server.getKryo();
		Registration.registerClasses(kryo);

		// add the listener
		server.addListener(new Listener() {
			public void received(Connection connection, Object object) {
				System.out.println("Request: " + object.getClass().toString());
				if (object instanceof String) {
					String request = (String) object;
					String response = "Message Received";
					server.sendToAllExceptTCP(connection.getID(), request);
					// Temporary
					connection.sendTCP(response);
				}
				if (object instanceof AddPlayerRequest) {
					AddPlayerRequest request = (AddPlayerRequest) object;
					System.out.println(request + " connected");
					request.player.setIndex(players.size);
					
					connection.sendTCP(new PlayerAddedResponse(players.size, players));
					players.add(request.player);
					server.sendToAllTCP(request);
					System.out.println("Connection Request Recieved");
				}
				if (object instanceof PlayerMoveRequest) {
					PlayerMoveRequest request = (PlayerMoveRequest) object;
					players.get(request.index).translate(request.delta);
					server.sendToAllTCP(request);
					// connection.sendTCP("Successfully Moved Player " +
					// request.index);
				}
			}
		});
		table = new Table();
		table.add(labelDetails);
		table.row();
		table.row();
		table.pack();
		// Add scene to stage
		stage.addActor(table);
	}

	@Override
	public void resize(int width, int height) {
		// TODO Auto-generated method stub

	}

	@Override
	public void render() {
		Gdx.gl.glClearColor(0.5f, 0.5f, 0.5f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		stage.draw();
		batch.end();
		//System.out.println(Arrays.toString(server.getConnections()));
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub

	}

	@Override
	public void resume() {

	}

	@Override
	public void dispose() {
		batch.dispose();
		server.close();
	}

}
