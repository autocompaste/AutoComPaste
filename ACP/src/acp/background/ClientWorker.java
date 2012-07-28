package acp.background;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import acp.ACPLogic;
import acp.beans.Operation;
import acp.beans.OperationReply;
import acp.beans.Suggestion;
import acp.beans.entity.Sentence;
import acp.beans.entity.Source;
import acp.beans.entity.Paragraph;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * Validate and Process the network request send by the client. This class
 * act as a interface between Plugins and ACPLogic; allowing Plugin to
 * call the methods in ACPLogic remotely. 
 *   
 * @author Loke Yan Hao
 */
public class ClientWorker implements Runnable {	
	private Socket client;
	final static Logger logger = LoggerFactory.getLogger(ClientWorker.class);
	
	/**
	 * Basic constructor to create an instance of ClientWorker.
	 * 
	 * @param client	the client connection established in Server class
	 * @see Server#listenConnection()
	 */
	public ClientWorker(Socket client){
		this.client = client;
	}
	
	/**
	 * Read the input from the established connection, and process the request accordingly.
	 * This method will call processInput() private method to do the actual processing. 
	 */
	@Override
	public void run() {
		BufferedReader in = null;
		PrintWriter out = null;
		
		try{
			in = new BufferedReader(new InputStreamReader(client.getInputStream()));
			out = new PrintWriter(client.getOutputStream(), true);
		} catch(IOException e){
			logger.error("IOException occur.");
		}
		
		processInput(in, out);		
		close(in, out);
	}
	
	/**
	 * Read the input send from client in JSON format, validate the syntax of the JSON input,
	 * call invokeOperation() private method, and write the result in JSON format to the 
	 * network output stream to send to the client.
	 * 
	 * @param in	the input send from client.
	 * @param out	the output result send to client.
	 */
	private void processInput(BufferedReader in, PrintWriter out){
		String output="";
		String line = "";
		boolean isPolling = false;
		
		try{
			// Read the input and serialized it into Operation object.
			line = in.readLine();
			Operation operation = new Gson().fromJson(line, Operation.class);
			logger.debug(line);
			
			// Invoke the operation
			OperationReply reply = new OperationReply();
			invokeOperation(operation, reply);
				
			output = new Gson().toJson(reply);
		} catch (NullPointerException e){
			OperationReply reply = new OperationReply(OperationReply.ERROR_REPLY, "Null Exception Occur.");
			output = new Gson().toJson(reply);
			logger.error("NullPointerException occur. It could be thrown by the operation invoked, and client is sending null data.");
		}catch(IOException e){
			logger.error("IOException occur.");
		} catch(JsonSyntaxException e){
			// Check if it is a plain GET HTTP request 
			// This is used by Firefox plugin to check status of the server.
			if(line.length() > 3 && line.substring(0, 3).equals("GET")){
				OperationReply reply = new OperationReply(OperationReply.REPLY, "Server is alive.");
				output = new Gson().toJson(reply);
				
				//Setting isPolling to true will disable logging message for this request
				isPolling = true; 
			}
			else{
				OperationReply reply = new OperationReply(OperationReply.ERROR_REPLY, "Syntax error");
				output = new Gson().toJson(reply);
				logger.error("Syntax error");
			}
		} catch(NoSuchMethodException e){
			OperationReply reply = new OperationReply(OperationReply.ERROR_REPLY, "Method is not invoked: No such method is found");
			output = new Gson().toJson(reply);
			logger.error("Method is not invoked: No such method is found");
		}
		
		out.println(output);

		if(!isPolling){
			logger.debug(output);
			logger.info("sent result to the client.");
		}
	}
	
