package acp.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.Properties;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import acp.ACPLogic;
import acp.beans.Suggestion;
import acp.beans.entity.Sentence;
import acp.beans.entity.Source;

/**
 * 
 * @author Loke Yan Hao
 *
 */
public class ACPLogicTest {
	private static ACPLogic logic;
	private static boolean resultForInitialiseLibraries = false;
	@BeforeClass
	public static void setupClass() {		
		logic = ACPLogic.getInstance();
		resultForInitialiseLibraries = logic.initialiseLibraries();
	}
	
	@AfterClass
	public static void closeClass(){
		logic.closeSourceDocument("testcase://ACPLogicTestProcessRawText");
		logic.closeSourceDocument("testcase://ACPLogicTestRequestSuggestion");
		logic.closeSourceDocument("testcase://ACPLogicTestChooseSuggestion1");
		logic.closeSourceDocument("testcase://ACPLogicTestChooseSuggestion2");
	}

	@Test
	public void testGetInstance() {
		assertNotNull(ACPLogic.getInstance());
	}

	@Test
	public void testInitialiseLibraries() {
		assertTrue(resultForInitialiseLibraries);
	}

	@Test
	public void testProcessRawText() {	
		logic.processRawText("<HTML><HEAD></HEAD><BODY><P>This is sentence1. This is sentence2.</P></BODY></HTML>", "ACPLogicTestProcessRawText", "testcase://ACPLogicTestProcessRawText");
		List<Suggestion> suggestions = logic.requestSuggestion("This is sentence1", false);
		
		boolean check = false;
		
		for(int i=0; i<suggestions.size(); i++){
			Suggestion suggestion = suggestions.get(i);
			if(suggestion.getSource().getName().equals("ACPLogicTestProcessRawText") && suggestion.getContent().equals("This is sentence1.")){
				check = true;
			}
		}
		assertTrue(check);
	}

	@Test
	public void testRequestSuggestion() {
		logic.processRawText("<HTML><HEAD></HEAD><BODY><P>Cake is sweet. Cake is delicious.</P></BODY></HTML>", "ACPLogicTestRequestSuggestion", "testcase://ACPLogicTestRequestSuggestion");
		List<Suggestion> suggestions = logic.requestSuggestion("Cake is", false);
		
		boolean check = false;
		boolean check1 = false;
		boolean check2 = false;
		
		if(suggestions.size() >= 2){
			check = true;
		}
		else{
			check = false;
		}
		
		// Check the size
		assertTrue(check);
		
		for(int i=0; i<suggestions.size(); i++){
			Suggestion suggestion = suggestions.get(i);
			if(suggestion.getSource().getName().equals("ACPLogicTestRequestSuggestion") && suggestion.getContent().equals("Cake is sweet.")){
				check1 = true;
			}
			if(suggestion.getSource().getName().equals("ACPLogicTestRequestSuggestion") && suggestion.getContent().equals("Cake is delicious.")){
				check2 = true;
			}
		}
		
		// Check the content
		assertTrue(check1 && check2);
	}

	@Test
	public void testChooseSuggestion() {
		logic.processRawText("<HTML><HEAD></HEAD><BODY><P>Cake is sweet. Cake is delicious.</P></BODY></HTML>", "ACPLogicTestChooseSuggestion1", "testcase://ACPLogicTestChooseSuggestion1");
		logic.processRawText("<HTML><HEAD></HEAD><BODY><P>Cake is bad. Cake is not delicious.</P></BODY></HTML>", "ACPLogicTestChooseSuggestion2", "testcase://ACPLogicTestChooseSuggestion2");
		
		List<Suggestion> suggestions = logic.requestSuggestion("cake is", false);
		Sentence testCaseSentence = null;
		for(int i=0; i<suggestions.size(); i++){
			Suggestion suggestion = suggestions.get(i);
			if(suggestion.getType() == Suggestion.SENTENCE && suggestion.getSource().getName().equals("ACPLogicTestChooseSuggestion1")){
				testCaseSentence = (Sentence) suggestion;
				break;
			}
		}
		
		logic.chooseSuggestion(testCaseSentence);
		logic.chooseSuggestion(testCaseSentence);
		logic.chooseSuggestion(testCaseSentence);
		logic.chooseSuggestion(testCaseSentence);
		logic.chooseSuggestion(testCaseSentence);
		
		int indexSuggestion1 = -1, indexSuggestion2 = -1;
		boolean check = false;
		
		suggestions = logic.requestSuggestion("Cake is", false);
		for(int i=0; i<suggestions.size(); i++){
			Suggestion suggestion = suggestions.get(i);
			if(suggestion.getSource().getName().equals("ACPLogicTestChooseSuggestion1")){
				indexSuggestion1 = i;
			}
			if(suggestion.getSource().getName().equals("ACPLogicTestChooseSuggestion2")){
				indexSuggestion2 = i;
			}
		}
		
		if(indexSuggestion1 < indexSuggestion2){
			check = true;
		}
		else{
			check = false;
		}
		
		assertTrue(check);
	}

	@Test
	public void testCloseSourceDocument() {
		logic.processRawText("<HTML><HEAD></HEAD><BODY><P>Cake is sweet. Cake is delicious.</P></BODY></HTML>", "ACPLogicTestCloseSourceDocument", "testcase://ACPLogicTestCloseSourceDocument");
		logic.closeSourceDocument("testcase://ACPLogicTestCloseSourceDocument");
		
		boolean check = true;
		
		List<Suggestion> suggestions = logic.requestSuggestion("Cake is", false);
		for(int i=0; i<suggestions.size(); i++){
			Suggestion suggestion = suggestions.get(i);
			if(suggestion.getSource().getName().equals("ACPLogicTestCloseSourceDocument")){
				check = false;
			}
		}
		
		assertTrue(check);
	}
}
