package offline.english.dictionary;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;

import java.util.ArrayList;

/**
 * Created by english on 16/05/material.
 */

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {
    private Context context;
    private ArrayList<History> histories;

    public HistoryAdapter(Context context, ArrayList<History> histories) {
        this.context = context;
        this.histories = histories;
    }

    public class HistoryViewHolder extends RecyclerView.ViewHolder {
        TextView textViewWord;
        TextView textViewDef;

        public HistoryViewHolder(View itemView)
        {
            super(itemView);
            textViewWord = (TextView) itemView.findViewById(R.id.en_word);
            textViewDef = (TextView) itemView.findViewById(R.id.en_def);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        int position = getAdapterPosition();

                        String text = histories.get(position).getEn_word();

                        Intent intent = new Intent(context,WordMeaningActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putString("en_word",text);
                        intent.putExtras(bundle);
                        context.startActivity(intent);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Crashlytics.logException(e);
                    }

                }
            });

        }
    }


    @NonNull
    @Override
    public HistoryAdapter.HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.history_item_layout,parent,false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryAdapter.HistoryViewHolder holder, int position) {
        holder.textViewWord.setText(histories.get(position).getEn_word());
        holder.textViewDef.setText(histories.get(position).getEn_def());

    }

    @Override
    public int getItemCount() {
        return histories.size();
    }


}
