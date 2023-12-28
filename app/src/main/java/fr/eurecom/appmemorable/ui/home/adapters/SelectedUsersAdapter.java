package fr.eurecom.appmemorable.ui.home.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import fr.eurecom.appmemorable.databinding.SelectedUserItemBinding;
import fr.eurecom.appmemorable.models.User;

public class SelectedUsersAdapter extends ArrayAdapter<User> {
    private List<User> selectedUsers;
    private ArrayAdapter<User> dropDownAdapter;
    public SelectedUsersAdapter(@NonNull Context context, List<User> users, ArrayAdapter<User> dropDownAdapter) {
        super(context, 0, users);
        this.dropDownAdapter = dropDownAdapter;
        this.selectedUsers = users;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        User user = getItem(position);
        SelectedUserItemBinding selectedUserItemBinding = SelectedUserItemBinding.inflate(LayoutInflater.from(getContext()));
        selectedUserItemBinding.textViewUser.setText(user.getName());
        //When user is removed from selected, it must be added back to the ones I can add
        selectedUserItemBinding.imageButtonCancel.setOnClickListener(v -> {
            this.remove(user);
            notifyDataSetChanged();
            dropDownAdapter.add(user);
            dropDownAdapter.notifyDataSetChanged();
        });
        convertView = selectedUserItemBinding.getRoot();
        return convertView;
    }
}
