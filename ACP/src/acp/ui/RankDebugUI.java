package acp.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;

import acp.beans.RankEntry;
import acp.manager.RankManager;


public class RankDebugUI extends JFrame{

	private JTextField sourceTF, scoreTF, turnOfLastUseTF, rankOfAdditionTF;
	private JLabel sourceLB, scoreLB, turnOfLastUseLB, rankOfAdditionLB;
	private JSplitPane splitPane, splitPane_left, splitPane_right;
	private JPanel splitePane_left_pane;
	private JPanel upperPane_left;
	private JScrollPane lowerPane_left;
	private ArrayList<ArrayList<RankEntry>> highlistList, lowlistList;
	private ArrayList<String> destinationDocumentList;
	private JComboBox<String> destinationDocumentListCB;
	private JButton resetButton;
	private RankManager rm;
	
	public RankDebugUI(ArrayList<ArrayList<RankEntry>> highlistList, ArrayList<ArrayList<RankEntry>> lowlistList, RankManager rm){
		this.rm = rm;
		this.highlistList = highlistList;
		this.lowlistList = lowlistList;
		this.destinationDocumentList = new ArrayList<String>();
		this.destinationDocumentList.add("<View Sequence>");
		initUI();
	}
	
	private void initUI(){
		
		/* settings for the window */
		setTitle("AutoComPaste ML Debug UI");
		setSize(690,250);
		setLocationRelativeTo(null);
		setResizable(false);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent winEvt){
				setVisible(false);
			}
		});
		
		try {
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		createRightScrollPane();
		createLeftScrollPane();
		
		/* add the 2 panels (treePanel and vaiablePanel) into split panel */
		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, splitePane_left_pane, splitPane_right);
		splitPane.setDividerLocation(400);
		
		this.getContentPane().add(splitPane);
		this.setVisible(false);
		
	}
	
	private void createRightScrollPane(){
		
		JPanel upperPane_right = new JPanel();
		JPanel lowerPane_right = new JPanel();
		
		sourceLB = new JLabel("Source Name");
		sourceLB.setSize(20, 5);
		sourceTF = new JTextField("NULL");
		sourceTF.setColumns(30);
		sourceTF.setEditable(false);
		sourceTF.setForeground(Color.red);
		lowerPane_right.add(sourceLB);
		lowerPane_right.add(sourceTF);

		scoreLB = new JLabel("Score");
		scoreLB.setSize(20, 5);
		scoreTF = new JTextField("NULL");
		scoreTF.setColumns(30);
		scoreTF.setEditable(false);
		scoreTF.setForeground(Color.red);
		lowerPane_right.add(scoreLB);
		lowerPane_right.add(scoreTF);

		turnOfLastUseLB = new JLabel("Last Used Turn");
		turnOfLastUseLB.setSize(20, 5);
		turnOfLastUseTF = new JTextField("NULL");
		turnOfLastUseTF.setColumns(30);
		turnOfLastUseTF.setEditable(false);
		turnOfLastUseTF.setForeground(Color.red);
		lowerPane_right.add(turnOfLastUseLB);
		lowerPane_right.add(turnOfLastUseTF);

		rankOfAdditionLB = new JLabel("Launch Sequence no.");
		rankOfAdditionLB.setSize(20, 5);
		rankOfAdditionTF = new JTextField("NULL");
		rankOfAdditionTF.setColumns(30);
		rankOfAdditionTF.setEditable(false);
		rankOfAdditionTF.setForeground(Color.red);
		lowerPane_right.add(rankOfAdditionLB);
		lowerPane_right.add(rankOfAdditionTF);
		
		resetButton = new JButton("Reset All");
		resetButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				rm.resetData();
			}
		});
		upperPane_right.add(resetButton);
		
		splitPane_right = new JSplitPane(JSplitPane.VERTICAL_SPLIT, upperPane_right, lowerPane_right);
		splitPane_right.setDividerLocation(30);
		splitPane_right.setMinimumSize(new Dimension(290, 250));
	}
	
	private void createLeftScrollPane(){
		
		upperPane_left = new JPanel();
		destinationDocumentListCB = new JComboBox<String>();
		for(String item : destinationDocumentList){
			destinationDocumentListCB.addItem(item);
		}
		upperPane_left.add(destinationDocumentListCB);
		
		lowerPane_left = new JScrollPane(generateTree(0));
		
		splitPane_left = new JSplitPane(JSplitPane.VERTICAL_SPLIT, upperPane_left, lowerPane_left);
		splitPane_left.setDividerLocation(30);
		
		splitePane_left_pane = new JPanel();
		splitePane_left_pane.setLayout(new GridLayout(1,1));
		splitePane_left_pane.add(splitPane_left);
		
		splitPane_left.setMinimumSize(new Dimension(400, 250));
	}
	
	private JTree generateTree(int index){
		
		/* generate the tree */
		DefaultMutableTreeNode rankingNode = new DefaultMutableTreeNode("Ranking Algorithm");  
		createNodes(rankingNode, index);
		final JTree rankingTree = new JTree(rankingNode);

		/* add selection listener */
		rankingTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		rankingTree.addTreeSelectionListener(new TreeSelectionListener() {
			public void valueChanged(TreeSelectionEvent e){
				DefaultMutableTreeNode node = (DefaultMutableTreeNode)rankingTree.getLastSelectedPathComponent();

				/* if nothing is selected */
				if (node == null) return;

				/* retrieve the node that was selected */
				Object nodeInfo = node.getUserObject();

				/* React to the node selection. */
				try{
					setVariables((RankEntry)nodeInfo, destinationDocumentListCB.getSelectedIndex());
				}catch(Exception ex){}
			}
		});

		/* expand the tree */
		for (int i=0; i < rankingTree.getRowCount(); i++)
			rankingTree.expandRow(i);
		
		rankingTree.setSelectionRow(0);
		
		return rankingTree;
	}
	
	private void createNodes(DefaultMutableTreeNode top, int index) {
		DefaultMutableTreeNode rankingList = null;         
		DefaultMutableTreeNode rankEntry = null;   
		
		rankingList = new DefaultMutableTreeNode("High List");         
		top.add(rankingList);
		ArrayList<RankEntry> highList = highlistList.get(index);
		if(highList.size() > 0){
			for(RankEntry e : highList){
				rankEntry = new DefaultMutableTreeNode(e); 
				rankingList.add(rankEntry);
			}
		}
		
		rankingList = new DefaultMutableTreeNode("Low List");        
		top.add(rankingList);
		ArrayList<RankEntry> lowList = lowlistList.get(index);
		if(lowList.size() > 0){
			for(RankEntry e : lowList){
				rankEntry = new DefaultMutableTreeNode(e);         
				rankingList.add(rankEntry);  
			}
		}
	}
	
	private void setVariables(RankEntry e, int destinationDocIndex){
		if(destinationDocIndex != 0){
			sourceTF.setText(e.getSource());
			sourceTF.setCaretPosition(0);
			rankOfAdditionTF.setText(String.valueOf(e.getRankOfAddition()));
			scoreTF.setText(String.valueOf(e.getScore()));
			turnOfLastUseTF.setText(String.valueOf(e.getTurnOfLastUse()));
		}else{
			sourceTF.setText(e.getSource());
			sourceTF.setCaretPosition(0);
			rankOfAdditionTF.setText(String.valueOf(e.getRankOfAddition()));
			//scoreTF.setText("NULL");
			scoreTF.setText(String.valueOf(e.getScore()));
			turnOfLastUseTF.setText("NULL");
		}
	}
	
	private void resetTextFields(){
		sourceTF.setText("NULL");
		rankOfAdditionTF.setText("NULL");
		scoreTF.setText("NULL");
		turnOfLastUseTF.setText("NULL");
	}
	
	public void refreshTreePane(ArrayList<ArrayList<RankEntry>> highlistList, ArrayList<ArrayList<RankEntry>> lowlistList, ArrayList<Integer> destinationDocumentList){
		this.highlistList = highlistList;
		this.lowlistList = lowlistList;
		
		this.destinationDocumentList = new ArrayList<String>();
		this.destinationDocumentList.add("<View Sequence>");
		for(int i : destinationDocumentList)
			this.destinationDocumentList.add(String.valueOf(i));
		destinationDocumentListCB = new JComboBox<String>();
		for(String item : this.destinationDocumentList)
			destinationDocumentListCB.addItem(item);
		destinationDocumentListCB.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				lowerPane_left.setViewportView(generateTree(destinationDocumentListCB.getSelectedIndex()));
				resetTextFields();
			}
		});
		
		upperPane_left = new JPanel();
		upperPane_left.add(destinationDocumentListCB);
		
		lowerPane_left = new JScrollPane(generateTree(0));
		
		splitPane_left = new JSplitPane(JSplitPane.VERTICAL_SPLIT, upperPane_left, lowerPane_left);
		splitPane_left.setDividerLocation(30);
		
		splitePane_left_pane.removeAll();
		splitePane_left_pane.add(splitPane_left);
		splitePane_left_pane.revalidate();
		
		resetTextFields();
	}
	
	public void showDebugUI(){
		this.setVisible(true);
	}
	
	public void closeDebugUI(){
		this.dispose();
	}
} 
