package acp.test;
import org.junit.*;
import static org.junit.Assert.*;

import java.net.*;
import java.util.Properties;

import acp.beans.*;
import acp.beans.entity.*;
import acp.manager.NLPManager;
import gate.util.*;

/**
 * @author Amulya Khare
 *
 */
public class NLPTest {

	private static NLPManager nlpManager;

	@BeforeClass
	public static void oneTimeSetUp() {
		nlpManager = new NLPManager();

		try{
			Properties props = System.getProperties();
			props.setProperty("gate.home", "GATE");
			nlpManager.initializingGate();
		} 
		catch (GateException ex) {
			fail("GATE Exception Raised");
		}
		catch (MalformedURLException ex) {   
			fail("Malformed URL Exception Raised");
		}
	}

	@AfterClass
	public static void oneTimeTearDown() {
		// one-time cleanup code
	}

	@Before
	public void setUp() {
	}

	@After
	public void tearDown() {
	}

	//Test if NLP manager instance is created.
	@Test
	public void testCreation() {
		assertNotNull(nlpManager);
	}

	/*
	 * Test: If '^' symbol is removed from sentences which have wikipedia like references.
	 */
	@Test
	public void testWikipediaReference() {
		String testString = "<HTML><HEAD><TITLE>My Web Page</TITLE></HEAD><BODY><P>^ Cake flour properties and substitutions. Gourmetsleuth.com. Retrieved on 2011-12-23.</P></BODY></HTML>";
		String testSource = "Cake - Wikipedia, the free encyclopedia - Mozilla Firefox";
		String testURL = "http://www.wikipedia.com";
		try{
			ProcessedText testText = nlpManager.processText(testString,testSource, testURL);
			isTextNull(testText);
			//sentence tests for the cleaning of the special symbol '^'
			isSentenceCountCorrent(testText.getParagraphs().get(1),3);
			isSentencePresent(testText.getParagraphs().get(1),"Cake flour properties and substitutions.");
			isSentenceAbsent(testText.getParagraphs().get(1),"^ Cake flour properties and substitutions.");
		} 
		catch (GateException ex) {
			fail("GATE Exception Raised");
		}
	}

	/*
	 * Test: If simple HTML can be processed with correct paragraph, sentence processing.
	 */
	@Test 
	public void testProcessTextWithSimpleHTML() {
		String testString = "<HTML><HEAD><TITLE>My Web Page</TITLE></HEAD><BODY><P>This is where you will enter all the text and images you want displayed in a browser window. Lets see if the sentence count is correct!</P></BODY></HTML>";
		String testSource = "Cake - Wikipedia, the free encyclopedia - Mozilla Firefox";
		String testURL = "http://www.wikipedia.com";
		try{
			ProcessedText testText = nlpManager.processText(testString,testSource, testURL);
			isTextNull(testText);
			isSourceCorrect(testText,testSource,testURL);
			//paragraph tests
			isParagraphCountCorrect(testText,2);
			assertEquals(testText.getParagraphs().get(0).getContent(),"My Web Page ");
			assertEquals(testText.getParagraphs().get(1).getContent(),"This is where you will enter all the text and images you want displayed in a browser window. Lets see if the sentence count is correct! ");
			//sentence tests
			isSentenceCountCorrent(testText.getParagraphs().get(1),2);
			isSentencePresent(testText.getParagraphs().get(1),"This is where you will enter all the text and images you want displayed in a browser window.");
			isSentencePresent(testText.getParagraphs().get(1),"Lets see if the sentence count is correct!");
			isSentenceAbsent(testText.getParagraphs().get(1),"My Web Page");
		} 
		catch (GateException ex) {
			fail("GATE Exception Raised");
		}
	}

