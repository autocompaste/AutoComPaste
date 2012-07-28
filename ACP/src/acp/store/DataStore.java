
package acp.store;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.queryParser.complexPhrase.ComplexPhraseQueryParser;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.Version;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.search.FullTextQuery;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import acp.util.SessionFactoryUtil;
import acp.beans.Preference;
import acp.beans.Suggestion;
import acp.beans.entity.Entity;
import acp.beans.entity.Paragraph;
import acp.beans.entity.Sentence;
import acp.beans.entity.Source;

/**
 * DataStore is utilize as the Data layer component to handle operation that communicates with Database. 
 * It is designed in a way that the methods doing simple and reusable task.
 * 
 * @author Teo Kee Cheng
 *
 */
public class DataStore {
	
	private final String UNICODE_LEFT_DOUBLE_QUOTE = "\u201C";
	private final String UNICODE_RIGHT_DOUBLE_QUOTE = "\u201D";
	
	private static final Class CLASS_TO_SEARCH_SENTENCE = Sentence.class;
	private static final Class CLASS_TO_SEARCH_ENTITY = Entity.class;
	private static final String FIELD_TO_SEARCH = "content";
	private static final double FUZZY_ALLOWANCE = 1.5;
	
	private static final String WILDCARD = "*";
	private static final String OR = " OR ";
	
	private static final String[] ENTITY_START_IGNORE = {"[", "(", "{"};
	
	final static Logger logger = LoggerFactory.getLogger(DataStore.class);
	public static final String SENTENCE_PREFIX = "prefix|"; // This attribute must be changed before storing of any data.
	public static final String INDEX_FOLDER_DIR = System.getProperty("user.dir")+"\\index";
	
	private Session session;
	private Transaction tx;

	/**
	 * Remove all sentences, entities, paragraphs, and sources.   
	 *
	 */
	public void removeAllDataAndIndexes(){
		logger.info("removeAllDataAndIndexes");
		
		FullTextSession fullTextSession = Search.getFullTextSession(session);
		
		org.hibernate.Query query1 = fullTextSession.createQuery("delete Sentence");
		org.hibernate.Query query2 = fullTextSession.createQuery("delete Entity");
		org.hibernate.Query query3 = fullTextSession.createQuery("delete Paragraph");
		org.hibernate.Query query4 = fullTextSession.createQuery("delete Source");
		
		try{
			int result1 = query1.executeUpdate();
			int result2 = query2.executeUpdate();
			int result3 = query3.executeUpdate();
			int result4 = query4.executeUpdate();
			
			fullTextSession.flush();
			fullTextSession.clear();
			
			fullTextSession.purgeAll(Entity.class);
			fullTextSession.purgeAll(Sentence.class);
			
			logger.debug("removed sentences: " + result1);
			logger.debug("removed entities: " + result2);
			logger.debug("removed paragraphs: " + result3);
			logger.debug("removed sources: " + result4);
		}catch(HibernateException he){
			if(tx != null)
				tx.rollback();
			
			logger.error(he.getMessage());
		}
	}
	
	/**
	 * Remove paragraphs of a source document by source ID.   
	 *
	 * @param  sourceID  	source id of the source document.
	 */
	public void removeParagraphs(int sourceID){
		logger.info("removeParagraphs");
		
		org.hibernate.Query query = session.createQuery("delete Paragraph where source_id = :id");
		query.setParameter("id", sourceID);
		
		try{
			int result = query.executeUpdate();		
				
			session.flush();
			session.clear();
					
			logger.debug("removed "+ result +" paragraph from sourceID "+sourceID);
		}catch(HibernateException he){
			if(tx != null)
				tx.rollback();
			
			logger.error(he.getMessage());
		}
	}
	
	
	/**
	 * Remove sentences and the lucene index from the database.   
	 *
	 * @param  sentences  	list of Sentence object to be removed.
	 */
	public void removeSentencesAndIndexes(List<Sentence> sentences){
		logger.info("removeSentencesAndIndexes");
		
		if(!(sentences == null || sentences.size() == 0)){
			
			try{				
				int flushCount = 0;
				for (Sentence deleteObject : sentences) {
					session.delete(deleteObject);
	
					flushCount++;
	
					if (flushCount % 20 == 0) {
						session.flush();
						session.clear();
					}
				}	
					
				logger.debug("removed " + sentences.size() + " sentences");
			}catch(HibernateException he){
				if(tx != null)
					tx.rollback();
				
				logger.error(he.getMessage());
			}
		}
	}
	
