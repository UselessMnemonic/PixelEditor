package edu.madrigal.pixeleditor;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Stack;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 * Defines a canvas on which to draw.
 * 
 * @author nihil
 */
public class EditorPanel extends JPanel
                         implements MouseListener, MouseMotionListener {
  private static final long serialVersionUID = -6619252965009376403L;
  //Color for grid
  private static final Color GRID_COLOR = new Color( 50, 50, 150, 30 );
  private static final int SIZE = 300;
  
	/*
	 * the buffered image we want, the color of the brush, the showGrid and
	 * modeFill, grid size, and image size
	 */
  private static final int INVERT_XOR = 0x00ffffff;
  private static final String CONFIRM_DIAG = "This will overwrite the image.\n"
                                           + "Are you sure?";
  private static final String RESIZE_DIAG = "This image is too large for the"
                                          + " canvas.\n Do you want it"
                                          + " stretched or fitted?";
  private static final String OPTION_STRETCH = "Stretched";
  private static final String OPTION_FIT = "Fit";
  private static final String OPTION_CANCEL = "Cancel";
  private static final String[] OPTIONS = new String[] { OPTION_FIT,
                                                         OPTION_STRETCH,
                                                         OPTION_CANCEL };
	private BufferedImage image;
	private Color currentColor;
	private boolean showGrid;
	private boolean fill;
	private int gridSize = 5;
	private LinkedList< BufferedImage > history;
	private int historyPtr;
	private boolean saved;
	private boolean editInProgress;
	
	/**
	 * Constructor for the editor pane
	 */
	public EditorPanel() {
	  history = new LinkedList< BufferedImage >();
	  historyPtr = -1;
	  addMouseListener( this );
	  addMouseMotionListener( this );
	  setBackground( Color.WHITE );
    setPreferredSize( new Dimension( SIZE, SIZE ) );
	  image = new BufferedImage( SIZE, SIZE, BufferedImage.TYPE_INT_ARGB );
		showGrid = true;
		saved = true;
		clearCanvas();
	}
	
	/**
	 * If the canvas has been saved to an image since the last edit.
	 * 
	 * @return whether the image has been saved
	 */
	public boolean isSaved() {
	  return saved;
	}
	
	/**
	 * Undoes the last operation on the canvas.
	 * 
	 * @return if the undo was successful
	 */
	public boolean undo() {
	  if( historyPtr > 0 ) {
	    historyPtr--;
	    Graphics g = image.getGraphics();
	    g.drawImage( history.get( historyPtr ), 0, 0, null );
      repaint();
      saved = false;
	    return true;
	  }
	  return false;
	}
	
	/**
	 * Redoes the following operation on the canvas.
	 * 
	 * @return if the redo was successful
	 */
	public boolean redo() {
	  if( historyPtr < history.size() - 1 ) {
      historyPtr++;
      Graphics g = image.getGraphics();
      g.drawImage( history.get( historyPtr ), 0, 0, null );
      repaint();
      saved = false;
      return true;
    }
    return false;
	}
	
	private void pushToHistory() {
	  BufferedImage frame = new BufferedImage( SIZE,
	                                           SIZE,
	                                           BufferedImage.TYPE_INT_ARGB );
	  Graphics g = frame.getGraphics();
	  g.drawImage( image, 0, 0, null );
	  g.dispose();
    while( history.size() > historyPtr + 1) {
      history.remove( history.size() - 1 );
    }
    historyPtr++;
    history.add( historyPtr, frame );
	  saved = false;
	}
	
	/**
	 * Sets the brush color
	 * 
	 * @param c The color to set the brush
	 */
	public void setColor(Color c) {
		currentColor = c;
	}
	
	/**
	 * Draws on the editor according to the draw rules
	 * 
	 * @param e The MouseEvent where the mouse was clicked
	 */
	protected void drawOnCanvas( MouseEvent e ) {
		
		//make a graphics object for the buffered image
		Graphics tg = image.createGraphics();
		
		//set its color to the current color
		tg.setColor(currentColor);
		
		/*
		 * since we're coloring pixels larger than the actual pixels,
		 * we have to calculate the top-left most corner of each region
		 * (a fix for the several jpanels)
		 */
		
		//lets first call xi and yi the actual coordinates
		int xi = e.getX();
		int yi = e.getY();
		
		/*
		 * perform (coordinate mod gridSize), which is the distance from the
		 * leftmost edge (dx) and topmost edge (dy)
		 */
		int dx = xi % gridSize;
		int dy = yi % gridSize;
		
		/*
		 * now we take the differences, and we get the coordinate of the
		 * top-left most corner!
		 */
		int xf = xi - dx;
		int yf = yi - dy;

		//if we're in fill mode, do a fill, otherwise draw a pixel
		if( fill )
			flood( xi, yi, tg );
		else
			drawDot( xf, yf, tg );
		
		//always repaint the image afterwards
		tg.dispose();
		repaint();
	}
	
	/**
   * A quick method for single-pixel editing
   * 
   * @param x the x-coord of the pixel
   * @param y the y-coord of the pixel
   * @param tg the graphics on which to draw
   */
  private void drawDot( int x, int y, Graphics tg ) {
    tg.fillRect( x, y, gridSize, gridSize );
  }

	/**
	 * Fill the contiguous space with a color
	 * 
	 * @param x The x-coord from which to start filling
	 * @param y The y-coord from which to start filling
	 * @param tg The graphics object on which to draw
	 */
	private void flood( int x, int y, Graphics tg ) {
	  
		//save the color in the grid box we're gonna overwrite
		int target = image.getRGB( x, y );
		int replacement = tg.getColor().getRGB();
		
		Stack< Point > points = new Stack<>();
    points.push( new Point( x, y ) );
    
    while( !points.isEmpty() ) {
      
      Point currentPoint = points.pop();
      int xi = currentPoint.x;
      int yi = currentPoint.y;
      
      if( xi < 0 || yi < 0 || xi >= SIZE || yi >= SIZE ) {
        continue;
      }
      
      int current = image.getRGB( xi, yi );
      
      if( current == target && current != replacement ) {
        tg.fillRect( xi, yi, 1, 1 );
        points.push( new Point( xi + 1, yi ) );
        points.push( new Point( xi - 1, yi ) );
        points.push( new Point( xi, yi + 1 ) );
        points.push( new Point( xi, yi - 1 ) );
      }
    }
	}
	
  /**
	 * Clears the canvas to all opaque white.
	 */
	public void clearCanvas() {
		Graphics g = image.getGraphics();
		g.setColor( Color.WHITE );
		g.fillRect( 0, 0, SIZE, SIZE );
		g.dispose();
		pushToHistory();
		repaint();
	}
	
	/**
	 * Saves the image to the given file in PNG format
	 * 
	 * @param f The file in which to save the image
	 * @throws Any IOException thrown by the write process
	 */
	public void saveImage( File f ) throws IOException {
		ImageIO.write( image, "PNG", f );
		saved = true;
	}
	
	/**
	 * Loads an image from the given file in PNG format
	 * 
	 * @param f The file from which to load the image
	 * @param parentComponent needed to display warning message
	 * 
	 * @throws any IOException thrown by the read process
	 */
	public void loadImage( File f, Component parentComponent )
	    throws IOException {
	  
	  Image loaded = ImageIO.read( f );
	  Object option;
	  int confirmation;
	  double scale;
	  
	  int srcWidth = loaded.getWidth( null );
	  int srcHeight = loaded.getHeight( null );
	  
	  if( srcWidth > SIZE || srcHeight > SIZE ) {
	    
	    option = JOptionPane.showOptionDialog( parentComponent,
	                                           RESIZE_DIAG,
	                                           "Warning",
	                                           JOptionPane.DEFAULT_OPTION,
	                                           JOptionPane.QUESTION_MESSAGE,
	                                           null,
	                                           OPTIONS,
	                                           OPTION_CANCEL );
	    
	    //if we are fitting the image
	    if( option == OPTION_FIT ) {
	      
	      if( srcWidth > srcHeight ) {
	        scale = SIZE / srcWidth;
	      }
	      else {
	        scale = SIZE / srcHeight;
	      }
	      
	      loaded = loaded.getScaledInstance( (int)( scale * srcWidth ),
                                           (int)( scale * srcHeight ),
                                           Image.SCALE_REPLICATE );
	    }
	    
	    //if we're squishing the image
	    else if( option == OPTION_STRETCH ) {
	      loaded = loaded.getScaledInstance( SIZE,
	                                         SIZE,
	                                         Image.SCALE_REPLICATE );
	    }
	    
	    //all else, like exit or cancel
	    else {
	      return;
	    }
	  }
	  
	  confirmation = JOptionPane.showConfirmDialog( parentComponent,
	                                          CONFIRM_DIAG,
	                                          "Confirm",
	                                          JOptionPane.YES_NO_OPTION );
	  
	  if( confirmation == JOptionPane.YES_OPTION ) {
	    Graphics g = image.getGraphics();
	    g.setColor( Color.WHITE );
	    g.drawRect( 0, 0, SIZE, SIZE );
	    g.drawImage( loaded, 0, 0, null );
	    g.dispose();
	    pushToHistory();
	    repaint();
	  }
	}

	/**
	 * The paint method for the editor.
	 * Draws the image as it's edited, and a grid
	 * 
	 * @param g The graphics object on which to draw
	 */
	public void paintComponent( Graphics g ) {
	  
		//always call the superclass's method
		super.paintComponent(g);
		
		//draw the buffered image
		g.drawImage( image, 0, 0, this );
		
		//this is my grid color, RGBA(50, 50, 150, 30)
		g.setColor( GRID_COLOR );
		
		/*
		 * if we want to show the grid, it's faster to draw
		 * all the vertical lines and then all the horizontal lines,
		 * and only if the grid size is greater than 1
		 */
		if( showGrid && gridSize > 1 ) {
		  for( int x = 0; x < SIZE; x += gridSize ) {
				g.drawLine( x, 0, x, SIZE );
			}
			
			for( int y = 0; y < SIZE; y += gridSize ) {
				g.drawLine( 0, y, SIZE, y );
			}
		}
	}

	/**
   * Sets the grid show option to on or off
   * 
   * @param selected Whether the grid option is on or off
   */
	public void showGrid( boolean selected ) {
		showGrid = selected;
		repaint();
	}
	
	/**
	 * Sets the fill mode to on or off
	 * 
	 * @param selected Whether the fill option is on or off
	 */
	public void setFill( boolean selected ) {
		fill = selected;
	}

	/**
	 * Inverts the image
	 */
	public void invertColors() {
	  Graphics g = image.getGraphics();
	  for( int x = 0; x < SIZE; x++ ) {
	    for( int y = 0; y < SIZE; y++ ) {
	      g.setColor( new Color( image.getRGB( x, y ) ^ INVERT_XOR ) );
	      g.fillRect( x, y, 1, 1 );
	    }
	  }
	  g.dispose();
	  pushToHistory();
		repaint();
	}

	/**
	 * Changes the grid size of the editor panel
	 * 
	 * @param intValue The grid size to use
	 */
	public void changeGridSize( int intValue ) {
		gridSize = intValue;
		repaint();
	}

  @Override
  public void mouseDragged( MouseEvent e ) {
    if( editInProgress ) {
      drawOnCanvas( e );
    }
  }

  @Override
  public void mouseMoved( MouseEvent e ) {
  }

  @Override
  public void mouseClicked( MouseEvent e ) {
  }

  @Override
  public void mousePressed( MouseEvent e ) {
    editInProgress = true;
    drawOnCanvas( e );
  }

  @Override
  public void mouseReleased( MouseEvent e ) {
    if( editInProgress ) {
      editInProgress = false;
      pushToHistory();
    }
  }

  @Override
  public void mouseEntered( MouseEvent e ) {
  }

  @Override
  public void mouseExited( MouseEvent e ) {
    if( editInProgress ) {
      editInProgress = false;
      pushToHistory();
    }
  }
}
