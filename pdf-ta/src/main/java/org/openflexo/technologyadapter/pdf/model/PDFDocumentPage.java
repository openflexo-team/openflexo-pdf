/*
 * (c) Copyright 2013 Openflexo
 *
 * This file is part of OpenFlexo.
 *
 * OpenFlexo is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OpenFlexo is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenFlexo. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.openflexo.technologyadapter.pdf.model;

import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.openflexo.foundation.InnerResourceData;
import org.openflexo.foundation.doc.FlexoDocument;
import org.openflexo.foundation.task.Progress;
import org.openflexo.foundation.technologyadapter.TechnologyObject;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.PropertyIdentifier;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.annotations.XMLElement;
import org.openflexo.technologyadapter.pdf.PDFTechnologyAdapter;

/**
 * Implementation of {@link FlexoDocument} for {@link PDFTechnologyAdapter}
 * 
 * @author sylvain
 *
 */
@ModelEntity
@ImplementationClass(PDFDocumentPage.PDFPageImpl.class)
@XMLElement
public interface PDFDocumentPage extends TechnologyObject<PDFTechnologyAdapter>, InnerResourceData<PDFDocument> {

	@PropertyIdentifier(type = Integer.class)
	public static final String PAGE_NUMBER_KEY = "pageNumber";
	@PropertyIdentifier(type = PDPage.class)
	public static final String PD_PAGE_KEY = "PDPage";
	@PropertyIdentifier(type = PDFDocument.class)
	public static final String PDF_DOCUMENT_KEY = "pdfDocument";

	@Getter(value = PAGE_NUMBER_KEY, defaultValue = "0")
	public int getPageNumber();

	@Getter(value = PD_PAGE_KEY, ignoreType = true)
	public PDPage getPDPage();

	@Setter(PD_PAGE_KEY)
	public void setPDPage(PDPage document);

	@Getter(PDF_DOCUMENT_KEY)
	public PDFDocument getPDFDocument();

	@Setter(PDF_DOCUMENT_KEY)
	public void setPDFDocument(PDFDocument document);

	/**
	 * This is the starting point for updating {@link PDFDocumentPage} with the document provided from pdfbox library<br>
	 * Take care that the supplied pdDocument is the object we should update with, but that {@link #getPDPage()} is unsafe in this context,
	 * because return former value
	 */
	public void updateFromPDPage(PDDocument document, PDPage pdPage, PDFFactory factory);

	public PDFFactory getFactory();

	public Image getRenderingImage();

	public List<TextBox> getTextBoxes();

	public List<ImageBox> getImageBoxes();

	public TextBox getClosestTextBox(Rectangle aBox, Collection<TextBox> ignoredBoxes);

	public ImageBox getClosestImageBox(Rectangle aBox, Collection<ImageBox> ignoredBoxes);

	/**
	 * Return a list of all boxes matching supplied box, that are totally or partially contained in bounding box
	 * <ul>
	 * <li>areaRatio > 0 means that matching boxes must have at least one point located in bounding box</li>
	 * <li>areaRatio = 1 means that matching boxes must be totally contained in bounding box</li>
	 * 
	 * @param boundingBox
	 * @param areaRatio
	 * @return
	 */
	public List<TextBox> getMatchingBoxes(TextBox boundingBox, float areaRatio, float HTolerance, float VTolerance,
			List<TextBox> ignoredBoxes);

	public double getContainmentRatio(TextBox tb, TextBox boundingBox);

	public double getWidth();

	public double getHeight();

	// public List<TextBox> getAVirer();

	public static abstract class PDFPageImpl extends FlexoObjectImpl implements PDFDocumentPage {

		private static final java.util.logging.Logger logger = org.openflexo.logging.FlexoLogger
				.getLogger(PDFPageImpl.class.getPackage().getName());

		private Image renderingImage;
		private List<TextBox> textBoxes;
		private List<ImageBox> imageBoxes;

