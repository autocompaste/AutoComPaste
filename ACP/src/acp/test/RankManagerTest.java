package acp.test;

import static org.junit.Assert.*;

import java.util.ArrayList;

import acp.beans.RankEntry;
import acp.manager.RankManager;
import acp.store.RankDataStore;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Ng Chin Hui
 *
 */
public class RankManagerTest {

	private RankManager rm;
	private int destinationDocument1, destinationDocument2;
	
	@Before
    public void setUp() {
		rm = new RankManager();
		rm.testing_mode();
		rm.showDebugUI();
        destinationDocument1 = 1122;
        destinationDocument2 = 2233;
    }
	
	@After
	public void tearDown(){
		rm.closeDebugUI();
		rm = null;
	}
	
	@Test
	public void testAddNewEntry_1(){
		
		/* construct expected result */
		ArrayList<RankEntry> expected_highList = new ArrayList<RankEntry>();
		ArrayList<RankEntry> expected_lowList = new ArrayList<RankEntry>();
		
		RankEntry e = new RankEntry("WIKIPEDIA", 1);
		expected_highList.add(e);
		
		/* execute the function to be tested */
		rm.addNewEntry("WIKIPEDIA");
		
		/* checking the result */
		ArrayList<RankEntry> result_highList = rm.getHighlistList().get(0);
		ArrayList<RankEntry> result_lowList = rm.getLowlistList().get(0);
		
		assertTrue(expected_highList.size() == result_highList.size());
		assertTrue(expected_lowList.size() == result_lowList.size());
		
		boolean check_highList = true;
		for(int i=0; i<expected_highList.size(); i++){
			if(!expected_highList.get(i).equals(result_highList.get(i))){
				check_highList = false;
			}
		}
		assertTrue(check_highList);
		
		boolean check_lowList = true;
		for(int i=0; i<expected_lowList.size(); i++){
			if(!expected_lowList.get(i).equals(result_lowList.get(i))){
				check_lowList = false;
			}
		}
		assertTrue(check_lowList);
	}
	
	@Test
	public void testAddNewEntry_2(){
		
		/* Construct expected result */
		ArrayList<ArrayList<RankEntry>> expected_highlistList = new ArrayList<ArrayList<RankEntry>>();
		ArrayList<ArrayList<RankEntry>> expected_lowlistList = new ArrayList<ArrayList<RankEntry>>();
		
		RankEntry e = new RankEntry("WIKIPEDIA", 1);
		for(int i=0; i<2; i++){
			ArrayList<RankEntry> expected_highList = new ArrayList<RankEntry>();
			expected_highList.add((RankEntry)e.clone());
			expected_highlistList.add(expected_highList);
			
			ArrayList<RankEntry> expected_lowList = new ArrayList<RankEntry>();
			expected_lowlistList.add(expected_lowList);
		}
		
		/* Setting up the environment */
		rm.setDestinationDocument(destinationDocument1);
        rm.setDestinationDocument(destinationDocument2);
		
		/* execute the function to be tested */
		rm.addNewEntry("WIKIPEDIA");
		
		/* checking the result */
		ArrayList<ArrayList<RankEntry>> result_highlistList = rm.getHighlistList();
		ArrayList<ArrayList<RankEntry>> result_lowlistList = rm.getLowlistList();
		
		/* Checking size */
		assertTrue(result_highlistList.size() == (expected_highlistList.size()+1));
		assertTrue(result_lowlistList.size() == (expected_lowlistList.size()+1));
		
		for(int i=1; i<result_highlistList.size(); i++){
			ArrayList<RankEntry> result_highList = result_highlistList.get(i);
			ArrayList<RankEntry> expected_highList = expected_highlistList.get(i-1);
			assertTrue(result_highList.size() == expected_highList.size());
			
			ArrayList<RankEntry> result_lowList = result_lowlistList.get(i);
			ArrayList<RankEntry> expected_lowList = expected_lowlistList.get(i-1);
			assertTrue(result_lowList.size() == expected_lowList.size());
		}
		
		/* Checking the content */
		for(int i=1; i<result_highlistList.size(); i++){
			ArrayList<RankEntry> result_highList = result_highlistList.get(i);
			ArrayList<RankEntry> expected_highList = expected_highlistList.get(i-1);
			boolean check_highList = true;
			for(int j=0; j<expected_highList.size(); j++){
				if(!expected_highList.get(j).equals(result_highList.get(j))){
					check_highList = false;
				}
			}
			assertTrue(check_highList);
			
			ArrayList<RankEntry> result_lowList = result_lowlistList.get(i);
			ArrayList<RankEntry> expected_lowList = expected_lowlistList.get(i-1);
			boolean check_lowList = true;
			for(int j=0; j<expected_lowList.size(); j++){
				if(!expected_lowList.get(j).equals(result_lowList.get(j))){
					check_lowList = false;
				}
			}
			assertTrue(check_lowList);
		}
	}
	
	@Test
	public void testAddNewEntry_3(){
		
		/* construct expected result */
		ArrayList<RankEntry> expected_highList = new ArrayList<RankEntry>();
		ArrayList<RankEntry> expected_lowList = new ArrayList<RankEntry>();
		
		RankEntry e = new RankEntry("WORDPRESS", 2);
		expected_highList.add(e);
		e = new RankEntry("WIKIPEDIA", 1);
		expected_highList.add(e);
		
		/* Setting up the environment */
		rm.addNewEntry("WIKIPEDIA");
		
		/* execute the function to be tested */
		rm.addNewEntry("WORDPRESS");
		
		/* checking the result */
		ArrayList<RankEntry> result_highList = rm.getHighlistList().get(0);
		ArrayList<RankEntry> result_lowList = rm.getLowlistList().get(0);
		
		assertTrue(expected_highList.size() == result_highList.size());
		assertTrue(expected_lowList.size() == result_lowList.size());
		
		boolean check_highList = true;
		for(int i=0; i<expected_highList.size(); i++){
			if(!expected_highList.get(i).equals(result_highList.get(i))){
				check_highList = false;
			}
		}
		assertTrue(check_highList);
		
		boolean check_lowList = true;
		for(int i=0; i<expected_lowList.size(); i++){
			if(!expected_lowList.get(i).equals(result_lowList.get(i))){
				check_lowList = false;
			}
		}
		assertTrue(check_lowList);
	}
	
	@Test
	public void testAddNewEntry_4(){
		
		/* Construct expected result */
		ArrayList<ArrayList<RankEntry>> expected_highlistList = new ArrayList<ArrayList<RankEntry>>();
		ArrayList<ArrayList<RankEntry>> expected_lowlistList = new ArrayList<ArrayList<RankEntry>>();
		
		for(int i=0; i<2; i++){
			ArrayList<RankEntry> expected_highList = new ArrayList<RankEntry>();
			RankEntry e = new RankEntry("WORDPRESS", 2);
			expected_highList.add((RankEntry)e.clone());
			e = new RankEntry("WIKIPEDIA", 1);
			expected_highList.add((RankEntry)e.clone());
			expected_highlistList.add(expected_highList);
			
			ArrayList<RankEntry> expected_lowList = new ArrayList<RankEntry>();
			expected_lowlistList.add(expected_lowList);
		}
		
		/* Setting up the environment */
		rm.setDestinationDocument(destinationDocument1);
        rm.setDestinationDocument(destinationDocument2);
        rm.addNewEntry("WIKIPEDIA");
		
		/* execute the function to be tested */
		rm.addNewEntry("WORDPRESS");
		
		/* checking the result */
		ArrayList<ArrayList<RankEntry>> result_highlistList = rm.getHighlistList();
		ArrayList<ArrayList<RankEntry>> result_lowlistList = rm.getLowlistList();
		
		/* Checking size */
		assertTrue(result_highlistList.size() == (expected_highlistList.size()+1));
		assertTrue(result_lowlistList.size() == (expected_lowlistList.size()+1));
		
		for(int i=1; i<result_highlistList.size(); i++){
			ArrayList<RankEntry> result_highList = result_highlistList.get(i);
			ArrayList<RankEntry> expected_highList = expected_highlistList.get(i-1);
			assertTrue(result_highList.size() == expected_highList.size());
			
			ArrayList<RankEntry> result_lowList = result_lowlistList.get(i);
			ArrayList<RankEntry> expected_lowList = expected_lowlistList.get(i-1);
			assertTrue(result_lowList.size() == expected_lowList.size());
		}
		
		/* Checking the content */
		for(int i=1; i<result_highlistList.size(); i++){
			ArrayList<RankEntry> result_highList = result_highlistList.get(i);
			ArrayList<RankEntry> expected_highList = expected_highlistList.get(i-1);
			boolean check_highList = true;
			for(int j=0; j<expected_highList.size(); j++){
				if(!expected_highList.get(j).equals(result_highList.get(j))){
					check_highList = false;
				}
			}
			assertTrue(check_highList);
			
			ArrayList<RankEntry> result_lowList = result_lowlistList.get(i);
			ArrayList<RankEntry> expected_lowList = expected_lowlistList.get(i-1);
			boolean check_lowList = true;
			for(int j=0; j<expected_lowList.size(); j++){
				if(!expected_lowList.get(j).equals(result_lowList.get(j))){
					check_lowList = false;
				}
			}
			assertTrue(check_lowList);
		}
	}
	
