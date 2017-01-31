package net.zylk.alfresco.addons.ocr.jfreeSane.device.manager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;

import au.com.southsky.jfreesane.OptionValueConstraintType;
import au.com.southsky.jfreesane.OptionValueType;
import au.com.southsky.jfreesane.RangeConstraint;
import au.com.southsky.jfreesane.SaneDevice;
import au.com.southsky.jfreesane.SaneException;
import au.com.southsky.jfreesane.SaneOption;
import au.com.southsky.jfreesane.SaneSession;
import au.com.southsky.jfreesane.SaneStatus;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.io.Closeables;

/**
 * @author sergio
 * 
 */
public class ScanManager {

	private static Log logger = LogFactory.getLog(ScanManager.class);

	private SaneSession session;

	public static ArrayList<BufferedImage> adfAcquisitionSucceeds(SaneDevice device) {
		logger.debug("In adfAcquisitionSucceeds()");
		ArrayList<BufferedImage> alHojasEscaneadas = new ArrayList<BufferedImage>();
		boolean status = true;
		int count = 0;

		// Open the session, select the device and open the device first
		try {
			logger.debug("recogemos las opciones del escaner");
			//logger.debug("" + device.getOption("source").getStringConstraints() + " - " + device.getOption("source").getStringValue());
			//logger.debug("" + device.getOption("source").getStringConstraints() + " - " + device.getOption("source").getStringValue());

			//Assert.assertTrue(device.getOption("source").getStringConstraints().contains("ADF Front"));
			//device.getOption("source").setStringValue("ADF Front");
			
			

			while (status == true && count < 25) {
				try {
					logger.debug("añado hoja");
					BufferedImage img = device.acquireImage();
					logger.debug("Extrae la información");
					alHojasEscaneadas.add(img);
					
					//alHojasEscaneadas.add(device.acquireImage());
					logger.debug("Añade una hoja al array");
				} catch (SaneException e) {
					if (e.getStatus() == SaneStatus.STATUS_NO_DOCS) {
						// out of documents to read, that's fine
						logger.debug("Ya no hay hojas.");
						status = false;
						break;
					}else{
						logger.debug("@@@@@ SaneException");
						e.printStackTrace();
						break;
					}
				}
				count++;
			}
			logger.debug("fin del escaneo");
		} catch (IOException e1) {
			logger.debug("IOException en el proceso de scan de varias imagenes.");
			e1.printStackTrace();
		}
		return alHojasEscaneadas;
	}

	public void optionGroupsArePopulated() throws Exception {
		SaneDevice device = session.getDevice("test");

		try {
			device.open();
			assertTrue(!device.getOptionGroups().isEmpty());
		} finally {
			Closeables.closeQuietly(device);
		}
	}

	public void imageAcquisitionSucceeds() throws Exception {
		SaneDevice device = session.getDevice("test");
		try {
			device.open();
			BufferedImage image = device.acquireImage();
			File file = File.createTempFile("image", ".png");
			ImageIO.write(image, "png", file);
			System.out.println("Successfully wrote " + file);
		} finally {
			Closeables.closeQuietly(device);
		}
	}

	public void listOptionsSucceeds() throws Exception {
		SaneDevice device = session.getDevice("test");
		try {
			device.open();
			List<SaneOption> options = device.listOptions();
			Assert.assertTrue("Expect multiple SaneOptions", options.size() > 0);
			System.out.println("We found " + options.size() + " options");
			for (SaneOption option : options) {
				System.out.println(option.toString());
				if (option.getType() != OptionValueType.BUTTON) {
					System.out.println(option.getValueCount());
				}
			}
		} finally {
			Closeables.closeQuietly(device);
		}
	}

	public void getOptionValueSucceeds() throws Exception {
		SaneDevice device = session.getDevice("test");
		try {
			device.open();
			List<SaneOption> options = device.listOptions();
			Assert.assertTrue("Expect multiple SaneOptions", options.size() > 0);
			// option 0 is always "Number of options"
			// must be greater than zero

			int optionCount = options.get(0).getIntegerValue();
			Assert.assertTrue("Option count must be > 0", optionCount > 0);

			// print out the value of all integer-valued options

			for (SaneOption option : options) {
				System.out.print(option.getTitle());

				if (!option.isActive()) {
					System.out.print(" [inactive]");
				} else {
					if (option.getType() == OptionValueType.INT && option.getValueCount() == 1 && option.isActive()) {
						System.out.print("=" + option.getIntegerValue());
					} else if (option.getType() == OptionValueType.STRING) {
						System.out.print("=" + option.getStringValue(Charsets.US_ASCII));
					}
				}

				System.out.println();
			}
		} finally {
			Closeables.closeQuietly(device);
		}
	}

