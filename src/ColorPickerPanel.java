import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseEvent;

import javax.swing.JPanel;

@SuppressWarnings("serial")
public class ColorPickerPanel extends JPanel {

	//saves the HSB color, and the X and Y coords of the crosshairs, which is very imporntant because
	//the crosshair will jump around unsmoothly if we don't look at them seperately
	private float[] mHSBCoordinates;
	private int mXCoord, mYCoord;
	
	//instantiate with a float and set the default color for now
	public ColorPickerPanel()
	{
		mHSBCoordinates = new float[3];
		setColor(180, 50, 80);
	}
	
	//when the panel is clicked, its given this mouse event will tells it the coordinates it was clicked on
	//with respect to its origin
	public void updateColor(MouseEvent e)
	{
		//get those coords
		mXCoord = e.getX();
		mYCoord = e.getY();
		
		//make sure they don't exceed bounds!
		//the pane is 360 wide to represent all 360 degrees,
		//and 100 tall to represent all brightnesses
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
		
		//set the HS values to the particular values represented by X and Y
		mHSBCoordinates[0] = mXCoord/360.0f;
		mHSBCoordinates[1] = mYCoord/100.0f;
	}
	
	//these getters just give the integer values of the HSB
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
	
	//if given a color, the pane will set its color to that,
	//where h and s are directly usable as x and y, and the color
	//values can be calculated
	public void setColor(int h, int s, int b)
	{
		mHSBCoordinates[0] = h/360.0f;
		mHSBCoordinates[1] = s/100.0f;
		mHSBCoordinates[2] = b/100.0f;
		mXCoord = h;
		mYCoord = s;
	}
	
	//rewrite the paint method to show the rainbow and crosshair
	@Override
	public void paintComponent(Graphics g)
	{
		//always call the superclass's method before your code
		super.paintComponent(g);
		
		//make floats to save the h, s, and b values as we scan the pane
		float h, s, b;
		b = mHSBCoordinates[2];
		
		//for every x column...
		for(int x = 0; x < 360; x++)
		{
			//get the particular hue for that column
			h = x/360.0f;
			
			//and for every y row...
			for(int y = 100; y >= 0; y--)
			{
				//get that particular saturation for that row
				s = y/100.0f;
				
				//for each coordinate, set the pixel to the HSB value used
				g.setColor(Color.getHSBColor(h, s, b));
				g.drawLine(x, y, x, y);
			}
		}
		
		//the way I draw the crosshair is, I take the opposite brightness, hue, and saturation, and use that color
		g.setColor(Color.getHSBColor(1.0f-mHSBCoordinates[0], 1.0f-mHSBCoordinates[1], 1.0f-b));
		
		//and I draw a 3-wide line west, east, south, and west of the pixel
		g.drawLine(mXCoord-3, mYCoord, mXCoord-1, mYCoord);
		g.drawLine(mXCoord+3, mYCoord, mXCoord+1, mYCoord);
		g.drawLine(mXCoord, mYCoord+3, mXCoord, mYCoord+1);
		g.drawLine(mXCoord, mYCoord-1, mXCoord, mYCoord-3);
	}
	
}
