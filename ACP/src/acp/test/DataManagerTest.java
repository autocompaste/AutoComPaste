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
import acp.beans.ProcessedText;
import acp.beans.Suggestion;
import acp.beans.entity.Entity;
import acp.beans.entity.Sentence;
import acp.beans.entity.Source;
import acp.beans.entity.Paragraph;
import acp.manager.DataManager;
import acp.manager.PreferenceManager;
import acp.store.DataStore;
import acp.util.SessionFactoryUtil;

/**
 * For testing of DataManager
 * 
 * @author Teo Kee Cheng
 *
 */
public class DataManagerTest {

	private static final int SENTENCE = 1;
	private static final int PARAGRAPH = 2;
	private static final int NOTHING = 3;
	
	private static final int FIRST_SENTENCE_OF_PARA  = 0;
	private static final int FIRST_PARA_OF_SOURCE  = 0;
	
	private static DataManager dm;
	private static DataStore ds;
	private static PreferenceManager prefManager;
	private ProcessedText pt1, pt2, pt3, pt4, pt5, pt6;
	
	private int sourceID = -1;
	private Paragraph p = null;
	private Sentence s = null;
	private Entity e = null;
	private List<Sentence> firstParaSentences = null;
	private List<Sentence> lastParaSentences = null;
	private Sentence firstSentence = null;
	private Sentence lastSentenceOfFirstPara = null;
	private Sentence lastSentenceOfLastPara = null;
	
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
		
		// Setup for creating ProcessedText object
		List<Paragraph> paragraphs = new ArrayList<Paragraph>();
		List<Paragraph> paragraphsWithNoSentences = new ArrayList<Paragraph>();
		
