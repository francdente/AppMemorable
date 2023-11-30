package fr.eurecom.appmemorable.repository;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import fr.eurecom.appmemorable.models.Album;
import fr.eurecom.appmemorable.models.AudioNode;
import fr.eurecom.appmemorable.models.ContentNode;
import fr.eurecom.appmemorable.models.ImageNode;
import fr.eurecom.appmemorable.models.TextNode;


//support classes used for serializing objects when sending and receiving data from firebase (because ContentNode is an abstract class)
class ConcreteNode{
    private String text, author, day, album;
    private int image=-1;
    public ConcreteNode(){
    }
    public ConcreteNode(ContentNode node){
        if (node instanceof TextNode){
            this.text = ((TextNode) node).getText();
            this.author = ((TextNode) node).getAuthor();
            this.day = node.getDay();
            this.album = node.getAlbum();
        }
        else if (node instanceof AudioNode){
            this.text = ((AudioNode) node).getText();
            this.author = ((AudioNode) node).getAuthor();
            this.day = node.getDay();
            this.album = node.getAlbum();
        }
        else if (node instanceof ImageNode){
            this.text = ((ImageNode) node).getText();
            this.author = ((ImageNode) node).getAuthor();
            this.day = node.getDay();
            this.album = node.getAlbum();
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

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }
}
class ConcreteAlbum {
    HashMap<String, ConcreteNode> concreteNodes = new HashMap<>();
    private String id;
    private String title;
    public ConcreteAlbum() {
    }
    public ConcreteAlbum(Album album){
        this.id = album.getId();
        this.title = album.getTitle();
        for (Map.Entry<String, ContentNode> entry : album.getNodes().entrySet()) {
            String key = entry.getKey();
            ContentNode node = entry.getValue();
            concreteNodes.put(key, new ConcreteNode(node));
        }
    }

    public Album IntoAlbum(){
        HashMap<String, ContentNode> nodes = new HashMap<>();
        for (Map.Entry<String, ConcreteNode> entry : concreteNodes.entrySet()) {
            String key = entry.getKey();
            ContentNode node = entry.getValue().IntoContentNode();
            node.setId(key);
            nodes.put(key, node);
        }
        Album album = new Album(this.title, nodes);
        album.setId(this.id);
        return album;
    }

    public HashMap<String, ConcreteNode> getConcreteNodes() {
        return concreteNodes;
    }

    public void setConcreteNodes(HashMap<String, ConcreteNode> concreteNodes) {
        this.concreteNodes = concreteNodes;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
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
            /*List<ConcreteAlbum> estart_albums = setAlbums();
            for (ConcreteAlbum album : estart_albums){
                String key = albumRef.push().getKey();
                albumRef.child(key).setValue(album);
            } this was used to initialize the database */

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
                                concreteAlbum.setId(albumSnapshot.getKey());
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

    private static List<ConcreteAlbum> setAlbums(){
        ArrayList<Album> albums = new ArrayList<>();
        //Hard-coded initilization data
        albums.add(new Album( "First Album", new HashMap<>()));
        albums.add(new Album( "Second Album", new HashMap<>()));
        albums.add(new Album( "Third Album", new HashMap<>()));
        albums.add(new Album( "Fourth Album", new HashMap<>()));
        albums.add(new Album( "Fifth Album", new HashMap<>()));
        albums.add(new Album( "Sixth Album", new HashMap<>()));;
        // add empyt concreteNodes
        return albums.stream().map(ConcreteAlbum::new).collect(Collectors.toList());
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

    public void addNodeToAlbum(ContentNode node, String albumKey) {
        Log.e("MemorableRepository", "adding node to album"+albumKey);
        DatabaseReference nodes = db.getReference("albums/"+albumKey+"/concreteNodes");
        String key = nodes.push().getKey();
        nodes.child(key).setValue(new ConcreteNode(node));
    }
    /**
    Update the node in the database, it detects the node using it's unique identifier generated by the db.

     The node is updated using the content of the ContentNode object passed as parameter
     @Params: ContentNode node - the updated version of the node
     @Params: String key - the unique identifier of the node to be updated
     */
    public void updateNode(ContentNode node, String key){
        DatabaseReference nodes = db.getReference("albums/"+node.getAlbum()+"/concreteNodes");
        nodes.child(key).setValue(new ConcreteNode(node));
    }

    /**
     * Delete the node in the database, it detects the node using it's unique identifier generated by the db.
     * @Params: String key
     */
    public void deleteNode(String albumId, String nodeId){
        DatabaseReference node = db.getReference("albums/"+albumId+"/concreteNodes/"+nodeId);
        Log.e("MemorableRepository", node.toString());
        node.removeValue();
    }

    public void addAlbum(Album album){
        DatabaseReference nodes = db.getReference("albums");
        String key = nodes.push().getKey();
        nodes.child(key).setValue(new ConcreteAlbum(album));
    }
}
