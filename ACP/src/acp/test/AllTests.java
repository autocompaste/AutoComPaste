package acp.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class) 
@SuiteClasses({ ACPLogicTest.class, DataManagerTest.class, DataStoreTest.class,
		RankManagerTest.class, RankDataStoreTest.class, NLPTest.class,
		PreferenceManagerTest.class, ServerTest.class, SortManagerTest.class })
public class AllTests {

}
