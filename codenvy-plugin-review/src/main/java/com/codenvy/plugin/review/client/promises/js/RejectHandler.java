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
package com.codenvy.plugin.review.client.promises.js;

import com.codenvy.plugin.review.client.promises.Function;
import com.codenvy.plugin.review.client.promises.Operation;
import com.codenvy.plugin.review.client.promises.PromiseError;
import com.google.gwt.core.client.JavaScriptObject;

public class RejectHandler<R> extends JavaScriptObject {

    protected RejectHandler() {}

    public static final native <R> RejectHandler<R> create(Function<PromiseError, R> rejectFunc) /*-{
        return function(reason) {
            return rejectFunc.@com.codenvy.plugin.review.client.promises.Function::apply(*)(reason);
        };
    }-*/;

    public static final native RejectHandler<Void> create(Operation<PromiseError> rejectFunc) /*-{
        return function(reason) {
            rejectFunc.@com.codenvy.plugin.review.client.promises.Operation::apply(*)(reason);
        };
    }-*/;

    public final native void reject(PromiseError reason) /*-{
        this(reason);
    }-*/;
}
