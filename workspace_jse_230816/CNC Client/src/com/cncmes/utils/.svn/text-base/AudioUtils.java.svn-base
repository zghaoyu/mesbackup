package com.cncmes.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;

import javazoom.jl.player.Player;

public class AudioUtils {
	private AudioUtils(){}
	
	public static void playMP3(String mp3FilePath){
		File mp3File = new File(mp3FilePath);
		Player player = null;
		BufferedInputStream buffer = null;
		
		if(mp3File.isFile()){
			try {
				buffer = new BufferedInputStream(new FileInputStream(mp3File));
				player = new Player(buffer);
				player.play();
				buffer.close();
				player.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