	@Test
	public void testAddNewEntry_5(){
		
		/* construct expected result */
		ArrayList<RankEntry> expected_highList = new ArrayList<RankEntry>();
		ArrayList<RankEntry> expected_lowList = new ArrayList<RankEntry>();
		
		RankEntry e = new RankEntry("PDF1", 4);
		expected_highList.add(e);
		e = new RankEntry("BLOGSPOT", 3);
		expected_highList.add(e);
		e = new RankEntry("WORDPRESS", 2);
		expected_highList.add(e);
		
		e = new RankEntry("WIKIPEDIA", 1);
		expected_lowList.add(e);
		
		/* Setting up the environment */
		rm.addNewEntry("WIKIPEDIA");
		rm.addNewEntry("WORDPRESS");
		rm.addNewEntry("BLOGSPOT");
		
		/* execute the function to be tested */
		rm.addNewEntry("PDF1");
		
		/* checking the result */
		ArrayList<RankEntry> result_highList = rm.getHighlistList().get(0);
		ArrayList<RankEntry> result_lowList = rm.getLowlistList().get(0);
		
		assertTrue(expected_highList.size() == result_highList.size());
		assertTrue(expected_lowList.size() == result_lowList.size());
		
		boolean check_highList = true;
		for(int i=0; i<expected_highList.size(); i++){
			if(!expected_highList.get(i).equals(result_highList.get(i))){
				check_highList = false;
			}
		}
		assertTrue(check_highList);
		
		boolean check_lowList = true;
		for(int i=0; i<expected_lowList.size(); i++){
			if(!expected_lowList.get(i).equals(result_lowList.get(i))){
				check_lowList = false;
			}
		}
		assertTrue(check_lowList);
	}
	
	@Test
	public void testAddNewEntry_6(){
		
		/* Construct expected result */
		ArrayList<ArrayList<RankEntry>> expected_highlistList = new ArrayList<ArrayList<RankEntry>>();
		ArrayList<ArrayList<RankEntry>> expected_lowlistList = new ArrayList<ArrayList<RankEntry>>();
		
		for(int i=0; i<2; i++){
			ArrayList<RankEntry> expected_highList = new ArrayList<RankEntry>();
			RankEntry e = new RankEntry("PDF1", 4);
			expected_highList.add((RankEntry)e.clone());
			e = new RankEntry("BLOGSPOT", 3);
			expected_highList.add((RankEntry)e.clone());
			e = new RankEntry("WORDPRESS", 2);
			expected_highList.add((RankEntry)e.clone());
			expected_highlistList.add(expected_highList);
			
			ArrayList<RankEntry> expected_lowList = new ArrayList<RankEntry>();
			e = new RankEntry("WIKIPEDIA", 1);
			expected_lowList.add((RankEntry)e.clone());
			expected_lowlistList.add(expected_lowList);
		}
		
		/* Setting up the environment */
		rm.setDestinationDocument(destinationDocument1);
        rm.setDestinationDocument(destinationDocument2);
        rm.addNewEntry("WIKIPEDIA");
		rm.addNewEntry("WORDPRESS");
		rm.addNewEntry("BLOGSPOT");
		
		/* execute the function to be tested */
		rm.addNewEntry("PDF1");
		
		/* checking the result */
		ArrayList<ArrayList<RankEntry>> result_highlistList = rm.getHighlistList();
		ArrayList<ArrayList<RankEntry>> result_lowlistList = rm.getLowlistList();
		
		/* Checking size */
		assertTrue(result_highlistList.size() == (expected_highlistList.size()+1));
		assertTrue(result_lowlistList.size() == (expected_lowlistList.size()+1));
		
		for(int i=1; i<result_highlistList.size(); i++){
			ArrayList<RankEntry> result_highList = result_highlistList.get(i);
			ArrayList<RankEntry> expected_highList = expected_highlistList.get(i-1);
			assertTrue(result_highList.size() == expected_highList.size());
			
			ArrayList<RankEntry> result_lowList = result_lowlistList.get(i);
			ArrayList<RankEntry> expected_lowList = expected_lowlistList.get(i-1);
			assertTrue(result_lowList.size() == expected_lowList.size());
		}
		
		/* Checking the content */
		for(int i=1; i<result_highlistList.size(); i++){
			ArrayList<RankEntry> result_highList = result_highlistList.get(i);
			ArrayList<RankEntry> expected_highList = expected_highlistList.get(i-1);
			boolean check_highList = true;
			for(int j=0; j<expected_highList.size(); j++){
				if(!expected_highList.get(j).equals(result_highList.get(j))){
					check_highList = false;
				}
			}
			assertTrue(check_highList);
			
			ArrayList<RankEntry> result_lowList = result_lowlistList.get(i);
			ArrayList<RankEntry> expected_lowList = expected_lowlistList.get(i-1);
			boolean check_lowList = true;
			for(int j=0; j<expected_lowList.size(); j++){
				if(!expected_lowList.get(j).equals(result_lowList.get(j))){
					check_lowList = false;
				}
			}
			assertTrue(check_lowList);
		}
	}
	
	@Test
	public void testChangeRanks_1(){
		
		/* construct expected result */
		ArrayList<RankEntry> expected_highList = new ArrayList<RankEntry>();
		ArrayList<RankEntry> expected_lowList = new ArrayList<RankEntry>();
		
		RankEntry e = new RankEntry("BLOGSPOT", 5);
		expected_highList.add(e);
		e = new RankEntry("PDF1", 4);
		expected_highList.add(e);
		e = new RankEntry("PDF2", 3);
		expected_highList.add(e);
		
		e = new RankEntry("WORDPRESS", 2);
		expected_lowList.add(e);
		e = new RankEntry("WIKIPEDIA", 1);
		expected_lowList.add(e);
		
		/* Setting up the environment */
		rm.addNewEntry("WIKIPEDIA");
		rm.addNewEntry("WORDPRESS");
		rm.addNewEntry("PDF2");
		rm.addNewEntry("PDF1");
		rm.addNewEntry("BLOGSPOT");
		
		/* execute the function to be tested */
		rm.changeRanks("ABC");
		
		/* checking the result */
		ArrayList<RankEntry> result_highList = rm.getHighlistList().get(0);
		ArrayList<RankEntry> result_lowList = rm.getLowlistList().get(0);
		
		assertTrue(expected_highList.size() == result_highList.size());
		assertTrue(expected_lowList.size() == result_lowList.size());
		
		boolean check_highList = true;
		for(int i=0; i<expected_highList.size(); i++){
			if(!expected_highList.get(i).equals(result_highList.get(i))){
				check_highList = false;
			}
		}
		assertTrue(check_highList);
		
		boolean check_lowList = true;
		for(int i=0; i<expected_lowList.size(); i++){
			if(!expected_lowList.get(i).equals(result_lowList.get(i))){
				check_lowList = false;
			}
		}
		assertTrue(check_lowList);
	}
	
