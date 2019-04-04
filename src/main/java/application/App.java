package application;

import model.Image;
import model.Vector2D;
import net.coobird.thumbnailator.makers.FixedSizeThumbnailMaker;
import net.coobird.thumbnailator.resizers.DefaultResizerFactory;
import net.coobird.thumbnailator.resizers.Resizer;
import network.NeuralNetwork;
import network.NeuralNetworkBuilder;
import network.activationfunction.FastSigmoid;
import network.activationfunction.RyanSigmoid;
import network.activationfunction.Sigmoid;
import network.neuron.InputNeuron;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Random;

class testClass {
	public static void main(String[] args) throws IOException {
		ImageIO.write(App.drawGraph(
				new Vector2D[] { new Vector2D(0, 0.8), new Vector2D(0, 0.5), new Vector2D(0, 0.8), new Vector2D(0, 0.8), new Vector2D(0, 0.8),
						new Vector2D(0, 0.4), new Vector2D(0, 0.9), new Vector2D(0, 0.3), new Vector2D(0, 0.8), new Vector2D(0, 1),
						new Vector2D(0, 0.3), new Vector2D(0, 0.3), new Vector2D(0, 0.4), new Vector2D(0, 0.2), new Vector2D(0, 0.7),
						new Vector2D(0, 0.9), new Vector2D(0, 0.8), new Vector2D(0, 0.1), new Vector2D(0, 0.6), new Vector2D(0, 0.1),
						new Vector2D(0, 0.6), }), "png", new File("testGraph.png"));
	}
}

public class App {
	public final static String ANNOTATION_FOLDER = "C:\\Users\\npreen.CARGOSOFT\\Desktop\\NNTest\\Annotation";
	public final static String IMAGES_FOLDER = "C:\\Users\\npreen.CARGOSOFT\\Desktop\\NNTest\\Images";
	public final static int IMAGE_WIDTH = 50;
	public final static int IMAGE_HEIGHT = 50;
	public final static DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
	public final static Random rnd = new Random();
	public final static DecimalFormat df1 = new DecimalFormat("0.0");

	public static void main(String... args) throws IOException, ParserConfigurationException, SAXException {
		File[] trainingFiles = loadTrainingFiles().toArray(new File[0]);
		NeuralNetwork finder = NeuralNetworkBuilder.build().inputLayer(IMAGE_WIDTH * IMAGE_HEIGHT).hiddenLayer(35, new RyanSigmoid())
				.hiddenLayer(35, new RyanSigmoid()).outputLayer(4, new RyanSigmoid()).getNeuralNetwork();
		//		NeuralNetwork specifier = NeuralNetworkBuilder.build().inputLayer(IMAGE_WIDTH * IMAGE_HEIGHT).hiddenLayer(100).outputLayer(120).getNeuralNetwork();

		File testFile = trainingFiles[rnd.nextInt(trainingFiles.length)];
		System.out.printf("Testing file: %s ...\n", testFile.getName());
		Image testImage = extractImage(testFile);
		testNetwork(finder, testFile, testImage.getMappedBndBox());
		BufferedImage image = ImageIO.read(testImage.getImageFile());
		drawRectangleOnImage(image, (int) testImage.getBndBox()[0], (int) testImage.getBndBox()[1], (int) testImage.getBndBox()[2],
				(int) testImage.getBndBox()[3], Color.BLACK);

		drawRectangleOnImage(image, (int) mapNumber(finder.getOutputLayer().getNeurons()[0].fire(), 0, 1, 0, testImage.getWidth()),
				(int) mapNumber(finder.getOutputLayer().getNeurons()[1].fire(), 0, 1, 0, testImage.getHeight()),
				(int) mapNumber(finder.getOutputLayer().getNeurons()[2].fire(), 0, 1, 0, testImage.getWidth()),
				(int) mapNumber(finder.getOutputLayer().getNeurons()[3].fire(), 0, 1, 0, testImage.getHeight()), Color.RED);

		ImageIO.write(image, "png", new File("beforeTraining.png"));
		int epochs = 1000;
		Vector2D[] vectors = new Vector2D[epochs];

		double loss = 0;
		for (int i = 0; i < epochs; i++) {
			File trainFile = trainingFiles[0];
			double[] targets = extractImage(trainFile).getMappedBndBox();
			setupInputs(finder, scaleImage(ImageIO.read(trainFile)));
			finder.train(targets, 0.5);

			if (i % (epochs / 100) == 0) {
				loss = calculateLoss(finder, targets);
				System.out.printf("%s: %s\n", i, loss);
			}
			vectors[i] = new Vector2D(i, loss);
		}

		ImageIO.write(drawGraph(vectors), "png", new File(epochs + "Graph.png"));
		System.out.println("After training ...");
		testNetwork(finder, testFile, testImage.getMappedBndBox());
		image = ImageIO.read(testImage.getImageFile());
		drawRectangleOnImage(image, (int) testImage.getBndBox()[0], (int) testImage.getBndBox()[1], (int) testImage.getBndBox()[2],
				(int) testImage.getBndBox()[3], Color.BLACK);

		drawRectangleOnImage(image, (int) mapNumber(finder.getOutputLayer().getNeurons()[0].fire(), 0, 1, 0, testImage.getWidth()),
				(int) mapNumber(finder.getOutputLayer().getNeurons()[1].fire(), 0, 1, 0, testImage.getHeight()),
				(int) mapNumber(finder.getOutputLayer().getNeurons()[2].fire(), 0, 1, 0, testImage.getWidth()),
				(int) mapNumber(finder.getOutputLayer().getNeurons()[3].fire(), 0, 1, 0, testImage.getHeight()), Color.RED);

		ImageIO.write(image, "png", new File("afterTraining.png"));
		System.out.println("\n");

		System.out.println("test");
	}

