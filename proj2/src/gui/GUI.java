package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.TitledBorder;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

import FileSystem.FileSystem;
import FileSystem.MyFile;
import client.Client;
import client.UpdateListener;

/**
 * This is the main GUI for the collaborative editor. One GUI per client. 
 * 
 * Specifications of the essential fields and their associated listeners 
 * can be found at their respective locations
 *
 */

@SuppressWarnings("serial")
public class GUI extends JFrame {
	private String newline = "\n";
	private final JPanel gui;
	private final JLabel guiTitle; // the title of the GUI
	// the two fields below display the current document name
	private final JLabel documentName; 
	public final JTextField documentNameField; 
	// the edit area in the GUI
	private final JTextPane editArea; 
	 // the edit history for client
	private final JTextArea editHistory;
	private final JButton createNew;
	private final JButton delete;
	private final JButton cutButton;
	private final JButton copyButton;
	private final JButton pasteButton;
	private final JButton selectAllButton;
	// a drop-down box listing all the files in the File System
	private final JComboBox fileList;
	private final JFileChooser fc;
	private final JTextArea log;
	private final JButton openButton;
	private final JButton saveButton;
	private String docName; 
	HashMap<Object, Action> actions;
	private HashMap<String, Integer> filenameToDocNum = new HashMap<String, Integer>();

	private AbstractDocument document;

	private final FileSystem fileSystem; 
	public final Client client;
	public MyFile currentFile;

