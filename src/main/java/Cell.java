package spConsole;

import java.awt.Color;
import java.awt.image.BufferedImage;

public class Cell {
	private char glyph;
	public char glyph() { return glyph; }
	
	private Color fg;
	public Color foreground() {
		return fg;
	}
	public void setForeground(Color newForeground) {
		fg = newForeground;
	}
	
	private Color bg;
	public Color background() {
		return bg;
	}
	public void setBackground(Color newBackground) {
		bg = newBackground;
	}
	
	Cell(char glyph, Color foregroundColor, Color backgroundColor){
		this.glyph = glyph;
		this.fg = foregroundColor == null ? PColor.white : foregroundColor;
		this.bg = backgroundColor == null ? PColor.black : backgroundColor;
	}
	
	private BufferedImage sprite;
	public BufferedImage getSprite() {
		return sprite;
	}
	public void setSprite(BufferedImage sprite) {
		this.sprite = sprite;
	}
	public boolean hasSprite() {
		return sprite != null;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((bg == null) ? 0 : bg.hashCode());
		result = prime * result + ((fg == null) ? 0 : fg.hashCode());
		result = prime * result + glyph;
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Cell other = (Cell) obj;
		if (!bg.equals(other.bg))
			return false;
		if (!fg.equals(other.fg))
			return false;
		if (glyph != other.glyph)
			return false;
		return true;
	}

}
