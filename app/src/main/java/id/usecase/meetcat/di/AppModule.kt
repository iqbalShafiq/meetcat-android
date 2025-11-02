package id.usecase.meetcat.di

import id.usecase.meetcat.data.repository.FakePostRepository
import id.usecase.meetcat.domain.repository.PostRepository
import id.usecase.meetcat.presentation.screen.explore.ExploreViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val appModule = module {
    viewModelOf(::ExploreViewModel)
}

val dataModule = module {
    singleOf(::FakePostRepository) bind PostRepository::class
}
