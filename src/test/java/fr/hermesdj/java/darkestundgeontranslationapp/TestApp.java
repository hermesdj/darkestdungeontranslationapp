package fr.hermesdj.java.darkestundgeontranslationapp;

import java.awt.EventQueue;

import fr.hermesdj.java.darkestdungeontranslationapp.TranslationAppMain;

public class TestApp {

    public static void main(String[] args) {
	EventQueue.invokeLater(new Runnable() {
	    @Override
	    public void run() {
		TranslationAppMain window = new TranslationAppMain();
		window.setVisible(true);
	    }
	});
    }
}
