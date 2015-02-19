package fr.hermesdj.java.darkestdungeontranslationapp;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Vector;

import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;

public class LoadFileTask extends SwingWorker<Void, Void> {
    private static final Logger LOG = LogManager.getLogger(LoadFileTask.class);
    private File file;
    private boolean blankOnly;
    private JProgressBar progressBar;
    private Vector<Vector<String>> tableData;
    private Document currentDocument;
    private String original_language;
    private String translation_language;
    private TranslationAppMain main;

    public LoadFileTask(TranslationAppMain m, File file, JProgressBar p,
	    Vector<Vector<String>> t, Document doc, String original_language,
	    String translated_language) {
	main = m;
	this.file = file;
	progressBar = p;
	tableData = t;
	currentDocument = doc;
	this.original_language = original_language;
	this.translation_language = translated_language;
	progressBar.setValue(0);
	setProgress(0);
    }

    @Override
    protected Void doInBackground() throws Exception {

	try {
	    LOG.info("Loading translation file " + file);

	    SAXBuilder builder = new SAXBuilder();

	    currentDocument = (Document) builder.build(file);
	    Element root = currentDocument.getRootElement();

	    LOG.info("Parsing source language " + original_language);

	    XPathFactory xFactory = XPathFactory.instance();
	    XPathExpression<Element> browseOriginal = xFactory.compile(
		    "//language[@id='" + original_language
			    + "']/entry[string-length(@id) > 0]/.[text()]",
		    Filters.element());

	    List<Element> originalElements = browseOriginal.evaluate(root);
	    LOG.info("This file contains " + originalElements.size()
		    + " elements to translate.");

	    LOG.info("Parsing " + originalElements.size() + " elements...");

	    tableData = new Vector<Vector<String>>();
	    int i = 0;
	    int subid = 0;
	    String lastid = "";
	    for (Element el : originalElements) {
		String id = el.getAttributeValue("id");

		List<Element> elems = xFactory.compile(
			"//language[@id='" + translation_language
				+ "']/entry[@id='" + id + "']",
			Filters.element()).evaluate(root);

		if (id.equals(lastid)) {
		    subid++;
		} else {
		    subid = 0;
		}

		if (!elems.get(subid).getText().isEmpty() && blankOnly) {
		    continue;
		}

		Vector<String> vector = new Vector<String>();
		vector.add(el.getText());
		vector.add(elems.get(subid).getText());
		vector.add(id);
		vector.add(String.valueOf(subid));

		tableData.add(vector);

		Double progress = Math
			.ceil(((double) i / (double) originalElements.size()) * 100);

		setProgress(progress.intValue());
		i++;
		lastid = id;
	    }
	} catch (JDOMException e) {
	    e.printStackTrace();
	} catch (IOException e) {
	    e.printStackTrace();
	} catch (RuntimeException e) {
	    e.printStackTrace();
	}

	return null;
    }

    @Override
    protected void done() {
	main.doneLoadingFile(tableData, currentDocument);
	super.done();
    }
}
