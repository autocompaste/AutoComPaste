
package acp.manager;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import acp.beans.Preference;
import acp.beans.ProcessedText;
import acp.beans.Suggestion;
import acp.beans.entity.Entity;
import acp.beans.entity.Paragraph;
import acp.beans.entity.Sentence;
import acp.beans.entity.Source;
import acp.store.DataStore;
import acp.util.SessionFactoryUtil;

/**
 * DataManager is utilize as the business logic component that package and process the information before passing
 * back to ACPLogic. It uses SessionFactoryUtil to initialize Hibernate and uses DataStore to communicate with 
 * the database.
 * 
 * @author Teo Kee Cheng
 *
 */
public class DataManager {
	
	final static Logger logger = LoggerFactory.getLogger(DataManager.class);
	
	//private DataStore ds;
	private static AtomicInteger mutex = new AtomicInteger(1);
	
	/**
	 * Initialise Hibernate library.
	 */
	public void initialiseHibernate() throws Throwable{
		SessionFactoryUtil.initialise();
	}
	
	/**
	 * Remove each sentence, paragraph, entity and the source information by the name of the source.   
	 *
	 * @param  url  	source name of the source document.
	 */
	public void removeData(String url){		
		logger.info("removeData");
		
		DataStore ds = new DataStore();
		
		lock();
		
		if(url != null){
			ds.startSession();
			
			int sourceID = -1;
			Source s = ds.getSource(url);
			if(s != null)
				sourceID = s.getSourceID();
			
			if(sourceID >= 0){
				ds.startTransaction();
				
				List<Paragraph> paragraphList = ds.getParagraphs(sourceID);
				
				for(int i = 0; i < paragraphList.size(); i++){
					Paragraph p = paragraphList.get(i);
					
					List<Sentence> sentenceList = ds.getSentences(p.getId());
					
					ds.removeSentencesAndIndexes(sentenceList);
				}
				ds.removeParagraphs(sourceID);
				
				List<Entity> entities = (List<Entity>) ds.getEntities(sourceID);
				ds.removeEntitiesAndIndexes(entities);
				
				ds.removeSource(sourceID);
				ds.commitTransaction();
			}
			
			ds.closeSession();
		}
		
		unlock();
	}
	
	/**
	 * Remove each sentence, paragraph, and the source information of all the sources in the database.   
	 *
	 */
	public void removeAllData(){		
		logger.info("removeAllData");
		
		DataStore ds = new DataStore();
		
		lock();

		ds.startSession();
		ds.startTransaction();
		ds.removeAllDataAndIndexes();
		ds.commitTransaction();
		ds.closeSession();
		
		unlock();
	}
	
	/**
	 * Retrieve a source information by source url.   
	 *
	 * @param  url  	source url.
	 * @return			source object if exist, null if does not exist.
	 * @see				Suggestion
	 */
	public Source retrieveSource(String url){
		logger.info("retrieveSource");
		
		DataStore ds = new DataStore();
		
		Source source = null;
		
		lock();
		
		ds.startSession();
		source = ds.getSource(url);
		ds.closeSession();
		
		unlock();
		
		return source;
	}
	
	/**
	 * Retrieve a source information by sentence/entity/paragraph ID.   
	 *
	 * @param  id  		sentence/entity/paragraph ID.
	 * @param  type  	type of Suggestion with regard to the ID. (Refer to Suggestion class constant variable)
	 * @return			source object if exist, null if does not exist.
	 * @see				Suggestion
	 */
	public Source retrieveSource(int id, int type){
		logger.info("retrieveSource");
		
		DataStore ds = new DataStore();
		
		Source source = null;
		
		lock();
		
		ds.startSession();
		switch(type){
			case Suggestion.PARAGRAPH:
				source = ds.getSource(Paragraph.class, id);
				break;
			case Suggestion.SENTENCE:
				Sentence s = ds.getSentence(id);
				if(s != null)
					source = ds.getSource(Paragraph.class, s.getParagraphID());
				break;
			case Suggestion.ENTITY:
				source = ds.getSource(Entity.class, id);
				break;
		}
		ds.closeSession();
		
		unlock();
		
		return source;
	}
	
