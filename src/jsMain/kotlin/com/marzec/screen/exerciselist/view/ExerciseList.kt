package com.marzec.screen.exerciselist.view

import com.marzec.common.useStateFlow
import com.marzec.mvi.State
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
import com.marzec.views.horizontalsplitview.HorizontalSplitDelegate
import com.marzec.views.loading.LoadingDelegate
import com.marzec.views.textinput.TextInputDelegate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.w3c.dom.url.URLSearchParams
import react.FC
import react.router.useLocation
import react.router.useNavigate
import react.useEffect


@ExperimentalCoroutinesApi
val ExerciseList = FC {
    val state = useStateFlow(exerciseListStore.state, defaultState)

    val location = useLocation()
    val navigateFunction = useNavigate()
    val queries = location.search

    useEffect(Unit) {
        val urlSearchParams = URLSearchParams(queries)
        val query = urlSearchParams.get("query").orEmpty()
        val filters = urlSearchParams.get("filters")?.split(",")?.toSet().orEmpty()
        exerciseListStore.sendAction(ExercisesListActions.Initialization(query, filters))
    }

    if (state is State.Data) {
        val params = URLSearchParams(queries)
        if (state.data.searchText.isNotEmpty()) {
            params.set("query", state.data.searchText)
        }
        val filters = state.data.checkedFilters.joinToString(",")
        if (filters.isNotEmpty()) {
            params.set("filters", filters)
        }
        val newPath = "/?$params"
        val currentPath = location.pathname + location.search
        console.log(currentPath)
        console.log(newPath)

        if (currentPath != newPath && params.toString().isNotEmpty()) {
            navigateFunction(newPath)
        }
    }

    val views: List<ViewItem> = ExerciseListUiMapper.map(state)

    ReactRenderer()
        .apply { builder = this@FC }
        .add(
            CheckboxDelegate { id: String ->
                exerciseListStore.sendAction(ExercisesListActions.OnFilterCheckedChange(filterId = id))
            }
        )
        .add(TextInputDelegate { _: String, text: String ->
            exerciseListStore.sendAction(ExercisesListActions.OnSearchTextChanged(text))

        })
        .add(HorizontalSplitDelegate())
        .add(ErrorDelegate())
        .add(ExerciseDelegate())
        .add(LoadingDelegate())
        .add(HeaderDelegate())
        .render(views)
}
