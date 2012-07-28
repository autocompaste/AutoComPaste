package acp.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import acp.ACPLogic;
import acp.beans.Preference;

import com.melloware.jintellitype.HotkeyListener;
import com.melloware.jintellitype.JIntellitype;

/**
 * This is the presentation layer for displaying the preference setting.
 * 
 * @author Loke Yan Hao, Ng Chin Hui
 *
 */
public class PreferenceUI extends JFrame implements KeyListener, HotkeyListener{
	
	final static Logger logger = LoggerFactory.getLogger(PreferenceUI.class);
	
	private GridBagConstraints inputConstraints = null;
    private GridBagConstraints labelConstraints = null;
    
    // settings tab
    private JComboBox<Integer> entriesInViewInput;
    private JSpinner thresholdInput;
    private ButtonGroup modeGroup;
	private JRadioButton manualInput;
	private JRadioButton autoInput;
	private JCheckBox startACPInput;
	private JSlider triggerDelaySlider;
	
	// hotkeys tab
	private JTextField triggerSuggestionPopUp;
	private JTextField enableACP;
	private JTextField extendWord, reduceWord;
	private JTextField extendSentence, reduceSentence;
	private JTextField extendParagraph, reduceParagraph;
	
	// default hotkeys
	public final static String DEFAULT_HOTKEY_TRIGGERSUGGESTIONPOPUP = "F9";
	public final static String DEFAULT_HOTKEY_ENABLEACP = "CTRL + D";
	public final static String DEFAULT_HOTKEY_EXTENDWORD = "CTRL + PERIOD";
	public final static String DEFAULT_HOTKEY_REDUCEWORD = "CTRL + COMMA";
	public final static String DEFAULT_HOTKEY_EXTENDSENTENCE = "CTRL + RIGHT";
	public final static String DEFAULT_HOTKEY_REDUCESENTENCE = "CTRL + LEFT";
	public final static String DEFAULT_HOTKEY_EXTENDPARAGRAPH = "CTRL + DOWN";
	public final static String DEFAULT_HOTKEY_REDUCEPARAGRAPH = "CTRL + UP";
	
	// Default Configuration
	public final static int DEFAULT_ENTRIES_IN_VIEW = 6;
	public final static int DEFAULT_THRESHOLD = 20;
	public final static int DEFAULT_TRIGGER_DELAY = 500;
	
	// Default Delay Trigger Slider value
	static final int FPS_MIN = 100;
	static final int FPS_MAX = 900;
	
	// bottom panel components
	private JButton okBut;
	private JButton applyBut;
	private Preference preference;
	private ACPLogic logic;
	
	boolean test = true;
	
	public PreferenceUI(ACPLogic logic){
		this.logic = logic;
		
		initialUI();
	}
	
	public void loadPreference(){
		// Load preference from ACP.ini
		preference = logic.getUserPreference();				
		initialPreference(preference);
	}
	