	@Test
	public void testChangeRanks_2(){
		
		/* Construct expected result */
		ArrayList<ArrayList<RankEntry>> expected_highlistList = new ArrayList<ArrayList<RankEntry>>();
		ArrayList<ArrayList<RankEntry>> expected_lowlistList = new ArrayList<ArrayList<RankEntry>>();
		
		for(int i=0; i<2; i++){
			ArrayList<RankEntry> expected_highList = new ArrayList<RankEntry>();
			RankEntry e = new RankEntry("BLOGSPOT", 5);
			expected_highList.add((RankEntry)e.clone());
			e = new RankEntry("PDF1", 4);
			expected_highList.add((RankEntry)e.clone());
			e = new RankEntry("PDF2", 3);
			expected_highList.add((RankEntry)e.clone());
			expected_highlistList.add(expected_highList);
			
			ArrayList<RankEntry> expected_lowList = new ArrayList<RankEntry>();
			e = new RankEntry("WORDPRESS", 2);
			expected_lowList.add((RankEntry)e.clone());
			e = new RankEntry("WIKIPEDIA", 1);
			expected_lowList.add((RankEntry)e.clone());
			expected_lowlistList.add(expected_lowList);
		}
		
		/* Setting up the environment */
		rm.setDestinationDocument(destinationDocument1);
        rm.setDestinationDocument(destinationDocument2);
    	rm.addNewEntry("WIKIPEDIA");
		rm.addNewEntry("WORDPRESS");
		rm.addNewEntry("PDF2");
		rm.addNewEntry("PDF1");
		rm.addNewEntry("BLOGSPOT");
		
		/* execute the function to be tested */
		rm.changeRanks("ABC");
		
		/* checking the result */
		ArrayList<ArrayList<RankEntry>> result_highlistList = rm.getHighlistList();
		ArrayList<ArrayList<RankEntry>> result_lowlistList = rm.getLowlistList();
		
		/* Checking size */
		assertTrue(result_highlistList.size() == (expected_highlistList.size()+1));
		assertTrue(result_lowlistList.size() == (expected_lowlistList.size()+1));
		
		for(int i=1; i<result_highlistList.size(); i++){
			ArrayList<RankEntry> result_highList = result_highlistList.get(i);
			ArrayList<RankEntry> expected_highList = expected_highlistList.get(i-1);
			assertTrue(result_highList.size() == expected_highList.size());
			
			ArrayList<RankEntry> result_lowList = result_lowlistList.get(i);
			ArrayList<RankEntry> expected_lowList = expected_lowlistList.get(i-1);
			assertTrue(result_lowList.size() == expected_lowList.size());
		}
		
		/* Checking the content */
		for(int i=1; i<result_highlistList.size(); i++){
			ArrayList<RankEntry> result_highList = result_highlistList.get(i);
			ArrayList<RankEntry> expected_highList = expected_highlistList.get(i-1);
			boolean check_highList = true;
			for(int j=0; j<expected_highList.size(); j++){
				if(!expected_highList.get(j).equals(result_highList.get(j))){
					check_highList = false;
				}
			}
			assertTrue(check_highList);
			
			ArrayList<RankEntry> result_lowList = result_lowlistList.get(i);
			ArrayList<RankEntry> expected_lowList = expected_lowlistList.get(i-1);
			boolean check_lowList = true;
			for(int j=0; j<expected_lowList.size(); j++){
				if(!expected_lowList.get(j).equals(result_lowList.get(j))){
					check_lowList = false;
				}
			}
			assertTrue(check_lowList);
		}
	}
	
	@Test
	public void testChangeRanks_3(){
		
		/* construct expected result */
		ArrayList<RankEntry> expected_highList = new ArrayList<RankEntry>();
		ArrayList<RankEntry> expected_lowList = new ArrayList<RankEntry>();
		
		RankEntry e = new RankEntry("PDF1", 4);
		expected_highList.add(e);
		e = new RankEntry("BLOGSPOT", 5);
		expected_highList.add(e);
		e = new RankEntry("PDF2", 3);
		expected_highList.add(e);
		
		e = new RankEntry("WORDPRESS", 2);
		expected_lowList.add(e);
		e = new RankEntry("WIKIPEDIA", 1);
		expected_lowList.add(e);
		
		/* Setting up the environment */
		rm.addNewEntry("WIKIPEDIA");
		rm.addNewEntry("WORDPRESS");
		rm.addNewEntry("PDF2");
		rm.addNewEntry("PDF1");
		rm.addNewEntry("BLOGSPOT");
		
		/* execute the function to be tested */
		rm.changeRanks("PDF1");
		
		/* checking the result */
		ArrayList<RankEntry> result_highList = rm.getHighlistList().get(0);
		ArrayList<RankEntry> result_lowList = rm.getLowlistList().get(0);
		
		assertTrue(expected_highList.size() == result_highList.size());
		assertTrue(expected_lowList.size() == result_lowList.size());
		
		boolean check_highList = true;
		for(int i=0; i<expected_highList.size(); i++){
			if(!expected_highList.get(i).equals(result_highList.get(i))){
				check_highList = false;
			}
		}
		assertTrue(check_highList);
		
		boolean check_lowList = true;
		for(int i=0; i<expected_lowList.size(); i++){
			if(!expected_lowList.get(i).equals(result_lowList.get(i))){
				check_lowList = false;
			}
		}
		assertTrue(check_lowList);
	}
	
	@Test
	public void testChangeRanks_4(){
		
		/* Construct expected result */
		ArrayList<ArrayList<RankEntry>> expected_highlistList = new ArrayList<ArrayList<RankEntry>>();
		ArrayList<ArrayList<RankEntry>> expected_lowlistList = new ArrayList<ArrayList<RankEntry>>();
		
		for(int i=0; i<2; i++){
			ArrayList<RankEntry> expected_highList = new ArrayList<RankEntry>();
			RankEntry e = new RankEntry("PDF1", 4);
			expected_highList.add((RankEntry)e.clone());
			e = new RankEntry("BLOGSPOT", 5);
			expected_highList.add((RankEntry)e.clone());
			e = new RankEntry("PDF2", 3);
			expected_highList.add((RankEntry)e.clone());
			expected_highlistList.add(expected_highList);
			
			ArrayList<RankEntry> expected_lowList = new ArrayList<RankEntry>();
			e = new RankEntry("WORDPRESS", 2);
			expected_lowList.add((RankEntry)e.clone());
			e = new RankEntry("WIKIPEDIA", 1);
			expected_lowList.add((RankEntry)e.clone());
			expected_lowlistList.add(expected_lowList);
		}
		
		/* Setting up the environment */
		rm.setDestinationDocument(destinationDocument1);
        rm.setDestinationDocument(destinationDocument2);
    	rm.addNewEntry("WIKIPEDIA");
		rm.addNewEntry("WORDPRESS");
		rm.addNewEntry("PDF2");
		rm.addNewEntry("PDF1");
		rm.addNewEntry("BLOGSPOT");
		
		/* execute the function to be tested */
		rm.changeRanks("PDF1");
		
		/* checking the result */
		ArrayList<ArrayList<RankEntry>> result_highlistList = rm.getHighlistList();
		ArrayList<ArrayList<RankEntry>> result_lowlistList = rm.getLowlistList();
		
		/* Checking size */
		assertTrue(result_highlistList.size() == (expected_highlistList.size()+1));
		assertTrue(result_lowlistList.size() == (expected_lowlistList.size()+1));
		
		for(int i=1; i<result_highlistList.size(); i++){
			ArrayList<RankEntry> result_highList = result_highlistList.get(i);
			ArrayList<RankEntry> expected_highList = expected_highlistList.get(i-1);
			assertTrue(result_highList.size() == expected_highList.size());
			
			ArrayList<RankEntry> result_lowList = result_lowlistList.get(i);
			ArrayList<RankEntry> expected_lowList = expected_lowlistList.get(i-1);
			assertTrue(result_lowList.size() == expected_lowList.size());
		}
		
		/* Checking the content */
		for(int i=1; i<result_highlistList.size(); i++){
			ArrayList<RankEntry> result_highList = result_highlistList.get(i);
			ArrayList<RankEntry> expected_highList = expected_highlistList.get(i-1);
			boolean check_highList = true;
			for(int j=0; j<expected_highList.size(); j++){
				if(!expected_highList.get(j).equals(result_highList.get(j))){
					check_highList = false;
				}
			}
			assertTrue(check_highList);
			
			ArrayList<RankEntry> result_lowList = result_lowlistList.get(i);
			ArrayList<RankEntry> expected_lowList = expected_lowlistList.get(i-1);
			boolean check_lowList = true;
			for(int j=0; j<expected_lowList.size(); j++){
				if(!expected_lowList.get(j).equals(result_lowList.get(j))){
					check_lowList = false;
				}
			}
			assertTrue(check_lowList);
		}
	}
	
	@Test
	public void testChangeRanks_5(){
		
		/* construct expected result */
		ArrayList<RankEntry> expected_highList = new ArrayList<RankEntry>();
		ArrayList<RankEntry> expected_lowList = new ArrayList<RankEntry>();
		
		RankEntry e = new RankEntry("WORDPRESS", 2);
		expected_highList.add(e);
		e = new RankEntry("BLOGSPOT", 5);
		expected_highList.add(e);
		e = new RankEntry("PDF1", 4);
		expected_highList.add(e);
		
		e = new RankEntry("PDF2", 3);
		expected_lowList.add(e);
		e = new RankEntry("WIKIPEDIA", 1);
		expected_lowList.add(e);
		
		/* Setting up the environment */
		rm.addNewEntry("WIKIPEDIA");
		rm.addNewEntry("WORDPRESS");
		rm.addNewEntry("PDF2");
		rm.addNewEntry("PDF1");
		rm.addNewEntry("BLOGSPOT");
		
		/* execute the function to be tested */
		rm.changeRanks("WORDPRESS");
		
		/* checking the result */
		ArrayList<RankEntry> result_highList = rm.getHighlistList().get(0);
		ArrayList<RankEntry> result_lowList = rm.getLowlistList().get(0);
		
		assertTrue(expected_highList.size() == result_highList.size());
		assertTrue(expected_lowList.size() == result_lowList.size());
		
		boolean check_highList = true;
		for(int i=0; i<expected_highList.size(); i++){
			if(!expected_highList.get(i).equals(result_highList.get(i))){
				check_highList = false;
			}
		}
		assertTrue(check_highList);
		
		boolean check_lowList = true;
		for(int i=0; i<expected_lowList.size(); i++){
			if(!expected_lowList.get(i).equals(result_lowList.get(i))){
				check_lowList = false;
			}
		}
		assertTrue(check_lowList);
	}
	