	/*
	 * Simple test case. Multiple Paragraphs. Sentences are extracted properly with respect to paragraphs.
	 */
	@Test 
	public void testProcessTextWithFewParagraphs() {
		String testString = "<HTML><HEAD><TITLE>My Web Page</TITLE></HEAD><BODY><P>This is where you will enter all the text and images you want displayed in a browser window. Lets see if the sentence count is correct!</P><P>Hello World</P></BODY></HTML>";
		String testSource = "Cake - Wikipedia, the free encyclopedia - Mozilla Firefox";
		String testURL = "http://www.wikipedia.com";
		try{
			ProcessedText testText = nlpManager.processText(testString,testSource, testURL);
			isTextNull(testText);
			isSourceCorrect(testText,testSource, testURL);
			isParagraphCountCorrect(testText,3);
			isSentenceCountCorrent(testText.getParagraphs().get(1),2);
			isSentencePresent(testText.getParagraphs().get(2),"Hello World");
			isSentencePresent(testText.getParagraphs().get(1),"Lets see if the sentence count is correct!");
			isSentenceAbsent(testText.getParagraphs().get(1),"My Web Page");
		} 
		catch (GateException ex) {
			fail("GATE Exception Raised");
		}
	}

	/*
	 * Simple test case. Few lines. One Paragraph. Sentences are extracted properly with respect to paragraphs.
	 */
	@Test 
	public void testProcessTextWithFewSentences() {
		String testString = "<html><head></head><body>Yeast cakes are the oldest and are very similar to yeast breads.\n Such cakes are often very traditional in form, and include such pastries as babka and stollen.</body></html>";
		String testSource = "Cake - Wikipedia, the free encyclopedia - Mozilla Firefox";
		String testURL = "http://www.wikipedia.com";
		try{
			ProcessedText testText = nlpManager.processText(testString,testSource, testURL);
			isTextNull(testText);
			isSourceCorrect(testText,testSource, testURL);
			isParagraphCountCorrect(testText,1);
			isSentenceCountCorrent(testText.getParagraphs().get(0),2);
			isSentencePresent(testText.getParagraphs().get(0),"Yeast cakes are the oldest and are very similar to yeast breads.");
			isSentencePresent(testText.getParagraphs().get(0),"Such cakes are often very traditional in form, and include such pastries as babka and stollen.");
			isSentenceAbsent(testText.getParagraphs().get(0),"Such cakes are often very traditional in form, and include such pastries as babka and stollen");
		} 
		catch (GateException ex) {
			fail("GATE Exception Raised");
		}
	}

	/*
	 * Test case with few lines. One Paragraph. Check for symbols like '()'
	 */
	@Test
	public void testProcessTextwithBracket() {
		String testString = "<html><head></head><body><p>A decorated veteran of World War I, Hitler joined the German Workers' Party, precursor of the Nazi Party, in 1919, and became leader of the NSDAP in 1921. In 1923, he attempted a coup d'état, known as the Beer Hall Putsch, in Munich.</p><p> The failed coup resulted in Hitler's imprisonment, during which time he wrote his memoir, Mein Kampf (My Struggle). After his release in 1924, Hitler gained popular support by attacking the Treaty of Versailles and promoting Pan-Germanism, antisemitism, and anticommunism with charismatic oratory and Nazi propaganda. After his appointment as chancellor in 1933, he transformed the Weimar Republic into the Third Reich, a single-party dictatorship based on the totalitarian and autocratic ideology of Nazism. His aim was to establish a New Order of absolute Nazi German hegemony in continental Europe.</p></body></html>";
		String testSource = "Adolf Hitler - Wikipedia, the free encyclopedia - Mozilla Firefox";
		String testURL = "http://www.wikipedia.com";
		try{
			ProcessedText testText = nlpManager.processText(testString,testSource,testURL);
			isTextNull(testText);
			isSourceCorrect(testText,testSource,testURL);
			isParagraphCountCorrect(testText,2);
			isSentenceCountCorrent(testText.getParagraphs().get(0),2);
			isSentencePresent(testText.getParagraphs().get(0),"A decorated veteran of World War I, Hitler joined the German Workers' Party, precursor of the Nazi Party, in 1919, and became leader of the NSDAP in 1921.");
			isSentencePresent(testText.getParagraphs().get(0),"In 1923, he attempted a coup d'état, known as the Beer Hall Putsch, in Munich.");
			isSentencePresent(testText.getParagraphs().get(1),"The failed coup resulted in Hitler's imprisonment, during which time he wrote his memoir, Mein Kampf (My Struggle).");
			isSentenceAbsent(testText.getParagraphs().get(1),"The failed coup resulted in Hitler's imprisonment, during which time he wrote his memoir, Mein Kampf");
		} 
		catch (GateException ex) {
			fail("GATE Exception Raised");
		}
	}

