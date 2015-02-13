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

import com.codenvy.ide.api.mvp.View;
import com.codenvy.ide.api.parts.base.BaseActionDelegate;

/**
 * Interface for the reviewer panel view.
 */
public interface ReviewPartView extends View<ReviewPartView.ActionDelegate> {
    /**
     * Action delegate interface for the reviewer panel.
     */
    interface ActionDelegate extends BaseActionDelegate {

    }
}
