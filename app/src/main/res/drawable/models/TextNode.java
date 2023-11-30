package fr.eurecom.memorable.models;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.transition.AutoTransition;
import androidx.transition.TransitionManager;

import java.io.Serializable;

import fr.eurecom.memorable.DetailsActivity;
import fr.eurecom.memorable.R;

public class TextNode extends ContentNode implements Serializable {

    private String text;

    //Layout things
    //TODO: leave UI things from here
    transient LinearLayout layout;
    transient  TextView details;

    public TextNode(int album, int day, String text) {
        super(album, day);
        this.text = text;
    }

    @Override
    public View createView(Context context) {
        //Basically here the view that will be shown in the card for the text is created
        View view = LayoutInflater.from(context).inflate(R.layout.text_node_layout,null, false);
        details = view.findViewById(R.id.text_view);
        details.setText(text);

        // Finding layout elements for enabling collapsing and expanding
        layout = view.findViewById(R.id.linear_layout);
        //Set listener for making it collapse and expand
        TextView textView = view.findViewById(R.id.author);
        textView.setOnClickListener(this::showDetails);

        return view;
    }

    private void showDetails(View view) {
        Intent intent = new Intent(view.getContext(), DetailsActivity.class);
        // Pass the current album as an extra
        intent.putExtra("node", this);
        view.getContext().startActivity(intent);
    }

    @Override
    public View createDetailedView(Context context) {
        //Basically here the view that will be shown in the card for the text is created
        View view = LayoutInflater.from(context).inflate(R.layout.text_node_details_layout,null, false);

        // Finding layout elements
        TextView detailed_details = view.findViewById(R.id.detail_content);
        detailed_details.setText(text);
        return view;
    }

    @Override
    public View createEditView(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.text_node_edit_layout, null, false);

        return null;
    }

    private void toggle(View view){
        int v = (details.getVisibility() == View.GONE)? View.VISIBLE : View.GONE;
        TransitionManager.beginDelayedTransition(layout, new AutoTransition());
        details.setVisibility(v);
    }

    public String getText() {
        return text;
    }
}
