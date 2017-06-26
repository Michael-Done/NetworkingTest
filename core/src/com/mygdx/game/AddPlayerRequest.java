package com.mygdx.game;

public class AddPlayerRequest {
	public Player player;
	public AddPlayerRequest(Player p) {
		player = p;
	}
	public String toString(){
		return player.toString();
	}
}
