package xyz.ryabov.giphysample.ui.main

import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import xyz.ryabov.giphysample.model.ImageModel
import xyz.ryabov.giphysample.network.Gif
import xyz.ryabov.giphysample.ui.main.UiAction.LoadAction
import xyz.ryabov.giphysample.ui.main.UiAction.LoadNextPage
import xyz.ryabov.giphysample.ui.main.UiAction.ReloadAction
import xyz.ryabov.giphysample.ui.main.UiModel.Failure
import xyz.ryabov.giphysample.ui.main.UiModel.Initial
import xyz.ryabov.giphysample.ui.main.UiModel.Loading
import xyz.ryabov.giphysample.ui.main.UiModel.NewPage
import xyz.ryabov.giphysample.ui.main.UiModel.NewPageFailure
import xyz.ryabov.giphysample.util.ofType
import javax.inject.Inject

class MainViewModel @Inject constructor(private val imageModel: ImageModel) {

  private val loadImages: ObservableTransformer<LoadAction, UiModel> = ObservableTransformer { actions ->
    actions.switchMap {
      imageModel.getInitial()
          .subscribeOn(Schedulers.io())
          .map<UiModel> { Initial(it) }
          .onErrorReturn { Failure(it.message, it) }
          .observeOn(AndroidSchedulers.mainThread())
          .toObservable()
          .startWith(Loading)
    }
  }

  private val reloadImages: ObservableTransformer<ReloadAction, UiModel> = ObservableTransformer { actions ->
    actions.switchMap {
      imageModel.fetchImages()
          .subscribeOn(Schedulers.io())
          .map<UiModel> { Initial(it) }
          .onErrorReturn { Failure(it.message, it) }
          .observeOn(AndroidSchedulers.mainThread())
          .toObservable()
          .startWith(Loading)
    }
  }

  private val loadMoreImages: ObservableTransformer<LoadNextPage, UiModel> = ObservableTransformer { actions ->
    actions.switchMapSingle {
      imageModel.fetchImages(it.offset)
          .subscribeOn(Schedulers.io())
          .map<UiModel> { NewPage(it) }
          .onErrorReturn { NewPageFailure(it.message, it) }
          .observeOn(AndroidSchedulers.mainThread())
    }
  }

  fun imagesTransformer(): ObservableTransformer<UiAction, UiModel> = ObservableTransformer { actions ->
    actions.publish { shared ->
      Observable.merge<UiModel>(
          shared.ofType<LoadAction>().compose(loadImages),
          shared.ofType<ReloadAction>().compose(reloadImages),
          shared.ofType<LoadNextPage>().compose(loadMoreImages)
      )
    }
  }
}


sealed class UiAction {
  object LoadAction : UiAction()
  object ReloadAction : UiAction()
  data class LoadNextPage(val offset: Int) : UiAction()
}

sealed class UiModel {
  object Loading : UiModel()
  data class Initial(val images: List<Gif>) : UiModel()
  data class NewPage(val images: List<Gif>) : UiModel()
  data class NewPageFailure(val message: String?, val throwable: Throwable? = null) : UiModel()
  data class Failure(val message: String?, val throwable: Throwable? = null) : UiModel()
}