	/*
	 * Test case with many lines. One Paragraph. Check for semicolon.
	 */
	@Test
	public void testProcessTextWithSemicolon() {
		String testString = "<html><head></head><body><p>Hello World</p><p>Some software developers find themselves stalled when trying to do object-oriented (OO) design. As programmers, they’ve understood the syntax of a programming language, and pieced together small examples. However, it is often difficult to take the next step to becoming a designer. The transition from guided learning of language features to self-directed design work is often ignored. Programmers are left to struggle through their first design projects without appropriate skills or support. This may be you. You’ve learned the language, but you can’t take the next step. While it is critically important to read examples of good design, a finished product doesn’t reveal the author’s decision-making process that created the design. There’s little support that helps a programmer come to understand the design process that leads to a final product. The most notable consequence of this skills gap is some n00b programmers will create of software that is far more complex than necessary to effectively solve a given problem. This, in turn, leads to software with high maintenance costs stemming from the low quality. It also leads to an unfair indictment of OO technology; this is usually voiced as we tried OO programming and it failed.</p></body></html>";
		String testSource = "Software Engineering - Wikipedia, the free encyclopedia - Mozilla Firefox";
		String testURL = "http://www.wikipedia.com";
		try{
			ProcessedText testText = nlpManager.processText(testString,testSource, testURL);
			isTextNull(testText);
			isSourceCorrect(testText,testSource,testURL);
			isParagraphCountCorrect(testText,2);
			isSentenceCountCorrent(testText.getParagraphs().get(1),12);
			isSentenceAbsent(testText.getParagraphs().get(1),"It also leads to an unfair indictment of OO technology;");
			isSentencePresent(testText.getParagraphs().get(1),"It also leads to an unfair indictment of OO technology; this is usually voiced as we tried OO programming and it failed.");
		} 
		catch (GateException ex) {
			fail("GATE Exception Raised");
		}
	}

	/*
	 * Test case with text containing URL. Sentences are extracted properly with respect to paragraphs.
	 */
	@Test
	public void testProcessTextWithURL() {
		String testString = "<html><head></head><body><p>Hello World <a href=\"http://www.gmail.com/\">amulyakhare@gmail.com</a></p><p>Some software developers find themselves stalled when trying to do object-oriented (OO) design. As programmers, they’ve understood the syntax of a programming language, and pieced together small examples. However, it is often difficult to take the next step to becoming a designer. The transition from guided learning of language features to self-directed design work is often ignored. Programmers are left to struggle through their first design projects without appropriate skills or support. This may be you. You’ve learned the language, but you can’t take the next step. While it is critically important to read examples of good design, a finished product doesn’t reveal the author’s decision-making process that created the design. There’s little support that helps a programmer come to understand the design process that leads to a final product. The most notable consequence of this skills gap is some n00b programmers will create of software that is far more complex than necessary to effectively solve a given problem. This, in turn, leads to software with high maintenance costs stemming from the low quality. It also leads to an unfair indictment of OO technology; this is usually voiced as we tried OO programming and it failed.</p></body></html>";
		String testSource = "Software Engineering - Wikipedia, the free encyclopedia - Mozilla Firefox";
		String testURL = "http://www.wikipedia.com";
		try{
			ProcessedText testText = nlpManager.processText(testString,testSource, testURL);
			isTextNull(testText);
			isSourceCorrect(testText,testSource,testURL);
			isParagraphCountCorrect(testText,2);
			isSentenceCountCorrent(testText.getParagraphs().get(1),12);
			isSentenceAbsent(testText.getParagraphs().get(1),"It also leads to an unfair indictment of OO technology;");
			isSentencePresent(testText.getParagraphs().get(1),"It also leads to an unfair indictment of OO technology; this is usually voiced as we tried OO programming and it failed.");
		} 
		catch (GateException ex) {
			fail("GATE Exception Raised");
		}
	}