	public void setOptionValueSucceedsForString() throws Exception {
		SaneDevice device = session.getDevice("test");

		try {
			device.open();
			SaneOption modeOption = device.getOption("mode");
			assertEquals("Gray", modeOption.setStringValue("Gray"));
		} finally {
			Closeables.closeQuietly(device);
		}
	}

	public void adfAcquisitionSucceeds() throws Exception {
		SaneDevice device = session.getDevice("test");
		device.open();

		Assert.assertTrue(device.getOption("source").getStringConstraints().contains("Automatic Document Feeder"));
		device.getOption("source").setStringValue("Automatic Document Feeder");

		for (int i = 0; i < 20; i++) {
			try {
				device.acquireImage();
			} catch (SaneException e) {
				if (e.getStatus() == SaneStatus.STATUS_NO_DOCS) {
					// out of documents to read, that's fine
					logger.debug("@ Warn:" + e.getStatus());
					break;
				} else {
					throw e;
				}
			}
		}
	}

	public void acquireMonoImage() throws Exception {
		SaneDevice device = session.getDevice("test");
		FileOutputStream stream = null;

		try {
			device.open();
			SaneOption modeOption = device.getOption("mode");
			assertEquals("Gray", modeOption.setStringValue("Gray"));
			BufferedImage image = device.acquireImage();

			File file = File.createTempFile("mono-image", ".png");
			ImageIO.write(image, "png", file);
			System.out.println("Successfully wrote " + file);
		} finally {
			Closeables.closeQuietly(stream);
			Closeables.closeQuietly(device);
		}
	}

	/**
	 * Tests that this SANE client produces images that match
	 * {@link "http://www.meier-geinitz.de/sane/test-backend/test-pictures.html"}
	 * .
	 */

	public void producesCorrectImages() throws Exception {
		SaneDevice device = session.getDevice("test");
		// Solid black and white
		try {
			device.open();
			device.getOption("br-x").setFixedValue(200);
			device.getOption("br-y").setFixedValue(200);

			/*
			 * assertProducesCorrectImage(device, "Gray", 1, "Solid white");
			 * assertProducesCorrectImage(device, "Gray", 8, "Solid white");
			 * assertProducesCorrectImage(device, "Gray", 16, "Solid white");
			 * assertProducesCorrectImage(device, "Gray", 1, "Solid black");
			 * assertProducesCorrectImage(device, "Gray", 8, "Solid black");
			 * assertProducesCorrectImage(device, "Gray", 16, "Solid black");
			 * 
			 * assertProducesCorrectImage(device, "Color", 1, "Solid white");
			 * assertProducesCorrectImage(device, "Color", 8, "Solid white");
			 * assertProducesCorrectImage(device, "Color", 16, "Solid white");
			 * assertProducesCorrectImage(device, "Color", 1, "Solid black");
			 * assertProducesCorrectImage(device, "Color", 8, "Solid black");
			 * assertProducesCorrectImage(device, "Color", 16, "Solid black");
			 * 
			 * assertProducesCorrectImage(device, "Gray", 1, "Color pattern");
			 * assertProducesCorrectImage(device, "Color", 1, "Color pattern");
			 * 
			 * assertProducesCorrectImage(device, "Gray", 8, "Color pattern");
			 * assertProducesCorrectImage(device, "Color", 8, "Color pattern");
			 */

			assertProducesCorrectImage(device, "Gray", 1, "Grid");
			assertProducesCorrectImage(device, "Color", 1, "Color pattern");

			// assertProducesCorrectImage(device, "Color", 8, "Color pattern");
			// assertProducesCorrectImage(device, "Color", 16, "Color pattern");
		} finally {
			Closeables.closeQuietly(device);
		}
	}

	public void readsAndSetsStringsCorrectly() throws Exception {
		SaneDevice device = session.getDevice("test");

		try {
			device.open();
			assertTrue(ImmutableSet.of("Color", "Gray").contains(device.getOption("mode").getStringValue(Charsets.US_ASCII)));
			assertEquals("Gray", device.getOption("mode").setStringValue("Gray"));
			assertEquals("Gray", device.getOption("mode").getStringValue(Charsets.US_ASCII));
			assertEquals("Default", device.getOption("read-return-value").getStringValue(Charsets.US_ASCII));
		} finally {
			Closeables.closeQuietly(device);
		}
	}

