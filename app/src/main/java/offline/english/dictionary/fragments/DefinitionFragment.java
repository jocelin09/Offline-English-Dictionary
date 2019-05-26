package offline.english.dictionary.fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;

import offline.english.dictionary.R;
import offline.english.dictionary.WordMeaningActivity;

/**
 * A simple {@link Fragment} subclass.
 */
public class DefinitionFragment extends Fragment {


    public DefinitionFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_definition, container, false);

        try {
            Context context = getActivity();
            TextView textViewD = (TextView) view.findViewById(R.id.textViewD);
            String en_Definition = ((WordMeaningActivity)context).definition;

            String definitions = en_Definition.substring(0,1).toUpperCase() + en_Definition.substring(1).toLowerCase();
            System.out.println("definitions::;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;" +definitions);


            textViewD.setText(definitions);

            if (en_Definition == null)
            {
                textViewD.setText("No definition found");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }
        return view;

    }

}
