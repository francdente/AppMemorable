package fr.eurecom.appmemorable.repository;

import android.util.Log;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import fr.eurecom.appmemorable.R;
import fr.eurecom.appmemorable.models.Album;
import fr.eurecom.appmemorable.models.ContentNode;
import fr.eurecom.appmemorable.models.ImageNode;
import fr.eurecom.appmemorable.models.TextNode;

//Singleton pattern to simulate database access
public class MemorableRepository {
    private static MemorableRepository instance;
    private FirebaseDatabase db;
    private DatabaseReference albumRef;
    private MutableLiveData<List<Album>> albums;
    public static MemorableRepository getInstance(){
        if (instance == null){
            instance = new MemorableRepository();
            instance.setAlbums(setAlbums());
            instance.setDb(FirebaseDatabase.getInstance("https://appmemorable-bb5f9-default-rtdb.firebaseio.com/"));
            instance.setAlbumRef(instance.getDb().getReference("albums"));
            instance.getAlbumRef().addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    // Clear the existing albums before updating with new data
                    List<Album> albums = new ArrayList<>();
                    // Check if snapshot exists and has children
                    if (snapshot.exists() && snapshot.hasChildren()) {
                        // Iterate through the children and add them to the albums list
                        for (DataSnapshot albumSnapshot : snapshot.getChildren()) {
                            Album album = albumSnapshot.getValue(Album.class);
                            if (album != null) {
                                albums.add(album);
                            }
                        }

                        // Notify observers of the change in data
                        instance.getMutableAlbums().postValue(albums);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
            instance.getAlbumRef().setValue(instance.getAlbums().getValue());
        }

        return instance;
    }

    private MutableLiveData<List<Album>> getMutableAlbums() {
        return albums;
    }

    public void addAlbumEventListener(ValueEventListener listener){
        albumRef.addValueEventListener(listener);
    }

    public LiveData<List<Album>> getAlbums(){
        return albums;
    }
    private static MutableLiveData<List<Album>> setAlbums(){
        ArrayList<Album> albums = new ArrayList<>();
        //Hard-coded initilization data
        ContentNode node1 = new TextNode(1,1,"Francesco","textnode1");
        ContentNode node2 = new TextNode(1,1, "Francesco", "textnode2");
        ContentNode node3 = new TextNode(1,1, "Francesco", "textnode3");
        ContentNode node4 = new TextNode(1,1, "Francesco", "textnode4");
        ContentNode node5 = new ImageNode(1, 1,"Francesco", "landscape1", R.drawable.image1);
        ContentNode node6 = new ImageNode(1, 1,"Francesco", "landscape2", R.drawable.image2);
        ContentNode node7 = new ImageNode(1, 1,"Francesco", "landscape3", R.drawable.image3);
        ArrayList<ContentNode> contentNodes1 = new ArrayList<ContentNode>();
        ArrayList<ContentNode> contentNodes2 = new ArrayList<ContentNode>();
        ArrayList<ContentNode> contentNodes3 = new ArrayList<ContentNode>();
        contentNodes1.add(node1);
        contentNodes1.add(node2);
        contentNodes1.add(node5);
        contentNodes2.add(node3);
        contentNodes2.add(node6);
        contentNodes3.add(node4);
        contentNodes3.add(node7);
        Album album1 = new Album(1, "TODAY", contentNodes1);
        Album album2 = new Album(2, "Album 2", contentNodes2);
        Album album3 = new Album(3, "Album 3", contentNodes3);
        albums.add(album1);
        albums.add(album2);
        albums.add(album3);
        MutableLiveData<List<Album>> data = new MutableLiveData<>();
        data.setValue(albums);
        return data;
    }


    public FirebaseDatabase getDb() {
        return db;
    }

    public void setDb(FirebaseDatabase db) {
        this.db = db;
    }

    public DatabaseReference getAlbumRef() {
        return albumRef;
    }

    public void setAlbumRef(DatabaseReference albumRef) {
        this.albumRef = albumRef;
    }

    public void setAlbums(MutableLiveData<List<Album>> albums) {
        this.albums = albums;
    }
}