	public void readsFixedPrecisionCorrectly() throws Exception {
		SaneDevice device = session.getDevice("test");

		try {
			device.open();

			// this option gets rounded to the nearest whole number by the
			// backend
			assertEquals(123, device.getOption("br-x").setFixedValue(123.456), 0.0001);
			assertEquals(123, device.getOption("br-x").getFixedValue(), 0.0001);
		} finally {
			Closeables.closeQuietly(device);
		}
	}

	public void readsBooleanOptionsCorrectly() throws Exception {
		SaneDevice device = session.getDevice("test");

		try {
			device.open();

			SaneOption option = device.getOption("hand-scanner");
			assertTrue(option.setBooleanValue(true));
			assertTrue(option.getBooleanValue());
			assertFalse(option.setBooleanValue(false));
			assertFalse(option.getBooleanValue());
		} finally {
			Closeables.closeQuietly(device);
		}
	}

	public void readsStringListConstraintsCorrectly() throws Exception {
		SaneDevice device = session.getDevice("test");

		try {
			device.open();

			SaneOption option = device.getOption("string-constraint-string-list");
			assertNotNull(option);
			assertEquals(OptionValueConstraintType.STRING_LIST_CONSTRAINT, option.getConstraintType());
			assertEquals(ImmutableList.of("First entry", "Second entry", "This is the very long third entry. Maybe the frontend has an idea how to display it"), option.getStringConstraints());
		} finally {
			Closeables.closeQuietly(device);
		}
	}

	public void readIntegerValueListConstraintsCorrectly() throws Exception {
		SaneDevice device = session.getDevice("test");

		try {
			device.open();

			SaneOption option = device.getOption("int-constraint-word-list");
			assertNotNull(option);
			assertEquals(OptionValueConstraintType.VALUE_LIST_CONSTRAINT, option.getConstraintType());
			assertEquals(ImmutableList.of(-42, -8, 0, 17, 42, 256, 65536, 16777216, 1073741824), option.getIntegerValueListConstraint());
		} finally {
			Closeables.closeQuietly(device);
		}
	}

	public void readFixedValueListConstraintsCorrectly() throws Exception {
		SaneDevice device = session.getDevice("test");

		try {
			device.open();

			SaneOption option = device.getOption("fixed-constraint-word-list");
			assertNotNull(option);
			assertEquals(OptionValueConstraintType.VALUE_LIST_CONSTRAINT, option.getConstraintType());
			List<Double> expected = ImmutableList.of(-32.7d, 12.1d, 42d, 129.5d);
			List<Double> actual = option.getFixedValueListConstraint();
			assertEquals(expected.size(), actual.size());

			for (int i = 0; i < expected.size(); i++) {
				assertEquals(expected.get(i), actual.get(i), 0.00001);
			}

		} finally {
			Closeables.closeQuietly(device);
		}
	}

	public void readIntegerConstraintRangeCorrectly() throws Exception {
		SaneDevice device = session.getDevice("test");

		try {
			device.open();

			SaneOption option = device.getOption("int-constraint-range");
			assertNotNull(option);
			assertEquals(OptionValueConstraintType.RANGE_CONSTRAINT, option.getConstraintType());
			assertEquals(4, option.getRangeConstraints().getMinimumInteger());
			assertEquals(192, option.getRangeConstraints().getMaximumInteger());
			assertEquals(2, option.getRangeConstraints().getQuantumInteger());
		} finally {
			Closeables.closeQuietly(device);
		}
	}

	public void readFixedConstraintRangeCorrectly() throws Exception {
		SaneDevice device = session.getDevice("test");

		try {
			device.open();

			SaneOption option = device.getOption("fixed-constraint-range");
			assertNotNull(option);
			assertEquals(OptionValueConstraintType.RANGE_CONSTRAINT, option.getConstraintType());
			assertEquals(-42.17, option.getRangeConstraints().getMinimumFixed(), 0.00001);
			assertEquals(32767.9999, option.getRangeConstraints().getMaximumFixed(), 0.00001);
			assertEquals(2.0, option.getRangeConstraints().getQuantumFixed(), 0.00001);
		} finally {
			Closeables.closeQuietly(device);
		}
	}

