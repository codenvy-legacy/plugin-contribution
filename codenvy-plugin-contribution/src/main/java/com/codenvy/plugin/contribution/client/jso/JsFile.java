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

import com.google.gwt.core.client.JavaScriptObject;

/**
 * Javascript Overlay class over File objects.
 */
public class JsFile extends JavaScriptObject implements File {

    /** JSO mandated protected constructor. */
    protected JsFile() {
    }

    /**
     * Creates a new file.
     * 
     * @return a File
     */
    public static final native JsFile create(Blob blob, String filename) /*-{
    return new File(blob, filename);
    }-*/;

    public final native double getSize() /*-{
    return this.size;
    }-*/;

    public final native String getType() /*-{
    return null;
    }-*/;

    public final native Date getLastModifiedDate() /*-{
    return this.lastModifiedDate;
    }-*/;

    public final native String getName() /*-{
    return this.name;
    }-*/;
}
