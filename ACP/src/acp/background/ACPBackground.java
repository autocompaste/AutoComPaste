package acp.background;

import acp.ACPLogic;
import acp.ACPLogic.State;

import com.sun.jna.*; 
import com.sun.jna.platform.win32.WinDef.DWORD;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.platform.win32.WinDef.HWND; 
import com.sun.jna.platform.win32.WinUser.WNDENUMPROC;
import com.sun.jna.win32.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

/**
 * ACPBackground is a thread that acts as a sensor for Machine learning algorithm; 
 * That is, extract the foreground window title that the user is viewing.
 * ACPBackground also detect the closing of firefox to trigger the clearing of data(database and MLdata).
 * 
 * @author Ng Chin Hui
 */
public class ACPBackground implements Runnable{

	private ACPLogic logic;
	private boolean isFirefoxLaunched;
	
	/**
	 * Basic constructor for ACPBackground process thread
	 */
	public ACPBackground(){
		isFirefoxLaunched = false;
		this.logic = ACPLogic.getInstance();
	}
	
	/**
	 * Interface for defining the methods that are used from the user32.dll
	 */
	private interface User32 extends StdCallLibrary { 
		User32 INSTANCE = (User32) Native.loadLibrary("user32", User32.class); 
		HWND GetForegroundWindow(); 
		boolean EnumWindows(WinUser.WNDENUMPROC lpEnumFunc, Pointer arg); 
		int GetWindowTextA(PointerType hWnd, byte[] lpString, int nMaxCount); 	
		boolean EnumChildWindows(HWND hWnd, WinUser.WNDENUMPROC lpEnumFunc, Pointer arg);
	}
	
	/**
	 * Extracts the title of the foreground window (active / focused window)
	 * 
	 * @return the title of the foreground window
	 */
	private String getWindowTitle(){
		byte[] windowText = new byte[512]; 				
		PointerType hwnd = User32.INSTANCE.GetForegroundWindow();
		User32.INSTANCE.GetWindowTextA(hwnd, windowText, 512); 
		return Native.toString(windowText);	
	}
	
	/**
	 * Changes the rank of the source document
	 * 
	 */
	private void detectSwitchDocument(String source){
		logic.changeRanks(source);
	}
	
	/**
	 * Gets the list of active processes and checks whether firefox.exe is active or not
	 * true - firefox.exe is active
	 * false - firefox.exe is inactive
	 * 
	 * @return the result of this operation
	 */
	private boolean detectMozillaFireFox(){
		boolean result = false;
		try {
			String line;
			Process p = Runtime.getRuntime().exec(System.getenv("windir") + "\\system32\\" + "tasklist.exe");
			BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
			while((line = input.readLine()) != null){
				if(line.contains("firefox.exe")){
					result = true;
				}
			}
			input.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	public void run(){		
		while(true){
			try{
				Thread.sleep(700); // 0.7s poll
				
				String sourceName = getWindowTitle();
				if(sourceName.contains("Mozilla Firefox")) {
					detectSwitchDocument(sourceName);
				}
				
				if(detectMozillaFireFox() == false && isFirefoxLaunched == true){
					logic.clearAllData();
					isFirefoxLaunched = false;
				}else if(detectMozillaFireFox() == true && isFirefoxLaunched == false){
					isFirefoxLaunched = true;
				}
				
			}catch (InterruptedException e) {
				e.printStackTrace();
			}			
		}		
	}
}
