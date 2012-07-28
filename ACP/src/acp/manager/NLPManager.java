package acp.manager;

import acp.beans.ProcessedText;
import acp.beans.entity.*;
import acp.util.GateParser;
import gate.*;
import gate.util.*;
import gate.corpora.RepositioningInfo;

import java.io.File;
import java.net.MalformedURLException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * NLP Manager is the component which takes the unprocessed extracted text (given by the text-extractor) and 
 * parses it using Natural Language Processing. The parsed text is composed of different entities.
 * The NLP manager sends the processed text to the ACP Logic. 
 * 
 * @author Amulya Khare
 *
 */
public class NLPManager {

	private GateParser parser;
	final static Logger logger = LoggerFactory.getLogger(NLPManager.class);
	
	public static final List<String> EMAILTOKENS = new ArrayList<String>(Arrays.asList("mail", "contact", "e-mail", "mailbox", "(find|drop|leave|send).+(message|us|our|him|her|them|me|note)")); 
	public static final List<String> URLTOKENS = new ArrayList<String>(Arrays.asList("link", "webpage", "url", "web-page", "website", "web page", "web address", "site", "homepage", "home page", "(visit|comment).+(page)")); 
	public static final List<String> NAMESALUTATION = new ArrayList<String>(Arrays.asList("mr\\.", "mrs\\.", "ms\\.", "miss", "mdm\\.", "dr\\.", "madame", "monsieur", "master", "madam", "prof\\.", "professor", "name"));
	public static final List<String> PLACETOKENS = new ArrayList<String>(Arrays.asList("place", "street", "locate", "location", "area", "region", "site", "venue", "residence", "city", "town", "village", "live", "reside")); 
	public static final List<String> NAMETOKENS = new ArrayList<String>(Arrays.asList("name", "title"));
	
	/**
	 * Default constructor for the NLPManager. It returns an empty instance of NLPManager. To initialize the
	 * NLP related libraries please use the initializing Gate method.
	*/
	public NLPManager()
	{
	}
	
	/**
	 * Initializes the required libraries for the component to work. This method may take a 
	 * few seconds to complete initialization.
	 * <p>
	 * This method may throw GateException if the gate library failed to be located or initialized.
	 *
	 *@throws GateException
	 *@throws MalformedURLException
	 */
	public void initializingGate() throws GateException, MalformedURLException
	{
		// initialize the GATE library
		Gate.init();

		// Load ANNIE plug-in
		File gateHome = Gate.getGateHome();
		File pluginsHome = new File(gateHome, "plugins");
		Gate.getCreoleRegister().registerDirectories(new File(pluginsHome, "ANNIE").toURL());

		// initialize ANNIE (this may take several minutes)
		parser = new GateParser();
		parser.init();
		
	}
	
	/**
	 * Takes a paragraph string as input parameter and returns a paragraph object after performing
	 * natural language processing on it.
	 *
	 * @param  		paraString a string representing a paragraph.
	 * @return      the processed paragraph object.
	 * @see         Paragraph
	 * @throws		GateException
	 */
	private Paragraph ProcessParagraph(int parent_order, String paraString) throws GateException
	{
		Document doc = createHTMLDocument(paraString);
		
		Corpus corpus = (Corpus) Factory.createResource("gate.corpora.CorpusImpl");
		corpus.add(doc);

		// tell the pipeline about the corpus and run it
		parser.setCorpus(corpus);
		try{
			parser.execute();
		}
		catch(Exception e) {
			// the document was either empty or null. 
			// GATE was somehow unable to parse it at all.
			logger.debug(e.getMessage());
		}
		
		AnnotationSet defaultAnnotSet = doc.getAnnotations();
		Set<String> annotTypesRequired = new HashSet<String>();
		annotTypesRequired.add("Sentence");
		Set<Annotation> sentenceAnnotations = new HashSet<Annotation>(defaultAnnotSet.get(annotTypesRequired));
		
		this.removeDuplicateAnnotations(sentenceAnnotations);
		
		List<Annotation> sentenceAnnotationList = new ArrayList<Annotation>(sentenceAnnotations);
		Collections.sort(sentenceAnnotationList, new OffsetComparator());
		Iterator<Annotation> it = sentenceAnnotationList.iterator();
		List<Sentence> sentenceList = new ArrayList<Sentence>();
		String docString = doc.getContent().toString();
		int i=0;
		
		while(it.hasNext()){
			Annotation a = (Annotation) it.next();
			String sentence = docString.substring((int)a.getStartNode().getOffset().longValue(),(int)a.getEndNode().getOffset().longValue());
			
			String str[] = sentence.split("\n");
			
			for(int j=0;j<str.length; j++) {
				
				//clean up sentence
				String clean = cleanUp(str[j]);
				
				//adjust full stop placement
				clean = adjustFullStop(clean);
				
				sentenceList.add(new Sentence(i,clean));
				i++;
			}
		}
		Paragraph p = new Paragraph(parent_order);
		p.setSentences(sentenceList);
		
		return p;
	}

