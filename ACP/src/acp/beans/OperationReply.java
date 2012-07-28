package acp.beans;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represent operation/method invoking result to be sent back to the network 
 * clients (eg. Mozilla Firefox Plugin, Microsoft Add-in) when an operation request is made. 
 * The result type is set to Object so that any method return type can be stored into it.
 * The status indicate the type of reply: REPLY or ERROR_REPLY.
 * 
 * @author Loke Yan Hao
 */
public class OperationReply {
	
	/**
	 * The following are a list of available type of reply.
	 * 
	 * REPLY: Normal reply.
	 * ERROR_REPLY: Error when processing the request. The result argument contains the error message.
	 */
	public final static int REPLY = 0;
	public final static int ERROR_REPLY = 1;
	
	private int status;
	private Object reply;	
	
	/**
	 * Basic constructor to create an instance of OperationReply object.
	 */
	public OperationReply(){
	}
	
	/**
	 * Basic constructor to create an instance of OperationReply object.
	 * 
	 * @param status	the type of reply to send. (REPLY or ERROR_REPLY).
	 * @param result	the return result after invoking a method.
	 */
	public OperationReply(int status, Object result) {
		super();
		this.status = status;
		this.reply = result;
	}
	
	/**
	 * Return the status of the operation reply. 
	 * <p>
	 * The status can be either one of the value: REPLY (0) or ERROR_REPLY (1).
	 * When the status is on ERROR_REPLY (1), the result variable of this object
	 * will contain the error message to describe the error encounter when
	 * invoking a method.
	 * 
	 * @return			the type of the operation reply.
	 */
	public int getStatus() {
		return status;
	}

	/**
	 * Set the the status of the operation reply.
	 * <p>
	 * Use the constant variable in this class to set it to the appropriate types:
	 * REPLY or ERROR_REPLY. 
	 * 
	 * @param status	the type of the operation reply.
	 */
	public void setStatus(int status) {
		this.status = status;
	}
	
	/**
	 * Return the result to be sent to the client.
	 * 
	 * @return			the return result after invoking a method.
	 */
	public Object getResult() {
		return reply;
	}

	/**
	 * Set the return result to be sent to the client after invoking a method.
	 * 
	 * @param result	the return result after invoking a method.
	 */
	public void setResult(Object result) {
		this.reply = result;
	}
	
	/**
	 * Set the operation reply to return an error reply with an error message.
	 * 
	 * @param message	the error message.
	 */
	public void setErrorReply(String message){
		setStatus(OperationReply.ERROR_REPLY);
		setResult(message);
	}
}