	/**
	 * Remove entities and the lucene index from the database.   
	 *
	 * @param  entities  	list of Entity object to be removed.
	 */
	public void removeEntitiesAndIndexes(List<Entity> entities){
		logger.info("removeEntitiesAndIndexes");
		
		if(!(entities == null || entities.size() == 0)){
			
			try{				
				int flushCount = 0;
				for (Entity deleteObject : entities) {
					session.delete(deleteObject);
	
					flushCount++;
	
					if (flushCount % 20 == 0) {
						session.flush();
						session.clear();
					}
				}	
					
				logger.debug("removed " + entities.size() + " entities");
			}catch(HibernateException he){
				if(tx != null)
					tx.rollback();
				
				logger.error(he.getMessage());
			}
		}
	}
	
	/**
	 * Remove source information of a source document by source ID.   
	 *
	 * @param  sourceID  	source ID of the source document.
	 */
	public void removeSource(int sourceID){
		logger.info("removeSource");
		
		org.hibernate.Query query = session.createQuery("delete Source where id = :id");
		query.setParameter("id", sourceID);
		
		try{	
			query.executeUpdate();		
				
			session.flush();
			session.clear();

			logger.debug("removed source "+sourceID);
		}catch(HibernateException he){
			if(tx != null)
				tx.rollback();
			
			logger.error(he.getMessage());
		}
	}
	
	/**
	 * Retrieve a paragraph by paragraph ID.   
	 *
	 * @param  paragraphID  	paragraph ID of the source document.
	 * @return					Paragraph object if exist, null if does not exist.
	 */
	public Paragraph getParagraph(int paragraphID) {
		logger.info("getParagraph");
		
		Paragraph p = null;
		
		p = (Paragraph) session.get(Paragraph.class, paragraphID);
		
		logger.debug("retrieved "+ p);
		return p;
	}
	
	/**
	 * Retrieve a paragraph by source ID and the order of the paragraph on the source.   
	 *
	 * @param  sourceID  		source ID of the source document.
	 * @param  parentOrder  	order of the paragraph on the source.
	 * @return					Paragraph object if exist, null if does not exist.
	 */
	public Paragraph getParagraph(int sourceID, int parentOrder) {
		logger.info("getParagraph");
		
		Paragraph p = null;
		
		org.hibernate.Query query = session.createQuery("from Paragraph where source_id = :id and parent_order = :p_order");
		query.setParameter("id", sourceID);
		query.setParameter("p_order", parentOrder);
		List<Paragraph> paragraphList = query.list();
		logger.debug(paragraphList.size()+" paragraph with sourceID="+sourceID+" and parentOrder="+parentOrder);
		
		if(paragraphList.size() == 1)
			p = paragraphList.get(0);
		
		logger.debug("retrieved "+ p);
		return p;
	}
	
	/**
	 * Retrieve paragraphs of a source document by source ID.   
	 *
	 * @param  sourceID  	source ID of the source document.
	 * @return				non-empty List<Paragraph> object if exist, 
	 * 						empty List<Paragraph> object if does not exist.
	 */
	public List<Paragraph> getParagraphs(int sourceID){
		logger.info("getParagraphs");
		
		org.hibernate.Query query = session.createQuery("from Paragraph where source_id = :id order by parent_order");
		query.setParameter("id", sourceID);
		List<Paragraph> paragraphList = query.list();
		
		logger.debug("retrieved " + paragraphList.size() + " paragraphs");
		return paragraphList;
	}
	
	/**
	 * Retrieve a entity by entity ID.   
	 *
	 * @param  entityID  	entity ID of the source document.
	 * @return				Entity object if exist, null if does not exist.
	 */
	public Entity getEntity(int entityID) {
		logger.info("getEntity");
		
		Entity e = null;
		
		e = (Entity) session.get(Entity.class, entityID);
		
		logger.debug("retrieved "+ e);
		return e;
	}
	
	/**
	 * Retrieve entities of a source document by source ID.   
	 *
	 * @param  sourceID  	source ID of the source document.
	 * @return				non-empty List<Entity> object if exist, 
	 * 						empty List<Entity> object if does not exist.
	 */
	public List<Entity> getEntities(int sourceID){
		logger.info("getEntities");
		
		org.hibernate.Query query = session.createQuery("from Entity where source_id = :id");
		query.setParameter("id", sourceID);
		List<Entity> entityList = query.list();
		
		logger.debug("retrieved " + entityList.size() + " entities");
		return entityList;
	}
	
