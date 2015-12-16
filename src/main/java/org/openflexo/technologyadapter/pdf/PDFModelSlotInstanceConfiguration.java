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

import org.openflexo.foundation.fml.rt.FreeModelSlotInstance;
import org.openflexo.foundation.fml.rt.View;
import org.openflexo.foundation.fml.rt.VirtualModelInstance;
import org.openflexo.foundation.fml.rt.action.CreateVirtualModelInstance;
import org.openflexo.foundation.technologyadapter.FreeModelSlotInstanceConfiguration;
import org.openflexo.technologyadapter.pdf.model.PDFDocument;

public class PDFModelSlotInstanceConfiguration extends FreeModelSlotInstanceConfiguration<PDFDocument, PDFModelSlot> {

	protected PDFModelSlotInstanceConfiguration(PDFModelSlot ms, CreateVirtualModelInstance action) {
		super(ms, action);
		/*setResourceUri(getAction().getFocusedObject().getProject().getURI() + "/DocX/MyDocument");
		setRelativePath("/");
		setFilename("MyDocument.docx");*/
	}

	@Override
	public boolean isValidConfiguration() {
		return super.isValidConfiguration();
	}

	@Override
	public void setOption(ModelSlotInstanceConfigurationOption option) {
		super.setOption(option);
		// TODO : add specific options here
	}

	@Override
	public String getResourceUri() {
		String returned = super.getResourceUri();
		if (returned == null && getOption() == DefaultModelSlotInstanceConfigurationOption.CreatePrivateNewResource) {
			return getAction().getFocusedObject().getProject().getURI() + getRelativePath() + getFilename();
		}
		return returned;
	}

	@Override
	public FreeModelSlotInstance<PDFDocument, PDFModelSlot> createModelSlotInstance(VirtualModelInstance vmInstance, View view) {
		// TODO Auto-generated method stub
		return super.createModelSlotInstance(vmInstance, view);
	}
}
