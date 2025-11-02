package id.usecase.meetcat.di

import id.usecase.meetcat.data.repository.FakePostRepository
import id.usecase.meetcat.data.repository.FakeSearchHistoryRepository
import id.usecase.meetcat.domain.repository.PostRepository
import id.usecase.meetcat.domain.repository.SearchHistoryRepository
import id.usecase.meetcat.domain.usecase.post.GetExploreFeedUseCase
import id.usecase.meetcat.domain.usecase.post.GetRandomPostsUseCase
import id.usecase.meetcat.domain.usecase.post.LovePostUseCase
import id.usecase.meetcat.domain.usecase.post.LoveReplyUseCase
import id.usecase.meetcat.domain.usecase.post.SearchPostsUseCase
import id.usecase.meetcat.domain.usecase.post.UnlovePostUseCase
import id.usecase.meetcat.domain.usecase.post.UnloveReplyUseCase
import id.usecase.meetcat.domain.usecase.search.ClearSearchHistoryUseCase
import id.usecase.meetcat.domain.usecase.search.DeleteSearchQueryUseCase
import id.usecase.meetcat.domain.usecase.search.GetSearchHistoryUseCase
import id.usecase.meetcat.domain.usecase.search.SaveSearchQueryUseCase
import id.usecase.meetcat.presentation.screen.explore.ExploreViewModel
import id.usecase.meetcat.presentation.screen.main.MainViewModel
import id.usecase.meetcat.presentation.screen.search.SearchViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val appModule = module {
    viewModelOf(::ExploreViewModel)
    viewModelOf(::MainViewModel)
    viewModelOf(::SearchViewModel)
}

val domainModule = module {
    // Post use cases
    factoryOf(::GetExploreFeedUseCase)
    factoryOf(::GetRandomPostsUseCase)
    factoryOf(::SearchPostsUseCase)
    factoryOf(::LovePostUseCase)
    factoryOf(::UnlovePostUseCase)
    factoryOf(::LoveReplyUseCase)
    factoryOf(::UnloveReplyUseCase)

    // Search use cases
    factoryOf(::GetSearchHistoryUseCase)
    factoryOf(::SaveSearchQueryUseCase)
    factoryOf(::DeleteSearchQueryUseCase)
    factoryOf(::ClearSearchHistoryUseCase)
}

val dataModule = module {
    singleOf(::FakePostRepository) bind PostRepository::class
    singleOf(::FakeSearchHistoryRepository) bind SearchHistoryRepository::class
}