	private void initialUI(){
		
		setTitle("AutoComPaste Preferences");
		setSize(595,500);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent winEvt){
				setVisible(false);
			}
		});
		
		setLayout(new BorderLayout());
		
		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.setTabPlacement(JTabbedPane.LEFT);
		
		createBottomPanel();
		createSettingsPanel(tabbedPane);
		createHotkeyPanel(tabbedPane);
		
		loadPreference();
		setUpJIntellitypeFile();
		setUpSystemKeyListener();
		JIntellitype.getInstance().addHotKeyListener(this); // can only add once
		
		add(tabbedPane, BorderLayout.CENTER);
		
		detectApplyButStatus();
	}
		
	private void createSettingsPanel(JTabbedPane tabbedPane){
		String[] groupLabels = {"System Settings", "Suggestion Mode"};
		String[] labels = {"Number of Entries in View: ", "      Threshold for auto-complete: ", "      Trigger Delay: ", "Start ACP when computer starts"};
		String[] modeLabels = {"Automatic Trigger - Suggestion appears when threshold is reach.", "Manual Trigger - Suggestion appears only when trigger manually with hotkey."};
		
		JPanel settingsPanel = new JPanel(false);
		tabbedPane.addTab(getTabFormat("Settings"), settingsPanel);
		tabbedPane.setMnemonicAt(0, KeyEvent.VK_1);
		
		settingsPanel.setLayout(new BorderLayout());
		
		JPanel form = new JPanel();
		form.setLayout(new GridBagLayout()); 
		
		initialGridBagConstraints();
		
		TitledBorder sysSettingsBorder = new TitledBorder(new EtchedBorder(), groupLabels[0]);
		JPanel sysSettingsGroup = new JPanel();
		sysSettingsGroup.setLayout(new GridBagLayout());
		sysSettingsGroup.setBorder(sysSettingsBorder);
		
		TitledBorder suggestionModeBorder = new TitledBorder(new EtchedBorder(), groupLabels[1]);
		JPanel suggestionModeGroup = new JPanel();
		suggestionModeGroup.setLayout(new GridBagLayout());
		suggestionModeGroup.setBorder(suggestionModeBorder);
		
		JPanel setDefaultPanel = new JPanel();
		setDefaultPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		JButton setDefaultBut = new JButton("Set Default");
		setDefaultBut.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				defaultSettings();
				detectApplyButStatus();
			}
		});
		setDefaultPanel.add(setDefaultBut);
		
		// initialising the components
		entriesInViewInput = new JComboBox<Integer>();
		int num[] = {4,5,6,7,8,9};
		for(int i : num)
			entriesInViewInput.addItem(i);
		entriesInViewInput.addItemListener(new ItemListener(){
			@Override
			public void itemStateChanged(ItemEvent arg0) {
				detectApplyButStatus();
			}
		});
		
		thresholdInput = new JSpinner(new SpinnerNumberModel());
		thresholdInput.setValue(DEFAULT_TRIGGER_DELAY);
		thresholdInput.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
				detectApplyButStatus();			
			}
		});
		
		modeGroup = new ButtonGroup();
		autoInput = new JRadioButton(modeLabels[0]);
		autoInput.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				detectApplyButStatus();		
				thresholdInput.setEnabled(true);
				triggerDelaySlider.setEnabled(true);
			}
		});
		
		manualInput = new JRadioButton(modeLabels[1]);
		manualInput.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				detectApplyButStatus();			
				thresholdInput.setEnabled(false);
				triggerDelaySlider.setEnabled(false);
			}
		});
		
		modeGroup.add(manualInput);
		modeGroup.add(autoInput);
		
		startACPInput = new JCheckBox(labels[3], true);
		startACPInput.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				detectApplyButStatus();
			}
		});
		
		JPanel triggerDelayPanel = new JPanel();
		JLabel triggerDelayLabel1 = new JLabel("Slow");
		JLabel triggerDelayLabel2 = new JLabel("Fast");
		
		triggerDelaySlider = new JSlider(JSlider.HORIZONTAL,
		                                      FPS_MIN, FPS_MAX, DEFAULT_TRIGGER_DELAY);
		triggerDelaySlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent arg0) {
				detectApplyButStatus();
			}
		});

		//Turn on labels at major tick marks.
		triggerDelaySlider.setMajorTickSpacing(200);
		triggerDelaySlider.setMinorTickSpacing(50);
		triggerDelaySlider.setSnapToTicks(true);
		triggerDelaySlider.setPaintTicks(true);
		triggerDelaySlider.setInverted(true);
		
		triggerDelayPanel.setLayout(new BorderLayout());
		triggerDelayPanel.add(triggerDelayLabel1, BorderLayout.LINE_START);
		triggerDelayPanel.add(triggerDelaySlider);
		triggerDelayPanel.add(triggerDelayLabel2, BorderLayout.LINE_END);
		
		// adding the components to groups
		addLabel(labels[0], sysSettingsGroup);
		addInputField(entriesInViewInput, sysSettingsGroup);
		addInputField(sysSettingsGroup, form);
		
		addInputField(autoInput, suggestionModeGroup);
		addLabel(labels[1], suggestionModeGroup);
		addInputField(thresholdInput, suggestionModeGroup);
		addLabel(labels[2], suggestionModeGroup);
		addInputField(triggerDelayPanel, suggestionModeGroup);
		
		addInputField(manualInput, suggestionModeGroup);
		addInputField(suggestionModeGroup, form);
		
		addInputField(startACPInput, form);
		
		addInputField(setDefaultPanel, form);		
		
		form.setBorder(new EmptyBorder(5,5,5,5));
		settingsPanel.add(form, BorderLayout.NORTH);
		
	}
	
	private void createHotkeyPanel(JTabbedPane tabbedPane){
		String[] groupLabels = {"System Hotkeys", "Extend/Reduce Suggestion Hotkeys"};
		String[] hotkeyLabels ={"Trigger Suggestion PopUp", "Enable/Disable ACP", 
								"Extend Suggestion by word", "Reduce Suggestion by word", 
								"Extend Suggestion by Sentence", "Reduce Suggestion by Sentence", 
								"Extend Suggestion by Paragraph", "Reduce Suggestion by Paragraph"};
		
		JPanel systemPanel = new JPanel(false);
		tabbedPane.addTab(getTabFormat("Hotkeys"), systemPanel);
		tabbedPane.setMnemonicAt(1, KeyEvent.VK_2);
		
		systemPanel.setLayout(new BorderLayout());
		
		JPanel form = new JPanel();
		form.setLayout(new GridBagLayout());
		
		initialGridBagConstraints();
		
		TitledBorder systemBorder = new TitledBorder(new EtchedBorder(), groupLabels[0]);
		JPanel systemGroup = new JPanel();
		systemGroup.setLayout(new GridBagLayout());
		systemGroup.setBorder(systemBorder);
		
		TitledBorder extendSuggestionBorder = new TitledBorder(new EtchedBorder(), groupLabels[1]);
		JPanel extendSuggestionGroup = new JPanel();
		extendSuggestionGroup.setLayout(new GridBagLayout());
		extendSuggestionGroup.setBorder(extendSuggestionBorder);
		
		JPanel setDefaultPanel = new JPanel();
		setDefaultPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		JButton setDefaultBut = new JButton("Set Default");
		setDefaultBut.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				defaultHotkeys();
				detectApplyButStatus();
			}
		});
		setDefaultPanel.add(setDefaultBut);
		
		// initialising the hotkey components
		DocumentFilter filter = new UppercaseDocumentFilter();
		triggerSuggestionPopUp = new JTextField();
		triggerSuggestionPopUp.addKeyListener(this);
		((AbstractDocument) triggerSuggestionPopUp.getDocument()).setDocumentFilter(filter);
		enableACP = new JTextField();
		enableACP.addKeyListener(this);
		((AbstractDocument) enableACP.getDocument()).setDocumentFilter(filter);
		extendWord = new JTextField();
		extendWord.addKeyListener(this);
		((AbstractDocument) extendWord.getDocument()).setDocumentFilter(filter);
		reduceWord = new JTextField();
		reduceWord.addKeyListener(this);
		((AbstractDocument) reduceWord.getDocument()).setDocumentFilter(filter);
		extendSentence = new JTextField();
		extendSentence.addKeyListener(this);
		((AbstractDocument) extendSentence.getDocument()).setDocumentFilter(filter);
		reduceSentence = new JTextField();
		reduceSentence.addKeyListener(this);
		((AbstractDocument) reduceSentence.getDocument()).setDocumentFilter(filter);
		extendParagraph = new JTextField();
		extendParagraph.addKeyListener(this);
		((AbstractDocument) extendParagraph.getDocument()).setDocumentFilter(filter);
		reduceParagraph = new JTextField();
		reduceParagraph.addKeyListener(this);
		((AbstractDocument) reduceParagraph.getDocument()).setDocumentFilter(filter);
		
		// add component to system hotkey group
		addLabel(hotkeyLabels[0], systemGroup);
		addInputField(triggerSuggestionPopUp, systemGroup);
		addLabel(hotkeyLabels[1], systemGroup);
		addInputField(enableACP, systemGroup);
		
		// add component to extend suggestion hotkey group
		addLabel(hotkeyLabels[2], extendSuggestionGroup);
		addInputField(extendWord, extendSuggestionGroup);
		addLabel(hotkeyLabels[3], extendSuggestionGroup);
		addInputField(reduceWord, extendSuggestionGroup);
		addLabel(hotkeyLabels[4], extendSuggestionGroup);
		addInputField(extendSentence, extendSuggestionGroup);
		addLabel(hotkeyLabels[5], extendSuggestionGroup);
		addInputField(reduceSentence, extendSuggestionGroup);
		addLabel(hotkeyLabels[6], extendSuggestionGroup);
		addInputField(extendParagraph, extendSuggestionGroup);
		addLabel(hotkeyLabels[7], extendSuggestionGroup);
		addInputField(reduceParagraph, extendSuggestionGroup);
		
		addInputField(systemGroup, form);
		addInputField(extendSuggestionGroup, form);
		addInputField(setDefaultPanel, form);
		
		form.setBorder(new EmptyBorder(5,5,5,5));		
		systemPanel.add(form, BorderLayout.NORTH);
	}
	
	private void defaultSettings(){
		entriesInViewInput.setSelectedIndex(DEFAULT_ENTRIES_IN_VIEW);
		autoInput.setSelected(true);
		thresholdInput.setEnabled(true);
		thresholdInput.setValue(DEFAULT_THRESHOLD);
		triggerDelaySlider.setEnabled(true);
		triggerDelaySlider.setValue(DEFAULT_TRIGGER_DELAY);
	}
	
	private String getTabFormat(String tabName){
		return "<html><body><table width='100'>" + tabName + "</table></body></html>";
	}
	
	private void addLabel(String labelName, Container parent){
		JLabel c = new JLabel(labelName);
		
		GridBagLayout gbl = (GridBagLayout) parent.getLayout();
        gbl.setConstraints(c, labelConstraints);
        parent.add(c);
	}
	
	private void addInputField(Component c, Container parent) {
        GridBagLayout gbl = (GridBagLayout) parent.getLayout();
        gbl.setConstraints(c, inputConstraints);
        parent.add(c);
    }
	
	private void initialGridBagConstraints(){
        inputConstraints = new GridBagConstraints();
        inputConstraints.fill = GridBagConstraints.HORIZONTAL;
        inputConstraints.anchor = GridBagConstraints.NORTHWEST;
        inputConstraints.weightx = 1.0;
        inputConstraints.gridwidth = GridBagConstraints.REMAINDER;
        inputConstraints.insets = new Insets(5, 5, 5, 5);

        labelConstraints = (GridBagConstraints) inputConstraints.clone();
        labelConstraints.weightx = 0.0;
        labelConstraints.gridwidth = 1;
	}
	
	private void createBottomPanel(){
		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		
		okBut = new JButton("OK");
		okBut.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				editUserPreference();
				applyHotkeyChange();
				setVisible(false);
			}
		});
		
		JButton cancelBut = new JButton("Cancel");
		cancelBut.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				cancelEditPreference();
			}
		});
		
		applyBut = new JButton("Apply");
		applyBut.setEnabled(false);
		applyBut.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				editUserPreference();
				applyHotkeyChange();
				applyBut.setEnabled(false);
			}
		});
		
		bottomPanel.add(okBut);
		bottomPanel.add(cancelBut);
		bottomPanel.add(applyBut);
		
		add(bottomPanel, BorderLayout.PAGE_END);
	}
	
	private void initialPreference(Preference pref){
		entriesInViewInput.setSelectedItem(preference.getEntriesInView());
		thresholdInput.setValue(preference.getThreshold());
		triggerDelaySlider.setValue(pref.getDelayTrigger());
		
		if(!preference.isAutoTrigger()){
			manualInput.setSelected(true);
			triggerDelaySlider.setEnabled(false);
			thresholdInput.setEnabled(false);
		}
		else{
			autoInput.setSelected(true);
			triggerDelaySlider.setEnabled(true);
			thresholdInput.setEnabled(true);
		}
		
		startACPInput.setSelected(preference.isOnStartUp());
		
		triggerSuggestionPopUp.setText(preference.getTriggerSuggestionPopUp());
		enableACP.setText(preference.getEnableACP());
		extendWord.setText(preference.getExtendWord());
		reduceWord.setText(preference.getReduceWord());
		extendSentence.setText(preference.getExtendSentence());
		reduceSentence.setText(preference.getReduceSentence());
		extendParagraph.setText(preference.getExtendParagraph());
		reduceParagraph.setText(preference.getReduceParagraph());
	}
	
	private void editUserPreference(){
		if(checkChanges() && checkHotKeys()){
			
			/* Settings */
			preference.setEntriesInView((int) entriesInViewInput.getSelectedItem());
			preference.setThreshold((int) thresholdInput.getValue());
			preference.setAutoTrigger(autoInput.isSelected());
			preference.setOnStartUp(startACPInput.isSelected());
			preference.setDelayTrigger(triggerDelaySlider.getValue());
			
			/* Hotkeys */
			preference.setTriggerSuggestionPopUp(triggerSuggestionPopUp.getText());
			preference.setEnableACP(enableACP.getText());
			preference.setExtendWord(extendWord.getText());
			preference.setReduceWord(reduceWord.getText());
			preference.setExtendSentence(extendSentence.getText());
			preference.setReduceSentence(reduceSentence.getText());
			preference.setExtendParagraph(extendParagraph.getText());
			preference.setReduceParagraph(reduceParagraph.getText());
			
			logic.editUserPreference(preference);
		}
	}
	
	private void detectApplyButStatus(){
		if(checkChanges()){
			applyBut.setEnabled(true);
		}
		else{
			applyBut.setEnabled(false);
		}
	}
	
	private void applyHotkeyChange(){
		JIntellitype.getInstance().unregisterHotKey(1);
		setUpSystemKeyListener();
	}
	
	private boolean checkChanges(){
		if(preference.getEntriesInView() == (int) entriesInViewInput.getSelectedItem() &&
				preference.getThreshold() == (int) thresholdInput.getValue() &&
				preference.getDelayTrigger() == (int) triggerDelaySlider.getValue() &&
				preference.isAutoTrigger() == autoInput.isSelected() &&
				preference.isOnStartUp() == startACPInput.isSelected() &&
				preference.getTriggerSuggestionPopUp().equals(triggerSuggestionPopUp.getText()) &&
				preference.getEnableACP().equals(enableACP.getText()) &&
				preference.getExtendWord().equals(extendWord.getText()) &&
				preference.getReduceWord().equals(reduceWord.getText()) &&
				preference.getExtendSentence().equals(extendSentence.getText()) &&
				preference.getReduceSentence().equals(reduceSentence.getText()) &&
				preference.getExtendParagraph().equals(extendParagraph.getText()) &&
				preference.getReduceParagraph().equals(reduceParagraph.getText()))
			return false;
		else
			return true;
	}
	
	private boolean checkHotKeys(){
		if(triggerSuggestionPopUp.getText().equals("") ||
				enableACP.getText().equals("") ||
				extendWord.getText().equals("") ||
				reduceWord.getText().equals("") ||
				extendSentence.getText().equals("") ||
				reduceSentence.getText().equals("") ||
				extendParagraph.getText().equals("") ||
				reduceParagraph.getText().equals(""))
			return false;
		else
			return true;
	}
	
	private void clearSameHotkeys(JComponent component){
		if(((JTextField)component).equals(triggerSuggestionPopUp)){
			if(((JTextField)component).getText().equals(enableACP.getText())){
				enableACP.setText("");
			}
			else if(((JTextField)component).getText().equals(extendWord.getText())){
				extendWord.setText("");
			}
			else if(((JTextField)component).getText().equals(reduceWord.getText())){
				reduceWord.setText("");
			}
			else if(((JTextField)component).getText().equals(extendSentence.getText())){
				extendSentence.setText("");
			}
			else if(((JTextField)component).getText().equals(reduceSentence.getText())){
				reduceSentence.setText("");
			}
			else if(((JTextField)component).getText().equals(extendParagraph.getText())){
				extendParagraph.setText("");
			}
			else if(((JTextField)component).getText().equals(reduceParagraph.getText())){
				reduceParagraph.setText("");
			}
		}
		else if(((JTextField)component).equals(enableACP)){
			if(((JTextField)component).getText().equals(triggerSuggestionPopUp.getText())){
				triggerSuggestionPopUp.setText("");
			}
			else if(((JTextField)component).getText().equals(extendWord.getText())){
				extendWord.setText("");
			}
			else if(((JTextField)component).getText().equals(reduceWord.getText())){
				reduceWord.setText("");
			}
			else if(((JTextField)component).getText().equals(extendSentence.getText())){
				extendSentence.setText("");
			}
			else if(((JTextField)component).getText().equals(reduceSentence.getText())){
				reduceSentence.setText("");
			}
			else if(((JTextField)component).getText().equals(extendParagraph.getText())){
				extendParagraph.setText("");
			}
			else if(((JTextField)component).getText().equals(reduceParagraph.getText())){
				reduceParagraph.setText("");
			}
		}
		else if(((JTextField)component).equals(extendWord)){
			if(((JTextField)component).getText().equals(triggerSuggestionPopUp.getText())){
				triggerSuggestionPopUp.setText("");
			}
			else if(((JTextField)component).getText().equals(enableACP.getText())){
				enableACP.setText("");
			}
			else if(((JTextField)component).getText().equals(reduceWord.getText())){
				reduceWord.setText("");
			}
			else if(((JTextField)component).getText().equals(extendSentence.getText())){
				extendSentence.setText("");
			}
			else if(((JTextField)component).getText().equals(reduceSentence.getText())){
				reduceSentence.setText("");
			}
			else if(((JTextField)component).getText().equals(extendParagraph.getText())){
				extendParagraph.setText("");
			}
			else if(((JTextField)component).getText().equals(reduceParagraph.getText())){
				reduceParagraph.setText("");
			}
		}
		else if(((JTextField)component).equals(reduceWord)){
			if(((JTextField)component).getText().equals(triggerSuggestionPopUp.getText())){
				triggerSuggestionPopUp.setText("");
			}
			else if(((JTextField)component).getText().equals(enableACP.getText())){
				enableACP.setText("");
			}
			else if(((JTextField)component).getText().equals(extendWord.getText())){
				extendWord.setText("");
			}
			else if(((JTextField)component).getText().equals(extendSentence.getText())){
				extendSentence.setText("");
			}
			else if(((JTextField)component).getText().equals(reduceSentence.getText())){
				reduceSentence.setText("");
			}
			else if(((JTextField)component).getText().equals(extendParagraph.getText())){
				extendParagraph.setText("");
			}
			else if(((JTextField)component).getText().equals(reduceParagraph.getText())){
				reduceParagraph.setText("");
			}
		}
		else if(((JTextField)component).equals(extendSentence)){
			if(((JTextField)component).getText().equals(triggerSuggestionPopUp.getText())){
				triggerSuggestionPopUp.setText("");
			}
			else if(((JTextField)component).getText().equals(enableACP.getText())){
				enableACP.setText("");
			}
			else if(((JTextField)component).getText().equals(extendWord.getText())){
				extendWord.setText("");
			}
			else if(((JTextField)component).getText().equals(reduceWord.getText())){
				reduceWord.setText("");
			}
			else if(((JTextField)component).getText().equals(reduceSentence.getText())){
				reduceSentence.setText("");
			}
			else if(((JTextField)component).getText().equals(extendParagraph.getText())){
				extendParagraph.setText("");
			}
			else if(((JTextField)component).getText().equals(reduceParagraph.getText())){
				reduceParagraph.setText("");
			}
		}
		else if(((JTextField)component).equals(reduceSentence)){
			if(((JTextField)component).getText().equals(triggerSuggestionPopUp.getText())){
				triggerSuggestionPopUp.setText("");
			}
			else if(((JTextField)component).getText().equals(enableACP.getText())){
				enableACP.setText("");
			}
			else if(((JTextField)component).getText().equals(extendWord.getText())){
				extendWord.setText("");
			}
			else if(((JTextField)component).getText().equals(reduceWord.getText())){
				reduceWord.setText("");
			}
			else if(((JTextField)component).getText().equals(extendSentence.getText())){
				extendSentence.setText("");
			}
			else if(((JTextField)component).getText().equals(extendParagraph.getText())){
				extendParagraph.setText("");
			}
			else if(((JTextField)component).getText().equals(reduceParagraph.getText())){
				reduceParagraph.setText("");
			}
		}
		else if(((JTextField)component).equals(extendParagraph)){
			if(((JTextField)component).getText().equals(triggerSuggestionPopUp.getText())){
				triggerSuggestionPopUp.setText("");
			}
			else if(((JTextField)component).getText().equals(enableACP.getText())){
				enableACP.setText("");
			}
			else if(((JTextField)component).getText().equals(extendWord.getText())){
				extendWord.setText("");
			}
			else if(((JTextField)component).getText().equals(reduceWord.getText())){
				reduceWord.setText("");
			}
			else if(((JTextField)component).getText().equals(extendSentence.getText())){
				extendSentence.setText("");
			}
			else if(((JTextField)component).getText().equals(reduceSentence.getText())){
				reduceSentence.setText("");
			}
			else if(((JTextField)component).getText().equals(reduceParagraph.getText())){
				reduceParagraph.setText("");
			}
		}
		else if(((JTextField)component).equals(reduceParagraph)){
			if(((JTextField)component).getText().equals(triggerSuggestionPopUp.getText())){
				triggerSuggestionPopUp.setText("");
			}
			else if(((JTextField)component).getText().equals(enableACP.getText())){
				enableACP.setText("");
			}
			else if(((JTextField)component).getText().equals(extendWord.getText())){
				extendWord.setText("");
			}
			else if(((JTextField)component).getText().equals(reduceWord.getText())){
				reduceWord.setText("");
			}
			else if(((JTextField)component).getText().equals(extendSentence.getText())){
				extendSentence.setText("");
			}
			else if(((JTextField)component).getText().equals(reduceSentence.getText())){
				reduceSentence.setText("");
			}
			else if(((JTextField)component).getText().equals(extendParagraph.getText())){
				extendParagraph.setText("");
			}
		}
		
	}
	
	private void cancelEditPreference(){
		if(checkChanges()){
			String message = "You have unsaved configuration changes. Do you wish to cancel the changes?";
			Object[] options = {"Yes", "No"};
			
			int n = JOptionPane.showOptionDialog(this, message, "AutoComPaste",	
					JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options,	options[0]);
			
			if(n==1){
				return;
			}
		}
		
		setVisible(false);
	}
	
	private void defaultHotkeys(){
		triggerSuggestionPopUp.setText(DEFAULT_HOTKEY_TRIGGERSUGGESTIONPOPUP);
		enableACP.setText(DEFAULT_HOTKEY_ENABLEACP);
		extendWord.setText(DEFAULT_HOTKEY_EXTENDWORD);
		reduceWord.setText(DEFAULT_HOTKEY_REDUCEWORD);
		extendSentence.setText(DEFAULT_HOTKEY_EXTENDSENTENCE);
		reduceSentence.setText(DEFAULT_HOTKEY_REDUCESENTENCE);
		extendParagraph.setText(DEFAULT_HOTKEY_EXTENDPARAGRAPH);
		reduceParagraph.setText(DEFAULT_HOTKEY_REDUCEPARAGRAPH);
	}
	
	private String updateKeyString(String keyMod){
		String keyString = "";
		
		if(!(keyMod.contains("Alt") && keyMod.contains("Ctrl")) && 
				!(keyMod.contains("Alt") && keyMod.contains("Ctrl") && keyMod.contains("Shift"))){
			
			if(keyMod.contains("Shift"))
				keyString = keyString + "SHIFT + ";
			
			if(keyMod.contains("Alt"))
				keyString = keyString + "ALT + ";
			
			if(keyMod.contains("Ctrl"))
				keyString = keyString + "CTRL + ";
			
		}
		
		return keyString;
	}
	
	private void displayHotKeys_KeyPressed(KeyEvent e){
		int keyCode = e.getKeyCode();
		String keyPressed = KeyEvent.getKeyText(keyCode);
		String keyMod = KeyEvent.getModifiersExText(e.getModifiersEx());
		String keyString = updateKeyString(keyMod);
		
		if(!(keyCode >= 16 && keyCode <= 18) && !keyString.equals("")){
			keyString += keyPressed;
			
			((JTextField)e.getSource()).setText(keyString);
			clearSameHotkeys((JTextField)e.getSource());
			okBut.requestFocus();
		}
	}

	@Override
	public void keyPressed(KeyEvent e) {
		displayHotKeys_KeyPressed(e);
		detectApplyButStatus();
	}

	@Override
	public void keyReleased(KeyEvent e) {
		e.consume();
	}

	@Override
	public void keyTyped(KeyEvent e) {
		e.consume();
	}
	
	public void setUpJIntellitypeFile(){
		boolean is64bit = (System.getProperty("sun.arch.data.model").indexOf("64") != -1);
		
		if(is64bit){
			try{
				File file1 = new File("JIntellitype64.dll");
				File file2 = new File("JIntellitype32.dll");
				File file3 = new File("JIntellitype.dll");
				if(file1.exists() && file2.exists() && file3.exists()){
					file2.delete();
				}
				if(file1.exists()){
					file3.renameTo(file2);
					file1.renameTo(file3);
				}
			}catch(Exception e){
				logger.error("JIntellitype file rename fail - System 64bit");
			}
		}else{
			try{
				File file1 = new File("JIntellitype32.dll");
				File file2 = new File("JIntellitype64.dll");
				File file3 = new File("JIntellitype.dll");
				if(file1.exists() && file2.exists() && file3.exists()){
					file1.delete();
				}
				if(file1.exists()){
					file3.renameTo(file2);
					file1.renameTo(file3);
				}
			}catch(Exception e){
				logger.error("JIntellitype file rename fail - System 32bit");
			}
		}
	}
	
	public void setUpSystemKeyListener(){
		String key ="0"; 
		String modKey1 = "0"; 
		String modKey2 = "0";
		String enableACPKey = preference.getEnableACP();
		String enableACPKeys[] = enableACPKey.split("\\+");
		for(int i=enableACPKeys.length-1; i>=0; i--){
			if(i == enableACPKeys.length-1)
				key = enableACPKeys[i].trim();
			else if(i == 0)
				modKey1 = enableACPKeys[i].trim();
			else
				modKey2 = enableACPKeys[i].trim();
		}
		JIntellitype.getInstance();
		JIntellitype.getInstance().registerHotKey(1, setModKeys(modKey1, modKey2), setKey(key));
	}
	
	private int setKey(String key){
		int returnKey = (int)key.charAt(0);
		
		if(key.length() > 1){
			
			// hardcoded values as the library does not have it
			switch(key){
			case "F1":returnKey = 112;
			break;
			case "F2": returnKey = 113; 
			break;
			case "F3": returnKey = 114; 
			break;
			case "F4": returnKey = 115; 
			break;
			case "F5": returnKey = 116; 
			break;
			case "F6": returnKey = 117; 
			break;
			case "F7": returnKey = 118; 
			break;
			case "F8": returnKey = 119; 
			break;
			case "F9": returnKey = 120; 
			break;
			case "F10": returnKey = 121; 
			break;
			case "F11": returnKey = 122; 
			break;
			case "F12": returnKey = 123; 
			break;
			}
		}
		
		return returnKey;
	}
	
	private int setModKeys(String modKey1, String modKey2){
		int modKey = 0;
		if(!modKey1.equals("0")){
			switch(modKey1){
			case "ALT":
				modKey = JIntellitype.MOD_ALT;
				break;
			case "CTRL":
				modKey = JIntellitype.MOD_CONTROL;
				break;
			case "SHIFT":
				modKey = JIntellitype.MOD_SHIFT;
				break;
			}
		}
		if(!modKey2.equals("0")){
			switch(modKey2){
			case "ALT":
				modKey += JIntellitype.MOD_ALT;
				break;
			case "CTRL":
				modKey += JIntellitype.MOD_CONTROL;
				break;
			case "SHIFT":
				modKey += JIntellitype.MOD_SHIFT;
				break;
			}
		}
		return modKey;
	}
	
	public void addObserver(SystrayUI systrayUI){
		preference.addObserver(systrayUI);
	}

	@Override
	public void onHotKey(int arg0) {
		if(arg0 == 1){
			if(preference.isEnableDisable()){
				preference.setEnableDisable(false);
				logic.editEnableDisableACP(preference);
			}else{
				preference.setEnableDisable(true);
				logic.editEnableDisableACP(preference);
			}
		}
	}
	
	public boolean isEnableDisable(){
		return preference.isEnableDisable();
	}
	
	public void setEnableDisable(boolean enableDisable){
		preference.setEnableDisable(enableDisable);
		logic.editEnableDisableACP(preference);
	}
}

class UppercaseDocumentFilter extends DocumentFilter{ 
	public void insertString(DocumentFilter.FilterBypass fb, int offset, String text, AttributeSet attr) throws BadLocationException {             
		fb.insertString(offset, text.toUpperCase(), attr);       
	}               
	public void replace(DocumentFilter.FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {             
		fb.replace(offset, length, text.toUpperCase(), attrs);        
	}   
}