	@Test
	public void testChangeRanks_6(){
		
		/* Construct expected result */
		ArrayList<ArrayList<RankEntry>> expected_highlistList = new ArrayList<ArrayList<RankEntry>>();
		ArrayList<ArrayList<RankEntry>> expected_lowlistList = new ArrayList<ArrayList<RankEntry>>();
		
		for(int i=0; i<2; i++){
			ArrayList<RankEntry> expected_highList = new ArrayList<RankEntry>();
			RankEntry e = new RankEntry("WORDPRESS", 2);
			expected_highList.add((RankEntry)e.clone());
			e = new RankEntry("BLOGSPOT", 5);
			expected_highList.add((RankEntry)e.clone());
			e = new RankEntry("PDF1", 4);
			expected_highList.add((RankEntry)e.clone());
			expected_highlistList.add(expected_highList);
			
			ArrayList<RankEntry> expected_lowList = new ArrayList<RankEntry>();
			e = new RankEntry("PDF2", 3);
			expected_lowList.add((RankEntry)e.clone());
			e = new RankEntry("WIKIPEDIA", 1);
			expected_lowList.add((RankEntry)e.clone());
			expected_lowlistList.add(expected_lowList);
		}
		
		/* Setting up the environment */
		rm.setDestinationDocument(destinationDocument1);
        rm.setDestinationDocument(destinationDocument2);
    	rm.addNewEntry("WIKIPEDIA");
		rm.addNewEntry("WORDPRESS");
		rm.addNewEntry("PDF2");
		rm.addNewEntry("PDF1");
		rm.addNewEntry("BLOGSPOT");
		
		/* execute the function to be tested */
		rm.changeRanks("WORDPRESS");
		
		/* checking the result */
		ArrayList<ArrayList<RankEntry>> result_highlistList = rm.getHighlistList();
		ArrayList<ArrayList<RankEntry>> result_lowlistList = rm.getLowlistList();
		
		/* Checking size */
		assertTrue(result_highlistList.size() == (expected_highlistList.size()+1));
		assertTrue(result_lowlistList.size() == (expected_lowlistList.size()+1));
		
		for(int i=1; i<result_highlistList.size(); i++){
			ArrayList<RankEntry> result_highList = result_highlistList.get(i);
			ArrayList<RankEntry> expected_highList = expected_highlistList.get(i-1);
			assertTrue(result_highList.size() == expected_highList.size());
			
			ArrayList<RankEntry> result_lowList = result_lowlistList.get(i);
			ArrayList<RankEntry> expected_lowList = expected_lowlistList.get(i-1);
			assertTrue(result_lowList.size() == expected_lowList.size());
		}
		
		/* Checking the content */
		for(int i=1; i<result_highlistList.size(); i++){
			ArrayList<RankEntry> result_highList = result_highlistList.get(i);
			ArrayList<RankEntry> expected_highList = expected_highlistList.get(i-1);
			boolean check_highList = true;
			for(int j=0; j<expected_highList.size(); j++){
				if(!expected_highList.get(j).equals(result_highList.get(j))){
					check_highList = false;
				}
			}
			assertTrue(check_highList);
			
			ArrayList<RankEntry> result_lowList = result_lowlistList.get(i);
			ArrayList<RankEntry> expected_lowList = expected_lowlistList.get(i-1);
			boolean check_lowList = true;
			for(int j=0; j<expected_lowList.size(); j++){
				if(!expected_lowList.get(j).equals(result_lowList.get(j))){
					check_lowList = false;
				}
			}
			assertTrue(check_lowList);
		}
	}
	
	@Test
	public void testLearnUserBehaviour_1(){
		
		/* construct expected result */
		// no expected result as the list does not exists
		
		/* Setting up the environment */
		rm.addNewEntry("WIKIPEDIA");
		rm.addNewEntry("WORDPRESS");
		rm.addNewEntry("PDF2");
		rm.addNewEntry("PDF1");
		rm.addNewEntry("BLOGSPOT");
		
		/* execute the function to be tested */
		rm.learnUserBehaviour(0, 0, "WIKIPEDIA");
		
		/* checking the result */
		ArrayList<ArrayList<RankEntry>> result_highlistList = rm.getHighlistList();
		ArrayList<ArrayList<RankEntry>> result_lowlistList = rm.getLowlistList();
		
		assertTrue(result_highlistList.size()-1 == 0); // -1 because first list is used to initializing new destination document
		assertTrue(result_lowlistList.size()-1 == 0); // -1 because first list is used to initializing new destination document
	}

	@Test
	public void testLearnUserBehaviour_2(){
		
		/* construct expected result */
		ArrayList<RankEntry> expected_highList = new ArrayList<RankEntry>();
		ArrayList<RankEntry> expected_lowList = new ArrayList<RankEntry>();
		
		RankEntry e = new RankEntry("BLOGSPOT", 5);
		expected_highList.add(e);
		e = new RankEntry("PDF1", 4);
		expected_highList.add(e);
		e = new RankEntry("PDF2", 3);
		expected_highList.add(e);
		
		e = new RankEntry("WORDPRESS", 2);
		expected_lowList.add(e);
		e = new RankEntry("WIKIPEDIA", 1);
		expected_lowList.add(e);
		
		/* Setting up the environment */
		rm.addNewEntry("WIKIPEDIA");
		rm.addNewEntry("WORDPRESS");
		rm.addNewEntry("PDF2");
		rm.addNewEntry("PDF1");
		rm.addNewEntry("BLOGSPOT");
		rm.setDestinationDocument(destinationDocument1);
		
		/* execute the function to be tested */
		rm.learnUserBehaviour(0, 0, "ABC");
		
		/* checking the result */
		ArrayList<RankEntry> result_highList = rm.getHighlistList().get(1);
		ArrayList<RankEntry> result_lowList = rm.getLowlistList().get(1);
		
		assertTrue(expected_highList.size() == result_highList.size());
		assertTrue(expected_lowList.size() == result_lowList.size());
		
		boolean check_highList = true;
		for(int i=0; i<expected_highList.size(); i++){
			if(!expected_highList.get(i).equals(result_highList.get(i)) || 
				expected_highList.get(i).getLastUsedParahraph() != result_highList.get(i).getLastUsedParahraph() ||
				expected_highList.get(i).getLastUsedSentence() != result_highList.get(i).getLastUsedSentence() ||
				expected_highList.get(i).getScore() != result_highList.get(i).getScore()){
				check_highList = false;
			}
		}
		assertTrue(check_highList);
		
		boolean check_lowList = true;
		for(int i=0; i<expected_lowList.size(); i++){
			if(!expected_lowList.get(i).equals(result_lowList.get(i)) || 
				expected_lowList.get(i).getLastUsedParahraph() != result_lowList.get(i).getLastUsedParahraph() ||
				expected_lowList.get(i).getLastUsedSentence() != result_lowList.get(i).getLastUsedSentence() ||
				expected_lowList.get(i).getScore() != result_lowList.get(i).getScore()){
				check_lowList = false;
				}
		}
		assertTrue(check_lowList);
	}
	
