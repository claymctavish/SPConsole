package spConsole;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import punkConsole.old.RLConsole;

/**
 * This class holds data for a font file.
 * TODO: ttf/off support, key color support
 */
public class BitmapFont {

	BufferedImage[] glyphs;
	private BufferedImage spriteSheet;
	private final String fontFilename;
	private Color keyColor;
	private int strategy;
	private int width;
	private int height;
	
	public String getFontFilename() {
		return fontFilename;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public Color getKeyColor() {
		return keyColor;
	}
	
	public BitmapFont(String filename, int width, int height, int strategy) {
		this.fontFilename = filename;
		this.width = width;
		this.height = height;
		this.keyColor = Color.black; //TODO: Actually implement this
		this.strategy = strategy;

	}
	
	public void loadGlyphs() {
		//Debug line if the font files aren't able to be located
    	//System.out.println(AsciiPanel.class.getClassLoader().getResource(""));
        try {
            spriteSheet = ImageIO.read(RLConsole.class.getClassLoader().getResource(fontFilename));
        } catch (IOException e) {
            System.err.println("loadGlyphs(): " + e.getMessage());
        }

        //spriteSheet = recolor(spriteSheet, Color.white, Color.black, 1);
        int sheetWidth = spriteSheet.getWidth();
        int sheetHeight = spriteSheet.getHeight();
        int charsPerRow = sheetWidth/width;
        int charsPerColumn = sheetHeight/height;

        glyphs = new BufferedImage[charsPerRow*charsPerColumn];
        for (int x = 0; x < charsPerRow; x++) {
            int xx = x * width;
            for (int y = 0; y < charsPerColumn; y++) {
            	int yy = y * height;
            	glyphs[y*charsPerColumn + x] = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                glyphs[y*charsPerColumn + x].createGraphics().drawImage(spriteSheet, 0, 0, width, height, xx, yy, xx + width, yy + height, null);
            }
        }
	}
	
	   public static BufferedImage recolor(BufferedImage source, Color fgColor, Color bgColor, int strategy) {
		   	int w = source.getWidth();
		   	int h = source.getHeight();
		   	
		   	BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		   	Graphics graphics = img.createGraphics();
		   	graphics.drawImage(source, 0, 0, null);
		   	
		   	int[] rgba = img.getRGB(0, 0, w, h, null,0, w);
		   	int a, r, g, b;
		   	int aa, rr, gg, bb;
		       int bga = bgColor.getAlpha();
		       int bgr = bgColor.getRed();
		       int bgg = bgColor.getGreen();
		       int bgb = bgColor.getBlue();

		       int fga = fgColor.getAlpha();
		       int fgr = fgColor.getRed();
		       int fgg = fgColor.getGreen();
		       int fgb = fgColor.getBlue();
		       
		       float ratio;
		   	switch(strategy) {

				//alpha blending
			   	case(1):
			   		for (int i = 0; i < w*h; i++) {
			   			a = (rgba[i] >> 24) & 0xff;
			   			r = (rgba[i] >> 16) & 0xff;
			   			g = (rgba[i] >> 8) & 0xff;
			   			b = (rgba[i]) & 0xff;
			   			ratio = a / 255f;
			   			aa = (int) ((ratio * fga)+((1-ratio)*bga));
			   			rr = (int) ((ratio * fgr)+((1-ratio)*bgr));
			   			gg = (int) ((ratio * fgg)+((1-ratio)*bgg));
			   			bb = (int) ((ratio * fgb)+((1-ratio)*bgb));
			   			rgba[i] = (aa & 0xff) << 24 |
			 					  (rr & 0xff) << 16 |
			 					  (gg & 0xff) << 8 |
			 					  (bb & 0xff);
			   		}
			   		break;
			   		
			   	//grayscale blending
				default:
			   	case(0):
			   		for (int i = 0; i < w*h; i++) {
			   			g = (rgba[i]) & 0xff;
			   			ratio = g / 255f;
			   			aa = (int) ((ratio * fga)+((1-ratio)*bga));
			   			rr = (int) ((ratio * fgr)+((1-ratio)*bgr));
			   			gg = (int) ((ratio * fgg)+((1-ratio)*bgg));
			   			bb = (int) ((ratio * fgb)+((1-ratio)*bgb));
			   			rgba[i] = (aa & 0xff) << 24 |
			   					  (rr & 0xff) << 16 |
			   					  (gg & 0xff) << 8 |
			   					  (bb & 0xff);
			   		}
			   		break;
		   	}
		   	img.setRGB(0, 0, w, h, rgba,0, w);
		   	return img;
		   }
}
