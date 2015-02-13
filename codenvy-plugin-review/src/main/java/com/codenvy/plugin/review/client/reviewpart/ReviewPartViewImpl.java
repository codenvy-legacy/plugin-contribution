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
package com.codenvy.plugin.review.client.reviewpart;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import com.codenvy.ide.api.parts.PartStackUIResources;
import com.codenvy.ide.api.parts.base.BaseView;
import com.codenvy.plugin.review.client.ReviewMessages;
import com.codenvy.plugin.review.client.ReviewResources;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.uibinder.client.UiField;

/**
 * Implementation of {@link com.codenvy.plugin.contribution.client.ReviewPartView.contribute.ContributePartView}.
 */
public class ReviewPartViewImpl extends BaseView<ReviewPartView.ActionDelegate> implements ReviewPartView {

    /** The uUI binder for this component. */
    private static final ReviewPartViewUiBinder UI_BINDER = GWT.create(ReviewPartViewUiBinder.class);

    /** The resources for the view. */
    @UiField(provided = true)
    ReviewResources resources;

    /** The i18n messages. */
    @UiField(provided = true)
    ReviewMessages messages;

    @Inject
    public ReviewPartViewImpl(@Nonnull final PartStackUIResources partStackUIResources,
                              @Nonnull final ReviewMessages messages,
                              @Nonnull final ReviewResources resources) {
        super(partStackUIResources);

        this.messages = messages;
        this.resources = resources;

        this.container.add(UI_BINDER.createAndBindUi(this));
    }
}
