package org.jboss.test;

import java.io.File;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

/**
 * Gets non-empty FSP labels.
 * 
 * @author Josef Cacek
 */
public class App {

	// Public methods --------------------------------------------------------

	public static void main(String[] args) throws DocumentException {
		if (args.length != 1) {
			System.out.println("Usage:\n java -jar fsp-app.jar /path/to/fsp.xml");
			System.exit(1);
		}
		SAXReader reader = new SAXReader();
		Document document = reader.read(new File(args[0]));
		@SuppressWarnings("unchecked")
		List<Node> list = document.selectNodes("//tsfi[sfrs/sfr]/label[string-length() > 0]");
		for (Node node : list) {
			System.out.println(node.getText());
		}
	}
	// Protected methods -----------------------------------------------------

	// Private methods -------------------------------------------------------

	// Embedded classes ------------------------------------------------------
}
