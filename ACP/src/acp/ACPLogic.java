package acp;

import gate.util.GateException;

import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import acp.beans.Preference;
import acp.beans.ProcessedText;
import acp.beans.Suggestion;
import acp.beans.entity.Sentence;
import acp.beans.entity.Source;
import acp.manager.DataManager;
import acp.manager.RankManager;
import acp.manager.NLPManager;
import acp.manager.PreferenceManager;
import acp.manager.SortManager;

/**
 * This component acts as a controller: receive requests from both the Java UI and the external
 * Plugins, validate the input based on the business rules, and direct them to the appropriate 
 * managers for further processing. 
 * 
 * @author Loke Yan Hao
 */
public class ACPLogic extends Observable{
	
	final static Logger logger = LoggerFactory.getLogger(ACPLogic.class);
	
	public enum State{
		IDLE,
		PROCESSING
	};
	
	private static ACPLogic instance;
	private State currentState;
	
	// Initial Manager classes
	private DataManager dataManager;
	private RankManager rankManager;
	private NLPManager nlpManager;
	private SortManager sortManager;
	private PreferenceManager prefManager;
	
	/**
	 * This constructor is meant to be private to implement the singleton
	 * pattern. So throughout the lifetime of the system, there can only be
	 * one instance of ACPLogic. The only method that can call this constructor
	 * is the getInstance() method in this class. 
	 */
	private ACPLogic(){
		createACPUserDirectory();
		
		dataManager = new DataManager();
		rankManager = new RankManager();
		nlpManager = new NLPManager();
		sortManager = new SortManager();
		prefManager = new PreferenceManager();
		
		currentState = State.IDLE;
	}
	
	/**
	 * Returns the reference to the single instance of ACPLogic.  
	 * <p>
	 * This method is a standard method for singleton class
	 *
	 * @return      the reference to the single instance of ACPLogic
	 */
	public static ACPLogic getInstance(){
		if(instance==null){
			instance = new ACPLogic();
		}
		
		return instance;
	}
	
	/**
	 * Returns true if it is able to initialise all the libraries successfully. 
	 * <p>
	 * This method may return false if it is unable to initialise a certain library.
	 * If that is the case, the program cannot continue executing. The calling method
	 * should prompt an error message and quit the system.  
	 *
	 * @return      the result of this operation.
	 */
	public boolean initialiseLibraries(){ 
		logger.info("Initializing libraries");
		
		try {
			// Set the file path location for Gate Library
			Properties props = System.getProperties();
			props.setProperty("gate.home", "GATE");
			
			dataManager.initialiseHibernate();
			nlpManager.initializingGate();
		} catch (MalformedURLException | GateException e) {
			logger.error("Critical Error: Gate Initialization Failed.");
			return false;
		} catch (Throwable ex){
			logger.error("Critical Error: Hibernate Initialization Failed.");
			return false;
		}

		logger.info("Initializing complete");
		return true;
	}
	
	/**
	 * Process and store the text extracted from opening windows (source document).  
	 * The rawText argument is the content of the source document. The sourceName 
	 * argument is the human readable identity name of the source document. The URL
	 * argument is the unique identity to identify the source document.
	 * <p>
	 * This method does not return any result. However if an error occur, GateException
	 * is catch, and since this method is called by a background process, it will be ignored.
	 *
	 * @param  rawText    	the content of the source document. For now, it must be HTML document.
	 * @param  sourceName 	the name for the source document.
	 * @param  url			the unique identity locator to identify the source document.
	 */
	synchronized public void processRawText(String rawText, String sourceName, String url){
		currentState = State.PROCESSING;
		
		setChanged();
	    notifyObservers();

	    Source source = dataManager.retrieveSource(url);
	    if(source == null){ // if there is no existing source
			try {
				ProcessedText text = nlpManager.processText(rawText,sourceName, url);
				dataManager.storeProcessedText(text);
				rankManager.addNewEntry(sourceName);
			} catch (GateException e) {
				e.printStackTrace();
			}
	    }
		
	    currentState = State.IDLE;
		setChanged();
	    notifyObservers();
	}
	
	/**
	 * Using prefix search to retrieve a list of suggestions based on the user input. If prefix
	 * search returns 0 result, it will perform entity search using the last word as the search 
	 * keyword. The userInput argument is the text entered by user in a text-editor. If the 
	 * autoTrigger argument is false, it will return all the suggestions retrieved. If the 
	 * autoTrigger argument is true, it will only return the list of suggestions when the 
	 * number of suggestions does not exceed the threshold specified in User Preference.
	 * <p>
	 * The result is sorted based on user preference from the PreferenceManager and the 
	 * user behaviors learned by the machine learning algorithm. This method returns 
	 * an empty list of suggestions if autoTrigger is true and the actual number of 
	 * suggestion in the suggestion list exceed the threshold specified in User 
	 * Preference (Suggestion Threshold). 
	 *
	 * @param  userInput  	the text entered by user in a text-editor.
	 * @param  autoTrigger	the trigger mode.
	 * @return      		a list of suggestions returned from either prefix search or entity search.
	 * @see 				Suggestion
	 */
	public List<Suggestion> requestSuggestion(String userInput, boolean autoTrigger){
		Preference pref = getUserPreference();
		List<Suggestion> suggestions =  dataManager.searchBasedOnRequest(userInput, pref, autoTrigger);
		
		if(suggestions.size() > 0){
			Suggestion suggestion = suggestions.get(0);
			if(suggestion instanceof Sentence){
				suggestions =  sortManager.sortSearchResults(-1, null, suggestions, rankManager.getRankingModel(), userInput);
			}else{
				int entityTypePriorty = nlpManager.findContext(userInput);
				suggestions =  sortManager.sortSearchResults(entityTypePriorty, null, suggestions, rankManager.getRankingModel(), userInput);
			}
		}

		return suggestions;
	}
	
