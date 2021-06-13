package spConsole;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.LookupOp;
import java.awt.image.ShortLookupTable;
import java.util.Arrays;

import javax.swing.JPanel;

public class SPConsole extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8874225840023145560L;
	
	//Size of console in characters
	private int width;
	private int height;
	
	//Number of layers. Defaults to 16
	private int layers;
	private int currentLayer;
	
	//Flag array to determine whether a region of the screen was updated
	private boolean[][] updateMap;
	

	private Cell[][][] cellMap;
	private Color defaultBackgroundColor;
	private Color defaultForegroundColor;
	
	//Currently only supports a single bitmap font
	private BitmapFont font;
	private int charHeight;
	private int charWidth;
	
	//screenBuffer and associated graphics object
	private BufferedImage screenBuffer;
	private Graphics2D screenGraphics;

	//Cursor location
	private int cursorX;
	private int cursorY;

	//Special Cell definitions
    static Cell empty = new Cell(' ',PColor.clear,PColor.clear);
	
    public SPConsole() {
        this(80, 45);
    }

    /**
     * Class constructor specifying the width and height in characters.
     * 
     */
    public SPConsole(int width, int height) {
    	this(width, height, 16, null);
    }
    
    public SPConsole(int width, int height, BitmapFont font) {
    	this(width, height, 16, font);
    }
    
    /**
     * Class constructor specifying the width and height in characters and the default BitmapFont
     *
     */
    public SPConsole(int width, int height, int numberOfLayers, BitmapFont font) {
        super();

        if (width < 1) {
            throw new IllegalArgumentException("width " + width + " must be greater than 0." );
        }

        if (height < 1) {
            throw new IllegalArgumentException("height " + height + " must be greater than 0." );
        }

        this.width = width;
        this.height = height;
        this.layers = numberOfLayers;
        
        defaultBackgroundColor = PColor.black;
        defaultForegroundColor = PColor.white;
        currentLayer = 0;

        updateMap = new boolean[width][height];
        cellMap = new Cell[layers][width][height];
        
    	for (int x = 0; x < width; x++) {
    		for (int y = 0; y < height; y++) {
    			updateMap[x][y] = false;
    			for (int l = 0; l < layers; l++) {
    				cellMap[l][x][y] = empty;
    			}
    		}
    	}

        if(font == null) {
        	font = new BitmapFont("talryth_square_15x15.png", 15, 15, 0);
        }
        setBitmapFont(font);
        
        screenGraphics.setBackground(defaultBackgroundColor);
       	screenGraphics.clearRect(0, 0, screenBuffer.getWidth(), screenBuffer.getHeight());
    }

    public BitmapFont getBitmapFont() {
    	return font;
    }
    
	private void setBitmapFont(BitmapFont font) {
        if(this.font == font)
        {
            return;
        }
        this.font = font;

        this.charHeight = font.getHeight();
        this.charWidth = font.getWidth();

        Dimension panelSize = new Dimension(charWidth * width, charHeight * height);
        setPreferredSize(panelSize);

        font.loadGlyphs();
        screenBuffer = new BufferedImage(panelSize.width, panelSize.height, BufferedImage.TYPE_INT_ARGB);
        if(screenGraphics != null) {
        	screenGraphics.dispose();
        }
        screenGraphics = screenBuffer.createGraphics();
	}
	
	public void update(Graphics g) {
        paint(g); 
   } 

   @Override
   public void paint(Graphics graphics) {
       if (graphics == null)
           throw new NullPointerException();


       for (int x = 0; x < width; x++) {
           for (int y = 0; y < height; y++) {
           	if (!updateMap[x][y])
           		continue;
           	
           	//Initialize background layer
           	screenGraphics.setBackground(defaultBackgroundColor);
           	screenGraphics.clearRect(x * charWidth, y * charHeight, charWidth, charHeight);
	        screenGraphics.setComposite(AlphaComposite.SrcOver);
	        
           	for (int l = 0; l < layers; l++) {
           		if (cellMap[l][x][y] == empty)
           				continue;
           		Cell cell = cellMap[l][x][y];
                BufferedImage _img;
           		if ( !cell.hasSprite() ) {
	           		char glyph = cell.glyph();
	                Color bg = cell.background();
	                Color fg = cell.foreground();
	                _img = reColor(font.glyphs[glyph], fg, bg, 0);
	                cell.setSprite(_img);
           		} else {
           			_img = cell.getSprite();
           		}
           		
                screenGraphics.drawImage(_img, x * charWidth, y * charHeight, null);
           	}
           	updateMap[x][y] = false;
           }
       }
       graphics.drawImage(screenBuffer,0,0,this);
   }

   /*
    * strategy = ? for key color (not supported yet), 0 for grayscale, 1 for alpha blending
    */
   public BufferedImage reColor(BufferedImage source, Color fgColor, Color bgColor, int strategy) {
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
   
   public void setCurrentLayer(int layer) {
	   currentLayer = layer;
   }
   
   public int getCurrentLayer() {
	   return currentLayer;
   }
   
   public int getLayers() {
	   return layers;
   }
   
   public void blit(Panel panel, int xSrc, int ySrc, int width, int height, int xDst, int yDst, int layer) {
	   int maxXDst = Math.min(xDst+width,this.width);
	   int maxYDst = Math.min(yDst+height, this.height);
	   
	   int maxXSrc = Math.min(xSrc+width, panel.width());
	   int maxYSrc = Math.min(ySrc+height, panel.height());
	   
	   int maxX = Math.min(maxXDst-xDst, maxXSrc-xSrc);
	   int maxY = Math.min(maxYDst-yDst, maxYSrc-ySrc);
	   
	   for(int x = 0; x < maxX; x++) {
		   for(int y = 0; y < maxY; y++) {
		        if (panel.tileMap[xSrc+x][ySrc+y] != cellMap[layer][xDst+x][yDst+y]) {
			        cellMap[layer][xDst+x][yDst+y] = panel.tileMap[xSrc+x][ySrc+y];
			        updateMap[xDst+x][yDst+y] = true;
		        }
		   }
	   }
   }

   public void clearLayer() {
	   clearLayer(currentLayer);
   }
   public void clearLayer(int layer) {
	   clearRect(0, 0, width, height, currentLayer);
   }
   
	public void clear() {
		for(int i = 0; i < layers; i++) {
			clearLayer(i);
		}
	}
	
	public void clearRect(int x, int y, int width, int height) {
		clearRect(x, y, width, height, currentLayer);
	}
	
	public void clearRect(int x, int y, int width, int height, int layer) {
        for (int xo = x; xo < x + width; xo++) {
            for (int yo = y; yo < y + height; yo++) {
                put(empty, xo, yo, layer);
            }
        }
	}
	
	public void clear(int x, int y) {
		put(empty, x, y, currentLayer);
	}
	
	public void clear(int x, int y, int layer) {
		put(empty, x, y, layer);
	}
	
    public void fill(char character) {
    	fill(character, defaultForegroundColor, defaultBackgroundColor, 0, 0, width, height);
    }

    public void fill(char character, Color foreground, Color background) {
    	fill(character, foreground, background, 0, 0, width, height);
    }

    public void fill(char character, int x, int y, int width, int height) {
    	fill(character, defaultForegroundColor, defaultBackgroundColor, x, y, width, height);
    }

    public void fill(char character, Color foreground, Color background, int x, int y, int width, int height) {
    	fill(character, defaultForegroundColor, defaultBackgroundColor, x, y, width, height, currentLayer);
    }
    
    public void fill(char character, Color foreground, Color background, int x, int y, int width, int height, int layer) {
        int originalCursorX = cursorX;
        int originalCursorY = cursorY;
        for (int xo = x; xo < x + width; xo++) {
            for (int yo = y; yo < y + height; yo++) {
                put(character, foreground, background, xo, yo, layer);
            }
        }
        cursorX = originalCursorX;
        cursorY = originalCursorY;
    }
    
    public void put(char character) {
        put(character, defaultForegroundColor, defaultBackgroundColor, cursorX, cursorY, currentLayer);
    }

    public void put(char character, Color foreground) {
        put(character, foreground, defaultBackgroundColor, cursorX, cursorY, currentLayer);
    }

    public void put(char character, Color foreground, Color background) {
        put(character, foreground, background, cursorX, cursorY, currentLayer);
    }

    public void put(char character, int x, int y) {
        put(character, defaultForegroundColor, defaultBackgroundColor, x, y, currentLayer);
    }
    
    public void put(char character, int x, int y, int layer) {
        put(character, defaultForegroundColor, defaultBackgroundColor, x, y, layer);
    }

    public void put(char character, int x, int y, Color foreground) {
        put(character, foreground, defaultBackgroundColor, x, y, currentLayer);
    }

    public void put(char character, Color foreground, Color background, int x, int y) {
        put(character, foreground, background, x, y, currentLayer);
    }

    public void put(char character, Color foreground, int x, int y, int layer) {
        put(character, foreground, defaultBackgroundColor, x, y, currentLayer);
    }
    
    public void put(char character, Color foreground, Color background, int x, int y, int layer) {
    	if (foreground == null) foreground = defaultForegroundColor;
        if (background == null) background = defaultBackgroundColor;

        Cell newCell = new Cell(character, foreground, background);

        if (!newCell.equals(cellMap[layer][x][y])) {
	        cellMap[layer][x][y] = newCell;
	        updateMap[x][y] = true;
        }
        cursorX = x + 1;
        cursorY = y;
    }
    
    void put(Cell newCell, int x, int y, int layer) {
    	if (!newCell.equals(cellMap[layer][x][y])) {
	        cellMap[layer][x][y] = newCell;
	        updateMap[x][y] = true;
        }
    }
    
    /*
     * Print a string at the current cursor location
     */
    public void print(String string) {
        print(string, cursorX, cursorY, defaultForegroundColor, defaultBackgroundColor);
    }

    public void print(String string, Color foreground) {
        print(string, cursorX, cursorY, foreground, defaultBackgroundColor);
    }

    public void print(String string, Color foreground, Color background) {
    	print(string, cursorX, cursorY, foreground, background);
    }


    public void print(String string, int x, int y) {
        print(string, x, y, defaultForegroundColor, defaultBackgroundColor);
    }

    public void print(String string, int x, int y, Color foreground) {
        print(string, x, y, foreground, defaultBackgroundColor);
    }

    public void print(String string, int x, int y, Color foreground, Color background) {
        if (foreground == null)
            foreground = defaultForegroundColor;

        if (background == null)
            background = defaultBackgroundColor;

        for (int i = 0; i < string.length(); i++) {
            put(string.charAt(i), foreground, background, x + i, y);
        }
    }
}


