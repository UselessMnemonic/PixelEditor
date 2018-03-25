import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseEvent;

import javax.swing.JPanel;

@SuppressWarnings("serial")
public class ColorPickerPanel extends JPanel {

	private float[] mHSBCoordinates;
	private int mXCoord, mYCoord;
	
	public ColorPickerPanel()
	{
		mHSBCoordinates = new float[3];
		setColor(180, 50, 80);
	}
	
	public void updateColor(MouseEvent e)
	{
		mXCoord = e.getX();
		mYCoord = e.getY();
		
		if(mXCoord >= 360)
		{
			mXCoord = 360;
		}
		else if(mXCoord <= 0)
		{
			mXCoord = 0;
		}
		
		if(mYCoord >= 100)
		{
			mYCoord = 100;
		}
		else if(mYCoord <= 0)
		{
			mYCoord = 0;
		}
		
		mHSBCoordinates[0] = mXCoord/360.0f;
		mHSBCoordinates[1] = mYCoord/100.0f;
	}
	
	public int getCurrentHue()
	{
		return (int)(mHSBCoordinates[0]*360.0);
	}
	
	public int getCurrentSaturation()
	{
		return (int)(mHSBCoordinates[1]*100.0);
	}
	
	public int getCurrentBrightness()
	{
		return (int)(mHSBCoordinates[2]*100.0);
	}
	
	public void setColor(int h, int s, int b)
	{
		mHSBCoordinates[0] = h/360.0f;
		mHSBCoordinates[1] = s/100.0f;
		mHSBCoordinates[2] = b/100.0f;
		mXCoord = h;
		mYCoord = s;
	}
	
	@Override
	public void paintComponent(Graphics g)
	{
		
		super.paintComponent(g);
		float h, s, b;
		b = mHSBCoordinates[2];
		
		for(int x = 0; x < 360; x++)
		{
			h = x/360.0f;
			
			for(int y = 100; y >= 0; y--)
			{
				s = y/100.0f;
				g.setColor(Color.getHSBColor(h, s, b));
				g.drawLine(x, y, x, y);
			}
		}
		g.setColor(Color.getHSBColor(1.0f-mHSBCoordinates[0], 1.0f-mHSBCoordinates[1], 1.0f-b));
		
		g.drawLine(mXCoord-3, mYCoord, mXCoord-1, mYCoord);
		g.drawLine(mXCoord+3, mYCoord, mXCoord+1, mYCoord);
		g.drawLine(mXCoord, mYCoord+3, mXCoord, mYCoord+1);
		g.drawLine(mXCoord, mYCoord-1, mXCoord, mYCoord-3);
	}
	
}
