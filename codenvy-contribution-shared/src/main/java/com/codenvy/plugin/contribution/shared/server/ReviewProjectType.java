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
package com.codenvy.plugin.contribution.shared.server;

import javax.inject.Inject;

import com.codenvy.api.project.server.type.ProjectType;
import com.codenvy.plugin.contribution.shared.shared.SharedConstants;

/**
 * Mixin type for the contribution review factories.
 */
public class ReviewProjectType extends ProjectType {

    /**
     * Display name of the project type.
     */
    private static final String TYPE_DISPLAYNAME = "Review Factory";

    @Inject
    public ReviewProjectType() {
        // this project type is not primary, is mixable and persistable
        super(SharedConstants.PROJECTTYPE_KEY_REVIEW, TYPE_DISPLAYNAME, false, true, true);

        addVariableDefinition(SharedConstants.ATTRIBUTE_REVIEW_PULLREQUEST_ID, "The id of the reviewed pull request", true);
        addVariableDefinition(SharedConstants.ATTRIBUTE_REVIEW_UPSTREAM_OWNER, "The owner of the upstream repository of the contribution", true);
        addVariableDefinition(SharedConstants.ATTRIBUTE_REVIEW_UPSTREAM_REPOSITORY, "The upstream repository of the contribution", true);
    }

}