	@Test
	public void testLearnUserBehaviour_3(){
		
		/* construct expected result */
		ArrayList<RankEntry> expected_highList = new ArrayList<RankEntry>();
		ArrayList<RankEntry> expected_lowList = new ArrayList<RankEntry>();
		
		RankEntry e = new RankEntry("PDF1", 4);
		e.setLastUsedParahraph(0);
		e.setLastUsedSentence(0);
		e.setScore(100);
		expected_highList.add(e);
		e = new RankEntry("BLOGSPOT", 5);
		expected_highList.add(e);
		e = new RankEntry("PDF2", 3);
		expected_highList.add(e);
		
		e = new RankEntry("WORDPRESS", 2);
		expected_lowList.add(e);
		e = new RankEntry("WIKIPEDIA", 1);
		expected_lowList.add(e);
		
		/* Setting up the environment */
		rm.setDestinationDocument(destinationDocument1);
		rm.addNewEntry("WIKIPEDIA");
		rm.addNewEntry("WORDPRESS");
		rm.addNewEntry("PDF2");
		rm.addNewEntry("PDF1");
		rm.addNewEntry("BLOGSPOT");
		
		/* execute the function to be tested */
		rm.learnUserBehaviour(0, 0, "PDF1");
		
		/* checking the result */
		ArrayList<RankEntry> result_highList = rm.getHighlistList().get(1);
		ArrayList<RankEntry> result_lowList = rm.getLowlistList().get(1);
		
		assertTrue(expected_highList.size() == result_highList.size());
		assertTrue(expected_lowList.size() == result_lowList.size());
		
		boolean check_highList = true;
		for(int i=0; i<expected_highList.size(); i++){
			if(!expected_highList.get(i).equals(result_highList.get(i)) || 
				expected_highList.get(i).getLastUsedParahraph() != result_highList.get(i).getLastUsedParahraph() ||
				expected_highList.get(i).getLastUsedSentence() != result_highList.get(i).getLastUsedSentence() ||
				expected_highList.get(i).getScore() != result_highList.get(i).getScore()){
				check_highList = false;
			}
		}
		assertTrue(check_highList);
		
		boolean check_lowList = true;
		for(int i=0; i<expected_lowList.size(); i++){
			if(!expected_lowList.get(i).equals(result_lowList.get(i)) || 
				expected_lowList.get(i).getLastUsedParahraph() != result_lowList.get(i).getLastUsedParahraph() ||
				expected_lowList.get(i).getLastUsedSentence() != result_lowList.get(i).getLastUsedSentence() ||
				expected_lowList.get(i).getScore() != result_lowList.get(i).getScore()){
				check_lowList = false;
			}
		}
		assertTrue(check_lowList);
	}
	
	@Test
	public void testLearnUserBehaviour_4(){
		
		/* Construct expected result */
		ArrayList<ArrayList<RankEntry>> expected_highlistList = new ArrayList<ArrayList<RankEntry>>();
		ArrayList<ArrayList<RankEntry>> expected_lowlistList = new ArrayList<ArrayList<RankEntry>>();
		
		for(int i=0; i<2; i++){
			if(i == 0){
				ArrayList<RankEntry> expected_highList = new ArrayList<RankEntry>();
				
				RankEntry e = new RankEntry("PDF1", 4);
				e.setLastUsedParahraph(0);
				e.setLastUsedSentence(0);
				e.setScore(100);
				expected_highList.add((RankEntry)e.clone());
				e = new RankEntry("BLOGSPOT", 5);
				expected_highList.add((RankEntry)e.clone());
				e = new RankEntry("PDF2", 3);
				expected_highList.add((RankEntry)e.clone());
				expected_highlistList.add(expected_highList);
				
				ArrayList<RankEntry> expected_lowList = new ArrayList<RankEntry>();
				
				e = new RankEntry("WORDPRESS", 2);
				expected_lowList.add((RankEntry)e.clone());
				e = new RankEntry("WIKIPEDIA", 1);
				expected_lowList.add((RankEntry)e.clone());
				expected_lowlistList.add(expected_lowList);
			}else{
				ArrayList<RankEntry> expected_highList = new ArrayList<RankEntry>();
				
				RankEntry e = new RankEntry("BLOGSPOT", 5);
				expected_highList.add((RankEntry)e.clone());
				e = new RankEntry("PDF1", 4);
				expected_highList.add((RankEntry)e.clone());
				e = new RankEntry("PDF2", 3);
				expected_highList.add((RankEntry)e.clone());
				expected_highlistList.add(expected_highList);
				
				ArrayList<RankEntry> expected_lowList = new ArrayList<RankEntry>();
				
				e = new RankEntry("WORDPRESS", 2);
				expected_lowList.add((RankEntry)e.clone());
				e = new RankEntry("WIKIPEDIA", 1);
				expected_lowList.add((RankEntry)e.clone());
				expected_lowlistList.add(expected_lowList);
			}
			
		}
		
		/* Setting up the environment */
		rm.setDestinationDocument(destinationDocument1);
        rm.setDestinationDocument(destinationDocument2);
    	rm.addNewEntry("WIKIPEDIA");
		rm.addNewEntry("WORDPRESS");
		rm.addNewEntry("PDF2");
		rm.addNewEntry("PDF1");
		rm.addNewEntry("BLOGSPOT");
		rm.setDestinationDocument(destinationDocument1);
		
		/* execute the function to be tested */
		rm.learnUserBehaviour(0, 0, "PDF1");
		
		/* checking the result */
		ArrayList<ArrayList<RankEntry>> result_highlistList = rm.getHighlistList();
		ArrayList<ArrayList<RankEntry>> result_lowlistList = rm.getLowlistList();
		
		/* Checking size */
		assertTrue(result_highlistList.size() == (expected_highlistList.size()+1));
		assertTrue(result_lowlistList.size() == (expected_lowlistList.size()+1));
		
		for(int i=1; i<result_highlistList.size(); i++){
			ArrayList<RankEntry> result_highList = result_highlistList.get(i);
			ArrayList<RankEntry> expected_highList = expected_highlistList.get(i-1);
			assertTrue(result_highList.size() == expected_highList.size());
			
			ArrayList<RankEntry> result_lowList = result_lowlistList.get(i);
			ArrayList<RankEntry> expected_lowList = expected_lowlistList.get(i-1);
			assertTrue(result_lowList.size() == expected_lowList.size());
		}
		
		/* Checking the content */
		for(int i=1; i<result_highlistList.size(); i++){
			ArrayList<RankEntry> result_highList = result_highlistList.get(i);
			ArrayList<RankEntry> expected_highList = expected_highlistList.get(i-1);
			boolean check_highList = true;
			for(int j=0; j<expected_highList.size(); j++){
				if(!expected_highList.get(j).equals(result_highList.get(j)) || 
					expected_highList.get(j).getLastUsedParahraph() != result_highList.get(j).getLastUsedParahraph() ||
					expected_highList.get(j).getLastUsedSentence() != result_highList.get(j).getLastUsedSentence() ||
					expected_highList.get(j).getScore() != result_highList.get(j).getScore()){
						check_highList = false;
				}
			}
			assertTrue(check_highList);
			
			ArrayList<RankEntry> result_lowList = result_lowlistList.get(i);
			ArrayList<RankEntry> expected_lowList = expected_lowlistList.get(i-1);
			boolean check_lowList = true;
			for(int j=0; j<expected_lowList.size(); j++){
				if(!expected_lowList.get(j).equals(result_lowList.get(j)) || 
					expected_lowList.get(j).getLastUsedParahraph() != result_lowList.get(j).getLastUsedParahraph() ||
					expected_lowList.get(j).getLastUsedSentence() != result_lowList.get(j).getLastUsedSentence() ||
					expected_lowList.get(j).getScore() != result_lowList.get(j).getScore()){
						check_lowList = false;
				}
			}
			assertTrue(check_lowList);
		}
	}
	
	@Test
	public void testLearnUserBehaviour_5(){
		
		/* construct expected result */
		ArrayList<RankEntry> expected_highList = new ArrayList<RankEntry>();
		ArrayList<RankEntry> expected_lowList = new ArrayList<RankEntry>();
		
		RankEntry e = new RankEntry("BLOGSPOT", 5);
		expected_highList.add(e);
		e = new RankEntry("PDF1", 4);
		expected_highList.add(e);
		e = new RankEntry("PDF2", 3);
		expected_highList.add(e);
		
		e = new RankEntry("WIKIPEDIA", 1);
		e.setLastUsedParahraph(0);
		e.setLastUsedSentence(0);
		e.setScore(100);
		expected_lowList.add(e);
		e = new RankEntry("WORDPRESS", 2);
		expected_lowList.add(e);
		
		/* Setting up the environment */
		rm.setDestinationDocument(destinationDocument1);
		rm.addNewEntry("WIKIPEDIA");
		rm.addNewEntry("WORDPRESS");
		rm.addNewEntry("PDF2");
		rm.addNewEntry("PDF1");
		rm.addNewEntry("BLOGSPOT");
		
		/* execute the function to be tested */
		rm.learnUserBehaviour(0, 0, "WIKIPEDIA");
		
		/* checking the result */
		ArrayList<RankEntry> result_highList = rm.getHighlistList().get(1);
		ArrayList<RankEntry> result_lowList = rm.getLowlistList().get(1);
		
		assertTrue(expected_highList.size() == result_highList.size());
		assertTrue(expected_lowList.size() == result_lowList.size());
		
		boolean check_highList = true;
		for(int i=0; i<expected_highList.size(); i++){
			if(!expected_highList.get(i).equals(result_highList.get(i)) || 
				expected_highList.get(i).getLastUsedParahraph() != result_highList.get(i).getLastUsedParahraph() ||
				expected_highList.get(i).getLastUsedSentence() != result_highList.get(i).getLastUsedSentence() ||
				expected_highList.get(i).getScore() != result_highList.get(i).getScore()){
					check_highList = false;
			}
		}
		assertTrue(check_highList);
		
		boolean check_lowList = true;
		for(int i=0; i<expected_lowList.size(); i++){
			if(!expected_lowList.get(i).equals(result_lowList.get(i)) || 
				expected_lowList.get(i).getLastUsedParahraph() != result_lowList.get(i).getLastUsedParahraph() ||
				expected_lowList.get(i).getLastUsedSentence() != result_lowList.get(i).getLastUsedSentence() ||
				expected_lowList.get(i).getScore() != result_lowList.get(i).getScore()){
					check_lowList = false;
			}
		}
		assertTrue(check_lowList);
	}
	
