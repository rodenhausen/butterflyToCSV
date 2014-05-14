import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import au.com.bytecode.opencsv.CSVWriter;

public class Main {

	/**
	 * @param args
	 * @throws IOException
	 * @throws JDOMException
	 */
	public static void main(String[] args) throws JDOMException, IOException {
		LinkedHashMap<String, LinkedHashMap<String, List<String>>> taxa = new LinkedHashMap<String, LinkedHashMap<String, List<String>>>();

		File file = new File("mapQuery.xml");
		SAXBuilder sax = new SAXBuilder();
		Document doc = sax.build(file);
		List<Element> taxonEntries = doc.getRootElement()
				.getChild("TaxonEntries").getChildren("TaxonEntry");

		Set<String> ranks = new HashSet<String>();
		ranks.addAll(Arrays.asList(new String[] { "life", "domain", "kingdom",
				"phylum", "class", "order", "family", "subfamily", "genus",
				"species", "subspecies", "variety", "form", "group" }));

		for (Element taxonEntry : taxonEntries) {
			LinkedHashMap<String, List<String>> characterStates = new LinkedHashMap<String, List<String>>();

			StringBuilder nameBuilder = new StringBuilder();
			for (Element items : taxonEntry.getChildren("Items")) {

				String nameValue = items.getAttributeValue("name");
				if (ranks.contains(nameValue)) {
					nameBuilder.append(normalize(nameValue) + " ");

					for (Element item : items.getChildren("Item")) {
						nameBuilder.append(normalize(item.getValue()) + " ");
					}

				} else {
					characterStates.put(nameValue, new LinkedList<String>());

					for (Element item : items.getChildren("Item")) {
						characterStates.get(nameValue).add(normalize(item.getValue()));
					}
				}
			}
			taxa.put(nameBuilder.toString().trim(), characterStates);
		}
		
		int size = -1;
		for(String taxon : taxa.keySet()) {
			int currentSize = taxa.get(taxon).size();
			if(size == -1)
				size = currentSize;
			assert(size == currentSize);
		}
		
		CSVWriter writer = new CSVWriter(new FileWriter("mapQuery.csv"));
		
		for(String taxon : taxa.keySet()) {
			String[] line = new String[taxa.get(taxon).size() + 1];
			line[0] = "Taxon";
			
			int i=1;
			for(String character : taxa.get(taxon).keySet()) {
				line[i++] = character;
			}
			writer.writeNext(line);
			break;
		}
		
		for(String taxon : taxa.keySet()) {
			String[] line = new String[taxa.get(taxon).size() + 1];
			line[0] = taxon;
			
			int i=1;
			for(String character : taxa.get(taxon).keySet()) {
				line[i++] = listToValue(taxa.get(taxon).get(character));
			}
			writer.writeNext(line);
		}	
		
		writer.flush();
		writer.close();
	}

	private static String listToValue(List<String> states) {
		StringBuilder stateBuilder = new StringBuilder();
		for(String state : states) {
			stateBuilder.append(state + " ; ");
		}
		String result = stateBuilder.toString();
		return result.substring(0, result.length() - 3);
	}

	private static String normalize(String value) {
		return value.replaceAll("\n", "").replaceAll(" +", " ").trim();
		
	}
}