	/**
	 * @param sentence to be adjusted for an extra space at the end, just before the '.'
	 * @return
	 */
	private String adjustFullStop(String sentence) {
		if(sentence.length() > 2) {
			char lastButOneCharacter = sentence.charAt(sentence.length()-2);
			if(lastButOneCharacter == ' ')
			{
				sentence = sentence.substring(0,sentence.length()-2).trim()+".";
			}
		}
		return sentence;
	}

	/**
	 * @param sentence to be cleaned up for extra spacing or '^' character
	 * @return
	 */
	private String cleanUp(String sentence) {
		sentence = sentence.replaceAll("^\\xA0","");
		sentence = sentence.replaceAll("^\\s","");
		sentence = sentence.replaceAll("\\s,",",");
		if(sentence.length() != 0) {
			char firstCharacter = sentence.charAt(0);
			if(firstCharacter == '^' || firstCharacter == ' ')
			{
				sentence = sentence.substring(1).trim();
			}
		}
		return sentence;
	}
	
	/**
	 * This method would take in an HTML string and create a GATE document with the correct document format.
	 *
	 * @param  		text a string of HTML content to be used for extraction
	 * @return      the GATE document which will be used by the parser for further processing
	 * @see         processText
	 */
	private Document createHTMLDocument(String text) throws GateException
	{
		Document doc = (Document)Factory.newDocument(text);
		doc.setMarkupAware(new Boolean(true)); 
		List<LanguageResource> tdfInstances = Gate.getCreoleRegister().getLrInstances("gate.corpora.TextualDocumentFormat"); 
		DocumentFormat format = (DocumentFormat)tdfInstances.get(0); 
		format.unpackMarkup(doc);
		return doc;
	}
	
	/**
	 * This method would the GATE document after it has been parsed, and return an annotation consisting of desired entities
	 *
	 * @param  		the GATE document after it has been parsed for annotations
	 * @return      the annotation set containing all the required annotations from the document (Duplicates may exist).
	 */
	private Set<Annotation> prepareAnnotationSet(Document doc) 
	{
		
		// create an annotation set for original markups which include paragraph<p> and <a> 
		AnnotationSet originalAnnotSet = doc.getAnnotations("Original markups");
		Set<String> originalAnnotTypesRequired = new HashSet<String>();
		originalAnnotTypesRequired.add("paragraph");
		// we comment out the following line because we do not want to extract hidden urls like: <a href="www.wikipedia.com">Hidden URL</a>
		// originalAnnotTypesRequired.add("a"); 
		Set<Annotation> originalAnnotations = new HashSet<Annotation>(originalAnnotSet.get(originalAnnotTypesRequired));
		
		// create an annotation set for enitity types.
		AnnotationSet defaultAnnotSet = doc.getAnnotations();
		Set<String> defaultAnnotTypesRequired = new HashSet<String>();
		defaultAnnotTypesRequired.add("Address");
		defaultAnnotTypesRequired.add("Person");
		defaultAnnotTypesRequired.add("Location");
		Set<Annotation> defaultAnnotations = new HashSet<Annotation>(defaultAnnotSet.get(defaultAnnotTypesRequired));
		 
		// combine the two annotation sets to be used for finding all annotations
		Set<Annotation> allAnnotations = new HashSet<Annotation>(originalAnnotations);
		allAnnotations.addAll(defaultAnnotations);
		return allAnnotations;
	}
	
