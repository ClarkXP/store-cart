package cl.clarkxp.store.di

import cl.clarkxp.store.base.BuildConfig
import cl.clarkxp.store.core.constants.AppConstants
import cl.clarkxp.store.data.remote.api.FakeStoreApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val loggingLevel = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.NONE
        }

        return OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = loggingLevel
            }).build()
    }

    //Provee una instancia de Retrofit para interactuar con la API.
    @Provides
    @Singleton
    fun provideFakeStoreApi(client: OkHttpClient): FakeStoreApi {
        return Retrofit.Builder()
            .baseUrl(AppConstants.BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build().create(FakeStoreApi::class.java)
    }
}