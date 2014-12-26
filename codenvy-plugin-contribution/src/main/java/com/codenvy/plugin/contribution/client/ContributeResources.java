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

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

import org.vectomatic.dom.svg.ui.SVGResource;

/**
 * Contributor plugin resources.
 */
public interface ContributeResources extends ClientBundle {
    interface ContributeCss extends CssResource {
        String inputError();
    }

    @Source({"Contribute.css", "com/codenvy/ide/api/ui/style.css"})
    ContributeCss contributeCss();

    @Source("contribute.svg")
    SVGResource contributeButton();
}