		@Override
		public void setPDPage(PDPage page) {

			if ((page == null && getPDPage() != null) || (page != null && !page.equals(getPDPage()))) {
				if (page != null) {
					updateFromPDPage(getPDFDocument().getPDDocument(), page, getFactory());
				}
			}
		}

		/**
		 * This is the starting point for updating {@link PDFDocumentPage} with the document provided from pdfbox library<br>
		 * Take care that the supplied pdPage is the object we should update with, but that {@link #getPDPage()} is unsafe in this context,
		 * because return former value
		 */
		@Override
		public void updateFromPDPage(PDDocument pdDocument, PDPage pdPage, PDFFactory factory) {

			System.out.println("updateFromPDPage with " + pdPage);

			Progress.progress(getLocales().localizedForKey("processing_renderer"));
			try {
				PDFRenderer pdfRenderer = new FlexoPDFRenderer(pdDocument);
				BufferedImage originalImage = null;
				float resolution = PDFDocument.DEFAULT_GENERAL_RENDERING_DPI;
				while (originalImage == null) {
					try {
						originalImage = pdfRenderer.renderImageWithDPI(0, resolution, ImageType.RGB);
					} catch (OutOfMemoryError e) {
						logger.warning("Not enough memory to process PDF page rendering for resolution: " + resolution + " dpi");
						resolution = resolution / 2;
					}
				}
				renderingImage = originalImage.getScaledInstance((int) pdPage.getMediaBox().getWidth(),
						(int) pdPage.getMediaBox().getHeight(), Image.SCALE_SMOOTH);

				Progress.progress(getLocales().localizedForKey("extract_text"));
				PDFTextBoxStripper textBoxStripper = new PDFTextBoxStripper(pdDocument, pdPage);
				textBoxes = textBoxStripper.extractTextBoxes();

			} catch (IOException e) {
				e.printStackTrace();
			}

			try {
				Progress.progress(getLocales().localizedForKey("extract_images"));
				PDFImageBoxStripper imageBoxStripper = new PDFImageBoxStripper(pdDocument, pdPage);
				imageBoxes = imageBoxStripper.extractImageBoxes();
			} catch (IOException e) {
				e.printStackTrace();
			}

			performSuperSetter(PD_PAGE_KEY, pdPage);

			System.out.println("DONE updateFromPDPage with " + pdPage);
		}

		@Override
		public List<TextBox> getTextBoxes() {
			return textBoxes;
		}

		@Override
		public List<ImageBox> getImageBoxes() {
			return imageBoxes;
		}

		@Override
		public Image getRenderingImage() {
			return renderingImage;
		}

		@Override
		public int getPageNumber() {
			PDFDocument doc = getPDFDocument();
			if (doc != null)
				if (doc.getPages() != null) {
					return getPDFDocument().getPages().indexOf(this) + 1;
				}
			return 0;
		}

		@Override
		public PDFFactory getFactory() {
			if (getPDFDocument() != null) {
				return getPDFDocument().getFactory();
			}
			return null;
		}

		@Override
		public PDFTechnologyAdapter getTechnologyAdapter() {
			if (getPDFDocument() != null) {
				return getPDFDocument().getTechnologyAdapter();
			}
			return null;
		}

		@Override
		public PDFDocument getResourceData() {
			return getPDFDocument();
		}

		/*private List<TextBox> aVirer = new ArrayList<>();
		
		@Override
		public List<TextBox> getAVirer() {
			return aVirer;
		}*/

		@Override
		public TextBox getClosestTextBox(Rectangle aBox, Collection<TextBox> ignoredBoxes) {
			// aVirer.add(textBox);
			// getPropertyChangeSupport().firePropertyChange("AVirer", null, textBox);
			TextBox returned = null;
			double minDist = Double.POSITIVE_INFINITY;
			for (TextBox tb : getTextBoxes()) {
				if (ignoredBoxes == null || !ignoredBoxes.contains(tb)) {
					double d = tb.distanceFrom(aBox);
					if (d < minDist) {
						returned = tb;
						minDist = d;
					}
				}
			}
			return returned;
		}