	/**
	 * Retrieve a sentence by sentence ID.   
	 *
	 * @param  sentenceID  		sentence ID of the source document.
	 * @return					Sentence object if exist, null if does not exist.
	 */
	public Sentence getSentence(int sentenceID) {
		logger.info("getSentence");
		
		Sentence s = null;
		
		s = (Sentence) session.get(Sentence.class, sentenceID);
		if(s != null && s.getContent().startsWith(DataStore.SENTENCE_PREFIX))
			s.setContent(s.getContent().substring(DataStore.SENTENCE_PREFIX.length()));
		
		
		logger.debug("retrieved "+ s);
		return s;
	}
	
	/**
	 * Retrieve a sentence by paragraph ID and the order of the sentence on the paragraph.   
	 *
	 * @param  paragraphID  	paragraph ID of the paragraph document.
	 * @param  parentOrder  	order of the sentence on the paragraph.
	 * @return					Sentence object if exist, null if does not exist.
	 */
	public Sentence getSentence(int paragraphID, int parentOrder) {
		logger.info("getSentence");
		
		Sentence s = null;
		
		org.hibernate.Query query = session.createQuery("from Sentence where paragraph_id = :id and parent_order = :p_order");
		query.setParameter("id", paragraphID);
		query.setParameter("p_order", parentOrder);
		List<Sentence> sentenceList = query.list();
		logger.debug(sentenceList.size()+" sentence with paragraphID="+paragraphID+" and parentOrder="+parentOrder);
		
		if(sentenceList.size() == 1){
			s = sentenceList.get(0);
			removePrefix(s);
		}
		
		logger.debug("retrieved "+ s);
		return s;
	}
	
	/**
	 * Retrieve sentences of a paragraph by paragraph ID.   
	 *
	 * @param  paragraphID  	paragraph ID of the paragraph.
	 * @return					non-empty List<Sentence> object if exist, 
	 * 							empty List<Sentence> object if does not exist.
	 */
	public List<Sentence> getSentences(int paragraphID){
		logger.info("getSentences");
		
		org.hibernate.Query query = session.createQuery("from Sentence where paragraph_id = :id order by parent_order");
		query.setParameter("id", paragraphID);
		List<Sentence> sentenceList = query.list();
		
		for(Sentence s: sentenceList){
			removePrefix(s);
		}
		
		logger.debug("retrieved " + sentenceList.size() + " sentences");
		return sentenceList;
	}
	
	/**
	 * Retrieve a source information by source name.   
	 *
	 * @param  url  	source name of the source document.
	 * @return				source object if exist, null if does not exist.
	 */
	public Source getSource(String url){
		logger.info("getSource");
		
		Source source = null;
		
		if(url != null){
			org.hibernate.Query query = session.createQuery("from Source where url = :url ");
			query.setParameter("url", url);
			List<Source> sourceList = query.list();
			logger.debug(sourceList.size()+" source with url="+url);
			
			if(sourceList.size() >= 1){		// TODO: sourceList.size() == 1
				source = sourceList.get(0);
			}
			
			logger.debug("retrieved " + source);
		}
		
		return source;
	}
	
	/**
	 * Retrieve a source information by paragraph/entity ID and the class type.   
	 *
	 * @param  classType  	class type of Paragraph/Entity class.
	 * @param  id			paragraph/entity ID.
	 * @return				source object if exist, null if does not exist.
	 */
	public Source getSource(Class classType, int id){
		logger.info("getSource");
		
		Source source = null;
		
		Object obj = session.get(classType, id);
		
		if(obj instanceof Paragraph)
			source = (Source) session.get(Source.class, ((Paragraph)obj).getSourceID());
		else if(obj instanceof Entity)
			source = (Source) session.get(Source.class, ((Entity)obj).getSourceID());
		
		logger.debug("retrieved " + source);
		return source;
	}
	
	/**
	 * Retrieve a source information by source ID.   
	 *
	 * @param  id		  	source ID of the source document.
	 * @return				source object if exist, null if does not exist.
	 */
	public Source getSource(int id){
		logger.info("getSource");
		
		Source source = null;
		
		source = (Source)session.get(Source.class, id);
		
		logger.debug("retrieved " + source);
		return source;
	}
	
