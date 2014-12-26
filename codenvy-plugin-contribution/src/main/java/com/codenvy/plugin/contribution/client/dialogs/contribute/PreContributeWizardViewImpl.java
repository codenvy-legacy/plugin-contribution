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
package com.codenvy.plugin.contribution.client.dialogs.contribute;

import com.codenvy.ide.ui.window.Window;
import com.codenvy.plugin.contribution.client.ContributeMessages;
import com.codenvy.plugin.contribution.client.ContributeResources;
import com.codenvy.plugin.contribution.client.dialogs.paste.PasteEvent;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;

import javax.inject.Inject;

/**
 * Implementation of {@link PreContributeWizardView}.
 */
public class PreContributeWizardViewImpl extends Window implements PreContributeWizardView {

    /** The uUI binder for this component. */
    private static final PreContributeWizardViewUiBinder UI_BINDER = GWT.create(PreContributeWizardViewUiBinder.class);

    /** The contribute button. */
    private final Button contributeButton;

    /** The input component for the branch name. */
    @UiField
    TextBox branchName;

    /** The input component for the contribution title. */
    @UiField
    TextBox contributionTitle;

    /** The input zone for the contribution comment. */
    @UiField
    TextArea contributionComment;

    /** The i18n messages. */
    @UiField(provided = true)
    ContributeMessages messages;

    /** The bound delegate. */
    private ActionDelegate delegate;

    /** The resources for the view. */
    private final ContributeResources resources;

    @Inject
    public PreContributeWizardViewImpl(final ContributeMessages messages,
                                       final ContributeResources resources) {
        this.messages = messages;
        this.resources = resources;

        setWidget(UI_BINDER.createAndBindUi(this));
        setTitle(messages.preContributeWizardTitle());

        this.branchName.getElement().setPropertyString("placeholder", messages.preContributeWizardBranchNameInputPlaceHolder());
        this.contributionTitle.getElement().setPropertyString("placeholder", messages.preContributeWizardContributionTitlePlaceHolder());
        this.contributionComment.getElement()
                                .setPropertyString("placeholder", messages.preContributeWizardContributionCommentPlaceHolder());

        this.contributeButton =
                createButton(messages.preContributeWizardContributeButton(), "pre-contribute-wizard-contribute-button", new ClickHandler() {
                    @Override
                    public void onClick(final ClickEvent event) {
                        delegate.onContribute();
                    }
                });
        this.contributeButton.addStyleName(Window.resources.centerPanelCss().blueButton());

        final Button cancelButton =
                createButton(messages.preContributeWizardCancelButton(), "pre-contribute-wizard-cancel-button", new ClickHandler() {
                    @Override
                    public void onClick(final ClickEvent event) {
                        delegate.onCancel();
                    }
                });

        getFooter().add(this.contributeButton);
        getFooter().add(cancelButton);
    }

    @Override
    public void reset() {
        branchName.setValue(delegate.suggestBranchName());
        contributionComment.setValue("");
        delegate.updateControls();
    }

    @Override
    protected void onClose() {
    }

    @Override
    public void setDelegate(final ActionDelegate delegate) {
        this.delegate = delegate;
    }

    @Override
    public String getBranchName() {
        return branchName.getValue();
    }

    @Override
    public String getContributionComment() {
        return contributionComment.getValue();
    }

    @Override
    public String getContributionTitle() {
        return contributionTitle.getValue();
    }

    @Override
    public void setContributeEnabled(final boolean enabled) {
        contributeButton.setEnabled(enabled);
    }

    @Override
    public void showBranchNameError(final boolean showError) {
        if (showError) {
            branchName.addStyleName(resources.contributeCss().inputError());
        } else {
            branchName.removeStyleName(resources.contributeCss().inputError());
        }
    }

    @SuppressWarnings("UnusedParameters")
    @UiHandler("branchName")
    public void branchNameChanged(final ValueChangeEvent<String> event) {
        delegate.updateControls();
    }

    @SuppressWarnings("UnusedParameters")
    @UiHandler("branchName")
    public void branchNameKeyUp(final KeyUpEvent event) {
        delegate.updateControls();
    }

    @SuppressWarnings("UnusedParameters")
    @UiHandler("branchName")
    public void branchNamePaste(final PasteEvent event) {
        delegate.updateControls();
    }

    @SuppressWarnings("UnusedParameters")
    @UiHandler("contributionComment")
    public void contributionCommentChanged(final ValueChangeEvent<String> event) {
        delegate.updateControls();
    }

    @SuppressWarnings("UnusedParameters")
    @UiHandler("contributionTitle")
    public void contributionTitleChanged(final ValueChangeEvent<String> event) {
        delegate.updateControls();
    }

    @SuppressWarnings("UnusedParameters")
    @UiHandler("contributionTitle")
    public void contributionTitleKeyUp(final KeyUpEvent event) {
        delegate.updateControls();
    }

    @SuppressWarnings("UnusedParameters")
    @UiHandler("contributionTitle")
    public void contributionTitlePaste(final PasteEvent event) {
        delegate.updateControls();
    }

    @Override
    public void show() {
        super.show();
        new Timer() {
            @Override
            public void run() {
                branchName.setFocus(true);
            }
        }.schedule(300);
    }
}
