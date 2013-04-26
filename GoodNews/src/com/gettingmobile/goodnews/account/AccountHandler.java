package com.gettingmobile.goodnews.account;


import com.gettingmobile.android.app.actions.ActionContext;

public interface AccountHandler {
    /**
     * Specifies whether an account has already been prompted and is still valid.
     * @return whether an account has already been prompted and is still valid.
     */
    boolean hasAccount();

    /**
     * Whether the prompt dialog is currently showing or not.
     * @return true if the prompt dialog is currently showing.
     */
    boolean isPromptShowing();

    /**
     * Prompts the user for a valid account.
     * @param actionContext the context to execute the operation in.
     * @param callback the callback to receive status updates.
     */
    void promptAccount(ActionContext actionContext, LoginCallback callback);

    /**
     * Silently tries to login using the already provided account.
     * @param callback the callback to receive status updates.
     * @throws IllegalStateException if {@link #hasAccount()} is {@code false}.
     */
	void login(LoginCallback callback) throws IllegalStateException;
}
