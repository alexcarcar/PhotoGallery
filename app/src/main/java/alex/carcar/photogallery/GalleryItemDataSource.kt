package alex.carcar.photogallery

import alex.carcar.photogallery.api.ApiService
import alex.carcar.photogallery.api.NetworkState
import alex.carcar.photogallery.api.PhotoResponse
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.paging.PageKeyedDataSource

class GalleryItemDataSource : PageKeyedDataSource<Int, GalleryItem>() {

    private val flickrFetcher = FlickrFetchr()
    private val api = flickrFetcher.flickrApi
    private var retry: (() -> Any)? = null
    private val network = MutableLiveData<NetworkState>()
    private val initial = MutableLiveData<NetworkState>()
    private val apiService = ApiService(api)


    override fun loadInitial(
        params: LoadInitialParams<Int>,
        callback: LoadInitialCallback<Int, GalleryItem>
    ) {
        val currentPage = 1
        val nextPage = currentPage + 1

        //testing only
        val testingThing = params.requestedLoadSize
        Log.d("params", "$testingThing")

        apiService.fetchPhotosSync(page = currentPage, perPage = params.requestedLoadSize,
            onPrepared = {
                postInitialState(NetworkState.LOADING)
            },
            onSuccess = {
                val photoResponse: PhotoResponse? = it?.photos
                val items = photoResponse?.galleryItems ?: emptyList()
                //val items = it?.galleryItems ?: emptyList()
                Log.d("LoadInitial", "$items")
                retry = null
                postInitialState(NetworkState.LOADED)
                callback.onResult(items, null, nextPage)
            },
            onError = {
                retry = { loadInitial(params, callback) }
                postInitialState(NetworkState.error(it))
                Log.d("Error", it)
            })


    }

    override fun loadAfter(
        params: LoadParams<Int>,
        callback: LoadCallback<Int, GalleryItem>
    ) {
        val currentPage = params.key
        val nextPage = currentPage + 1
        Log.d("currentpage", "$currentPage")

        apiService.fetchPhotosAsync(page = currentPage, perPage = params.requestedLoadSize,
            onPrepared = {
                postAfterState(NetworkState.LOADING)
            },
            onSuccess = {
                val photoResponse: PhotoResponse? = it?.photos
                val items = photoResponse?.galleryItems ?: emptyList()
                //val items = it?.galleryItems ?: emptyList()
                Log.d("LoadInitial", "$items")
                retry = null
                callback.onResult(items, nextPage)
                postAfterState(NetworkState.LOADED)
            },
            onError = {
                retry = { loadAfter(params, callback) }
                postAfterState(NetworkState.error(it))
            })

    }

    override fun loadBefore(
        params: LoadParams<Int>,
        callback: LoadCallback<Int, GalleryItem>
    ) {
        //ignore this
    }

    private fun postInitialState(state: NetworkState) {
        network.postValue(state)
        initial.postValue(state)
    }

    private fun postAfterState(state: NetworkState) {
        network.postValue(state)
    }


}
