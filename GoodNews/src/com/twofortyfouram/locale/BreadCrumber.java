// Copyright 2010 two forty four a.m. LLC <http://www.twofortyfouram.com>
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at <http://www.apache.org/licenses/LICENSE-2.0>
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.twofortyfouram.locale;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.gettingmobile.goodnews.R;

/**
 * Utility class to generate a breadcrumb title string for {@code Activity} instances in <i>Locale</i>.
 * <p>
 * This class cannot be instantiated.
 */
public final class BreadCrumber
{
	/**
	 * Class name cached for logging purposes
	 */
	private static final String LOGGING_CLASS_NAME = BreadCrumber.class.getSimpleName();

	/**
	 * Private constructor prevents instantiation
	 *
	 * @throws UnsupportedOperationException because this class cannot be instantiated.
	 */
	private BreadCrumber()
	{
		throw new UnsupportedOperationException(String.format("%s(): This class is non-instantiable", LOGGING_CLASS_NAME)); //$NON-NLS-1$
	}

	/**
	 * Static helper method to generate breadcrumbs. Breadcrumb strings will be properly formatted for the current language,
	 * including right-to-left languages, as long as the proper {@link R.string#twofortyfouram_locale_breadcrumb_format} string
	 * resources have been created.
	 * 
	 * @param c {@code Context} for loading platform resources. Cannot be null.
	 * @param intent {@code Intent} to extract the breadcrumb from.
	 * @param currentCrumb The last element of the breadcrumb path.
	 * @return {@code String} presentation of the breadcrumb. If the intent parameter is null, then this method returns
	 *         currentCrumb. If currentCrumb is null, then this method returns the empty string "". If intent contains a private
	 *         Serializable instances as an extra, then this method returns the empty string "".
	 */
	public static CharSequence generateBreadcrumb(final Context c, final Intent intent, final String currentCrumb)
	{
		try
		{
			if (currentCrumb == null)
			{
				Log.w(Constants.LOG_TAG, String.format("%s.generateBreadcrumb(Context, Intent, String): currentCrumb param was null", LOGGING_CLASS_NAME)); //$NON-NLS-1$
				return ""; //$NON-NLS-1$
			}
			if (intent == null)
			{
				Log.w(Constants.LOG_TAG, String.format("%s.generateBreadcrumb(Context, Intent, String): Intent param was null", LOGGING_CLASS_NAME)); //$NON-NLS-1$
				return currentCrumb;
			}

			/*
			 * Note: this is vulnerable to a custom serializable attack, but the try-catch will solve that
			 */
			final String breadcrumbString = intent.getStringExtra(com.twofortyfouram.locale.Intent.EXTRA_STRING_BREADCRUMB);
			if (breadcrumbString != null)
			{
				return c
						.getString(R.string.twofortyfouram_locale_breadcrumb_format, breadcrumbString, c.getString(R.string.twofortyfouram_locale_breadcrumb_separator), currentCrumb);
			}
			return currentCrumb;
		}
		catch (final Exception e)
		{
			Log.e(Constants.LOG_TAG, String.format("%s.generateBreadcrumb.(Context, Intent, String): Encountered error generating breadcrumb", LOGGING_CLASS_NAME), e); //$NON-NLS-1$
			return ""; //$NON-NLS-1$
		}
	}
}