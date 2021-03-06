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

package org.openflexo.technologyadapter.pdf.rm;

import java.util.logging.Logger;

import org.apache.commons.io.FilenameUtils;
import org.openflexo.foundation.resource.FlexoResourceCenter;
import org.openflexo.foundation.resource.TechnologySpecificPamelaResourceFactory;
import org.openflexo.foundation.technologyadapter.TechnologyContextManager;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.technologyadapter.pdf.PDFTechnologyAdapter;
import org.openflexo.technologyadapter.pdf.model.PDFDocument;
import org.openflexo.technologyadapter.pdf.model.PDFFactory;

/**
 *
 * @author sylvain
 *
 */
public class PDFDocumentResourceFactory
		extends TechnologySpecificPamelaResourceFactory<PDFDocumentResource, PDFDocument, PDFTechnologyAdapter, PDFFactory> {

	private static final Logger logger = Logger.getLogger(PDFDocumentResourceFactory.class.getPackage().getName());

	public static final String PDF_FILE_EXTENSION = "pdf";

	public PDFDocumentResourceFactory() throws ModelDefinitionException {
		super(PDFDocumentResource.class);
	}

	@Override
	public PDFFactory makeModelFactory(PDFDocumentResource resource,
			TechnologyContextManager<PDFTechnologyAdapter> technologyContextManager) throws ModelDefinitionException {
		return new PDFFactory(resource, technologyContextManager.getServiceManager().getEditingContext());
	}

	@Override
	public PDFDocument makeEmptyResourceData(PDFDocumentResource resource) {
		return resource.getFactory().makeNewPDFDocument();
	}

	@Override
	public <I> boolean isValidArtefact(I serializationArtefact, FlexoResourceCenter<I> resourceCenter) {
		String name = resourceCenter.retrieveName(serializationArtefact);
		return FilenameUtils.isExtension(name, PDF_FILE_EXTENSION);
	}

	@Override
	public <I> PDFDocumentResource registerResource(PDFDocumentResource resource, FlexoResourceCenter<I> resourceCenter) {

		super.registerResource(resource, resourceCenter);

		// Register the resource in the PDFDocumentRepository of supplied resource center
		registerResourceInResourceRepository(resource,
				getTechnologyAdapter(resource.getServiceManager()).getPDFDocumentRepository(resourceCenter));

		return resource;
	}
}
