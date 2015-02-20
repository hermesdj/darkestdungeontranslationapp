package fr.hermesdj.java.darkestdungeontranslationapp;

import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.filter.Filters;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;

public class CleanAllTask extends SwingWorker<Integer, Void> {
    private static final Logger LOG = LogManager.getLogger(CleanAllTask.class);
    DefaultTableModel model;
    Document doc;
    private String translated_language;

    public CleanAllTask(DefaultTableModel tableModel, Document document,
	    String translated_language) {
	model = tableModel;
	doc = document;
	this.translated_language = translated_language;
    }

    @Override
    protected Integer doInBackground() throws Exception {
	XPathFactory xFactory = XPathFactory.instance();
	Integer total = 0;

	for (int i = 0; i < model.getRowCount(); i++) {
	    String original = (String) model.getValueAt(i, 0);
	    String translated = (String) model.getValueAt(i, 1);
	    String key = (String) model.getValueAt(i, 2);
	    Integer subid = Integer.valueOf((String) model.getValueAt(i, 3));

	    if (original.equals(translated)) {
		model.setValueAt("", i, 1);

		XPathExpression<Element> expression = xFactory.compile(
			"//language[@id='" + translated_language
				+ "']/entry[@id='" + key + "']",
			Filters.element());

		List<Element> el = expression.evaluate(doc);

		el.get(subid).removeContent();
		model.fireTableCellUpdated(i, 1);

		LOG.info("Cleaned value for key " + key + " and subid " + subid
			+ ". Original value : " + original);

		Double progress = Math.ceil(((double) i / (double) model
			.getRowCount()) * 100);

		setProgress(progress.intValue());
		total += 1;
	    }
	}
	return total;
    }

    @Override
    protected void done() {
	try {
	    LOG.info("Done Cleaning table ! " + get() + " elements cleaned");
	} catch (InterruptedException e) {
	    LOG.error(e.getMessage(), e);
	} catch (ExecutionException e) {
	    LOG.error(e.getMessage(), e);
	}
	super.done();
    }

}
