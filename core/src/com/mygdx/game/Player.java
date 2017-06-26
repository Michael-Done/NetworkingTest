package com.mygdx.game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;

public class Player {
	private Texture texture;
	private int index;
	private Vector2 location;

	public Player(Texture tex, int index) {
		setTexture(tex);
		setIndex(index);
	}

	public Player(Texture tex, int index, int x, int y) {
		this(tex, index, new Vector2(x, y));
	}

	public Player(Texture tex, int index, Vector2 loc) {
		this(tex, index);
		setLocation(loc);
	}
	public void translate(Vector2 delta){
		location.add(delta);
	}
	public Texture getTexture() {
		return texture;
	}

	public void setTexture(Texture texture) {
		this.texture = texture;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public Vector2 getLocation() {
		return location;
	}

	public void setLocation(Vector2 location) {
		this.location = location;
	}

	public void setLocation(int x, int y) {
		setLocation(new Vector2(x, y));
	}
	
	public String toString(){
		return ("Player " + index + " at location " + location);
	}
}
