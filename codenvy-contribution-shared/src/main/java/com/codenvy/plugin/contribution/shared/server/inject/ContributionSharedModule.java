/*******************************************************************************
 * Copyright (c) 2014-2015 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package com.codenvy.plugin.contribution.shared.server.inject;

import com.codenvy.api.project.server.type.ProjectType;
import com.codenvy.plugin.contribution.shared.server.ReviewProjectType;
import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

public class ContributionSharedModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(ReviewProjectType.class);
        Multibinder.newSetBinder(binder(), ProjectType.class).addBinding().to(ReviewProjectType.class);
    }
}
