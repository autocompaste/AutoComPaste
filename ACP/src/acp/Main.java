package acp;

import java.util.Properties;
import javax.swing.UIManager;
import acp.background.ACPBackground;
import acp.background.Server;
import acp.ui.SplashUI;
import acp.ui.SystrayUI;

/**
 * This is the main class for Java ACP. It contains the application's execution entry point.
 * It triggers the display of Splash UI, initials ACPLogic class, spawns 2 threads
 * to handle the network and the background process, and adds ACP main process to the
 * system tray.
 * 
 * @author Loke Yan Hao
 */
public class Main {
	public static void main(String[] args){		
		SplashUI splash = new SplashUI();
		
		ACPLogic logic = ACPLogic.getInstance();
		splash.updateSplash(SplashUI.LoadingState.LOADING_LIBRARIES);
		logic.initialiseLibraries(); // Init all libraries like Hibernate and Gate
		logic.clearAllData(); 
		
		// Two separate threads will be created: 
		// listen to client connection, poll to detect windows launch/close
		splash.updateSplash(SplashUI.LoadingState.LOADING_SERVER_BACKGROUND);
		ACPBackground background = new ACPBackground();
		Server server = new Server(5566);
		
		Thread backgroundThread = new Thread(background);
		Thread serverThread = new Thread(server);
		
		// Start the two background threads
		backgroundThread.start();
		serverThread.start();
		
		// Set Windows Look and Feel for Java Swing
		try {
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
	    } catch (Exception e) {
	    	e.printStackTrace();
	    }
		
		// Main thread will be handling the preference UI
		SystrayUI systrayUI = new SystrayUI();
		splash.closeSplash();
		
		// To allow systrayUI to observe ACP Logic state changes
		logic.addObserver(systrayUI);
		
		// Debug purpose only
		logic.triggerDebugUI();
	}
}
