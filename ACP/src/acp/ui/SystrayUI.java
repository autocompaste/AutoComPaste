package acp.ui;

import java.awt.AWTException;
import java.awt.CheckboxMenuItem;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.TrayIcon.MessageType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Observable;
import java.util.Observer;

import javax.swing.UIManager;

import acp.ACPLogic;
import acp.beans.Preference;

public class SystrayUI implements Observer{
	private ACPLogic logic;
	private PreferenceUI preferenceUI;
	private ExitUI exitUI;
	private Image image, image2;
	private TrayIcon trayIcon;
	private CheckboxMenuItem enableItem;
	
	public SystrayUI(){
		this.logic = ACPLogic.getInstance();
		preferenceUI = new PreferenceUI(logic);
		exitUI = new ExitUI();
		
		initialSystray();
	}
	
	private void initialSystray(){
		if (SystemTray.isSupported()) {
			SystemTray tray = SystemTray.getSystemTray();
			image = Toolkit.getDefaultToolkit().getImage("icon.png");
			image2 = Toolkit.getDefaultToolkit().getImage("icon2.gif");
		            
		    PopupMenu popup = new PopupMenu();
		    MenuItem defaultItem = new MenuItem("Exit");
		    defaultItem.addActionListener(new ActionListener() {
		        public void actionPerformed(ActionEvent e) {
		            exitUI.exitACP(logic);
		        }
		    });
		    
		    enableItem = new CheckboxMenuItem("Enable ACP");
		    enableItem.setState(preferenceUI.isEnableDisable());
		    enableItem.addItemListener(new ItemListener(){
				@Override
				public void itemStateChanged(ItemEvent arg0) {
					preferenceUI.setEnableDisable(enableItem.getState());
				}
		    });
		    
		    MenuItem preferencesItem = new MenuItem("Preferences");
		    preferencesItem.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent arg0) {
					preferenceUI.loadPreference();
					preferenceUI.setVisible(true);
				}
		    });
		    
		    popup.add(enableItem);
		    popup.add(preferencesItem);
		    popup.addSeparator();
		    popup.add(defaultItem);

		    trayIcon = new TrayIcon(image, "AutoComPaste", popup);

		    trayIcon.setImageAutoSize(true);

		    try {
		        tray.add(trayIcon);
		        preferenceUI.addObserver(this);
		        trayIcon.displayMessage("", "AutoComPaste has started!", MessageType.NONE);
		    } catch (AWTException e) {
		        System.err.println("TrayIcon could not be added.");
		    }
		} else {
			System.err.println("System Tray not supported");
		}
	}

	@Override
	public void update(Observable arg0, Object arg1) {
		if(arg0 instanceof ACPLogic){
		ACPLogic.State mode = ((ACPLogic)arg0).getCurrentState();
		if(mode == ACPLogic.State.IDLE)
				trayIcon.setImage(image);
		else if(mode == ACPLogic.State.PROCESSING)
				trayIcon.setImage(image2);
		}else{
			boolean isEnableDisable = ((Preference)arg0).isEnableDisable();
			if(isEnableDisable){
				enableItem.setState(true);
				trayIcon.displayMessage("", "ACP is Enabled!", MessageType.NONE);
			}else{
				enableItem.setState(false);
				trayIcon.displayMessage("", "ACP is Disabled!", MessageType.NONE);
			}
			
		}
	}
}