	public static BufferedImage drawGraph(Vector2D[] vectors) throws IOException {
		int width = 5000;
		int height = 5000;
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics2D = image.createGraphics();
		graphics2D.setFont(new Font("TimesRoman", Font.PLAIN, 25));
		int offset = 100;
		int minX = 1000;
		int maxX = 4000;
		int minY = 1000;
		int maxY = 4000;

		for (double y = 0; y < 1; y += 0.1) {
			int curDrawY = (int) mapNumber(y, 0, 1, minY + offset, maxY - offset);
			graphics2D.setStroke(new BasicStroke(1));
			graphics2D.setPaint(Color.lightGray);
			graphics2D.drawLine(maxX + offset, curDrawY, minX - offset, curDrawY);

			graphics2D.setPaint(Color.black);
			graphics2D.setStroke(new BasicStroke(5));
			drawCenteredString(graphics2D, df1.format(y), new Rectangle((minX - offset * 2) - offset / 2, curDrawY - offset / 2, offset, offset),
					graphics2D.getFont());
		}

		for (int x = 0; x < vectors.length; x++) {
			int curDrawX = (int) mapNumber(x, 0, vectors.length - 1, minX + offset, maxX - offset);
			if (x % (vectors.length / 10) == 0) {
				graphics2D.setStroke(new BasicStroke(1));
				graphics2D.setPaint(Color.lightGray);
				graphics2D.drawLine(curDrawX, maxY + offset, curDrawX, minY - offset);

				graphics2D.setPaint(Color.black);
				graphics2D.setStroke(new BasicStroke(5));
				drawCenteredString(graphics2D, x + "", new Rectangle(curDrawX - offset / 2, (maxY + offset * 2) - offset / 2, offset, offset),
						graphics2D.getFont());
			}

			if (x > 0) {
				graphics2D.setStroke(new BasicStroke(2));
				int curDrawY = (int) mapNumber(vectors[x].getY(), 0, 1, minY + offset, maxY - offset);
				int beforeDrawX = (int) mapNumber(x - 1, 0, vectors.length - 1, minX + offset, maxX - offset);
				int beforeDrawY = (int) mapNumber(vectors[x - 1].getY(), 0, 1, minY + offset, maxY - offset);
				graphics2D.drawLine(curDrawX, curDrawY, beforeDrawX, beforeDrawY);
			}
		}
		graphics2D.setStroke(new BasicStroke(1));
		graphics2D.setPaint(Color.black);
		graphics2D.drawRect(minX, minY, maxX - minX, maxY - minY);

		graphics2D.dispose();
		return image;
	}

	private static double calculateLoss(NeuralNetwork nn, double[] targets) {
		double ret = 0;
		for (int i = 0; i < nn.getOutputLayer().getNeurons().length; i++) {
			ret += Math.abs((nn.getOutputLayer().getNeurons()[i].fire() - targets[i]) / 4);
		}
		return ret;
	}

