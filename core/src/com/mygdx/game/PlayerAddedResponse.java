package com.mygdx.game;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.utils.Array;

public class PlayerAddedResponse implements Serializable {
	private static final long serialVersionUID = 6369306932330693322L;

	public final int index;
	public final Player[] currentPlayers;
	public PlayerAddedResponse(int index, Array<Player> currentPlayers) {
		this.index = index;
		
		List<Player> t = new ArrayList<Player>(currentPlayers.size);
		for(Player p: currentPlayers) {
			t.add(p);
		}
		
		this.currentPlayers = t.toArray(new Player[0]);
	}

}
