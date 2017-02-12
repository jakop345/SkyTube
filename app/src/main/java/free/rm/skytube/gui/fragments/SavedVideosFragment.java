/*
 * SkyTube
 * Copyright (C) 2016  Ramon Mifsud
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation (version 3 of the License).
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package free.rm.skytube.gui.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.Bind;
import free.rm.skytube.R;
import free.rm.skytube.businessobjects.AsyncTaskParallel;
import free.rm.skytube.businessobjects.MainActivityListener;
import free.rm.skytube.businessobjects.db.SavedVideosDb;
import free.rm.skytube.gui.businessobjects.SavedVideoGridAdapter;
import free.rm.skytube.gui.businessobjects.SimpleItemTouchHelperCallback;

public class SavedVideosFragment extends VideosGridFragment implements SavedVideosDb.SavedVideosDbListener {
	@Bind(R.id.noSavedVideosText)
	View noSavedVideosText;

	private SavedVideoGridAdapter savedVideoGridAdapter;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setLayoutResource(R.layout.videos_gridview_savedvideos);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = super.onCreateView(inflater, container, savedInstanceState);
		if(savedVideoGridAdapter == null)
			savedVideoGridAdapter = new SavedVideoGridAdapter(getActivity());
		gridView.setAdapter(savedVideoGridAdapter);

		savedVideoGridAdapter.setListener((MainActivityListener)getActivity());

		ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(savedVideoGridAdapter);
		ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
		touchHelper.attachToRecyclerView(gridView);
		return view;
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		swipeRefreshLayout.setEnabled(false);
		populateList();
	}

	private void populateList() {
		int numVideosSaved = SavedVideosDb.getSavedVideosDb().getNumVideos();
		if (numVideosSaved == 0) {
			swipeRefreshLayout.setVisibility(View.GONE);
			noSavedVideosText.setVisibility(View.VISIBLE);
		} else {
			swipeRefreshLayout.setVisibility(View.VISIBLE);
			noSavedVideosText.setVisibility(View.GONE);

			savedVideoGridAdapter.clearList();
			new AsyncTaskParallel<Void, Void, Void>() {
				@Override
				protected Void doInBackground(Void... voids) {
					new Handler(Looper.getMainLooper()).post(new Runnable() {
						@Override
						public void run() {
							savedVideoGridAdapter.appendList(SavedVideosDb.getSavedVideosDb().getSavedVideos());
						}
					});
					return null;
				}
			}.executeInParallel();
		}
	}

	public void onSelected() {
		populateList();
	}

	@Override
	public void onUpdated() {
		populateList();
	}
}