	/*
	 * Test case. Many lines. One Paragraph. Check for semicolon and abbreviation being detected properly.
	 */
	@Test
	public void testProcessTextWithAbbreviation() {
		String testString = "<html><head></head><body><p>Hello U.S.A. World <a href=\"http://www.gmail.com/\">amulyakhare@gmail.com</a></p><p>Some software developers with a C.S. degree find themselves stalled when trying to do object-oriented (OO) design. As programmers, they’ve understood the syntax of a programming language, and pieced together small examples. However, it is often difficult to take the next step to becoming a designer. The transition from guided learning of language features to self-directed design work is often ignored. Programmers are left to struggle through their first design projects without appropriate skills or support. This may be you. You’ve learned the language, but you can’t take the next step. While it is critically important to read examples of good design, a finished product doesn’t reveal the author’s decision-making process that created the design. There’s little support that helps a programmer come to understand the design process that leads to a final product. The most notable consequence of this skills gap is some n00b programmers will create of software that is far more complex than necessary to effectively solve a given problem. This, in turn, leads to software with high maintenance costs stemming from the low quality. It also leads to an unfair indictment of OO technology; this is usually voiced as we tried OO programming and it failed.</p></body></html>";
		String testSource = "Software Engineering - Wikipedia, the free encyclopedia - Mozilla Firefox";
		String testURL = "http://www.wikipedia.com";
		try{
			ProcessedText testText = nlpManager.processText(testString,testSource, testURL);
			isTextNull(testText);
			isSourceCorrect(testText,testSource,testURL);
			isParagraphCountCorrect(testText,2);
			isSentenceCountCorrent(testText.getParagraphs().get(1),12);
			isSentencePresent(testText.getParagraphs().get(0),"Hello U.S.A. World amulyakhare@gmail.com");
			isSentenceAbsent(testText.getParagraphs().get(1),"It also leads to an unfair indictment of OO technology;");
			isSentencePresent(testText.getParagraphs().get(1),"It also leads to an unfair indictment of OO technology; this is usually voiced as we tried OO programming and it failed.");
		} 
		catch (GateException ex) {
			fail("GATE Exception Raised");
		}
	}

	/*
	 * Test case to check for no extra space at end of line, when a link is present on the last word.
	 */
	@Test 
	public void testNoExtraSpace() {
		String testString = "<html><head></head><body>Yeast cakes are the oldest and are very similar to yeast breads.\n Such cakes are often very traditional in form, and include such pastries as babka and <a>stollen</a>.</body></html>";
		String testSource = "Cake - Wikipedia, the free encyclopedia - Mozilla Firefox";
		String testURL = "http://www.wikipedia.com";
		try{
			ProcessedText testText = nlpManager.processText(testString,testSource, testURL);
			isTextNull(testText);
			isSourceCorrect(testText,testSource, testURL);
			isParagraphCountCorrect(testText,1);
			isSentenceCountCorrent(testText.getParagraphs().get(0),2);
			isSentencePresent(testText.getParagraphs().get(0),"Yeast cakes are the oldest and are very similar to yeast breads.");
			isSentencePresent(testText.getParagraphs().get(0),"Such cakes are often very traditional in form, and include such pastries as babka and stollen.");
			isSentenceAbsent(testText.getParagraphs().get(0),"Such cakes are often very traditional in form, and include such pastries as babka and stollen .");
		} 
		catch (GateException ex) {
			fail("GATE Exception Raised");
		}
	}
	
