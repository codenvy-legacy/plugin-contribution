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
package com.codenvy.ide.contributor;

import com.codenvy.ide.api.extension.Extension;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * @author 
 */
@Singleton
@Extension(title = "Factory", version = "3.0.0")
public class ContributorExtension {
    
    @Inject
    public ContributorExtension() {
        
    }
                           
}