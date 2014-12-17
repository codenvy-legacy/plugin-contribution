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
package com.codenvy.plugin.contribution.client.vcshost;

import com.google.gwt.user.client.rpc.AsyncCallback;

import java.util.List;

public interface RepositoryHost {
    void getUserInfo(AsyncCallback<HostUser> callback);

    void getRepositoriesList(AsyncCallback<List<Repository>> callback);
}
