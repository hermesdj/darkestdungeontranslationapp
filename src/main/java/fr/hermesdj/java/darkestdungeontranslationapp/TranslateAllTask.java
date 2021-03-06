package fr.hermesdj.java.darkestdungeontranslationapp;

import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.CDATA;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.filter.Filters;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;

import com.memetix.mst.language.Language;
import com.memetix.mst.translate.Translate;

public class TranslateAllTask extends SwingWorker<Integer, Void> {
    private static final Logger LOG = LogManager
	    .getLogger(TranslateAllTask.class);
    DefaultTableModel model;
    Document doc;
    String translatedLanguage;
    String originalLanguage;

    public TranslateAllTask(DefaultTableModel m, Document currentDocument,
	    String originalLanguage, String translatedLanguage) {
	model = m;
	doc = currentDocument;
	this.translatedLanguage = translatedLanguage;
	this.originalLanguage = originalLanguage;
    }

    @Override
    protected Integer doInBackground() throws Exception {
	Integer total = 0;

	XPathFactory xFactory = XPathFactory.instance();

	for (int i = 0; i < model.getRowCount(); i++) {
	    String original = (String) model.getValueAt(i, 0);
	    String translated = (String) model.getValueAt(i, 1);
	    String key = (String) model.getValueAt(i, 2);
	    Integer subid = Integer.valueOf((String) model.getValueAt(i, 3));

	    if (translated.equals("")) {

		XPathExpression<Element> expression = xFactory.compile(
			"//language[@id='" + translatedLanguage
				+ "']/entry[@id='" + key + "']",
			Filters.element());

		List<Element> el = expression.evaluate(doc);

		String newTranslation = Translate.execute(original,
			Language.valueOf(originalLanguage.toUpperCase()),
			Language.valueOf(translatedLanguage.toUpperCase()));

		el.get(subid).setContent(new CDATA(newTranslation));
		model.setValueAt(newTranslation, i, 1);
		model.fireTableCellUpdated(i, 1);

		LOG.info("Translated key " + key + " with subid " + subid
			+ ". Original value : " + original
			+ " Translated value : "
			+ el.get(subid).getContent().get(0));

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
	    LOG.info("Done translating all empty elements ! " + get()
		    + " elements translated");
	} catch (InterruptedException e) {
	    LOG.error(e.getMessage(), e);
	} catch (ExecutionException e) {
	    LOG.error(e.getMessage(), e);
	}
	super.done();
    }

}
