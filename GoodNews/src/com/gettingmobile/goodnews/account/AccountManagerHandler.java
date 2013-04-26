package com.gettingmobile.goodnews.account;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.util.Log;
import com.gettingmobile.android.app.DialogFactory;
import com.gettingmobile.android.app.actions.ActionContext;
import com.gettingmobile.goodnews.Application;
import com.gettingmobile.goodnews.R;

public final class AccountManagerHandler extends AbstractAccountHandler {
    private static final String LOG_TAG = "goodnews.AccountManagerHandler";
    protected static final String ACCOUNT_TYPE = AccountManagerHandler.class.getSimpleName();
	private static final String MANAGED_ACCOUNT_TYPE = "com.google";
	private static final String AUTH_TOKEN_TYPE = "reader";
	private final AccountManager accountManager;
    private final AccountHandler credentialsAccountHandler;
	
	public AccountManagerHandler(Application app) {
		super(app);
		accountManager = AccountManager.get(app);
        credentialsAccountHandler = new CredentialsAccountHandler(app);
	}

    /*
     * login handling
     */

	private Account[] getAccounts() {
		return accountManager.getAccountsByType(MANAGED_ACCOUNT_TYPE);
	}

    private Account getAccount() {
        final String accountName = getSettings().getAccountName();

        if (accountName != null) {
            final Account[] accounts = getAccounts();

            /*
             * check whether account does still exist
             */
            for (Account account : accounts) {
                if (accountName.equals(account.name)) {
                    return account;
                }
            }
        }

        /*
         * no account has been specified or the specified account is no long existing
         */
        return null;
    }

    private boolean wasLastLoginWithCredentials() {
        final String lastLoginAccountType = getSettings().getAccountType();
        return lastLoginAccountType != null && lastLoginAccountType.equals(CredentialsAccountHandler.ACCOUNT_TYPE);
    }

    @Override
    public boolean hasAccount() {
        return wasLastLoginWithCredentials() ? credentialsAccountHandler.hasAccount() : getAccount() != null;
    }

	@Override
	public void login(LoginCallback callback) {
        /*
         * check type of the last login
         */
        if (wasLastLoginWithCredentials()) {
            /*
             * login using the credentials account manager
             */
            Log.d(LOG_TAG, "Last login was done using credentials account handler. So forward request to this one.");
            credentialsAccountHandler.login(callback);
        } else {
            /*
             * get managed account used the last time
             */
            Log.d(LOG_TAG, "Last login was done using account manager handler.");
            final Account account = getAccount();
            if (account == null)
                throw new IllegalStateException("An account needs to be selected at first. Call promptAccount().");

            authenticate(callback, account, null);
        }
	}

    private void authenticate(final LoginCallback loginCallback, Account account, Activity activity) {
        fireOnLoginStarted(loginCallback);

        if (!account.name.equals(getSettings().getAccountName())) {
            /*
             * we are changing the user, so lets swipe our data (tags and items, etc.) as it will not be valid for the
             * new user
             */
            invalidateCache();
        }
        invalidateSession();
        getSettings().setAccountType(ACCOUNT_TYPE);
        getSettings().setAccountName(account.name);

        final AccountManagerCallback<Bundle> c = new AccountManagerCallback<Bundle>() {
            @Override
            public void run(AccountManagerFuture<Bundle> result) {
                onAuthenticationFinished(loginCallback, result);
            }
        };

        if (activity != null) {
            accountManager.getAuthToken(account, AUTH_TOKEN_TYPE, null, activity, c, null);
        } else {
            accountManager.getAuthToken(account, AUTH_TOKEN_TYPE, true, c, null);
        }
    }

    private void onAuthenticationFinished(
            final LoginCallback loginCallback, AccountManagerFuture<Bundle> result) {
        assert result.isDone();
        try {
            final String authToken = result.getResult().getString(AccountManager.KEY_AUTHTOKEN);
            onAuthenticationFinished(loginCallback, null, authToken);
        } catch (Exception ex) {
            onAuthenticationFinished(loginCallback, ex, null);
        }
    }

	private void invalidateSession() {
		final String authToken = getSettings().getAuthToken();
		if (authToken != null) {
			accountManager.invalidateAuthToken(MANAGED_ACCOUNT_TYPE, authToken);
		}
		getSettings().setAccountName(null);
		getApp().authenticate(null);
	}


    /*
     * prompt account handling
     */

    @Override
	public void promptAccount(final ActionContext actionContext, final LoginCallback callback) {
        super.promptAccount(actionContext, callback);

        final Account[] accounts = getAccounts();
        final String[] accountNames = new String[accounts.length + 2];
        for (int i = 0; i < accounts.length; ++i) {
            accountNames[i] = accounts[i].name;
        }
        accountNames[accounts.length] = getString(R.string.account_create);
        accountNames[accounts.length + 1] = getString(R.string.account_manual);

        promptDialog = new AlertDialog.Builder(actionContext.getActivity())
            .setTitle(R.string.account_select_title)
            .setItems(accountNames, new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    onAccountSelection(actionContext, callback, dialog, accounts, which);
                }
            }).show();
        promptDialog.setOwnerActivity(actionContext.getActivity());
	}

    private void onAccountSelection(final ActionContext actionContext,
            final LoginCallback loginCallback, final DialogInterface dialog, Account[] accounts, int which) {
        DialogFactory.dismissDialogSafely(dialog);
        assert accounts != null;
        assert which >= 0 && which <= accounts.length;
        if (which < accounts.length) {
            if (wasLastLoginWithCredentials()) {
                invalidateCache();
            }
            authenticate(loginCallback, accounts[which], actionContext.getActivity());
        } else if (which == accounts.length) {
            final AccountManagerCallback<Bundle> c = new AccountManagerCallback<Bundle>() {
                @Override
                public void run(AccountManagerFuture<Bundle> result) {
                    onAccountCreationFinished(actionContext, loginCallback, result);
                }
            };
            accountManager.addAccount(MANAGED_ACCOUNT_TYPE, AUTH_TOKEN_TYPE, null, null, actionContext.getActivity(), c, null);
        } else {
            if (!wasLastLoginWithCredentials()) {
                invalidateCache();
            }
            credentialsAccountHandler.promptAccount(actionContext, loginCallback);
        }
    }

    private void onAccountCreationFinished(final ActionContext actionContext, LoginCallback loginCallback,
                                           AccountManagerFuture<Bundle> result) {
        assert result.isDone();
        try {
            final Bundle b = result.getResult();
            authenticate(loginCallback, new Account(
                    b.getString(AccountManager.KEY_ACCOUNT_NAME),
                    b.getString(AccountManager.KEY_ACCOUNT_TYPE)), actionContext.getActivity());
        } catch (Exception ex) {
            promptAccount(actionContext, loginCallback);
        }
    }
}
