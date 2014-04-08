package spiderdrawer.ui;

import javax.imageio.ImageIO;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import net.sourceforge.tess4j.TessAPI.TessPageSegMode;

import java.awt.FileDialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 *
 * @author charliebashford
 */
public class MainForm extends JFrame {
	
	private DrawingPanel drawingPanel;
    private MessageBox messageBox = null;

	
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
    
    private void loadMenuItemClicked() {
    	String filename = FileChooserForm.showDialog(this, new File(".").getAbsolutePath());
    	if (filename == null || filename.equals("")) {
    		return;
    	}
		String content = null;
		try {
			byte[] encoded = Files.readAllBytes(Paths.get(filename + ".spi"));
	    	content = Charset.defaultCharset().decode(ByteBuffer.wrap(encoded)).toString();
		} catch (IOException e) {
			e.printStackTrace();
		}
		drawingPanel.loadDrawablesString(content);
		drawingPanel.repaint();
    }
    
    private void saveMenuItemClicked() {
    	String name;
    	int result = JOptionPane.NO_OPTION;
    	File file;
    	do {
			do {
		    	name = JOptionPane.showInputDialog(this, "Enter name:", "Save", JOptionPane.PLAIN_MESSAGE);
		    	if (name == null) {
		    		return;
		    	}
			} while (name.equals(""));
			file = new File(name + ".spi");
			if(file.exists() && !file.isDirectory()) {
				result = JOptionPane.showConfirmDialog(null, "The file already exists! Want to choose a new name?", "Save", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
				if (result == JOptionPane.CANCEL_OPTION) {
					return;
				}
			}
    	} while (result == JOptionPane.YES_OPTION);
    	try {	
			
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(drawingPanel.drawablesAsString());
			bw.close();
    	} catch (IOException x) {
    	    System.err.format("IOException: %s%n", x);
    	}
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
    
    private void ocrMenuItemClicked() {
    	Tesseract instance = Tesseract.getInstance();
    	instance.setPageSegMode(TessPageSegMode.PSM_SINGLE_CHAR);
    	try {
            String result = instance.doOCR(drawingPanel.createImage());
            if (result.length() > 0) {
            	drawingPanel.addLabel(result.charAt(0));
            }
        } catch (TesseractException e) {
            System.err.println(e.getMessage());
        }
    }
    
    private void initComponents() {
        setSize(900, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        
        addDrawingFrame();
        addMenu();      
        
        /*messageBox = new MessageBox("Hello");
    	add(messageBox);*/
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
        
        JMenuItem loadMenuItem = new JMenuItem();
        loadMenuItem.setText("Load");
        loadMenuItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				loadMenuItemClicked();		
			}
        });
        optionsMenu.add(loadMenuItem);
        
        JMenuItem saveMenuItem = new JMenuItem();
        saveMenuItem.setText("Save");
        saveMenuItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				saveMenuItemClicked();
			}
        });
        optionsMenu.add(saveMenuItem);
        
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
        
        JMenuItem pointMenuItem = new JMenuItem();
        pointMenuItem.setText("Point");
        pointMenuItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				drawingPanel.addPoint();					
			}
        });
        testMenu.add(pointMenuItem);
        
        JMenuItem labelMenuItem = new JMenuItem();
        labelMenuItem.setText("Label");
        labelMenuItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				drawingPanel.addLabel();					
			}
        });
        testMenu.add(labelMenuItem);
        
        JMenuItem boxMenuItem = new JMenuItem();
        boxMenuItem.setText("Box");
        boxMenuItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				drawingPanel.addBox();					
			}
        });
        testMenu.add(boxMenuItem);
        
        JMenuItem connectiveMenuItem = new JMenuItem();
        connectiveMenuItem.setText("Connective");
        connectiveMenuItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				drawingPanel.addConnective();					
			}
        });
        testMenu.add(connectiveMenuItem);
        
        JMenuItem ocrMenuItem = new JMenuItem();
        ocrMenuItem.setText("OCR");
        ocrMenuItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				ocrMenuItemClicked();					
			}
        });
        testMenu.add(ocrMenuItem);
        
        JMenuItem checkLabelMenuItem = new JMenuItem();
        checkLabelMenuItem.setText("Check Label");
        checkLabelMenuItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				drawingPanel.checkLabel();					
			}
        });
        testMenu.add(checkLabelMenuItem);
        
        final JMenuItem recognitionMenuItem = new JMenuItem();
        if (drawingPanel.isRecognition())
        	recognitionMenuItem.setText("Recognition Off");
        else
        	recognitionMenuItem.setText("Recognition On");
        recognitionMenuItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				if (drawingPanel.isRecognition()) {
					drawingPanel.turnOffRecognition();
		        	recognitionMenuItem.setText("Recognition On");
				} else {
					drawingPanel.turnOnRecognition();
		        	recognitionMenuItem.setText("Recognition Off");	
				}
			}
        });
        testMenu.add(recognitionMenuItem);
        
        final JMenuItem shadingRecognitionMenuItem = new JMenuItem();
        if (drawingPanel.isShadingRecognition())
        	shadingRecognitionMenuItem.setText("Shading Off");
        else
        	shadingRecognitionMenuItem.setText("Shading On");
        shadingRecognitionMenuItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				if (drawingPanel.isShadingRecognition()) {
					drawingPanel.turnOffShadingRecognition();
					shadingRecognitionMenuItem.setText("Shading On");
				} else {
					drawingPanel.turnOnShadingRecognition();
					shadingRecognitionMenuItem.setText("Shading Off");	
				}
			}
        });
        testMenu.add(shadingRecognitionMenuItem);
        
        final JMenuItem connectiveRecognitionMenuItem = new JMenuItem();
        if (drawingPanel.isConnectiveRecognition())
        	connectiveRecognitionMenuItem.setText("Connective Off");
        else
        	connectiveRecognitionMenuItem.setText("Connective On");
        connectiveRecognitionMenuItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				if (drawingPanel.isConnectiveRecognition()) {
					drawingPanel.turnOffConnectiveRecognition();
					connectiveRecognitionMenuItem.setText("Connective On");
				} else {
					drawingPanel.turnOnConnectiveRecognition();
					connectiveRecognitionMenuItem.setText("Connective Off");	
				}
			}
        });
        testMenu.add(connectiveRecognitionMenuItem);
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        new MainForm().setVisible(true);
    }


}
