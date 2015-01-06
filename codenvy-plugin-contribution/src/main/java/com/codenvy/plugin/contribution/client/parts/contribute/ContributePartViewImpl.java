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
package com.codenvy.plugin.contribution.client.parts.contribute;

import com.codenvy.ide.api.parts.PartStackUIResources;
import com.codenvy.ide.api.parts.base.BaseView;
import com.codenvy.plugin.contribution.client.ContributeMessages;
import com.codenvy.plugin.contribution.client.ContributeResources;
import com.codenvy.plugin.contribution.client.dialogs.paste.PasteEvent;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import static com.google.gwt.dom.client.Style.Display.BLOCK;
import static com.google.gwt.dom.client.Style.Display.NONE;

/**
 * Implementation of {@link com.codenvy.plugin.contribution.client.parts.contribute.ContributePartView}.
 */
public class ContributePartViewImpl extends BaseView<ContributePartView.ActionDelegate> implements ContributePartView {

    /** The uUI binder for this component. */
    private static final ContributePartViewUiBinder UI_BINDER = GWT.create(ContributePartViewUiBinder.class);

    /** The contribute button. */
    @UiField
    Button contributeButton;

    /** The resources for the view. */
    @UiField(provided = true)
    ContributeResources resources;

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

    /** The contribution status section. */
    @UiField
    HTMLPanel statusSection;

    /** The create fork check box. */
    @UiField
    CheckBox createForkCheckBox;

    /** The push branch check box. */
    @UiField
    CheckBox pushBranchCheckBox;

    /** The issue pull request check box. */
    @UiField
    CheckBox issuePullRequestCheckBox;

    /** The status section footer. */
    @UiField
    HTMLPanel statusSectionFooter;

    /** Open on repository host button. */
    @UiField
    Button openOnRepositoryHostButton;

    @Inject
    public ContributePartViewImpl(@Nonnull final PartStackUIResources partStackUIResources,
                                  @Nonnull final ContributeMessages messages,
                                  @Nonnull final ContributeResources resources) {
        super(partStackUIResources);

        this.messages = messages;
        this.resources = resources;

        this.container.add(UI_BINDER.createAndBindUi(this));
        setTitle(messages.contributePartTitle());

        this.statusSection.setVisible(false);
        this.branchName.getElement().setPropertyString("placeholder", messages.contributePartBranchNameInputPlaceHolder());
        this.contributionTitle.getElement().setPropertyString("placeholder", messages.contributePartContributionTitlePlaceHolder());
        this.contributionComment.getElement().setPropertyString("placeholder", messages.contributePartContributionCommentPlaceHolder());
    }

    @Override
    public void reset() {
        branchName.setValue(delegate.suggestBranchName());
        contributionComment.setValue("");
        contributeButton.getElement().getStyle().setDisplay(BLOCK);

        statusSection.setVisible(false);
        statusSectionFooter.setVisible(false);
        createForkCheckBox.setValue(false);
        pushBranchCheckBox.setValue(false);
        issuePullRequestCheckBox.setValue(false);

        delegate.updateControls();
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
    public void hideContribute() {
        contributeButton.getElement().getStyle().setDisplay(NONE);
    }

    @Override
    public void showBranchNameError(final boolean showError) {
        if (showError) {
            branchName.addStyleName(resources.contributeCss().inputError());
        } else {
            branchName.removeStyleName(resources.contributeCss().inputError());
        }
    }

    @Override
    public void showContributionTitleError(boolean showError) {
        if (showError) {
            contributionTitle.addStyleName(resources.contributeCss().inputError());
        } else {
            contributionTitle.removeStyleName(resources.contributeCss().inputError());
        }
    }

    @Override
    public void showStatusSection() {
        statusSection.setVisible(true);
    }

    @Override
    public void checkCreateForkCheckBox() {
        createForkCheckBox.setValue(true);
    }

    @Override
    public void checkPushBranchCheckBox() {
        pushBranchCheckBox.setValue(true);
    }

    @Override
    public void checkIssuePullRequestCheckBox() {
        issuePullRequestCheckBox.setValue(true);
    }

    @Override
    public void showStatusSectionFooter() {
        statusSectionFooter.setVisible(true);
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
    @UiHandler("openOnRepositoryHostButton")
    public void openOnRepositoryHostClick(final ClickEvent event) {
        delegate.onOpenOnRepositoryHost();
    }

    @SuppressWarnings("UnusedParameters")
    @UiHandler("contributeButton")
    public void contributeClick(final ClickEvent event) {
        delegate.onContribute();
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
}
