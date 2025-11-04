package id.usecase.meetcat.di

import android.content.Context
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import id.usecase.meetcat.data.repository.FakePostRepository
import id.usecase.meetcat.data.repository.FakeSearchHistoryRepository
import id.usecase.meetcat.data.repository.LocationRepositoryImpl
import id.usecase.meetcat.domain.repository.LocationRepository
import id.usecase.meetcat.domain.repository.PostRepository
import id.usecase.meetcat.domain.repository.SearchHistoryRepository
import id.usecase.meetcat.domain.usecase.location.GetCurrentLocationUseCase
import id.usecase.meetcat.domain.usecase.location.HasLocationPermissionUseCase
import id.usecase.meetcat.domain.usecase.post.GetExploreFeedUseCase
import id.usecase.meetcat.domain.usecase.post.GetNearbyPostsUseCase
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
import id.usecase.meetcat.presentation.screen.maps.MapsViewModel
import id.usecase.meetcat.presentation.screen.search.SearchViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val appModule = module {
    viewModelOf(::ExploreViewModel)
    viewModelOf(::MainViewModel)
    viewModelOf(::SearchViewModel)
    viewModelOf(::MapsViewModel)
}

val domainModule = module {
    // Post use cases
    factoryOf(::GetExploreFeedUseCase)
    factoryOf(::GetRandomPostsUseCase)
    factoryOf(::GetNearbyPostsUseCase)
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

    // Location use cases
    factoryOf(::GetCurrentLocationUseCase)
    factoryOf(::HasLocationPermissionUseCase)
}

val dataModule = module {
    singleOf(::FakePostRepository) bind PostRepository::class
    singleOf(::FakeSearchHistoryRepository) bind SearchHistoryRepository::class

    // Location Services
    single<FusedLocationProviderClient> {
        LocationServices.getFusedLocationProviderClient(androidContext())
    }
    single<LocationRepository> {
        LocationRepositoryImpl(
            context = androidContext(),
            fusedLocationClient = get()
        )
    }
}
