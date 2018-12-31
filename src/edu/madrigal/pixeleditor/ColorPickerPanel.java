package edu.madrigal.pixeleditor;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseEvent;

import javax.swing.JPanel;

/**
 * A color picker that displays a range of colors to select from
 */
public class ColorPickerPanel extends JPanel {

  private static final long serialVersionUID = -3485421816975836165L;
  // HSB coordinate identifiers
  private static final int H_COORD = 0;
  private static final int S_COORD = 1;
  private static final int B_COORD = 2;

  // default color values
  private static final int DEFAULT_H = 180;
  private static final int DEFAULT_S = 50;
  private static final int DEFAULT_B = 80;

  // number of hsb coords
  private static final int NUM_HSB_COORDS = 3;
  
  //drawing constants
  private static final int CROSSHAIR_SIZE = 16;
  private static final int CROSSHAIR_OFFSET = CROSSHAIR_SIZE / 2;
  private static final int MAX_WIDTH = 360;
  private static final int MAX_HEIGHT = 100;
  /*
   * Saves the HSB color, and the X and Y coords of the cross hairs, which is
   * very important because the cross hair will jump around unsmoothly if we
   * don't look at them separately.
   */
  private float[] hsbCoordinates;
  private int xCoord, yCoord;

  /**
   * Constructor for the Color Picker
   */
  public ColorPickerPanel() {
    setPreferredSize( new Dimension( MAX_WIDTH, MAX_HEIGHT ) );
    hsbCoordinates = new float[NUM_HSB_COORDS];
    setColor( DEFAULT_H, DEFAULT_S, DEFAULT_B );
  }
  
  /**
   * The currently selected color
   * 
   * @return The color picked on the panel
   */
  public Color getColor() {
    return Color.getHSBColor( getHue(), getSaturation(), getBrightness() );
  }

  /**
   * The current hue of the selected color
   * 
   * @return The current Hue value
   */
  public float getHue() {
    return hsbCoordinates[H_COORD];
  }

  /**
   * Set the hue of the color picker
   * 
   * @param h the Hue of the color picker, from 0.0 to 1.0. If the argument
   *          falls outside these bounds, they are taken as 0 or 1 respectively.
   */
  public void setHue( float h ) {

    if( h < 0.0f ) {
      h = 0.0f;
    }

    else if( h > 1.0f ) {
      h = 1.0f;
    }

    hsbCoordinates[H_COORD] = h;
    xCoord = (int) ( h * getWidth() );
    repaint();
  }

  /**
   * The current saturation of the selected color
   * 
   * @return The current Saturation value
   */
  public float getSaturation() {
    return hsbCoordinates[S_COORD];
  }

  /**
   * Set the saturation of the color picker
   * 
   * @param s the Saturation of the color picker, from 0.0 to 1.0. If the
   *          argument falls outside these bounds, they are taken as 0 or 1
   *          respectively.
   */
  public void setSaturation( float s ) {

    if( s < 0.0f ) {
      s = 0.0f;
    }

    else if( s > 1.0f ) {
      s = 1.0f;
    }

    hsbCoordinates[S_COORD] = s;
    yCoord = (int) ( s * getHeight() );
    repaint();
  }

  /**
   * The current hue of the selected color
   * 
   * @return The current Hue value
   */
  public float getBrightness() {
    return hsbCoordinates[B_COORD];
  }

  /**
   * Set the hue of the color picker
   * 
   * @param h the Hue of the color picker, from 0.0 to 1.0. If the argument
   *          falls outside these bounds, they are taken as 0 or 1 respectively.
   */
  public void setBrightness( float b ) {

    if( b < 0.0f ) {
      b = 0.0f;
    }

    else if( b > 1.0f ) {
      b = 1.0f;
    }

    hsbCoordinates[B_COORD] = b;
    repaint();
  }

  /**
   * Sets the color of the color picker
   * 
   * @param color The color to reflect on the color picker
   */
  public void setColor( Color color ) {
    Color.RGBtoHSB( color.getRed(),
                    color.getGreen(),
                    color.getBlue(),
                    hsbCoordinates );

    xCoord = (int) ( hsbCoordinates[H_COORD] * getWidth() );
    yCoord = (int) ( hsbCoordinates[S_COORD] * getHeight() );
    repaint();
  }

