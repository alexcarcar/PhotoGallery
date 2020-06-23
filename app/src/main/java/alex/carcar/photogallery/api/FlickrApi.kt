package alex.carcar.photogallery.api

import retrofit2.Call
import retrofit2.http.GET

interface FlickrApi {

    @GET(
        "/services/rest/?method=flickr.interestingness.getList" +
                "&api_key=77299b0b96772b661f4950ee96f5f959" +
                "&format=json" +
                "&nojsoncallback=1" +
                "&extras=url_s"
    )
    fun fetchPhotos(): Call<PhotoResponse>
}