package acp.manager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import acp.beans.Preference;
import acp.ui.PreferenceUI;

/**
 * This class act as the controller component for the preferences. It provides utility functions related 
 * to the preferences such as updating preferences etc. User preference is stored into a configuration file
 * in ".ini" format for persistent storing.
 * 
 * @author Loke Yan Hao, Ng Chin Hui 
 */
public class PreferenceManager {
	
	private static String filename = System.getProperty("user.home")+"\\Documents\\AutoComPaste\\PreferenceStore.ini";
	
	final static Logger logger = LoggerFactory.getLogger(PreferenceManager.class);
	
	private Preference cache;
	
	/**
	 * Default Constructor
	 */
	public PreferenceManager(){
	}
	
	/**
	 * Retrieves the user preference from preferenceStore.ini file and initializes the preference object
	 * 
	 * @return	the preference object
	 */
	public Preference getUserPreference(){
		if(cache == null){
			Preference preference = new Preference();
			
			Ini ini = loadFile();
			
			if(ini!=null){
				logger.debug("Reading user preference from " + filename);
				Ini.Section settingsSection = ini.get("settings");
				int entriesInView = settingsSection.get("entriesInView", int.class);
				int threshold = settingsSection.get("threshold", int.class);
				int triggerDelay = settingsSection.get("triggerDelay", int.class);
				boolean enableDisable = settingsSection.get("enableDisable", boolean.class);
				boolean trigger = settingsSection.get("autoTrigger", boolean.class);
				boolean onStartUp = settingsSection.get("onStartUp", boolean.class);
				boolean doNotPromptExit = settingsSection.get("doNotPromptExit", boolean.class);
				
				Ini.Section hotkeysSection = ini.get("hotkeys");
				String triggerSuggestionPopUp = hotkeysSection.get("triggerSuggestionPopUp", String.class);
				String enableACP = hotkeysSection.get("enableACP", String.class);
				String extendWord = hotkeysSection.get("extendWord", String.class);
				String reduceWord = hotkeysSection.get("reduceWord", String.class);
				String extendSentence = hotkeysSection.get("extendSentence", String.class);
				String reduceSentence = hotkeysSection.get("reduceSentence", String.class);
				String extendParagraph = hotkeysSection.get("extendParagraph", String.class);
				String reduceParagraph = hotkeysSection.get("reduceParagraph", String.class);
				
				preference.setEntriesInView(entriesInView);
				preference.setThreshold(threshold);
				preference.setDelayTrigger(triggerDelay);
				preference.setEnableDisable(enableDisable);
				preference.setAutoTrigger(trigger);
				preference.setOnStartUp(onStartUp);
				preference.setDoNotPromptExit(doNotPromptExit);
				preference.setTriggerSuggestionPopUp(triggerSuggestionPopUp);
				preference.setEnableACP(enableACP);
				preference.setExtendWord(extendWord);
				preference.setReduceWord(reduceWord);
				preference.setExtendSentence(extendSentence);
				preference.setReduceSentence(reduceSentence);
				preference.setExtendParagraph(extendParagraph);
				preference.setReduceParagraph(reduceParagraph);
			}
			else{
				preference = null;
			}
			
			cache = preference;
			
			return preference;
		}
		else{
			return cache;
		}
	}
	
	/**
	 * Updates the new value of preference field, Enable or Disable ACP to file.
	 * 
	 * @param preference	the preference object to be stored
	 * @return	true if the preference is stored successfully
	 */
	public boolean updateEnableDisableACP(Preference preference){
		Ini ini = loadFile();
		
		if(ini!=null){
			Ini.Section settingsSection = ini.get("settings");
			settingsSection.put("enableDisable", preference.isEnableDisable());
			
			try {
				writeFile(ini);
				logger.debug("User Preference write to file successfully");
				cache = preference;
				return true;
			} catch (IOException e) {
				logger.error("Unable to write file");
				return false;
			}
		}
		else{
			return false;
		}
	}
	
