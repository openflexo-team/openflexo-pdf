package org.openflexo.technologyadapter.pdf.test;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.pdfbox.contentstream.PDContentStream;
import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceStream;
import org.apache.pdfbox.text.PDFTextStripperByArea;
import org.apache.pdfbox.text.TextPosition;
import org.junit.Test;
import org.openflexo.rm.FileResourceImpl;
import org.openflexo.rm.ResourceLocator;

public class TestShowPDF {

	private PDDocument getDocument(String fileName) throws IOException {
		final File resource = ((FileResourceImpl) ResourceLocator.locateResource("TestResourceCenter/PDF/" + fileName)).getFile();
		return PDDocument.load(resource);
	}

	private void loadAndDisplayDocument(String title, PDDocument doc) throws IOException {
		for (PDPage page : doc.getPages()) {
			new PDFFrame(title, doc, page);
		}
		doc.close();

	}

	@Test
	public void showFile1() throws IOException {
		loadAndDisplayDocument("EH200052_MAXITAB Regular_5kg.pdf", getDocument("EH200052_MAXITAB Regular_5kg.pdf"));
	}

	@Test
	public void showFile2() throws IOException {
		loadAndDisplayDocument("EH201895_MAXITAB Regular_5kg.pdf", getDocument("EH201895_MAXITAB Regular_5kg.pdf"));
	}

	@Test
	public void showFile3() throws IOException {
		loadAndDisplayDocument("EH201976_MAXITAB Regular_1-2kg.pdf", getDocument("EH201976_MAXITAB Regular_1-2kg.pdf"));
	}

	@Test
	public void showFile4() throws IOException {
		loadAndDisplayDocument("EH202050-Action5-200g-5kg.pdf", getDocument("EH202050-Action5-200g-5kg.pdf"));
	}

	@Test
	public void showFile5() throws IOException {
		loadAndDisplayDocument("EH202051-Action5-200g-5kg.pdf", getDocument("EH202051-Action5-200g-5kg.pdf"));
	}

	@Test
	public void waitUser() throws IOException {
		while (true) {
			System.out.println("hop");
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	public class PDFFrame extends JFrame {

		public PDFFrame(String name, PDDocument document, PDPage page) {
			super();
			try {
				getContentPane().add(new PagePanel(document, page));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			setTitle(name);
			validate();
			pack();
			setVisible(true);
		}

		public class PagePanel extends JPanel {

			private final List<PDFormXObject> formObjects;
			private final List<TextBox> textBoxes;

			public PagePanel(PDDocument document, PDPage page) throws IOException {
				super();

				PDResources resources = page.getResources();
				System.out.println("xobjects =" + resources.getXObjectNames());

				formObjects = new ArrayList<>();
				textBoxes = new ArrayList<>();

				for (COSName n : resources.getXObjectNames()) {
					// System.out.println("for " + n);
					PDXObject obj;
					obj = resources.getXObject(n);
					// System.out.println("obj=" + obj);
					if (obj instanceof PDImageXObject) {
						PDImageXObject image = (PDImageXObject) obj;
						add(new JLabel(new ImageIcon(image.getImage())));
					}
					else if (obj instanceof PDFormXObject) {
						PDFormXObject form = (PDFormXObject) obj;
						formObjects.add(form);
						// System.out.println("form=" + form);
						PDResources formResources = form.getResources();
						// System.out.println("xobjects =" + formResources.getXObjectNames());
						// System.out.println("box=" + form.getBBox());
						for (COSName n2 : formResources.getXObjectNames()) {
							PDXObject obj2 = formResources.getXObject(n2);
							// System.out.println("n2=" + n2 + " obj2=" + obj2);
						}
					}
				}

				int width = 612;
				int height = 792;

				int hX = 320, tX = 340, cX = 100;
				int hY = 0, tY = 580, cY = 200;
				int hW = width - hX, tW = width - tX, cW = 100;
				int hH = 80, tH = height - tY, cH = 60;

				Rectangle header = new Rectangle();
				// header.setBounds(hX, hY, hW, hH);
				header.setBounds(219, 313, 180, 30);
				Rectangle totals = new Rectangle();
				totals.setBounds(tX, tY, tW, tH);
				Rectangle customer = new Rectangle();
				customer.setBounds(cX, cY, cW, cH);

				PDFTextStripperByArea stripper = new PDFTextStripperByArea() {

					private StringBuffer currentString;
					private Rectangle box;
					private float fontSize;
					private float dir;

					private void reset() {
						currentString = null;
						box = null;
					}

					@Override
					protected void processTextPosition(TextPosition text) {
						// System.out.println("* " + text + " on " + text.getX() + " " + text.getY());
						super.processTextPosition(text);
						if (currentString == null) {
							currentString = new StringBuffer();
						}
						currentString.append(text.toString());
						if (box == null) {
							box = new Rectangle((int) text.getX(), (int) text.getY(), (int) text.getWidth(), (int) text.getHeight());
						}
						else {
							box = box.union(
									new Rectangle((int) text.getX(), (int) text.getY(), (int) text.getWidth(), (int) text.getHeight()));
						}
						fontSize = text.getFontSize();
						dir = text.getDir();
						textBoxes.add(new TextBox(currentString.toString(), box, text.getDir()));
					}

					@Override
					protected void processOperator(Operator operator, List<COSBase> operands) throws IOException {
						if (currentString != null && box != null) {
							System.out.println("> [" + currentString + "] box=" + box + " font=" + fontSize + " dir=" + dir);
						}
						reset();
						super.processOperator(operator, operands);
					}

					@Override
					protected void processAnnotation(PDAnnotation annotation, PDAppearanceStream appearance) throws IOException {
						System.out.println("processAnnotation " + annotation + " " + appearance);
						super.processAnnotation(annotation, appearance);
					}

					@Override
					protected void processChildStream(PDContentStream contentStream, PDPage page) throws IOException {
						System.out.println("processChildStream " + contentStream + " " + page);
						super.processChildStream(contentStream, page);
					}

				};

				stripper.addRegion("header", header);
				stripper.addRegion("totals", totals);
				stripper.addRegion("customer", customer);
				stripper.setSortByPosition(true);

				int j = 0;
				// for (PDPage page : document.getPages()) {
				stripper.extractRegions(page);
				List<String> regions = stripper.getRegions();
				for (String region : regions) {
					String text = stripper.getTextForRegion(region);
					System.out.println("Region: " + region + " on Page " + j);
					System.out.println("\tText: \n" + text);
				}
				j++;
				// }
			}

			public class TextBox {
				private final String text;
				private final Rectangle box;
				private final float dir;

				public TextBox(String text, Rectangle box, float dir) {
					super();
					this.text = text;
					this.box = box;
					this.dir = dir;
				}

			}

			@Override
			public void paint(Graphics g) {
				// TODO Auto-generated method stub
				super.paint(g);

				for (PDFormXObject o : formObjects) {
					g.setColor(Color.RED);
					g.drawRect((int) o.getBBox().getLowerLeftX(), (int) o.getBBox().getUpperRightY(), (int) o.getBBox().getWidth(),
							(int) o.getBBox().getHeight());
				}

				for (TextBox tb : textBoxes) {
					System.out.println("box: " + tb.box);
					g.setColor(Color.WHITE);
					g.setFont(g.getFont().deriveFont((float) (tb.box.height * 1.5)));
					// g.drawRect(tb.box.x, tb.box.y, tb.box.width, tb.box.height);
					if (tb.dir == 0) {
						g.drawString(tb.text, tb.box.x, tb.box.y + tb.box.height);
					}
				}

			}
		}
	}
}
