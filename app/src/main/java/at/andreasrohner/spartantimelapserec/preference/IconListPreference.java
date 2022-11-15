/*
 * Spartan Time Lapse Recorder - Minimalistic android time lapse recording app
 * Copyright (C) 2014  Andreas Rohner
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package at.andreasrohner.spartantimelapserec.preference;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.preference.ListPreference;
import android.util.AttributeSet;
import android.widget.ListAdapter;
import at.andreasrohner.spartantimelapserec.R;

public class IconListPreference extends ListPreference {
	String[] mImagePaths;

	public IconListPreference(Context context, AttributeSet attrs) {
		super(context, attrs);

		int resId = attrs.getAttributeResourceValue(null, "entryImages", 0);
		mImagePaths = context.getResources().getStringArray(resId);
	}

	@Override
	protected void onPrepareDialogBuilder(Builder builder) {
		ListAdapter listAdapter = new IconArrayAdapter(getContext(),
				R.layout.dialog_image_list_preference, getEntries(),
				mImagePaths, findIndexOfValue(getValue()));

		builder.setAdapter(listAdapter, this);

		super.onPrepareDialogBuilder(builder);
	}
}