	@Test
	public void testLearnUserBehaviour_6(){
		
		/* Construct expected result */
		ArrayList<ArrayList<RankEntry>> expected_highlistList = new ArrayList<ArrayList<RankEntry>>();
		ArrayList<ArrayList<RankEntry>> expected_lowlistList = new ArrayList<ArrayList<RankEntry>>();
		
		for(int i=0; i<2; i++){
			ArrayList<RankEntry> expected_highList = new ArrayList<RankEntry>();
			
			RankEntry e = new RankEntry("BLOGSPOT", 5);
			expected_highList.add((RankEntry)e.clone());
			e = new RankEntry("PDF1", 4);
			expected_highList.add((RankEntry)e.clone());
			e = new RankEntry("PDF2", 3);
			expected_highList.add((RankEntry)e.clone());
			expected_highlistList.add(expected_highList);
			
			ArrayList<RankEntry> expected_lowList = new ArrayList<RankEntry>();
			
			if(i == 0){
				e = new RankEntry("WIKIPEDIA", 1);
				e.setLastUsedParahraph(0);
				e.setLastUsedSentence(0);
				e.setScore(100);	
				expected_lowList.add((RankEntry)e.clone());
				e = new RankEntry("WORDPRESS", 2);
				expected_lowList.add((RankEntry)e.clone());
				expected_lowlistList.add(expected_lowList);
			}else{
				e = new RankEntry("WORDPRESS", 2);
				expected_lowList.add((RankEntry)e.clone());
				e = new RankEntry("WIKIPEDIA", 1);
				expected_lowList.add((RankEntry)e.clone());
				expected_lowlistList.add(expected_lowList);
			}
		}
		
		/* Setting up the environment */
		rm.setDestinationDocument(destinationDocument1);
        rm.setDestinationDocument(destinationDocument2);
    	rm.addNewEntry("WIKIPEDIA");
		rm.addNewEntry("WORDPRESS");
		rm.addNewEntry("PDF2");
		rm.addNewEntry("PDF1");
		rm.addNewEntry("BLOGSPOT");
		rm.setDestinationDocument(destinationDocument1);
		
		/* execute the function to be tested */
		rm.learnUserBehaviour(0, 0, "WIKIPEDIA");
		
		/* checking the result */
		ArrayList<ArrayList<RankEntry>> result_highlistList = rm.getHighlistList();
		ArrayList<ArrayList<RankEntry>> result_lowlistList = rm.getLowlistList();
		
		/* Checking size */
		assertTrue(result_highlistList.size() == (expected_highlistList.size()+1));
		assertTrue(result_lowlistList.size() == (expected_lowlistList.size()+1));
		
		for(int i=1; i<result_highlistList.size(); i++){
			ArrayList<RankEntry> result_highList = result_highlistList.get(i);
			ArrayList<RankEntry> expected_highList = expected_highlistList.get(i-1);
			assertTrue(result_highList.size() == expected_highList.size());
			
			ArrayList<RankEntry> result_lowList = result_lowlistList.get(i);
			ArrayList<RankEntry> expected_lowList = expected_lowlistList.get(i-1);
			assertTrue(result_lowList.size() == expected_lowList.size());
		}
		
		/* Checking the content */
		for(int i=1; i<result_highlistList.size(); i++){
			ArrayList<RankEntry> result_highList = result_highlistList.get(i);
			ArrayList<RankEntry> expected_highList = expected_highlistList.get(i-1);
			boolean check_highList = true;
			for(int j=0; j<expected_highList.size(); j++){
				if(!expected_highList.get(j).equals(result_highList.get(j)) || 
					expected_highList.get(j).getLastUsedParahraph() != result_highList.get(j).getLastUsedParahraph() ||
					expected_highList.get(j).getLastUsedSentence() != result_highList.get(j).getLastUsedSentence() ||
					expected_highList.get(j).getScore() != result_highList.get(j).getScore()){
						check_highList = false;
				}
			}
			assertTrue(check_highList);
			
			ArrayList<RankEntry> result_lowList = result_lowlistList.get(i);
			ArrayList<RankEntry> expected_lowList = expected_lowlistList.get(i-1);
			boolean check_lowList = true;
			for(int j=0; j<expected_lowList.size(); j++){
				if(!expected_lowList.get(j).equals(result_lowList.get(j)) || 
					expected_lowList.get(j).getLastUsedParahraph() != result_lowList.get(j).getLastUsedParahraph() ||
					expected_lowList.get(j).getLastUsedSentence() != result_lowList.get(j).getLastUsedSentence() ||
					expected_lowList.get(j).getScore() != result_lowList.get(j).getScore()){
						check_lowList = false;
				}
			}
			assertTrue(check_lowList);
		}
	}
	
	@Test
	public void testRemoveRankData_1(){
		
		/* construct expected result */
		ArrayList<RankEntry> expected_highList = new ArrayList<RankEntry>();
		ArrayList<RankEntry> expected_lowList = new ArrayList<RankEntry>();
		
		RankEntry e = new RankEntry("BLOGSPOT", 5);
		expected_highList.add(e);
		e = new RankEntry("PDF1", 4);
		expected_highList.add(e);
		e = new RankEntry("PDF2", 3);
		expected_highList.add(e);
		
		e = new RankEntry("WORDPRESS", 2);
		expected_lowList.add(e);
		e = new RankEntry("WIKIPEDIA", 1);
		expected_lowList.add(e);
		
		/* Setting up the environment */
		rm.addNewEntry("WIKIPEDIA");
		rm.addNewEntry("WORDPRESS");
		rm.addNewEntry("PDF2");
		rm.addNewEntry("PDF1");
		rm.addNewEntry("BLOGSPOT");
		rm.setDestinationDocument(destinationDocument1);
		
		/* execute the function to be tested */
		rm.removeRankData("ABC");
		
		/* checking the result */
		ArrayList<RankEntry> result_highList = rm.getHighlistList().get(1);
		ArrayList<RankEntry> result_lowList = rm.getLowlistList().get(1);
		
		assertTrue(expected_highList.size() == result_highList.size());
		assertTrue(expected_lowList.size() == result_lowList.size());
		
		boolean check_highList = true;
		for(int i=0; i<expected_highList.size(); i++){
			if(!expected_highList.get(i).equals(result_highList.get(i))){
				check_highList = false;
			}
		}
		assertTrue(check_highList);
		
		boolean check_lowList = true;
		for(int i=0; i<expected_lowList.size(); i++){
			if(!expected_lowList.get(i).equals(result_lowList.get(i))){
				check_lowList = false;
			}
		}
		assertTrue(check_lowList);
	}
	
	@Test
	public void testRemoveRankData_2(){
		
		/* construct expected result */
		ArrayList<RankEntry> expected_highList = new ArrayList<RankEntry>();
		ArrayList<RankEntry> expected_lowList = new ArrayList<RankEntry>();
		
		RankEntry e = new RankEntry("BLOGSPOT", 5);
		expected_highList.add(e);
		e = new RankEntry("PDF2", 3);
		expected_highList.add(e);
		e = new RankEntry("WORDPRESS", 2);
		expected_highList.add(e);
		
		e = new RankEntry("WIKIPEDIA", 1);
		expected_lowList.add(e);
		
		/* Setting up the environment */
		rm.addNewEntry("WIKIPEDIA");
		rm.addNewEntry("WORDPRESS");
		rm.addNewEntry("PDF2");
		rm.addNewEntry("PDF1");
		rm.addNewEntry("BLOGSPOT");
		rm.setDestinationDocument(destinationDocument1);
		
		/* execute the function to be tested */
		rm.removeRankData("PDF1");
		
		/* checking the result */
		ArrayList<RankEntry> result_highList = rm.getHighlistList().get(1);
		ArrayList<RankEntry> result_lowList = rm.getLowlistList().get(1);
		
		assertTrue(expected_highList.size() == result_highList.size());
		assertTrue(expected_lowList.size() == result_lowList.size());
		
		boolean check_highList = true;
		for(int i=0; i<expected_highList.size(); i++){
			if(!expected_highList.get(i).equals(result_highList.get(i))){
				check_highList = false;
			}
		}
		assertTrue(check_highList);
		
		boolean check_lowList = true;
		for(int i=0; i<expected_lowList.size(); i++){
			if(!expected_lowList.get(i).equals(result_lowList.get(i))){
				check_lowList = false;
			}
		}
		assertTrue(check_lowList);
	}
	