	/**
	 * Retrieve subsequent suggestion by the ID of previous Sentence object and the requested
	 * type of subsequent suggestion.   
	 *
	 * @param  id  		ID of previous sentence.
	 * @param  type  	type of subsequent suggestion. (Refer to Suggestion class constant variable)
	 * @return			Suggestion object if subsequent suggestion exist, null if subsequent suggestion does not exist. 
	 * @see				Suggestion
	 */
	// For type, please use constant variable from Suggestion class. If return null, means there is no next suggestion
	public Suggestion retrieveSubsequentText(int id, int type){
		logger.info("retrieveSubsequentText");
		
		DataStore ds = new DataStore();
		
		Suggestion subsequentSuggestion = null;
		
		Sentence s = null;
		Paragraph currParagraph = null;
		Paragraph nextParagraph = null;
		ArrayList<Sentence> sentences = null;
		
		lock();
		
		ds.startSession();
		switch(type){
			case Suggestion.PARAGRAPH:
				s = ds.getSentence(id);
				
				if(s != null){
					currParagraph = ds.getParagraph(s.getParagraphID());
					
					if(currParagraph != null){
						List<Sentence> currParaSentences = ds.getSentences(currParagraph.getId());
						if(s.getId() != currParaSentences.get(currParaSentences.size()-1).getId()){
							currParagraph.setSentences(currParaSentences);
							subsequentSuggestion = currParagraph; // Return subsequent as current paragraph if not the last sentence
							subsequentSuggestion.setType(Suggestion.PARAGRAPH);
						}else{
							nextParagraph = ds.getParagraph(currParagraph.getSourceID(), currParagraph.getParentOrder()+1);
							if(nextParagraph != null){
								nextParagraph.setSentences(ds.getSentences(nextParagraph.getId()));
								subsequentSuggestion = nextParagraph;
								subsequentSuggestion.setType(Suggestion.PARAGRAPH);
							}
						}						
					}
				}
				break;
			case Suggestion.SENTENCE:
				s = ds.getSentence(id);
				if(s != null){
					Sentence nextSentence = ds.getSentence(s.getParagraphID(), s.getParentOrder()+1);
					if(nextSentence != null){
						subsequentSuggestion = nextSentence; // set subsequent as the next sentence in the same paragraph 
						subsequentSuggestion.setType(Suggestion.SENTENCE);
					}else{ // if nextSentence == null
						currParagraph = ds.getParagraph(s.getParagraphID());
						
						if(currParagraph != null){
							nextParagraph = ds.getParagraph(currParagraph.getSourceID(), currParagraph.getParentOrder()+1);
							
							if(nextParagraph != null){
								sentences = (ArrayList<Sentence>) ds.getSentences(nextParagraph.getId());
								subsequentSuggestion = sentences.get(0); // set subsequent as the first sentence of next paragraph
								subsequentSuggestion.setType(Suggestion.SENTENCE);
							}
						}
					}
				}
				
				break;
			default:
				logger.error("entered switch statement default case with type ="+ type);
				break;
		}
		ds.closeSession();
		
		unlock();
		
		return subsequentSuggestion;
	}
	