	/*
	 * Test case to check for no extra space in the middle of the line, when a link is present on the word.
	 */
	@Test 
	public void testNoExtraSpaceAfterComma() {
		String testString = "<html><head></head><body>Yeast cakes are the oldest , and very similar to yeast breads.\n Such cakes are often very traditional in form, and include such pastries as babka and <a>stollen</a>.</body></html>";
		String testSource = "Cake - Wikipedia, the free encyclopedia - Mozilla Firefox";
		String testURL = "http://www.wikipedia.com";
		try{
			ProcessedText testText = nlpManager.processText(testString,testSource, testURL);
			isTextNull(testText);
			isSourceCorrect(testText,testSource, testURL);
			isParagraphCountCorrect(testText,1);
			isSentenceCountCorrent(testText.getParagraphs().get(0),2);
			isSentencePresent(testText.getParagraphs().get(0),"Yeast cakes are the oldest, and very similar to yeast breads.");
			isSentencePresent(testText.getParagraphs().get(0),"Such cakes are often very traditional in form, and include such pastries as babka and stollen.");
			isSentenceAbsent(testText.getParagraphs().get(0),"Such cakes are often very traditional in form, and include such pastries as babka and stollen .");
		} 
		catch (GateException ex) {
			fail("GATE Exception Raised");
		}
	}
	
	/*
	 * Test case to check for no extra space at end of line, when a link is present on the last word.
	 */
	@Test 
	public void testSplitofSentenceOnNewline() {
		String testString = "<html><head></head><body>Yeast cakes are the oldest , and very similar to yeast breads <br/> Such cakes are often very traditional in form, and include such pastries as babka and <a>stollen</a>.</body></html>";
		String testSource = "Cake - Wikipedia, the free encyclopedia - Mozilla Firefox";
		String testURL = "http://www.wikipedia.com";
		try{
			ProcessedText testText = nlpManager.processText(testString,testSource, testURL);
			isTextNull(testText);
			isSourceCorrect(testText,testSource, testURL);
			isParagraphCountCorrect(testText,1);
			isSentenceCountCorrent(testText.getParagraphs().get(0),2);
			isSentencePresent(testText.getParagraphs().get(0),"Yeast cakes are the oldest, and very similar to yeast breads");
			isSentencePresent(testText.getParagraphs().get(0),"Such cakes are often very traditional in form, and include such pastries as babka and stollen.");
			isSentenceAbsent(testText.getParagraphs().get(0),"Such cakes are often very traditional in form, and include such pastries as babka and stollen .");
		} 
		catch (GateException ex) {
			fail("GATE Exception Raised");
		}
	}
	
	/*
	 * Test case to check for no extra space at end of line, when a newline is terminating the line.
	 */
	@Test 
	public void testNoExtraSpaceOnNewline() {
		String testString = "<html><head></head><body>Yeast cakes are the oldest , and very similar to yeast breads . <br/> Such cakes are often very traditional in form, and include such pastries as babka and <a>stollen</a>.</body></html>";
		String testSource = "Cake - Wikipedia, the free encyclopedia - Mozilla Firefox";
		String testURL = "http://www.wikipedia.com";
		try{
			ProcessedText testText = nlpManager.processText(testString,testSource, testURL);
			isTextNull(testText);
			isSourceCorrect(testText,testSource, testURL);
			isParagraphCountCorrect(testText,1);
			isSentenceCountCorrent(testText.getParagraphs().get(0),2);
			isSentencePresent(testText.getParagraphs().get(0),"Yeast cakes are the oldest, and very similar to yeast breads.");
			isSentencePresent(testText.getParagraphs().get(0),"Such cakes are often very traditional in form, and include such pastries as babka and stollen.");
			isSentenceAbsent(testText.getParagraphs().get(0),"Such cakes are often very traditional in form, and include such pastries as babka and stollen .");
		} 
		catch (GateException ex) {
			fail("GATE Exception Raised");
		}
	}
	
