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
import com.codenvy.ide.ui.buttonLoader.ButtonLoaderResources;
import com.codenvy.plugin.contribution.client.ContributeMessages;
import com.codenvy.plugin.contribution.client.ContributeResources;
import com.codenvy.plugin.contribution.client.dialogs.paste.PasteEvent;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;

import org.vectomatic.dom.svg.ui.SVGImage;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import static com.google.gwt.dom.client.Style.Unit.PX;

/**
 * Implementation of {@link com.codenvy.plugin.contribution.client.parts.contribute.ContributePartView}.
 */
public class ContributePartViewImpl extends BaseView<ContributePartView.ActionDelegate> implements ContributePartView {

    /** The uUI binder for this component. */
    private static final ContributePartViewUiBinder UI_BINDER = GWT.create(ContributePartViewUiBinder.class);

    /** The button loader resources. */
    private final ButtonLoaderResources buttonLoaderResources;

    /** The contribute button. */
    @UiField
    Button contributeButton;

    /** The resources for the view. */
    @UiField(provided = true)
    ContributeResources resources;

    /** The component for the URL of factory repository. */
    @UiField
    Anchor repositoryUrl;

    /** The component for the name of cloned branch. */
    @UiField
    Label clonedBranch;

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

    /** The create fork status panel. */
    @UiField
    SimplePanel createForkStatus;

    /** The push branch status panel. */
    @UiField
    SimplePanel pushBranchStatus;

    /** The issue pull request status panel. */
    @UiField
    SimplePanel issuePullRequestStatus;

    /** The status section footer. */
    @UiField
    HTMLPanel statusSectionFooter;

    /** Open on repository host button. */
    @UiField
    Button openOnRepositoryHostButton;

    /** The start new contribution section. */
    @UiField
    HTMLPanel newContributionSection;

    /** The new contribution button. */
    @UiField
    Button newContributionButton;

    /** The contribute button text. */
    private String contributeButtonText;

    @Inject
    public ContributePartViewImpl(@Nonnull final PartStackUIResources partStackUIResources,
                                  @Nonnull final ContributeMessages messages,
                                  @Nonnull final ContributeResources resources,
                                  @Nonnull final ButtonLoaderResources buttonLoaderResources) {
        super(partStackUIResources);

        this.messages = messages;
        this.resources = resources;
        this.buttonLoaderResources = buttonLoaderResources;

        this.container.add(UI_BINDER.createAndBindUi(this));

        setTitle(messages.contributePartTitle());

        this.contributeButtonText = contributeButton.getText();
        this.contributeButton.addStyleName(buttonLoaderResources.Css().buttonLoader());

        this.statusSection.setVisible(false);
        this.newContributionSection.setVisible(false);
        this.branchName.getElement()
                       .setPropertyString("placeholder", messages.contributePartConfigureContributionSectionBranchNamePlaceholder());
        this.contributionTitle.getElement().setPropertyString("placeholder",
                                                              messages.contributePartConfigureContributionSectionContributionTitlePlaceholder());
        this.contributionComment.getElement().setPropertyString("placeholder",
                                                                messages.contributePartConfigureContributionSectionContributionCommentPlaceholder());
    }

    @Override
    public void reset() {
        branchName.setValue(delegate.suggestBranchName());
        contributionComment.setValue("");

        hideStatusSection();
        resetStatusSection();

        hideNewContributionSection();

        delegate.updateControls();
    }

    @Override
    public void setRepositoryUrl(final String url) {
        repositoryUrl.setHref(url);
        repositoryUrl.setText(url);
    }

    @Override
    public void setClonedBranch(final String branch) {
        clonedBranch.setText(branch);
    }

    @Override
    public void setContributeButtonText(final String text) {
        contributeButton.setText(text);
        contributeButtonText = contributeButton.getText();
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
    public void setBranchNameEnabled(final boolean enabled) {
        branchName.setEnabled(enabled);
    }

    @Override
    public void setBranchNameFocus(final boolean focused) {
        branchName.setFocus(focused);
    }

    @Override
    public void setContributionCommentEnabled(final boolean enabled) {
        contributionComment.setEnabled(enabled);
        if (!enabled) {
            contributionComment.getElement().getStyle().setBackgroundColor("#5a5c5c");
        } else {
            contributionComment.getElement().getStyle().clearBackgroundColor();
        }
    }

    @Override
    public void setContributionTitleEnabled(final boolean enabled) {
        contributionTitle.setEnabled(enabled);
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

    @Override
    public void showContributionTitleError(final boolean showError) {
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
    public void hideStatusSection() {
        statusSection.setVisible(false);
    }

    @Override
    public void resetStatusSection() {
        statusSectionFooter.setVisible(false);
        createForkStatus.clear();
        pushBranchStatus.clear();
        issuePullRequestStatus.clear();
    }

    @Override
    public void setCreateForkStatus(final boolean success) {
        createForkStatus.clear();
        createForkStatus.add(getStatusImage(success));
    }


    @Override
    public void setPushBranchStatus(final boolean success) {
        pushBranchStatus.clear();
        pushBranchStatus.add(getStatusImage(success));
    }

    @Override
    public void setIssuePullRequestStatus(final boolean success) {
        issuePullRequestStatus.clear();
        issuePullRequestStatus.add(getStatusImage(success));
    }

    @Override
    public void setContributionProgressState(final boolean progress) {
        if (progress) {
            contributeButton.setHTML("<i></i>");
        } else {
            contributeButton.setText(contributeButtonText);
        }
    }

    private SVGImage getStatusImage(final boolean success) {
        final SVGImage image = new SVGImage(success ? resources.ok() : resources.error());
        image.getElement().getStyle().setWidth(15, PX);
        image.getElement().getStyle().setProperty("fill", success ? "#FFFFFF" : "#CF3C3E");

        return image;
    }

    @Override
    public void showStatusSectionFooter() {
        statusSectionFooter.setVisible(true);
    }

    @Override
    public void showNewContributionSection() {
        newContributionSection.setVisible(true);
    }

    @Override
    public void hideNewContributionSection() {
        newContributionSection.setVisible(false);
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
    @UiHandler("newContributionButton")
    public void newContributionClick(final ClickEvent event) {
        delegate.onNewContribution();
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
