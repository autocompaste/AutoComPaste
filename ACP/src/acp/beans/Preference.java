package acp.beans;

import java.util.Observable;

import javax.swing.JTextField;

import acp.ACPLogic;
import javassist.expr.Instanceof;

/**
 * The Preference class represent user preference. This class maintain the data structure 
 * needed to store user preference for ACP. The equals() method is override to allow 
 * comparison of preference objects based on the attribute of the preference object. 
 * 
 * @author Loke Yan Hao, Ng Chin Hui
 */
public class Preference extends Observable{
	
	private int entriesInView;
	private int threshold;
	private int triggerDelay;

	// hotkeys
	private String triggerSuggestionPopUp;
	private String enableACP;
	private String extendWord, reduceWord;
	private String extendSentence, reduceSentence;
	private String extendParagraph, reduceParagraph;

	private boolean enableDisable;
	private boolean autoTrigger; // false is manual, auto is true
	private boolean onStartUp;
	private boolean doNotPromptExit;

	/**
	 * Initializes a newly created Preference object so that it represents an 
	 * empty Preference object.
	 */
	public Preference() {
	}

	/**
	 * Return the number of entries to display on the auto-completion box (without scrolling).
	 *
	 * @return     the number of entries to display on the auto-completion box.
	 */
	public int getEntriesInView() {
		return entriesInView;
	}

	/**
	 * Change the number of entries of this preference.
	 *
	 * @param	entriesInView	the number of entries to display on the auto-completion box.
	 */
	public void setEntriesInView(int entriesInView) {
		this.entriesInView = entriesInView;
	}
	
	/**
	 * Return the status of ACP during application launch.
	 * <p>
	 * Return true if, and only if ACP is enable during application launch.
	 * 
	 * @return	the status of ACP during application launch.
	 */
	public boolean isEnableDisable() {
		return enableDisable;
	}

	/**
	 * Change the status of ACP during application launch of this preference.
	 * 
	 * @param enableDisable		the status of ACP during application launch.
	 * 							Set to true to enable ACP during application launch.	
	 */
	public void setEnableDisable(boolean enableDisable) {
		this.enableDisable = enableDisable;
		setChanged();
	    notifyObservers();
	}

	/**
	 * Return the trigger mode for auto-completion in text-editor add-in. 
	 * <p>
	 * If the trigger mode is in manual mode, this method returns false. User 
	 * need to press a hotkey to trigger auto-completion in the respective
	 * text-editor.
	 * <p>
	 * If the trigger mode is in auto mode, this method returns true. User 
	 * does not need to press a hotkey to trigger auto-completion. It is
	 * triggered when user is typing new characters. 
	 * 
	 * @return	the trigger mode for auto-completion.
	 */
	public boolean isAutoTrigger() {
		return autoTrigger;
	}

	/**
	 * Change the trigger mode for auto-completion of this preference.
	 * 
	 * @param autoTrigger	the trigger mode for auto-completion. 
	 * 						Set to true for automatic mode, set to false for manual mode.
	 */
	public void setAutoTrigger(boolean autoTrigger) {
		this.autoTrigger = autoTrigger;
	}

	/**
	 * Return true if, and only if ACP is set to launch during computer startup.
	 * <p>
	 * 
	 * @return 	return true if ACP is set to launch during computer startup.
	 */
	public boolean isOnStartUp() {
		return onStartUp;
	}

	/**
	 * Change the ACP startup option of this preference.
	 * 
	 * @param onStartUp		the ACP startup option.
	 * 						Set to true to launch ACP during computer startup; 
	 * 						set to false to prevent it from been launch during startup. 
	 */
	public void setOnStartUp(boolean onStartUp) {
		this.onStartUp = onStartUp;
	}
	
	/**
	 * Return true if, and only if user do not wish to see a prompt during exit.
	 * <p>
	 * 
	 * @return 	return true if user do not wish to see a prompt during exit.
	 */
	public boolean isDoNotPromptExit() {
		return doNotPromptExit;
	}
	
	
	/**
	 * Change the exit prompt status of this preference.
	 * 
	 * @param doNotPromptExit	the exit prompt status.
	 * 							Set to true if user wish to see the prompt during exit;
	 *							set to false if user do no wish to see the prompt during exit.
	 */
	public void setDoNotPromptExit(boolean doNotPromptExit) {
		this.doNotPromptExit = doNotPromptExit;
	}
	
	/**
	 * Return the threshold for the number of suggestion to auto-trigger auto-completion.
	 * <p>
	 * When user request for suggestion, a list of suggestions will be returned. If, and only if
	 * the number of suggestion in the list is higher than the threshold defined, an empty list
	 * of suggestion will be returned to prevent auto-completion from triggering.
	 *
	 * @return     the sorting order type when displaying the list of suggestion.
	 * @see ACPLogic#requestSuggestion(String, boolean)
	 */
	public int getThreshold() {
		return threshold;
	}

	/**
	 * Change the threshold for the number of suggestion of this preference
	 * 
	 * @param threshold	the threshold for the number of suggestion.
	 */
	public void setThreshold(int threshold) {
		this.threshold = threshold;
	}
	
