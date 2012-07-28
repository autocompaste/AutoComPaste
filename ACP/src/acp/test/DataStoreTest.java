package acp.test;

import static org.junit.Assert.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import acp.beans.Preference;
import acp.beans.Suggestion;
import acp.beans.entity.Entity;
import acp.beans.entity.Paragraph;
import acp.beans.entity.Sentence;
import acp.beans.entity.Source;
import acp.manager.DataManager;
import acp.manager.PreferenceManager;
import acp.store.DataStore;
import acp.util.SessionFactoryUtil;

/**
 * For testing of DataStore
 * 
 * @author Teo Kee Cheng
 *
 */
public class DataStoreTest {

	private static DataManager dm;
	private static DataStore ds;
	private static PreferenceManager prefManager;
	
	private static Source source1;
	private static Source source2;
	private static Paragraph para1;
	private static Paragraph para2;
	private static Sentence sent1;
	private static Sentence sent2;
	private static Entity entity1;
	private static Entity entity2;
	private static List<Sentence> sentences; // For testing searchText only
	private static List<Entity> entities; // For testing searchEntity only
	
	private static List<Integer> sourceAdded;
	
	private int sourceID1 = -1;
	private int sourceID2 = -1;
	private int pid1 = -1;
	private int pid2 = -1;
	private int sid1 = -1;
	private int sid2 = -1;
	private int eid1 = -1;
	private int eid2 = -1;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		dm = new DataManager();
		ds = new DataStore();
		prefManager = new PreferenceManager();
		
		try {
			dm.initialiseHibernate();
		} catch (Throwable e) {
			e.printStackTrace();
		}
		
		
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {

	}