	/**
	 * Updates the new values of all fields of preference object to file.
	 * 
	 * @param preference	the preference object to be stored
	 * @return	true if the preference is stored successfully
	 */
	public boolean updateUserPreference(Preference preference){
		Ini ini = loadFile();
		
		if(ini!=null){
			Ini.Section settingsSection = ini.get("settings");
			settingsSection.put("entriesInView", preference.getEntriesInView());
			settingsSection.put("threshold", preference.getThreshold());
			settingsSection.put("triggerDelay", preference.getDelayTrigger());
			settingsSection.put("enableDisable", preference.isEnableDisable());
			settingsSection.put("autoTrigger", preference.isAutoTrigger());
			settingsSection.put("onStartUp", preference.isOnStartUp());
			settingsSection.put("doNotPromptExit", preference.isDoNotPromptExit());
			
			Ini.Section hotkeysSection = ini.get("hotkeys");
			hotkeysSection.put("triggerSuggestionPopUp", preference.getTriggerSuggestionPopUp());
			hotkeysSection.put("enableACP", preference.getEnableACP());
			hotkeysSection.put("extendWord", preference.getExtendWord());
			hotkeysSection.put("reduceWord", preference.getReduceWord());
			hotkeysSection.put("extendSentence", preference.getExtendSentence());
			hotkeysSection.put("reduceSentence", preference.getReduceSentence());
			hotkeysSection.put("extendParagraph", preference.getExtendParagraph());
			hotkeysSection.put("reduceParagraph", preference.getReduceParagraph());
			
			try {
				writeFile(ini);
				logger.debug("User Preference write to file successfully");
				cache = preference;
				return true;
			} catch (IOException e) {
				logger.error("Unable to write file");
				return false;
			}
		}
		else{
			return false;
		}
	}
	
	/**
	 * Gets the absolute file path of preferenceStore.ini
	 * 
	 * @return 	the absolute file path of preferenceStore.ini
	 */
	public String getFilePath(){
		File file = new File(filename);
		return file.getAbsolutePath();
	}
	
	/**
	 * Load the preference.ini file in. 
	 * If file does not exists, a new preference.ini file with default values will be created
	 * 
	 * @return	the ini object that contains the preference.ini file
	 */
	private Ini loadFile(){
		
		Ini ini = new Ini(); 
		File file = new File(filename);
		
		try {
			if(file.exists()){
				ini.load(new FileReader(file));
			}else{
				ini = createFile();
			}
		} catch (IOException e) {
			logger.error("Configuration file not found.");
			return null;
		}
		
		return ini;
	}
	
	/**
	 * Writes the ini object into the file
	 * 
	 * @param ini	the ini object updated with preference object value.
	 * @throws IOException
	 */
	private void writeFile(Ini ini) throws IOException{
		File file = new File(filename);
		ini.store(file);
	}
	
	/**
	 * Creates the ini file if it does not exists.
	 * 
	 * @return	the ini object
	 */
	private Ini createFile(){
		logger.error("Creating configuration file");
		
		Ini ini = new Ini(); 
		File file = new File(filename);
		try {
			
			file.createNewFile();
			
			ini.load(new FileReader(file));
			
			ini.add("settings", "entriesInView", PreferenceUI.DEFAULT_ENTRIES_IN_VIEW);
			ini.add("settings", "threshold", PreferenceUI.DEFAULT_THRESHOLD);
			ini.add("settings", "triggerDelay", PreferenceUI.DEFAULT_TRIGGER_DELAY);
			ini.add("settings", "enableDisable", true);
			ini.add("settings", "autoTrigger", true);
			ini.add("settings", "onStartUp", true);
			ini.add("settings", "doNotPromptExit", false);
			ini.add("settings", "tourEnabled", true);
			
			ini.add("hotkeys", "triggerSuggestionPopUp", PreferenceUI.DEFAULT_HOTKEY_TRIGGERSUGGESTIONPOPUP);
			ini.add("hotkeys", "enableACP", PreferenceUI.DEFAULT_HOTKEY_ENABLEACP);
			ini.add("hotkeys", "extendWord", PreferenceUI.DEFAULT_HOTKEY_EXTENDWORD);
			ini.add("hotkeys", "reduceWord", PreferenceUI.DEFAULT_HOTKEY_REDUCEWORD);
			ini.add("hotkeys", "extendSentence", PreferenceUI.DEFAULT_HOTKEY_EXTENDSENTENCE);
			ini.add("hotkeys", "reduceSentence", PreferenceUI.DEFAULT_HOTKEY_REDUCESENTENCE);
			ini.add("hotkeys", "extendParagraph", PreferenceUI.DEFAULT_HOTKEY_EXTENDPARAGRAPH);
			ini.add("hotkeys", "reduceParagraph", PreferenceUI.DEFAULT_HOTKEY_REDUCEPARAGRAPH);
			
			ini.store(file);
			
		} catch (Exception e1) {
			logger.error("Configuration file create error!");
			return null;
		}
		
		return ini;
	}
}
