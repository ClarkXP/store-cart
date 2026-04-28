package cl.clarkxp.store.di

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

    //Provee una instancia del cliente OkHttpClient con un interceptor de registro para depuración.
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
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