	@Before
	public void setUp() throws Exception {
		sourceAdded = new ArrayList<Integer>();
		
		source1 = new Source("sourcename", "Testing1");
		source2 = new Source("sourcename", "Testing2");
		para1 = new Paragraph(1);
		para2 = new Paragraph(2);
		sent1 = new Sentence(1, "abcde");
		sent2 = new Sentence(2, "edcba");
		entity1 = new Entity(Entity.URL, "http://www.yandao.com");
		entity2 = new Entity(Entity.EMAIL, "yandao@yandao.com");
		
		sentences = new ArrayList<Sentence>();
		entities = new ArrayList<Entity>();
		Scanner sc = null, sc1 = null;
		try {
			sc = new Scanner(new File("testsearchtext.txt"));
			sc1 = new Scanner(new File("testsearchentity.txt"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		sc.useDelimiter("[.]");
		sc1.useDelimiter("[|]");
		
		int i = 1;
		while(sc.hasNext()){
			String temp = sc.next().trim();
			sentences.add(new Sentence(i, temp));		
			i++;
		}
		
		int currType = -1; 
		while(sc1.hasNext()){
			String temp = sc1.next().trim();
			if(temp.equals("place")){currType = Entity.PLACE;} else
			if(temp.equals("url")){currType = Entity.URL;} else
			if(temp.equals("email")){currType = Entity.EMAIL;} else
			if(temp.equals("address")){currType = Entity.ADDRESS;} 
			else{
				entities.add(new Entity(currType, temp));		
			}
		}
	}

	@After
	public void tearDown() throws Exception {
		if(ds.getSession() != null && ds.getSession().isConnected())
			ds.closeSession();
		
		ds.startSession();
		ds.startTransaction();
		for(Integer sourceID : sourceAdded){
			List<Paragraph> paragraphs = (List<Paragraph>) ds.getParagraphs(sourceID);
				
			for(Paragraph para : paragraphs){
				List<Sentence> sentences = (List<Sentence>) ds.getSentences(para.getId());
				ds.removeSentencesAndIndexes(sentences);
			}		
			ds.removeParagraphs(sourceID);
			
			List<Entity> entities = (List<Entity>) ds.getEntities(sourceID);
			
			ds.removeEntitiesAndIndexes(entities);
			ds.removeSource(sourceID);
		}
		ds.commitTransaction();
		ds.closeSession();
	}
	
	@Test
	public void testStoreMetadata() {
		ds.startSession();
		
		ds.startTransaction();
		// store normal source object
		int id = ds.storeMetadata(source1);
		sourceAdded.add(id);
		assertTrue(id > 0);
		
		// store null object
		id = ds.storeMetadata(null);
		assertTrue(id < 0);
		ds.commitTransaction();
		
		ds.closeSession();
	}
	
	@Test
	public void testStoreParagraph() {
		ds.startSession();
		
		ds.startTransaction();
		int sourceID = ds.storeMetadata(source1);
		sourceAdded.add(sourceID);
		assertTrue(sourceID > 0);
		
		// store normal paragraph object
		para1.setSourceID(sourceID);
		int pid = ds.storeParagraph(para1);
		assertTrue(pid > 0);
		
		// store null object
		pid = ds.storeParagraph(null);
		assertTrue(pid < 0);
		
		ds.commitTransaction();
		ds.closeSession();
	}
	
	@Test
	public void testStoreSentence() {
		ds.startSession();
		
		ds.startTransaction();
		int sourceID = ds.storeMetadata(source1);
		sourceAdded.add(sourceID);
		assertTrue(sourceID > 0);
		
		para1.setSourceID(sourceID);
		int pid = ds.storeParagraph(para1);
		assertTrue(pid > 0);
		
		// store normal sentence object
		sent1.setParagraphID(pid);
		int sid = ds.storeSentence(sent1);
		assertTrue(sid > 0);
		
		// store null object
		sid = ds.storeSentence(null);
		assertTrue(sid < 0);
		
		ds.commitTransaction();
		
		ds.closeSession();
	}
	
	@Test
	public void testStoreEntity() {
		ds.startSession();
		
		ds.startTransaction();
		int sourceID = ds.storeMetadata(source1);
		sourceAdded.add(sourceID);
		assertTrue(sourceID > 0);
		
		// store normal entity object
		entity1.setSourceID(sourceID);
		int eid = ds.storeEntity(entity1);
		assertTrue(eid > 0);
		
		entity2.setSourceID(sourceID);
		eid = ds.storeEntity(entity2);
		assertTrue(eid > 0);
		
		// store null object
		eid = ds.storeEntity(null);
		assertTrue(eid < 0);
		
		ds.commitTransaction();
		
		ds.closeSession();
	}
	
	public void getSourceSetup(){
		ds.startSession();
		
		ds.startTransaction();
		sourceID1 = ds.storeMetadata(source1);
		sourceAdded.add(sourceID1);
		assertTrue(sourceID1 > 0);
		
		sourceID2 = ds.storeMetadata(source2);
		sourceAdded.add(sourceID2);
		assertTrue(sourceID2 > 0);
		ds.commitTransaction();
		ds.closeSession();
	}
	@Test
	public void testGetSource1() {
		getSourceSetup();
		
		ds.startSession();
		
		// get source with existing id
		Source s = ds.getSource(sourceID1);
		assertTrue(source1.getName().equals(s.getName()));
		
		ds.closeSession();
	}
	@Test
	public void testGetSource2() {
		getSourceSetup();
		
		ds.startSession();
		
		// get source with non-existing id
		Source s = ds.getSource(sourceID1 + 100000);
		assertNull(s);
		
		ds.closeSession();
	}
	@Test
	public void testGetSource3() {
		getSourceSetup();
		
		ds.startSession();
		
		// get source with negative id
		Source s = ds.getSource(-100);
		assertNull(s);
		
		ds.closeSession();
	}
	@Test
	public void testGetSourceByURL1() {
		getSourceSetup();
		
		ds.startSession();
		
		// get source with existing url
		Source s = ds.getSource(source1.getUrl());
		assertNotNull(s);
		
		ds.closeSession();
	}
	@Test
	public void testGetSourceByURL2() {
		getSourceSetup();
		
		ds.startSession();
		
		// get source with null
		Source s = ds.getSource(null);
		assertNull(s);
		
		ds.closeSession();
	}
	@Test
	public void testGetSourceByURL3() {
		getSourceSetup();
		
		ds.startSession();
		
		// get source with non-existing name
		Source s = ds.getSource("~!@FF$^DVdbdg$%^'\"");
		assertNull(s);
		
		ds.closeSession();
	}

	public void getParagraphSetup(){
		ds.startSession();
		
		ds.startTransaction();
		int sourceID = ds.storeMetadata(source1);
		sourceAdded.add(sourceID);
		assertTrue(sourceID > 0);
		
		para1.setSourceID(sourceID);
		pid1 = ds.storeParagraph(para1);
		assertTrue(pid1 > 0);
		
		para2.setSourceID(sourceID);
		pid2 = ds.storeParagraph(para2);
		assertTrue(pid2 > 0);
		ds.commitTransaction();
		
		ds.closeSession();
	}
	@Test
	public void testGetParagraph1() {
		getParagraphSetup();
		
		ds.startSession();
		
		// get paragraph with existing id
		Paragraph p = ds.getParagraph(pid1);
		assertTrue(para1.getParentOrder() == p.getParentOrder());
		
		ds.closeSession();
	}
	@Test
	public void testGetParagraph2() {
		getParagraphSetup();
		
		ds.startSession();
		
		// get paragraph with non-existing id
		Paragraph p = ds.getParagraph(pid1 + 100000);
		assertNull(p);
		
		ds.closeSession();
	}
	@Test
	public void testGetParagraph3() {
		getParagraphSetup();
		
		ds.startSession();
		
		// get paragraph with negative id
		Paragraph p = ds.getParagraph(-100);
		assertNull(p);
		
		ds.closeSession();
	}
	
	public void getParagraphsSetup(){
		ds.startSession();
		
		ds.startTransaction();
		sourceID1 = ds.storeMetadata(source1);
		sourceAdded.add(sourceID1);
		assertTrue(sourceID1 > 0);
		
		// Add two paragraphs
		para1.setSourceID(sourceID1);
		pid1 = ds.storeParagraph(para1);
		assertTrue(pid1 > 0);

		para2.setSourceID(sourceID1);
		pid2 = ds.storeParagraph(para2);
		assertTrue(pid2 > 0);
		ds.commitTransaction();
		
		ds.closeSession();
	}
	@Test
	public void testGetParagraphs1() {
		getParagraphsSetup();
		
		ds.startSession();
		
		// get paragraphs with existing source id
		List<Paragraph> paragraphs = (List<Paragraph>) ds.getParagraphs(sourceID1);
		assertTrue(paragraphs.size() == 2);
		
		ds.closeSession();
	}
	
	@Test
	public void testGetParagraphs2() {
		getParagraphsSetup();
		
		ds.startSession();
		
		// get paragraphs with non-existing source id
		List<Paragraph> paragraphs = (List<Paragraph>) ds.getParagraphs(sourceID1 + 100000);
		assertTrue(paragraphs.size() == 0);

		ds.closeSession();
	}
	@Test
	public void testGetParagraphs3() {
		getParagraphsSetup();
		
		ds.startSession();
		
		// get paragraphs with negative source id
		List<Paragraph> paragraphs = (List<Paragraph>) ds.getParagraphs(-100);
		assertTrue(paragraphs.size() == 0);
		
		ds.closeSession();
	}
	
	public void getEntitySetup(){
		ds.startSession();
		
		ds.startTransaction();
		int sourceID = ds.storeMetadata(source1);
		sourceAdded.add(sourceID);
		assertTrue(sourceID > 0);
		
		// Add two entities
		entity1.setSourceID(sourceID);
		eid1 = ds.storeEntity(entity1);
		assertTrue(eid1 > 0);

		entity2.setSourceID(sourceID);
		eid2 = ds.storeEntity(entity2);
		assertTrue(eid2 > 0);
		ds.commitTransaction();
		
		ds.closeSession();
	}
	@Test
	public void testGetEntity1() {
		getEntitySetup();
		
		ds.startSession();
		
		// get entities with existing id
		Entity e = ds.getEntity(eid1);
		assertTrue(entity1.getContent().equals(e.getContent()));
		
		ds.closeSession();
	}
	@Test
	public void testGetEntity2() {
		getEntitySetup();
		
		ds.startSession();
		
		// get entities with non-existing id
		Entity e = ds.getEntity(eid1 + 100000);
		assertNull(e);
		
		ds.closeSession();
	}
	@Test
	public void testGetEntity3() {
		getEntitySetup();
		
		ds.startSession();

		// get entities with negative id
		Entity e = ds.getEntity(-100);
		assertNull(e);
		
		ds.closeSession();
	}
	
	public void getEntitiesSetup(){
		ds.startSession();
		
		ds.startTransaction();
		sourceID1 = ds.storeMetadata(source1);
		sourceAdded.add(sourceID1);
		assertTrue(sourceID1 > 0);
		
		// Add two entities
		entity1.setSourceID(sourceID1);
		eid1 = ds.storeEntity(entity1);
		assertTrue(eid1 > 0);

		entity2.setSourceID(sourceID1);
		eid2 = ds.storeEntity(entity2);
		assertTrue(eid2 > 0);
		ds.commitTransaction();
		ds.closeSession();
	}
	@Test
	public void testGetEntities1() {
		getEntitiesSetup();
		
		ds.startSession();
		
		// get entities with existing source id
		List<Entity> entities = (List<Entity>) ds.getEntities(sourceID1);
		assertTrue(entities.size() == 2);
		
		ds.closeSession();
	}
	@Test
	public void testGetEntities2() {
		getEntitiesSetup();
		
		ds.startSession();
		
		// get entities with non-existing source id
		List<Entity> entities = (List<Entity>) ds.getEntities(sourceID1 + 100000);
		assertTrue(entities.size() == 0);
		
		ds.closeSession();
	}
	@Test
	public void testGetEntities3() {
		getEntitiesSetup();
		
		ds.startSession();
		
		// get entities with negative source id
		List<Entity> entities = (List<Entity>) ds.getEntities(-100);
		assertTrue(entities.size() == 0);
		
		ds.closeSession();
	}
	
	public void getSentenceSetup(){
		ds.startSession();
		
		ds.startTransaction();
		sourceID1 = ds.storeMetadata(source1);
		sourceAdded.add(sourceID1);
		assertTrue(sourceID1 > 0);
		
		para1.setSourceID(sourceID1);
		pid1 = ds.storeParagraph(para1);
		assertTrue(pid1 > 0);
		
		// Add two sentences
		sent1.setParagraphID(pid1);
		sid1 = ds.storeSentence(sent1);
		assertTrue(sid1 > 0);
		sent2.setParagraphID(pid1);
		sid2 = ds.storeSentence(sent2);
		assertTrue(sid2 > 0);
		ds.commitTransaction();
		
		ds.closeSession();
	}
	@Test
	public void testGetSentenceByID1() {
		getSentenceSetup();
		
		ds.startSession();
		
		// get sentence with existing id
		Sentence s = ds.getSentence(sid1);
		assertTrue(sent1.getParentOrder() == s.getParentOrder());
		
		ds.closeSession();
	}
	@Test
	public void testGetSentenceByID2() {
		getSentenceSetup();
		
		ds.startSession();
		// get sentence with non-existing id
		Sentence s = ds.getSentence(sid1 + 100000);
		assertNull(s);
				
		ds.closeSession();
	}
	@Test
	public void testGetSentenceByID3() {
		getSentenceSetup();
		
		ds.startSession();
		// get sentence with negative id
		Sentence s = ds.getSentence(-100);
		assertNull(s);
		
		ds.closeSession();
	}
	@Test
	public void testGetSentence1() {
		getSentenceSetup();
		
		ds.startSession();
		// get sentence with paragraph id and parentOrder
		Sentence s = ds.getSentence(pid1, sent1.getParentOrder());
		assertTrue(sent1.getParentOrder() == s.getParentOrder());
		
		ds.closeSession();
	}
	@Test
	public void testGetSentence2() {
		getSentenceSetup();
		
		ds.startSession();
		// get sentence with non-existing parentOrder
		Sentence s = ds.getSentence(pid1, sent1.getParentOrder() + 100000);
		assertNull(s);
		
		ds.closeSession();
	}
	@Test
	public void testGetSentence3() {
		getSentenceSetup();
		
		ds.startSession();
		// get sentence with negative parentOrder
		Sentence s = ds.getSentence(pid1, -100);
		assertNull(s);
		
		ds.closeSession();
	}
	@Test
	public void testGetSentence4() {
		getSentenceSetup();
		
		ds.startSession();
		// get sentence with non-existing paragraph id
		Sentence s = ds.getSentence(pid1 + 100000, sent1.getParentOrder());
		assertNull(s);
		
		ds.closeSession();
	}
	@Test
	public void testGetSentence5() {
		getSentenceSetup();
		
		ds.startSession();
		// get sentence with negative paragraph id
		Sentence s = ds.getSentence(-100, sent1.getParentOrder());
		assertNull(s);	
		
		ds.closeSession();
	}
	@Test
	public void testGetSentence6() {
		getSentenceSetup();
		
		ds.startSession();
		
		// Test if sentence content is correct
		Sentence s = ds.getSentence(sid1);
		String correctContent = s.getContent();
		Sentence newS = null;
		for(int i = 0; i < 1000; i++){
			newS = ds.getSentence(sid1);
		}
		assertTrue(correctContent.equals(newS.getContent()));
		
		ds.closeSession();
	}
	
	public void getSentencesSetup(){
		ds.startSession();
		
		ds.startTransaction();
		int sourceID = ds.storeMetadata(source1);
		sourceAdded.add(sourceID);
		assertTrue(sourceID > 0);
		
		para1.setSourceID(sourceID);
		pid1 = ds.storeParagraph(para1);
		assertTrue(pid1 > 0);
		
		// Add two sentences
		sent1.setParagraphID(pid1);
		sid1 = ds.storeSentence(sent1);
		assertTrue(sid1 > 0);
		sent2.setParagraphID(pid1);
		sid2 = ds.storeSentence(sent2);
		assertTrue(sid2 > 0);
		ds.commitTransaction();
		
		ds.closeSession();
	}
	@Test
	public void testGetSentences1() {
		getSentencesSetup();
		
		ds.startSession();
		
		// get sentences with existing paragraph id
		List<Sentence> sentences = (List<Sentence>) ds.getSentences(pid1);
		assertTrue(sentences.size() == 2);
		
		ds.closeSession();
	}
	@Test
	public void testGetSentences2() {
		getSentencesSetup();
		
		ds.startSession();
		
		// get sentences with non-existing paragraph id
		List<Sentence> sentences = (List<Sentence>) ds.getSentences(pid1 + 100000);
		assertTrue(sentences.size() == 0);
		
		ds.closeSession();
	}
	@Test
	public void testGetSentences3() {
		getSentencesSetup();
		
		ds.startSession();
		
		// get sentences with negative paragraph id
		List<Sentence> sentences = (List<Sentence>) ds.getSentences(-100);
		assertTrue(sentences.size() == 0);
		
		ds.closeSession();
	}
	
	@Test
	public void testRemoveSource() {
		ds.startSession();
		ds.startTransaction();
		
		int id = ds.storeMetadata(source1);
		sourceAdded.add(id);
		assertTrue(id > 0);
		
		ds.removeSource(id);

		assertNull(ds.getSource(id));
		
		ds.commitTransaction();
		ds.closeSession();
	}
	
	@Test
	public void testRemoveParagraphs() {
		ds.startSession();
		ds.startTransaction();
		
		int sourceID = ds.storeMetadata(source1);
		sourceAdded.add(sourceID);
		assertTrue(sourceID > 0);
		
		para1.setSourceID(sourceID);
		int pid = ds.storeParagraph(para1);
		assertTrue(pid > 0);
		
		ds.removeParagraphs(sourceID);
		
		assertNull(ds.getParagraph(pid));
		
		ds.commitTransaction();
		ds.closeSession();
	}
	
	@Test
	public void testRemoveAllDataAndIndexes() {
		ds.startSession();
		ds.startTransaction();
		
		int sourceID = ds.storeMetadata(source1);
		sourceAdded.add(sourceID);
		assertTrue(sourceID > 0);
		
		para1.setSourceID(sourceID);
		int pid = ds.storeParagraph(para1);
		assertTrue(pid > 0);
		
		// Add two sentences
		sent1.setParagraphID(pid);
		int sid1 = ds.storeSentence(sent1);
		assertTrue(sid1 > 0);
		sent2.setParagraphID(pid);
		int sid2 = ds.storeSentence(sent2);
		assertTrue(sid2 > 0);
		
		ds.removeAllDataAndIndexes();
		
		assertNull(ds.getSource(sourceID));
		assertNull(ds.getParagraph(pid));
		assertNull(ds.getSentence(sid1));
		assertNull(ds.getSentence(sid2));
		
		ds.commitTransaction();
		ds.closeSession();
	}
	
	@Test
	public void testRemoveSentencesAndIndexes() {
		ds.startSession();
		ds.startTransaction();
		
		int sourceID = ds.storeMetadata(source1);
		sourceAdded.add(sourceID);
		assertTrue(sourceID > 0);
		
		para1.setSourceID(sourceID);
		int pid = ds.storeParagraph(para1);
		assertTrue(pid > 0);
		
		sent1.setParagraphID(pid);
		int sid = ds.storeSentence(sent1);
		assertTrue(sid > 0);
		
		List<Sentence> sentences = new ArrayList<Sentence>();
		sentences.add(ds.getSentence(sid));
		ds.removeSentencesAndIndexes(sentences);
		
		assertNull(ds.getSentence(sid));
		
		ds.commitTransaction();
		ds.closeSession();
	}
	
	@Test
	public void testRemoveEntitiesAndIndexes() {
		ds.startSession();
		ds.startTransaction();
		
		int sourceID = ds.storeMetadata(source1);
		sourceAdded.add(sourceID);
		assertTrue(sourceID > 0);
		
		entity1.setSourceID(sourceID);
		int eid = ds.storeEntity(entity1);
		assertTrue(eid > 0);
		
		List<Entity> entities = new ArrayList<Entity>();
		entities.add(ds.getEntity(eid));
		ds.removeEntitiesAndIndexes(entities);
		
		assertNull(ds.getEntity(eid));
		
		ds.commitTransaction();
		ds.closeSession();
	}
	
	public void searchTextSetup(){
		ds.startSession();
		
		ds.startTransaction();
		int sourceID = ds.storeMetadata(source1);
		sourceAdded.add(sourceID);
		assertTrue(sourceID > 0);
		
		para1.setSourceID(sourceID);
		int pid = ds.storeParagraph(para1);
		assertTrue(pid > 0);
		
		// Add 64 sentences from testsearchtext.txt
		for(Sentence s : sentences){
			s.setParagraphID(pid);
			int sid = ds.storeSentence(s);
			assertTrue(sid > 0);
		}
		ds.commitTransaction();
		ds.closeSession();
	}
	@Test
	public void testSearchText1() {
		boolean result = false;
		
		searchTextSetup();
		
		Preference prefs = prefManager.getUserPreference();
		prefs.setThreshold(10000);
		ds.startSession();
		// Correct prefix without typing mistake (1 Match)
		List<Suggestion> suggestions = (List<Suggestion>) ds.searchText("Although", prefs, true);
		assertTrue(suggestions.size() >= 1);
		for(int i = 0; i < suggestions.size(); i++){
			if(suggestions.get(i).getContent().toLowerCase().startsWith("although"))
				result = true;
		}
		assertTrue(result);	
		ds.closeSession();
	}
	@Test
	public void testSearchText2() {
		boolean result = false;
		
		searchTextSetup();
		
		Preference prefs = prefManager.getUserPreference();
		prefs.setThreshold(10000);
		ds.startSession();
		// Correct prefix without typing mistake and test search regardless of word upper/lower cases (1 Match)
		List<Suggestion> suggestions = (List<Suggestion>) ds.searchText("the ancient", prefs, true);
		for(int i = 0; i < suggestions.size(); i++){
			if(suggestions.get(i).getContent().toLowerCase().startsWith("the ancient"))
				result = true;
		}
		assertTrue(result);	
		ds.closeSession();
	}
	@Test
	public void testSearchText3() {
		boolean result = false;
		
		searchTextSetup();
		
		Preference prefs = prefManager.getUserPreference();
		prefs.setThreshold(10000);
		ds.startSession();
		// Double spacing error (1 Match)
		List<Suggestion> suggestions = (List<Suggestion>) ds.searchText("the  ancient", prefs, true);
		for(int i = 0; i < suggestions.size(); i++){
			if(suggestions.get(i).getContent().toLowerCase().startsWith("the ancient"))
				result = true;
		}
		assertTrue(result);	
		ds.closeSession();
	}
	@Test
	public void testSearchText4() {
		boolean result = false;
		
		searchTextSetup();
		ds.startSession();
		Preference prefs = prefManager.getUserPreference();
		prefs.setThreshold(10000);
		
		// Correct prefix without typing mistake (2 Match)
		List<Suggestion> suggestions = (List<Suggestion>) ds.searchText("Such", prefs, true);
		assertTrue(suggestions.size() >= 2);
		for(int i = 0; i < suggestions.size(); i++){
			if(suggestions.get(i).getContent().toLowerCase().startsWith("such"))
				result = true;
		}
		assertTrue(result);	
		ds.closeSession();
	}
	@Test
	public void testSearchText5() {
		boolean result = false;
		
		searchTextSetup();
		
		Preference prefs = prefManager.getUserPreference();
		prefs.setThreshold(10000);
		ds.startSession();
		// Correct prefix without typing mistake (2 Match)
		List<Suggestion> suggestions = (List<Suggestion>) ds.searchText("For example", prefs, true);
		assertTrue(suggestions.size() >= 2);
		for(int i = 0; i < suggestions.size(); i++){
			if(suggestions.get(i).getContent().toLowerCase().startsWith("for example"))
				result = true;
		}
		assertTrue(result);	
		ds.closeSession();
	}
	@Test
	public void testSearchText6() {
		boolean result = false;
		
		searchTextSetup();
		
		Preference prefs = prefManager.getUserPreference();
		prefs.setThreshold(10000);
		ds.startSession();
		// Prefix that have other prefixes that are close to the prefix (9 Match. cake + cakes)
		List<Suggestion> suggestions = (List<Suggestion>) ds.searchText("cake", prefs, true);
		assertTrue(suggestions.size() >= 9);
		for(int i = 0; i < suggestions.size(); i++){
			if(suggestions.get(i).getContent().toLowerCase().startsWith("cake"))
				result = true;
		}
		assertTrue(result);	
		ds.closeSession();
	}
	@Test
	public void testSearchText7() {
		boolean result = false;
		
		searchTextSetup();
		
		Preference prefs = prefManager.getUserPreference();
		prefs.setThreshold(10000);
		ds.startSession();
		// Wrong prefix with one letter additional (2 Match)
		List<Suggestion> suggestions = (List<Suggestion>) ds.searchText("cakes is", prefs, true);
		assertTrue(suggestions.size() >= 2);
		for(int i = 0; i < suggestions.size(); i++){
			if(suggestions.get(i).getContent().toLowerCase().startsWith("cake is"))
				result = true;
		}
		assertTrue(result);	
		ds.closeSession();
	}
	@Test
	public void testSearchText8() {
		boolean result = false;
		
		searchTextSetup();
		
		Preference prefs = prefManager.getUserPreference();
		prefs.setThreshold(10000);
		ds.startSession();
		// Wrong prefix with one letter wrong (2 Match)
		List<Suggestion> suggestions = (List<Suggestion>) ds.searchText("cheesecakea", prefs, true);
		assertTrue(suggestions.size() >= 2);
		for(int i = 0; i < suggestions.size(); i++){
			if(suggestions.get(i).getContent().toLowerCase().startsWith("cheesecakes"))
				result = true;
		}
		assertTrue(result);	
		ds.closeSession();
	}
	@Test
	public void testSearchText9() {
		boolean result = false;
		
		searchTextSetup();
		
		Preference prefs = prefManager.getUserPreference();
		prefs.setThreshold(10000);
		ds.startSession();
		// Search string with words in single quote as prefix
		List<Suggestion> suggestions = (List<Suggestion>) ds.searchText("'yeast'", prefs, true);
		assertTrue(suggestions.size() >= 1);
		for(int i = 0; i < suggestions.size(); i++){
			if(suggestions.get(i).getContent().toLowerCase().startsWith("'yeast'"))
				result = true;
		}
		assertTrue(result);	
		ds.closeSession();
	}
	@Test
	public void testSearchText10() {
		boolean result = false;
		
		searchTextSetup();
		
		Preference prefs = prefManager.getUserPreference();
		prefs.setThreshold(10000);
		ds.startSession();
		// Search string with words in single quote somewhere in the sentence
		List<Suggestion> suggestions = (List<Suggestion>) ds.searchText("yeast 'cakes'", prefs, true);
		assertTrue(suggestions.size() >= 1);
		for(int i = 0; i < suggestions.size(); i++){
			if(suggestions.get(i).getContent().toLowerCase().startsWith("yeast 'cakes'"))
				result = true;
		}
		assertTrue(result);	
		ds.closeSession();
	}
	@Test
	public void testSearchText11() {
		boolean result = false;
		
		searchTextSetup();
		
		Preference prefs = prefManager.getUserPreference();
		prefs.setThreshold(10000);
		ds.startSession();
		// Search string with a single quote for the prefix
		List<Suggestion> suggestions = (List<Suggestion>) ds.searchText("' yeast", prefs, true);
		assertTrue(suggestions.size() >= 1);
		for(int i = 0; i < suggestions.size(); i++){
			if(suggestions.get(i).getContent().toLowerCase().startsWith("' yeast"))
				result = true;
		}
		assertTrue(result);	
		ds.closeSession();
	}
	@Test
	public void testSearchText12() {
		boolean result = false;
		
		searchTextSetup();
		
		Preference prefs = prefManager.getUserPreference();
		prefs.setThreshold(10000);
		ds.startSession();
		// Search string with a single quote somewhere in the sentence
		List<Suggestion> suggestions = (List<Suggestion>) ds.searchText("yeast '", prefs, true);
		assertTrue(suggestions.size() >= 1);
		for(int i = 0; i < suggestions.size(); i++){
			if(suggestions.get(i).getContent().toLowerCase().startsWith("yeast '"))
				result = true;
		}
		assertTrue(result);	
		ds.closeSession();
	}
	@Test
	public void testSearchText13() {
		boolean result = false;
		
		searchTextSetup();
		
		Preference prefs = prefManager.getUserPreference();
		prefs.setThreshold(10000);
		ds.startSession();
		// Search string with words in double quote as prefix
		List<Suggestion> suggestions = (List<Suggestion>) ds.searchText("\"yeast\"", prefs, true);
		assertTrue(suggestions.size() >= 1);
		for(int i = 0; i < suggestions.size(); i++){
			if(suggestions.get(i).getContent().toLowerCase().startsWith("\"yeast\""))
				result = true;
		}
		assertTrue(result);	
		ds.closeSession();
	}
	@Test
	public void testSearchText14() {
		boolean result = false;
		
		searchTextSetup();
		
		Preference prefs = prefManager.getUserPreference();
		prefs.setThreshold(10000);
		ds.startSession();
		// Search string with words in double quote somewhere in the sentence
		List<Suggestion> suggestions = (List<Suggestion>) ds.searchText("yeast \"cakes\"", prefs, true);
		assertTrue(suggestions.size() >= 1);
		for(int i = 0; i < suggestions.size(); i++){
			if(suggestions.get(i).getContent().toLowerCase().startsWith("yeast \"cakes\""))
				result = true;
		}
		assertTrue(result);	
		ds.closeSession();
	}
	@Test
	public void testSearchText15() {
		boolean result = false;
		
		searchTextSetup();
		
		Preference prefs = prefManager.getUserPreference();
		prefs.setThreshold(10000);
		ds.startSession();
		// Search string with a double quote for the prefix
		List<Suggestion> suggestions = (List<Suggestion>) ds.searchText("\" yeast", prefs, true);
		assertTrue(suggestions.size() >= 1);
		for(int i = 0; i < suggestions.size(); i++){
			if(suggestions.get(i).getContent().toLowerCase().startsWith("\" yeast"))
				result = true;
		}
		assertTrue(result);	
		ds.closeSession();
	}
	@Test
	public void testSearchText16() {
		boolean result = false;
		
		searchTextSetup();
		
		Preference prefs = prefManager.getUserPreference();
		prefs.setThreshold(10000);
		ds.startSession();
		// Search string with a double quote somewhere in the sentence
		List<Suggestion> suggestions = (List<Suggestion>) ds.searchText("yeast \"", prefs, true);
		assertTrue(suggestions.size() >= 1);
		for(int i = 0; i < suggestions.size(); i++){
			if(suggestions.get(i).getContent().toLowerCase().startsWith("yeast \""))
				result = true;
		}
		assertTrue(result);	
		ds.closeSession();
	}
	@Test
	public void testSearchText17() {
		boolean result = false;
		
		searchTextSetup();
		
		Preference prefs = prefManager.getUserPreference();
		prefs.setThreshold(10000);
		ds.startSession();
		// lucene special character (Only one special character)
		List<Suggestion> suggestions = (List<Suggestion>) ds.searchText("+", prefs, true);
		assertTrue(suggestions.size() >= 1);
		for(int i = 0; i < suggestions.size(); i++){
			if(suggestions.get(i).getContent().toLowerCase().startsWith("+"))
				result = true;
		}
		assertTrue(result);	
		ds.closeSession();
	}
	@Test
	public void testSearchText18() {
		boolean result = false;
		
		searchTextSetup();
		
		Preference prefs = prefManager.getUserPreference();
		prefs.setThreshold(10000);
		ds.startSession();
		// All lucene special character (Partial search)
		List<Suggestion> suggestions = (List<Suggestion>) ds.searchText("+ - && || ! ( ) { } [", prefs, true);
		assertTrue(suggestions.size() >= 1);
		for(int i = 0; i < suggestions.size(); i++){
			if(suggestions.get(i).getContent().toLowerCase().startsWith("+ - && || ! ( ) { } ["))
				result = true;
		}
		assertTrue(result);	
		ds.closeSession();
	}
	@Test
	public void testSearchText19() {
		boolean result = false;
		
		searchTextSetup();
		
		Preference prefs = prefManager.getUserPreference();
		prefs.setThreshold(10000);
		ds.startSession();
		// All lucene special character
		List<Suggestion> suggestions = (List<Suggestion>) ds.searchText("+ - && || ! ( ) { } [ ] ^ \" ~ * ? : \\", prefs, true);
		assertTrue(suggestions.size() >= 1);
		for(int i = 0; i < suggestions.size(); i++){
			if(suggestions.get(i).getContent().toLowerCase().startsWith("+ - && || ! ( ) { } [ ] ^ \" ~ * ? : \\"))
				result = true;
		}
		assertTrue(result);	
		ds.closeSession();
	}
	@Test
	public void testSearchText20() {
		searchTextSetup();
		
		Preference prefs = prefManager.getUserPreference();
		prefs.setThreshold(10000);
		ds.startSession();
		// Empty user input
		List<Suggestion> suggestions = (List<Suggestion>) ds.searchText("", prefs, true);
		assertTrue(suggestions.size() == 0); 
		ds.closeSession();
	}
	@Test
	public void testSearchText21() {
		searchTextSetup();
		
		Preference prefs = prefManager.getUserPreference();
		prefs.setThreshold(10);
		ds.startSession();
		// Correct prefix without typing mistake with more than the default threshold of 10 (Should not return result)
		List<Suggestion> suggestions = (List<Suggestion>) ds.searchText("yeast", prefs, true);
		assertTrue(suggestions.size() == 0); 
		ds.closeSession();
		
	}
	@Test
	public void testSearchText22() {
		searchTextSetup();
		
		Preference prefs = prefManager.getUserPreference();
		prefs.setThreshold(10000);
		ds.startSession();
		// Null userinput
		List<Suggestion> suggestions = (List<Suggestion>) ds.searchText(null, prefs, true);
		assertTrue(suggestions.size() == 0);
		ds.closeSession();
	}
	@Test
	public void testSearchText23() {
		searchTextSetup();
		
		Preference prefs = prefManager.getUserPreference();
		prefs.setThreshold(10000);
		ds.startSession();
		// Null preference
		List<Suggestion> suggestions = (List<Suggestion>) ds.searchText("aaa", null, true);
		assertTrue(suggestions.size() == 0);
		ds.closeSession();
	}
	@Test
	public void testSearchText24() {
		searchTextSetup();
		
		Preference prefs = prefManager.getUserPreference();
		prefs.setThreshold(10000);
		ds.startSession();
		// Null userinput and preference
		List<Suggestion> suggestions = (List<Suggestion>) ds.searchText(null, null, true);
		assertTrue(suggestions.size() == 0);
		ds.closeSession();
	}
	
	public void searchEntitySetup(){
		ds.startSession();
		
		ds.startTransaction();
		int sourceID = ds.storeMetadata(source1);
		sourceAdded.add(sourceID);
		assertTrue(sourceID > 0);
		
		para1.setSourceID(sourceID);
		int pid = ds.storeParagraph(para1);
		assertTrue(pid > 0);
		
		// Add sentences from testsearchentity.txt
		for(Entity e : entities){
			e.setSourceID(sourceID);
			int eid = ds.storeEntity(e);
			assertTrue(pid > 0);
		}
		ds.commitTransaction();
		ds.closeSession();
	}
	@Test
	public void testSearchEntity1() {
		boolean result = false;
		
		searchEntitySetup();
		
		Preference prefs = prefManager.getUserPreference();
		prefs.setThreshold(10000);
		ds.startSession();
		// Correct input without typing mistake (At least 1 Match)
		List<Suggestion> entities = (List<Suggestion>) ds.searchEntity("yanhao", prefs, true);
		assertTrue(entities.size() >= 1);
		for(int i = 0; i < entities.size(); i++){
			if(entities.get(i).getContent().toLowerCase().contains("yanhao"))
				result = true;
		}
		assertTrue(result);	
		ds.closeSession();
	}
	@Test
	public void testSearchEntity2() {	
		searchEntitySetup();
		
		Preference prefs = prefManager.getUserPreference();
		prefs.setThreshold(10000);
		ds.startSession();
		// Null userInput
		List<Suggestion> entities = (List<Suggestion>) ds.searchEntity(null, prefs, true);
		assertTrue(entities.size() == 0);
		ds.closeSession();
	}
	@Test
	public void testSearchEntity3() {	
		searchEntitySetup();
		
		Preference prefs = prefManager.getUserPreference();
		prefs.setThreshold(10000);
		ds.startSession();
		// Null preference
		List<Suggestion> entities = (List<Suggestion>) ds.searchEntity("aaa", null, true);
		assertTrue(entities.size() == 0);
		ds.closeSession();
	}
	@Test
	public void testSearchEntity4() {	
		searchEntitySetup();
		
		Preference prefs = prefManager.getUserPreference();
		prefs.setThreshold(10000);
		ds.startSession();
		// Null userinput and preference
		List<Suggestion> entities = (List<Suggestion>) ds.searchEntity(null, null, true);
		assertTrue(entities.size() == 0);
		ds.closeSession();
	}
	
	@Test
	public void testBulkInsertSentence() {
		ds.startSession();
		ds.startTransaction();
		
		int sourceID = ds.storeMetadata(source1);
		sourceAdded.add(sourceID);

		para1.setSourceID(sourceID);
		int pid = ds.storeParagraph(para1);
		
		double start = System.currentTimeMillis();

		// Add 64 sentences from testsearchtext.txt
		for(Sentence s : sentences){
			s.setParagraphID(pid);
			int sid = ds.storeSentence(s);
			//assertTrue(sid > 0);
		}
		
		double end = System.currentTimeMillis();
		double time = (end-start)/1000;
		
		assertTrue(time <= 1); // Test time taking to be <= 1 sec
		
		ds.commitTransaction();
		ds.closeSession();
	}
}