	@Test
	public void testRemoveRankData_3(){
		
		/* Construct expected result */
		ArrayList<ArrayList<RankEntry>> expected_highlistList = new ArrayList<ArrayList<RankEntry>>();
		ArrayList<ArrayList<RankEntry>> expected_lowlistList = new ArrayList<ArrayList<RankEntry>>();
		
		for(int i=0; i<2; i++){
			ArrayList<RankEntry> expected_highList = new ArrayList<RankEntry>();

			RankEntry e = new RankEntry("BLOGSPOT", 5);
			expected_highList.add((RankEntry)e.clone());
			e = new RankEntry("PDF2", 3);
			expected_highList.add((RankEntry)e.clone());
			e = new RankEntry("WORDPRESS", 2);
			expected_highList.add((RankEntry)e.clone());
			expected_highlistList.add(expected_highList);

			ArrayList<RankEntry> expected_lowList = new ArrayList<RankEntry>();

			e = new RankEntry("WIKIPEDIA", 1);
			expected_lowList.add((RankEntry)e.clone());
			expected_lowlistList.add(expected_lowList);
		}
		
		/* Setting up the environment */
		rm.setDestinationDocument(destinationDocument1);
        rm.setDestinationDocument(destinationDocument2);
    	rm.addNewEntry("WIKIPEDIA");
		rm.addNewEntry("WORDPRESS");
		rm.addNewEntry("PDF2");
		rm.addNewEntry("PDF1");
		rm.addNewEntry("BLOGSPOT");
		
		/* execute the function to be tested */
		rm.removeRankData("PDF1");
		
		/* checking the result */
		ArrayList<ArrayList<RankEntry>> result_highlistList = rm.getHighlistList();
		ArrayList<ArrayList<RankEntry>> result_lowlistList = rm.getLowlistList();
		
		/* Checking size */
		assertTrue(result_highlistList.size() == (expected_highlistList.size()+1));
		assertTrue(result_lowlistList.size() == (expected_lowlistList.size()+1));
		
		for(int i=1; i<result_highlistList.size(); i++){
			ArrayList<RankEntry> result_highList = result_highlistList.get(i);
			ArrayList<RankEntry> expected_highList = expected_highlistList.get(i-1);
			assertTrue(result_highList.size() == expected_highList.size());
			
			ArrayList<RankEntry> result_lowList = result_lowlistList.get(i);
			ArrayList<RankEntry> expected_lowList = expected_lowlistList.get(i-1);
			assertTrue(result_lowList.size() == expected_lowList.size());
		}
		
		/* Checking the content */
		for(int i=1; i<result_highlistList.size(); i++){
			ArrayList<RankEntry> result_highList = result_highlistList.get(i);
			ArrayList<RankEntry> expected_highList = expected_highlistList.get(i-1);
			boolean check_highList = true;
			for(int j=0; j<expected_highList.size(); j++){
				if(!expected_highList.get(j).equals(result_highList.get(j))){
						check_highList = false;
				}
			}
			assertTrue(check_highList);
			
			ArrayList<RankEntry> result_lowList = result_lowlistList.get(i);
			ArrayList<RankEntry> expected_lowList = expected_lowlistList.get(i-1);
			boolean check_lowList = true;
			for(int j=0; j<expected_lowList.size(); j++){
				if(!expected_lowList.get(j).equals(result_lowList.get(j))){
						check_lowList = false;
				}
			}
			assertTrue(check_lowList);
		}
	}
	
	@Test
	public void testRemoveRankData_4(){
		
		/* construct expected result */
		ArrayList<RankEntry> expected_highList = new ArrayList<RankEntry>();
		ArrayList<RankEntry> expected_lowList = new ArrayList<RankEntry>();
		
		RankEntry e = new RankEntry("BLOGSPOT", 5);
		expected_highList.add(e);
		e = new RankEntry("PDF1", 4);
		expected_highList.add(e);
		e = new RankEntry("PDF2", 3);
		expected_highList.add(e);
		
		e = new RankEntry("WIKIPEDIA", 1);
		expected_lowList.add(e);
		
		/* Setting up the environment */
		rm.addNewEntry("WIKIPEDIA");
		rm.addNewEntry("WORDPRESS");
		rm.addNewEntry("PDF2");
		rm.addNewEntry("PDF1");
		rm.addNewEntry("BLOGSPOT");
		rm.setDestinationDocument(destinationDocument1);
		
		/* execute the function to be tested */
		rm.removeRankData("WORDPRESS");
		
		/* checking the result */
		ArrayList<RankEntry> result_highList = rm.getHighlistList().get(1);
		ArrayList<RankEntry> result_lowList = rm.getLowlistList().get(1);
		
		assertTrue(expected_highList.size() == result_highList.size());
		assertTrue(expected_lowList.size() == result_lowList.size());
		
		boolean check_highList = true;
		for(int i=0; i<expected_highList.size(); i++){
			if(!expected_highList.get(i).equals(result_highList.get(i))){
				check_highList = false;
			}
		}
		assertTrue(check_highList);
		
		boolean check_lowList = true;
		for(int i=0; i<expected_lowList.size(); i++){
			if(!expected_lowList.get(i).equals(result_lowList.get(i))){
				check_lowList = false;
			}
		}
		assertTrue(check_lowList);
	}
	
	@Test
	public void testRemoveRankData_5(){
		
		/* Construct expected result */
		ArrayList<ArrayList<RankEntry>> expected_highlistList = new ArrayList<ArrayList<RankEntry>>();
		ArrayList<ArrayList<RankEntry>> expected_lowlistList = new ArrayList<ArrayList<RankEntry>>();
		
		for(int i=0; i<2; i++){
			ArrayList<RankEntry> expected_highList = new ArrayList<RankEntry>();

			RankEntry e = new RankEntry("BLOGSPOT", 5);
			expected_highList.add((RankEntry)e.clone());
			e = new RankEntry("PDF1", 4);
			expected_highList.add((RankEntry)e.clone());
			e = new RankEntry("PDF2", 3);
			expected_highList.add((RankEntry)e.clone());
			expected_highlistList.add(expected_highList);

			ArrayList<RankEntry> expected_lowList = new ArrayList<RankEntry>();

			e = new RankEntry("WIKIPEDIA", 1);
			expected_lowList.add((RankEntry)e.clone());
			expected_lowlistList.add(expected_lowList);
		}
		
		/* Setting up the environment */
		rm.setDestinationDocument(destinationDocument1);
        rm.setDestinationDocument(destinationDocument2);
    	rm.addNewEntry("WIKIPEDIA");
		rm.addNewEntry("WORDPRESS");
		rm.addNewEntry("PDF2");
		rm.addNewEntry("PDF1");
		rm.addNewEntry("BLOGSPOT");
		
		/* execute the function to be tested */
		rm.removeRankData("WORDPRESS");
		
		/* checking the result */
		ArrayList<ArrayList<RankEntry>> result_highlistList = rm.getHighlistList();
		ArrayList<ArrayList<RankEntry>> result_lowlistList = rm.getLowlistList();
		
		/* Checking size */
		assertTrue(result_highlistList.size() == (expected_highlistList.size()+1));
		assertTrue(result_lowlistList.size() == (expected_lowlistList.size()+1));
		
		for(int i=1; i<result_highlistList.size(); i++){
			ArrayList<RankEntry> result_highList = result_highlistList.get(i);
			ArrayList<RankEntry> expected_highList = expected_highlistList.get(i-1);
			assertTrue(result_highList.size() == expected_highList.size());
			
			ArrayList<RankEntry> result_lowList = result_lowlistList.get(i);
			ArrayList<RankEntry> expected_lowList = expected_lowlistList.get(i-1);
			assertTrue(result_lowList.size() == expected_lowList.size());
		}
		
		/* Checking the content */
		for(int i=1; i<result_highlistList.size(); i++){
			ArrayList<RankEntry> result_highList = result_highlistList.get(i);
			ArrayList<RankEntry> expected_highList = expected_highlistList.get(i-1);
			boolean check_highList = true;
			for(int j=0; j<expected_highList.size(); j++){
				if(!expected_highList.get(j).equals(result_highList.get(j))){
						check_highList = false;
				}
			}
			assertTrue(check_highList);
			
			ArrayList<RankEntry> result_lowList = result_lowlistList.get(i);
			ArrayList<RankEntry> expected_lowList = expected_lowlistList.get(i-1);
			boolean check_lowList = true;
			for(int j=0; j<expected_lowList.size(); j++){
				if(!expected_lowList.get(j).equals(result_lowList.get(j))){
						check_lowList = false;
				}
			}
			assertTrue(check_lowList);
		}
	}
	