		@Override
		public ImageBox getClosestImageBox(Rectangle aBox, Collection<ImageBox> ignoredBoxes) {
			// aVirer.add(textBox);
			// getPropertyChangeSupport().firePropertyChange("AVirer", null, textBox);
			ImageBox returned = null;
			double minDist = Double.POSITIVE_INFINITY;
			for (ImageBox tb : getImageBoxes()) {
				if (ignoredBoxes == null || !ignoredBoxes.contains(tb)) {
					double d = tb.distanceFrom(aBox);
					if (d < minDist) {
						returned = tb;
						minDist = d;
					}
				}
			}
			return returned;
		}

		/**
		 * Return a list of all boxes matching supplied box, that are totally or partially contained in bounding box
		 * <ul>
		 * <li>areaRatio > 0 means that matching boxes must have at least one point located in bounding box</li>
		 * <li>areaRatio = 1 means that matching boxes must be totally contained in bounding box</li>
		 * 
		 * @param boundingBox
		 * @param areaRatio
		 * @return
		 */
		@Override
		public List<TextBox> getMatchingBoxes(TextBox boundingBox, float areaRatio, float HTolerance, float VTolerance,
				List<TextBox> ignoredBoxes) {
			List<TextBox> returned = new ArrayList<>();
			Rectangle bBox = new Rectangle(boundingBox.getBox());
			if (bBox.x > HTolerance && bBox.y > VTolerance) {
				bBox.setLocation((int) (bBox.x - HTolerance * bBox.width), (int) (bBox.y - VTolerance * bBox.height));
				bBox.setSize((int) (bBox.width + 2 * HTolerance * bBox.width), (int) (bBox.height + 2 * VTolerance * bBox.height));
			}
			for (TextBox tb : getTextBoxes()) {
				if (ignoredBoxes == null || !ignoredBoxes.contains(tb)) {
					Rectangle r = bBox.intersection(tb.getBox());
					if (r.getWidth() > 0 && r.getHeight() > 0) { // Box have an intersection
						// compute the ratio
						double ratio = (r.getWidth() * r.getHeight()) / (tb.getBox().getWidth() * tb.getBox().getHeight());
						if (ratio > areaRatio) {
							returned.add(tb);
						}
						// System.out.println("ratio=" + ratio);
					}
				}
			}
			return returned;
		}

		@Override
		public double getContainmentRatio(TextBox tb, TextBox boundingBox) {
			Rectangle bBox = new Rectangle(boundingBox.getBox());
			Rectangle r = bBox.intersection(tb.getBox());
			if (r.getWidth() > 0 && r.getHeight() > 0) { // Box have an intersection
				// compute the ratio
				return (r.getWidth() * r.getHeight()) / (tb.getBox().getWidth() * tb.getBox().getHeight());
			}
			return 0;
		}

		@Override
		public double getWidth() {
			// System.out.println("width is " + getPDPage().getCropBox().getWidth());
			return getPDPage().getCropBox().getWidth();
		}

		@Override
		public double getHeight() {
			// System.out.println("width is " + getPDPage().getCropBox().getHeight());
			return getPDPage().getCropBox().getHeight();
		}

		@Override
		public boolean delete(Object... context) {

			PropertyChangeSupport pcSupport = this.getPropertyChangeSupport();

			for (PropertyChangeListener cl : pcSupport.getPropertyChangeListeners()) {
				pcSupport.removePropertyChangeListener(cl);
			}

			for (ImageBox i : imageBoxes) {
				i.delete(context);
			}
			imageBoxes.clear();
			imageBoxes = null;
			for (TextBox t : textBoxes) {
				t.delete(context);
			}
			textBoxes.clear();
			textBoxes = null;
			renderingImage = null;

			this.performSuperDelete(context);

			return true;
		}

		@Override
		protected void finalize() throws Throwable {
			System.out.println("************************************** PDFPageImp has been cleaned / GC.. ********************");
			super.finalize();
		}

	}

}
