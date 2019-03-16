package edu.madrigal.pixeleditor;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;

import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * The window for the pixel editor
 * 
 * @author nihil
 */
public class PixelEditorWindow implements WindowListener {

  // visuals
  private static final Color SELECTED_BORDER_COLOR = new Color( 255, 255, 0 );
  private static final int SELECTED_BORDER_SIZE = 2;

  private static final int HUE_MAX = 360;
  private static final int SATUR_MAX = 100;
  private static final int BRIGHT_MAX = 100;

  private static final int RGBA_MAX = 255;

  private static final int COLOR_DISPLAY_WIDTH = 42;
  private static final int COLOR_DISPLAY_HEIGHT = 26;

  private static final Color DEFAULT_COLOR_A = new Color( 40, 50, 80, 255 );
  private static final Color DEFAULT_COLOR_B = new Color( 200, 50, 100 );

  // update codes
  private static final int SRC_HSB = 0;
  private static final int SRC_RGB = 1;
  private static final int SRC_PICKER = 2;
  private static final int SRC_HEX = 3;
  private static final int SRC_OTHER = 4;

  // labels
  private static final String TITLE = "Pixel Editor";
  private static final String HUE_NAME = "Hue";
  private static final String SATURATION_NAME = "Saturation";
  private static final String BRIGHTNESS_NAME = "Brightness";
  private static final String RED_NAME = "Red";
  private static final String BLUE_NAME = "Blue";
  private static final String GREEN_NAME = "Green";
  private static final String ALPHA_NAME = "Opacity";
  private static final String HEX_NAME = "Hex Code";
  private static final String GRID_NAME = "Show Grid";
  private static final String SIZE_NAME = "Grid Size";
  private static final String FILL_NAME = "Fill Mode";
  
  //menu items and key combos
  private static final String CLEAR_NAME = "Clear";
  private static final KeyStroke CLEAR_COMBO = KeyStroke.getKeyStroke( 
      KeyEvent.VK_C, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() );
  
  private static final String INVERT_NAME = "Invert";
  private static final KeyStroke INVERT_COMBO = KeyStroke.getKeyStroke( 
      KeyEvent.VK_I, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() );
  
  private static final String SAVE_NAME = "Save...";
  private static final KeyStroke SAVE_COMBO = KeyStroke.getKeyStroke( 
      KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() );
  
  private static final String OPEN_NAME = "Open...";
  private static final KeyStroke OPEN_COMBO = KeyStroke.getKeyStroke( 
      KeyEvent.VK_O, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() );
  
  private static final String UNDO_NAME = "Undo";
  private static final KeyStroke UNDO_COMBO = KeyStroke.getKeyStroke( 
      KeyEvent.VK_Z, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() );
  
  private static final String REDO_NAME = "Redo";
  private static final KeyStroke REDO_COMBO = KeyStroke.getKeyStroke( 
      KeyEvent.VK_X, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() );

  // this is the actual window
  private JFrame pixelEditorFrame;

  // this is the panel that we are editing into
  private EditorPanel editorPanel;

  // these are the little boxes that show the current color selection
  private JPanel selectedColorDisplay;

  // this is the panel that shows the color picker
  private ColorPickerPanel colorPickerPanel;

  // these are the number fields that let us fine tune values for the HSB and
  // RGB-A values
  private JSpinner hueSpinner, saturationSpinner, brightnessSpinner,
                   redSpinner, greenSpinner, blueSpinner, alphaSpinner;

  // this is the text field that displays or allows us to enter Web colors (HEX)
  private JTextField hexCodeField;

  // prevents loops when updating several components
  private boolean updateInProgress;

  /**
   * Launch the application.
   */
  public static void main( String[] args ) {
    System.setProperty( "apple.laf.useScreenMenuBar", "true" );
    EventQueue.invokeLater( new Runnable() {
      public void run() {
        try {
          PixelEditorWindow window = new PixelEditorWindow();
          window.pixelEditorFrame.setVisible( true );
        } catch( Exception e ) {
          e.printStackTrace();
        }
      }
    } );
  }

  /**
   * Create the application.
   */
  public PixelEditorWindow() {
    updateInProgress = false;
    initialize();
  }

