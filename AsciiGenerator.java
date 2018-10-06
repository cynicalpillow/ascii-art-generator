import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;
import java.awt.image.Raster;
import java.math.*;
import java.util.*;
import javax.imageio.stream.*;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.metadata.*;
import org.w3c.dom.*;

public class AsciiGenerator {
	static char[] bValChars = {'@', '%', '#', '*', '+', '=', '-', ':', '.', ' '};
	static char[] bValChars1 = {'$','@','B','%','8','&','W','M','#','*','o','a','h','k','b','d','p','q','w','m','Z','O','0',
	'Q','L','C','J','U','Y','X','z','c','v','u','n','x','r','j','f','t','/','\\','|','(',')','1','{','}','[',']','?','-','_',
	'+','~','<','>','i','!','l','I',';',':','"','^','`','\'','.',' '};
	static int scale = 8;
	static int compress = 4;
	static boolean inverted = false;
	static boolean dark = false;
	static double darkSens = 1.0;
	static boolean getGif = false;
	static int speed = 0;
	static boolean large = false;

	public static void main(String args[]){
		try{
			if(args.length > 0){
				if(args[0].equalsIgnoreCase("true"))
					getGif = true;
				if(args.length > 1 && args[1].equalsIgnoreCase("true"))
					dark = true;
				if(args.length > 2 && args[2].equalsIgnoreCase("true"))
					inverted = true;
				if(args.length > 3)
					compress = Integer.parseInt(args[3]);
				if(args.length > 4)
					scale = Integer.parseInt(args[4]);
				if(args.length > 5)
					darkSens = Double.parseDouble(args[5]);
				if(args.length > 6)
					speed = Integer.parseInt(args[6]);
				if(args.length > 7 && args[7].equalsIgnoreCase("true"))
					large = true;
				if(inverted)
					reverse();
			}
			if(getGif){
				File f = new File("image.gif");
				ImageFrame[] frames = readGif(new FileInputStream(f));
				ImageOutputStream output = new FileImageOutputStream(new File("asciiimage.gif"));
			    GifSequenceWriter writer = new GifSequenceWriter(output, frames[0].getImage().getType(), Math.max(speed*10, 1), true);
			    int c = 1;
				for(ImageFrame fr : frames){
					BufferedImage b = fr.getImage();
					int[][] vals = findImageVals(b);
					BufferedImage ret = new BufferedImage(vals[0].length*scale, vals.length*scale, BufferedImage.TYPE_BYTE_GRAY);
					Graphics g = ret.getGraphics();
					Font fo = new Font(Font.MONOSPACED, Font.PLAIN, scale);
					if(!dark)g.fillRect(0, 0, vals[0].length*scale, vals.length*scale);
					g.setFont(fo);
					if(!dark)g.setColor(Color.black);
					System.out.println("------------------------------------------");
					System.out.println("Generating frame " + c + "...");
					System.out.println("Height: " + vals.length*scale + "px Width: " + vals[0].length*scale +"px");
					for(int i = 0; i < vals.length; i++)
						for(int j = 0; j < vals[i].length; j++)
							g.drawString(String.valueOf((large)?bValChars1[binarySearchChars(vals[i][j])]:bValChars[binarySearchChars(vals[i][j])]), j*scale, i*scale);
					g.dispose();
					writer.writeToSequence(ret);
					c++;
				}
				writer.close();
    			output.close();
			} else {
				BufferedImage image = ImageIO.read(new File("image.jpg"));
				int[][] vals = findImageVals(image);
				BufferedImage ret = new BufferedImage(vals[0].length*scale, vals.length*scale, BufferedImage.TYPE_BYTE_GRAY);
				Graphics g = ret.getGraphics();
				Font f = new Font(Font.MONOSPACED, Font.PLAIN, scale);
				if(!dark)g.fillRect(0, 0, vals[0].length*scale, vals.length*scale);
				g.setFont(f);
				if(!dark)g.setColor(Color.black);
				System.out.println("Height: " + vals.length*scale + "px Width: " + vals[0].length*scale +"px");
				System.out.println("Generating...");
				PrintWriter p = new PrintWriter(new File("image.txt"));
				for(int i = 0; i < vals.length; i++){
					for(int j = 0; j < vals[i].length; j++){
						p.write((large)?bValChars1[binarySearchChars(vals[i][j])]:bValChars[binarySearchChars(vals[i][j])]);
						g.drawString(String.valueOf((large)?bValChars1[binarySearchChars(vals[i][j])]:bValChars[binarySearchChars(vals[i][j])]), j*scale, i*scale);
					}
					p.write("\r\n");
				}
				p.close();
				g.dispose();
				File output = new File("asciiimage.jpg");
				ImageIO.write(ret, "jpg", output);
				System.out.println("Generated at " + output.getAbsolutePath());
			}
		}catch(Exception e){
			System.out.println(e);
			System.out.println("Error occured.");
			System.out.println("\nRun with arguments: " + "\nGif or nah: true/false" + "\nDark/light: true/false" + "\nInverted: true/false" + "\nCompression: int value" + "\nFont size: int value" + "\nDark/light sensitivity: decimal value");
		}
	}
	public static void reverse(){
		if(!large){
			for(int i = 0; i < bValChars.length / 2; i++){
			    char temp = bValChars[i];
			    bValChars[i] = bValChars[bValChars.length - i - 1];
			    bValChars[bValChars.length - i - 1] = temp;
			}
		} else {
			for(int i = 0; i < bValChars1.length / 2; i++){
			    char temp = bValChars1[i];
			    bValChars1[i] = bValChars1[bValChars1.length - i - 1];
			    bValChars1[bValChars1.length - i - 1] = temp;
			}
		}
	}
	public static int binarySearchChars(int val){
		int low = 0;
		int high = (large)?bValChars1.length-1 : bValChars.length-1;
		double interval = 255.0/((large)?bValChars1.length : bValChars.length);
		while(low <= high){
			int mid = (low+high)/2;
			if((int)Math.round(mid*interval) <= val && (int)Math.round(mid*interval+interval) >= val)
				return mid;
			if((int)Math.round(mid*interval) > val)
				high = mid-1;
			else
				low = mid+1;
		}
		return -1;
	}
	public static int[][] findImageVals(BufferedImage image){
		BufferedImage rr = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);  
		Graphics g = rr.getGraphics();  
		g.drawImage(image, 0, 0, null); 
		g.dispose();
		Raster r = rr.getData();
		int[][] result = new int[r.getHeight()/compress][r.getWidth()/compress];
		int y = 0;
		int x = 0;
		for(int i = 0; i < r.getHeight(); i+=compress){
			x = 0;
			for(int j = 0; j < r.getWidth(); j+=compress){
				if(y < result.length && x < result[0].length){
					if(compress > 1)result[y][x] = compressSection(i, j, r.getHeight(), r.getWidth(), r);
					else result[y][x] = r.getSample(j, i, 0);
					x++;
				}
			}
			y++;
		}

