import java.nio.file.Paths;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.ArrayList;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.FSDirectory;


public class generateIndex {
	
	private static String indexPath;
	private static String dataPath;
	private static ArrayList<HashMap<String, String>> documents;
	
	public generateIndex() {
		// Empty Constructor
	}
	
	public generateIndex(String i, String d) {
		indexPath = i;
		dataPath = d;
	}
	
	public static void main(String[] args) throws Exception {
		
		indexPath = new String("/home/nitesh-jaswal/Study/Search/Project/Task1/index");
		dataPath = new String("/home/nitesh-jaswal/Study/Search/Project/Task1/data");
		
		parseTrectext myparser = new parseTrectext(dataPath);
		generateIndex obj = new generateIndex();
		
		documents = myparser.generateDocuments();
		System.out.println("Size of ArrayList: " + documents.size());
		Analyzer std = new StandardAnalyzer();
		
		// Clear folder before Indexing
		obj.clearIndexFolder();
		obj.writeIndex(std);
		
	}
	
	// Same as main except callable by object
	public void makeNewIndex(Analyzer analyzer) throws Exception  {
		parseData myparser = new parseData(dataPath);
		generateIndex obj = new generateIndex();
		
		documents = myparser.generateDocuments();
		System.out.println("Size of ArrayList: " + documents.size());
		
		obj.clearIndexFolder();
		obj.writeIndex(analyzer);
	}
	
	public boolean writeIndex(Analyzer analyzer) {
		boolean flag;
		try {
			System.out.println("Indexing to directory '" + indexPath + "'...");

			Directory dir = FSDirectory.open(Paths.get(indexPath));
			
			IndexWriterConfig iwc = new IndexWriterConfig(analyzer);

			iwc.setOpenMode(OpenMode.CREATE);

			IndexWriter writer = new IndexWriter(dir, iwc);

			for (HashMap<String, String> document : documents) {
				indexDoc(writer, document);
			}
			
			flag = true;
			writer.close();
			System.out.println("Done ...");
			
		} catch (IOException e) {
			flag = false;
			System.out.println(" caught a " + e.getClass()
					+ "\n with message: " + e.getMessage());
		}
		
		return(flag);
	}
	
	private static void indexDoc(IndexWriter writer, HashMap<String, String> document) throws IOException {
		Document lDoc = new Document();

		lDoc.add(new StringField("DOCNO", document.get("DOCNO"),
				Field.Store.YES));

		lDoc.add(new TextField("PRICE", document.get("PRICE"), Field.Store.YES));
		writer.addDocument(lDoc);
		
		lDoc.add(new TextField("STARS", document.get("STARS"), Field.Store.YES));
		writer.addDocument(lDoc);
		
		lDoc.add(new TextField("CAT", document.get("CAT"), Field.Store.YES));
		writer.addDocument(lDoc);
		
		lDoc.add(new TextField("REVIEW", document.get("REVIEW"), Field.Store.YES));
		writer.addDocument(lDoc);
	}
	

	
	public void clearIndexFolder() throws FileNotFoundException {
		//clear index folder
		File dir = new File(indexPath);
		for(File file: dir.listFiles()) 
		    if (!file.isDirectory()) 
		        file.delete();
	}
}
