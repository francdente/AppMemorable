//package fr.eurecom.appmemorable.ui.home;
//
//import android.util.Log;
//
//import androidx.lifecycle.LiveData;
//import androidx.lifecycle.MutableLiveData;
//import androidx.lifecycle.ViewModel;
//
//import java.util.List;
//
//import fr.eurecom.appmemorable.models.Album;
//import fr.eurecom.appmemorable.models.ContentNode;
//import fr.eurecom.appmemorable.repository.MemorableRepository;
//
//public class HomeViewModel extends ViewModel {
//
//    private MutableLiveData<List<Album>> mAlbums;
//    private MemorableRepository mRepo;
//
//    public void init(){
//        if (mAlbums != null){
//            return;
//        }
//        Log.e("HomeViewModel", "Inside init");
//        mRepo = MemorableRepository.getInstance();
//        mAlbums = mRepo.getAlbums();
//    }
//
//    public LiveData<List<Album>> getAlbums(){
//        return mAlbums;
//    }
//
//    public void addNode(ContentNode node, int position){
//        List<Album> currentAlbums = mAlbums.getValue();
//        currentAlbums.get(position).addNode(node);
//        mAlbums.postValue(currentAlbums);
//    }
//}