		return result;
	}
	private static ImageFrame[] readGif(InputStream stream) throws IOException{
	    ArrayList<ImageFrame> frames = new ArrayList<ImageFrame>(2);

	    ImageReader reader = (ImageReader) ImageIO.getImageReadersByFormatName("gif").next();
	    reader.setInput(ImageIO.createImageInputStream(stream));

	    int lastx = 0;
	    int lasty = 0;

	    int width = -1;
	    int height = -1;

	    IIOMetadata metadata = reader.getStreamMetadata();

	    Color backgroundColor = null;

	    if(metadata != null) {
	        IIOMetadataNode globalRoot = (IIOMetadataNode) metadata.getAsTree(metadata.getNativeMetadataFormatName());

	        NodeList globalColorTable = globalRoot.getElementsByTagName("GlobalColorTable");
	        NodeList globalScreeDescriptor = globalRoot.getElementsByTagName("LogicalScreenDescriptor");

	        if (globalScreeDescriptor != null && globalScreeDescriptor.getLength() > 0){
	            IIOMetadataNode screenDescriptor = (IIOMetadataNode) globalScreeDescriptor.item(0);

	            if (screenDescriptor != null){
	                width = Integer.parseInt(screenDescriptor.getAttribute("logicalScreenWidth"));
	                height = Integer.parseInt(screenDescriptor.getAttribute("logicalScreenHeight"));
	            }
	        }

	        if (globalColorTable != null && globalColorTable.getLength() > 0){
	            IIOMetadataNode colorTable = (IIOMetadataNode) globalColorTable.item(0);

	            if (colorTable != null) {
	                String bgIndex = colorTable.getAttribute("backgroundColorIndex");

	                IIOMetadataNode colorEntry = (IIOMetadataNode) colorTable.getFirstChild();
	                while (colorEntry != null) {
	                    if (colorEntry.getAttribute("index").equals(bgIndex)) {
	                        int red = Integer.parseInt(colorEntry.getAttribute("red"));
	                        int green = Integer.parseInt(colorEntry.getAttribute("green"));
	                        int blue = Integer.parseInt(colorEntry.getAttribute("blue"));

	                        backgroundColor = new Color(red, green, blue);
	                        break;
	                    }

	                    colorEntry = (IIOMetadataNode) colorEntry.getNextSibling();
	                }
	            }
	        }
	    }

	    BufferedImage master = null;
	    boolean hasBackround = false;

	    for (int frameIndex = 0;; frameIndex++) {
	        BufferedImage image;
	        try{
	            image = reader.read(frameIndex);
	        }catch (IndexOutOfBoundsException io){
	            break;
	        }

	        if (width == -1 || height == -1){
	            width = image.getWidth();
	            height = image.getHeight();
	        }

	        IIOMetadataNode root = (IIOMetadataNode) reader.getImageMetadata(frameIndex).getAsTree("javax_imageio_gif_image_1.0");
	        IIOMetadataNode gce = (IIOMetadataNode) root.getElementsByTagName("GraphicControlExtension").item(0);
	        NodeList children = root.getChildNodes();

	        int delay = Integer.valueOf(gce.getAttribute("delayTime"));

	        String disposal = gce.getAttribute("disposalMethod");

	        if (master == null){
	            master = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
	            master.createGraphics().setColor(backgroundColor);
	            master.createGraphics().fillRect(0, 0, master.getWidth(), master.getHeight());

	        hasBackround = image.getWidth() == width && image.getHeight() == height;

	            master.createGraphics().drawImage(image, 0, 0, null);
	        }else{
	            int x = 0;
	            int y = 0;

	            for (int nodeIndex = 0; nodeIndex < children.getLength(); nodeIndex++){
	                Node nodeItem = children.item(nodeIndex);

	                if (nodeItem.getNodeName().equals("ImageDescriptor")){
	                    NamedNodeMap map = nodeItem.getAttributes();

	                    x = Integer.valueOf(map.getNamedItem("imageLeftPosition").getNodeValue());
	                    y = Integer.valueOf(map.getNamedItem("imageTopPosition").getNodeValue());
	                }
	            }

	            if (disposal.equals("restoreToPrevious")){
	                BufferedImage from = null;
	                for (int i = frameIndex - 1; i >= 0; i--){
	                    if (!frames.get(i).getDisposal().equals("restoreToPrevious") || frameIndex == 0){
	                        from = frames.get(i).getImage();
	                        break;
	                    }
	                }

	                {
	                    ColorModel model = from.getColorModel();
	                    boolean alpha = from.isAlphaPremultiplied();
	                    WritableRaster raster = from.copyData(null);
	                    master = new BufferedImage(model, raster, alpha, null);
	                }
	            }else if (disposal.equals("restoreToBackgroundColor") && backgroundColor != null){
	                if (!hasBackround || frameIndex > 1){
	                    master.createGraphics().fillRect(lastx, lasty, frames.get(frameIndex - 1).getWidth(), frames.get(frameIndex - 1).getHeight());
	                }
	            }
	            master.createGraphics().drawImage(image, x, y, null);

	            lastx = x;
	            lasty = y;
	        }

	        {
	            BufferedImage copy;

	            {
	                ColorModel model = master.getColorModel();
	                boolean alpha = master.isAlphaPremultiplied();
	                WritableRaster raster = master.copyData(null);
	                copy = new BufferedImage(model, raster, alpha, null);
	            }
	            frames.add(new ImageFrame(copy, delay, disposal, image.getWidth(), image.getHeight()));
	        }

	        master.flush();
	    }
	    reader.dispose();

	    return frames.toArray(new ImageFrame[frames.size()]);
	}
	public static int compressSection(int y, int x, int maxH, int maxW, Raster r){
		double average = 0.0;
		int c = 0;
		for(int i = y; i < Math.min(y+compress, maxH); i++){
			for(int j = x; j < Math.min(x+compress, maxW); j++){
				average += r.getSample(j, i, 0);
				c++;
			}
		}
		return Math.min((int)(Math.round(average/c)*darkSens), 255);
	}
}