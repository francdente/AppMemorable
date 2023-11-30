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
import fr.eurecom.appmemorable.models.AudioNode;
import fr.eurecom.appmemorable.models.ContentNode;
import fr.eurecom.appmemorable.models.ImageNode;
import fr.eurecom.appmemorable.models.TextNode;

class ConcreteNode{
    private String text, author;
    private int image=-1, day, album;
    public ConcreteNode(){
    }
    public ConcreteNode(ContentNode node){
        if (node instanceof TextNode){
            this.text = ((TextNode) node).getText();
            this.author = ((TextNode) node).getAuthor();
            this.day = ((TextNode) node).getDay();
            this.album = ((TextNode) node).getAlbum();
        }
        else if (node instanceof AudioNode){
            this.text = ((AudioNode) node).getText();
            this.author = ((AudioNode) node).getAuthor();
            this.day = ((AudioNode) node).getDay();
            this.album = ((AudioNode) node).getAlbum();
        }
        else if (node instanceof ImageNode){
            this.text = ((ImageNode) node).getText();
            this.author = ((ImageNode) node).getAuthor();
            this.day = ((ImageNode) node).getDay();
            this.album = ((ImageNode) node).getAlbum();
            this.image = ((ImageNode) node).getImage();
        }

    }


    public ContentNode IntoContentNode() {
        if (image == -1){
            return new TextNode(album, day, author, text);
        }
        else {
            return new ImageNode(album, day, author, text, image);
        }
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public int getAlbum() {
        return album;
    }

    public void setAlbum(int album) {
        this.album = album;
    }
}
class ConcreteAlbum {
    List<ConcreteNode> concreteNodes = new ArrayList<>();
    private int id;
    private String title;
    public ConcreteAlbum() {
    }
    public ConcreteAlbum(Album album){
        this.id = album.getId();
        this.title = album.getTitle();
        for (ContentNode node: album.getNodes()){
            concreteNodes.add(new ConcreteNode(node));
        }
    }

    public Album IntoAlbum(){
        List<ContentNode> nodes = new ArrayList<>();
        for (ConcreteNode node : concreteNodes){
            nodes.add(node.IntoContentNode());
        }
        return new Album(this.id, this.title, nodes);
    }

    public List<ConcreteNode> getConcreteNodes() {
        return concreteNodes;
    }

    public void setConcreteNodes(List<ConcreteNode> concreteNodes) {
        this.concreteNodes = concreteNodes;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}

public class MemorableRepository {

    private static MemorableRepository instance;
    private FirebaseDatabase db;
    private DatabaseReference albumRef;
    private MutableLiveData<List<Album>> albums;

    public static MemorableRepository getInstance(){
        if (instance == null){
            instance = new MemorableRepository();
            instance.setDb(FirebaseDatabase.getInstance("https://appmemorable-bb5f9-default-rtdb.firebaseio.com/"));
            MutableLiveData<List<Album>> data = new MutableLiveData<>();
            data.setValue(new ArrayList<>());
            instance.setAlbums(data);
        }

        return instance;
    }

    private MutableLiveData<List<Album>> getMutableAlbums() {
        return albums;
    }

    public LiveData<List<Album>> getAlbums(){
        //Retrieve the reference and set the listeners only the first time the function is called
        if (albumRef == null){
            albumRef = db.getReference("albums");
            //Initial fetch of the data + Set listener for listening to changes of data
            albumRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    // Clear the existing albums before updating with new data
                    List<Album> newAlbums = new ArrayList<>();
                    // Check if snapshot exists and has children
                    if (snapshot.exists() && snapshot.hasChildren()) {
                        // Iterate through the children and add them to the albums list
                        for (DataSnapshot albumSnapshot : snapshot.getChildren()) {
                            ConcreteAlbum concreteAlbum = albumSnapshot.getValue(ConcreteAlbum.class);
                            if (concreteAlbum != null) {
                                newAlbums.add(concreteAlbum.IntoAlbum());
                            }
                        }

                        // Notify observers of the change in data
                        albums.postValue(newAlbums);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
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