	/**
	 * Search for relevant suggestion by input from the user and the user preferences. It will search on each
	 * sentence.   
	 *
	 * @param  userInput  	search string input from the user.
	 * @param  prefs		user preferences of ACP.
	 * @param  autoTrigger	indication of the suggestion trigger mode.
	 * @return				non-empty List<Suggestion> object if result exist and is according to the preferences (Threshold setting), 
	 * 						empty List<Suggestion> object if result does not exist or is not according to the preferences (Threshold setting).
	 * @see					Suggestion
	 */
	public List<Suggestion> searchBasedOnRequest(String userInput, Preference prefs, boolean autoTrigger){
		logger.info("searchBasedOnRequest");
		
		DataStore ds = new DataStore();
		
		List<Suggestion> suggestionList = new ArrayList<Suggestion>();
		
		lock();
		
		ds.startSession();
		List<Suggestion> suggListWithoutSource = ds.searchText(userInput, prefs, autoTrigger);
		
		if(suggListWithoutSource != null){
			for(int i = 0; i < suggListWithoutSource.size(); i++){
				Suggestion s = suggListWithoutSource.get(i);
				
				Source source = null;
				if(s instanceof Sentence){
					source = ds.getSource(Paragraph.class, ((Sentence)s).getParagraphID());
					s.setType(Suggestion.SENTENCE);
				}else{ // if(s instanceof Entity)
					source = ds.getSource(Entity.class, ((Entity)s).getId());
					s.setType(Suggestion.ENTITY);
				}
				
				s.setSource(source);
				suggestionList.add(s);
			}
		}
		ds.closeSession();
		
		unlock();
		
		return suggestionList;
	}
	
	/**
	 * Store processed information from an extracted source (Source, Paragraph, Sentence, Entities). 
	 * For each Source, Paragraph, Sentence, and Entity object, please initialise according to the
	 * only constructor provided with variables. 
	 *
	 * @param  processedText  	processed information from an extracted source. 
	 * @return					source ID if store succeed, -1 if store fail.
	 * @see						ProcessedText
	 */
	public synchronized int storeProcessedText(ProcessedText processedText){
		logger.info("storeProcessedText");
		
		DataStore ds = new DataStore();
		
		int sourceID = -1;
		boolean error = false;
		
		lock();
		
		if(processedText != null){ 
			if(!(processedText.getParagraphs() == null && processedText.getEntities() == null)){
				ds.startSession();
				ds.startTransaction();
				
				Source metadata = processedText.getMetadata();
				List<Paragraph> paragraphs = processedText.getParagraphs();
				List<Entity> entities = processedText.getEntities();
				
				sourceID = ds.storeMetadata(metadata);
				
				if(sourceID >= 0){			
					if(paragraphs != null){
						for(int i = 0; i < paragraphs.size(); i++){
							Paragraph p = paragraphs.get(i);
							p.setSourceID(sourceID);
							
							int paragraphID = ds.storeParagraph(p);
							
							if(paragraphID >= 0){
								List<Sentence> sentenceList = p.getSentences();
								
								if(sentenceList != null){
									for(int j = 0; j < sentenceList.size(); j++){
										Sentence s = sentenceList.get(j);
										s.setParagraphID(paragraphID);
										
										int sentenceID = ds.storeSentence(s);
										if(sentenceID < 0)
											error = true;
									}
								}
							}else // if(paragraphID < 0){		
								error = true; 
						}
					}
					
					if(entities != null){
						entities = removeDuplicateEntity(entities);
						
						for(int i = 0; i < entities.size(); i++){
							Entity e = entities.get(i);
							e.setSourceID(sourceID);
							
							int entityID = ds.storeEntity(e);
							if(entityID < 0)
								error = true;
						}
					}
				}else // if(sourceID < 0){		
					error = true; 
				
				
				if(error){
					ds.rollbackTransaction();
					sourceID = -1;
				}else
					ds.commitTransaction();
				
				ds.closeSession();
			}
		}
		
		unlock();
		
		return sourceID;
	}
	
	private List<Entity> removeDuplicateEntity(List<Entity> entities){
		HashSet<Entity> hs = new HashSet<Entity>();
		hs.addAll(entities);
		entities.clear();
		entities.addAll(hs);
		
		return entities;
	}

	private void lock(){
		while(mutex.getAndSet(0) != 1){
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				logger.error(e.getMessage());
			}
		}
	}
	
	private void unlock(){
		mutex.set(1);
	} 
}
