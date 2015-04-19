package com.friscotap.mugclub;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.friscotap.core.BeerDataSource;
import com.friscotap.core.R;

public class ConfirmationFragment extends DialogFragment {
    private ArrayList<MugClubBeer> mBeers;
    private ConfirmationAdapter mAdapter;
    private ListView mListView;

    public static final ConfirmationFragment newInstance(ArrayList<MugClubBeer> arg) {
        ConfirmationFragment f = new ConfirmationFragment();
        Bundle bdl = new Bundle(1);

        // Set bundle arguments to pass in
        bdl.putSerializable("beers", arg);
        f.setArguments(bdl);

        return f;
    }

    public ConfirmationFragment() {
        // Required empty constructor
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Grab the beers to be verified.
        mBeers = (ArrayList<MugClubBeer>) getArguments().getSerializable("beers");
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View v = getActivity()
                .getLayoutInflater()
                .inflate(R.layout.fragment_confirmation, null);

        // Setup the array list and adapter
        mListView = (ListView) v.findViewById(R.id.mugclub_code_list_view);
        mAdapter = new ConfirmationAdapter(
                getActivity(),
                R.layout.mugclub_confirm_item,
                mBeers
        );

        mListView.setAdapter(mAdapter);

        // Now build the dialog box to be displayed
        AlertDialog.Builder dialogBuilder =
                new AlertDialog.Builder(getActivity(), AlertDialog.THEME_HOLO_DARK);
        dialogBuilder.setTitle("Mug Club Book Confirmation");
        dialogBuilder.setView(v);

        // Setup the Ok and Cancel buttons
        dialogBuilder.setPositiveButton("Confirm", new MugBeerConfirm());
        dialogBuilder.setNegativeButton("Cancel", new MugBeerCancel());

        return dialogBuilder.create();
    }

    private class MugBeerConfirm implements DialogInterface.OnClickListener {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            int remoteCode = 0;

            // TODO: We need to start an async task to call out for the
            // confirmation code remotely
            //remoteCode = RetrieveRemoteCodeTask().execute.get();

            // Get the value from the EditText that contains the passcode
            EditText passCodeView = (EditText) getActivity().findViewById(R.id.mugclub_edit_code);
            String strInt = passCodeView.getText().toString();
            int passCode = Integer.parseInt(strInt);

            if(passCode == 9181985) {
                BeerDataSource mSource = new BeerDataSource(getActivity());
                mSource.open();

                // Default value
                for(int i = 0; i < mBeers.size(); i++) {
                    MugClubBeer beer = mBeers.get(i);

                    beer.setDate(new Date());
                    beer.setConfirmed(true);

                    // Update the entry in the database
                    mSource.updateMugBeer(beer);
                }

                mSource.close();
            }
            else if(passCode == remoteCode) {
                // Remote value successfully retrieved
            }
        }
    }

    private class MugBeerCancel implements DialogInterface.OnClickListener {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            dialog.cancel();

        }
    }

    private class ConfirmationAdapter extends ArrayAdapter<MugClubBeer> {
        private ArrayList<MugClubBeer> items;

        public ConfirmationAdapter(Context context, int resource, List<MugClubBeer> objects) {
            super(context, resource, objects);
            this.items = (ArrayList<MugClubBeer>) objects;
        }

        @Override
        public View getView(int pos, View convertView, ViewGroup parent) {
            LayoutInflater vi = LayoutInflater.from(getContext());
            View v = vi.inflate(R.layout.mugclub_confirm_item, null);

            MugClubBeer b = items.get(pos);

            if(b != null) {
                // Setup the view for this specific beer
                //CheckBox chBox = (CheckBox) v.findViewById(R.id.mugclub_confirm_checkbox);
                TextView tBox = (TextView) v.findViewById(R.id.mugclub_confirm_beername);
                tBox.setText(b.getName());
            }

            return v;
        }
    }
}
