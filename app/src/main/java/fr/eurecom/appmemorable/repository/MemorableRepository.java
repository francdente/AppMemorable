package fr.eurecom.appmemorable.repository;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import fr.eurecom.appmemorable.models.Album;
import fr.eurecom.appmemorable.models.ConcreteAlbum;
import fr.eurecom.appmemorable.models.ConcreteNode;
import fr.eurecom.appmemorable.models.ContentNode;
import fr.eurecom.appmemorable.models.User;

//THIS IS NOT USED ANYMORE, IMPORTANT !!!!!

public class MemorableRepository {

    private static MemorableRepository instance;
    private FirebaseDatabase db;
    private DatabaseReference albumRef;
    private DatabaseReference usersRef;
    private FirebaseAuth mAuth;
    private MutableLiveData<List<Album>> albums;
    private MutableLiveData<List<User>> users;
    private MutableLiveData<Boolean> isSignedIn;
    private MutableLiveData<List<ContentNode>> contentNodes;
    private FirebaseUser user;

    public static MemorableRepository getInstance(){
        if (instance == null){
            instance = new MemorableRepository();
            instance.setDb(FirebaseDatabase.getInstance("https://appmemorable-bb5f9-default-rtdb.firebaseio.com/"));

            MutableLiveData<List<Album>> data = new MutableLiveData<>();
            data.setValue(new ArrayList<>());
            instance.setAlbums(data);

            MutableLiveData<List<User>> userData = new MutableLiveData<>();
            userData.setValue(new ArrayList<>());
            instance.setUsers(userData);

            MutableLiveData<Boolean> signedIn = new MutableLiveData<>();
            signedIn.setValue(false);
            instance.setIsSignedIn(signedIn);

            MutableLiveData<List<ContentNode>> contentNodes = new MutableLiveData<>();
            contentNodes.setValue(new ArrayList<>());
            instance.setContentNodes(contentNodes);

            //Entry point of firebase auth SDK
            instance.setmAuth(FirebaseAuth.getInstance());
        }

        return instance;
    }

    public LiveData<List<ContentNode>> getNodes(String albumKey){
        db.getReference("albumNodes/"+albumKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<ContentNode> nodes = new ArrayList<>();
                // Check if snapshot exists and has children
                if (snapshot.exists() && snapshot.hasChildren()) {
                    for (DataSnapshot nodeSnapshot : snapshot.getChildren()) {
                        ConcreteNode concreteNode = nodeSnapshot.getValue(ConcreteNode.class);
                        if (concreteNode != null) {
                            ContentNode node = concreteNode.IntoContentNode();
                            node.setId(nodeSnapshot.getKey());
                            nodes.add(node);
                        }
                    }
                }
                // Notify observers of the change in data
                contentNodes.postValue(nodes);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        return contentNodes;
    }

    public LiveData<List<Album>> getAlbums(){
        //Retrieve the reference and set the listeners only the first time the function is called
        if (albumRef == null){
            albumRef = db.getReference("albums");
            if (user == null){
                return albums;
            }
            String targetUserId = user.getUid();

            // Attach a listener
            db.getReference("userAlbums/"+targetUserId).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot userAlbumSnapshot) {
                    List<Album> albums = new ArrayList<>();
                    for (DataSnapshot albumSnapshot : userAlbumSnapshot.getChildren()) {
                        String albumId = albumSnapshot.getValue(String.class);
                        Log.e("MemorableRepository", "albumId"+albumId);
                        db.getReference("albums/"+albumId).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot albumSnapshot) {
                                ConcreteAlbum concreteAlbum = albumSnapshot.getValue(ConcreteAlbum.class);
                                if (concreteAlbum != null) {
                                    Album album = concreteAlbum.IntoAlbum();
                                    album.setId(albumSnapshot.getKey());
                                    albums.add(album);
                                }
                                MemorableRepository.this.albums.postValue(albums);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                // Handle errors if needed
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Handle errors if needed
                }
            });
        }
        return albums;
    }

    public LiveData<List<User>> getUsers(){
        if (usersRef == null){
            usersRef = db.getReference("users");
            usersRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    List<User> newUsers = new ArrayList<>();
                    // Check if snapshot exists and has children
                    if (snapshot.exists() && snapshot.hasChildren()) {
                        for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                            User user = userSnapshot.getValue(User.class);
                            if (user != null) {
                                newUsers.add(user);
                            }
                        }
                    }
                    // Notify observers of the change in data
                    users.postValue(newUsers);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
        Log.e("MemorableRepository", "users"+users.getValue().size());
        return users;
    }
    public FirebaseDatabase getDb() {
        return db;
    }



    public void addNodeToAlbum(ContentNode node, String albumKey) {
        Log.e("MemorableRepository", "adding node to album"+albumKey);
        if (user == null){
            return;
        }
        DatabaseReference nodes = db.getReference("albumNodes/"+albumKey);
        String key = nodes.push().getKey();
        node.setUser(new User(user.getEmail(), user.getDisplayName(), user.getUid()));
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
     * @Params: String albumId
     * @Params: String nodeId
     */
    public void deleteNode(String albumId, String nodeId){
        DatabaseReference node = db.getReference("albumNodes/"+albumId+"/"+nodeId);
        node.removeValue();
    }

    public void deleteAlbum(String albumId){
        DatabaseReference node = db.getReference("albums/"+albumId);
        Log.e("MemorableRepository", node.toString());
        node.removeValue();
    }

    public void addAlbum(Album album, List<String> userEmails){
        DatabaseReference nodes = db.getReference("albums");
        String key = nodes.push().getKey();
        nodes.child(key).setValue(new ConcreteAlbum(album));
        users.getValue().stream().filter(u -> userEmails.contains(u.getEmail())).forEach(u -> {
            db.getReference("userAlbums/"+u.getUid()).push().setValue(key);
        });

    }



    //Authentication logic
    public void addUser(String email, String username, String uid){
        DatabaseReference users = db.getReference("users");
        String key = users.push().getKey();
        users.child(key).setValue(new User(email, username, uid));
    }


    public void signOut(){
        mAuth.signOut();
        isSignedIn.setValue(false);
    }

    public FirebaseUser getUser() {
        return user;
    }

    /**
     * Return livedata describing if the user is currently logged in or not. Use .observe() on this to react to its changes.
     * @return
     */
    public LiveData<Boolean> getIsSignedIn(){
        return isSignedIn;
    }

    public void setIsSignedIn(MutableLiveData<Boolean> isSignedIn) {
        this.isSignedIn = isSignedIn;
    }

    public void setmAuth(FirebaseAuth mAuth) {
        this.mAuth = mAuth;
    }

    public DatabaseReference getUsersRef() {
        return usersRef;
    }

    public void setUsersRef(DatabaseReference usersRef) {
        this.usersRef = usersRef;
    }

    public void setUsers(MutableLiveData<List<User>> users) {
        this.users = users;
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

    public MutableLiveData<List<ContentNode>> getContentNodes() {
        return contentNodes;
    }

    public void setContentNodes(MutableLiveData<List<ContentNode>> contentNodes) {
        this.contentNodes = contentNodes;
    }
}