	public void arrayOption() throws Exception {
		SaneDevice device = session.getDevice("pixma");

		try {
			device.open();

			SaneOption option = device.getOption("gamma-table");
			assertNotNull(option);
			// assertFalse(option.isConstrained());
			assertEquals(OptionValueType.INT, option.getType());
			List<Integer> values = Lists.newArrayList();

			for (int i = 0; i < option.getValueCount(); i++) {
				values.add(i % 256);
			}

			assertEquals(values, option.setIntegerValue(values));
			assertEquals(values, option.getIntegerArrayValue());
		} finally {
			Closeables.closeQuietly(device);
		}
	}

	public void pixmaConstraints() throws Exception {
		SaneDevice device = session.getDevice("pixma");

		try {
			device.open();

			SaneOption option = device.getOption("tl-x");
			assertNotNull(option);
			assertEquals(OptionValueConstraintType.RANGE_CONSTRAINT, option.getConstraintType());
			assertEquals(OptionValueType.FIXED, option.getType());
			RangeConstraint constraint = option.getRangeConstraints();

			System.out.println(constraint.getMinimumFixed());
			System.out.println(constraint.getMaximumFixed());
			System.out.println(option.getUnits());
			System.out.println(option.setFixedValue(-4));
			System.out.println(option.setFixedValue(97.5));
		} finally {
			Closeables.closeQuietly(device);
		}
	}

	public void multipleListDevicesCalls() throws Exception {
		session.listDevices();
		session.listDevices();
	}

	public void multipleGetDeviceCalls() throws Exception {
		session.getDevice("test");
		session.getDevice("test");
	}

	public void multipleOpenDeviceCalls() throws Exception {
		{
			SaneDevice device = session.getDevice("test");
			openAndCloseDevice(device);
		}

		{
			SaneDevice device = session.getDevice("test");
			openAndCloseDevice(device);
		}
	}

	public void canSetButtonOption() throws Exception {
		SaneDevice device = session.getDevice("pixma");
		try {
			device.open();
			device.getOption("button-update").setButtonValue();
			assertEquals("Gray", device.getOption("mode").setStringValue("Gray"));
			assertEquals("Gray", device.getOption("mode").getStringValue());
		} finally {
			Closeables.closeQuietly(device);
		}
	}

	private void openAndCloseDevice(SaneDevice device) throws Exception {
		try {
			device.open();
			device.listOptions();
		} finally {
			Closeables.closeQuietly(device);
		}
	}

	private void assertProducesCorrectImage(SaneDevice device, String mode, int sampleDepth, String testPicture) throws IOException, SaneException {
		BufferedImage actualImage = acquireImage(device, mode, sampleDepth, testPicture);

		writeImage(mode, sampleDepth, testPicture, actualImage);

		if (testPicture.startsWith("Solid")) {
			assertImageSolidColor(testPicture.endsWith("black") ? Color.black : Color.white, actualImage);
		} else {
			// compare with sample images
		}
	}

	private void writeImage(String mode, int sampleDepth, String testPicture, BufferedImage actualImage) throws IOException {
		File file = File.createTempFile(String.format("image-%s-%d-%s", mode, sampleDepth, testPicture.replace(' ', '_')), ".png");
		ImageIO.write(actualImage, "png", file);
		System.out.println("Successfully wrote " + file);
	}

	private void assertImageSolidColor(Color color, BufferedImage image) {
		for (int x = 0; x < image.getWidth(); x++) {
			for (int y = 0; y < image.getHeight(); y++) {
				assertEquals(color.getRGB(), image.getRGB(x, y));
			}
		}
	}

	private BufferedImage acquireImage(SaneDevice device, String mode, int sampleDepth, String testPicture) throws IOException, SaneException {
		device.getOption("mode").setStringValue(mode);
		device.getOption("depth").setIntegerValue(sampleDepth);
		device.getOption("test-picture").setStringValue(testPicture);

		return device.acquireImage();
	}

	@SuppressWarnings("unused")
	private void assertImagesEqual(BufferedImage expected, BufferedImage actual) {
		assertEquals("image widths differ", expected.getWidth(), actual.getWidth());
		assertEquals("image heights differ", expected.getHeight(), actual.getHeight());

		Raster expectedRaster = expected.getRaster();
		Raster actualRaster = actual.getRaster();

		for (int x = 0; x < expected.getWidth(); x++) {
			for (int y = 0; y < expected.getHeight(); y++) {
				int[] expectedPixels = expectedRaster.getPixel(x, y, (int[]) null);
				int[] actualPixels = actualRaster.getPixel(x, y, (int[]) null);

				// assert that all the samples are the same for the given pixel
				Assert.assertArrayEquals(expectedPixels, actualPixels);
			}
		}
	}

}
