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
package com.codenvy.plugin.contribution.client;

import com.codenvy.api.project.shared.dto.ProjectDescriptor;
import com.codenvy.ide.api.action.ActionManager;
import com.codenvy.ide.api.action.DefaultActionGroup;
import com.codenvy.ide.api.app.AppContext;
import com.codenvy.ide.api.constraints.Anchor;
import com.codenvy.ide.api.constraints.Constraints;
import com.codenvy.ide.api.event.ProjectActionEvent;
import com.codenvy.ide.api.event.ProjectActionHandler;
import com.codenvy.ide.api.extension.Extension;
import com.codenvy.ide.api.notification.Notification;
import com.codenvy.plugin.contribution.client.parts.contribute.ContributePartPresenter;
import com.codenvy.plugin.contribution.client.value.Context;
import com.codenvy.plugin.contribution.client.vcs.Branch;
import com.codenvy.plugin.contribution.client.vcs.Remote;
import com.codenvy.plugin.contribution.client.vcs.VcsService;
import com.codenvy.plugin.contribution.client.vcshost.RepositoryHost;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import javax.inject.Named;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.codenvy.ide.api.action.IdeActions.GROUP_MAIN_TOOLBAR;
import static com.codenvy.ide.api.action.IdeActions.GROUP_RUN_TOOLBAR;
import static com.codenvy.ide.api.notification.Notification.Status.PROGRESS;
import static com.codenvy.ide.api.notification.Notification.Type.INFO;
import static com.google.gwt.http.client.URL.encodeQueryString;

/**
 * @author Stephane Tournie
 * @author Kevin Pollet
 */
@Singleton
@Extension(title = "Contributor", version = "1.0.0")
public class ContributorExtension {
    private static final String WORKING_BRANCH_NAME_PREFIX = "contrib-";
    private static final String ORIGIN_REMOTE_NAME         = "origin";

    private final ActionManager           actionManager;
    private final Context                 context;
    private final ContributeAction        contributeAction;
    private final ContributeMessages      messages;
    private final VcsService              vcsService;
    private final String                  baseUrl;
    private final AppContext              appContext;
    private final NotificationHelper      notificationHelper;
    private final RepositoryHost          repositoryHost;
    private final ContributePartPresenter contributePartPresenter;

    private DefaultActionGroup contributeToolbarGroup;
    private DefaultActionGroup mainToolbarGroup;

    @Inject
    public ContributorExtension(final Context context,
                                final EventBus eventBus,
                                final ActionManager actionManager,
                                final ContributeAction contributeAction,
                                final ContributeMessages messages,
                                final VcsService gitAgent,
                                final ContributeResources resources,
                                final @Named("restContext") String baseUrl,
                                final AppContext appContext,
                                final NotificationHelper notificationHelper,
                                final RepositoryHost repositoryHost,
                                final ContributePartPresenter contributePartPresenter) {
        this.actionManager = actionManager;
        this.context = context;
        this.contributeAction = contributeAction;
        this.messages = messages;
        this.vcsService = gitAgent;
        this.baseUrl = baseUrl;
        this.appContext = appContext;
        this.notificationHelper = notificationHelper;
        this.repositoryHost = repositoryHost;
        this.contributePartPresenter = contributePartPresenter;

        resources.contributeCss().ensureInjected();
        contributePartPresenter.process();

        eventBus.addHandler(ProjectActionEvent.TYPE, new ProjectActionHandler() {
            @Override
            public void onProjectOpened(final ProjectActionEvent event) {
                initContributeMode(event);
            }

            @Override
            public void onProjectClosed(final ProjectActionEvent projectActionEvent) {
                exitContributeMode();
            }
        });
    }

    /**
     * Initialize contributor environment
     *
     * @param event
     *         the load event.
     */
    private void initContributeMode(final ProjectActionEvent event) {
        if (!appContext.getCurrentUser().isUserPermanent()) {
            authenticateWithVCSHost();
        }

        final ProjectDescriptor project = event.getProject();
        // get origin repository's URL from default remote
        vcsService.listRemotes(event.getProject(), new AsyncCallback<List<Remote>>() {

            @Override
            public void onSuccess(final List<Remote> result) {
                for (final Remote remote : result) {
                    // save origin repository name & owner in context
                    if (ORIGIN_REMOTE_NAME.equalsIgnoreCase(remote.getName())) {
                        final String remoteUrl = remote.getUrl();
                        final String repositoryName = repositoryHost.getRepositoryNameFromUrl(remoteUrl);
                        final String repositoryOwner = repositoryHost.getRepositoryOwnerFromUrl(remoteUrl);

                        context.setOriginRepositoryOwner(repositoryOwner);
                        context.setOriginRepositoryName(repositoryName);

                        // initiate contributor button & working branch
                        onDefaultRemoteReceived(project);
                        break;
                    }
                }
            }

            @Override
            public void onFailure(final Throwable exception) {
                notificationHelper.showError(ContributorExtension.class, exception);
            }
        });
    }

