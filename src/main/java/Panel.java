package spConsole;

import java.awt.Color;

public class Panel {

    Cell[][] tileMap;
    private int layer;
    private int width;
    private int height;
    private int offsetWidth;
    private int offsetHeight;
    
    
    public Panel(int width, int height) {
    	this.width = width;
    	this.height = height;
    	
    	tileMap = new Cell[width][height];
    	for (int x = 0; x < width; x++) {
    		for (int y = 0; y < height; y++) {
    				tileMap[x][y] = new Cell(' ', Color.black, Color.black);
    		}
    	}
    }
    
    public int width() {
    	return width;
    }
    public int height() {
    	return height;
    }
    
    public void blit(Panel other, int xSrc, int ySrc, int width, int height, int xDst, int yDst){
 	   int maxXDst = Math.min(xDst+width,this.width);
 	   int maxYDst = Math.min(yDst+height, this.height);
 	   
 	   int maxXSrc = Math.min(xSrc+width, other.width());
 	   int maxYSrc = Math.min(ySrc+height, other.height());
 	   
 	   int maxX = Math.min(maxXDst-xDst, maxXSrc-xSrc);
 	   int maxY = Math.min(maxYDst-yDst, maxYSrc-ySrc);
 	   
 	   for(int x = 0; x < maxX; x++) {
 		   for(int y = 0; y < maxY; y++) {
 		        if (other.tileMap[xSrc+x][ySrc+y] != tileMap[xDst+x][yDst+y]) {
 			        tileMap[xDst+x][yDst+y] = other.tileMap[xSrc+x][ySrc+y];
 		        }
 		   }
 	   }
    }
}