	/**
	 * Search a list of relevant suggestions by user input and user preferences.    
	 *
	 * @param  userInput  	search string input from the user.
	 * @param  prefs		user preferences of ACP.
	 * @return				non-empty List<Suggestion> object if result exist and is according to the preferences (Threshold setting), 
	 * 						empty List<Suggestion> object if result does not exist or is not according to the preferences (Threshold setting).
	 */

	public List<Suggestion> searchText(String userInput, Preference prefs, boolean autoTrigger){
		logger.info("searchText");
		
		List<Suggestion> suggestionList = new ArrayList<Suggestion>();
		
		if(!(userInput == null || prefs == null || userInput.trim().equals(""))){
			FullTextSession fullTextSession = Search.getFullTextSession(session);
			
			Analyzer analyzer = fullTextSession.getSearchFactory().getAnalyzer("customanalyzer");
			
			ComplexPhraseQueryParser parser = new ComplexPhraseQueryParser(Version.LUCENE_35, FIELD_TO_SEARCH, analyzer) ;
			BooleanQuery.setMaxClauseCount(Integer.MAX_VALUE);
			parser.setAllowLeadingWildcard(true);
			
			String lastWord = null;

			Query query = null;
			try {	
				userInput = userInput.toLowerCase();
				String[] words = userInput.trim().split(" ");
				lastWord = words[words.length - 1];
				
				StringBuffer q = new StringBuffer();
				if(words.length > 100) // TODO: if too many words, then return empty list. need to change the hardcoding
					return suggestionList;
				
				if(words.length > 1){
					
					for(int i = 0; i < words.length; i++){
						String word = words[i].trim();
						word = processSpecialCharacter(word);
						String fuzziness = calcFuzzy(word);
						String prefix_fuzziness = calcFuzzy(SENTENCE_PREFIX+word);
						word = QueryParser.escape(word);
						word = QueryParser.escape(word);
						
						if(!word.equals("")){
							if(i == 0){
								q.append("\"");
								q.append(SENTENCE_PREFIX+word + prefix_fuzziness +" ");
							}else if(i == words.length-1){
								String temp = q.toString();
								q.append(word+fuzziness);
								q.append("\"");
								
								q.append(OR + temp+word+WILDCARD);
								q.append("\"");
							}else
								q.append(word+fuzziness+" ");
						}
					}
					
				}else{
					String word = words[0].trim();
					word = processSpecialCharacter(word);
					String prefix_fuzziness = calcFuzzy(SENTENCE_PREFIX+word);
					word = QueryParser.escape(word);
					
					q.append(SENTENCE_PREFIX+word+prefix_fuzziness + OR + SENTENCE_PREFIX+word+WILDCARD);
				}
			
				
				// Uncomment to search for the term from any part of the sentence
	//			
	//			q.append(" ");
	//			
	//			if(words.length > 1){
	//				q.append("\"");
	//				for(int i = 0; i < words.length; i++){
	//					if(!words[i].trim().equals("")){
	//						if(i != words.length-1)
	//							q.append(words[i]+FUZZY+" ");
	//						else if(i == words.length-1)
	//							q.append(words[i]+WILDCARD+FUZZY);		
	//					}
	//				}
	//				q.append("\"");
	//			}else{
	//				q.append(words[0]+FUZZY);
	//			}
				
				logger.debug("query: "+q.toString());
				query = parser.parse(q.toString());
				
			} catch (ParseException e) {
				logger.error(e.getMessage());
			}
			
			FullTextQuery hibQuery = fullTextSession.createFullTextQuery(query, CLASS_TO_SEARCH_SENTENCE);
			
			logger.debug("search sentence result size of " + hibQuery.getResultSize());
			
			if(hibQuery.getResultSize() > 0){
				if(autoTrigger){
					if(hibQuery.getResultSize() <= prefs.getThreshold())
						suggestionList = (List<Suggestion>) hibQuery.list();
				}else
					suggestionList = (List<Suggestion>) hibQuery.list();
				
				for(Suggestion s: suggestionList){
					removePrefix((Sentence)s);
				}
			}else{
				if((userInput.toCharArray())[userInput.length()-1] != ' ') // if last character of search entity text is a space
					suggestionList = searchEntity(lastWord, prefs, autoTrigger);
			}
		}
		
		logger.debug("returning suggestion result size of " + suggestionList.size());
		return suggestionList;
	}

	
	/**
	 * Search a list of relevant entities by user input and user preferences.    
	 *
	 * @param  userInput  	search string input from the user.
	 * @param  prefs		user preferences of ACP.
	 * @return				non-empty List<Suggestion> object if result exist and is according to the preferences (Threshold setting), 
	 * 						empty List<Suggestion> object if result does not exist or is not according to the preferences (Threshold setting).
	 */
	public List<Suggestion> searchEntity(String userInput, Preference prefs, boolean autoTrigger){
		logger.info("searchEntity");
		
		List<Suggestion> entityList = new ArrayList<Suggestion>();
		
		if(!(userInput == null || prefs == null || userInput.trim().equals(""))){
			FullTextSession fullTextSession = Search.getFullTextSession(session);
			
			Analyzer analyzer = fullTextSession.getSearchFactory().getAnalyzer("custom_keyword_analyzer");
			
			ComplexPhraseQueryParser parser = new ComplexPhraseQueryParser(Version.LUCENE_35, FIELD_TO_SEARCH, analyzer) ;
			parser.setAllowLeadingWildcard(true);
			BooleanQuery.setMaxClauseCount(Integer.MAX_VALUE);
			
			userInput = userInput.trim();
			// filter start character of the input
			for(int i = 0; i < ENTITY_START_IGNORE.length; i++){
				if(userInput.startsWith(ENTITY_START_IGNORE[i])){
					userInput = userInput.substring(1, userInput.length());
					userInput = userInput.trim();
					break;
				}
			}
			
			
			Query query = null;
			try {	
				userInput = userInput.toLowerCase();
				StringBuffer q = new StringBuffer();
				String escaped = QueryParser.escape(userInput);
				
				q.append(WILDCARD).append(escaped).append(WILDCARD);
				
				logger.debug("query: "+q.toString());
				query = parser.parse(q.toString());
			} catch (ParseException e) {
				logger.error(e.getMessage());
			}
			
			FullTextQuery hibQuery = fullTextSession.createFullTextQuery(query, CLASS_TO_SEARCH_ENTITY);
			
			logger.debug("search entity result size of " + hibQuery.getResultSize());
			
			if(autoTrigger){
				if(hibQuery.getResultSize() <= prefs.getThreshold())
					entityList = (List<Suggestion>) hibQuery.list();
			}else{
				entityList = (List<Suggestion>) hibQuery.list();
			}
		}
		
		logger.debug("returning entity result size of " + entityList.size());
		return entityList;
	}
	