	/**
	 * Call the methods in ACP logic based on client request. The return result of the 
	 * methods are stored in the reply argument object.
	 * 	
	 * @param operation					client request.
	 * @param reply						client reply.
	 * @throws NoSuchMethodException 	thrown when no method is been called.
	 */
	private void invokeOperation(Operation operation, OperationReply reply) throws NoSuchMethodException{
		String operationName = operation.getName().trim();
		ACPLogic logic = ACPLogic.getInstance();
		List<Object> parameters = operation.getParameters();
		
		logger.info("Invoking the function call");
		switch(operationName){
		case "processRawText":
		{
			if(isValidParameter(parameters, 3, new Class[]{String.class, String.class, String.class}, reply)){
				String rawText = (String) parameters.get(0);
				String sourceName = (String) parameters.get(1);
				String url = (String) parameters.get(2);
				logic.processRawText(rawText, sourceName, url);
				reply.setResult(null);
			}
			break;
		}
		case "requestSuggestion":
		{
			if(isValidParameter(parameters, 2, new Class[]{String.class, boolean.class}, reply)){
				String userInput = (String) parameters.get(0);
				boolean autoTrigger = (boolean) parameters.get(1);
				List result = logic.requestSuggestion(userInput, autoTrigger);
				reply.setResult(result);
				reply.setStatus(OperationReply.REPLY);
			}
			break;
		}
		case "chooseSuggestion":
		{
			// TODO: Quick fix for now
			// GSON does not provide support for casting of dynamic List<Object>
			//if(isValidParameter(parameters, 1, new Class[]{Suggestion.class}, reply)){
				LinkedHashMap hm = (LinkedHashMap) parameters.get(0);
				
				Sentence sentence = new Sentence();
				//sourcename, parentOrder, paragraphID
				LinkedHashMap hm2 = (LinkedHashMap) hm.get("source");
				
				sentence.setSource(new Source(hm2.get("name").toString(), hm2.get("name").toString()));
				sentence.setParentOrder( (int) Double.parseDouble(hm.get("parentOrder").toString()));
				sentence.setParagraphID( (int) Double.parseDouble(hm.get("paragraphID").toString()));
				
				Suggestion suggestion = sentence;
				logic.chooseSuggestion(suggestion);
				reply.setResult(null);
				reply.setStatus(OperationReply.REPLY);
			//}
			break;
		}
		case "closeSourceDocument":
		{
			if(isValidParameter(parameters, 1, new Class[]{String.class}, reply)){
				String sourceName = (String) parameters.get(0);
				logic.closeSourceDocument(sourceName);
				reply.setResult(null);
				reply.setStatus(OperationReply.REPLY);
			}		
			break;
		}
		case "getFilePath":
		{
			String result = logic.getPreferenceConfigurationFileAbsolutePath();
			reply.setResult(result);
			reply.setStatus(OperationReply.REPLY);
			break;
		}
		case "requestExtendSuggestion":
			if(isValidParameter(parameters, 2, new Class[]{Double.class, Double.class}, reply)){
				int id = (int)(double)parameters.get(0);
				int type = (int)(double)parameters.get(1);
				Suggestion sugg = logic.requestExtendSuggestion(id, type);
				List result = new ArrayList();
				List sentences = null;
				
				if(sugg != null){
					if(sugg instanceof Sentence){
						sentences = new ArrayList();
						sentences.add(sugg);
					}
					else
						sentences = ((Paragraph)sugg).getSentences();
					
					result = sentences;
				}
				
				reply.setResult(result);
				reply.setStatus(OperationReply.REPLY);
			}
			break;
		case "setDestinationDocument":
			if(isValidParameter(parameters, 1, new Class[]{Double.class}, reply)){
				int id = (int)(double)parameters.get(0);
				logic.setDestinationDocument(id);
				reply.setResult(null);
				reply.setStatus(OperationReply.REPLY);
			}
			break;
		case "closeDestinationDocument":
			if(isValidParameter(parameters, 1, new Class[]{Double.class}, reply)){
				int id = (int)(double)parameters.get(0);
				logic.closeDestinationDocument(id);
				reply.setResult(null);
				reply.setStatus(OperationReply.REPLY);
			}
			break;
		default:
			throw new NoSuchMethodException();
		}
	}
	
	/**
	 * Validate the length and the type of the parameters.
	 *  
	 * @param parameters	the list of function parameters.
	 * @param parameterSize	the correct array size.
	 * @param parameterType	the correct array of parameters' class/type.
	 * @param reply			Client reply.
	 * @return				true, if the length and the type of the parameters is correct.
	 */
	private boolean isValidParameter(List<Object> parameters, int parameterSize, Class[] parameterType, OperationReply reply){
		if(parameters!=null && parameters.size() == parameterSize){
			for(int i=0; i < parameterSize; i++){
				if(!isCompatible(parameters.get(i), parameterType[i])){
					reply.setErrorReply("Mismatch parameter type");
					return false;
				}
			}
			
			return true;
		}
		else{
			reply.setErrorReply("Invalid parameter length");
			return false;
		}
	}

	/**
	 * Check if the object is compatible with the defined type. The object argument
	 * is the object to be validated. The paramType argument is the type it is checking.
	 * 
	 * @param object	the testing object.
	 * @param paramType	the correct object type.
	 * @return			true, if the testing object can be cast to the object type.
	 */
	private boolean isCompatible(final Object object, final Class<?> paramType){
		try{
		    if(object == null){
		        return !paramType.isPrimitive();
		    }
	
		    if(paramType.isInstance(object)){
		        return true;
		    }
	
		    if(paramType.isPrimitive()){
		        return isWrapperTypeOf(object.getClass(), paramType);
		    }
		}
		catch(Exception e){
		}
		
	    return false;
	}
	
	/**
	 * Check if the object is compatible with the defined primitive type. The candidate 
	 * argument is the variable to be validated. The primitiveType argument is the
	 * primitive type (eg. int, char) it is checking.
	 * 
	 * @param candidate		the testing variable.
	 * @param primitiveType	the correct primitive type.
	 * @return				true, if the testing variable can be cast to the primitive type.
	 * @throws Exception	thrown when general exception occur during converting.
	 */
	private boolean isWrapperTypeOf(final Class<?> candidate,
	    final Class<?> primitiveType) throws Exception{
	    try{
	    	// To fix the problem with double/int
	    	if(candidate.getDeclaredField("TYPE").get(null).toString().equals("double") && primitiveType.getName().equals("int")){
	    		return true;
	    	}
	    	else{
	    		return !candidate.isPrimitive() && candidate.getDeclaredField("TYPE").get(null).equals(primitiveType);
	    	}
	    } catch(final NoSuchFieldException e){
	        return false;
	    } catch(final Exception e){
	        throw e;
	    }
	}
	
	/**
	 * Close the network input and output stream, and the client connection.
	 * @param in	input stream.
	 * @param out	output stream.
	 */
	private void close(BufferedReader in, PrintWriter out){
		try{
			in.close();
			out.close();
			client.close();
		} catch(IOException e){
			System.out.println("Close failed");
		}
	}
}
