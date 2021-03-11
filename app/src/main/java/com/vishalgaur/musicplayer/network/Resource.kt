package com.vishalgaur.musicplayer.network


const val NETWORK_ERROR = "NETWORK_ERROR"
const val MEDIA_ROOT_ID = "root_id"

enum class Status { SUCCESS, LOADING, ERROR }

data class Resource<out T>(val status: Status, val data: T?, val message: String?) {

    companion object {
        fun <T> success(data: T?) = Resource(Status.SUCCESS, data, null)

        fun <T> error(message: String, data: T?) = Resource(Status.ERROR, data, message)

        fun <T> loading(data: T?) = Resource(Status.LOADING, data, null)
    }
}