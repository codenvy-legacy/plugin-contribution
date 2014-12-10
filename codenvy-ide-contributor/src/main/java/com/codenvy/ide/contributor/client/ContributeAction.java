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
package com.codenvy.ide.contributor.client;

import com.codenvy.ide.api.action.ActionEvent;
import com.codenvy.ide.api.action.ProjectAction;
import com.google.inject.Inject;

public class ContributeAction extends ProjectAction {
    
    @Inject
    public ContributeAction(ContributeResources contributeResources, ContributorLocalizationConstant localConstant) {
        super(localConstant.contributorButtonName(), localConstant.contributorButtonDescription(), contributeResources.contributeButton());
    }
    
    @Override
    protected void updateProjectAction(ActionEvent e) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // TODO Auto-generated method stub
        
    }
}
