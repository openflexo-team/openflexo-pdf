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

package org.openflexo.technologyadapter.pdf.rm;

import org.openflexo.foundation.resource.PamelaResource;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterResource;
import org.openflexo.foundation.technologyadapter.TechnologyContextManager;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.technologyadapter.pdf.PDFTechnologyAdapter;
import org.openflexo.technologyadapter.pdf.model.PDFDocument;
import org.openflexo.technologyadapter.pdf.model.PDFFactory;

@ModelEntity
@ImplementationClass(PDFDocumentResourceImpl.class)
public interface PDFDocumentResource
		extends TechnologyAdapterResource<PDFDocument, PDFTechnologyAdapter>, PamelaResource<PDFDocument, PDFFactory> {
	public static final String TECHNOLOGY_CONTEXT_MANAGER = "technologyContextManager";

	public PDFDocument getDocument();

	@Getter(value = TECHNOLOGY_CONTEXT_MANAGER, ignoreType = true)
	public abstract TechnologyContextManager<PDFTechnologyAdapter> getTechnologyContextManager();

	@Setter(TECHNOLOGY_CONTEXT_MANAGER)
	public abstract void setTechnologyContextManager(TechnologyContextManager<PDFTechnologyAdapter> pdfTechnologyContextManager);

}
