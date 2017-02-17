package com;


import java.awt.EventQueue;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

import javax.swing.JFrame;

/**
 * 登录njuBras
 * @author hjg
 *
 */
public class Main{
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				Properties properties=new Properties();
				try {
					properties.load(Files.newInputStream(Paths.get("config.properties")));
				} catch (IOException e) {
					e.printStackTrace();
				}
				JFrame frame=new GUIFrame(properties.getProperty("account"),
						properties.getProperty("password"));
				frame.setTitle("南大校园网登录");
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				frame.setVisible(true);
			}
		});
	}
}
