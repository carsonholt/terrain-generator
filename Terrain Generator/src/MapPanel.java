import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.ScrollPane;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 * A panel for the heightmap to be displayed
 * @author Carson Holt
 *
 */
public class MapPanel extends JPanel {
	private static final long serialVersionUID = 1L; // the serial version id
	private static long serialNumber = 0; // the map number (in current session)
	private BufferedImage image; // the image to be drawn on
	private JFrame frame; // the frame for the map panel
	private int size; // the size of the map
	private int heightLimit; // the height limit/range
	private float[][] heightMap; // the height map
	private int xScale; // the level of detail on x-axis
	private int yScale; // the level of detail on y-axis
	private boolean monochrome; // whether the image is in black & white or not
	private static final int MAX_PIXELS = 32; // the max number of pixels in a cell
	
	/**
	 * A panel for the heightmap to be displayed
	 */
	public MapPanel() {
		// create the frame and set its properties
		frame = new JFrame("Auto-generated terrain");
		frame.setLayout(new BorderLayout());
		JButton saveButton = new JButton("Save Image");
		frame.add(saveButton, BorderLayout.SOUTH);
		frame.setSize(600, 600);
		frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        frame.setVisible(true);
        
        // add listeners to buttons
        saveButton.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		try {
        			File outFile = new File("img/map" + serialNumber + ".png");
        			if (outFile.exists()) {
        				int option = JOptionPane.showConfirmDialog(frame, "Would you like to overwrite the current file?", 
        						"File already exists", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
        				if (option == 0) {
        					// yes, overwrite file
        					ImageIO.write(image, "png", outFile);
                			serialNumber++;
        				} else if (option == 1) {
        					// no, rename file
        					String name = JOptionPane.showInputDialog("Enter an alternate file name: ");
        					ImageIO.write(image, "png", new File("img/" + name + ".png"));
        					serialNumber++;
        				}
        			} else {
        				ImageIO.write(image, "png", outFile);
            			serialNumber++;
        			}
        			
        		} catch (IOException ex) {
        			JOptionPane.showMessageDialog(frame, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        		}
        	}
        });
	}
	
	/**
	 * Draw the height map
	 * @param min - the minimum height value
	 * @param max - the maximum height value
	 * @param mono - whether the image is monochrome or not
	 * @return the image with the height values drawn on
	 */
	public BufferedImage draw(float min, float max, boolean mono) {
		image =	new BufferedImage(xScale*size, yScale*size, BufferedImage.TYPE_INT_ARGB);
		float range = max - min; // range of height values
		
		System.out.println("Range: " + range);
		for (int x = 0; x < xScale * size; x++) {
			for (int y = 0; y < yScale * size; y++) {
				// Set coordinate to proper RGB value
				if (mono) {
					image.setRGB(x, y, new Color((int)(heightMap[x][y] + 128.0f), 
							(int)(heightMap[x][y] + 128.0f), (int)(heightMap[x][y] + 128.0f)).getRGB());
				} else {
					image.setRGB(x, y, 
							new Color(0, (int)(heightMap[x][y] + 128.0f), 0).getRGB());
				}
			}
		}

		return resize();
	}
	
	/**
	 * resize the image according to the scale
	 * @return the resized image
	 */
	public BufferedImage resize() {		
		Image tmp = image.getScaledInstance(size*MAX_PIXELS, size*MAX_PIXELS, Image.SCALE_SMOOTH);
		BufferedImage bi = new BufferedImage(MAX_PIXELS*size, MAX_PIXELS*size, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = bi.createGraphics();
		g2d.drawImage(tmp, 0, 0, size*MAX_PIXELS, size*MAX_PIXELS, null);
		g2d.dispose();
		JLabel label = new JLabel(new ImageIcon(bi));	
		ScrollPane scrollPane = new ScrollPane();
		scrollPane.add(label);
		frame.add(scrollPane, BorderLayout.CENTER);
		return bi;
	};
	
	/**
	 * draw grid lines at 100m intervals
	 * @param image - the bufferedImage that will be drawn on
	 */
	public void drawGridLines(BufferedImage image) {
		Graphics2D g2d = image.createGraphics();
		g2d.setColor(Color.BLACK);
		// draw vertical lines
		for (int x = 1; x < size; x++) {
			g2d.drawLine(x*MAX_PIXELS, 0, x*MAX_PIXELS, size*MAX_PIXELS);
		}
		// draw horizontal lines
		for (int y = 1; y < size; y++) {
			g2d.drawLine(0, y*MAX_PIXELS, size*MAX_PIXELS, y*MAX_PIXELS);
		}
		g2d.dispose();
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
	 * get the height map at each coordinate
	 * @return the 2D height map
	 */
	public float[][] getHeightMap() {
		return heightMap;
	}
	
	/**
	 * set the values in the height map
	 * @param heightMap
	 */
	public void setHeightMap(float[][] heightMap) {
		this.heightMap = heightMap;
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
	 * Create a gradient to show the range of values
	 * @param width - the width of the gradient
	 * @param height - the height of the gradient
	 * @param min - the minimum height value
	 * @param max - the maximum height value
	 * @return a gradient depicting the range of colors on the heightmap
	 */
	public BufferedImage createGradientImage(int width, int height,	
			float min, float max, boolean monochrome) 
	{
		// create a buffered image and a panel for it
		BufferedImage gradientImage = createCompatibleImage(width, height);
		JPanel gradientPane = new JPanel();
		gradientPane.setLayout(new BoxLayout(gradientPane, BoxLayout.Y_AXIS));
		gradientPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
		
		// Create the maximum and minimum gradients, respectively
		Color gradient1, gradient2;
		if (monochrome) {
			gradient1 = new Color((int)max+128, (int)max+128, (int)max+128); // at top
			gradient2 = new Color((int)min+128, (int)min+128, (int)min+128); // at bottom
		} else {
			gradient1 = new Color(0, (int)max+128, 0); // at top
			gradient2 = new Color(0, (int)min+128, 0); // at bottom
		}
		
		
		// Create the gradient bar with the specified gradients
	    GradientPaint gradient = new GradientPaint(0, 100, gradient1, 0, height, gradient2, false);
	    Graphics2D g2 = (Graphics2D) gradientImage.getGraphics();
	    g2.setPaint(gradient);
	    g2.fillRect(0, 0, width, height);
	    JLabel gradientLabel = new JLabel(new ImageIcon(gradientImage));
	    
	    // Display maximum and minimum values and add them to the panel, 
	    // along with the gradient bar
	    JLabel maxLabel = new JLabel(String.format("%f", max));
	    JLabel minLabel = new JLabel(String.format("%f", min));
	    gradientPane.add(maxLabel);
	    gradientPane.add(gradientLabel);
	    gradientPane.add(minLabel);
	    
	    frame.add(gradientPane, BorderLayout.EAST); // add gradient panel to frame
	    g2.dispose(); // dispose of graphics
	    return gradientImage;
	}

	/**
	 * Create a gradient that is compatible with the screen
	 * @param width
	 * @param height
	 * @return the scaled gradient bar
	 */
	private BufferedImage createCompatibleImage(int width, int height) {
		 return GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice()
			        .getDefaultConfiguration().createCompatibleImage(width, height);
	}

	/**
	 * 
	 * @return whether the image is B&W or not
	 */
	public boolean isMonochrome() {
		return monochrome;
	}

	/**
	 * set the color status of the image
	 * @param monochrome
	 */
	public void setMonochrome(boolean monochrome) {
		this.monochrome = monochrome;
	}
}