	private static void drawRectangleOnImage(BufferedImage image, int xMin, int yMin, int xMax, int yMax, Paint strokeColor) {
		Graphics2D graphics2D = image.createGraphics();
		graphics2D.setStroke(new BasicStroke(1));
		graphics2D.setPaint(strokeColor);
		graphics2D.drawRect(xMin, yMin, xMax - xMin, yMax - yMin);
		graphics2D.dispose();
	}

	private static void testNetwork(NeuralNetwork nn, File file, double[] targets) throws IOException, ParserConfigurationException, SAXException {
		setupInputs(nn, scaleImage(ImageIO.read(file)));

		System.out.printf("| %20s | %20s |\n| %20s | %20s |\n| %20s | %20s |\n| %20s | %20s |\n| %20s | %20s |\n", "Calculated", "Expected",
				nn.getOutputLayer().getNeurons()[0].fire(), targets[0], nn.getOutputLayer().getNeurons()[1].fire(), targets[1],
				nn.getOutputLayer().getNeurons()[2].fire(), targets[2], nn.getOutputLayer().getNeurons()[3].fire(), targets[3]);
	}

	private static File imageToAnnotation(File file) {
		return new File(file.getAbsolutePath().replace("Images", "Annotation").replace(".jpg", ""));
	}

	private static File annotationToImage(File file) {
		return new File(file.getAbsolutePath().replace("Annotation", "Images").replace(".jpg", ""));
	}

	private static Image extractImage(File file) throws ParserConfigurationException, IOException, SAXException {
		File annotation = imageToAnnotation(file);
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(annotation);
		doc.getDocumentElement().normalize();
		int width = Integer.parseInt(doc.getElementsByTagName("width").item(0).getTextContent());
		int height = Integer.parseInt(doc.getElementsByTagName("height").item(0).getTextContent());
		String name = doc.getElementsByTagName("name").item(0).getTextContent();
		double[] bndBox = new double[4];
		bndBox[0] = Integer.parseInt(doc.getElementsByTagName("xmin").item(0).getTextContent());
		bndBox[1] = Integer.parseInt(doc.getElementsByTagName("ymin").item(0).getTextContent());
		bndBox[2] = Integer.parseInt(doc.getElementsByTagName("xmax").item(0).getTextContent());
		bndBox[3] = Integer.parseInt(doc.getElementsByTagName("ymax").item(0).getTextContent());

		return new Image(file, annotation, name, width, height, bndBox,
				new double[] { mapNumber(bndBox[0], 0, width, 0, 1), mapNumber(bndBox[1], 0, height, 0, 1), mapNumber(bndBox[2], 0, width, 0, 1),
						mapNumber(bndBox[3], 0, height, 0, 1) });
	}

	private static void setupInputs(NeuralNetwork nn, BufferedImage image) {
		for (int i = 0; i < nn.getInputLayer().getNeurons().length; i++) {
			InputNeuron inputNeuron = (InputNeuron) nn.getInputLayer().getNeurons()[i];
			int y = i / IMAGE_HEIGHT;
			int x = i - y * IMAGE_WIDTH;
			inputNeuron.setValue(image.getRGB(x, y));
		}
	}

	private static BufferedImage scaleImage(BufferedImage imageToScale) {
		Resizer resizer = DefaultResizerFactory.getInstance()
				.getResizer(new Dimension(imageToScale.getWidth(), imageToScale.getHeight()), new Dimension(IMAGE_WIDTH, IMAGE_HEIGHT));
		return new FixedSizeThumbnailMaker(IMAGE_WIDTH, IMAGE_HEIGHT, false, true).resizer(resizer).make(imageToScale);
	}

	private static Collection<File> loadTrainingFiles() {
		return FileUtils.listFiles(new File(IMAGES_FOLDER), new RegexFileFilter("^(.*?)"), DirectoryFileFilter.DIRECTORY);
	}

	private static double mapNumber(double x, double in_min, double in_max, double out_min, double out_max) {
		return (x - in_min) * ((out_max - out_min) / (in_max - in_min)) + out_min;
	}

	public static void drawCenteredString(Graphics g, String text, Rectangle rect, Font font) {
		FontMetrics metrics = g.getFontMetrics(font);
		int x = rect.x + (rect.width - metrics.stringWidth(text)) / 2;
		int y = rect.y + ((rect.height - metrics.getHeight()) / 2) + metrics.getAscent();
		g.setFont(font);
		g.drawString(text, x, y);
	}
}