  /**
   * Initialize the contents of the frame.
   */
  private void initialize() {

    // make sure that the window system matches the OS
    try {
      UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
    } catch( Exception cnfe ) {
      JOptionPane.showMessageDialog( pixelEditorFrame,
          "Your application may look strange.\n\n" + "Details:\n"
              + cnfe.getMessage(),
          "Display Error", JOptionPane.WARNING_MESSAGE );
    }

    // a border to be given to the selected color boxes
    LineBorder selectedBorder = new LineBorder( SELECTED_BORDER_COLOR,
                                                SELECTED_BORDER_SIZE,
                                                false );

    JPanel firstColorDisplay, secondColorDisplay;

    /*
     * These listeners update the rest of the components when the color is
     * changed.
     */
    ChangeListener rbgChangeListener = new ChangeListener() {

      public void stateChanged( ChangeEvent e ) {
        if( updateInProgress ) {
          return;
        }

        Color updatedColor = new Color(
            (Integer) redSpinner.getValue(),
            (Integer) greenSpinner.getValue(),
            (Integer) blueSpinner.getValue(),
            (Integer) alphaSpinner.getValue() );

        updateColor( updatedColor, SRC_RGB );
      }
    };

    ChangeListener hsbChangeListener = new ChangeListener() {

      public void stateChanged( ChangeEvent e ) {
        if( updateInProgress ) {
          return;
        }
        updateInProgress = true;

        Color updatedColor = Color.getHSBColor(
            (Integer) hueSpinner.getValue() / (float) HUE_MAX,
            (Integer) saturationSpinner.getValue() / (float) SATUR_MAX,
            (Integer) brightnessSpinner.getValue() / (float) BRIGHT_MAX );

        updatedColor = new Color( updatedColor.getRed(),
                                  updatedColor.getGreen(),
                                  updatedColor.getBlue(),
                                  (Integer) alphaSpinner.getValue() );

        updateColor( updatedColor, SRC_HSB );
      }
    };
    
    //window setup
    pixelEditorFrame = new JFrame();
    pixelEditorFrame.setTitle( TITLE );
    pixelEditorFrame.setDefaultCloseOperation( JFrame.DO_NOTHING_ON_CLOSE );
    pixelEditorFrame.addWindowListener( this );
    pixelEditorFrame.getContentPane().setLayout( new BorderLayout() );
    pixelEditorFrame.setResizable( false );
    
    //menu bar
    JMenuBar menuBar = new JMenuBar();
    pixelEditorFrame.setJMenuBar( menuBar );
    
    //content
    JPanel content = new JPanel();
    content.setLayout( null );
    content.setBackground( pixelEditorFrame.getBackground() );
    pixelEditorFrame.getContentPane().add( content, BorderLayout.CENTER );

    // editor
    editorPanel = new EditorPanel();
    editorPanel.setBounds( 6, 6, 300, 300 );
    content.add( editorPanel );
    content.revalidate();

    // color picker
    colorPickerPanel = new ColorPickerPanel();
    colorPickerPanel.setBounds( 312, 6, 490, 100 );

    colorPickerPanel.setBackground( Color.BLACK );
    content.add( colorPickerPanel );

    //color displays
    firstColorDisplay = new JPanel();
    secondColorDisplay = new JPanel();
    
    // first color display
    firstColorDisplay.setBounds( 312, 112, 68, 21 );
    content.add( firstColorDisplay );
    firstColorDisplay.setPreferredSize(
        new Dimension( COLOR_DISPLAY_WIDTH, COLOR_DISPLAY_HEIGHT ) );

    // these tell the program to update the color based on which color box
    // is selected, an gives the selected box a border to indicate it is active
    firstColorDisplay.addMouseListener( new MouseAdapter() {
      @Override
      public void mouseClicked( MouseEvent e ) {
        setColorDisplay( firstColorDisplay );
        firstColorDisplay.setBorder( selectedBorder );
        secondColorDisplay.setBorder( null );
      }
    } );

    // second color display
    secondColorDisplay.setBounds( 386, 112, 68, 21 );
    content.add( secondColorDisplay );
    secondColorDisplay.setPreferredSize(
        new Dimension( COLOR_DISPLAY_WIDTH, COLOR_DISPLAY_HEIGHT ) );
    secondColorDisplay.addMouseListener( new MouseAdapter() {
      @Override
      public void mouseClicked( MouseEvent e ) {
        setColorDisplay( secondColorDisplay );
        firstColorDisplay.setBorder( null );
        secondColorDisplay.setBorder( selectedBorder );
      }
    } );

    // bttn eyedropper
    JButton mEyedropperButton = new JButton( "" );
    mEyedropperButton.setBounds( 460, 112, 31, 21 );
    content.add( mEyedropperButton );

    mEyedropperButton.setIcon( new ImageIcon(
        getClass().getResource( "/resources/eyedropper.png" ) ) );

    // chkbox grid
    JCheckBox chckbxGrid = new JCheckBox( GRID_NAME );
    chckbxGrid.setBounds( 407, 283, 95, 23 );
    content.add( chckbxGrid );
    chckbxGrid.setSelected( true );

    // chkbox fill
    JCheckBox chckbxFill = new JCheckBox( FILL_NAME );
    chckbxFill.setBounds( 312, 283, 89, 23 );
    content.add( chckbxFill );

    // red spinner
    redSpinner = new JSpinner();
    redSpinner.setBounds( 566, 112, 68, 26 );
    content.add( redSpinner );
    redSpinner.setName( RED_NAME );

    redSpinner.setModel( new SpinnerNumberModel( 0, 0, RGBA_MAX, 1 ) );

    JLabel lblRed = new JLabel( redSpinner.getName() );
    lblRed.setHorizontalAlignment( SwingConstants.RIGHT );
    lblRed.setBounds( 518, 112, 48, 26 );
    content.add( lblRed );

    // green spinner
    greenSpinner = new JSpinner();
    greenSpinner.setBounds( 566, 138, 68, 26 );
    content.add( greenSpinner );
    greenSpinner.setName( GREEN_NAME );

    greenSpinner.setModel( new SpinnerNumberModel( 0, 0, RGBA_MAX, 1 ) );

    JLabel lblGreen = new JLabel( greenSpinner.getName() );
    lblGreen.setHorizontalAlignment( SwingConstants.RIGHT );
    lblGreen.setBounds( 518, 138, 48, 26 );
    content.add( lblGreen );

    // blue spinner
    blueSpinner = new JSpinner();
    blueSpinner.setBounds( 566, 164, 68, 26 );
    content.add( blueSpinner );
    blueSpinner.setName( BLUE_NAME );

    blueSpinner.setModel( new SpinnerNumberModel( 0, 0, RGBA_MAX, 1 ) );

    JLabel lblBlue = new JLabel( blueSpinner.getName() );
    lblBlue.setHorizontalAlignment( SwingConstants.RIGHT );
    lblBlue.setBounds( 518, 164, 48, 26 );
    content.add( lblBlue );

    // hex code
    hexCodeField = new JTextField();
    hexCodeField.setBounds( 386, 137, 68, 29 );
    content.add( hexCodeField );
    hexCodeField.setFont( new Font( "Andale Mono", Font.PLAIN, 13 ) );
    hexCodeField.setName( HEX_NAME );

    hexCodeField.setHorizontalAlignment( SwingConstants.RIGHT );
    hexCodeField.setText( "#A00000" );

    JLabel lblHex = new JLabel( hexCodeField.getName() );
    lblHex.setHorizontalAlignment( SwingConstants.RIGHT );
    lblHex.setBounds( 312, 137, 68, 29 );
    content.add( lblHex );

    // hue
    hueSpinner = new JSpinner();
    hueSpinner.setBounds( 734, 112, 68, 26 );
    content.add( hueSpinner );
    hueSpinner.setName( HUE_NAME );

    hueSpinner.setModel( new SpinnerNumberModel( 0, 0, HUE_MAX, 1 ) );

    JLabel lblHue = new JLabel( hueSpinner.getName() );
    lblHue.setHorizontalAlignment( SwingConstants.RIGHT );
    lblHue.setBounds( 659, 112, 75, 26 );
    content.add( lblHue );

    // saturation
    saturationSpinner = new JSpinner();
    saturationSpinner.setBounds( 734, 138, 68, 26 );
    content.add( saturationSpinner );
    saturationSpinner.setName( SATURATION_NAME );

    saturationSpinner.setModel( new SpinnerNumberModel( 0, 0, SATUR_MAX, 1 ) );

    JLabel lblSaturation = new JLabel( saturationSpinner.getName() );
    lblSaturation.setHorizontalAlignment( SwingConstants.RIGHT );
    lblSaturation.setBounds( 659, 138, 75, 26 );
    content.add( lblSaturation );

    // brightness
    brightnessSpinner = new JSpinner();
    brightnessSpinner.setBounds( 734, 164, 68, 26 );
    content.add( brightnessSpinner );
    brightnessSpinner.setName( BRIGHTNESS_NAME );
    brightnessSpinner.setModel( new SpinnerNumberModel( 0, 0, BRIGHT_MAX, 1 ) );

    JLabel lblBrightness = new JLabel( brightnessSpinner.getName() );
    lblBrightness.setHorizontalAlignment( SwingConstants.RIGHT );
    lblBrightness.setBounds( 659, 164, 75, 26 );
    content.add( lblBrightness );

    // alpha spinner
    alphaSpinner = new JSpinner();
    alphaSpinner.setBounds( 566, 190, 68, 26 );
    content.add( alphaSpinner );
    alphaSpinner.setName( ALPHA_NAME );
    alphaSpinner.setModel( new SpinnerNumberModel( RGBA_MAX, 0, RGBA_MAX, 1 ) );

    JLabel lblAlpha = new JLabel( alphaSpinner.getName() );
    lblAlpha.setBounds( 518, 190, 48, 26 );
    content.add( lblAlpha );

    // grid size selector
    JComboBox< Integer > comboBox = new JComboBox< Integer >();
    comboBox.setBounds( 230, 312, 76, 26 );
    content.add( comboBox );

    comboBox.setModel( new DefaultComboBoxModel< Integer >(
        new Integer[]{ 1, 5, 10, 15, 20, 30, 50, 60 } ) );
    comboBox.setSelectedIndex( 1 );

    // grid size
    JLabel lblGridSize = new JLabel( SIZE_NAME );
    lblGridSize.setBounds( 167, 312, 63, 26 );
    content.add( lblGridSize );

    // makes the grid size change
    comboBox.addItemListener( new ItemListener() {
      public void itemStateChanged( ItemEvent e ) {
        editorPanel.changeGridSize( (Integer) e.getItem() );
      }
    } );

    // this tells the hex code field to update the color stuff if a valid hex
    // string is entered
    hexCodeField.addKeyListener( new KeyAdapter() {
      @Override
      public void keyPressed( KeyEvent evt ) {

        // if the user hits enter...
        if( evt.getKeyCode() == KeyEvent.VK_ENTER ) {

          // make a null reference
          Color newColor = null;

          try {
            // check to see if the color in the box can be identified by the
            // Color class. This function throws exceptions if the color is not
            // valid. if successful, newColor will not be null
            newColor = Color.decode( hexCodeField.getText() );
          } catch( Exception e ) {
            // if unsuccessful, tell the user the color is invalid
            JOptionPane.showMessageDialog( pixelEditorFrame,
                "That HEX code is invalid.\n\n" + "Details:\n" + e.getMessage(),
                "Invalid Hex String", JOptionPane.WARNING_MESSAGE );
          }

          // since the use may have been unsuccessful, we check if the color
          // object
          // is null. If it is not, then we an update colors.
          if( newColor != null ) {
            updateColor( newColor, SRC_HEX );
          }
        }
      }

    } );

    // listeners
    redSpinner.addChangeListener( rbgChangeListener );
    greenSpinner.addChangeListener( rbgChangeListener );
    blueSpinner.addChangeListener( rbgChangeListener );
    alphaSpinner.addChangeListener( rbgChangeListener );

    hueSpinner.addChangeListener( hsbChangeListener );
    saturationSpinner.addChangeListener( hsbChangeListener );
    brightnessSpinner.addChangeListener( hsbChangeListener );

    // this tells the canvas to go into color fill mode if selected
    chckbxFill.addItemListener( new ItemListener() {
      public void itemStateChanged( ItemEvent e ) {
        editorPanel.setFill( chckbxFill.isSelected() );
      }
    } );

    // this tells the canvas to show the grid if the checkbox is selected
    chckbxGrid.addItemListener( new ItemListener() {
      public void itemStateChanged( ItemEvent e ) {
        editorPanel.showGrid( chckbxGrid.isSelected() );
      }
    } );

    // sets the current color to the color clicked on, same for both actions
    mEyedropperButton.addMouseListener( new MouseAdapter() {
      @Override
      public void mouseReleased( MouseEvent e ) {
        try {
          // a really nice class that lets us look at the entire screen
          Robot getter = new Robot();
          // use getXOnScreen to get absolute position where the mouse is, so we
          // can grab the color anywhere
          Color selectedColor = getter.getPixelColor( e.getXOnScreen(),
              e.getYOnScreen() );
          // same update stuff
          updateColor( selectedColor, SRC_OTHER );
        } catch( AWTException awte ) {
          JOptionPane.showMessageDialog( pixelEditorFrame,
              "Your application may look strange.\n\n" + "Details:\n"
                  + awte.getMessage(),
              "System Error", JOptionPane.ERROR_MESSAGE );
        }
      }
    } );

    mEyedropperButton.addMouseMotionListener( new MouseMotionAdapter() {
      @Override
      public void mouseDragged( MouseEvent e ) {
        try {
          Robot getter = new Robot();
          Color selectedColor = getter.getPixelColor( e.getXOnScreen(),
              e.getYOnScreen() );
          updateColor( selectedColor, SRC_OTHER );
        } catch( AWTException awte ) {
          JOptionPane.showMessageDialog( pixelEditorFrame,
              "Your application may look strange.\n\n" + "Details:\n"
                  + awte.getMessage(),
              "System Error", JOptionPane.ERROR_MESSAGE );
        }
      }
    } );

    // these lines give the color picker the update function to update the color
    // based on whatever color has been clicked on
    MouseAdapter colorPickerMouseListener = new MouseAdapter() {
      
      public void mousePressed( MouseEvent e ) {
        colorPickerPanel.updateColor( e );
        Color toUpdate = colorPickerPanel.getColor();
        toUpdate = new Color( toUpdate.getRed(),
                              toUpdate.getGreen(),
                              toUpdate.getBlue(),
                              ( Integer ) alphaSpinner.getValue() );
        updateColor( toUpdate, SRC_PICKER );
      }
      
      public void mouseDragged( MouseEvent e ) {
        mousePressed( e );
      }
    };
    
    colorPickerPanel.addMouseListener( colorPickerMouseListener );
    colorPickerPanel.addMouseMotionListener( colorPickerMouseListener );
    
    // menu setup
    JMenu fileMenu = new JMenu("File");
    
    JMenuItem openMenuItem = new JMenuItem( OPEN_NAME );
    openMenuItem.setAccelerator( OPEN_COMBO );
    
    JMenuItem saveMenuItem = new JMenuItem( SAVE_NAME );
    saveMenuItem.setAccelerator( SAVE_COMBO );
    
    JMenu editMenu = new JMenu("Edit");
    
    JMenuItem invertMenuItem = new JMenuItem( INVERT_NAME );
    invertMenuItem.setAccelerator( INVERT_COMBO );
    
    JMenuItem clearMenuItem = new JMenuItem( CLEAR_NAME );
    clearMenuItem.setAccelerator( CLEAR_COMBO );
    
    JMenuItem undoMenuItem = new JMenuItem( UNDO_NAME );
    undoMenuItem.setAccelerator( UNDO_COMBO );
    
    JMenuItem redoMenuItem = new JMenuItem( REDO_NAME );
    redoMenuItem.setAccelerator( REDO_COMBO );
    
    menuBar.add( fileMenu );
    fileMenu.add( openMenuItem );
    fileMenu.add( saveMenuItem );
    
    menuBar.add( editMenu );
    editMenu.add( undoMenuItem );
    editMenu.add( redoMenuItem );
    editMenu.addSeparator();
    editMenu.add( invertMenuItem );
    editMenu.add( clearMenuItem );
    
    // logic to open an image
    openMenuItem.addActionListener( new ActionListener() {
      @Override
      public void actionPerformed( ActionEvent ae ) {

        // since the dialogues and file operations throw exceptions, everything
        // is
        // encased in a try-catch block, and whatever error is thrown
        // is caught and the user is greeted with a generic error message
        try {
          // use the file chooser to selected only PNG files
          JFileChooser fileChooser = new JFileChooser();
          fileChooser.setDialogTitle( "Specify a file to open" );
          fileChooser.setFileFilter(
              new FileNameExtensionFilter( "PNG Files", "png" ) );
          fileChooser.setFileSelectionMode( JFileChooser.FILES_ONLY );

          // check if the user did indeed want to open to a file
          int userSelection;
          File toOpen = null;

          userSelection = fileChooser.showOpenDialog( pixelEditorFrame );

          if( userSelection == JFileChooser.APPROVE_OPTION ) {

            toOpen = fileChooser.getSelectedFile();
            editorPanel.loadImage( toOpen, pixelEditorFrame );
          }

        } catch( Exception e ) {
          // upon any error, tell the user we are unable to save. you can catch
          // the particular exceptions thrown and give detailed messages, but in
          // this
          // case I do not
          JOptionPane.showMessageDialog( pixelEditorFrame,
              "Unable to open file.\n\n" + "Details:\n" + e.getMessage(),
              "File Error", JOptionPane.ERROR_MESSAGE );
        }
      }
    } );

    // logic to save an image
    saveMenuItem.addActionListener( new ActionListener() {
      public void actionPerformed( ActionEvent ae ) {

        // since the dialogues and file operations throw exceptions, everything
        // is
        // encased in a try-catch block, and whatever error is thrown
        // is caught and the user is greeted with a generic error message
        try {
          // use the file chooser to selected only PNG files
          JFileChooser fileChooser = new JFileChooser();
          fileChooser.setDialogTitle( "Specify a file to save" );
          fileChooser.setFileFilter(
              new FileNameExtensionFilter( "PNG Files", "png" ) );
          fileChooser.setFileSelectionMode( JFileChooser.FILES_ONLY );

          // check if the user did indeed want to save to a file
          int userSelection;
          File toSave = null;

          do {
            userSelection = fileChooser.showSaveDialog( pixelEditorFrame );

            if( userSelection == JFileChooser.APPROVE_OPTION ) {

              toSave = fileChooser.getSelectedFile();

              if( !toSave.getName().endsWith( ".png" ) ) {
                toSave = new File( toSave.getCanonicalPath() + ".png" );
              }

              if( toSave.exists() ) {
                userSelection = JOptionPane.showConfirmDialog( pixelEditorFrame,
                    "Are you sure you want to overwrite the existing file?",
                    "Confirm", JOptionPane.YES_NO_OPTION );
              }
            } else
              return;

          } while( userSelection != JOptionPane.YES_OPTION );

          editorPanel.saveImage( toSave );

        } catch( Exception e ) {
          // upon any error, tell the user we are unable to save. you can catch
          // the particular exceptions thrown and give detailed messages, but in
          // this
          // case I do not
          JOptionPane.showMessageDialog( pixelEditorFrame,
              "Unable to save file.\n\n" + "Details:\n" + e.getMessage(),
              "File Error",
              JOptionPane.ERROR_MESSAGE );
        }
      }
    } );

    // tells the canvas to invert the colors in the image
    invertMenuItem.addActionListener( new ActionListener() {
      public void actionPerformed( ActionEvent ae ) {
        editorPanel.invertColors();
      }
    } );

    // this tells the canvas to clear if the button is hit and the user confirms
    clearMenuItem.addActionListener( new ActionListener() {
      @Override
      public void actionPerformed( ActionEvent ae ) {
        // selected can be yes or no
        int selected = JOptionPane.showConfirmDialog(
            pixelEditorFrame,
            "Are you sure you want to clear?",
            "Confirm",
            JOptionPane.YES_NO_OPTION );

        // if the user selects yes, then call the clear function of the editor
        if( selected == JOptionPane.YES_OPTION )
          editorPanel.clearCanvas();
      }
    } );
    
    undoMenuItem.addActionListener( new ActionListener() {
      public void actionPerformed( ActionEvent ae ) {
        if( !editorPanel.undo() ) {
          JOptionPane.showMessageDialog( pixelEditorFrame,
              "No undos left.",
              "Whoops!",
              JOptionPane.OK_OPTION );
        }
      }
    });
    
    redoMenuItem.addActionListener( new ActionListener() {
      public void actionPerformed( ActionEvent ae ) {
        if( !editorPanel.redo() ) {
          JOptionPane.showMessageDialog( pixelEditorFrame,
              "No redos left.",
              "Whoops!",
              JOptionPane.OK_OPTION );
        }
      }
    });
    
    // ok, with the mess out of the way, we now select the first box...
    setColorDisplay( firstColorDisplay );
    
    // ...and give it that color...
    updateColor( DEFAULT_COLOR_A, SRC_OTHER );
    
    // ...then select the second box...
    setColorDisplay( secondColorDisplay );

    // and give it this color...
    updateColor( DEFAULT_COLOR_B, SRC_OTHER );
    
    // ...then reselect the first box...
    setColorDisplay( firstColorDisplay );
    
    // ...and give it the border!
    firstColorDisplay.setBorder( selectedBorder );
    
    content.setLocation( 0, 0 );
    content.setPreferredSize( new Dimension(
        saturationSpinner.getX() + saturationSpinner.getWidth() + 6,
        lblGridSize.getY() + lblGridSize.getHeight() + 6 ) );
    pixelEditorFrame.pack();
  }

