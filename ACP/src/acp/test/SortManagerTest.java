package acp.test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import acp.beans.RankEntry;
import acp.beans.ProcessedText;
import acp.beans.Suggestion;
import acp.beans.entity.Paragraph;
import acp.beans.entity.Sentence;
import acp.beans.entity.Source;
import acp.manager.DataManager;
import acp.manager.RankManager;
import acp.manager.PreferenceManager;
import acp.manager.SortManager;
import acp.store.DataStore;

/**
 * @author Ng Chin Hui
 *
 */
public class SortManagerTest {

	private static SortManager sm;
	private static RankManager mlm;
	private static DataManager dm;
	private static PreferenceManager pm;
	private static ProcessedText pt1, pt2;
	private static int destinationDocument;
	
	@BeforeClass
    public static void setUpBeforeClass() {
		sm = new SortManager();
		dm = new DataManager();
		mlm = new RankManager();
		mlm.testing_mode();
		destinationDocument = 1122;
        mlm.setDestinationDocument(destinationDocument);
		mlm.showDebugUI();
		pm = new PreferenceManager();
		
		try {
			dm.initialiseHibernate();
		} catch (Throwable e) {
			e.printStackTrace();
		}
		
		// construct test data
		mlm.addNewEntry("WIKIPEDIA");
		mlm.addNewEntry("BLOGSPOT");	// most recently viewed
		
		
		Paragraph p;
		ArrayList<Paragraph> paragraphs;
		ArrayList<Sentence> sentences;
		Source s;
		
		// constructing and inserting document 'WIKIPEDIA'
		paragraphs = new ArrayList<Paragraph>();
		p = new Paragraph(0);
		sentences = new ArrayList<Sentence>();
		
		for(int i=0; i<3; i++){
			String temp = "This is wikipedia sentence number "+ i +".";
			sentences.add(new Sentence(i, temp));
		}
		
		p.setSentences(sentences);
		paragraphs.add(p);
		s = new Source("WIKIPEDIA", "http://www.wikipedia.com");
		pt1 = new ProcessedText(s, paragraphs, null); 
		dm.storeProcessedText(pt1);
		
		// constructing and inserting document 'BLOGSPOT'
		paragraphs = new ArrayList<Paragraph>();
		p = new Paragraph(0);
		sentences = new ArrayList<Sentence>();
		
		for(int i=0; i<3; i++){
			String temp = "This is blogspot sentence number "+ i +".";
			sentences.add(new Sentence(i, temp));
		}
		
		p.setSentences(sentences);
		paragraphs.add(p);
		s = new Source("BLOGSPOT", "http://www.blogspot.com");
		pt2 = new ProcessedText(s, paragraphs, null); 
		dm.storeProcessedText(pt2);
    }
	
	@Test
	public void testSortSearchResults_1() {
		
		// rank of document 1:BLOGSPOT 2:WIKIPEDIA
		mlm.setDestinationDocument(destinationDocument);
		ArrayList<RankEntry> rankingModel = mlm.getRankingModel();
		String userInput = "This is";
		List<Suggestion> resultList = sm.sortSearchResults(-1, null, dm.searchBasedOnRequest(userInput, pm.getUserPreference(), true), rankingModel, userInput);
		
		// check if ranks of each entries are correct
		boolean chk = true;
		for(int i=0; i<resultList.size(); i++){
			Suggestion sugg = resultList.get(i);
			if(sugg.getContent().equalsIgnoreCase("This is blogspot sentence number 0.") && i!=0)
				chk = false;
			else if(sugg.getContent().equalsIgnoreCase("This is blogspot sentence number 1.") && i!=1)
				chk = false;
			else if(sugg.getContent().equalsIgnoreCase("This is blogspot sentence number 2.") && i!=2)
				chk = false;
			else if(sugg.getContent().equalsIgnoreCase("This is wikipedia sentence number 0.") && i!=3)
				chk = false;
			else if(sugg.getContent().equalsIgnoreCase("This is wikipedia sentence number 1.") && i!=4)
				chk = false;
			else if(sugg.getContent().equalsIgnoreCase("This is wikipedia sentence number 2.") && i!=5)
				chk = false;
		}
		assertTrue(chk);
	}
	
