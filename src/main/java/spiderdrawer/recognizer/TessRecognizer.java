package spiderdrawer.recognizer;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.Rectangle;

import spiderdrawer.shape.Freeform;
import net.sourceforge.tess4j.Tesseract1;
import net.sourceforge.tess4j.TesseractException;
import net.sourceforge.tess4j.TessAPI.TessPageSegMode;

public class TessRecognizer {

	private Tesseract1 instance;
	
	
	public TessRecognizer() {
		instance = new Tesseract1();
    	instance.setDatapath(".");
    	instance.setPageSegMode(TessPageSegMode.PSM_SINGLE_CHAR);
	}
	
	public Character classifyText(Freeform[] freeforms) {
    	return convertToChar(freeforms, true, true);
    }
	
	public Character classifyConnective(Freeform[] freeforms) {
    	return convertToChar(freeforms, true, false);
    }
	
	public Character classifyLetter(Freeform[] freeforms) {
    	return convertToChar(freeforms, false, true);
    }
	
	public static Rectangle rectangle(Freeform[] freeforms) {
    	int maxX = Integer.MIN_VALUE;
    	int maxY = Integer.MIN_VALUE;
    	for (int i = 0; i < freeforms.length; i++) {
    		int currentMaxX = freeforms[i].minX() + freeforms[i].maxX();
    		if (currentMaxX > maxX) {	
    			maxX = currentMaxX; 
    		}
    		int currentMaxY = freeforms[i].minY() + freeforms[i].maxY();
    		if (currentMaxY > maxY) {
    			maxY = currentMaxY; 
    		}
    	}    
    	return new Rectangle(0, 0, maxX, maxY);
	}
	
	private Character convertToChar(Freeform[] freeforms, boolean connective, boolean letter) {
    	System.out.println("num freeforms: " + freeforms.length);
    	Rectangle rect = rectangle(freeforms);
    	synchronized(this) {
	    	if (connective && letter) {
	    		instance.setTessVariable("tessedit_char_whitelist", "ABCDEFGHIJKLMNOPQRSTUVWXYZ↔→∨∧¬");
	    		instance.setLanguage("eng+con");
	    	} else if (connective) {
	    		instance.setTessVariable("tessedit_char_whitelist", "↔→∨∧¬");
	    		instance.setLanguage("con");
	    	} else { //letter == true
	    		instance.setTessVariable("tessedit_char_whitelist", "ABCDEFGHIJKLMNOPQRSTUVWXYZ");
	    		instance.setLanguage("eng");
	    	}
	    	try {
	           String result = instance.doOCR(createImage(freeforms, (int) rect.getWidth(), (int) rect.getHeight()));
	            if (result.length() > 0) {
	            	char character = '\0';
	            	for(int i = 0; i < result.length(); i++) {
	                    if(!Character.isWhitespace(result.charAt(i))) {
	                    	character = result.charAt(i);
	                    }
	                }
	            	System.out.println("\"" +result + "\"");
	            	if (character != '\0') {
	            		return character;
	            	}
	            }
	        } catch (TesseractException e) {
	            System.err.println(e.getMessage());
	        }
    	}
    	return null;
    }
	
	public static BufferedImage createImage(Freeform[] freeforms, int width, int height) {
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = bufferedImage.createGraphics();
        for (int i = 0; i < freeforms.length; i++)
        	freeforms[i].draw(g);
        return bufferedImage;
    }
}
