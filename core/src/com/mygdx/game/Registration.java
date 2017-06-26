package com.mygdx.game;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.serializers.JavaSerializer;

public class Registration {
	public static void registerClasses(Kryo kryo){
		kryo.register(String.class);
		kryo.register(Player.class, new JavaSerializer());
		kryo.register(PlayerMoveRequest.class, new JavaSerializer());
		kryo.register(AddPlayerRequest.class, new JavaSerializer());
		kryo.register(PlayerAddedResponse.class, new JavaSerializer());
		kryo.register(Vector2.class, new JavaSerializer());
	}

}
