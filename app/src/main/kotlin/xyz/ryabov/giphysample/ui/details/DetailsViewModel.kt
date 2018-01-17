package xyz.ryabov.giphysample.ui.details

import io.reactivex.ObservableTransformer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import xyz.ryabov.giphysample.model.ImageModel
import xyz.ryabov.giphysample.network.Gif
import xyz.ryabov.giphysample.ui.details.UiModel.Failure
import xyz.ryabov.giphysample.ui.details.UiModel.Success
import javax.inject.Inject

class DetailsViewModel @Inject constructor(private val imageModel: ImageModel) {

  fun loadImageById(): ObservableTransformer<UiAction.LoadAction, UiModel> = ObservableTransformer { actions ->
    actions.switchMapSingle {
      imageModel.getById(it.id)
          .subscribeOn(Schedulers.io())
          .map<UiModel> { Success(it) }
          .onErrorReturn { Failure(it.message, it) }
          .observeOn(AndroidSchedulers.mainThread())
    }
  }
}

sealed class UiAction {
  data class LoadAction(val id: String) : UiAction()
}

sealed class UiModel {
  data class Success(val image: Gif) : UiModel()
  data class Failure(val message: String?, val throwable: Throwable? = null) : UiModel()
}