	/**
	 * To inform the system that user has selected a specific suggestion for 
	 * machine learning purposes. The sourceName argument is the selected 
	 * suggestion's source name.
	 *
	 * @param  suggestion  	the source name of the selected suggestion.
	 */
	public void chooseSuggestion(Suggestion suggestion){
		logger.info("Select source: " + suggestion.getSource().getName());
		rankManager.learnUserBehaviour(((Sentence)suggestion).getParagraphID(), ((Sentence)suggestion).getParentOrder(), suggestion.getSource().getName());
	}
	
	/**
	 * Retrieve the next paragraph, sentence or word based on the current selected suggestion. 
	 * The id argument denote which sentence/paragraph it will refer from. The type argument is 
	 * the type that user wish to request. The type can be one of the following: 
	 * paragraph, sentence or word. 
	 *
	 * @param  id			the id of the sentence/paragraph it will refer from.
	 * @param  type		 	the type that user wish to extend next. (Refer to Suggestion constant variable)
	 * @return  			the next suggestion.
	 * @see 				Suggestion 		
	 */
	public Suggestion requestExtendSuggestion(int id, int type){
		return dataManager.retrieveSubsequentText(id, type);
	}

	/**
	 * Remove all the text extracted from the specified opening window (source document).
	 * The sourceName argument is the name of the source document. 
	 *
	 * @param  url  the name of the source document.
	 */
	synchronized public void closeSourceDocument(String url){
		Source source = dataManager.retrieveSource(url);
		if(source != null){
			rankManager.removeRankData(source.getName());
			dataManager.removeData(url);
		}
	}
	
	/**
	 * Update the ranking of the opening window (source document).
	 * Having a higher source document rank mean that the source document have higher 
	 * sorting order, and will be listed at the top few entries in the auto-completion 
	 * suggestion box.
	 * <p>
	 * This method is called by ACPBackground class to give higher ranking for 
	 * source documents which are recently viewed.
	 *
	 * @param  sourceName  the source name of the source document.
	 */
	public void changeRanks(String sourceName){
		rankManager.changeRanks(sourceName);
	}
	
	/**
	 * Retrieve the user preference from the configuration file.
	 * <p>
	 * This method always returns a preference object, whether or not the 
	 * configuration file exists.  
	 *
	 * @return      the current user preference options.
	 * @see 		Preference
	 */
	public Preference getUserPreference(){
		return prefManager.getUserPreference();
	}
	
	/**
	 * Save the updated user preference to the configuration file.
	 * The preference argument contain the updated preference.
	 * <p>
	 * This method will return true if it successfully update the configuration 
	 * file.
	 *
	 * @param  preference	the updated user preference options.  
	 * @return     	 		the result of this operation.
	 */
	public boolean editUserPreference(Preference preference){
		return prefManager.updateUserPreference(preference);
	}
	
	public boolean editEnableDisableACP(Preference preference){
		return prefManager.updateEnableDisableACP(preference);
	}
	
	/**
	 * Retrieve the absolute file path of the configuration file.
	 * This method is necessary for the Text-editor plugin to observe the file
	 * to track configuration changes.
	 * <p>
	 *
	 * @return     	 		the absolute file path to locate the configuration file.
	 */	
	public String getPreferenceConfigurationFileAbsolutePath(){
		return prefManager.getFilePath();
	}
	
	/**
	 * Store the learned user behaviors to a file. This method is called
	 * before the program exit.  
	 */
	public void storeUserBehaviors(){
		rankManager.storeUserBehavior();
	}
	
	/**
	 * Clear all the extracted data from the database This method is called
	 * during the launching and the closing of Java ACP, and when ACPBackground
	 * detect that the Mozilla Firefox plugin is closed.
	 */
	synchronized public void clearAllData(){
		rankManager.removeAllRankData();
		dataManager.removeAllData();
	}
	
	/**
	 * Inform MLManager the current destination document (eg. Microsoft Word Document)
	 * it is working on. This allows ACP to track the different context between 
	 * documents in the Text Editor.
	 * 
	 * @param id	the unique identity to identify the document.
	 */
	public void setDestinationDocument(int id){
		rankManager.setDestinationDocument(id);
	}
	
	/**
	 * Display the debug UI for Machine Learning. This function should not be
	 * called in the release version.
	 */
	public void triggerDebugUI(){
		rankManager.showDebugUI();
	}
	
	/**
	 * Get the current state of ACP logic. This method is used by the Systray UI
	 * to display the appropriate icons based on the state.  
	 * 
	 * @return	the current ACP logic state.
	 * @see State
	 */
	public State getCurrentState(){
		return this.currentState;
	}
	
	/**
	 * Creates the file directory for storing of user data which consists of user preference and user behaviors.
	 * This method is used by ACPLogic at the start of initialization.
	 */
	private void createACPUserDirectory(){
		File directory = new File(System.getProperty("user.home")+"\\Documents\\AutoComPaste\\");
		if(directory.exists() == false){
			directory.mkdir();
		}
	}
	
	/**
	 * Alert the rank manager when a text editor (eg. Microsoft Word) is exited.
	 * 
	 * @param id the document id.
	 */
	public void closeDestinationDocument(int id){
		rankManager.removeDestinationDocument(id);
	}
}