	@Test
	public void testSortSearchResults_2() {
		
		// learn the 2nd sentence of document, 'WIKIPEDIA'
		List<Suggestion> resultList = dm.searchBasedOnRequest("This is wikipedia sentence number 1.", pm.getUserPreference(), true);
		Suggestion s = resultList.get(0);
		mlm.setDestinationDocument(destinationDocument);
		mlm.learnUserBehaviour(((Sentence)s).getParagraphID(), ((Sentence)s).getParentOrder(), "WIKIPEDIA");	// this will push wikipedia to rank higher than blogspot
		
		// ranks of documents, 1:WIKIPEDIA 2:BLOGSPOT
		ArrayList<RankEntry> rankingModel = mlm.getRankingModel();
		String userInput = "This is ";
		resultList = sm.sortSearchResults(-1, null, dm.searchBasedOnRequest(userInput, pm.getUserPreference(), true), rankingModel, userInput);
		
		// check if the ranks of each entries are correct
		boolean chk = true;
		for(int i=0; i<resultList.size(); i++){
			Suggestion sugg = resultList.get(i);
			if(sugg.getContent().equalsIgnoreCase("This is wikipedia sentence number 2.") && i!=0)
				chk = false;
			else if(sugg.getContent().equalsIgnoreCase("This is wikipedia sentence number 0.") && i!=1)
				chk = false;
			else if(sugg.getContent().equalsIgnoreCase("This is wikipedia sentence number 1.") && i!=2)
				chk = false;
			else if(sugg.getContent().equalsIgnoreCase("This is blogspot sentence number 0.") && i!=3)
				chk = false;
			else if(sugg.getContent().equalsIgnoreCase("This is blogspot sentence number 1.") && i!=4)
				chk = false;
			else if(sugg.getContent().equalsIgnoreCase("This is blogspot sentence number 2.") && i!=5)
				chk = false;
		}
		assertTrue(chk);
	}
	
	@Test
	public void testSortSearchResults_3() {
		
		// learn the 2nd sentence of document, 'WIKIPEDIA'
		List<Suggestion> resultList = dm.searchBasedOnRequest("This is wikipedia sentence number 1.", pm.getUserPreference(), true);
		Suggestion s = resultList.get(0);
		mlm.setDestinationDocument(destinationDocument);
		mlm.learnUserBehaviour(((Sentence)s).getParagraphID(), ((Sentence)s).getParentOrder(), "WIKIPEDIA");	// this will push wikipedia to rank higher than blogspot
		
		// ranks of documents, 1:WIKIPEDIA 2:BLOGSPOT
		ArrayList<RankEntry> rankingModel = mlm.getRankingModel();
		String userInput = "This is wikipedia sentence number 0.";
		resultList = sm.sortSearchResults(-1, null, dm.searchBasedOnRequest(userInput, pm.getUserPreference(), true), rankingModel, userInput);
		
		assertTrue(resultList.size() >= 1);
	}
	
	@Test
	public void testSortSearchResults_4() {
		
		// test if it will return result if rankingModel is empty
		mlm.setDestinationDocument(destinationDocument);
		ArrayList<RankEntry> rankingModel = new ArrayList<RankEntry>();
		String userInput = "This is";
		List<Suggestion> resultList = sm.sortSearchResults(-1, null, dm.searchBasedOnRequest(userInput, pm.getUserPreference(), true), rankingModel, userInput);
		
		assertTrue(resultList.size() == 6);
	}
	
	@Test
	public void testSortSearchResults_5() {
		// test if it will return result if search result is empty
		mlm.setDestinationDocument(destinationDocument);
		ArrayList<RankEntry> rankingModel = mlm.getRankingModel();
		String userInput = "ABCDEFKAWFBDK";
		List<Suggestion> resultList = sm.sortSearchResults(-1, null, dm.searchBasedOnRequest(userInput, pm.getUserPreference(), true), rankingModel, userInput);
		
		assertTrue(resultList.size() == 0);
	}
	
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		dm.removeData(pt1.getMetadata().getUrl());
		dm.removeData(pt2.getMetadata().getUrl());
	}

}
