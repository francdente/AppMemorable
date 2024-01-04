package fr.eurecom.appmemorable.ui.home.adapters;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import fr.eurecom.appmemorable.models.Album;

public class SharedAlbumsListViewAdapter extends ArrayAdapter<Album> {

    public SharedAlbumsListViewAdapter(@NonNull Context context, int resource) {
        super(context, resource);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        return super.getView(position, convertView, parent);
    }
}
