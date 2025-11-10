package id.usecase.meetcat.di

import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import id.usecase.meetcat.data.local.TokenStorage
import id.usecase.meetcat.data.network.HttpClientFactory
import id.usecase.meetcat.data.network.NetworkMonitor
import id.usecase.meetcat.data.remote.datasource.AuthRemoteDataSource
import id.usecase.meetcat.data.remote.datasource.PostRemoteDataSource
import id.usecase.meetcat.data.remote.datasource.SearchHistoryRemoteDataSource
import id.usecase.meetcat.data.remote.datasource.UserRemoteDataSource
import id.usecase.meetcat.data.repository.AuthRepositoryImpl
import id.usecase.meetcat.data.repository.LocationRepositoryImpl
import id.usecase.meetcat.data.repository.PostRepositoryImpl
import id.usecase.meetcat.data.repository.SearchHistoryRepositoryImpl
import id.usecase.meetcat.data.repository.UserRepositoryImpl
import id.usecase.meetcat.domain.repository.AuthRepository
import id.usecase.meetcat.domain.repository.LocationRepository
import id.usecase.meetcat.domain.repository.PostRepository
import id.usecase.meetcat.domain.repository.SearchHistoryRepository
import id.usecase.meetcat.domain.repository.UserRepository
import id.usecase.meetcat.domain.usecase.auth.GetCurrentUserUseCase
import id.usecase.meetcat.domain.usecase.auth.LoginUseCase
import id.usecase.meetcat.domain.usecase.auth.LogoutUseCase
import id.usecase.meetcat.domain.usecase.auth.RegisterUseCase
import id.usecase.meetcat.domain.usecase.auth.ResetPasswordUseCase
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
import id.usecase.meetcat.domain.usecase.user.GetFollowersUseCase
import id.usecase.meetcat.domain.usecase.user.GetFollowingUseCase
import id.usecase.meetcat.domain.usecase.user.GetUserLovedItemsUseCase
import id.usecase.meetcat.domain.usecase.user.GetUserPostsUseCase
import id.usecase.meetcat.domain.usecase.user.GetUserRepliesUseCase
import id.usecase.meetcat.presentation.screen.auth.forgotpassword.ForgotPasswordViewModel
import id.usecase.meetcat.presentation.screen.auth.login.LoginViewModel
import id.usecase.meetcat.presentation.screen.auth.register.RegisterViewModel
import id.usecase.meetcat.presentation.screen.auth.splash.SplashViewModel
import id.usecase.meetcat.presentation.screen.createpost.CreatePostViewModel
import id.usecase.meetcat.presentation.screen.createreply.CreateReplyViewModel
import id.usecase.meetcat.presentation.screen.editpost.EditPostViewModel
import id.usecase.meetcat.presentation.screen.editprofile.EditProfileViewModel
import id.usecase.meetcat.presentation.screen.explore.ExploreViewModel
import id.usecase.meetcat.presentation.screen.followerslist.FollowersListViewModel
import id.usecase.meetcat.presentation.screen.followinglist.FollowingListViewModel
import id.usecase.meetcat.presentation.screen.main.MainViewModel
import id.usecase.meetcat.presentation.screen.maps.MapsViewModel
import id.usecase.meetcat.presentation.screen.mediapicker.MediaPickerViewModel
import id.usecase.meetcat.presentation.screen.postdetail.PostDetailViewModel
import id.usecase.meetcat.presentation.screen.profile.ProfileViewModel
import id.usecase.meetcat.presentation.screen.replydetail.ReplyDetailViewModel
import id.usecase.meetcat.presentation.screen.search.SearchViewModel
import id.usecase.meetcat.presentation.screen.settings.SettingsViewModel
import id.usecase.meetcat.presentation.screen.settings.about.AboutViewModel
import id.usecase.meetcat.presentation.screen.settings.account.AccountSettingsViewModel
import id.usecase.meetcat.presentation.screen.settings.privacy.PrivacySettingsViewModel
import id.usecase.meetcat.presentation.screen.userprofile.UserProfileViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val appModule = module {
    // Auth ViewModels
    viewModelOf(::SplashViewModel)
    viewModelOf(::LoginViewModel)
    viewModelOf(::RegisterViewModel)
    viewModelOf(::ForgotPasswordViewModel)

    // Main ViewModels
    viewModelOf(::ExploreViewModel)
    viewModelOf(::MainViewModel)
    viewModelOf(::SearchViewModel)
    viewModelOf(::MapsViewModel)
    viewModel {
        ProfileViewModel(
            getCurrentUserUseCase = get(),
            getUserPostsUseCase = get(),
            getUserRepliesUseCase = get(),
            getUserLovedItemsUseCase = get(),
            postRepository = get()
        )
    }

    // Profile Management ViewModels
    viewModelOf(::EditProfileViewModel)

    // Content Creation ViewModels
    viewModel {
        CreatePostViewModel(
            postRepository = get()
        )
    }
    viewModelOf(::MediaPickerViewModel)

    // Settings ViewModels
    viewModelOf(::SettingsViewModel)
    viewModelOf(::AccountSettingsViewModel)
    viewModelOf(::PrivacySettingsViewModel)
    viewModelOf(::AboutViewModel)

    // Detail screen ViewModels with parameters
    viewModel { (postId: String) ->
        PostDetailViewModel(
            postRepository = get(),
            postId = postId
        )
    }
    viewModel { (replyId: String) ->
        ReplyDetailViewModel(
            postRepository = get(),
            replyId = replyId
        )
    }
    viewModel { (userId: String) ->
        UserProfileViewModel(
            userId = userId,
            getUserPostsUseCase = get(),
            getUserRepliesUseCase = get(),
            getUserLovedItemsUseCase = get(),
            postRepository = get(),
            userRepository = get()
        )
    }
    viewModel { (userId: String) ->
        FollowersListViewModel(
            userId = userId,
            getFollowersUseCase = get(),
            userRepository = get()
        )
    }
    viewModel { (userId: String) ->
        FollowingListViewModel(
            userId = userId,
            getFollowingUseCase = get(),
            userRepository = get()
        )
    }
    viewModel { (postId: String) ->
        CreateReplyViewModel(
            postRepository = get(),
            postId = postId
        )
    }
    viewModel { (postId: String) ->
        EditPostViewModel(
            postRepository = get(),
            postId = postId
        )
    }
}