    private void onDefaultRemoteReceived(final ProjectDescriptor project) {
        context.setProject(project);

        final Map<String, List<String>> attributes = project.getAttributes();
        if (attributes != null && attributes.containsKey(ContributeConstants.ATTRIBUTE_CONTRIBUTE_KEY)) {

            final String contributeAttribute = attributes.get(ContributeConstants.ATTRIBUTE_CONTRIBUTE_KEY).get(0);
            if ("true".equalsIgnoreCase(contributeAttribute)) {

                // branch specified in factory.json has been already checkout at this point
                // register & display contribute button
                actionManager.registerAction(messages.contributorButtonName(), contributeAction);
                mainToolbarGroup = (DefaultActionGroup)actionManager.getAction(GROUP_MAIN_TOOLBAR);
                contributeToolbarGroup = new DefaultActionGroup(GROUP_MAIN_TOOLBAR, false, actionManager);
                contributeToolbarGroup.add(contributeAction);
                mainToolbarGroup.add(contributeToolbarGroup, new Constraints(Anchor.AFTER, GROUP_RUN_TOOLBAR));

                final String workingBranchName = generateWorkingBranchName();
                context.setWorkBranchName(workingBranchName);

                final Notification createWorkingBranchNotification =
                        new Notification(messages.notificationCreatingNewWorkingBranch(workingBranchName), INFO, PROGRESS);
                notificationHelper.showNotification(createWorkingBranchNotification);

                // the working branch is only created if it doesn't exist
                vcsService.listLocalBranches(project, new AsyncCallback<List<Branch>>() {
                    @Override
                    public void onFailure(final Throwable exception) {
                        notificationHelper.finishNotificationWithError(ContributorExtension.class, exception,
                                                                       createWorkingBranchNotification);
                    }

                    @Override
                    public void onSuccess(final List<Branch> branches) {
                        boolean workingBranchExists = false;

                        for (final Branch oneBranch : branches) {
                            if (workingBranchName.equals(oneBranch.getDisplayName())) {
                                workingBranchExists = true;
                                break;
                            }
                        }

                        // shorthand for create + checkout new temporary working branch -> checkout -b branchName
                        vcsService.checkoutBranch(project, workingBranchName, !workingBranchExists, new AsyncCallback<String>() {
                            @Override
                            public void onSuccess(final String result) {
                                contributePartPresenter.showContributePart();
                                notificationHelper.finishNotification(
                                        messages.notificationBranchSuccessfullyCreatedAndCheckedOut(workingBranchName),
                                        createWorkingBranchNotification);
                            }

                            @Override
                            public void onFailure(final Throwable exception) {
                                notificationHelper.finishNotificationWithError(ContributorExtension.class, exception,
                                                                               createWorkingBranchNotification);
                            }
                        });
                    }
                });
            }
        }
    }

    private void exitContributeMode() {
        // remove contribute button
        actionManager.unregisterAction(messages.contributorButtonName());
        if (mainToolbarGroup != null && contributeToolbarGroup != null) {
            mainToolbarGroup.remove(contributeToolbarGroup);
        }
    }

    /**
     * Authenticates the user on the VCS Host.
     */
    private void authenticateWithVCSHost() {
        final String authUrl = baseUrl
                               + "/oauth/authenticate?oauth_provider=github&mode=federated_login"
                               + "&scope=user,repo,write:public_key&redirect_after_login="
                               + encodeQueryString(baseUrl + "/oauth?redirect_url=" + Window.Location.getHref() + "&oauth_provider=github");

        Window.Location.assign(authUrl);
    }

    /**
     * Generates the working branch name used for the contribution.
     *
     * @return the working branch name, never {@code null}.
     */
    private String generateWorkingBranchName() {
        final DateTimeFormat dateTimeFormat = DateTimeFormat.getFormat("MMddyyyy");
        return WORKING_BRANCH_NAME_PREFIX + dateTimeFormat.format(new Date());
    }
}
