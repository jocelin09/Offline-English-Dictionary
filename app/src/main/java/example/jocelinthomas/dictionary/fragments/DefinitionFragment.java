package example.jocelinthomas.dictionary.fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.w3c.dom.Text;

import example.jocelinthomas.dictionary.R;
import example.jocelinthomas.dictionary.WordMeaningActivity;

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

        Context context = getActivity();
        TextView textViewD = (TextView) view.findViewById(R.id.textViewD);
        String en_Definition = ((WordMeaningActivity)context).definition;
        textViewD.setText(en_Definition);

        if (en_Definition == null)
        {
            textViewD.setText("No definition found");
        }
        return view;

    }

}
