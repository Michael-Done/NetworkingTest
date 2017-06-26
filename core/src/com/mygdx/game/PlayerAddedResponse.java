package com.mygdx.game;

import java.io.Serializable;

public class PlayerAddedResponse implements Serializable {
	private static final long serialVersionUID = 6369306932330693322L;

	public final int index;

	public PlayerAddedResponse(int index) {
		this.index = index;
	}

}
