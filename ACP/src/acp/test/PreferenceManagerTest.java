package acp.test;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import acp.beans.Preference;
import acp.manager.PreferenceManager;

/**
 * @author Loke Yan Hao
 *
 */
public class PreferenceManagerTest {
	
	private static PreferenceManager prefManager;
	private static Preference originalPreference;
	
	@BeforeClass
	public static void setupClass() {
		prefManager = new PreferenceManager();
		originalPreference = prefManager.getUserPreference();
	}
	
	@AfterClass
	public static void closeClass(){
		prefManager.updateUserPreference(originalPreference);
	}

	@Test
	public void testGetUserPreference() {
		Assert.assertNotNull(originalPreference);
	}

	@Test
	public void testUpdateUserPreference() {
		Preference testPreference = new Preference();
		
		testPreference.setDoNotPromptExit(true);
		testPreference.setEnableDisable(true);
		testPreference.setEntriesInView(5);
		testPreference.setOnStartUp(true);
		testPreference.setThreshold(50);
		testPreference.setAutoTrigger(true);
		
		prefManager.updateUserPreference(testPreference);
		
		Preference checkPreference = prefManager.getUserPreference();
		Assert.assertTrue(checkPreference.equals(testPreference));
	}
	
	

}
