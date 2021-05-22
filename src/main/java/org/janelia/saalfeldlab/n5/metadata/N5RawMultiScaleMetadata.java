/**
 * Copyright (c) 2018--2020, Saalfeld lab
 * All rights reserved.
 * <p>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * <p>
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * <p>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package org.janelia.saalfeldlab.n5.metadata;

import net.imglib2.realtransform.AffineGet;
import org.janelia.saalfeldlab.n5.N5DatasetDiscoverer;
import org.janelia.saalfeldlab.n5.N5Reader;
import org.janelia.saalfeldlab.n5.N5TreeNode;

import java.io.IOException;
import java.util.Optional;

public class N5RawMultiScaleMetadata extends MultiscaleMetadata<N5SingleScaleMetadata> implements N5Metadata, PhysicalMetadata {

  public N5RawMultiScaleMetadata( String basePath, N5SingleScaleMetadata[] childrenMetadata) {

	super(basePath, childrenMetadata);
  }

  protected N5RawMultiScaleMetadata(String basePath) {

	super();
  }

  /**
   * Called by the {@link N5DatasetDiscoverer}
   * while discovering the N5 tree and filling the metadata for datasets or groups.
   *
   * @param node the node
   * @return the metadata
   */
  public static Optional<N5RawMultiScaleMetadata> parseMetadataGroup(final N5Reader reader, final N5TreeNode node) {

	if (node.getMetadata() instanceof N5DatasetMetadata)
	  return Optional.empty(); // we're a dataset, so not a multiscale group

	/* check by attribute */
	try {
	  boolean isMultiscale = Optional.ofNullable(reader.getAttribute(node.getPath(), MULTI_SCALE_KEY, Boolean.class)).orElse(false);
	  if (isMultiscale) {
		return Optional.of(new N5RawMultiScaleMetadata(node.getPath()));
	  }
	} catch (IOException ignore) {
	}

	/* We'll short-circuit here if any of the children don't conform to the scaleLevelPredicate */
	for (final N5TreeNode childNode : node.childrenList()) {
	  boolean isMultiScale = scaleLevelPredicate.test(childNode.getNodeName());
	  if (!isMultiScale) {
		return Optional.empty();
	  }
	}

	/* Otherwise, if we get here, nothing went wrong, assume we are multiscale*/
	return Optional.of(new N5RawMultiScaleMetadata(node.getPath()));
  }

  @Override
  public AffineGet physicalTransform() {
	// spatial transforms are specified by the individual scales
	return null;
  }

}
