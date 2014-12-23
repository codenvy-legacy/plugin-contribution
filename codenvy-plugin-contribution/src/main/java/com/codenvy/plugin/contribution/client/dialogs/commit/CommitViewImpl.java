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
package com.codenvy.plugin.contribution.client.dialogs.commit;

import com.codenvy.ide.ui.window.Window;
import com.codenvy.plugin.contribution.client.ContributeMessages;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.TextArea;
import com.google.inject.Inject;

import javax.annotation.Nonnull;

/**
 * UI for {@link CommitView}.
 *
 * @author Kevin Pollet
 */
public class CommitViewImpl extends Window implements CommitView {

    /** The UI binder for this component. */
    private static final CommitViewUiBinder UI_BINDER = GWT.create(CommitViewUiBinder.class);

    @UiField(provided = true)
    ContributeMessages messages;

    @UiField
    TextArea commitDescription;

    private final Button ok;

    private ActionDelegate delegate;

    @Inject
    public CommitViewImpl(final ContributeMessages messages) {
        this.messages = messages;

        setWidget(UI_BINDER.createAndBindUi(this));
        setTitle(messages.commitDialogTitle());

        ok = createButton(messages.commitDialogButtonOkText(), "commit-dialog-ok", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                delegate.onOk();
            }
        });
        ok.addStyleName(resources.centerPanelCss().blueButton());

        final Button continueWithoutCommitting =
                createButton(messages.commitDialogButtonContinueText(), "commit-dialog-continue-without-committing",
                             new ClickHandler() {
                                 @Override
                                 public void onClick(ClickEvent event) {
                                     delegate.onContinue();
                                 }
                             });

        getFooter().add(ok);
        getFooter().add(continueWithoutCommitting);
    }

    @Override
    public void show() {
        new Timer() {
            @Override
            public void run() {
                commitDescription.setFocus(true);
            }
        }.schedule(300);
        super.show();
    }

    @Override
    public void close() {
        hide();
    }

    @Nonnull
    @Override
    public String getCommitDescription() {
        return commitDescription.getText();
    }

    @Override
    public void setOkButtonEnabled(final boolean enabled) {
        ok.setEnabled(enabled);
    }

    @Override
    public void setDelegate(final ActionDelegate delegate) {
        this.delegate = delegate;
    }

    @Override
    protected void onClose() {
    }

    @SuppressWarnings("UnusedParameters")
    @UiHandler("commitDescription")
    void onCommitDescriptionChanged(final KeyUpEvent event) {
        delegate.onCommitDescriptionChanged();
    }
}