	/**
	 * Insert an entity object   
	 *
	 * @param  entity  	entity object.
	 * @return			entity ID if store succeed, -1 if store fail.
	 */
	public int storeEntity(Entity entity){
		logger.info("storeEntity");
		
		int id = -1;
		
		if(entity != null){
			try{		
				id = (Integer) session.save(entity);		
				
				session.flush();
				session.clear();
			}catch(HibernateException he){
				if(tx != null)
					tx.rollback();
				
				logger.error(he.getMessage());
			}
	
			logger.debug("store entity with id "+id);
		}
		
		return id;
	}

	/**
	 * Insert a source object   
	 *
	 * @param  metadata  	source object.
	 * @return				source ID if store succeed, -1 if store fail.
	 */
	public int storeMetadata(Source metadata){
		logger.info("storeMetadata");

		int id = -1;
		
		if(metadata != null){
			try{			
				id = (Integer) session.save(metadata);			
				
				session.flush();
				session.clear();
			}catch(HibernateException he){
				if(tx != null)
					tx.rollback();
				
				logger.error(he.getMessage());
			}
	
			logger.debug("store source with id "+id);	
		}
		
		return id;
	}

	/**
	 * Insert a paragraph object   
	 *
	 * @param  paragraph  	paragraph object.
	 * @return				paragraph ID if store succeed, -1 if store fail.
	 */
	public int storeParagraph(Paragraph paragraph){
		logger.info("storeParagraph");
		
		int id = -1;
		
		if(paragraph != null){
			try{
				id = (Integer) session.save(paragraph);			
				
				session.flush();
				session.clear();
			}catch(HibernateException he){
				if(tx != null)
					tx.rollback();
				
				logger.error(he.getMessage());
			}
	
			logger.debug("store paragraph with id "+id);
		}
		
		return id;
	}