	public GUI(Client client) {

		this.setTitle("Collaborative Editor");
		this.client = client;
		this.fileSystem = client.fileSystem;
		this.fileSystem.addView(this);
		this.currentFile = fileSystem.getFile().get(notNullIndex());
		this.document = currentFile.getDoc();
		this.docName = currentFile.getDocName();

		// create GUI title
		guiTitle = new JLabel("Welcome to Collaborative Editor!");
		getContentPane().add(guiTitle);

		// create GUI Image
		ImageIcon icon1 = new ImageIcon("/Users/tonii/OneDrive/Documents/collaborative_editing/proj2/image/writing-2.jpg",
		        "Collaborative Editing");
		JLabel guiPicture = new JLabel(icon1);

		// create JButton
		ImageIcon newIcon = new ImageIcon("/Users/tonii/OneDrive/Documents/collaborative_editing/proj2/image/new.png");
		createNew = new JButton(newIcon);
		getContentPane().add(createNew);
		createNew.addActionListener(new createDocListener());
		createNew.setToolTipText("Create New File");

		// delete JButton
		ImageIcon deleteIcon = new ImageIcon("/Users/tonii/OneDrive/Documents/collaborative_editing/proj2/image/delete.png");
		delete = new JButton(deleteIcon);
		getContentPane().add(delete);
		delete.addActionListener(new deleteDocListener());
		delete.setToolTipText("Delete Current File");

		// create drop-down box
		JLabel dropDownHeader = new JLabel("-Select Document-");
		fileList = new JComboBox();
		fileSystem.guiWantDoc();
		fileList.addActionListener(new dropDownListener());
		getContentPane().add(dropDownHeader);
		getContentPane().add(fileList);

		// display document name
		documentName = new JLabel("-Current File (click to rename)-");
		getContentPane().add(documentName);
		documentNameField = new JTextField(docName);
		documentNameField.setEditable(false);
		documentNameField.addMouseListener(new ChangeDocNameListener());

		// create an editor pane
		editArea = new JTextPane();
		editArea.setDocument(document);
		editArea.setCaretPosition(0); // text-insertion point
		JScrollPane editScrollPane = new JScrollPane(editArea);
		editScrollPane.setPreferredSize(new Dimension(500, 300));
		getContentPane().add(editScrollPane);

		// create a edit history table
		editHistory = new JTextArea();
		editHistory.setEditable(false);
		JScrollPane historyScrollPane = new JScrollPane(editHistory);
		historyScrollPane.setPreferredSize(new Dimension(500, 80));
		getContentPane().add(historyScrollPane);

		// Set up the menu bar
		actions = createActionTable(editArea);
		
		// create file menu
		JMenu filemenu = new JMenu("File");
		filemenu.setMnemonic(KeyEvent.VK_F);
		JMenuItem newFileMenu = new JMenuItem("New File");
		newFileMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N,
		        ActionEvent.ALT_MASK));
		newFileMenu.addActionListener(new createDocListener());
		
		JMenuItem deleteFileMenu = new JMenuItem("Delete");
		deleteFileMenu.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_D,ActionEvent.ALT_MASK));
		deleteFileMenu.addActionListener(new deleteDocListener());
		
		JMenuItem saveFileMenu = new JMenuItem("Save as...");
		saveFileMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S
				,ActionEvent.ALT_MASK));
		saveFileMenu.addActionListener(new saveDocListener());
		
		filemenu.add(newFileMenu);
		filemenu.add(deleteFileMenu);
		filemenu.addSeparator();
		filemenu.add(saveFileMenu);
		

		// create edit menu
		JMenu editmenu = new JMenu("Edit");
		editmenu.setMnemonic('E');

		Action cutAction = new DefaultEditorKit.CutAction();
		cutAction.putValue(Action.NAME, "Cut");
		editmenu.add(cutAction);

		Action copyAction = new DefaultEditorKit.CopyAction();
		copyAction.putValue(Action.NAME, "Copy");
		editmenu.add(copyAction);

		Action pasteAction = new DefaultEditorKit.PasteAction();
		pasteAction.putValue(Action.NAME, "Paste");
		editmenu.add(pasteAction);

		editmenu.addSeparator();

		Action backAction = getActionByName(DefaultEditorKit.backwardAction);
		backAction.putValue(Action.NAME, "Caret Back");
		editmenu.add(backAction);

		Action forwardAction = getActionByName(DefaultEditorKit.forwardAction);
		forwardAction.putValue(Action.NAME, "Caret Forward");
		editmenu.add(forwardAction);

		Action upAction = getActionByName(DefaultEditorKit.upAction);
		upAction.putValue(Action.NAME, "Caret Up");
		editmenu.add(upAction);

		Action downAction = getActionByName(DefaultEditorKit.downAction);
		downAction.putValue(Action.NAME, "Caret Down");
		editmenu.add(downAction);

		editmenu.addSeparator();

		Action selectAllAction = getActionByName(DefaultEditorKit.selectAllAction);
		selectAllAction.putValue(Action.NAME, "Select All");
		editmenu.add(selectAllAction);

		JMenuBar mb = new JMenuBar();
		mb.add(filemenu);
		mb.add(editmenu);
		setJMenuBar(mb);

		// set up file chooser
		log = new JTextArea(5, 20);
		log.setMargin(new Insets(5, 5, 5, 5));
		log.setEditable(false);
		
		
		fc = new JFileChooser(){
		    @Override
		    public void approveSelection(){
		        File f = getSelectedFile();
		        if(f.exists() && getDialogType() == SAVE_DIALOG){
		            int result = JOptionPane.showConfirmDialog(
		            		gui,"The file exists. Would you like to overwrite?",
		            		"Existing file", JOptionPane.YES_NO_CANCEL_OPTION);
		            switch(result){
		                case JOptionPane.YES_OPTION:
		                    super.approveSelection();
		                    return;
		                case JOptionPane.NO_OPTION:
		                    return;
		                case JOptionPane.CLOSED_OPTION:
		                    return;
		                case JOptionPane.CANCEL_OPTION:
		                    cancelSelection();
		                    return;
		            }
		        }
		        super.approveSelection();
		    }
		};
		ImageIcon openIcon = new ImageIcon("/Users/tonii/OneDrive/Documents/collaborative_editing/proj2/image/open.png");
		openButton = new JButton(openIcon);
		openButton.addActionListener(new loadDocListener());
		openButton.setToolTipText("Open a New File");
		
		ImageIcon saveIcon = new ImageIcon("/Users/tonii/OneDrive/Documents/collaborative_editing/proj2/image/save.png");
		saveButton = new JButton(saveIcon);
		saveButton.addActionListener(new saveDocListener());
		saveButton.setToolTipText("Save and Export");
		

		// Add hot-key commands
		addHotKey();

		// add listeners
		JPanel statusPane = new JPanel(new GridLayout(1, 1));
		CaretListenerLabel caretListenerLabel = new CaretListenerLabel(
		        "Caret Status");
		statusPane.add(caretListenerLabel);
		editArea.addCaretListener(caretListenerLabel);

		// set layout

		// 1. parent window
		gui = new JPanel(new BorderLayout(5, 5));
		gui.setBorder(new TitledBorder("Azure v1.2"));

		// top panel: document name, create new document, change theme
		JPanel plafComponents = new JPanel(new FlowLayout(FlowLayout.CENTER,5,3));
		
		JPanel displayDocName = new JPanel(new BorderLayout());
		displayDocName.add(documentName, BorderLayout.NORTH);
		displayDocName.add(documentNameField, BorderLayout.SOUTH);

		plafComponents.add(createNew);
		plafComponents.add(openButton);
		plafComponents.add(delete);
		plafComponents.add(saveButton);
		plafComponents.add(displayDocName);

		JPanel plafSubComp = new JPanel(new BorderLayout(3, 3));

		plafSubComp.setBorder(new TitledBorder("Choose Theme"));

		final UIManager.LookAndFeelInfo[] plafInfos = UIManager
		        .getInstalledLookAndFeels();
		String[] plafNames = new String[plafInfos.length];
		for (int ii = 0; ii < plafInfos.length; ii++) {
			plafNames[ii] = plafInfos[ii].getName();
		}
		final JComboBox plafChooser = new JComboBox(plafNames);
		plafSubComp.add(plafChooser, BorderLayout.CENTER);


		plafComponents.add(plafSubComp);

		plafChooser.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				int index = plafChooser.getSelectedIndex();
				try {
					UIManager.setLookAndFeel(plafInfos[index].getClassName());
					SwingUtilities.updateComponentTreeUI(getRootPane());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		JPanel upperPortion = new JPanel(new BorderLayout());
		upperPortion.add(guiPicture, BorderLayout.NORTH);
		upperPortion.add(plafComponents, BorderLayout.SOUTH);

		gui.add(upperPortion, BorderLayout.NORTH);

		// left panel
		// left panel: drop down file list
		JPanel dynamicLabels = new JPanel(new BorderLayout(4, 4));
		dynamicLabels.setBorder(new TitledBorder("Control Panel"));
		gui.add(dynamicLabels, BorderLayout.WEST);

		JPanel selectDoc = new JPanel(new BorderLayout());
		selectDoc.add(dropDownHeader, BorderLayout.NORTH);
		selectDoc.add(fileList, BorderLayout.SOUTH);

		// left panel: control buttons
		JPanel controlButtons = new JPanel(new GridLayout(2, 2));
		ImageIcon cutIcon = new ImageIcon("/Users/tonii/OneDrive/Documents/collaborative_editing/proj2/image/cut.png");
		cutButton = new JButton(cutAction);
		cutButton.setText("");
		cutButton.setIcon(cutIcon);
		cutButton.setToolTipText("cut");
		controlButtons.add(cutButton);

		ImageIcon copyIcon = new ImageIcon("/Users/tonii/OneDrive/Documents/collaborative_editing/proj2/image/copy.png");
		copyButton = new JButton(copyAction);
		copyButton.setIcon(copyIcon);
		copyButton.setText("");
		copyButton.setToolTipText("copy");
		controlButtons.add(copyButton);

		ImageIcon pasteIcon = new ImageIcon("/Users/tonii/OneDrive/Documents/collaborative_editing/proj2/image/paste.png");
		pasteButton = new JButton(pasteAction);
		pasteButton.setIcon(pasteIcon);
		pasteButton.setText("");
		pasteButton.setToolTipText("paste");
		controlButtons.add(pasteButton);

		ImageIcon selectAllIcon = new ImageIcon("/Users/tonii/OneDrive/Documents/collaborative_editing/proj2/image/selectAll.png");
		selectAllButton = new JButton(selectAllAction);
		selectAllButton.setIcon(selectAllIcon);
		selectAllButton.setText("");
		selectAllButton.setToolTipText("select all");
		controlButtons.add(selectAllButton);

		dynamicLabels.add(selectDoc, BorderLayout.NORTH);
		dynamicLabels.add(controlButtons, BorderLayout.SOUTH);

		// right panel
		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
		        editScrollPane, historyScrollPane);
		gui.add(splitPane, BorderLayout.CENTER);

		// set background color
		Color color = new Color(240, 248, 255);
		documentName.setBackground(color);
		controlButtons.setBackground(color);
		upperPortion.setBackground(color);
		plafSubComp.setBackground(color);
		dropDownHeader.setBackground(color);
		dynamicLabels.setBackground(color);
		gui.setBackground(color);
		splitPane.setBackground(color);
		plafComponents.setBackground(color);


		Thread t = new UpdateListener(client);
		t.start();
		
		// at the end
		this.setContentPane(gui);
		this.pack();
		this.setLocationRelativeTo(null);
		try {
			this.setLocationByPlatform(true);
			this.setMinimumSize(this.getSize());
		} catch (Throwable ignoreAndContinue) {
		}
		this.setVisible(true);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

	}
	
	
	protected class ChangeDocNameListener implements MouseListener {
    public void mouseClicked(MouseEvent e){
        // initiate a pop up window for renaming
		try {
		    for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
		        if ("Nimbus".equals(info.getName())) {
		            UIManager.setLookAndFeel(info.getClassName());
		            break;
		        }
		    }
		} catch (UnsupportedLookAndFeelException e1) {
		} catch (ClassNotFoundException e1) {
		} catch (InstantiationException e1) {
		} catch (IllegalAccessException e1) {
		}
				
		SwingUtilities.invokeLater(new Runnable(){
			public void run(){
				System.out.println("creating rename pop up window");
				new renamePopUp(client,currentFile,documentNameField);
			}
		});
    }
	@Override
	public void mouseEntered(MouseEvent arg0) {	
	}
	@Override
	public void mouseExited(MouseEvent arg0) {	
	}
	@Override
	public void mousePressed(MouseEvent arg0) {
	}
	@Override
	public void mouseReleased(MouseEvent arg0) {
	}
}

	// Listener for uploading new document
	protected class loadDocListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			int returnVal = fc.showOpenDialog(gui);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();
				BufferedReader reader;
				try {
					FileInputStream fis = new FileInputStream(file);
					InputStreamReader in = new InputStreamReader(fis, "UTF-8");
					reader = new BufferedReader(in);

					String line = null;
					StringBuilder stringBuilder = new StringBuilder();
					String ls = System.getProperty("line.separator");

					while ((line = reader.readLine()) != null) {
						stringBuilder.append(line.replace("\uFEFF", ""));
						stringBuilder.append(ls);
					}
					String content = stringBuilder.toString();
					client.uploadFiletoServer(file, content);
				} catch (Exception e2) {
					e2.printStackTrace();

				}
				log.append("Opening: " + file.getName() + "." + newline);
			} else {
				log.append("Open command cancelled by user." + newline);
			}
			log.setCaretPosition(log.getDocument().getLength());
		}
	}

	// Listener for saving new document
	protected class saveDocListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			int returnVal = fc.showSaveDialog(gui);
			if (returnVal == JFileChooser.APPROVE_OPTION){
				File file = fc.getSelectedFile ();
				if (!file.exists()){
					try {
						file.createNewFile();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
				try {
					FileWriter fw = new FileWriter(file.getAbsoluteFile());
					BufferedWriter bw = new BufferedWriter(fw);
					bw.write(editArea.getText());
					bw.close();
					System.out.println("file saved");
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				
			}
		}
	}
	

	// Listener for creating new document
	protected class createDocListener implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			try {
				client.createNewFileOnServer();
			} catch (IOException e1) {
				e1.printStackTrace();
			}

		}
	}

	// Listener for deleting new document
	protected class deleteDocListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			String f = fileList.getSelectedItem().toString();
			System.out.println("gui: delete file");
			
			try {
			    System.out.println(filenameToDocNum.get(f));
				client.deleteFileOnServer(filenameToDocNum.get(f));
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}

	// Listener for drop-down box of file list
    protected class dropDownListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            Object holder = e.getSource();
            JComboBox tempComboBox = (JComboBox) holder;
            String f = tempComboBox.getSelectedItem().toString();
            int curDocNum = filenameToDocNum.get(f);
            currentFile = fileSystem.files.get(curDocNum);
            System.out.println("current editing file is "
                    + Integer.toString(curDocNum));
            document = currentFile.getDoc();
            docName = currentFile.getDocName();
            documentNameField.setText(docName);
            System.out.println(docName);
            editArea.setDocument(document);
            editArea.setCaretPosition(0);
            int j = 0;

            for (DocumentListener d : document.getDocumentListeners()) {
                if (d instanceof MyDocumentListener) {
                    j += 1;
                }
            }
            System.out.println("number of listener =" + j);
        }
    }

	// Listener for Caret movement
	protected class CaretListenerLabel extends JLabel implements CaretListener {
		public CaretListenerLabel(String label) {
			super(label);
		}

		// Might not be invoked from the event dispatch thread.
		public void caretUpdate(CaretEvent e) {
			displaySelectionInfo(e.getDot(), e.getMark());
		}

		// This method can be invoked from any thread. It
		// invokes the setText and modelToView methods, which
		// must run on the event dispatch thread. We use
		// invokeLater to schedule the code for execution
		// on the event dispatch thread.
		protected void displaySelectionInfo(final int dot, final int mark) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					if (dot == mark) { // no selection
						try {
							Rectangle caretCoords = editArea.modelToView(dot);
							// Convert it to view coordinates.
							setText("caret: text position: " + dot
							        + ", view location = [" + caretCoords.x
							        + ", " + caretCoords.y + "]" + newline);
						} catch (BadLocationException ble) {
							setText("caret: text position: " + dot + newline);
						}
					} else if (dot < mark) {
						setText("selection from: " + dot + " to " + mark
						        + newline);
					} else {
						setText("selection from: " + mark + " to " + dot
						        + newline);
					}
				}
			});
		}
	}

	// Listener for Document changes (text insertion, deletion, etc)
    protected class MyDocumentListener implements DocumentListener {
        public void insertUpdate(DocumentEvent e) {
            try {
                if (currentFile.getDoc() == e.getDocument()) {
                    client.updateServer(currentFile
                    .DocumentEventToEventPackage(e));
                    System.out.println("Update server with doc #" + currentFile.docNum);

                } else {
                    System.out
                            .println("The document is being updated in background, " +
                            		"don't send update to server!");
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            displayEditInfo(e);
		}

		public void removeUpdate(DocumentEvent e) {

			try {
			    if (currentFile.getDoc() == e.getDocument()) {
                    client.updateServer(currentFile
                    .DocumentEventToEventPackage(e));
                } else {
                    System.out
                            .println("The document is being updated in background, " +
                                    "don't send update to server!");
                }
				
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			displayEditInfo(e);
		}

		public void changedUpdate(DocumentEvent e) {
			System.out.println("change");
			displayEditInfo(e);
		}

		private void displayEditInfo(DocumentEvent e) {
			Document document1 = e.getDocument();
			int changeLength = e.getLength();
			editHistory
			        .append(e.getType().toString() + ": " + changeLength
			                + " character"
			                + ((changeLength == 1) ? ". " : "s. ")
			                + " Text length = " + document1.getLength() + "."
			                + newline);
		}
	}

	// Add a couple of emacs key bindings for navigation.
	protected void addHotKey() {
		InputMap inputMap = editArea.getInputMap();

		// Ctrl-b to go backward one character
		KeyStroke key = KeyStroke.getKeyStroke(KeyEvent.VK_B, Event.CTRL_MASK);
		inputMap.put(key, DefaultEditorKit.backwardAction);

		// Ctrl-f to go forward one character
		key = KeyStroke.getKeyStroke(KeyEvent.VK_F, Event.CTRL_MASK);
		inputMap.put(key, DefaultEditorKit.forwardAction);

		// Ctrl-p to go up one line
		key = KeyStroke.getKeyStroke(KeyEvent.VK_P, Event.CTRL_MASK);
		inputMap.put(key, DefaultEditorKit.upAction);

		// Ctrl-n to go down one line
		key = KeyStroke.getKeyStroke(KeyEvent.VK_N, Event.CTRL_MASK);
		inputMap.put(key, DefaultEditorKit.downAction);
	}

	// Create the edit menu.
	protected JMenu createEditMenu() {
		JMenu menu = new JMenu("Edit");

		Action cutAction = new DefaultEditorKit.CutAction();
		cutAction.putValue(Action.NAME, "Cut");
		menu.add(cutAction);

		Action copyAction = new DefaultEditorKit.CopyAction();
		copyAction.putValue(Action.NAME, "Copy");
		menu.add(copyAction);

		Action pasteAction = new DefaultEditorKit.PasteAction();
		pasteAction.putValue(Action.NAME, "Paste");
		menu.add(pasteAction);

		menu.addSeparator();

		Action backAction = getActionByName(DefaultEditorKit.backwardAction);
		backAction.putValue(Action.NAME, "Caret Back");
		menu.add(backAction);

		Action forwardAction = getActionByName(DefaultEditorKit.forwardAction);
		forwardAction.putValue(Action.NAME, "Caret Forward");
		menu.add(forwardAction);

		Action upAction = getActionByName(DefaultEditorKit.upAction);
		upAction.putValue(Action.NAME, "Caret Up");
		menu.add(upAction);

		Action downAction = getActionByName(DefaultEditorKit.downAction);
		downAction.putValue(Action.NAME, "Caret Down");
		menu.add(downAction);

		menu.addSeparator();

		Action selectAllAction = getActionByName(DefaultEditorKit.selectAllAction);
		selectAllAction.putValue(Action.NAME, "Select All");
		menu.add(selectAllAction);

		return menu;
	}

	// The following two methods allow us to find an
	// action provided by the editor kit by its name.
	private HashMap<Object, Action> createActionTable(
	        JTextComponent textComponent) {
		HashMap<Object, Action> actions = new HashMap<Object, Action>();
		Action[] actionsArray = textComponent.getActions();
		for (int i = 0; i < actionsArray.length; i++) {
			Action a = actionsArray[i];
			actions.put(a.getValue(Action.NAME), a);
		}
		return actions;
	}

	private Action getActionByName(String name) {
		return actions.get(name);
	}

	/**
	 * @param docName2 the name of the file to be added
	 */
	public void addFile(String docName2, int docNum2) {
		fileList.addItem(makeObj(docName2));
		filenameToDocNum.put(docName2, docNum2);
		fileSystem.getFile().get(docNum2).getDoc().addDocumentListener(new MyDocumentListener());
	}

	private Object makeObj(final String item) {
		return new Object() {
			public String toString() {
				return item;
			}
		};
	}

	/**
	 * @param docname2 the name of the file to be deleted
	 */
	public void deleteFile(String docname2) {
		if (fileList.getItemCount()<=1) return;
		int position = -1;
		for (int i = 0; i < fileList.getItemCount(); i++) {
			if (fileList.getItemAt(i).toString().equals(docname2)) {
			    position = i;
			    System.out.println("found position");
			    System.out.println(position);
			    break;
			}

		}
		
		fileList.removeItemAt(position);
	    filenameToDocNum.remove(docname2);
	    

	}
	
	/**
	 * change the name of the specified file
	 * @param docNum: the number/index of the file to be renamed
	 * @param newDocName: the document name to be changed to 
	 */
	public void changeFileName(int docNum, String newDocName){
	    System.out.println("triggered changefilename");
	    String oldDocName = null;
	    List <String> docNames = new ArrayList<String>();
	    for (String docName:filenameToDocNum.keySet()){
	        docNames.add(docName);
	    }
	    for (String docName :docNames){
	        if (filenameToDocNum.get(docName) == docNum){
	            oldDocName = docName;
	        }
	    }
	    filenameToDocNum.remove(oldDocName);
	    filenameToDocNum.put(newDocName,docNum);
	    System.out.println("oldfilename is "+oldDocName);
	    
	    int position = -1;
	    for (int i = 0; i < fileList.getItemCount(); i++) {
            if (fileList.getItemAt(i).toString().equals(oldDocName)) {
                position = i;
                System.out.println("position found");
                break;
            }

        }
	    fileList.insertItemAt(makeObj(newDocName),position);
	    fileList.removeItemAt(position+1);
        System.out.println("removed old item from filelist");
        
	    
	}

	/**
	 * @return current document number/index
	 */
    public Integer curDocNum() {
	    return filenameToDocNum.get(docName);
    }

	/** update the caret position in the edit area of current GUI
	 * @param offset
	 * @param len
	 */
    public void CaretPosition(int offset, int len) {
	    int curPos=editArea.getCaretPosition();
	    if (offset>=curPos) return;
	    curPos=Math.max(offset, curPos+len);
	    try{
	    	editArea.setCaretPosition(curPos);
	    }catch (Exception e){
	    	e.printStackTrace();
	    }finally{
	    }
    }
    
    /**
     * 
     * @return the last (i.e., the highest index) file in the 
     * File System that is not null
     */
    public int notNullIndex(){
        int position = fileSystem.getFile().size() - 1;
        while (position >=0){
            if (fileSystem.getFile().get(position)==null){
                position -=1;
            } else {
                break;
            }
        }
        return position;
    }

}
