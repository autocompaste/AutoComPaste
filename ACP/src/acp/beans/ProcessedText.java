package acp.beans;

import java.util.List;

import acp.beans.entity.Entity;
import acp.beans.entity.Paragraph;
import acp.beans.entity.Source;

/**
 * ProcessedText class is use for communicating text that are processed. When raw text are processed,
 * it is stored in a ProcessedText object and passed to DataManager to process further and store it. 
 * 
 * @author Teo Kee Cheng, Amulya Khare
 */
public class ProcessedText {
	private Source metadata;
	private List<Paragraph> paragraphs;
	private List<Entity> entities;
	
	/**
	 * Default Constructor
	 */
	public ProcessedText(){}
	
	/**
	 * Constructor to initialize object for processing of text
	 */
	public ProcessedText(Source metadata, List<Paragraph> paragraphs,
			List<Entity> entities) {
		super();
		this.metadata = metadata;
		this.paragraphs = paragraphs;
		this.entities = entities;
	}
	
	/**
	 * Get source object of the source document.   
	 *
	 * @return			source object.
	 */
	public Source getMetadata() {
		return metadata;
	}
	public void setMetadata(Source metadata) {
		this.metadata = metadata;
	}
	
	/**
	 * Get all the Paragraph object of the source document that are processed.   
	 *
	 * @return			List<Paragraph> object.
	 */
	public List<Paragraph> getParagraphs() {
		return paragraphs;
	}
	
	/**
	 * Set the list of processed Paragraph object.   
	 *
	 * @param paragraphs			List<Paragraph> object.
	 */
	public void setParagraphs(List<Paragraph> paragraphs) {
		this.paragraphs = paragraphs;
	}
	
	/**
	 * Get all the Entity object of the source document that are processed.   
	 *
	 * @return			List<Entity> object.
	 */
	public List<Entity> getEntities() {
		return entities;
	}
	
	/**
	 * Set the list of processed Entity object.   
	 *
	 * @param entities			List<Entity> object.
	 */
	public void setEntities(List<Entity> entities) {
		this.entities = entities;
	}
}
