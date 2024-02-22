package org.example

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.IOException

object NetworkUtil{
    fun uploadFile(path: String, url: String) : Int {
        val client = OkHttpClient()
        val file = File(path)

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "file", // 与接口中期望的文件参数名称相匹配
                file.name,
                file.asRequestBody("application/octet-stream".toMediaTypeOrNull())
            )
            .build()

        val request = Request.Builder()
            .url(url) // 接口的URL
            .post(requestBody)
            .build()

        val response : Response
        client.newCall(request).execute().use { resp ->
            response = resp
        }

        return response.code
    }
}