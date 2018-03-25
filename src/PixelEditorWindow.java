import java.awt.EventQueue;
import java.awt.Graphics;

import javax.swing.JFrame;

import javax.swing.JPanel;
import javax.swing.border.LineBorder;
import java.awt.Color;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
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

public class PixelEditorWindow {

	private JFrame mPixelEditorFrame;
	private EditorPanel mEditorPanel;
	private JPanel mFirstColorDisplay, mSecondColorDisplay;
	private ColorPickerPanel mColorPickerPanel;
	private JSpinner mHueSpinner, mSaturationSpinner, mBrightnessSpinner, mRedSpinner, mGreenSpinner, mBlueSpinner, mAlphaSpinner;
	private JTextField mHexCodeField;
	
	private int mCurrentColorSelection;
	private int[][] mRGBAValues;
	private int[][] mHSBValues;
	
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
		mRGBAValues = new int[][]{{0, 0, 0, 255}, {0, 0, 0, 255}};
		mHSBValues = new int[2][3];
		updateInProgress = false;
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		
		ChangeListener mRGBChangeListener = new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if(updateInProgress)
					return;
				int r = ((Number)mRedSpinner.getValue()).intValue();
				int g = ((Number)mGreenSpinner.getValue()).intValue();
				int b = ((Number)mBlueSpinner.getValue()).intValue();
				int a = ((Number)mAlphaSpinner.getValue()).intValue();
				updateColorRGBA(r, g, b, a);
			}
		};
		
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
		mClearButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent evt) {
				try
				{
					int selected = JOptionPane.showConfirmDialog(mPixelEditorFrame,
			    			"Are you sure you want to clear?", "Confirm", JOptionPane.YES_NO_OPTION);
			    	if(selected == 0)
			    		 mEditorPanel.clearCanvas();
				}
				catch(Exception e)
				{
					JOptionPane.showMessageDialog(mPixelEditorFrame,
						    "Internal Error",
						    "Error",
						    JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		mClearButton.setToolTipText("Clear the canvas");
		mClearButton.setBounds(320, 176, 93, 23);
		mPixelEditorFrame.getContentPane().add(mClearButton);
		
		JButton mInvertButton = new JButton("Invert");
		mInvertButton.setToolTipText("Invert the canvas colors");
		mInvertButton.setBounds(320, 210, 93, 23);
		mPixelEditorFrame.getContentPane().add(mInvertButton);
		
		JButton mSaveButton = new JButton("Save...");
		mSaveButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent evt) {
				try
				{
					JFileChooser fileChooser = new JFileChooser();
					fileChooser.setDialogTitle("Specify a file to save"); 
					fileChooser.setFileFilter(new FileNameExtensionFilter("PNG Files", "png"));
					fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
					 
					int userSelection = fileChooser.showSaveDialog(mPixelEditorFrame);
					 
					if (userSelection == JFileChooser.APPROVE_OPTION) {
					    File fileToSave = fileChooser.getSelectedFile();
					    if(fileToSave.exists())
					    {
					    	int selected = JOptionPane.showConfirmDialog(mPixelEditorFrame,
					    			"Are you sure you want to overwrite the existing file?", "Confirm", JOptionPane.YES_NO_OPTION);
					    	if(selected == 0)
					    		 mEditorPanel.saveImage(fileToSave);
					    }
					    else
					    	 mEditorPanel.saveImage(fileToSave);
					}
				}
				catch(Exception e)
				{
					JOptionPane.showMessageDialog(mPixelEditorFrame,
						    "Unable to Save",
						    "Error",
						    JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		mSaveButton.setToolTipText("Save your work");
		mSaveButton.setBounds(320, 287, 93, 23);
		mPixelEditorFrame.getContentPane().add(mSaveButton);
		
		mFirstColorDisplay = new JPanel();
		mFirstColorDisplay.setBackground(Color.BLACK);
		mFirstColorDisplay.setToolTipText("Primary Color");
		mFirstColorDisplay.setBounds(320, 122, 45, 20);
		mPixelEditorFrame.getContentPane().add(mFirstColorDisplay);
		
		JCheckBox chckbxGrid = new JCheckBox("Grid");
		chckbxGrid.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e){
				mEditorPanel.showGrid(chckbxGrid.isSelected());
			}
		});
		chckbxGrid.setSelected(true);
		chckbxGrid.setBounds(316, 149, 45, 23);
		mPixelEditorFrame.getContentPane().add(chckbxGrid);
		
		mSecondColorDisplay = new JPanel();
		mSecondColorDisplay.setToolTipText("Secondary Color");
		mSecondColorDisplay.setBackground(Color.BLACK);
		mSecondColorDisplay.setBounds(368, 122, 45, 20);
		mPixelEditorFrame.getContentPane().add(mSecondColorDisplay);
		
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
		mHueSpinner.addChangeListener(mHSBChangeListener);
		mSaturationSpinner.addChangeListener(mHSBChangeListener);
		mBrightnessSpinner.addChangeListener(mHSBChangeListener);
		
		mRedSpinner.addChangeListener(mRGBChangeListener);
		mGreenSpinner.addChangeListener(mRGBChangeListener);
		mBlueSpinner.addChangeListener(mRGBChangeListener);
		mAlphaSpinner.addChangeListener(mRGBChangeListener);

		mHexCodeField.addKeyListener(new KeyAdapter() {
	        @Override
	        public void keyPressed(KeyEvent evt) {
	            if(evt.getKeyCode() == KeyEvent.VK_ENTER){
	            	if(updateInProgress)
						return;
					
					Color newColor = null;
					try
					{
						newColor = Color.decode(mHexCodeField.getText());
					}
					catch(Exception e)
					{
						JOptionPane.showMessageDialog(mPixelEditorFrame,
							    "Invalid HEX string.",
							    "Whoops!",
							    JOptionPane.WARNING_MESSAGE);
					}
					if(newColor != null)
						updateColorRGBA(newColor.getRed(), newColor.getGreen(), newColor.getBlue(), newColor.getAlpha());
	            }
	        }

	    });
		mFirstColorDisplay.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				setColorSelection(0);
				mFirstColorDisplay.setBorder(new LineBorder(Color.YELLOW));
				mSecondColorDisplay.setBorder(null);
			}
		});
		mSecondColorDisplay.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				setColorSelection(1);
				mFirstColorDisplay.setBorder(null);
				mSecondColorDisplay.setBorder(new LineBorder(Color.YELLOW));
			}
		});
		setColorSelection(0);
		updateColorHSB(40, 50, 80);
		setColorSelection(1);
		updateColorHSB(200, 50, 100);
		setColorSelection(0);
		mFirstColorDisplay.setBorder(new LineBorder(Color.YELLOW));
		
		JCheckBox chckbxFill = new JCheckBox("Fill");
		chckbxFill.setBounds(363, 149, 50, 23);
		chckbxFill.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e){
				mEditorPanel.setFill(chckbxFill.isSelected());
			}
		});
		mPixelEditorFrame.getContentPane().add(chckbxFill);
	}
	
	private void setColorSelection(int i) {
		mCurrentColorSelection = i;
		updateColor();
	}

	public void drawOnCanvas(MouseEvent e, Graphics g) {
		g.setColor(new Color(mRGBAValues[mCurrentColorSelection][0], mRGBAValues[mCurrentColorSelection][1], mRGBAValues[mCurrentColorSelection][2], mRGBAValues[mCurrentColorSelection][3]));
		g.drawLine(e.getX(), e.getY(), e.getX(), e.getY());
	}

	public void updateColorRGBA(int r, int g, int b, int a)
	{
		float[] hsbBuff = new float[3];
		mRGBAValues[mCurrentColorSelection][0] = r;
		mRGBAValues[mCurrentColorSelection][1] = g;
		mRGBAValues[mCurrentColorSelection][2] = b;
		mRGBAValues[mCurrentColorSelection][3] = a;
		Color.RGBtoHSB(r, g, b, hsbBuff);
		mHSBValues[mCurrentColorSelection][0] = (int)(hsbBuff[0]*360);
		mHSBValues[mCurrentColorSelection][1] = (int)(hsbBuff[1]*100);
		mHSBValues[mCurrentColorSelection][2] = (int)(hsbBuff[2]*100);
		updateColor();
	}
	
	public void updateColorHSB(int h, int s, int b)
	{
		mHSBValues[mCurrentColorSelection][0] = h;
		mHSBValues[mCurrentColorSelection][1] = s;
		mHSBValues[mCurrentColorSelection][2] = b;
		Color nextColor = Color.getHSBColor(h/360.0f, s/100.0f, b/100.0f);
		mRGBAValues[mCurrentColorSelection][0] = nextColor.getRed();
		mRGBAValues[mCurrentColorSelection][1] = nextColor.getGreen();
		mRGBAValues[mCurrentColorSelection][2] = nextColor.getBlue();
		mRGBAValues[mCurrentColorSelection][3] = ((Number)mAlphaSpinner.getValue()).intValue();
		updateColor();
	}
	
	public void updateColor()
	{
		updateInProgress = true;
		
		mRedSpinner.setValue(mRGBAValues[mCurrentColorSelection][0]);
		mGreenSpinner.setValue(mRGBAValues[mCurrentColorSelection][1]);
		mBlueSpinner.setValue(mRGBAValues[mCurrentColorSelection][2]);
		mAlphaSpinner.setValue(mRGBAValues[mCurrentColorSelection][3]);
		
		mHueSpinner.setValue(mHSBValues[mCurrentColorSelection][0]);
		mSaturationSpinner.setValue(mHSBValues[mCurrentColorSelection][1]);
		mBrightnessSpinner.setValue(mHSBValues[mCurrentColorSelection][2]);
		
		mColorPickerPanel.setColor(mHSBValues[mCurrentColorSelection][0], mHSBValues[mCurrentColorSelection][1], mHSBValues[mCurrentColorSelection][2]);
		
		mFirstColorDisplay.setBackground(new Color(mRGBAValues[0][0], mRGBAValues[0][1], mRGBAValues[0][2], mRGBAValues[mCurrentColorSelection][3]));
		mSecondColorDisplay.setBackground(new Color(mRGBAValues[1][0], mRGBAValues[1][1], mRGBAValues[1][2], mRGBAValues[mCurrentColorSelection][3]));
		
		mHexCodeField.setText(String.format("#%02x%02x%02x", mRGBAValues[mCurrentColorSelection][0], mRGBAValues[mCurrentColorSelection][1], mRGBAValues[mCurrentColorSelection][2]));
		mEditorPanel.setColor(new Color(mRGBAValues[mCurrentColorSelection][0], mRGBAValues[mCurrentColorSelection][1], mRGBAValues[mCurrentColorSelection][2], mRGBAValues[mCurrentColorSelection][3]));
		
		mPixelEditorFrame.repaint();
		
		updateInProgress = false;
	}
}