	/* ---  Testing Entity Detection / Extraction --- */
	
	/*
	 * Test case: identifying entities in simple HTML. One paragraph. One line.
	 * Entities: Name, Place
	 */
	@Test
	public void testEntityExtractionInSimpleHTML() {
		String testString = "<html><head></head><body><p>Thomas Cook of Melbourne, Derbyshire, England founded a travel agency.</p></body></html>";
		String testSource = "Thomas Cook - Wikipedia, the free encyclopedia - Mozilla Firefox";
		String testURL = "http://www.wikipedia.com";
		try{
			ProcessedText testText = nlpManager.processText(testString,testSource, testURL);
			isTextNull(testText);
			//testing entities
			isEntityPresent(testText,"Thomas Cook");
			isEntityCorrectType(testText, "Thomas Cook", Entity.NAME);
			
			isEntityPresent(testText,"Melbourne");
			isEntityCorrectType(testText, "Melbourne", Entity.PLACE);
			
			isEntityPresent(testText,"England");
			isEntityCorrectType(testText, "England", Entity.PLACE);
			
		} 
		catch (GateException ex) {
			fail("GATE Exception Raised");
		}
	}
	
	/*
	 * Test case: identifying entities in simple HTML. Multiple paragraph. Multiple line.
	 * Entities: Name, Place, URL, Email
	 */
	@Test
	public void testEntityExtractionAllTypes() {
		String testString = "<html><head></head><body><p>Harvard University is an American private Ivy League research university located in Cambridge, Massachusetts, United States, established in 1636 by the Massachusetts legislature.Harvard is the oldest institution of higher learning in the United States and the first corporation (officially The President and Fellows of Harvard College) chartered in the country. Harvard's history, influence, and wealth have made it one of the most prestigious universities in the world.</p><p>Harvard was named after its first benefactor, John Harvard. The Harvard website can be found at <a>www.harvard.edu</a> Contact the harvard staff at their email contact@harvard.edu</p></body></html>";
		String testSource = "Thomas Cook - Wikipedia, the free encyclopedia - Mozilla Firefox";
		String testURL = "http://www.wikipedia.com";
		try{
			ProcessedText testText = nlpManager.processText(testString,testSource, testURL);
			isTextNull(testText);
			
			//testing entities
			isEntityPresent(testText,"John Harvard");
			isEntityCorrectType(testText, "John Harvard", Entity.NAME);
			
			isEntityPresent(testText,"Cambridge");
			isEntityCorrectType(testText, "Cambridge", Entity.PLACE);
			
			isEntityPresent(testText,"United States");
			isEntityCorrectType(testText, "United States", Entity.PLACE);
			
			isEntityPresent(testText,"www.harvard.edu");
			isEntityCorrectType(testText, "www.harvard.edu", Entity.URL);
			
			isEntityPresent(testText,"contact@harvard.edu");
			isEntityCorrectType(testText, "contact@harvard.edu", Entity.EMAIL);
		} 
		catch (GateException ex) {
			fail("GATE Exception Raised");
		}
	}
	
