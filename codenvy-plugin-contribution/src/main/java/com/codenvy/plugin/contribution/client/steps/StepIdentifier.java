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
package com.codenvy.plugin.contribution.client.steps;

public enum StepIdentifier {
    COMMIT_WORKING_TREE,
    AUTHORIZE_CODENVY_ON_VCS_HOST,
    CREATE_FORK,
    CHECKOUT_BRANCH_TO_PUSH,
    ADD_FORK_REMOTE,
    PUSH_BRANCH_ON_FORK,
    ISSUE_PULL_REQUEST,
    GENERATE_REVIEW_FACTORY,
    ADD_REVIEW_FACTORY_LINK,
    WAIT_FORK_ON_REMOTE,
    INITIALIZE_WORKFLOW,
    DEFINE_WORK_BRANCH
}