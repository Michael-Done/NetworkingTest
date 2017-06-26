package com.mygdx.game;

import java.io.Serializable;

public class AddPlayerRequest implements Serializable  {
	private static final long serialVersionUID = 7643033334188497399L;

	public Player player;
	public AddPlayerRequest(Player p) {
		player = p;
	}
	public String toString(){
		return "Request to add: " + player.toString();
	}
}