  private void updateColor( Color color, int src ) {
    updateInProgress = true;

    if( src != SRC_PICKER && src != SRC_HSB ) {
      colorPickerPanel.setColor( color );
    }
    else if( src != SRC_PICKER && src == SRC_HSB ) {
      colorPickerPanel
          .setHue( (Integer) hueSpinner.getValue() / (float) HUE_MAX );

      colorPickerPanel.setSaturation(
          (Integer) saturationSpinner.getValue() / (float) SATUR_MAX );

      colorPickerPanel.setBrightness(
          (Integer) brightnessSpinner.getValue() / (float) BRIGHT_MAX );
    }

    if( src != SRC_HSB ) {
      hueSpinner
      .setValue( (int) ( colorPickerPanel.getHue() * HUE_MAX ) );

      saturationSpinner
      .setValue( (int) ( colorPickerPanel.getSaturation() * SATUR_MAX ) );

      brightnessSpinner
      .setValue( (int) ( colorPickerPanel.getBrightness() * BRIGHT_MAX ) );
    }

    if( src != SRC_RGB ) {
      redSpinner.setValue( color.getRed() );
      greenSpinner.setValue( color.getGreen() );
      blueSpinner.setValue( color.getBlue() );
    }
    
    alphaSpinner.setValue( color.getAlpha() );
    selectedColorDisplay.setBackground( color );
    hexCodeField.setText( String.format( "#%02x%02x%02x",
                          color.getRed(),
                          color.getGreen(),
                          color.getBlue() ) );
    hexCodeField.setForeground( color );
    editorPanel.setColor( color );

    updateInProgress = false;
  }

  private void setColorDisplay( JPanel selection ) {
    selectedColorDisplay = selection;
    Color currColor = selection.getBackground();
    updateColor( currColor, SRC_OTHER );
  }

  @Override
  public void windowOpened( WindowEvent e ) {
  }

  @Override
  public void windowClosing( WindowEvent e ) {
    if( !editorPanel.isSaved() ) {
      int selected = JOptionPane.showConfirmDialog(
          pixelEditorFrame,
          "Your image is unsaved. Are you sure you want to exit?",
          "Unsaved Edits",
          JOptionPane.YES_NO_OPTION );
      if( selected != JOptionPane.YES_OPTION ) {
        return;
      }
    }
    e.getWindow().dispose();
    System.exit( 0 );
  }

  @Override
  public void windowClosed( WindowEvent e ) {
  }

  @Override
  public void windowIconified( WindowEvent e ) {
  }

  @Override
  public void windowDeiconified( WindowEvent e ) {
  }

  @Override
  public void windowActivated( WindowEvent e ) {
  }

  @Override
  public void windowDeactivated( WindowEvent e ) {
  }
}