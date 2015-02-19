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
package com.codenvy.plugin.contribution.shared.client;

import com.google.gwt.i18n.client.Messages;

/**
 * Internationalizable messages for the contributor plugin.
 */
public interface AuthentifierMessages extends Messages {

    /*
     * Notification message prefix.
     */
    @Key("notification.message.prefix")
    String notificationMessagePrefix(String notificationMessage);

    /*
     * Authorize Codenvy on VCS Host step
     */
    @Key("error.cannot.access.vcs.host")
    String errorCannotAccessVCSHost();
}