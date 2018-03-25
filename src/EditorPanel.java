import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class EditorPanel extends JPanel {

	//the buffered image we want, the color of the brush, the showGrid and modeFill, grid size, and image size
	private BufferedImage image;
	private Color currentColor;
	private boolean mShowGrid;
	private boolean mFill;
	private int gridSize = 5;
	private final int imageSide = 300;
	
	public EditorPanel()
	{
		image = new BufferedImage(imageSide, imageSide, BufferedImage.TYPE_INT_ARGB);
		mShowGrid = true;
		clearCanvas();
		addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseDragged(MouseEvent e) {
				drawOnCanvas(e);
			}
		});
		
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				drawOnCanvas(e);
			}
		});
	}
	
	//sets the brush color
	public void setColor(Color c)
	{
		currentColor = c;
	}
	
	//when the mouse is clicked, draws based on the mode
	protected void drawOnCanvas(MouseEvent e) {
		
		//make a graphics object for the buffered image
		Graphics tg = image.createGraphics();
		
		//set its color to the current color
		tg.setColor(currentColor);
		
		//since we're coloring pixels larger than the actual pixels,
		//we have to calculate the top-left most corner of each region (a fix for the several jpanels)
		
		//lets first call xi and yi the actual coordinates
		int xi = e.getX();
		int yi = e.getY();
		
		//perform (coordinate mod gridSize), which is the distance from the leftmost edge (dx) and topmost edge (dy)
		int dx = xi % gridSize;
		int dy = yi % gridSize;
		
		//now we take the differences, and we get the coordinate of the top-left most corner!
		int xf = xi - dx;
		int yf = yi - dy;

		//if we're in fill mode, do a fill, otherwise draw a pixel
		if(mFill)
			doFill(xf, yf, tg);
		else
			doDot(xf, yf, tg);
		
		//always repaint the image afterwards
		repaint();
	}
	
	//if we're drawing a pixel, fill the box from the topleft most corner
	//with a square the gridsize in side length
	private void doDot(int x, int y, Graphics tg) {
		tg.fillRect(x, y, gridSize, gridSize);
	}

	//if we're doing a fill, prepare for a recursive fill
	private void doFill(int x, int y, Graphics tg) {
		//save the color in the grid box we're gonna overwrite
		Color targetColor = new Color(image.getRGB(x, y));
		
		//the grapics object is already set the color we're gonna fill with,
		//now call a recursive fill at that starting coordinate
		floodFillRecursive(x, y, tg, targetColor);
	}
	
	/*
	 * a recursive fill works by this algorithm:
	 * 
	 * -If the color we're gonna overwrite is the same
	 * 		as the color we're using, stop
	 * 
	 * -If we're beyond the image boundaries, stop
	 * 
	 * -If the color at the pixel we're looking at is not
	 * 		the color we're trying to overwrite, stop
	 * 
	 * -Now set the color at the pixel we're looking at to
	 * 		the color we want.
	 * 
	 * -Now look up, down, left, and right, and call a recursive fill on those
	 */
	private void floodFillRecursive(int x, int y, Graphics tg, Color target)
	{

		if( target.equals(tg.getColor()) && (target.getAlpha() == tg.getColor().getAlpha()) )
			return;
		
		if(x < 0 || x > imageSide-1)
			return;
		if(y < 0 || y > imageSide-1)
			return;
		
		if(!new Color(image.getRGB(x, y)).equals(target))
			return;
		
		tg.fillRect(x, y, gridSize, gridSize);
		
		try{floodFillRecursive(x-gridSize, y, tg, target);}catch(Exception e){}
		try{floodFillRecursive(x+gridSize, y, tg, target);}catch(Exception e){}
		try{floodFillRecursive(x, y-gridSize, tg, target);}catch(Exception e){}
		try{floodFillRecursive(x, y+gridSize, tg, target);}catch(Exception e){}
	}

	//clears the canvas to all white
	public void clearCanvas()
	{
		Graphics g = image.getGraphics();
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, imageSide, imageSide);
		repaint();
	}
	
	//saves the image to the given file in PNG form
	public void saveImage(File f) throws IOException
	{
		ImageIO.write(image, "PNG", f);
	}

	/*
	 * The paint function for this object
	 * Specialized to draw a grid over the image if
	 * that is selected
	 */
	@Override
	public void paintComponent(Graphics g)
	{
		//aways call the superclass's method
		super.paintComponent(g);
		
		//draw the buffered image
		g.drawImage(image, 0, 0, this);
		
		//this is my grid color, RGBA(50, 50, 150, 30)
		g.setColor(new Color(50, 50, 150, 30));
		
		//if we want to show the grid, it's faster to draw
		//all the vertical lines and then all the horizontal lines
		if(mShowGrid)
		{
			for(int x = 0; x < imageSide; x+=gridSize)
			{
				g.drawLine(x, 0, x, imageSide);
			}
			
			for(int y = 0; y < imageSide; y+=gridSize) {
				g.drawLine(0, y, imageSide, y);
			}
		}
	}

	//setters to set the grid and fill options
	public void showGrid(boolean selected) {
		mShowGrid = selected;
		repaint();
	}
	
	public void setFill(boolean selected)
	{
		mFill = selected;
	}

	public void invertColors() {
		Color currentColor;
		Color invertedColor;
		Graphics g = image.createGraphics();
		
		for(int x = 0; x < imageSide; x+=gridSize)
		{
			for(int y = 0; y < imageSide; y+=gridSize)
			{
				currentColor = new Color(image.getRGB(x, y));
				invertedColor = new Color(currentColor.getRGB() ^ 0x00ffffff);
				g.setColor(invertedColor);
				g.fillRect(x, y, gridSize, gridSize);
			}
		}
		repaint();
	}

	public void changeGridSize(int intValue) {
		gridSize = intValue;
		repaint();
	}
}
