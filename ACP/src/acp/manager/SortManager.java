package acp.manager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import acp.beans.RankEntry;
import acp.beans.Preference;
import acp.beans.Suggestion;
import acp.beans.entity.Entity;
import acp.beans.entity.Sentence;

/**
 * SortManager is the component that sort the result list according the the ranking model provided by the machine learning component.
 * SortManager sorts both sentences and entity. 
 * 
 * @author Ng Chin Hui
 */
public class SortManager {
	
	final static Logger logger = LoggerFactory.getLogger(SortManager.class);
	
	public SortManager(){}
	
	/**
	 * Sorts the search result according by the type of the search result (Sentence or Entity). 
	 * 
	 * @param entityTypePriority		entity that has the highest priority
	 * @param preference 			sorting preference in which the user specify
	 * @param suggestions			list of search results
	 * @param rankingModel			ranking model produce by machine learning component based on user behavior
	 */
	public List<Suggestion> sortSearchResults(int entityTypePriority, Preference[] preference, List<Suggestion> suggestions, ArrayList<RankEntry> rankingModel, String userInput){
		
		/* set the rank of each suggestion according to its rank from the ranking model */
		for(Suggestion s : suggestions){
			int rank = getIndexOfSource(s.getSource().getName(), rankingModel);
			if(rank == -1)
				rank = 1000000;
			s.getSource().setRank(rank);
		}
		
		if(entityTypePriority == -1){
			List<Suggestion> suggList = sortSentence(preference, suggestions, rankingModel);
			
			List<Suggestion> fuzzySuggList = new ArrayList<Suggestion>();
			List<Suggestion> prioritisePrefixSuggList = new ArrayList<Suggestion>();
			
			for(int i = 0; i < suggList.size(); i++){
				Suggestion sugg = suggList.get(i);
				String content = sugg.getContent();
				
				String lowerCaseUserInput = userInput.toLowerCase();
				
				if(content.toLowerCase().startsWith(lowerCaseUserInput))
					prioritisePrefixSuggList.add(sugg);
				else
					fuzzySuggList.add(sugg);
			}
			
			prioritisePrefixSuggList.addAll(fuzzySuggList);
			
			return prioritisePrefixSuggList;
		}else{
			return sortEntities(entityTypePriority, preference, suggestions, rankingModel);
		}
	}
	
	/**
	 * Sorts the result list of sentence type. It will first sort the results by source document rank. 
	 * For sentences from the same source document, it is ranked according the appearance in the source document.
	 * After which, the search result is then sorted according to the last pasted sentence position. Sentences that appear before the last pasted sentence are ranked lower.
	 * 
	 * @param preference 			sorting preference in which the user specify
	 * @param suggestions			list of search results
	 * @param rankingModel			ranking model produce by machine learning component based on user behavior
	 */
	private List<Suggestion> sortSentence(Preference[] preference, List<Suggestion> suggestions, ArrayList<RankEntry> rankingModel){
		logger.info("sortSearchResults - Sentence");
		
		/* sort according to source ranking, paragraph order, sentence order */
		Collections.sort(suggestions, new SuggestionSentenceComparator());
		
		/* sort the suggestions according to previously pasted suggestion position in document */
		if(rankingModel.size()!=0 && suggestions.size()!=0){
			List<Suggestion> tempSuggestions = new ArrayList<Suggestion>();
			int startIndex = 0;
			int endIndex = 0;
			int cutOffIndex = -1;
			Suggestion s = null;
			
			for(RankEntry e : rankingModel){
				if(endIndex < suggestions.size())
					s = suggestions.get(startIndex);
				
				int lastUsedParagraph = e.getLastUsedParahraph();
				int lastUsedSentence = e.getLastUsedSentence();
				
				if(lastUsedParagraph > -1 && lastUsedSentence > -1){
					while(endIndex < suggestions.size() && e.getSource().equalsIgnoreCase(s.getSource().getName())){
						if(cutOffIndex == -1){
							if(lastUsedParagraph == ((Sentence)s).getParagraphID()){	// in same paragraph
								if(lastUsedSentence < ((Sentence)s).getParentOrder()){	// must be greater
									cutOffIndex = suggestions.indexOf(s);
								}
							}else if(lastUsedParagraph < ((Sentence)s).getParagraphID()){	// confirmed to be greater
								cutOffIndex = suggestions.indexOf(s);
							}
						}
						
						endIndex++;
						if(endIndex < suggestions.size())
							s = suggestions.get(endIndex);
					}
					if(cutOffIndex != -1){
						for(int i=cutOffIndex; i<endIndex; i++)
							tempSuggestions.add(suggestions.get(i));
						for(int i=startIndex; i<cutOffIndex; i++)
							tempSuggestions.add(suggestions.get(i));
						cutOffIndex = -1;
					}else{
						for(int i=startIndex; i<endIndex; i++)
							tempSuggestions.add(suggestions.get(i));
					}
				}else{
					while(endIndex < suggestions.size() && e.getSource().equalsIgnoreCase(s.getSource().getName())){
						tempSuggestions.add(s);
						endIndex++;
						if(endIndex < suggestions.size())
							s = suggestions.get(endIndex);
					}
				}
				startIndex = endIndex;
			}
			suggestions = tempSuggestions;
		}
		return suggestions;
	}
	
