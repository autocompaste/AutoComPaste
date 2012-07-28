package acp.test;

import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Properties;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import acp.ACPLogic;
import acp.background.Server;
import acp.beans.Operation;
import acp.beans.OperationReply;
import acp.beans.Suggestion;
import acp.beans.entity.Sentence;
import acp.beans.entity.Source;

import com.google.gson.Gson;

/**
 * @author Loke Yan hao
 *
 */
public class ServerTest {
	
	private static int port = 5566;
	
	// Server
	private static Server server;
	private static Thread serverThread;
	
	// Client
	private Socket socket;
	private PrintWriter out;
	private BufferedReader in;
	
	@BeforeClass
	public static void setupClass() {
		server = new Server(port);
		serverThread = new Thread(server);
		serverThread.start();
		
		ACPLogic logic = ACPLogic.getInstance();
		logic.initialiseLibraries();
	}
	
	@AfterClass
	public static void closeClass(){		
		ACPLogic logic = ACPLogic.getInstance();
		logic.closeSourceDocument("testcase://TestProcessRawText");
		logic.closeSourceDocument("testcase://TestCloseSourceDocument");
		
		server.terminate();
	}
	
	@Before
	public void setupClient(){
		try{
			socket = new Socket("127.0.0.1", port);
			out = new PrintWriter(socket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		} catch(UnknownHostException e){
			fail("Unknown host: 127.0.0.1");
		} catch(IOException e){
			fail("I/O failed");
		}
	}
	
	@Test
	public void testSendingWrongMessage() {
		out.println("sending a network message that is not formatted correctly");
		
		try {
			String output = in.readLine();
			OperationReply reply = new Gson().fromJson(output, OperationReply.class);
			Assert.assertEquals(OperationReply.ERROR_REPLY ,reply.getStatus());
			Assert.assertEquals("Syntax error", (String) reply.getResult());
		} catch (Exception e) {
			fail("Exception");
		}
	}
	
	// Note: The following few methods only test if the server does reply the network message;
	///      it does not check if the underlying methods are giving correct output
	
	@Test
	public void testProcessRawText(){		
		ArrayList<Object> parameters = new ArrayList<Object>();

		parameters.add("This is an example of a sentence. This is another example of a sentence.");
		parameters.add("TestProcessRawText");
		parameters.add("testcase://TestProcessRawText");
		
		Operation operation = new Operation("processRawText", parameters);
		String input = new Gson().toJson(operation);
		
		out.println(input);
		
		try {
			String output = in.readLine();
			OperationReply reply = new Gson().fromJson(output, OperationReply.class);
			Assert.assertEquals(OperationReply.REPLY ,reply.getStatus());
		} catch (Exception e) {
			fail("Exception");
		}
	}
	
	@Test
	public void testProcessRawTextWithInvalidParameterLength(){		
		ArrayList<Object> parameters = new ArrayList<Object>();
		parameters.add("This is an example of a sentence. This is another example of a sentence.");
		
		Operation operation = new Operation("processRawText", parameters);
		String input = new Gson().toJson(operation);
		
		out.println(input);
		
		try {
			String output = in.readLine();
			OperationReply reply = new Gson().fromJson(output, OperationReply.class);
			Assert.assertEquals(OperationReply.ERROR_REPLY ,reply.getStatus());
			Assert.assertEquals("Invalid parameter length", (String)reply.getResult());
		} catch (Exception e) {
			fail("Exception");
		}
	}
	
	@Test
	public void testProcessRawTextWithInvalidParameterType(){		
		ArrayList<Object> parameters = new ArrayList<Object>();
		parameters.add("This is an example of a sentence. This is another example of a sentence.");
		parameters.add(5);
		parameters.add("testcase://testProcessRawTextWithInvalidParameterType");
		
		Operation operation = new Operation("processRawText", parameters);
		String input = new Gson().toJson(operation);
		
		out.println(input);
		
		try {
			String output = in.readLine();
			OperationReply reply = new Gson().fromJson(output, OperationReply.class);
			Assert.assertEquals(OperationReply.ERROR_REPLY ,reply.getStatus());
			Assert.assertEquals("Mismatch parameter type", (String)reply.getResult());
		} catch (Exception e) {
			fail("Exception");
		}
	}
	
	@Test
	public void testRequestSuggestion(){		
		ArrayList<Object> parameters = new ArrayList<Object>();
		parameters.add("Testcase");
		parameters.add(true);
		
		Operation operation = new Operation("requestSuggestion", parameters);
		String input = new Gson().toJson(operation);
		
		out.println(input);
		
		try {
			String output = in.readLine();
			OperationReply reply = new Gson().fromJson(output, OperationReply.class);
			Assert.assertEquals(OperationReply.REPLY ,reply.getStatus());
		} catch (Exception e) {
			fail("Exception");
		}
	}
	
	@Test
	public void testChooseSuggestion(){		
		ArrayList<Object> parameters = new ArrayList<Object>();
		Sentence sentence = new Sentence();
		
		sentence.setSource(new Source("testChooseSuggestion", "testcase://testChooseSuggestion"));
		sentence.setParentOrder(0);
		sentence.setParagraphID(1);
		
		parameters.add(sentence);
		
		Operation operation = new Operation("chooseSuggestion", parameters);
		String input = new Gson().toJson(operation);
		
		out.println(input);
		
		try {
			String output = in.readLine();
			OperationReply reply = new Gson().fromJson(output, OperationReply.class);
			Assert.assertEquals(OperationReply.REPLY ,reply.getStatus());
		} catch (Exception e) {
			fail("Exception");
		}
	}
	
	@Test
	public void testCloseSourceDocument(){
		ACPLogic logic = ACPLogic.getInstance();
		logic.processRawText("<HTML><HEAD></HEAD><BODY><P>Test1 Test test. Test2 Test test.</P></BODY></HTML>", "TestCloseSourceDocument", "testcase://TestCloseSourceDocument");
		
		ArrayList<Object> parameters = new ArrayList<Object>();
		parameters.add("testcase://TestCloseSourceDocument");
		
		Operation operation = new Operation("closeSourceDocument", parameters);
		String input = new Gson().toJson(operation);
		
		out.println(input);
		
		try {
			String output = in.readLine();
			OperationReply reply = new Gson().fromJson(output, OperationReply.class);
			Assert.assertEquals(OperationReply.REPLY, reply.getStatus());
		} catch (Exception e) {
			fail("Exception");
		}
	}
		
	@Test
	public void testInvokeInexistingMethod(){
		ArrayList<Object> parameters = new ArrayList<Object>();
		parameters.add("Testcase");
		
		Operation operation = new Operation("noSuchMethod", parameters);
		String input = new Gson().toJson(operation);
		
		out.println(input);
		
		try {
			String output = in.readLine();
			OperationReply reply = new Gson().fromJson(output, OperationReply.class);
			Assert.assertEquals(OperationReply.ERROR_REPLY,reply.getStatus());
			Assert.assertEquals("Method is not invoked: No such method is found",(String)reply.getResult());
		} catch (Exception e) {
			fail("Exception");
		}
	}
	
	@After
	public void closeClient(){
		try{
			in.close();
			out.close();
			socket.close();
		} catch(Exception e){
			fail("Exception");
		}
	}
}
