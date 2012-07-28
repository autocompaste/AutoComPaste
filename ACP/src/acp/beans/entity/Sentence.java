package acp.beans.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.apache.solr.analysis.LowerCaseFilterFactory;
import org.apache.solr.analysis.WhitespaceTokenizerFactory;
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
import acp.store.DataStore;


@Entity
@Indexed
@AnalyzerDef(name="customanalyzer",
tokenizer = @TokenizerDef(factory = WhitespaceTokenizerFactory.class),
filters = {
	@TokenFilterDef(factory = LowerCaseFilterFactory.class),
})
@Analyzer (definition = "customanalyzer")
@Table(name="sentence")
/**
 * Sentence class is utilize as a mapping to database table for Hibernate using EJB 3.0 annotation standard. It is 
 * also used as a java bean to pass around the data in ACP.
 * 
 * @author Teo Kee Cheng
 */
public class Sentence extends Suggestion{
	
	@Column(name="paragraph_id", nullable=true)
	private int paragraphID;
	
	@Column(name="parent_order", nullable=true)
	private int parentOrder;
	
	@Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
	@Column(name="content", nullable=true)
	private String content;
	
	/**
	 * Default constructor
	 */
	public Sentence(){}
	
	/**
	 * Constructor to initialize object for storing in database
	 */
	public Sentence(int parentOrder,
			String content) {
		super();
		this.parentOrder = parentOrder;
		this.content = content;
	}
	
	/**
	 * Get paragraph ID of the Sentence object.   
	 *
	 * @return			paragraph ID of the object.
	 */
	public int getParagraphID() {
		return paragraphID;
	}
	
	/**
	 * Set paragraph ID of the Sentence object.   
	 *
	 * @param paragraphID			paragraph ID of the object.
	 */
	public void setParagraphID(int paragraphID) {
		this.paragraphID = paragraphID;
	}
	
	/**
	 * Get parent order of the Sentence object.   
	 *
	 * @return			parent order of the object.
	 */
	public int getParentOrder() {
		return parentOrder;
	}
	
	/**
	 * Set parent order of the Sentence object.   
	 *
	 * @param parentOrder			parent order of the object.
	 */
	public void setParentOrder(int parentOrder) {
		this.parentOrder = parentOrder;
	}
	
	/**
	 * Get content of the Sentence object.   
	 *
	 * @return			content of the object.
	 */
	public String getContent() {
		return content;
	}
	
	/**
	 * Set content of the Sentence object.   
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
		return "Sentence [id="+ id + ", paragraphID=" + paragraphID + ", parentOrder="
				+ parentOrder + "]";
	}
}