	/*
	 * Testing find context in multiple possible cases
	 */
	@Test
	public void testEmailCase1() {
		assertEquals(Entity.EMAIL, nlpManager.findContext("To contact send an email:"));
	}
	@Test
	public void testEmailCase2() {
		assertEquals(Entity.EMAIL, nlpManager.findContext("Please leave a message at"));
	}
	@Test
	public void testEmailCase3() {
		assertEquals(Entity.EMAIL, nlpManager.findContext("For any queries drop our team"));
	}
	@Test
	public void testEmailCase4() {
		assertEquals(Entity.EMAIL, nlpManager.findContext("Email:"));
	}
	@Test
	public void testUrlCase1() {
		assertEquals(Entity.URL, nlpManager.findContext("Checkout our website when you are free."));
	}
	@Test
	public void testUrlCase2() {
		assertEquals(Entity.URL, nlpManager.findContext("Webpage:"));
	}
	@Test
	public void testUrlCase3() {
		assertEquals(Entity.URL, nlpManager.findContext("visit our official page at"));
	}
	@Test
	public void testUrlCase4() {
		assertEquals(Entity.URL, nlpManager.findContext("The url of the demo is given below."));
	}
	@Test
	public void testNameCase1() {
		assertEquals(Entity.NAME, nlpManager.findContext("We would like to thank mr. h"));
	}
	@Test
	public void testNameCase2() {
		assertEquals(Entity.NAME, nlpManager.findContext("Our teammate is miss s"));
	}
	@Test
	public void testNameCase3() {
		assertEquals(Entity.NAME, nlpManager.findContext("Our teacher madame a"));
	}
	@Test
	public void testNameCase4() {
		assertFalse(Entity.NAME == nlpManager.findContext("Our teacher miss eline, her"));
	}
	@Test
	public void testMixedEntityCase1() {
		assertEquals(Entity.URL, nlpManager.findContext("Our company can be contacted at companyemail.gmail.com or you can visit our homepage at"));
	}
	@Test
	public void testMixedEntityCase2() {
		assertEquals(Entity.NAME, nlpManager.findContext("Our company can be contacted at companyemail.gmail.com or contact Mr. Ro"));
	}
	@Test
	public void testMixedEntityCase3() {
		assertEquals(Entity.EMAIL, nlpManager.findContext("The email of Mr. Davis is "));
	}


	//Generic HELPER Tests.
	public void isTextNull(ProcessedText text){
		assertNotNull(text);
	}

	public void isSourceCorrect(ProcessedText text, String source, String url){
		assertEquals(text.getMetadata().getSourceID(), new Source(source, url).getSourceID());
		assertEquals(text.getMetadata().getName(), new Source(source, url).getName());
	}

	public void isParagraphCountCorrect(ProcessedText text, int count){
		assertEquals(count,text.getParagraphs().size());
	}

	public void isSentenceCountCorrent(Paragraph para, int count){
		assertEquals(count,para.getSentences().size());
	}

	public void testSentencePresent(Paragraph para, String s, boolean presence){
		boolean flag = false;
		String sentWithPrefix = s;
		for(int i=0;i<para.getSentences().size();i++)
		{
			if(para.getSentences().get(i).getContent().equals(sentWithPrefix))
			{
				flag=true;
			}
		}
		assertEquals(presence,flag);
	}
	
	public void testEntityPresent(ProcessedText text, String s, boolean presence){
		boolean flag = false;
		for(int i=0;i<text.getEntities().size();i++)
		{
			if(text.getEntities().get(i).getContent().equals(s))
			{
				flag=true;
			}
		}
		assertEquals(presence,flag);
	}

	public void isSentencePresent(Paragraph para, String s){
		testSentencePresent(para, s, true);
	}

	public void isSentenceAbsent(Paragraph para, String s){
		testSentencePresent(para, s, false);
	}
	
	public void isEntityPresent(ProcessedText text, String s){
		testEntityPresent(text, s, true);
	}
	
	public void isEntityAbsent(ProcessedText text, String s){
		testEntityPresent(text, s, false);
	}
	
	public void isEntityCorrectType(ProcessedText text, String s, int type){
		for(int i=0;i<text.getEntities().size();i++)
		{
			if(text.getEntities().get(i).getContent().equals(s))
			{
				if(text.getEntities().get(i).getType() == type) {
					assert(true);
					return;
				} 
				else {
					assert(false);
					return;
				}
			}
		}
		fail("Entity not found!");
	}
}
