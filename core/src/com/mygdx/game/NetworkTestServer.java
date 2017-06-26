package com.mygdx.game;

import java.io.IOException;

import com.badlogic.gdx.ApplicationListener;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;

public class NetworkTestServer implements ApplicationListener {
	private Server server;
	private Kryo kryo;
	
	public NetworkTestServer() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void create() {
		server = new Server();
		server.start();
		try {
			server.bind(54555, 54777);
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		kryo = server.getKryo();
		kryo.register(String.class);

		// add the listener
		server.addListener(new Listener() {
			public void received(Connection connection, Object object) {
				if (object instanceof String) {
					String request = (String) object;
					System.out.println("Request: " + request);

					String response = "Message Received";
					server.sendToAllExceptTCP(connection.getID(), request);
					// Temporary
					connection.sendTCP(response);
				}
			}
		});
	}

	@Override
	public void resize(int width, int height) {
		// TODO Auto-generated method stub

	}

	@Override
	public void render() {
		// TODO Auto-generated method stub

	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub

	}

	@Override
	public void resume() {
		server.close();

	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

}
