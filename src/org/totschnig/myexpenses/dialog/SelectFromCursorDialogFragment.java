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

package org.totschnig.myexpenses.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.widget.SimpleCursorAdapter;

public class SelectFromCursorDialogFragment extends DialogFragment implements OnClickListener {
  SimpleCursorAdapter mAdapter;
  public interface SelectFromCursorDialogListener {
    Cursor getCursor(int cursorId, String tag);
    void onItemSelected(Bundle args);
}
  /**
   * @param args required keys: uri,selection, column
   * @return
   */
  public static final SelectFromCursorDialogFragment newInstance(Bundle args) {
    SelectFromCursorDialogFragment dialogFragment = new SelectFromCursorDialogFragment();
    dialogFragment.setArguments(args);
    return dialogFragment;
  }
  //KEY_ROWID + " != " + getArguments().getLong("fromAccountId")
  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    Context ctx = getActivity();
    Bundle bundle = getArguments();
    String column = bundle.getString("column");
    Cursor c = ((SelectFromCursorDialogListener) ctx).getCursor(bundle.getInt("cursorId"),getTag());
    mAdapter = new SimpleCursorAdapter(ctx, android.R.layout.select_dialog_singlechoice,
        c, new String[]{column}, new int[]{android.R.id.text1},0);
    return new AlertDialog.Builder(ctx)
      .setTitle(bundle.getString("dialogTitle"))
      .setAdapter(mAdapter,this)
      .create();
  }
  @Override
  public void onClick(DialogInterface dialog, int which) {
    SelectFromCursorDialogListener activity = (SelectFromCursorDialogListener) getActivity();
    Bundle args = getArguments();
    args.putLong("result", ((AlertDialog) dialog).getListView().getItemIdAtPosition(which));
    activity.onItemSelected(args);
    dismiss();
  }
  public void setCursor(Cursor c) {
    mAdapter.swapCursor(c);
  }
}