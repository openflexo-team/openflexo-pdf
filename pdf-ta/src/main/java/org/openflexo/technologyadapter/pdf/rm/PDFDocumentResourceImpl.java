/*
 * (c) Copyright 2015 Openflexo
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.resource.FileIODelegate;
import org.openflexo.foundation.resource.FileWritingLock;
import org.openflexo.foundation.resource.PamelaResourceImpl;
import org.openflexo.foundation.resource.ResourceLoadingCancelledException;
import org.openflexo.foundation.resource.SaveResourceException;
import org.openflexo.foundation.task.Progress;
import org.openflexo.technologyadapter.pdf.model.PDFDocument;
import org.openflexo.technologyadapter.pdf.model.PDFFactory;
import org.openflexo.toolbox.FileUtils;

public abstract class PDFDocumentResourceImpl extends PamelaResourceImpl<PDFDocument, PDFFactory> implements PDFDocumentResource {
	private static final Logger logger = Logger.getLogger(PDFDocumentResourceImpl.class.getPackage().getName());

	@Override
	protected PDFDocument performLoad() throws IOException, Exception {

		if (getFlexoIOStreamDelegate() == null) {
			throw new FlexoException("Cannot load PDF document with this IO/delegate: " + getIODelegate());
		}

		Progress.progress(getLocales().localizedForKey("loading") + " " + getIODelegate().getSerializationArtefact());
		try (PDDocument document = PDDocument.load(getInputStream())) {
			PDFDocument returned = getFactory().makeNewPDFDocument(document);
			return returned;
		}
	}

	@Override
	protected void _saveResourceData(boolean clearIsModified) throws SaveResourceException {

		if (getFlexoIOStreamDelegate() == null) {
			throw new SaveResourceException(getIODelegate());
		}

		FileWritingLock lock = getFlexoIOStreamDelegate().willWriteOnDisk();

		if (logger.isLoggable(Level.INFO)) {
			logger.info("Saving resource " + this + " : " + getIODelegate().getSerializationArtefact() + " version=" + getModelVersion());
		}

		if (getFlexoIOStreamDelegate() instanceof FileIODelegate) {
			File temporaryFile = null;
			try {
				File fileToSave = ((FileIODelegate) getFlexoIOStreamDelegate()).getFile();
				// Make local copy
				makeLocalCopy(fileToSave);
				// Using temporary file
				temporaryFile = ((FileIODelegate) getIODelegate()).createTemporaryArtefact(".pdf");
				if (logger.isLoggable(Level.FINE)) {
					logger.finer("Creating temp file " + temporaryFile.getAbsolutePath());
				}
				try (FileOutputStream fos = new FileOutputStream(temporaryFile)) {
					write(fos);
				}
				System.out.println("Renamed " + temporaryFile + " to " + fileToSave);
				FileUtils.rename(temporaryFile, fileToSave);
			} catch (IOException e) {
				e.printStackTrace();
				if (temporaryFile != null) {
					temporaryFile.delete();
				}
				if (logger.isLoggable(Level.WARNING)) {
					logger.warning("Failed to save resource " + this + " with model version " + getModelVersion());
				}
				getFlexoIOStreamDelegate().hasWrittenOnDisk(lock);
				throw new SaveResourceException(getIODelegate(), e);
			}
		}
		else {
			try {
				write(getOutputStream());
			} catch (IOException e) {
				e.printStackTrace();
				if (logger.isLoggable(Level.WARNING)) {
					logger.warning("Failed to save resource " + this + " with model version " + getModelVersion());
				}
				getFlexoIOStreamDelegate().hasWrittenOnDisk(lock);
				throw new SaveResourceException(getIODelegate(), e);
			}
		}

		getFlexoIOStreamDelegate().hasWrittenOnDisk(lock);
		if (clearIsModified) {
			notifyResourceStatusChanged();
		}
	}

	private void write(OutputStream out) throws SaveResourceException, IOException {
		try {
			System.out.println("Writing pdf file in : " + getIODelegate().getSerializationArtefact());
			getDocument().getPDDocument().save(out);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new SaveResourceException(getIODelegate());
		} finally {
			out.close();
		}
		System.out.println("Wrote : " + getIODelegate().getSerializationArtefact());
	}

	@Override
	public PDFDocument getDocument() {
		try {
			return getResourceData();
		} catch (ResourceLoadingCancelledException e) {
			e.printStackTrace();
			return null;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		} catch (FlexoException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public Class<PDFDocument> getResourceDataClass() {
		return PDFDocument.class;
	}

}
