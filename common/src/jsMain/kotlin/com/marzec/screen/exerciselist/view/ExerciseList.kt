package com.marzec.screen.exerciselist.view

import com.marzec.common.useStateFlow
import com.marzec.screen.exerciselist.model.ExerciseListUiMapper
import com.marzec.screen.exerciselist.model.ExercisesListActions
import com.marzec.screen.exerciselist.model.defaultState
import com.marzec.screen.exerciselist.model.exerciseListStore
import com.marzec.views.HeaderDelegate
import com.marzec.views.base.ReactRenderer
import com.marzec.views.base.ViewItem
import com.marzec.views.checkbox.CheckboxDelegate
import com.marzec.views.error.ErrorDelegate
import com.marzec.views.exerciserowview.ExerciseDelegate
import com.marzec.views.loading.LoadingDelegate
import com.marzec.views.textinput.TextInputDelegate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import react.RProps
import react.functionalComponent
import react.useEffect


@ExperimentalCoroutinesApi
val ExerciseList = functionalComponent<RProps> { _ ->
    val state = useStateFlow(exerciseListStore.state, defaultState)

    useEffect(emptyList()) {
        exerciseListStore.sendAction(ExercisesListActions.Initialization)
    }

    val views: List<ViewItem> = ExerciseListUiMapper.map(state)

    ReactRenderer()
        .apply { builder = this@functionalComponent }
        .add(
            CheckboxDelegate { id: String ->
                exerciseListStore.sendAction(ExercisesListActions.OnFilterCheckedChange(filterId = id))
            }
        )
        .add(TextInputDelegate { _: String, text: String ->
            exerciseListStore.sendAction(ExercisesListActions.OnSearchTextChanged(text))

        })
        .add(ErrorDelegate())
        .add(ExerciseDelegate())
        .add(LoadingDelegate())
        .add(HeaderDelegate())
        .render(views)
}