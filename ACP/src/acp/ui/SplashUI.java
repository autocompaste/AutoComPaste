package acp.ui;

import java.awt.*;
import java.awt.event.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import acp.background.ClientWorker;

public class SplashUI extends Frame {
	public enum LoadingState{
		LOADING_LIBRARIES,
		LOADING_SERVER_BACKGROUND
	}
	
	private Graphics2D g;
	private SplashScreen splash;
	private final String[] comps = {"Starting ACP", "Loading Libraries", "Starting Server and Background process"};
	private final static Logger logger = LoggerFactory.getLogger(SplashUI.class);
	private boolean enabled;
	private FontMetrics fm;
	
	public SplashUI() {
		initSplash();
	}

	public void initSplash(){
		splash = SplashScreen.getSplashScreen();
		if (splash == null) {
			//logger.error("Unable to load the splash image");
			enabled = false;
			return;
		}
		else{
			enabled = true;
		}
		g = splash.createGraphics();
		if (g == null) {
			System.out.println("g is null");
			return;
		}
		renderSplashFrame(g, 0);
	}
    
	public void updateSplash(LoadingState state){
		int index=0;
		
		switch(state){
		case LOADING_LIBRARIES:
			index=1;
			break;
		case LOADING_SERVER_BACKGROUND:
			index=2;
			break;
		}
		
		if(enabled){
			renderSplashFrame(g, index);
		    splash.update();
		}
	}
	
	public void renderSplashFrame(Graphics2D g, int index) {    	
		if(enabled){
			if(fm == null){
				fm = g.getFontMetrics();
			}
			
			String line = comps[index] + "...";
			
			g.setComposite(AlphaComposite.Clear);
			g.fillRect(120,240,300,40);
			g.setPaintMode();
			g.setColor(Color.WHITE);
			g.drawString(line, (400-fm.stringWidth(line))/2, 250);
		}
	}
    
	public void closeSplash(){
		if(enabled){
			splash.close();
		}
 	}
}