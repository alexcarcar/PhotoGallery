package alex.carcar.photogallery


import androidx.lifecycle.ViewModel
import androidx.paging.Config
import androidx.paging.toLiveData


class PhotoGalleryViewModel: ViewModel(){


    /** Chapter 24 Challenge 2 **/
    /** Don't forget to add paging dependencies to build.grad.e **/

    private val myPagingConfig = Config(
        pageSize = 100,
        prefetchDistance = 500,
        enablePlaceholders = true
    )



    private val dataSourceFactory = GalleryItemDataSourceFactory()
    val galleryItemList = dataSourceFactory.toLiveData(myPagingConfig)

    fun refresh(){
        dataSourceFactory.sourceLiveData.value?.invalidate()

    }





}