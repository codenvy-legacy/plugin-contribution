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
package com.codenvy.ide.contributor.client.vcs;

/**
 * Representation of a VCS branch.<br>
 * Mirrors the plugin-git interface until we define an abstraction on VCSes and branches.
 */
public class Branch {
    private String name;
    private boolean active;
    private String displayName;
    private boolean remote;

    /** @return full name of branch, e.g. 'refs/heads/master' */
    public String getName() {
        return this.name;
    }

    /** @return <code>true</code> if branch is checked out and false otherwise */
    public boolean isActive() {
        return this.active;
    }

    /** @return display name of branch, e.g. 'refs/heads/master' -> 'master' */
    public String getDisplayName() {
        return this.displayName;
    }

    /** @return <code>true</code> if branch is a remote branch */
    public boolean isRemote() {
        return this.remote;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setActive(boolean isActive) {
        this.active = isActive;
    }

    public void setRemote(boolean isRemote) {
        this.remote = isRemote;
    }

    public Branch withName(String name) {
        this.name = name;
        return this;
    }

    public Branch withDisplayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

    public Branch withActive(boolean isActive) {
        this.active = isActive;
        return this;
    }

    public Branch withRemote(boolean isRemote) {
        this.remote = isRemote;
        return this;
    }
}
