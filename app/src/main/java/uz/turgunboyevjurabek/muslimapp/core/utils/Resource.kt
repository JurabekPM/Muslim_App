package uz.turgunboyevjurabek.muslimapp.core.utils

import androidx.annotation.StringRes

data class UiMessage(@StringRes val resource: Int, val argument: String? = null)

data class Resource<out T>(val status: Status, val data:T?, val message:UiMessage?){

    companion object{
        fun <T>success(data:T): Resource<T> {
            return Resource(Status.SUCCESS, data, null)
        }
        fun <T>error(message: UiMessage?): Resource<T> {
            return Resource(Status.ERROR, null, message)
        }
        fun <T>loading(message: UiMessage?): Resource<T> {
            return Resource(Status.LOADING, null, message)
        }
    }
}
