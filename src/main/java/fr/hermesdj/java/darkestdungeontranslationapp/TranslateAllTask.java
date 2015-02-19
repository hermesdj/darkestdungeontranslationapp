package fr.hermesdj.java.darkestdungeontranslationapp;

import javax.swing.SwingWorker;

public class TranslateAllTask extends SwingWorker<Void, Void> {
    TranslationAppMain main;

    public TranslateAllTask(TranslationAppMain m) {
	main = m;
    }

    @Override
    protected Void doInBackground() throws Exception {
	return null;
    }

}
