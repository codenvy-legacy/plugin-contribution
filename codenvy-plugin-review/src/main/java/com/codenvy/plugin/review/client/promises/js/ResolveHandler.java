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
import com.google.gwt.core.client.JavaScriptObject;

public class ResolveHandler<A, R> extends JavaScriptObject {

    protected ResolveHandler() {}

    public static final native <U, S> ResolveHandler<U, S> create(Function<U, S> resolveFunc) /*-{
        return function(value) {
            return resolveFunc.@com.codenvy.plugin.review.client.promises.Function::apply(*)(value);
        };
    }-*/;

    public static final native <U> ResolveHandler<U, Void> create(Operation<U> resolveFunc) /*-{
        return function(value) {
            // does not (explicitely) return a value
            resolveFunc.@com.codenvy.plugin.review.client.promises.Operation::apply(*)(value);
        };
    }-*/;

    public final native R resolve(A value) /*-{
        return this(value);
    }-*/;
}
