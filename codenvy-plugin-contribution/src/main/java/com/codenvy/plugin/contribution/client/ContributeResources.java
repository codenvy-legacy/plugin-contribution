/*******************************************************************************
 * Copyright (c) 2014 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package com.codenvy.plugin.contribution.client;

import org.vectomatic.dom.svg.ui.SVGResource;

import com.google.gwt.resources.client.ClientBundle;

public interface ContributeResources extends ClientBundle {

    @Source("contribute.svg")
    SVGResource contributeButton();
}
