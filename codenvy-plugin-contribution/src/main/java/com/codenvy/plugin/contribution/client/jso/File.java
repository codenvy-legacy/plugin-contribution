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
package com.codenvy.plugin.contribution.client.jso;

import java.util.Date;

/**
 * Interface for FileAPI File type.
 */
public interface File extends Blob {

    /**
     * Retuns the last modified date of the file. On getting, if user agents can make this information available,<br>
     * this must return a new Date[HTML] object initialized to the last modified date of the file. If the last<br>
     * modification date and time are not known, the attribute must return the current date and time as a Date object.
     * 
     * @return the last modification date
     */
    Date getLastModifiedDate();

    /**
     * Returns the name of the file; on getting, this must return the name of the file as a string. There are<br>
     * numerous file name variations on different systems; this is merely the name of the file, without path<br>
     * information.<br>
     * On getting, if user agents cannot make this information available, they must return the empty string.
     * 
     * @return the name of the file
     */
    String getName();
}
