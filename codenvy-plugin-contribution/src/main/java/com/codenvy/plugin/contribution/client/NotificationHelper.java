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

import com.codenvy.ide.api.notification.Notification;
import com.codenvy.ide.api.notification.NotificationManager;
import com.codenvy.ide.util.loging.Log;
import com.google.inject.Inject;

import javax.annotation.Nonnull;

import static com.codenvy.ide.api.notification.Notification.Status.FINISHED;
import static com.codenvy.ide.api.notification.Notification.Type.ERROR;
import static com.codenvy.ide.api.notification.Notification.Type.INFO;
import static com.codenvy.ide.api.notification.Notification.Type.WARNING;

/**
 * Helper class to work with notifications and standardize contribute workflow notifications.
 *
 * @author Kevin Pollet
 */
public final class NotificationHelper {
    /** The notification manger used to handle errors. */
    private final NotificationManager notificationManager;

    /** The contribute messages. */
    private final ContributeMessages messages;

    @Inject
    protected NotificationHelper(@Nonnull final NotificationManager notificationManager, @Nonnull final ContributeMessages messages) {
        this.notificationManager = notificationManager;
        this.messages = messages;
    }

    /**
     * Shows a warning notification.
     *
     * @param message
     *         the info message.
     */
    public void showInfo(@Nonnull final String message) {
        showNotification(new Notification(message, INFO));
    }

    /**
     * Shows a warning notification.
     *
     * @param message
     *         the warning message.
     */
    public void showWarning(@Nonnull final String message) {
        showNotification(new Notification(message, WARNING));
    }

    /**
     * Shows an error notification.
     *
     * @param cls
     *         {@link java.lang.Class} where the exception is caught.
     * @param message
     *         the error message.
     */
    public void showError(@Nonnull final Class<?> cls, @Nonnull final String message) {
        showError(cls, new Exception(message));
    }

    /**
     * Handles an exception and display the error message in a notification.
     *
     * @param cls
     *         {@link java.lang.Class} where the exception is caught.
     * @param exception
     *         exception to handle.
     */
    public void showError(@Nonnull final Class<?> cls, @Nonnull final Throwable exception) {
        showNotification(new Notification(exception.getMessage(), ERROR));
        Log.error(cls, exception);
    }

    /**
     * Log the exception, display the error message in the notification.
     */
    public void showError(@Nonnull final Class< ? > cls, @Nonnull final String errorMessage, @Nonnull final Throwable exception) {
        showNotification(new Notification(errorMessage, ERROR));
        Log.error(cls, exception);
    }

    /**
     * Finish a notification in progress with an error.
     *
     * @param cls
     *         the class where the exception is caught.
     * @param message
     *         the error message.
     * @param notification
     *         the notification to finish.
     */
    public void finishNotificationWithError(@Nonnull final Class<?> cls,
                                            @Nonnull final String message,
                                            @Nonnull final Notification notification) {
        finishNotificationWithError(cls, new Exception(message), notification);
    }

    /**
     * Finish a notification in progress with an error.
     *
     * @param cls
     *         the class where the exception is caught.
     * @param exception
     *         the exception.
     * @param notification
     *         the notification to finish.
     */
    public void finishNotificationWithError(@Nonnull final Class<?> cls,
                                            @Nonnull final Throwable exception,
                                            @Nonnull final Notification notification) {
        notification.setType(ERROR);
        finishNotification(exception.getMessage(), notification);
        Log.error(cls, exception);
    }

    /**
     * Finish a notification in progress with a warning.
     *
     * @param message
     *         the warning message.
     * @param notification
     *         the notification to finish.
     */
    public void finishNotificationWithWarning(@Nonnull final String message, @Nonnull final Notification notification) {
        notification.setType(WARNING);
        finishNotification(message, notification);
    }

    /**
     * Finish a notification in progress.
     *
     * @param message
     *         the finish message.
     * @param notification
     *         the notification to finish.
     */
    public void finishNotification(@Nonnull final String message, @Nonnull final Notification notification) {
        notification.setMessage(messages.prefixNotification(message));
        notification.setStatus(FINISHED);
    }

    /**
     * Shows a notification.
     *
     * @param notification
     *         notification to display.
     */
    public void showNotification(@Nonnull final Notification notification) {
        notification.setMessage(messages.prefixNotification(notification.getMessage()));
        notificationManager.showNotification(notification);
    }
}
