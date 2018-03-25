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

public class EditorPanel extends JPanel {

	private BufferedImage image;
	private Color currentColor;
	private boolean mShowGrid;
	private boolean mFill;
	private final int pixelSize = 5;
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
	
	public void setColor(Color c)
	{
		currentColor = c;
	}
	
	protected void drawOnCanvas(MouseEvent e) {
		Graphics tg = image.createGraphics();
		tg.setColor(currentColor);
		
		int xi = e.getX();
		int yi = e.getY();
		
		//we need to find the "pixel" the mouse is in. One way to do this is to perform coordinate mod pixel size, which will tell us how many pixels we are from an edge
		int dx = xi%pixelSize;
		int dy = yi%pixelSize;
		
		//we can then subtract from the coordinate we are in, giving us the top left corner of the "pixel" we are in
		int xf = xi - dx;
		int yf = yi - dy;

		if(mFill)
			doFill(xf, yf, tg);
		else
			doDot(xf, yf, tg);
		repaint();
	}
	
	private void doDot(int x, int y, Graphics tg) {
		tg.fillRect(x, y, pixelSize, pixelSize);
	}

	private void doFill(int x, int y, Graphics tg) {
		Color targetColor = new Color(image.getRGB(x, y));
		floodFillRecursive(x, y, tg, targetColor);
	}
	
	private void floodFillRecursive(int x, int y, Graphics tg, Color target)
	{
		if(x < 0 || x > imageSide-1)
			return;
		if(y < 0 || y > imageSide-1)
			return;
		if(target.equals(tg.getColor()))
			return;
		if(!new Color(image.getRGB(x, y)).equals(target))
			return;
		tg.fillRect(x, y, pixelSize, pixelSize);
		floodFillRecursive(x-pixelSize, y, tg, target);
		floodFillRecursive(x+pixelSize, y, tg, target);
		floodFillRecursive(x, y-pixelSize, tg, target);
		floodFillRecursive(x, y+pixelSize, tg, target);
	}

	public void clearCanvas()
	{
		Graphics g = image.getGraphics();
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, imageSide, imageSide);
		repaint();
	}
	
	public boolean saveImage(File f)
	{
		try
		{
			ImageIO.write(image, "PNG", f);
			return true;
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		
		g.drawImage(image, 0, 0, this);
		
		g.setColor(new Color(50, 50, 150, 30));
		int x1, y1;
		
		if(mShowGrid)
		{
			for(int x = 0; x < imageSide; x+=pixelSize)
			{
				g.drawLine(x, 0, x, imageSide);
			}
			
			for(int y = 0; y < imageSide; y+=pixelSize) {
				g.drawLine(0, y, imageSide, y);
			}
		}
	}

	public void showGrid(boolean selected) {
		mShowGrid = selected;
		repaint();
	}
	
	public void setFill(boolean selected)
	{
		mFill = selected;
	}
}