	/**
	 * Sorts the result list of entity type. It will first sort the entities by source document rank. 
	 * Then after, according to the specified type priority,  entities of a given type will be given higher ranking.
	 * If there is no specific type priority, the result list is sorted by source document rank.
	 * 
	 * @param entityTypePriority		entity that has the highest priority
	 * @param preference 			sorting preference in which the user specify
	 * @param suggestions			list of search results
	 * @param rankingModel			ranking model produce by machine learning component based on user behavior
	 */
	private List<Suggestion> sortEntities(int entityTypePriority, Preference[] preference, List<Suggestion> suggestions, ArrayList<RankEntry> rankingModel){
		logger.info("sortSearchResults - Entity : " + entityTypePriority);
		
		/* sort according to source ranking */
		Collections.sort(suggestions, new SuggestionEntityComparator());
		
		/* sort according to the priority given to a specified type */
		List<Suggestion> tempList = new ArrayList<Suggestion>();
		if(entityTypePriority > 0){
			for(Suggestion suggestion : suggestions){
				if(((Entity)suggestion).getEntityType() == entityTypePriority){
					tempList.add(suggestion);
				}
			}
			suggestions.removeAll(tempList);
			tempList.addAll(suggestions);
			suggestions = tempList;
		}
		
		return suggestions;
	}
	
	/**
	 * Find the index of the source document in the ranking model.
	 * 
	 * @param sourceName 		the name of the source document
	 * @param rankingModel		the ranking model produced by machine learning component based on user behavior
	 * @return the index of source document in the ranking model
	 */
	private int getIndexOfSource(String sourceName, ArrayList<RankEntry> rankingModel){
		int index = -1;
		for(int i=0; i<rankingModel.size(); i++){
			RankEntry e = rankingModel.get(i);
			if(e.getSource().equalsIgnoreCase(sourceName))
				return i;
		}	
		return index;
	}
}

/**
 * A comparator class used by Collections class for the Sort method for Sentences.
 * Compare source rank followed by paragraph order and lastly sentence order.
 * 
 * @author Ng Chin Hui
 */
class SuggestionSentenceComparator implements Comparator<Suggestion>{ 

	@Override
	public int compare(Suggestion arg0, Suggestion arg1) {
			
		int result;
		if(Integer.compare(arg0.getSource().getRank(), arg1.getSource().getRank()) == 0){
			if(Integer.compare(((Sentence)arg0).getParagraphID(), ((Sentence)arg1).getParagraphID())==0){
				result = Integer.compare(((Sentence)arg0).getParentOrder(), ((Sentence)arg1).getParentOrder());
			}else{
				result = Integer.compare(((Sentence)arg0).getParagraphID(), ((Sentence)arg1).getParagraphID());
			}			
		}else
			result = Double.compare(arg0.getSource().getRank(), arg1.getSource().getRank());		
		return result;		
	}
}

/**
 * A comparator class used by Collections class for the Sort method for Entity.
 * Compare by source ranks only.
 * 
 * @author Ng Chin Hui
 */
class SuggestionEntityComparator implements Comparator<Suggestion>{ 

	@Override
	public int compare(Suggestion arg0, Suggestion arg1) {	
		return Double.compare(arg0.getSource().getRank(), arg1.getSource().getRank());		
	}
}
