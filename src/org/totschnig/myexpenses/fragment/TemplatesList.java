/*   This file is part of My Expenses.
 *   My Expenses is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   My Expenses is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with My Expenses.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.totschnig.myexpenses.fragment;

import org.totschnig.myexpenses.R;
import org.totschnig.myexpenses.provider.DatabaseConstants;
import org.totschnig.myexpenses.provider.TransactionProvider;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import org.totschnig.myexpenses.ui.SimpleCursorTreeAdapter;

import com.actionbarsherlock.app.SherlockFragment;

import android.widget.ExpandableListView.OnChildClickListener;

public class TemplatesList extends SherlockFragment implements LoaderManager.LoaderCallbacks<Cursor> {
  private MyExpandableListAdapter mAdapter;
  int mGroupIdColumnIndex;
  private LoaderManager mManager;
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View v = inflater.inflate(R.layout.templates_list, null, false);
    ExpandableListView lv = (ExpandableListView) v.findViewById(R.id.list);
    mManager = getLoaderManager();
    mManager.initLoader(-1, null, this);
    mAdapter = new MyExpandableListAdapter(getActivity(),
        null,
        android.R.layout.simple_expandable_list_item_1,
        android.R.layout.simple_expandable_list_item_1,
        new String[]{"label"},
        new int[] {android.R.id.text1},
        new String[] {"title"},
        new int[] {android.R.id.text1});
    lv.setAdapter(mAdapter);
    lv.setEmptyView(v.findViewById(R.id.empty));
    //requires using activity (ManageTemplates) to implement OnChildClickListener
    lv.setOnChildClickListener((OnChildClickListener) getActivity());
    registerForContextMenu(lv);
    return v;
  }
  public class MyExpandableListAdapter extends SimpleCursorTreeAdapter {
    
    public MyExpandableListAdapter(Context context,Cursor cursor, int groupLayout,
            int childLayout, String[] groupFrom, int[] groupTo, String[] childrenFrom,
            int[] childrenTo) {
        super(context, cursor, groupLayout, groupFrom, groupTo, childLayout, childrenFrom,
                childrenTo);
    }
    /* (non-Javadoc)
     * returns a cursor with the subcategories for the group
     * @see android.widget.CursorTreeAdapter#getChildrenCursor(android.database.Cursor)
     */
    @Override
    protected Cursor getChildrenCursor(Cursor groupCursor) {
        // Given the group, we return a cursor for all the children within that group
      long accountId = groupCursor.getLong(groupCursor.getColumnIndexOrThrow(DatabaseConstants.KEY_ROWID));Bundle bundle = new Bundle();
      bundle.putLong("account_id", accountId);
      int groupPos = groupCursor.getPosition();
      if (mManager.getLoader(groupPos) != null && !mManager.getLoader(groupPos).isReset()) {
        try {
          mManager.restartLoader(groupPos, bundle, TemplatesList.this);
        } catch (NullPointerException e) {
          // a NPE is thrown in the following scenario:
          //1)open a group
          //2)orientation change
          //3)open the same group again
          //in this scenario getChildrenCursor is called twice, second time leads to error
          //maybe it is trying to close the group that had been kept open before the orientation change
          e.printStackTrace();
        }
      } else {
        mManager.initLoader(groupPos, bundle, TemplatesList.this);
      }
      return null;
    }
  }
  @Override
  public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
    if (id == -1)
      return new CursorLoader(getActivity(),TransactionProvider.ACCOUNTS_URI, null, null,null, null);
    else {
      long accountId = bundle.getLong("account_id");
      return new CursorLoader(getActivity(),TransactionProvider.TEMPLATES_URI, null,
          "account_id = ?",new String[] { String.valueOf(accountId) }, "usages DESC");
    }
  }
  @Override
  public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
    int id = loader.getId();
    if (id == -1)
      mAdapter.setGroupCursor(data);
    else
      mAdapter.setChildrenCursor(id, data);
    
  }
  @Override
  public void onLoaderReset(Loader<Cursor> loader) {
    if (loader.getId() == -1)
      mAdapter.setGroupCursor(null);
  }
}
