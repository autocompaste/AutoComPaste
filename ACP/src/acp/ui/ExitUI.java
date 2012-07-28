package acp.ui;

import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import com.melloware.jintellitype.JIntellitype;

import acp.ACPLogic;
import acp.beans.Preference;

/**
 * This is the presentation layer which display the exit panel.
 * 
 * @author Loke Yan Hao
 *
 */
public class ExitUI extends JFrame {
	private ACPLogic logic;
	
	public void exitACP(ACPLogic logic){
		this.logic = logic;
		boolean doNotPrompt = checkPreference();
		
		if(!doNotPrompt){
			promptExitDialog();
		}
		else{
			System.exit(0);
		}
	}
	
	private boolean checkPreference(){
		Preference pref = logic.getUserPreference();
		return pref.isDoNotPromptExit();
	}
	
	private void updatePreference(){
		Preference pref = logic.getUserPreference();
		pref.setDoNotPromptExit(true);
		logic.editUserPreference(pref);
	}
	
	private void clearJIntellitype(){
		JIntellitype.getInstance().unregisterHotKey(1);
		JIntellitype.getInstance().cleanUp();
	}
	
	private void promptExitDialog(){
		Object[] options = {"Yes", "No"};
		
		final int YES = 0;
		
		JCheckBox checkbox = new JCheckBox("Do not prompt this message again.");
		String message = "Do you wish to exit AutoComPaste?";
		
		Object[] params = {message, checkbox};
		
		int n = JOptionPane.showOptionDialog(this, params, "AutoComPaste",	
				JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options,	options[0]);
		
		if(checkbox.isSelected()){
			updatePreference();
		}

		if(n==YES){
			logic.clearAllData();
			logic.storeUserBehaviors();
			clearJIntellitype();
			System.exit(0);
		}
	}
}
