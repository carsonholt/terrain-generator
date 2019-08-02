import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import javax.swing.*;

/**
 * Main class for terrain generator
 * @author Carson Holt
 *
 */
public class Main {
	/**
	 * Main function
	 * @param args
	 */
	public static void main(String[] args) {
		FieldPanel fp = new FieldPanel();
		fp.setVisible(true);
	}	
}

/**
 * Provide a frame and panel to adjust the input fields
 * @author Carson Holt
 *
 */
class FieldPanel extends JPanel implements ActionListener {
	private int size; // the size of the map (in 100m cells)
	private int heightLimit; // the height range/limit of the map
	private int xScale; // how much detail in the x-axis
	private int yScale; // how much detail in the y-axis
	private boolean monochrome; // whether the image is black & white or not
	private BufferedImage image; // the image that the map will be drawn on
	
	/**
	 * create GUI function
	 * creates components of the User Interface and sets their properties
	 */
	public FieldPanel() {
		JFrame frame = new JFrame("Terrain Generator");
		GridLayout fields = new GridLayout(6, 2);
		
		String[] mapSizes = {"Tiny: 200x200 m", "Small: 400x400 m", "Medium: 800x800 m",
				"Large: 1600x1600 m", "Huge: 3200x3200 m"};
		int[] mapSizeInts = {2, 4, 8, 16, 32};
		
		// set gap properties
		fields.setHgap(10);
		fields.setVgap(30);
		frame.setResizable(true);
		
		// declare constants
		final int MAX_HEIGHT = 256;
		final int MIN_HEIGHT = 0;
		
		// create field for map size
		JLabel sizeLabel = new JLabel("Size of map: ");
		sizeLabel.setAlignmentY(RIGHT_ALIGNMENT);
		frame.add(sizeLabel);
		JComboBox sizeBox = new JComboBox(mapSizes);
		sizeBox.setSize(40, 20);
		sizeBox.setToolTipText("The size of the map");
		sizeBox.setSelectedIndex(2);
		sizeBox.addActionListener(this);
		frame.add(sizeBox);
		
		// create field for the height range
		JLabel heightLabel = new JLabel("Height: ");
		frame.add(heightLabel);
		JFormattedTextField heightText = new JFormattedTextField();
		heightText.setValue(128);
		heightText.setToolTipText("The maximum height range of the map (in meters)");
		frame.add(heightText);
		
		// create field for x-scale
		JLabel xScaleLabel = new JLabel("X-scale: ");
		frame.add(xScaleLabel);
		JFormattedTextField xScaleText = new JFormattedTextField();
		xScaleText.setValue(10);
		xScaleText.setToolTipText("The amount of stretch in the x-axis. Larger values indicate more detail.");
		frame.add(xScaleText);
		
		// create field for y-scale
		JLabel yScaleLabel = new JLabel("y-scale: ");
		frame.add(yScaleLabel);
		JFormattedTextField yScaleText = new JFormattedTextField();
		yScaleText.setValue(10);
		yScaleText.setToolTipText("The amount of stretch in the y-axis. Larger values indicate more detail.");
		frame.add(yScaleText);
		
		// create checkbox 
		JCheckBox monochromeChk = new JCheckBox("Monochrome");
		monochromeChk.setSelected(false);
		monochromeChk.setToolTipText("Whether the image is monochrome or not -"
				+ "\n true if in black and white, false if in color");
		frame.add(monochromeChk);
		
		// create filler label
		JLabel filler = new JLabel("");
		frame.add(filler);
		
		// create button
		JButton createButton = new JButton("Create terrain");
		
		/**
		 * Add listener to the "create terrain" button
		 */
		createButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int sizeIndex = sizeBox.getSelectedIndex();
				size = mapSizeInts[sizeIndex];
				heightLimit = Integer.parseInt(heightText.getText());
				if (heightLimit > MAX_HEIGHT || heightLimit < MIN_HEIGHT) {
					JOptionPane.showMessageDialog(frame, "Height range must be between 0 and 256, inclusive",
							"Error", JOptionPane.ERROR_MESSAGE);
					throw new RuntimeException("Height out of range.");
				}
				
				// get values of x-scale and y-scale respectively
				xScale = Integer.parseInt(xScaleText.getText());
				yScale = Integer.parseInt(yScaleText.getText());
				
				// error handling for the scale values
				if (xScale < 2 || xScale > 32) {
					JOptionPane.showMessageDialog(frame, "x-scale must be between 2 and 32, inclusive",
					"Error", JOptionPane.ERROR_MESSAGE);
					throw new RuntimeException("x-Scale out of range");
				}
				if (yScale < 2 || yScale > 32) {
					JOptionPane.showMessageDialog(frame, "y-scale must be between 2 and 32, inclusive",
					"Error", JOptionPane.ERROR_MESSAGE);
					throw new RuntimeException("y-Scale out of range");
				}
				
				// monochrome
				if (monochromeChk.isSelected()) {
					monochrome = true;
				} else {
					monochrome = false;
				}
				// create the map
				System.out.println("Creating map with size " + size);
				Map map = new Map(size, heightLimit, xScale, yScale);
				float[][] heightMap = new float[xScale*size][yScale*size];
				float max = 0.0f;
				float min = 0.0f;
				
				// randomize the height map at each coordinate
				for (float a = 0; a < size; a += (1.0f/xScale)) {
					for (float b = 0; b < size; b += (1.0f/yScale)) {
						heightMap[(int) (a*xScale)][(int) (b*yScale)] = map.perlin(a, b);
						if (heightMap[(int) (a*xScale)][(int) (b*yScale)] < min) {
							min = heightMap[(int) (a*xScale)][(int) (b*yScale)];
						}
						if (heightMap[(int) (a*xScale)][(int) (b*yScale)] > max) {
							max = heightMap[(int) (a*xScale)][(int) (b*yScale)];
						}
					}
				}
				System.out.println("Min: " + min + " Max: " + max + " Mean: " + ((min+max)/2.0f));

				MapPanel mp = new MapPanel();
				mp.setMapSize(size);
				mp.setHeightLimit(heightLimit);
				mp.setHeightMap(heightMap);
				mp.setXScale(xScale);
				mp.setYScale(yScale);
				image = mp.draw(min, max, monochrome); // draw the map
				
				if (size < 16) {
					// if map is medium-sized or smaller, the gradient bar will be the height of the image.
					mp.createGradientImage(20, image.getHeight(), min, max, monochrome);
				} else {
					// if the map is large or huge, the gradient bar will be the size of the frame
					mp.createGradientImage(20, frame.getHeight(), min, max, monochrome);
				}
			}
		});
		
		// exit Button
		JButton exitButton = new JButton("Exit");
		exitButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		// add buttons to frame
        frame.add(createButton);
        frame.add(exitButton);
        
        // set frame properties
        frame.setLayout(fields);
        //frame.add(panel);
        frame.setSize(300, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
	}
	
	/**
	 * 
	 * @return the map size (in 100m cells)
	 */
	public int getMapSize() {
		return size;
	}
	
	/**
	 * set the map size
	 * @param size
	 */
	public void setMapSize(int size) {
		this.size = size;
	}
	
	/**
	 * get the height range of the map
	 * @return the height limit/range
	 */
	public int getHeightLimit() {
		return heightLimit;
	}
	
	/**
	 * set the height limit/range
	 * @param heightLimit
	 */
	public void setHeightLimit(int heightLimit) {
		this.heightLimit = heightLimit;
	}
	
	/**
	 * get the x-scale
	 * @return the level of detail on the x-axis (2-32)
	 */
	public int getXScale() {
		return xScale;
	}
	
	/**
	 * set the x-scale/detail level
	 * @param xScale
	 */
	public void setXScale(int xScale) {
		this.xScale = xScale;
	}
	
	/**
	 * get the y-scale
	 * @return the level of detail on the y-axis (2-32)
	 */
	public int getYScale() {
		return yScale;
	}
	
	/**
	 * set the y-scale/detail level
	 * @param yScale
	 */
	public void setYScale(int yScale) {
		this.yScale = yScale;
	}
	
	/**
	 * 
	 * @return whether the image has color or not
	 */
	public boolean isMonochrome() {
		return monochrome;
	}
	
	/**
	 * set the monochrome status of the image
	 * @param monochrome - true if B&W, false if in color
	 */
	public void setMonochrome(boolean monochrome) {
		this.monochrome = monochrome;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		// TODO Auto-generated method stub
		
	}
}
