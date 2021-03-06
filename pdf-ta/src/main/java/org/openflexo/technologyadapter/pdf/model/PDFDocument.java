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

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.openflexo.foundation.doc.FlexoDocument;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.resource.CannotRenameException;
import org.openflexo.foundation.resource.ResourceData;
import org.openflexo.foundation.task.Progress;
import org.openflexo.foundation.technologyadapter.TechnologyObject;
import org.openflexo.localization.LocalizedDelegate;
import org.openflexo.pamela.annotations.Adder;
import org.openflexo.pamela.annotations.CloningStrategy;
import org.openflexo.pamela.annotations.Embedded;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.PropertyIdentifier;
import org.openflexo.pamela.annotations.Remover;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.annotations.XMLAttribute;
import org.openflexo.pamela.annotations.XMLElement;
import org.openflexo.pamela.annotations.CloningStrategy.StrategyType;
import org.openflexo.pamela.annotations.Getter.Cardinality;
import org.openflexo.technologyadapter.pdf.PDFTechnologyAdapter;
import org.openflexo.technologyadapter.pdf.rm.PDFDocumentResource;

/**
 * Implementation of {@link FlexoDocument} for {@link PDFTechnologyAdapter}
 * 
 * @author sylvain
 *
 */
@ModelEntity
@ImplementationClass(PDFDocument.PDFDocumentImpl.class)
@XMLElement
public interface PDFDocument extends TechnologyObject<PDFTechnologyAdapter>, ResourceData<PDFDocument> {

	public static final float DEFAULT_GENERAL_RENDERING_DPI = 300f; // 300 pixels per inch
	public static final float DEFAULT_IMAGE_RENDERING_DPI = 100f; // 100 pixels per inch

	@PropertyIdentifier(type = String.class)
	public static final String NAME_KEY = "name";
	@PropertyIdentifier(type = PDDocument.class)
	public static final String PD_DOCUMENT_KEY = "PDDocument";
	@PropertyIdentifier(type = PDFDocumentPage.class, cardinality = Cardinality.LIST)
	public static final String PAGES_KEY = "pages";

	@Getter(value = NAME_KEY)
	@XMLAttribute
	public String getName();

	@Setter(NAME_KEY)
	public void setName(String name);

	public String getURI();

	@Getter(value = PD_DOCUMENT_KEY, ignoreType = true)
	public PDDocument getPDDocument();

	@Setter(PD_DOCUMENT_KEY)
	public void setPDDocument(PDDocument document);

	@Getter(value = PAGES_KEY, cardinality = Cardinality.LIST, inverse = PDFDocumentPage.PDF_DOCUMENT_KEY)
	@XMLElement(primary = true)
	@CloningStrategy(StrategyType.CLONE)
	@Embedded
	public List<PDFDocumentPage> getPages();

	@Setter(PAGES_KEY)
	public void setPages(List<PDFDocumentPage> somePages);

	@Adder(PAGES_KEY)
	public void addToPages(PDFDocumentPage aPage);

	@Remover(PAGES_KEY)
	public void removeFromPages(PDFDocumentPage aPage);

	/**
	 * This is the starting point for updating {@link PDFDocument} with the document provided from pdfbox library<br>
	 * Take care that the supplied pdDocument is the object we should update with, but that {@link #getPDDocument()} is unsafe in this
	 * context, because return former value
	 */
	public void updateFromPDDocument(PDDocument pdDocument, PDFFactory factory);

	public PDFFactory getFactory();

	public static abstract class PDFDocumentImpl extends FlexoObjectImpl implements PDFDocument {

		private static final java.util.logging.Logger logger = org.openflexo.logging.FlexoLogger
				.getLogger(PDFDocumentImpl.class.getPackage().getName());

		private Map<PDPage, PDFDocumentPage> pageMap = new HashMap<>();

		@Override
		public LocalizedDelegate getLocales() {
			return super.getLocales();
		}

		@Override
		public void setPDDocument(PDDocument document) {

			if ((document == null && getPDDocument() != null) || (document != null && !document.equals(getPDDocument()))) {
				if (document != null && getResource() != null) {
					updateFromPDDocument(document, ((PDFDocumentResource) getResource()).getFactory());
				}
			}
		}

		@Override
		public boolean delete(Object... context) {

			try {
				getPDDocument().close();
			} catch (IOException e) {
				e.printStackTrace();
			}

			performSuperDelete(context);

			return true;
		}

		/**
		 * This is the starting point for updating {@link PDFDocument} with the document provided from pdfbox library<br>
		 * Take care that the supplied pdDocument is the object we should update with, but that {@link #getPDDocument()} is unsafe in this
		 * context, because return former value
		 */
		@Override
		public void updateFromPDDocument(PDDocument pdDocument, PDFFactory factory) {

			// System.out.println("updateFromPDDocument with " + pdDocument);

			int i = 0;
			for (PDPage page : pdDocument.getPages()) {
				i++;
				Progress.progress(getLocales().localizedForKey("processing_page") + " " + i);
				// System.out.println("************* >> HERE in PDFDocument with thread: " + Thread.currentThread());
				PDFDocumentPage pdfPage = pageMap.get(page);
				if (pdfPage == null) {
					pdfPage = factory.makeNewPDFPage(pdDocument, page);
					pageMap.put(page, pdfPage);
					addToPages(pdfPage);
				}
				else {
					pdfPage.updateFromPDPage(pdDocument, page, factory);
				}
			}

			performSuperSetter(PD_DOCUMENT_KEY, pdDocument);
		}

		@Override
		public PDFFactory getFactory() {
			if (getResource() != null) {
				return ((PDFDocumentResource) getResource()).getFactory();
			}
			return null;
		}

		@Override
		public PDFTechnologyAdapter getTechnologyAdapter() {
			if (getResource() != null) {
				return ((PDFDocumentResource) getResource()).getTechnologyAdapter();
			}
			return null;
		}

		/**
		 * Return the URI of the {@link VirtualModel}<br>
		 * The convention for URI are following: <viewpoint_uri>/<virtual_model_name >#<flexo_concept_name>.<edition_scheme_name> <br>
		 * eg<br>
		 * http://www.mydomain.org/MyViewPoint/MyVirtualModel#MyFlexoConcept. MyEditionScheme
		 * 
		 * @return String representing unique URI of this object
		 */
		@Override
		public String getURI() {
			if (getResource() != null) {
				return getResource().getURI();
			}
			return null;
		}

		@Override
		public String getName() {
			if (getResource() != null) {
				return getResource().getName();
			}
			return (String) performSuperGetter(NAME_KEY);
		}

		@Override
		public void setName(String name) {

			// WARN prevent renaming Resource to null when rename ResourceData

			if (name != null && requireChange(getName(), name)) {
				String oldValue = getName();
				if (getResource() != null) {
					try {
						getResource().setName(name);
						getPropertyChangeSupport().firePropertyChange("name", oldValue, name);
					} catch (CannotRenameException e) {
						e.printStackTrace();
					}
				}
				else {
					performSuperSetter(NAME_KEY, name);
				}
			}
		}

		@Override
		public void finalize() {
			logger.warning("PDFDocument has been garbage collected");
		}

	}

}