val domainModule = module {
    // Auth use cases
    factoryOf(::LoginUseCase)
    factoryOf(::RegisterUseCase)
    factoryOf(::ResetPasswordUseCase)
    factoryOf(::LogoutUseCase)
    factoryOf(::GetCurrentUserUseCase)

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

    // User use cases
    factoryOf(::GetUserPostsUseCase)
    factoryOf(::GetUserRepliesUseCase)
    factoryOf(::GetUserLovedItemsUseCase)
    factoryOf(::GetFollowersUseCase)
    factoryOf(::GetFollowingUseCase)
}

val dataModule = module {
    // Token Storage
    single { TokenStorage(androidContext()) }

    // Network Monitor
    single { NetworkMonitor(androidContext()) }

    // HTTP Client with token provider
    single {
        HttpClientFactory.create(
            tokenProvider = { get<TokenStorage>().getToken() }
        )
    }

    // Remote Data Sources
    single { AuthRemoteDataSource(get()) }
    single { PostRemoteDataSource(httpClient = get(), context = androidContext()) }
    single { UserRemoteDataSource(get()) }
    single { SearchHistoryRemoteDataSource(get()) }

    // Repositories - Real implementations with API integration
    single<AuthRepository> {
        AuthRepositoryImpl(
            remoteDataSource = get(),
            tokenStorage = get()
        )
    }

    single<PostRepository> {
        PostRepositoryImpl(
            remoteDataSource = get()
        )
    }

    single<UserRepository> {
        UserRepositoryImpl(
            remoteDataSource = get()
        )
    }

    single<SearchHistoryRepository> {
        SearchHistoryRepositoryImpl(
            remoteDataSource = get()
        )
    }

    // Location Services - Real implementation with Android dependencies
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
