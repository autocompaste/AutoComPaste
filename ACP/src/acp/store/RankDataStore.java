package acp.store;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RankDataStore handles the creating of the file that stores the user behaviors as well as 
 * retrieving and storing of the user behavior at the start of ACP and end of ACP respectively.
 * 
 * @author Ng Chin Hui
 *
 */
public class RankDataStore {

	final static Logger logger = LoggerFactory.getLogger(RankDataStore.class);
	
	private ArrayList<String> documentList;
	private ArrayList<Double> scoreList;
	private String url;
	
	/**
	 * Default Constructor
	 */
	public RankDataStore(){
		url = getURL();
		documentList = new ArrayList<String>();
		scoreList = new ArrayList<Double>();
	}
	
	/**
	 * To enable testing mode for JUnit Test cases.
	 */
	public void testing_mode(){
		url = "MachineLearningTestData.txt";
	}
	
	/**
	 * Gets the URL of the file for storing of user behavior in user profile directory.
	 * 
	 * @return	URL of the file that stores the user behavior
	 */
	private String getURL(){
		return System.getProperty("user.home")+"\\Documents\\AutoComPaste\\MachineLearningData.txt";
	}
	
	/**
	 * Retrieves past user behavior data from file.
	 * The method retrieves all the data from the file and stores it into temporary store variables.
	 * 
	 * This method is called once at the start of ACP.
	 */
	public void retrieveRankData(){
		logger.info("retrieveMLData");
		
		documentList = new ArrayList<String>();
		scoreList = new ArrayList<Double>();
		Scanner scanner = null;
		
		/* setup scanner and retrieve data from file */
		try {	
			
			File file = new File(url);
			
			if(file.exists()){
				
				scanner = new Scanner(file);
	            while (scanner.hasNext()){            	
	            	String line = scanner.nextLine();             	
	            	StringTokenizer st = new StringTokenizer(line, ";");  
	            	documentList.add(st.nextToken());            	
	            	scoreList.add(Double.parseDouble(st.nextToken()));            	
	            }
	            scanner.close();
	            
			}else{
				
				file.createNewFile();
				
			}
		}catch(Exception e){
			logger.error("Rank Data Retrieve Error");
		}
	}
	
	/**
	 * Stores user behavior data into the file. 
	 * The method stores all the data that are in the two temporary store variables into the file.
	 * 
	 * This method is called once at the end of ACP.
	 */
	public void storeUserBehaviour(){
		logger.info("storeUserBehaviour");			
		
		/* setup bufferedWriter and write to file */
		try{
			
			File file = new File(url);
			BufferedWriter output = new BufferedWriter(new FileWriter(file.getAbsoluteFile()));
			
			for(int i=0; i<documentList.size(); i++){
				String s = documentList.get(i) + ";" + scoreList.get(i);
				output.write(s);
				output.newLine();				
			}
			
			output.close();
			
		}catch(Exception e) {
			logger.error("Rank Data Write Error");
		}
	}
	
	/**
	 * Updates the variables that are used as temporary store for user behavior data during runtime.
	 * 
	 * @param source	the name of the source document
	 * @param score		the score of the source document
	 */
	public void updateDocumentList(String source, double score){
		documentList.add(source);
		scoreList.add(score);
	}
	
	/**
	 * Removes the entry from temporary store variables for user behavior data during runtime.
	 * 
	 * @param index		the index of the entry in the temporary store variable (documentLsit)
	 */
	public void removeDocumentFromList(int index){
		documentList.remove(index);
		scoreList.remove(index);
	}
	
	/**
	 * Gets the list of document. One of the two temporary store variables.
	 * 
	 * @return		the list of documents
	 */
	public ArrayList<String> getDocumentList(){
		return documentList;
	}
	
	/**
	 * Gets the list of scores. One of the two temporary store variables.
	 * 
	 * @return		the list of scores
	 */
	public ArrayList<Double> getScoreList(){
		return scoreList;
	}
	
}
