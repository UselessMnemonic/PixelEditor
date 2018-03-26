package edu.madrigal.pixeleditor;
import java.awt.EventQueue;
import java.awt.Robot;

import javax.swing.JFrame;

import javax.swing.JPanel;
import javax.swing.border.LineBorder;

import java.awt.AWTException;
import java.awt.Color;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.io.File;

import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.event.ChangeEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.JCheckBox;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import javax.swing.ImageIcon;
import java.awt.event.MouseMotionAdapter;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;

public class PixelEditorWindow {
	
	/*
	 * All this describes the main window. For everything ELSE, check the other classes
	 */

	//this is the actual window
	private JFrame mPixelEditorFrame;
	
	//this is the panel that we are editing into
	private EditorPanel mEditorPanel;
	
	//these are the little boxes that show the current color selection
	private JPanel mFirstColorDisplay, mSecondColorDisplay;
	
	//this is the panel that shows the color picker
	private ColorPickerPanel mColorPickerPanel;
	
	//these are the number fields that let us fine tune values for the HSB and RGB-A values
	private JSpinner mHueSpinner, mSaturationSpinner, mBrightnessSpinner, 
	mRedSpinner, mGreenSpinner, mBlueSpinner, mAlphaSpinner;
	
	//this is the text field that displays or allows us to enter Web colors (HEX)
	private JTextField mHexCodeField;
	
	//this keeps track of which color we're currently using (the boxes)
	private int mCurrentColorSelection;
	
	/*these matrices work like this:
	//the first index is exactly whatever color selection we're on (which box is selected)
	//the second index is the color component for that color model
	//so the set up is this:
	//[[R1, G1, B1, A1], [R2, G2, B2, A2]]
	//[[H1, S1, B1], [H2, S2, B2]]
	//keep in mind the 1 and 2 aren't indices*/
	private int[][] mRGBAValues;
	private int[][] mHSBValues;
	
	/*
	 * this is here because when I manually change the values in the number
	 * selectors, it triggers their update functions, which calls the function
	 * that manually changes their values, which again triggers their update functions,
	 * and so on. this boolean is checked to prevent it
	 */
	private boolean updateInProgress;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					PixelEditorWindow window = new PixelEditorWindow();
					window.mPixelEditorFrame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public PixelEditorWindow() {
		//this starts the color selections to RGB black with alpha 255...
		mRGBAValues = new int[][]{{0, 0, 0, 255}, {0, 0, 0, 255}};
		
		//...and HSB black with 0 degree hue
		mHSBValues = new int[2][3];
		
		//Initializes the boolean to false
		updateInProgress = false;
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		//make sure that the window system matches the OS
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e1) {
			//ignore errors here.
			e1.printStackTrace();
		}
		
		//a border to be given to the selected color boxes
		LineBorder selectedBorder = new LineBorder(new Color(255, 255, 0), 2, false);
		