  /**
   * Sets the color of the color picker, given the HSB model provided by Java
   * 
   * @param h the Hue to reflect on the color picker, from 0.0 to 1.0
   * @param s the Saturation to reflect on the color picker, from 0.0 to 1.0
   * @param b the Brightness to reflect on the color picker, from 0.0 to 1.0
   */
  public void setColor( float h, float s, float b ) {
    setHue(h);
    setSaturation(s);
    setBrightness(b);
  }

  /**
   * Sets the color of the color picker, given the RGB model provided by Java.
   * 
   * @param r the Red value to reflect on the color picker, from 0 to 255
   * @param g the Green value to reflect on the color picker, from 0 to 255
   * @param b the Blue value to reflect on the color picker, from 0 to 255
   */
  public void setColor( int r, int g, int b ) {
    setColor( new Color( r, g, b ) );
  }

  /**
   * Updates the color given the mouse event inside the panel.
   * 
   * @param e The mouse event in the panel
   */
  public void updateColor( MouseEvent e ) {

    // get those coords
    xCoord = e.getX();
    yCoord = e.getY();

    // check x bounds
    if( xCoord > getWidth() ) {
      xCoord = getWidth();
    } else if( xCoord < 0 ) {
      xCoord = 0;
    }

    // check y bounds
    if( yCoord > getHeight() ) {
      yCoord = getHeight();
    } else if( yCoord < 0 ) {
      yCoord = 0;
    }

    // set the H and S values to the particular values represented by X and Y
    hsbCoordinates[H_COORD] = (float) xCoord / getWidth();
    hsbCoordinates[S_COORD] = (float) yCoord / getHeight();
    repaint();
  }

  /**
   * Paints the color picker across the panel.
   * 
   * @param g The graphics object with which to paint
   */
  public void paintComponent( Graphics g ) {

    // always call the superclass's method before your code
    super.paintComponent( g );

    // make floats to save the h, s, and b values as we scan the pane
    float h, s, b = hsbCoordinates[B_COORD];

    int width = getWidth();
    int height = getHeight();

    xCoord = (int)( width * hsbCoordinates[H_COORD] );
    yCoord = (int)( height * hsbCoordinates[S_COORD] );
    
    // for every x column...
    for( int x = 0; x <= width; x++ ) {

      // get the particular hue for that column
      h = (float) x / width;

      // and for every y row...
      for( int y = height; y >= 0; y-- ) {

        // get that particular saturation for that row
        s = (float) y / height;

        // for each coordinate, set the pixel to the HSB value used
        g.setColor( Color.getHSBColor( h, s, b ) );
        g.drawLine( x, y, x, y );
      }
    }

    //The way I draw the crosshair is...
    
    //...take a contrasting brightness and saturation for the crosshair...
    g.setColor( Color.getHSBColor( hsbCoordinates[H_COORD],
                                   b,
                                   1.0f - b ) );

    //...and I draw a line west, east, south, and west of the pixel...
    g.drawLine( 0, yCoord, xCoord - CROSSHAIR_OFFSET, yCoord );
    g.drawLine( xCoord + CROSSHAIR_OFFSET, yCoord, width, yCoord );
    g.drawLine( xCoord, height, xCoord, yCoord + CROSSHAIR_OFFSET );
    g.drawLine( xCoord, yCoord - CROSSHAIR_OFFSET, xCoord, 0 );
    
    //...blow up the color that the user selected...
    g.setColor( Color.getHSBColor( hsbCoordinates[H_COORD],
                                   hsbCoordinates[S_COORD],
                                   hsbCoordinates[B_COORD] ) );
    
    g.fillRect( xCoord - CROSSHAIR_OFFSET, yCoord - CROSSHAIR_OFFSET,
                CROSSHAIR_SIZE, CROSSHAIR_SIZE );
    
    //... and draw a box around!
    g.setColor( Color.getHSBColor( hsbCoordinates[H_COORD],
                                   b,
                                   1.0f - b ) );
    g.drawRect( xCoord - CROSSHAIR_OFFSET, yCoord - CROSSHAIR_OFFSET,
                CROSSHAIR_SIZE, CROSSHAIR_SIZE );
    
  }
}