	@Test
	public void testRemoveAllRankData_1(){
		/* construct expected result */
		ArrayList<ArrayList<RankEntry>> expected_highlistList = new ArrayList<ArrayList<RankEntry>>();
		ArrayList<ArrayList<RankEntry>> expected_lowlistList = new ArrayList<ArrayList<RankEntry>>();
		
		/* Setting up the environment */
		// empty environment
		
		/* execute the function to be tested */
		rm.removeAllRankData();
		
		/* checking the result */
		ArrayList<ArrayList<RankEntry>> result_highlistList = rm.getHighlistList();
		ArrayList<ArrayList<RankEntry>> result_lowlistList = rm.getLowlistList();
		
		assertTrue(expected_highlistList.size() == result_highlistList.size()-1); 	// first list is used as a reference list for initialization of new destination document
		assertTrue(expected_lowlistList.size() == result_lowlistList.size()-1); 	// first list is used as a reference list for initialization of new destination document
	}
	
	@Test
	public void testRemoveAllRankData_2(){
		/* construct expected result */
		ArrayList<RankEntry> expected_highList = new ArrayList<RankEntry>();
		ArrayList<RankEntry> expected_lowList = new ArrayList<RankEntry>();
		
		/* Setting up the environment */
		rm.addNewEntry("WIKIPEDIA");
		rm.addNewEntry("WORDPRESS");
		rm.addNewEntry("PDF2");
		rm.addNewEntry("PDF1");
		rm.addNewEntry("BLOGSPOT");
		rm.setDestinationDocument(destinationDocument1);
		
		/* execute the function to be tested */
		rm.removeAllRankData();
		
		/* checking the result */
		ArrayList<RankEntry> result_highList = rm.getHighlistList().get(1);
		ArrayList<RankEntry> result_lowList = rm.getLowlistList().get(1);
		
		assertTrue(expected_highList.size() == result_highList.size());
		assertTrue(expected_lowList.size() == result_lowList.size());
	}
	
	@Test
	public void testRemoveAllRankData_3(){
		
		/* Construct expected result */
		ArrayList<ArrayList<RankEntry>> expected_highlistList = new ArrayList<ArrayList<RankEntry>>();
		ArrayList<ArrayList<RankEntry>> expected_lowlistList = new ArrayList<ArrayList<RankEntry>>();
		
		for(int i=0; i<2; i++){
			ArrayList<RankEntry> expected_highList = new ArrayList<RankEntry>();
			expected_highlistList.add(expected_highList);

			ArrayList<RankEntry> expected_lowList = new ArrayList<RankEntry>();
			expected_lowlistList.add(expected_lowList);
		}
		
		/* Setting up the environment */
		rm.setDestinationDocument(destinationDocument1);
        rm.setDestinationDocument(destinationDocument2);
    	rm.addNewEntry("WIKIPEDIA");
		rm.addNewEntry("WORDPRESS");
		rm.addNewEntry("PDF2");
		rm.addNewEntry("PDF1");
		rm.addNewEntry("BLOGSPOT");
		
		/* execute the function to be tested */
		rm.removeAllRankData();
		
		/* checking the result */
		ArrayList<ArrayList<RankEntry>> result_highlistList = rm.getHighlistList();
		ArrayList<ArrayList<RankEntry>> result_lowlistList = rm.getLowlistList();
		
		/* Checking size */
		assertTrue(result_highlistList.size() == (expected_highlistList.size()+1));
		assertTrue(result_lowlistList.size() == (expected_lowlistList.size()+1));
		
		for(int i=1; i<result_highlistList.size(); i++){
			ArrayList<RankEntry> result_highList = result_highlistList.get(i);
			ArrayList<RankEntry> expected_highList = expected_highlistList.get(i-1);
			assertTrue(result_highList.size() == expected_highList.size());
			
			ArrayList<RankEntry> result_lowList = result_lowlistList.get(i);
			ArrayList<RankEntry> expected_lowList = expected_lowlistList.get(i-1);
			assertTrue(result_lowList.size() == expected_lowList.size());
		}
	}
	
	@Test
	public void testSetDestinationDocument_1(){
		
		/* Construct expected result */
		ArrayList<Integer> expected_destinationDocumentList = new ArrayList<Integer>();
		expected_destinationDocumentList.add(1122);
		
		/* Setting up the environment */
		
		/* execute the function to be tested */
		rm.setDestinationDocument(1122);
		
		/* checking the result */
		ArrayList<Integer> result_destinationDocumentList = rm.getDestinationDocumentList();
		
		assertTrue(result_destinationDocumentList.size() == expected_destinationDocumentList.size());
		
		for(int i=0; i<result_destinationDocumentList.size(); i++){
			assertTrue(Integer.compare(result_destinationDocumentList.get(i), expected_destinationDocumentList.get(i)) == 0);
		}
	}
	
	@Test
	public void testSetDestinationDocument_2(){
		
		/* Construct expected result */
		ArrayList<Integer> expected_destinationDocumentList = new ArrayList<Integer>();
		expected_destinationDocumentList.add(1122);
		
		/* Setting up the environment */
		rm.setDestinationDocument(1122);
		
		/* execute the function to be tested */
		rm.setDestinationDocument(1122);
		
		/* checking the result */
		ArrayList<Integer> result_destinationDocumentList = rm.getDestinationDocumentList();
		
		assertTrue(result_destinationDocumentList.size() == expected_destinationDocumentList.size());
		
		for(int i=0; i<result_destinationDocumentList.size(); i++){
			assertTrue(Integer.compare(result_destinationDocumentList.get(i), expected_destinationDocumentList.get(i)) == 0);
		}
	}
	
	@Test
	public void testSetDestinationDocument_3(){
		
		/* Construct expected result */
		ArrayList<Integer> expected_destinationDocumentList = new ArrayList<Integer>();
		expected_destinationDocumentList.add(1122);
		
		ArrayList<RankEntry> expected_highList = new ArrayList<RankEntry>();
		ArrayList<RankEntry> expected_lowList = new ArrayList<RankEntry>();
		
		RankEntry e = new RankEntry("BLOGSPOT", 5);
		expected_highList.add(e);
		e = new RankEntry("PDF1", 4);
		expected_highList.add(e);
		e = new RankEntry("PDF2", 3);
		expected_highList.add(e);
		
		e = new RankEntry("WORDPRESS", 2);
		expected_lowList.add(e);
		e = new RankEntry("WIKIPEDIA", 1);
		expected_lowList.add(e);
		
		/* Setting up the environment */
		rm.addNewEntry("WIKIPEDIA");
		rm.addNewEntry("WORDPRESS");
		rm.addNewEntry("PDF2");
		rm.addNewEntry("PDF1");
		rm.addNewEntry("BLOGSPOT");
		
		/* execute the function to be tested */
		rm.setDestinationDocument(1122);
		
		/* checking the result */
		ArrayList<Integer> result_destinationDocumentList = rm.getDestinationDocumentList();
		
		assertTrue(result_destinationDocumentList.size() == expected_destinationDocumentList.size());
		
		for(int i=0; i<result_destinationDocumentList.size(); i++){
			assertTrue(Integer.compare(result_destinationDocumentList.get(i), expected_destinationDocumentList.get(i)) == 0);
		}
		
		ArrayList<RankEntry> result_highList = rm.getHighlistList().get(1);
		ArrayList<RankEntry> result_lowList = rm.getLowlistList().get(1);
		
		assertTrue(expected_highList.size() == result_highList.size());
		assertTrue(expected_lowList.size() == result_lowList.size());
		
		boolean check_highList = true;
		for(int i=0; i<expected_highList.size(); i++){
			if(!expected_highList.get(i).equals(result_highList.get(i))){
				check_highList = false;
			}
		}
		assertTrue(check_highList);
		
		boolean check_lowList = true;
		for(int i=0; i<expected_lowList.size(); i++){
			if(!expected_lowList.get(i).equals(result_lowList.get(i))){
				check_lowList = false;
			}
		}
		assertTrue(check_lowList);
	}
}