		/*this is given to the number selectors that show the RGBA values, 
		 *and is needed to update all color editors with new values
		 *for the RGBA selectors, any time the user changes a value we
		 *have to be sure to use the new value to update the others.
		 *this can maybe be improved by creating separate functions that
		 *only update based on the particular value changed.
		 *In this case, all RGBA values are read and an entire update is made
		 */
		ChangeListener mRGBChangeListener = new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				//if we haven't already...
				if(updateInProgress)
					return;
				//...read the RGBA selector values and update the currently
				//selected color with those values
				int r = ((Number)mRedSpinner.getValue()).intValue();
				int g = ((Number)mGreenSpinner.getValue()).intValue();
				int b = ((Number)mBlueSpinner.getValue()).intValue();
				int a = ((Number)mAlphaSpinner.getValue()).intValue();
				updateColorRGBA(r, g, b, a);
			}
		};
		
		//this is the exact same stuff but for the HSB value selectors
		ChangeListener mHSBChangeListener = new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if(updateInProgress)
					return;
				int h = ((Number)mHueSpinner.getValue()).intValue();
				int s = ((Number)mSaturationSpinner.getValue()).intValue();
				int b = ((Number)mBrightnessSpinner.getValue()).intValue();
				updateColorHSB(h, s, b);
			}
		};
		
		//you can skip most of this, it's generated from the design tab if you're using
		//WindowBuilder on eclipse
		mPixelEditorFrame = new JFrame();
		mPixelEditorFrame.setTitle("Pixel Editor");
		mPixelEditorFrame.setBounds(100, 100, 696, 349);
		mPixelEditorFrame.setResizable(false);
		mPixelEditorFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mPixelEditorFrame.getContentPane().setLayout(null);
		
		mEditorPanel = new EditorPanel();
		mEditorPanel.setBackground(Color.WHITE);
		mEditorPanel.setBorder(new LineBorder(new Color(0, 0, 0)));
		mEditorPanel.setBounds(9, 9, 301, 301);
		mPixelEditorFrame.getContentPane().add(mEditorPanel);
		
		mColorPickerPanel = new ColorPickerPanel();
		mColorPickerPanel.setBackground(Color.BLACK);
		mColorPickerPanel.setBounds(320, 11, 360, 100);
		mPixelEditorFrame.getContentPane().add(mColorPickerPanel);
		
		JLabel lblHue = new JLabel("Hue");
		lblHue.setHorizontalAlignment(SwingConstants.RIGHT);
		lblHue.setBounds(581, 122, 34, 20);
		mPixelEditorFrame.getContentPane().add(lblHue);
		
		JLabel lblSaturation = new JLabel("Saturation");
		lblSaturation.setHorizontalAlignment(SwingConstants.RIGHT);
		lblSaturation.setBounds(565, 156, 50, 17);
		mPixelEditorFrame.getContentPane().add(lblSaturation);
		
		JLabel lblBrightness = new JLabel("Brightness");
		lblBrightness.setHorizontalAlignment(SwingConstants.RIGHT);
		lblBrightness.setBounds(550, 187, 65, 14);
		mPixelEditorFrame.getContentPane().add(lblBrightness);
		
		mHueSpinner = new JSpinner();
		mHueSpinner.setToolTipText("Hue Value");
		mHueSpinner.setModel(new SpinnerNumberModel(0, 0, 360, 1));
		mHueSpinner.setBounds(625, 122, 55, 20);
		mPixelEditorFrame.getContentPane().add(mHueSpinner);
		
		mSaturationSpinner = new JSpinner();
		mSaturationSpinner.setToolTipText("Saturation Value");
		mSaturationSpinner.setModel(new SpinnerNumberModel(0, 0, 100, 1));
		mSaturationSpinner.setBounds(625, 154, 55, 20);
		mPixelEditorFrame.getContentPane().add(mSaturationSpinner);
		
		mBrightnessSpinner = new JSpinner();
		mBrightnessSpinner.setToolTipText("Brightess Value");
		mBrightnessSpinner.setModel(new SpinnerNumberModel(0, 0, 100, 1));
		mBrightnessSpinner.setBounds(625, 184, 55, 20);
		mPixelEditorFrame.getContentPane().add(mBrightnessSpinner);
		
		JLabel lblRed = new JLabel("Red");
		lblRed.setHorizontalAlignment(SwingConstants.RIGHT);
		lblRed.setBounds(441, 122, 34, 20);
		mPixelEditorFrame.getContentPane().add(lblRed);
		
		JLabel lblGreen = new JLabel("Green");
		lblGreen.setHorizontalAlignment(SwingConstants.RIGHT);
		lblGreen.setBounds(425, 155, 50, 17);
		mPixelEditorFrame.getContentPane().add(lblGreen);
		
		JLabel lblBlue = new JLabel("Blue");
		lblBlue.setHorizontalAlignment(SwingConstants.RIGHT);
		lblBlue.setBounds(410, 187, 65, 14);
		mPixelEditorFrame.getContentPane().add(lblBlue);
		
		mRedSpinner = new JSpinner();
		mRedSpinner.setToolTipText("Red Value");
		mRedSpinner.setModel(new SpinnerNumberModel(0, 0, 255, 1));
		mRedSpinner.setBounds(485, 122, 55, 20);
		mPixelEditorFrame.getContentPane().add(mRedSpinner);
		
		mGreenSpinner = new JSpinner();
		mGreenSpinner.setToolTipText("Green Value");
		mGreenSpinner.setModel(new SpinnerNumberModel(0, 0, 255, 1));
		mGreenSpinner.setBounds(485, 153, 55, 20);
		mPixelEditorFrame.getContentPane().add(mGreenSpinner);
		
		mBlueSpinner = new JSpinner();
		mBlueSpinner.setToolTipText("Blue Value");
		mBlueSpinner.setModel(new SpinnerNumberModel(0, 0, 255, 1));
		mBlueSpinner.setBounds(485, 184, 55, 20);
		mPixelEditorFrame.getContentPane().add(mBlueSpinner);
		
		JLabel lblAlpha = new JLabel("Alpha");
		lblAlpha.setHorizontalAlignment(SwingConstants.RIGHT);
		lblAlpha.setBounds(550, 219, 65, 14);
		mPixelEditorFrame.getContentPane().add(lblAlpha);
		
		mAlphaSpinner = new JSpinner();
		mAlphaSpinner.setToolTipText("Opacity Value");
		mAlphaSpinner.setModel(new SpinnerNumberModel(255, 0, 255, 1));
		mAlphaSpinner.setBounds(625, 216, 55, 20);
		mPixelEditorFrame.getContentPane().add(mAlphaSpinner);
		
		JLabel lblHex = new JLabel("Hex");
		lblHex.setHorizontalAlignment(SwingConstants.RIGHT);
		lblHex.setBounds(410, 219, 50, 14);
		mPixelEditorFrame.getContentPane().add(lblHex);
		
		mHexCodeField = new JTextField();
		mHexCodeField.setToolTipText("Hex Code");
		mHexCodeField.setHorizontalAlignment(SwingConstants.RIGHT);
		mHexCodeField.setText("#A00000");
		mHexCodeField.setBounds(470, 216, 70, 20);
		mPixelEditorFrame.getContentPane().add(mHexCodeField);
		
		JButton mClearButton = new JButton("Clear");
		mClearButton.setToolTipText("Clear the canvas");
		mClearButton.setBounds(320, 219, 93, 23);
		mPixelEditorFrame.getContentPane().add(mClearButton);
		
		JButton mInvertButton = new JButton("Invert");
		mInvertButton.setToolTipText("Invert the canvas colors");
		mInvertButton.setBounds(320, 253, 93, 23);
		mPixelEditorFrame.getContentPane().add(mInvertButton);
		
		JButton mSaveButton = new JButton("Save...");
		mSaveButton.setToolTipText("Save your work");
		mSaveButton.setBounds(320, 287, 93, 23);
		mPixelEditorFrame.getContentPane().add(mSaveButton);
		
		mFirstColorDisplay = new JPanel();
		mFirstColorDisplay.setBackground(Color.BLACK);
		mFirstColorDisplay.setToolTipText("Primary Color");
		mFirstColorDisplay.setBounds(320, 122, 45, 20);
		mPixelEditorFrame.getContentPane().add(mFirstColorDisplay);
		
		JCheckBox chckbxGrid = new JCheckBox("Grid");
		chckbxGrid.setSelected(true);
		chckbxGrid.setBounds(320, 149, 45, 23);
		mPixelEditorFrame.getContentPane().add(chckbxGrid);
		
		JCheckBox chckbxFill = new JCheckBox("Fill");
		chckbxFill.setBounds(320, 178, 37, 23);
		mPixelEditorFrame.getContentPane().add(chckbxFill);
		
		JButton mEyedropperButton = new JButton("");
		mEyedropperButton.setIcon(new ImageIcon(getClass().getResource("/resources/eyedropper.png")));
		mEyedropperButton.setBounds(391, 156, 24, 22);
		mPixelEditorFrame.getContentPane().add(mEyedropperButton);
		
		mSecondColorDisplay = new JPanel();
		mSecondColorDisplay.setToolTipText("Secondary Color");
		mSecondColorDisplay.setBackground(Color.BLACK);
		mSecondColorDisplay.setBounds(368, 122, 45, 20);
		mPixelEditorFrame.getContentPane().add(mSecondColorDisplay);
		
		JComboBox<Integer> comboBox = new JComboBox<Integer>();
		comboBox.setModel(new DefaultComboBoxModel<Integer>(new Integer[] {5, 10, 15, 20, 30, 50, 60}));
		comboBox.setToolTipText("Grid Size");
		comboBox.setBounds(625, 244, 55, 20);
		mPixelEditorFrame.getContentPane().add(comboBox);
		
		//STOP
		//hammer time
		//These lines give listeners to the value selectors
		mHueSpinner.addChangeListener(mHSBChangeListener);
		mSaturationSpinner.addChangeListener(mHSBChangeListener);
		mBrightnessSpinner.addChangeListener(mHSBChangeListener);
		
		mRedSpinner.addChangeListener(mRGBChangeListener);
		mGreenSpinner.addChangeListener(mRGBChangeListener);
		mBlueSpinner.addChangeListener(mRGBChangeListener);
		mAlphaSpinner.addChangeListener(mRGBChangeListener);

		//these lines give the color picker the update function to update the color
		//based on whatever color has been clicked on
		mColorPickerPanel.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				mColorPickerPanel.updateColor(e);
				updateColorHSB(mColorPickerPanel.getCurrentHue(), mColorPickerPanel.getCurrentSaturation(), mColorPickerPanel.getCurrentBrightness());
			}
		});
		mColorPickerPanel.addMouseMotionListener(new MouseMotionListener() {
			
			@Override
			public void mouseMoved(MouseEvent evt) {
				
			}
			
			@Override
			public void mouseDragged(MouseEvent e) {
				mColorPickerPanel.updateColor(e);
				updateColorHSB(mColorPickerPanel.getCurrentHue(), mColorPickerPanel.getCurrentSaturation(), mColorPickerPanel.getCurrentBrightness());
			}
		});
		
		//this tells the canvas to show the grid if the checkbox is selected
		chckbxGrid.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e){
				mEditorPanel.showGrid(chckbxGrid.isSelected());
			}
		});
		
		//this tells the canvas to clear if the button is hit and the user confirms
		mClearButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent evt) {
					//selected can be yes or no
					int selected = JOptionPane.showConfirmDialog(mPixelEditorFrame,
			    			"Are you sure you want to clear?", "Confirm", JOptionPane.YES_NO_OPTION);
			    	
					//if the user selects yes, then call the clear function of the editor
					if(selected == JOptionPane.YES_OPTION)
			    		 mEditorPanel.clearCanvas();
			}
		});
		
		//this tells the hex code field to update the color stuff if a valid hex string is entered
		mHexCodeField.addKeyListener(new KeyAdapter() {
	        @Override
	        public void keyPressed(KeyEvent evt) {
	        	
	        	//if the user hits enter...
	            if(evt.getKeyCode() == KeyEvent.VK_ENTER){
					
	            	//make a null reference
					Color newColor = null;
					
					try
					{
						//check to see if the color in the box can be identified by the
						//Color class. This function throws exceptions if the color is not
						//valid. if successful, newColor will not be null
						newColor = Color.decode(mHexCodeField.getText());
					}
					catch(Exception e)
					{
						//if unsuccessful, tell the user the color is invalid
						JOptionPane.showMessageDialog(mPixelEditorFrame,
							    "Invalid HEX string.",
							    "Whoops!",
							    JOptionPane.WARNING_MESSAGE);
					}
					
					//since the use may have been unsuccessful, we check if the color object
					//is null. If it is not, then we an update colors.
					if(newColor != null)
						updateColorRGBA(newColor.getRed(), newColor.getGreen(), newColor.getBlue(), newColor.getAlpha());
	            }
	        }

	    });
		
		//these tell the program to update the color based on which color box
		//is selected, an gives the selected box a border to indicate it is active
		mFirstColorDisplay.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				setColorSelection(0);
				mFirstColorDisplay.setBorder(selectedBorder);
				mSecondColorDisplay.setBorder(null);
			}
		});
		mSecondColorDisplay.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				setColorSelection(1);
				mFirstColorDisplay.setBorder(null);
				mSecondColorDisplay.setBorder(selectedBorder);
			}
		});
		
		//this tells the program to save the image on the canvas if the user wants
		mSaveButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent evt) {
				
				//since the dialogues and file operations throw exceptions, everything is
				//encased in a try-catch block, and whatever error is thrown
				//is caught and the user is greeted with a generic error message
				try
				{
					//use the file chooser to selected only PNG files
					JFileChooser fileChooser = new JFileChooser();
					fileChooser.setDialogTitle("Specify a file to save"); 
					fileChooser.setFileFilter(new FileNameExtensionFilter("PNG Files", "png"));
					fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
					
					//check if the user did indeed want to save to a file
					int userSelection = fileChooser.showSaveDialog(mPixelEditorFrame);
					 
					//if they do want to save...
					if (userSelection == JFileChooser.APPROVE_OPTION) {
						
						//...make a reference to the file...
					    File fileToSave = fileChooser.getSelectedFile();
					    
					    //...and check if the file already exists
					    if(fileToSave.exists())
					    {
					    	//if the file does exist...
					    	int selected = JOptionPane.showConfirmDialog(mPixelEditorFrame,
					    			"Are you sure you want to overwrite the existing file?", "Confirm", JOptionPane.YES_NO_OPTION);
					    	//confirm to the user that they want to overwrite.
					    	if(selected == JOptionPane.YES_OPTION)
					    		 mEditorPanel.saveImage(fileToSave);
					    	
					    	//otherwise we just don't save.
					    }
					    //if the file does not already exist, let's save!
					    else
					    	 mEditorPanel.saveImage(fileToSave);
					}
				}
				catch(Exception e)
				{
					//upon any error, tell the user we are unable to save. you can catch
					//the particular exceptions thrown and give detailed messages, but in this
					//case I do not
					JOptionPane.showMessageDialog(mPixelEditorFrame,
						    "Unable to Save\n" + e.getMessage(),
						    "Error",
						    JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		
		//this tells the canvas to go into color fill mode if selected
		chckbxFill.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e){
				mEditorPanel.setFill(chckbxFill.isSelected());
			}
		});
		
		//tells the canvas to invert the colors in the image
		mInvertButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				mEditorPanel.invertColors();
			}
		});
		
		//sets the current color to the color clicked on, same for both actions
		mEyedropperButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				try {
					//a really nice class that lets us look at the entire screen
					Robot getter = new Robot();
					//use getXOnScreen to get absolute position where the mouse is, so we can grab the color anywhere
					Color selectedColor = getter.getPixelColor(e.getXOnScreen(), e.getYOnScreen());
					//same update stuff
					updateColorRGBA(selectedColor.getRed(), selectedColor.getGreen(), selectedColor.getBlue(), selectedColor.getAlpha());
				} catch (AWTException e1) {
					//ignore any error here
					e1.printStackTrace();
				}
			}
		});
		mEyedropperButton.addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseDragged(MouseEvent e) {
				try {
					Robot getter = new Robot();
					Color selectedColor = getter.getPixelColor(e.getXOnScreen(), e.getYOnScreen());
					updateColorRGBA(selectedColor.getRed(), selectedColor.getGreen(), selectedColor.getBlue(), selectedColor.getAlpha());
				} catch (AWTException e1) {
					e1.printStackTrace();
				}
			}
		});
		
		//makes the grid size change
		comboBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				mEditorPanel.changeGridSize( ((Integer)e.getItem()).intValue() );
			}
		});
		
		//ok, with the mess out of the way, we now select the first box...
		setColorSelection(0);
		//...and give it that color...
		updateColorHSB(40, 50, 80);
		//...then select the second box...
		setColorSelection(1);
		//and give it this color...
		updateColorHSB(200, 50, 100);
		//...then reselect the first box...
		setColorSelection(0);
		//...and give it the border
		mFirstColorDisplay.setBorder(selectedBorder);
		
		JLabel lblGridSize = new JLabel("Grid Size");
		lblGridSize.setBounds(575, 247, 46, 14);
		mPixelEditorFrame.getContentPane().add(lblGridSize);
	}
	
	//this function is called to update the color selection when a color box is selected
	private void setColorSelection(int i) {
		//make the selection whatever index
		mCurrentColorSelection = i;
		//call the update color function
		updateColor();
	}

	//when any of the RGBA value selectors are updated, 
	//this function is called...
	public void updateColorRGBA(int r, int g, int b, int a)
	{
		//...make this float array to save the return value of Color.RGBtoHSB
		float[] hsbBuff = new float[3];
		
		//save the RGBA values to the given indices for the given
		//selection
		mRGBAValues[mCurrentColorSelection][0] = r;
		mRGBAValues[mCurrentColorSelection][1] = g;
		mRGBAValues[mCurrentColorSelection][2] = b;
		mRGBAValues[mCurrentColorSelection][3] = a;
		
		//make the HSV values, which is saved to hsbBuff
		Color.RGBtoHSB(r, g, b, hsbBuff);
		
		//the way HSB values work is like this:
		/*
		 * Hue is an angle measure between 0 and 360
		 * Saturation is measured between 0.0 and 1.0
		 * Brightness is measured between 0.0 and 1.0
		 * 
		 * the HSB matrix holds integers because if I have to use
		 * float values, when I go calculate new colors, there will
		 * be rounding issues.
		 * 
		 * Java implementation uses float values of 0.0
		 * to 1.0 for all three of these values. They more or
		 * less represent percentages. So I have to multiply them
		 * by 360, 100, and 100, to get integers I can save
		 */
		mHSBValues[mCurrentColorSelection][0] = (int)(hsbBuff[0]*360);
		mHSBValues[mCurrentColorSelection][1] = (int)(hsbBuff[1]*100);
		mHSBValues[mCurrentColorSelection][2] = (int)(hsbBuff[2]*100);
		
		//update all color displays
		updateColor();
	}
	
	
	public void updateColorHSB(int h, int s, int b)
	{
		//update the HSB matrix with the given values
		mHSBValues[mCurrentColorSelection][0] = h;
		mHSBValues[mCurrentColorSelection][1] = s;
		mHSBValues[mCurrentColorSelection][2] = b;
		
		//I have to divide by 360, 100, and 100 again to get back to the 0.0 to 1.0 scale
		//either case h, s, and b to floats or put ".0f" on the end of the numbers to auto
		//cast to float
		Color nextColor = Color.getHSBColor(h/360.0f, s/100.0f, b/100.0f);
		
		//Save that color's RGB representation to the RGBA matrix. HSB does not hold
		//alpha values, so I just grab the alpha value from the alpha selector
		mRGBAValues[mCurrentColorSelection][0] = nextColor.getRed();
		mRGBAValues[mCurrentColorSelection][1] = nextColor.getGreen();
		mRGBAValues[mCurrentColorSelection][2] = nextColor.getBlue();
		mRGBAValues[mCurrentColorSelection][3] = ((Number)mAlphaSpinner.getValue()).intValue();
		
		//update color displays
		updateColor();
	}
	
	//the color display update function!!!
	public void updateColor()
	{
		//set this to true to prevent update loops
		updateInProgress = true;
		
		//set the selector values to whatever the matrix values are
		//for both the RGBA...
		mRedSpinner.setValue(mRGBAValues[mCurrentColorSelection][0]);
		mGreenSpinner.setValue(mRGBAValues[mCurrentColorSelection][1]);
		mBlueSpinner.setValue(mRGBAValues[mCurrentColorSelection][2]);
		mAlphaSpinner.setValue(mRGBAValues[mCurrentColorSelection][3]);
		
		//...and HSB selectors
		mHueSpinner.setValue(mHSBValues[mCurrentColorSelection][0]);
		mSaturationSpinner.setValue(mHSBValues[mCurrentColorSelection][1]);
		mBrightnessSpinner.setValue(mHSBValues[mCurrentColorSelection][2]);
		
		//Set the color picker to the color we are currently using (it takes HSB vlues)
		mColorPickerPanel.setColor(mHSBValues[mCurrentColorSelection][0], mHSBValues[mCurrentColorSelection][1], mHSBValues[mCurrentColorSelection][2]);
		
		//set the color display boxes to the colors in the RGBA matrix
		mFirstColorDisplay.setBackground(new Color(mRGBAValues[0][0], mRGBAValues[0][1], mRGBAValues[0][2], mRGBAValues[0][3]));
		mSecondColorDisplay.setBackground(new Color(mRGBAValues[1][0], mRGBAValues[1][1], mRGBAValues[1][2], mRGBAValues[1][3]));
		
		//set the HEX code to the currently selected value, using the funny string which tells
		//the String class to save the RGB (and not A) values to hex values
		mHexCodeField.setText(String.format("#%02x%02x%02x", mRGBAValues[mCurrentColorSelection][0], mRGBAValues[mCurrentColorSelection][1], mRGBAValues[mCurrentColorSelection][2]));
		
		//just added this today, sets the hex string text color to the current color
		mHexCodeField.setForeground(new Color(mRGBAValues[mCurrentColorSelection][0], mRGBAValues[mCurrentColorSelection][1], mRGBAValues[mCurrentColorSelection][2], mRGBAValues[mCurrentColorSelection][3]));
		
		//sets the canvas color to the current color
		mEditorPanel.setColor(new Color(mRGBAValues[mCurrentColorSelection][0], mRGBAValues[mCurrentColorSelection][1], mRGBAValues[mCurrentColorSelection][2], mRGBAValues[mCurrentColorSelection][3]));
		
		//very important: REPAINT the entire window!!
		mPixelEditorFrame.repaint();
		
		//finally, set this to false so we can update again
		updateInProgress = false;
	}
}