	/**
	 * Insert a sentence object   
	 *
	 * @param  sentence  	sentence object.
	 * @return				sentence ID if store succeed, -1 if store fail.
	 */
	public int storeSentence(Sentence sentence){
		logger.info("storeSentence");
		
		int id = -1;
		
		if(sentence != null){
			try{				
				sentence.setContent(DataStore.SENTENCE_PREFIX+sentence.getContent());
				id = (Integer) session.save(sentence);
				
				session.flush();
				session.clear();
			}catch(HibernateException he){
				if(tx != null)
					tx.rollback();
				
				logger.error(he.getMessage());
			}
			
			logger.debug("store sentence with id "+id);	
		}
		
		return id;
	}

	/**
	 * Start a session with Hibernate. Allows retrieval of data but not manipulation of data.
	 */
	public void startSession(){
		
		if(session == null || !session.isOpen()){
			logger.info("startSession");
			session = SessionFactoryUtil.getFactory().openSession();
		}
			
	}
	
	/**
	 * Get the current session.
	 * 
	 * @return		the current session.
	 */
	public Session getSession() {
		logger.info("getSession");
		return session;
	}
	
	/**
	 * Close the session with Hibernate.
	 */
	public void closeSession(){
		if(tx == null || !session.getTransaction().isActive()){
			logger.info("closeSession");
			session.close();
			session = null;
		}
	}
	
	/**
	 * Start a transaction with Hibernate. Allows retrieval of data and manipulation of data.
	 */
	public void startTransaction(){
		logger.info("startTransaction");
		
		try{
			tx = session.beginTransaction();
		}catch(HibernateException he){
			if(tx != null)
				tx.rollback();
			
			logger.error(he.getMessage());
		}
	}
	
	/**
	 * Commit changes through Hibernate. All changes made will be updated.
	 */
	public void commitTransaction(){
		logger.info("commitTransaction");
		
		try{
			tx.commit();
			tx = null;
		}catch(HibernateException he){
			if(tx != null)
				tx.rollback();

			
			he.printStackTrace();
			logger.error(he.getMessage());
		}
	}
	
	/**
	 * Rollback a transaction with Hibernate. All changes made will be undo.
	 */
	public void rollbackTransaction(){
		logger.info("rollbackTransaction");
		if(tx != null)
			tx.rollback();
	}
	
	/**
	 * Remove prefix appended on each sentence that aid in searching.   
	 *
	 * @param  s  	Sentence object.
	 * @return		Sentence object with prefix removed.
	 */
	private Sentence removePrefix(Sentence s){
		if(s != null && s.getContent().startsWith(DataStore.SENTENCE_PREFIX))
			s.setContent(s.getContent().substring(DataStore.SENTENCE_PREFIX.length())); 
	
		return s;
	}
	
	/**
	 * Calculate fuzziness value of a string using the length of the string.   
	 *
	 * @param  word  	String object.
	 * @return			Fuzzy value of the the string.
	 */
	private String calcFuzzy(String word){
		double wordLength = word.length();
		
		if(word.startsWith(SENTENCE_PREFIX) && wordLength == SENTENCE_PREFIX.length()+1) // off fuzziness for one letter word for prefix
			return "";
		if(wordLength == 1) // off fuzziness for one letter word
			return ""; 
		else if(FUZZY_ALLOWANCE > wordLength)
			return "~0.0";
		else
			return "~"+(wordLength-FUZZY_ALLOWANCE)/wordLength;
	}
	
	/**
	 * Process special character in a string.   
	 *
	 * @param  str  	String object.
	 * @return			Processed String object.
	 */
	private String processSpecialCharacter(String str){
		str = str.replaceAll(unicodeToUTF8(UNICODE_LEFT_DOUBLE_QUOTE), "\"");
		str = str.replaceAll(unicodeToUTF8(UNICODE_RIGHT_DOUBLE_QUOTE), "\"");
		
		return str;
	}
	
	/**
	 * Converts unicode string to the default charset  
	 *
	 * @param  str  	String object.
	 * @return			Converted String object.
	 */
	private String unicodeToUTF8(String str){
		return new String(str.getBytes(Charset.defaultCharset()), Charset.defaultCharset());
	}
	
}
