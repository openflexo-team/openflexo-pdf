/*
 * (c) Copyright 2013- Openflexo
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

package org.openflexo.technologyadapter.pdf;

import java.io.File;
import java.util.logging.Logger;

import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.fml.annotations.DeclareModelSlots;
import org.openflexo.foundation.fml.annotations.DeclareResourceFactories;
import org.openflexo.foundation.resource.FileSystemBasedResourceCenter;
import org.openflexo.foundation.resource.FlexoResourceCenter;
import org.openflexo.foundation.resource.FlexoResourceCenterService;
import org.openflexo.foundation.resource.SaveResourceException;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterBindingFactory;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterInitializationException;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.technologyadapter.pdf.rm.PDFDocumentRepository;
import org.openflexo.technologyadapter.pdf.rm.PDFDocumentResource;
import org.openflexo.technologyadapter.pdf.rm.PDFDocumentResourceFactory;

/**
 * This class defines and implements the PDF technology adapter, which allows to manage .pdf documents<br>
 * This technology adapter internally uses pdfbox library.
 * 
 * 
 * @author sylvain
 * 
 */

@DeclareModelSlots({ PDFModelSlot.class })
@DeclareResourceFactories({ PDFDocumentResourceFactory.class })
public class PDFTechnologyAdapter extends TechnologyAdapter<PDFTechnologyAdapter> {

	protected static final Logger logger = Logger.getLogger(PDFTechnologyAdapter.class.getPackage().getName());

	public PDFTechnologyAdapter() throws TechnologyAdapterInitializationException {
	}

	@Override
	public String getName() {
		return new String("PDF Technology Adapter");
	}

	@Override
	protected String getLocalizationDirectory() {
		return "FlexoLocalization/PDFTechnologyAdapter";
	}

	@Override
	public PDFTechnologyContextManager createTechnologyContextManager(FlexoResourceCenterService service) {
		return new PDFTechnologyContextManager(this, getTechnologyAdapterService().getServiceManager().getResourceCenterService());
	}

	@Override
	public PDFTechnologyContextManager getTechnologyContextManager() {
		return (PDFTechnologyContextManager) super.getTechnologyContextManager();
	}

	@Override
	public TechnologyAdapterBindingFactory getTechnologyAdapterBindingFactory() {
		return null;
	}

	@Override
	public <I> boolean isIgnorable(FlexoResourceCenter<I> resourceCenter, I contents) {
		return false;
	}

	@SuppressWarnings("unchecked")
	public <I> PDFDocumentRepository<I> getPDFDocumentRepository(FlexoResourceCenter<I> resourceCenter) {
		PDFDocumentRepository<I> returned = resourceCenter.retrieveRepository(PDFDocumentRepository.class, this);
		if (returned == null) {
			returned = PDFDocumentRepository.instanciateNewRepository(this, resourceCenter);
			resourceCenter.registerRepository(returned, PDFDocumentRepository.class, this);
		}
		return returned;
	}

	/**
	 * Create a new {@link PDFDocumentResource} using supplied configuration options<br>
	 * 
	 * @param project
	 * @param filename
	 * @param modelUri
	 * @param createEmptyDocument
	 *            a flag indicating if created resource should encodes an empty (but existing) document or if resource data should remain
	 *            empty
	 * @return
	 * @throws ModelDefinitionException
	 * @throws SaveResourceException
	 */
	public PDFDocumentResource createNewPDFDocumentResource(FlexoProject<File> project, String filename, boolean createEmptyDocument)
			throws SaveResourceException, ModelDefinitionException {

		return createNewPDFDocumentResource((FileSystemBasedResourceCenter) project.getDelegateResourceCenter(), File.separator + "PDF",
				filename, createEmptyDocument);

	}

	/**
	 * Create a new {@link PDFDocumentResource} using supplied configuration options<br>
	 * 
	 * @param resourceCenter
	 * @param relativePath
	 * @param filename
	 * @param createEmptyDocument
	 *            a flag indicating if created resource should encodes an empty (but existing) document or if resource data should remain
	 *            empty
	 * @return
	 * @throws ModelDefinitionException
	 * @throws SaveResourceException
	 */
	public PDFDocumentResource createNewPDFDocumentResource(FileSystemBasedResourceCenter resourceCenter, String relativePath,
			String filename, boolean createEmptyDocument) throws SaveResourceException, ModelDefinitionException {

		if (!relativePath.startsWith(File.separator)) {
			relativePath = File.separator + relativePath;
		}

		File pdfFile = new File(resourceCenter.getRootDirectory() + relativePath, filename);

		PDFDocumentResource pdfDocumentResource = getPDFDocumentResourceFactory().makeResource(pdfFile, resourceCenter, true);

		return pdfDocumentResource;
	}

	@Override
	public String getIdentifier() {
		return "PDF";
	}

	public PDFDocumentResourceFactory getPDFDocumentResourceFactory() {
		return getResourceFactory(PDFDocumentResourceFactory.class);
	}

}
