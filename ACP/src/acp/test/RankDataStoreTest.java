package acp.test;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import acp.beans.RankEntry;
import acp.store.RankDataStore;

/**
 * @author Ng Chin Hui
 *
 */
public class RankDataStoreTest {
	
	private RankDataStore mls;

	@Before
    public void setUp() {
		mls = new RankDataStore();
		mls.testing_mode();
    }
	
	@Test
	public void testRetrieveMLData() {
		mls.retrieveRankData();
		assertTrue(mls.getDocumentList().size() >= 0);
		assertTrue(mls.getScoreList().size() >= 0);
	}
	
	@Test
	public void testStoreUserBehaviour_1() {
		
		/* Test basic storing - when there are no records in file */
		for(int i=1; i<=6; i++){
			String s = "Test"+i;
			mls.updateDocumentList(s, (double)i);
		}
		mls.storeUserBehaviour();
		mls.retrieveRankData();
		
		assertTrue(mls.getDocumentList().size() == 6);
		assertTrue(mls.getScoreList().size() == 6);
	}
	
	@Test
	public void testStoreUserBehaviour_2() {
		
		/* Test storing - when there are existing records in file */
		mls.retrieveRankData();
		
		int previousSize = mls.getDocumentList().size();
		for(int i=previousSize+1; i<=previousSize+6; i++){			// add 6 more records
			String s = "Test"+i;
			mls.updateDocumentList(s, (double)i);
		}
		
		mls.storeUserBehaviour();
		mls.retrieveRankData();
		
		assertTrue(mls.getDocumentList().size() == previousSize+6);
		assertTrue(mls.getScoreList().size() == previousSize+6);
	}
}