		Scanner sc = null, sc1 = null;
		try {
			sc = new Scanner(new File("testsearchtext.txt"));
			sc1 = new Scanner(new File("testsearchentity.txt"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		sc.useDelimiter("[.]");
		sc1.useDelimiter("[|]");
		
		List<Sentence> sentences = new ArrayList<Sentence>();
		int sCount = 0;
		int pCount = 0;
		while(sc.hasNext()){
			String temp = sc.next().trim();

			if(sCount > 0 && (sCount%10 == 0 || !sc.hasNext())){
				Paragraph p = new Paragraph(pCount);
				paragraphsWithNoSentences.add(p);
				
				p = new Paragraph(pCount);
				p.setSentences(sentences);
				paragraphs.add(p);
				pCount++;
				
				sentences = new ArrayList<Sentence>();
			}
			
			if(!temp.equals(""))
				sentences.add(new Sentence((sCount%10), temp));
			
			sCount++;
		}
		
		List<Entity> entities = new ArrayList<Entity>();
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
		
		Source s = new Source("testsearchtext.txt", "testsearchtext.txt");
		pt1 = new ProcessedText(s, paragraphs, null); 
		
		s = new Source("testsearchtext1.txt", "testsearchtext1.txt");
		pt2 = new ProcessedText(s, paragraphsWithNoSentences, null); 
		
		pt3 = new ProcessedText();
		pt3.setParagraphs(paragraphs);
		
		pt4 = new ProcessedText();
		s = new Source("testsearchtext2.txt", "testsearchtext2.txt");
		pt4.setMetadata(s);
		
		s = new Source("testsearchtext3.txt", "testsearchtext3.txt");
		pt5 = new ProcessedText(s, paragraphs, entities);  
		
		s = new Source("testsearchtext4.txt", "testsearchtext4.txt");
		pt6 = new ProcessedText(s, null, entities);  
	}

	@After
	public void tearDown() throws Exception {
		if(ds.getSession() != null && ds.getSession().isConnected())
			ds.closeSession();
		
		dm.removeData(pt1.getMetadata().getUrl());
		dm.removeData(pt2.getMetadata().getUrl());
		dm.removeData(pt5.getMetadata().getUrl());
		dm.removeData(pt6.getMetadata().getUrl());
	}

	@Test
	public void testStoreProcessTest1(){
		// Storing of paragraphs and entities
		int sourceID = dm.storeProcessedText(pt5);
		assertTrue(sourceID > 0);
		
		ds.startSession();
		Source source = ds.getSource(pt5.getMetadata().getUrl());
		List<Paragraph> paragraphs = (List<Paragraph>) ds.getParagraphs(source.getSourceID());
		assertTrue(pt5.getMetadata().getName().equals(source.getName()));
		assertTrue(pt5.getParagraphs().size() == paragraphs.size());
		
		for(int i = 0; i < pt5.getParagraphs().size(); i++){
			Paragraph p = paragraphs.get(i);
			Paragraph originalP = pt5.getParagraphs().get(i);
			assertTrue(originalP.getParentOrder() == p.getParentOrder());
			
			List<Sentence> sentences = (List<Sentence>) ds.getSentences(p.getId());
			List<Sentence> originalSentences = (List<Sentence>) originalP.getSentences();
			for(int j = 0; j < originalSentences.size(); j++){
				assertTrue(originalSentences.get(j).getContent().substring(DataStore.SENTENCE_PREFIX.length()).equals(sentences.get(j).getContent()));
				assertTrue(originalSentences.get(j).getParentOrder() == sentences.get(j).getParentOrder());
			}
		}
		List<Entity> entities = (List<Entity>) ds.getEntities(sourceID);
		assertTrue(pt5.getEntities().size() == entities.size());
		
		for(int i = 0; i < pt5.getEntities().size(); i++){
			Entity originalEntity = pt5.getEntities().get(i);
			Entity retrievedEntity = entities.get(i);
			assertTrue(originalEntity.getContent().equals(retrievedEntity.getContent()));
		}
		ds.closeSession();
	}
	@Test
	public void testStoreProcessText2() {
		// Storing of paragraphs but without entities
		int sourceID = dm.storeProcessedText(pt1);
		assertTrue(sourceID > 0);
		
		ds.startSession();
		Source source = ds.getSource(pt1.getMetadata().getUrl());
		List<Paragraph> paragraphs = (List<Paragraph>) ds.getParagraphs(source.getSourceID());
		assertTrue(pt1.getMetadata().getName().equals(source.getName()));
		assertTrue(pt1.getParagraphs().size() == paragraphs.size());
		
		for(int i = 0; i < pt1.getParagraphs().size(); i++){
			Paragraph p = paragraphs.get(i);
			Paragraph originalP = pt1.getParagraphs().get(i);
			assertTrue(originalP.getParentOrder() == p.getParentOrder());
			
			List<Sentence> sentences = (List<Sentence>) ds.getSentences(p.getId());
			List<Sentence> originalSentences = (List<Sentence>) originalP.getSentences();
			for(int j = 0; j < originalSentences.size(); j++){
				assertTrue(originalSentences.get(j).getContent().substring(DataStore.SENTENCE_PREFIX.length()).equals(sentences.get(j).getContent()));
				assertTrue(originalSentences.get(j).getParentOrder() == sentences.get(j).getParentOrder());
			}
		}
		ds.closeSession();
	}
	
	@Test
	public void testStoreProcessText3() {
		// Storing of entities but without paragraphs
		int sourceID = dm.storeProcessedText(pt6);
		assertTrue(sourceID > 0);
		
		ds.startSession();
		Source source = ds.getSource(pt6.getMetadata().getUrl());
		assertTrue(pt6.getMetadata().getName().equals(source.getName()));
		
		List<Entity> entities = (List<Entity>) ds.getEntities(sourceID);
		assertTrue(pt6.getEntities().size() == entities.size());
		
		for(int i = 0; i < pt6.getEntities().size(); i++){
			Entity originalEntity = pt6.getEntities().get(i);
			Entity retrievedEntity = entities.get(i);
			assertTrue(originalEntity.getContent().equals(retrievedEntity.getContent()));
		}
		ds.closeSession();
		
		
	}
	
	@Test
	public void testStoreProcessText4() {
		// Sentences object is null in ProcessedText object 
		int sourceID = dm.storeProcessedText(pt2);
		assertTrue(sourceID > 0);
		
		ds.startSession();
		Source source = ds.getSource(pt2.getMetadata().getUrl());
		List<Paragraph> paragraphs = (List<Paragraph>) ds.getParagraphs(source.getSourceID());
		assertTrue(pt2.getMetadata().getName().equals(source.getName()));
		assertTrue(pt2.getParagraphs().size() == paragraphs.size());
		
		for(int i = 0; i < pt2.getParagraphs().size(); i++){
			Paragraph p = paragraphs.get(i);
			Paragraph originalP = pt2.getParagraphs().get(i);
			assertTrue(originalP.getParentOrder() == p.getParentOrder());
			
			List<Sentence> sentences = (List<Sentence>) ds.getSentences(p.getId());
			assertTrue(sentences.size() == 0);
		}
		ds.closeSession();		
	}
	
	@Test
	public void testStoreProcessText5() {
		// Null object as input
		int sourceID = dm.storeProcessedText(null);
		assertTrue(sourceID == -1);
	}
	
	@Test
	public void testStoreProcessText6() {
		// Source object is null in ProcessedText object (Source obj must not be null)
		int sourceID = dm.storeProcessedText(pt3);
		assertTrue(sourceID == -1);
	}
	
	@Test
	public void testStoreProcessText7() {		
		// Paragraphs and entities object is null in ProcessedText object 
		int sourceID = dm.storeProcessedText(pt4);
		assertTrue(sourceID == -1);
	}

	public void removeDataSetup(){
		sourceID = dm.storeProcessedText(pt5);
		assertTrue(sourceID > 0);
	}
	@Test
	public void testRemoveData1() {
		removeDataSetup();
		
		// normal remove
		ds.startSession();
		List<Paragraph> paragraphs = (List<Paragraph>) ds.getParagraphs(sourceID);
		ds.closeSession();
		dm.removeData(pt5.getMetadata().getUrl());
		ds.startSession();
		for(Paragraph p : paragraphs){
			List<Sentence> sentences = (List<Sentence>) ds.getSentences(p.getId());
			assertTrue(sentences.size() == 0);
		}
		paragraphs = (List<Paragraph>) ds.getParagraphs(sourceID);
		assertTrue(paragraphs.size() == 0);
		List<Entity> entities = (List<Entity>) ds.getEntities(sourceID);
		assertTrue(entities.size() == 0);
		Source s = ds.getSource(sourceID);
		assertNull(s);
		ds.closeSession();
	}
	
	@Test
	public void testRemoveAllData1() {
		int sourceID = dm.storeProcessedText(pt1);
		assertTrue(sourceID > 0);
		
		dm.removeAllData();
		
		ds.startSession();
		List<Paragraph> paragraphs = (List<Paragraph>) ds.getParagraphs(sourceID);
		for(Paragraph p : paragraphs){
			List<Sentence> sentences = (List<Sentence>) ds.getSentences(p.getId());
			assertTrue(sentences.size() == 0);
		}
		paragraphs = (List<Paragraph>) ds.getParagraphs(sourceID);
		assertTrue(paragraphs.size() == 0);
		Source s = ds.getSource(sourceID);
		assertNull(s);
		ds.closeSession();
	}
	
	public void retrieveSourceSetup(){
		sourceID = dm.storeProcessedText(pt5);
		assertTrue(sourceID > 0);
		
		ds.startSession();
		List<Paragraph> paragraphs = (List<Paragraph>) ds.getParagraphs(sourceID);
		p = paragraphs.get(0);
		List<Sentence> sentences = (List<Sentence>) ds.getSentences(p.getId());
		s = sentences.get(0);
		List<Entity> entities = (List<Entity>) ds.getEntities(sourceID);
		e = entities.get(0);
		ds.closeSession();
	}
	@Test
	public void testRetrieveSource1() {
		retrieveSourceSetup();
		
		// Retrieve by paragraph id
		Source source = dm.retrieveSource(p.getId(), Suggestion.PARAGRAPH);
		assertTrue(source.getSourceID() == sourceID);
		
	}
	@Test
	public void testRetrieveSource2() {
		retrieveSourceSetup();
		
		// Retrieve by entity id
		Source source = dm.retrieveSource(e.getId(), Suggestion.ENTITY);
		assertTrue(source.getSourceID() == sourceID);
	}
	@Test
	public void testRetrieveSource3() {
		retrieveSourceSetup();
		
		// Retrieve by sentence id
		Source source = dm.retrieveSource(s.getId(), Suggestion.SENTENCE);
		assertTrue(source.getSourceID() == sourceID);
		
		
	}
	@Test
	public void testRetrieveSource4() {
		retrieveSourceSetup();
		
		// Retrieve by non-existing paragraph id
		Source source = dm.retrieveSource(p.getId() + 100000, Suggestion.PARAGRAPH);
		assertNull(source);
	}
	@Test
	public void testRetrieveSource5() {
		retrieveSourceSetup();
		
		// Retrieve by non-existing entity id
		Source source = dm.retrieveSource(e.getId() + 100000, Suggestion.ENTITY);
		assertNull(source);
	}
	@Test
	public void testRetrieveSource6() {
		retrieveSourceSetup();
		
		// Retrieve by non-existing sentence id
		Source source = dm.retrieveSource(s.getId() + 100000, Suggestion.SENTENCE);
		assertNull(source);
	}
	@Test
	public void testRetrieveSource7() {
		retrieveSourceSetup();
		
		// Retrieve by non-existing paragraph id and type
		Source source = dm.retrieveSource(p.getId() + 100000, -100);
		assertNull(source);
	}
	@Test
	public void testRetrieveSource8() {
		retrieveSourceSetup();
		
		// Retrieve by non-existing entity id and type
		Source source = dm.retrieveSource(e.getId() + 100000, -100);
		assertNull(source);
	}
	@Test
	public void testRetrieveSource9() {
		retrieveSourceSetup();
		
		// Retrieve by non-existing sentence id and type
		Source source = dm.retrieveSource(s.getId() + 100000, -100);
		assertNull(source);
	}
	@Test
	public void testRetrieveSourceByURL1() {
		retrieveSourceSetup();
		
		ds.startSession();
		Source s = ds.getSource(sourceID);
		ds.closeSession();
		
		// Normal case
		Source source = dm.retrieveSource(s.getUrl());
		assertTrue(source.getSourceID() == sourceID);
		
	}
	@Test
	public void testRetrieveSourceByURL2() {
		retrieveSourceSetup();
		
		// Non-existing url
		Source source = dm.retrieveSource("~!@FF$^DVdbdg$%^'\"");
		assertNull(source);
	}
	@Test
	public void testRetrieveSourceByURL3() {
		retrieveSourceSetup();
		
		// NULL input
		Source source = dm.retrieveSource(null);
		assertNull(source);
		
		
	}
	
	public void retrieveSubsequentTextSetup(){
		int sourceID = dm.storeProcessedText(pt1);
		assertTrue(sourceID > 0);
		
		ds.startSession();
		List<Paragraph> paragraphs = (List<Paragraph>) ds.getParagraphs(sourceID);
		Paragraph firstParagraph = paragraphs.get(0);
		assertNotNull(firstParagraph);
		Paragraph lastParagraph = paragraphs.get(paragraphs.size()-1);
		assertNotNull(lastParagraph);
		
		firstParaSentences = (List<Sentence>) ds.getSentences(firstParagraph.getId());
		lastParaSentences = (List<Sentence>) ds.getSentences(lastParagraph.getId());
		firstSentence = firstParaSentences.get(0);
		assertNotNull(firstSentence);
		lastSentenceOfFirstPara = firstParaSentences.get(firstParaSentences.size()-1);
		assertNotNull(lastSentenceOfFirstPara);
		lastSentenceOfLastPara = lastParaSentences.get(lastParaSentences.size()-1);
		assertNotNull(lastSentenceOfLastPara);
		ds.closeSession();
	}
	@Test
	public void testRetrieveSubsequentText1() {
		retrieveSubsequentTextSetup();
		
		// first sentence retrieve next sentence
		Suggestion sugg = dm.retrieveSubsequentText(firstSentence.getId(), Suggestion.SENTENCE);
		isSubsequentSuggestCorrect(firstSentence, sugg, SENTENCE);
		
	}
	@Test
	public void testRetrieveSubsequentText2() {
		retrieveSubsequentTextSetup();
		
		// last sentence of first paragraph retrieve next sentence
		Suggestion sugg = dm.retrieveSubsequentText(lastSentenceOfFirstPara.getId(), Suggestion.SENTENCE);
		isSubsequentSuggestCorrect(lastSentenceOfFirstPara, sugg, SENTENCE);
		
	}
	@Test
	public void testRetrieveSubsequentText3() {
		retrieveSubsequentTextSetup();
		
		// last sentence of last paragraph retrieve next sentence
		Suggestion sugg = dm.retrieveSubsequentText(lastSentenceOfLastPara.getId(), Suggestion.SENTENCE);
		isSubsequentSuggestCorrect(lastSentenceOfLastPara, sugg, NOTHING);
		
	}
	@Test
	public void testRetrieveSubsequentText4() {
		retrieveSubsequentTextSetup();
		
		// first sentence retrieve current paragraph
		Suggestion sugg = dm.retrieveSubsequentText(firstSentence.getId(), Suggestion.PARAGRAPH);
		isSubsequentSuggestCorrect(firstSentence, sugg, PARAGRAPH);
		
	}
	@Test
	public void testRetrieveSubsequentText5() {
		retrieveSubsequentTextSetup();
		
		// last sentence retrieve next paragraph
		Suggestion sugg = dm.retrieveSubsequentText(lastSentenceOfFirstPara.getId(), Suggestion.PARAGRAPH);
		isSubsequentSuggestCorrect(lastSentenceOfFirstPara, sugg, PARAGRAPH);
		
	}
	@Test
	public void testRetrieveSubsequentText6() {
		retrieveSubsequentTextSetup();
		
		// non-existing sentence id
		Suggestion sugg = dm.retrieveSubsequentText(firstSentence.getId()+10000, Suggestion.SENTENCE);
		isSubsequentSuggestCorrect(firstSentence, sugg, NOTHING);
		
	}
	@Test
	public void testRetrieveSubsequentText7() {
		retrieveSubsequentTextSetup();
		
		// non-existing type
		Suggestion sugg = dm.retrieveSubsequentText(firstSentence.getId(), -100);
		isSubsequentSuggestCorrect(firstSentence, sugg, NOTHING);
	}

	private void isSubsequentSuggestCorrect(Suggestion current, Suggestion subsequent, int expected) {

		if(expected == SENTENCE){ // The parentOrder must be consecutive
			assertTrue((current instanceof Sentence) && (subsequent instanceof Sentence));
			Sentence first = (Sentence) current;
			Sentence second = (Sentence) subsequent;
			
			if(first.getParagraphID() == second.getParagraphID())
				assertTrue(second.getParentOrder() == first.getParentOrder()+1);
			else{
				ds.startSession();
				Paragraph p1 = ds.getParagraph(first.getParagraphID());
				Paragraph p2 = ds.getParagraph(second.getParagraphID());
				ds.closeSession();
				
				assertTrue(p2.getParentOrder() == p1.getParentOrder()+1);
				assertTrue(second.getParentOrder() == FIRST_SENTENCE_OF_PARA);
			}
		}else if(expected == PARAGRAPH){ // The sentence must be last sentence of previous paragraph and paragraph retrieve must be consecutive
			assertTrue((current instanceof Sentence) && (subsequent instanceof Paragraph));
			Sentence first = (Sentence) current;
			Paragraph second = (Paragraph) subsequent;
			
			ds.startSession();
			Paragraph p = ds.getParagraph(first.getParagraphID());
			List<Sentence> sentences = (List<Sentence>) ds.getSentences(p.getId());
			Sentence lastSentenceOfP = sentences.get(sentences.size()-1);
			ds.closeSession();
			assertNotNull(lastSentenceOfP);
			if(first.getId() == lastSentenceOfP.getId())
				assertTrue(second.getParentOrder() == p.getParentOrder()+1);
			else
				assertTrue(second.getParentOrder() == p.getParentOrder());
		}else if(expected == NOTHING){
			assertNull(subsequent);
		}
		
	}
	
	private void searchBasedOnRequestSetup(){
		int sourceID = dm.storeProcessedText(pt5);
		assertTrue(sourceID > 0);
	}
	@Test
	public void testSearchBasedOnRequest1() {
		// if result is of sentence type
		searchBasedOnRequestSetup();
		
		Preference prefs = prefManager.getUserPreference();
		prefs.setThreshold(10000);
		
		List<Suggestion> sugg = dm.searchBasedOnRequest("Flavorful", prefs, true);
		assertTrue(sugg.size() > 0);
		assertTrue(sugg.get(0).getType() == Suggestion.SENTENCE);
	}
	@Test
	public void testSearchBasedOnRequest2() {
		// if result is of entity type
		searchBasedOnRequestSetup();
		
		Preference prefs = prefManager.getUserPreference();
		prefs.setThreshold(10000);
		
		List<Suggestion> sugg = dm.searchBasedOnRequest("lokeyanhao", prefs, true);
		assertTrue(sugg.size() > 0);
		assertTrue(sugg.get(0).getType() == Suggestion.ENTITY);
	}
	@Test
	public void testSearchBasedOnRequest3() {
		// check userInput null
		searchBasedOnRequestSetup();
		
		Preference prefs = prefManager.getUserPreference();
		prefs.setThreshold(10000);
		
		List<Suggestion> sugg = dm.searchBasedOnRequest(null, prefs, true);
		assertTrue(sugg.size() == 0);
	}
	@Test
	public void testSearchBasedOnRequest4() {
		// check Preference obj null
		searchBasedOnRequestSetup();
		
		List<Suggestion> sugg = dm.searchBasedOnRequest("Flavorful", null, true);
		assertTrue(sugg.size() == 0);
	}
}
