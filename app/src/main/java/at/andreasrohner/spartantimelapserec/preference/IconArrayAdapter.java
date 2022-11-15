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

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import at.andreasrohner.spartantimelapserec.R;

public class IconArrayAdapter extends ArrayAdapter<CharSequence> {
	private int mIndex;
	private String[] mImagePaths;

	public IconArrayAdapter(Context context, int textViewResourceId,
			CharSequence[] objects, String[] mImagePaths, int index) {
		super(context, textViewResourceId, objects);

		this.mIndex = index;
		this.mImagePaths = mImagePaths;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = ((Activity) getContext()).getLayoutInflater();
		View view = inflater.inflate(R.layout.dialog_image_list_preference,
				parent, false);

		ImageView imageView = (ImageView) view
				.findViewById(R.id.dialog_image_list_preference_image);
		String name = mImagePaths[position];
		name = name.substring(name.lastIndexOf('/') + 1, name.lastIndexOf('.'));

		int resId = getContext().getResources().getIdentifier(name, "drawable",
				getContext().getPackageName());
		imageView.setImageResource(resId);

		CheckedTextView checkedTextView = (CheckedTextView) view
				.findViewById(R.id.dialog_image_list_preference_check);

		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
			checkedTextView.setTextColor(Color.BLACK);
			imageView.setBackgroundColor(Color.DKGRAY);
		}

		checkedTextView.setText(getItem(position));

		if (position == mIndex) {
			checkedTextView.setChecked(true);
		}

		return view;
	}
}
