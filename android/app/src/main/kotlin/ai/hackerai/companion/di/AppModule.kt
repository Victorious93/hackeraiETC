package ai.hackerai.companion.di

import ai.hackerai.companion.llm.HttpLocalLlmProvider
import ai.hackerai.companion.llm.LocalLlmProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {
    @Binds
    @Singleton
    abstract fun bindLocalLlmProvider(impl: HttpLocalLlmProvider): LocalLlmProvider
}