	/**
	 * Get the hotkey of Trigger Suggestion PopUp.
	 * 
	 * @return	the hotkey for trigger suggestion popup
	 */
	public String getTriggerSuggestionPopUp() {
		return triggerSuggestionPopUp;
	}

	/**
	 * Set the hotkey Trigger Suggestion PopUp.
	 * 
	 * @param triggerSuggestionPopUp 	the hotkey for Trigger Suggestion PopUp
	 */
	public void setTriggerSuggestionPopUp(String triggerSuggestionPopUp) {
		this.triggerSuggestionPopUp = triggerSuggestionPopUp;
	}

	/**
	 * Get the hotkey enable and disable acp.
	 * 
	 * @return	the hotkey for enable and disable acp
	 */
	public String getEnableACP() {
		return enableACP;
	}

	/**
	 * Set the hotkey of enable and disable acp.
	 * 
	 * @param enableACP		the hotkey for enable and disable acp
	 */
	public void setEnableACP(String enableACP) {
		this.enableACP = enableACP;
	}

	/**
	 * Get the hotkey of extend suggestion by word.
	 * 
	 * @return		the hotkey of extend suggestion by word
	 */
	public String getExtendWord() {
		return extendWord;
	}

	/**
	 * Set the hotkey of extend suggestion by word.
	 * 
	 * @param extendWord	the hotkey of extend suggestion by word
	 */
	public void setExtendWord(String extendWord) {
		this.extendWord = extendWord;
	}

	/**
	 * Get the hotkey of reduce suggestion by word.
	 * 
	 * @return		the hotkey of reduce suggestion by word
	 */
	public String getReduceWord() {
		return reduceWord;
	}

	/**
	 * Set the hotkey of reduce suggestion by word.
	 * 
	 * @param reduceWord	the hotkey of reduce suggestion by word
	 */
	public void setReduceWord(String reduceWord) {
		this.reduceWord = reduceWord;
	}

	/**
	 * Get the hotkey of extend suggestion by sentence.
	 * 
	 * @return	the hotkey of extend suggestion by sentence
	 */
	public String getExtendSentence() {
		return extendSentence;
	}

	/**
	 * Set the hotkey of extend suggestion by sentence.
	 * 
	 * @param extendSentence	the hotkey of extend suggestion by sentence
	 */
	public void setExtendSentence(String extendSentence) {
		this.extendSentence = extendSentence;
	}

	/**
	 * Get the hotkey of reduce suggestion by sentence.
	 * 
	 * @return	the hotkey of reduce suggestion by sentence
	 */
	public String getReduceSentence() {
		return reduceSentence;
	}

	/**
	 * Set the hotkey of reduce suggestion by sentence.
	 * 
	 * @param reduceSentence	the hotkey of reduce suggestion by sentence
	 */
	public void setReduceSentence(String reduceSentence) {
		this.reduceSentence = reduceSentence;
	}

	/**
	 * Get the hotkey of extend suggestion by paragraph.
	 * 
	 * @return	the hotkey of extend suggestion by paragraph
	 */
	public String getExtendParagraph() {
		return extendParagraph;
	}

	/**
	 * Set the hotkey of extend suggestion by paragraph.
	 * 
	 * @param extendParagraph	the hotkey of extend suggestion by paragraph
	 */
	public void setExtendParagraph(String extendParagraph) {
		this.extendParagraph = extendParagraph;
	}

	/**
	 * Get the hotkey of reduce suggestion by paragraph.
	 * 
	 * @return	the hotkey of reduce suggestion by paragraph
	 */
	public String getReduceParagraph() {
		return reduceParagraph;
	}

	/**
	 * Set the hotkey of reduce suggestion by paragraph.
	 * 
	 * @param reduceParagraph	the hotkey of reduce suggestion by paragraph
	 */
	public void setReduceParagraph(String reduceParagraph) {
		this.reduceParagraph = reduceParagraph;
	}
	
	/**
	 * Get the delay(in ms) to trigger the suggestion popup.
	 * 
	 * @return the delay to trigger suggestion popup
	 */
	public int getDelayTrigger() {
		return triggerDelay;
	}

	/**
	 * Set the delay(in ms) to trigger the suggestion popup.
	 * 
	 * @param triggerDelay		the delay to trigger suggestion popup
	 */
	public void setDelayTrigger(int triggerDelay) {
		this.triggerDelay = triggerDelay;
	}

	/**
	 * Return true if, and only if the object argument has the same attribute values
	 * with this preference object.
	 *
	 * @return     the result of this operation.
	 */
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof Preference)){
			return false;
		}
		
		Preference pref = (Preference) obj;
		
		if(this.getEntriesInView() == pref.getEntriesInView() &&
				this.getThreshold() == pref.getThreshold() &&
				this.isDoNotPromptExit() == pref.isDoNotPromptExit() &&
				this.isEnableDisable() == pref.isEnableDisable() &&
				this.isOnStartUp() == pref.isOnStartUp() &&
				this.isAutoTrigger() == pref.isAutoTrigger())
			return true;
		else
			return false;
	}
}