	/**
	 * This method would the remove duplicate annotations from a annotation list passed to it
	 *
	 * @param  		the annotation set containing all the required annotations from the document.
	 */
	private void removeDuplicateAnnotations(Set<Annotation> allAnnotations) {
		List<Annotation> annList = new ArrayList<Annotation>(allAnnotations);
		Collections.sort(annList, new OffsetComparator());

		for (int i=0 ; i < annList.size() - 1 ; i++) {
		  Annotation annI = annList.get(i);
		  
		  for (int j=i+1 ; j < annList.size() ; j++) {
		    Annotation annJ = annList.get(j);
		    
		    if (annJ.getStartNode().getOffset().equals(annI.getStartNode().getOffset())
		        && annJ.getEndNode().getOffset().equals(annI.getEndNode().getOffset()) && annJ.getType().equals(annI.getType())) {
		    	allAnnotations.remove(annI);
		      break;
		    }
		  }
		}
	}
	
	/**
	 * This method would take in raw HTML string and the sourceName as input and returns processedText back.
	 * This processed text is composed of extracted paragraphs, sentences and entities from the rawText HTML string
	 *
	 * @param  		rawText a string of HTML content to be used for extraction
	 * @param  		sourceName the name of the source document used for extraction.
	 * @return      the processed text composed of extracted paragraphs, sentences and entities from the rawText
	 */
	public ProcessedText processText(String rawText, String sourceName, String url) throws GateException
	{   		
		Document doc = createHTMLDocument(rawText);
		
		// We add the document to the GATE corpus
		Corpus corpus = (Corpus) Factory.createResource("gate.corpora.CorpusImpl");
		corpus.add(doc);

		// We tell the parser about the corpus and execute it.
		parser.setCorpus(corpus);
		try {
			parser.execute();
		}
		catch(Exception e) {
			// the document was either empty or null. 
			// GATE was somehow unable to parse it at all.
			logger.debug(e.getMessage());
		}
		
		List<Paragraph> paragraphs = new ArrayList<Paragraph>();
		List<Entity> entity = new ArrayList<Entity>();
		
		// prepare annotation set and remove duplicate annotations.
		Set<Annotation> allAnnotations = prepareAnnotationSet(doc);
		removeDuplicateAnnotations(allAnnotations);
		
		// prepare for iteration over each annotation and process it.
		List<Annotation> annotationList = new ArrayList<Annotation>(allAnnotations);
		Collections.sort(annotationList, new OffsetComparator());
		Iterator<Annotation> it = annotationList.iterator();
		String docString = doc.getContent().toString();
		int paracount=0;
		
		// process each type of annotation found and store it respectively.
		while(it.hasNext()){
			Annotation a = (Annotation) it.next();
			
			if(a.getType().equals("paragraph"))
			{
				// annotation is a paragraph, then extract sentences and store them.
				String para = docString.substring((int)a.getStartNode().getOffset().longValue(),(int)a.getEndNode().getOffset().longValue());
				paragraphs.add(ProcessParagraph(paracount,para));
				paracount++;
			}
			else
			{
				// annotation is a entity type, process it and store it in the entity list.
				processEntityAnnotation(entity, docString, a);
			}
		}
		Source s = new Source(sourceName, url);
		entity.add(new Entity(Entity.URL,url));
		ProcessedText pt = new ProcessedText(s, paragraphs, entity);
		return pt;
	}

