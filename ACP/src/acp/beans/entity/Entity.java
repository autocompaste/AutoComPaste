package acp.beans.entity;

import javax.persistence.Column;
import javax.persistence.Table;

import org.apache.solr.analysis.KeywordTokenizerFactory;
import org.apache.solr.analysis.LowerCaseFilterFactory;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.AnalyzerDef;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Store;
import org.hibernate.search.annotations.TokenFilterDef;
import org.hibernate.search.annotations.TokenizerDef;

import acp.beans.Suggestion;

@javax.persistence.Entity
@Indexed
@AnalyzerDef(name="custom_keyword_analyzer",
tokenizer = @TokenizerDef(factory = KeywordTokenizerFactory.class),
filters = {
	@TokenFilterDef(factory = LowerCaseFilterFactory.class),
})
@Analyzer (definition = "custom_keyword_analyzer")
@Table(name="Entity")
/**
 * Entity class is utilize as a mapping to database table for Hibernate using EJB 3.0 annotation standard. It is 
 * also used as a java bean to pass around the data in ACP.
 * 
 * @author Teo Kee Cheng
 */
public class Entity extends Suggestion{
	
	public static final int EMAIL = 201;
	public static final int ADDRESS = 202;
	public static final int PLACE = 203;
	public static final int URL = 204;
	public static final int NAME = 205;
	
	
	@Column(name="source_id", nullable=true)
	private int sourceID;
	
	@Column(name="type", nullable=true)
	private int entityType;
	
	@Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
	@Column(name="content", nullable=true)
	private String content;
	
	/**
	 * Default constructor
	 */
	public Entity(){}
	
	/**
	 * Constructor to initialize object for storing in database
	 */
	public Entity(int type, String content) {
		super();
		this.entityType = type;
		this.content = content;
	}
	
	/**
	 * Get source ID of the Entity object.   
	 *
	 * @return			source ID of the object.
	 */
	public int getSourceID() {
		return sourceID;
	}

	/**
	 * Set source ID of the Entity object.   
	 *
	 * @param sourceID			source ID of the object.
	 */
	public void setSourceID(int sourceID) {
		this.sourceID = sourceID;
	}

	/**
	 * Get entity type of the Entity object.   
	 *
	 * @return			entity type of the object.
	 */
	public int getEntityType() {
		return entityType;
	}
	
	/**
	 * Set entity type of the Entity object.   
	 *
	 * @param entityType			entity type of the object.
	 */
	public void setEntityType(int entityType) {
		this.entityType = entityType;
	}
	
	/**
	 * Get content of the Entity object.   
	 *
	 * @return			content of the object.
	 */
	public String getContent() {
		return content;
	}
	
	/**
	 * Set content of the Entity object.   
	 *
	 * @param content			content of the object.
	 */
	public void setContent(String content) {
		this.content = content;
	}
	
	@Override
	/**
	 * Get the formatted object information through a string.   
	 *
	 * @return			string format of the object.
	 */
	public String toString() {
		return "Entity [id=" + id + ", sourceID=" + sourceID + ", entityType=" + entityType + "]";
	}
	
	@Override
	/**
	 * Get the result if two object is equal through custom comparison.   
	 *
	 * @return			boolean result of whether the two object are equal.
	 */
	public boolean equals(Object o)  
	{  
		return (this.content.trim().equals(((Entity)o).content.trim()));    
	}  
	
	@Override
	/**
	 * Get custom hash code of the object.   
	 *
	 * @return			hash code of the object.
	 */
	public int hashCode()  
	{  
		return content.trim().toLowerCase().hashCode();  
	} 
}
