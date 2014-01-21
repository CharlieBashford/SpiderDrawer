package spiderdrawer.ui;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import java.awt.FileDialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

/**
 *
 * @author charliebashford
 */
public class MainForm extends JFrame {
	
	private DrawingPanel drawingPanel;
	
    /**
     * Creates new form MainForm
     */
    public MainForm() {
        initComponents();
    }
    
    private void clearMenuItemClicked() {
    	drawingPanel.clearDrawable();
    	drawingPanel.repaint();
    }
    
    private void exportMenuItemClicked() {
    	FileDialog fileDialog = new FileDialog(this, "Save", FileDialog.SAVE);
        fileDialog.setFilenameFilter(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".png");
            }
        });
        fileDialog.setFile("output.png");
        fileDialog.setVisible(true);
        String fileName = fileDialog.getFile();
    	if (fileName != null) {
    		File outputFile;
    		if (fileName.lastIndexOf('.') != -1 && fileName.substring(fileName.lastIndexOf('.') + 1).equals("png")) {
    			outputFile = new File(fileDialog.getDirectory() + fileDialog.getFile());
    		} else {
    			outputFile = new File(fileDialog.getDirectory() + fileDialog.getFile() + ".png");
    		}
    		try {
    			ImageIO.write(drawingPanel.createImage(), "png", outputFile);
    		} catch (IOException e) {
    			JOptionPane.showMessageDialog(null, "Error exporting file.");
    		} 
    		//log.append("Saving: " + file.getName() + ".");
    	} else {
    		//log.append("Saving command cancelled by user.");
    	}	
    }
    
    private void initComponents() {
        setSize(900, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        addMenu();
        addDrawingFrame();        
    }
    
    private void addDrawingFrame() {
    	drawingPanel = new DrawingPanel();
        this.setContentPane(drawingPanel);
    }
    
    private void addMenu() {
    	JMenuBar menuBar = new JMenuBar();
    	setJMenuBar(menuBar);
    	
        JMenu optionsMenu = new JMenu();
        optionsMenu.setText("Options");        
        menuBar.add(optionsMenu);
        
        JMenuItem exportMenuItem = new JMenuItem();
        exportMenuItem.setText("Export");
        exportMenuItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				exportMenuItemClicked();				
			}
        });
        optionsMenu.add(exportMenuItem);
        
        JMenuItem clearMenuItem = new JMenuItem();
        clearMenuItem.setText("Clear");
        clearMenuItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				clearMenuItemClicked();				
			}
        });
        optionsMenu.add(clearMenuItem);

        
        
        /*Test menu starts */
        JMenu testMenu = new JMenu();
        testMenu.setText("Test");
        menuBar.add(testMenu);
        
        JMenuItem circleMenuItem = new JMenuItem();
        circleMenuItem.setText("Circle");
        circleMenuItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				drawingPanel.addCircle();				
			}
        });
        testMenu.add(circleMenuItem);
        
        JMenuItem lineMenuItem = new JMenuItem();
        lineMenuItem.setText("Line");
        lineMenuItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				drawingPanel.addLine();					
			}
        });
        testMenu.add(lineMenuItem);
        
        JMenuItem labelMenuItem = new JMenuItem();
        labelMenuItem.setText("Label");
        labelMenuItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				drawingPanel.addLabel();					
			}
        });
        testMenu.add(labelMenuItem);
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        new MainForm().setVisible(true);
    }


}