	/**
	 * This method will take in an annotation and if the annotation type is one of the entities that is needed, it process and store it in the entity list.
	 * 
	 * @param entity list in which the extracted entity will be stored
	 * @param docStrig which is the document text in string format
	 * @param a which is the annotation being processed
	 */
	private void processEntityAnnotation(List<Entity> entity, String docString, Annotation a) 
	{
		if(a.getType().equals("a"))
		{
			String urlstr = a.getFeatures().get("href").toString();
			entity.add(new Entity(Entity.URL,urlstr));
		}
		if(a.getType().equals("Address") && a.getFeatures().get("kind").equals("email"))
		{
			String email = docString.substring((int)a.getStartNode().getOffset().longValue(),(int)a.getEndNode().getOffset().longValue());
			entity.add(new Entity(Entity.EMAIL,email));
		}
		if(a.getType().equals("Address") && a.getFeatures().get("kind").equals("url"))
		{
			String email = docString.substring((int)a.getStartNode().getOffset().longValue(),(int)a.getEndNode().getOffset().longValue());
			entity.add(new Entity(Entity.URL,email));
		}
		if(a.getType().equals("Person"))
		{
			String email = docString.substring((int)a.getStartNode().getOffset().longValue(),(int)a.getEndNode().getOffset().longValue());
			entity.add(new Entity(Entity.NAME,email));
		}
		if(a.getType().equals("Location"))
		{
			String email = docString.substring((int)a.getStartNode().getOffset().longValue(),(int)a.getEndNode().getOffset().longValue());
			entity.add(new Entity(Entity.PLACE,email));
		}
	}
	
	public int findContext(String sentence)
	{
		for(int i=0;i<NAMESALUTATION.size();i++)
		{
			try {
				String words[] = sentence.toLowerCase().split(" ");
				String toMatch = NAMESALUTATION.get(i);
				Pattern pattern = Pattern.compile(toMatch);
				Matcher matcher = pattern.matcher(words[words.length-2]);
				if (matcher.find()){
					return Entity.NAME;  
				}	
			}
			catch(Exception e) {
				//may not be enough words in the sentence to detect this case
			}
		}
		int nameMatchIndex=-1;
		for(int i=0;i<NAMETOKENS.size();i++)
		{
			String toMatch = NAMETOKENS.get(i);
			Pattern pattern = Pattern.compile(toMatch);
			Matcher matcher = pattern.matcher(sentence.toLowerCase());
			if (matcher.find()){
				nameMatchIndex = matcher.start();
			}
		}
		int emailMatchIndex=-1;
		for(int i=0;i<EMAILTOKENS.size();i++)
		{
			String toMatch = EMAILTOKENS.get(i);
			Pattern pattern = Pattern.compile(toMatch);
			Matcher matcher = pattern.matcher(sentence.toLowerCase());
			if (matcher.find()){
				emailMatchIndex = matcher.start();
			}
		}
		int URLMatchIndex=-1;
		for(int i=0;i<URLTOKENS.size();i++)
		{
			String toMatch = URLTOKENS.get(i);
			Pattern pattern = Pattern.compile(toMatch);
			Matcher matcher = pattern.matcher(sentence.toLowerCase());
			if (matcher.find()){
				URLMatchIndex = matcher.start(); 
			}
		}
		int locationMatchIndex=-1;
		for(int i=0;i<PLACETOKENS.size();i++)
		{
			String toMatch = PLACETOKENS.get(i);
			Pattern pattern = Pattern.compile(toMatch);
			Matcher matcher = pattern.matcher(sentence.toLowerCase());
			if (matcher.find()){
				locationMatchIndex = matcher.start(); 
			}
		}
		if(emailMatchIndex == URLMatchIndex && URLMatchIndex == locationMatchIndex && nameMatchIndex == locationMatchIndex && emailMatchIndex == -1)
			return 0;
		
		if(emailMatchIndex >= URLMatchIndex)
		{
			if(locationMatchIndex >= nameMatchIndex)
			{
				return emailMatchIndex >= locationMatchIndex ? Entity.EMAIL : Entity.PLACE;
			}
			else
			{
				return emailMatchIndex >= nameMatchIndex ? Entity.EMAIL : Entity.NAME;
			}
		}
		else
		{
			if(locationMatchIndex >= nameMatchIndex)
			{
				return URLMatchIndex >= locationMatchIndex ? Entity.URL : Entity.PLACE;
			}
			else
			{
				return URLMatchIndex >= nameMatchIndex ? Entity.URL : Entity.NAME;
			}
			
		}
	}
	
}