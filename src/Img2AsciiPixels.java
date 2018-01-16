import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.Graphics2D;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.File;
import java.awt.image.DataBufferByte;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.lang.StringBuffer;
import java.util.stream.Collectors;
// TODO: imports, remove some .

public class Img2AsciiPixels {

    // variables .
    private static ArrayList<ArrayList<Color>> colors = new ArrayList<ArrayList<Color>>();
    private static ArrayList<Color> reduction = new ArrayList<Color>();
    private static ArrayList<String> representation = new ArrayList<String>();
    private static int representationIndex = 0;

    private static String nextRepresentation() {
	int temp = representationIndex;
	representationIndex++;
	return getCharForNumber(temp);
    }

    private static String getCharForNumber(int i) {
	String res = null;
	if (i >= 0 && i <= 25) { 
	    res = String.valueOf((char)(i + 65));
	} else if (i >= 26 && i <= 51) {
	    res = String.valueOf((char)(i + 71));
	}
	return res;
    }
    
    public static void main(String[] args) {
	
	// input . TODO .
	String fileName = "chickens.png";
	int lastIndex = fileName.lastIndexOf(".");
	String fileCore = fileName.contains(".") ? fileName.substring(0, lastIndex) : fileName; // split at most-right '.' .
	fileCore += "_converted";
	boolean isAddTimestamp = false; // add timestamp to file name (for debugging purposes) .
	int numPartsX = 17; // number of pixels to split into horizontally .
	int numPartsY = 17; // number of pixels to split into vertically .
	int threshold = 17; // difference by which colors can be off before being defined .
	int width = 8; 
	int height = 8; 
	
	// setup .
	Img2AsciiPixels i2ap = new Img2AsciiPixels();
	
	// read image .
	BufferedImage img = null;
	try {
	    img = ImageIO.read(new File(fileName));
	} catch (IOException e) {
	    System.err.println("Error while reading image.");
	    return;
	}
	
	// parse image and convert to text (then to asciipixels) .
	StringBuffer buffer = new StringBuffer();
	ArrayList<Color> colorList = null;
	try {
	    // divide in (numPartsY x numPartsX) chunks .
	    for (int i = 0; i < numPartsY; i++) {
		colorList = new ArrayList<Color>();
		for (int j = 0; j < numPartsX; j++) {
		    // scan chunks individually .
		    int blueSum = 0;
		    int greenSum = 0;
		    int redSum = 0;
		    int num = 0;
		    for (int y = 0; y < img.getHeight()/numPartsY; y++) {
			for (int x = 0; x < img.getWidth()/numPartsX; x++) {
			    int color = img.getRGB(j * numPartsX + x, i * numPartsY + y);
			    int blue  = (color & 0xff);
			    int green = (color & 0xff00) >> 8;
			    int red   = (color & 0xff0000) >> 16;
			    blueSum += blue;
			    greenSum += green;
			    redSum += red;
			    num++;
			}
		    }
		    // compute means of chunks and write them to color array .
		    colorList.add(new Color(redSum/num, greenSum/num, blueSum/num));
		}
		colors.add(colorList);
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	    System.err.println("Error while converting image to asciipixels.");
	    return;
	}

	// reduce colors to necessary reduction within threshold .
	for (ArrayList<Color> cs : colors) {
	    for (Color color : cs) {
		int r = color.getRed();
		int g = color.getGreen();
		int b = color.getBlue();
		Color goodRepresentation = null;
		int goodIndex = 0;
		for (int i = 0; i < reduction.size(); i++) {
		    Color colorReduce = reduction.get(i);
		    int rr = colorReduce.getRed();
		    int gg = colorReduce.getGreen();
		    int bb = colorReduce.getBlue();
		    // sum up the differences for each color value .
		    int differences = Math.abs(r - rr) + Math.abs(g - gg) + Math.abs(b - bb); 
		    if (differences < threshold) {
			goodRepresentation = colorReduce;
			goodIndex = i;
		    }
		}
		// if representation was found - re-use its letter .
		// otherwise add new color and letter to reduced colors .
		if (goodRepresentation == null) {
		    reduction.add(color);
		    String rep = nextRepresentation();
		    representation.add(rep);
		    buffer.append(rep);
		} else {
		    String rep = representation.get(goodIndex);
		    buffer.append(rep);
		}
	    }
	    buffer.append("\n");
	}

	// write header file .
	StringBuffer headerBuffer = new StringBuffer();
	headerBuffer.append("WIDTH=\"" + width + "\"\n");
	headerBuffer.append("HEIGHT=\"" + height + "\"\n");
	headerBuffer.append("FILENAME=\"" + fileCore + "\"\n");
	// write symbol-color pairs (eg x="140,90,50") .
	for (int i = 0; i < reduction.size(); i++) {
	    Color color = reduction.get(i);
	    int r = color.getRed();
	    int g = color.getGreen();
	    int b = color.getBlue();
	    String symbol = representation.get(i);
	    headerBuffer.append(symbol + "=\"" + r + "," + g + "," + b + "\"\n");
	}
	headerBuffer.append("\n"); // split header form body .

	// write to file .
        BufferedWriter writer = null;
        try {
	    
            // create a file
	    String timestamp = "";
	    if (isAddTimestamp) { // DEBUG .
		timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm").format(Calendar.getInstance().getTime());
		timestamp = "_" + timestamp;
	    }
            File logFile = new File(fileCore + timestamp + ".txt");
            System.out.println("Writing to: " + logFile.getCanonicalPath());
            writer = new BufferedWriter(new FileWriter(logFile));
            writer.write(headerBuffer.toString());
            writer.write(buffer.toString());
	    
        } catch (Exception e) {
	    System.err.println("Error while writing to file.");
        } finally {
            try {
                writer.close();
            } catch (Exception e) {
            }
	}
	
	return;
    } // end main .
}
