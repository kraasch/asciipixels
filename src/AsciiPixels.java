import javax.swing.JFrame;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.Graphics2D;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.File;

public class AsciiPixels extends JFrame {

  // constants .
  private static ArrayList<String> header = new ArrayList<String>();
  private static ArrayList<String> body = new ArrayList<String>();
  private static ArrayList<String> parsedBody = new ArrayList<String>();
  private static String VAR_REGEX   = "ßäöüa-z0-9A-Z";
  private static String SYMBOLS_REGEX = "#$'.,-" + VAR_REGEX; // <>?!@$%^&*()_-;, TODO: more symbols .
  private static String DELIM_REGEX = ",=\"";
  private static String NUMS_REGEX  = "[0-9,]+";
  private static Color TRANSPARENT = new Color(0, 0, 0, 1); // 100% transparency .

  // options (set in the header) .
  private static Color defaultColor = TRANSPARENT;
  private static int width = 10; // width of pixels .
  private static int height = 10; // width of pixels .
  private static String filename = "out";

  // symbol-color pairs (set in the header) .
  private static ArrayList<Color> colors = new ArrayList<Color>();
  private static ArrayList<Character> symbols = new ArrayList<Character>();

  // read variables from body .
  private static int maxLineLen = -1; // longest line in body defines the max width of graphic .
  private static int numX = -1; // number of pixel in x direction .
  private static int numY = -1; // number of pixel in y direction .
  private static int xSize = -1; // the size of the window in x direction .
  private static int ySize = -1; // the size of the window in y direction .

  public static void readOptions() throws Exception {
    for (String line : header) {
      if (line.matches("[" + SYMBOLS_REGEX + VAR_REGEX + DELIM_REGEX + "]+")) { // possible variable/values delimiters, or symbols .
        String[] parts = line.split("\"");
        if (line.startsWith("WIDTH=\"")) {
          width = Integer.parseInt(parts[1]);
        } else if (line.startsWith("HEIGHT=\"")) {
          height = Integer.parseInt(parts[1]);
        } else if (line.startsWith("FILENAME=\"")) {
          filename = parts[1];
        } else if (line.startsWith("DEFAULT=\"")) {
          String str = parts[1]==null?"":parts[1];
          if (str.equals("WHITE")) {
            defaultColor = Color.WHITE;
          } else if (str.equals("BLACK")) {
            defaultColor = Color.BLACK;
          } else {
            System.err.println("Header lines: default background should be set to WHITE or BLACK.");
            System.err.println("              other values result in a transparent background.");
          }
        } else if (line.matches("^([" + SYMBOLS_REGEX + "]+=\"" + NUMS_REGEX + "\")$")) {
          // convert symbol part to Character, and number part to Color .
          String[] nums = parts[1].split(",");
          int r = Integer.parseInt(nums[0]);
          int g = Integer.parseInt(nums[1]);
          int b = Integer.parseInt(nums[2]);
          char c = parts[0].charAt(0);
          if (c != '\u0000' && r >= 0 && r <= 255 && g >= 0 && g <= 255 && b >= 0 && b <= 255) {
            symbols.add(new Character(c));
            colors.add(new Color(r, g, b));
          } else {
            System.err.println("Header lines: symbol-color pairs broken (color values should be from 0-255)");
          }
        }
      } else {
        System.err.println("Header lines: can define symbol-color pairs and the variables: WIDTH, HEIGHT, FILENAME");
        System.err.println("              to  match symbols to colors can also contain '" + SYMBOLS_REGEX + "' and '" + NUMS_REGEX + "'.");
        System.err.println("              to set options should only contain only '" + SYMBOLS_REGEX + VAR_REGEX + DELIM_REGEX + "'.");
        break;
      }
    }
  }

  public static void parseBody() throws Exception {
    System.out.println("Lines read as:");
    for (String line : body) {
      line = line.replaceAll(" ", "");
      line = line.replaceAll("\t", ""); // TODO: remove more white spaces than only tabs .
      if (line.matches("^([" + SYMBOLS_REGEX + VAR_REGEX + "]+)$")) { // only allow symbol, numbers or characters .
                                                                      // watch out for the longest line .
        int len = line.length();
        if (len > maxLineLen) {
          maxLineLen = len;
        }
        parsedBody.add(line);
        System.out.println("  '" + line + "'");
      } else {
        System.err.println("Body lines to should only contain only '" + SYMBOLS_REGEX + VAR_REGEX + "'.");
        break;
      }
    }
  }

  public static void paintImage(Graphics2D g) {
    for (int j = 0; j < numY; j++) {
      String line = parsedBody.get(j);
      for (int i = 0; i < numX; i++) {
        char symbol = ' ';
        Color c = defaultColor;
        int index = -1;
        if (i < line.length()) {
          symbol = line.charAt(i);
          index = symbols.indexOf(symbol);
          if (index != -1 && index < colors.size()) {
            c = colors.get(index);
          }
        }
        makeRect(g, c, i * width, j * height, width, height);
      }
    }
  }

  public static void makeRect(Graphics2D g, Color c, int x, int y, int w, int h) {
    g.setColor(c);
    g.fillRect(x, y, w, h);
    g.setColor(Color.WHITE);
  }

  public static void parsePipe() throws IOException {
    BufferedReader standardInput = new BufferedReader(new InputStreamReader(System.in));
    String line = null;
    boolean isHeader = true;
    while ((line = standardInput.readLine()) != null) {
      if (line.equals("")) { // body is everything after first fully empty line .
        isHeader = false;
      } else {
        if (isHeader) {
          header.add(line);
        } else {
          body.add(line);
        }
      }
    }
  }

  public static void main(String[] args) {
    AsciiPixels ap = new AsciiPixels();
    try {
      ap.parsePipe();
    } catch (IOException ioEx) {
      System.err.println("Error while reading from pipe.");
      return;
    }
    try {
      ap.readOptions();
    } catch (Exception ex) {
      System.err.println("Error while interpreting file header.");
      return;
    }
    System.out.println("Header lines read: " + Integer.toString(header.size()) + ".");
    System.out.println("  Symbols read:    " + Arrays.toString(symbols.toArray()) + ".");
    System.out.println("  Colors read:     " + Arrays.toString(colors.toArray()) + ".");
    try {
      ap.parseBody();
    } catch (Exception ex) {
      System.err.println("Error while interpreting file body.");
      return;
    }
    numY = body.size();
    numX = maxLineLen;
    System.out.println("Body lines read:   " + numY + " (height).");
    System.out.println("  Max length read: " + numX + " (width).");
    xSize = numX * width;
    ySize = numY * height;
    try {
      BufferedImage image = new BufferedImage(xSize, ySize, BufferedImage.TYPE_INT_ARGB);
      Graphics2D graphics = image.createGraphics();
      ap.paintImage(graphics);
      ImageIO.write(image, "png", new File(filename + ".png")); // export the new image .
    } catch (IOException ioEx) {
      System.err.println("Error while writing to file.");
      return;
    }
  }
}
