package com.marzec.views.exerciserowview

import com.marzec.model.domain.Category
import com.marzec.model.domain.Exercise
import com.marzec.views.base.ReactRendererDelegate
import com.marzec.views.base.RendererDelegate
import com.marzec.views.base.ViewItem
import com.marzec.widget.exerciseview.ExerciseRowView
import react.RBuilder
import react.child
import react.key

class ExerciseRowViewItem(
    override val id: String,
    val name: String,
    val animationUrl: String?,
    val imageUrl: String?,
    val category: List<Category>
) : ViewItem {

    override fun check(render: RendererDelegate): Boolean {
        return render is ExerciseDelegate
    }
}

class ExerciseDelegate : ReactRendererDelegate() {

    override fun RBuilder.render(item: ViewItem) {
        item as ExerciseRowViewItem
        child(ExerciseRowView) {
            this.attrs.key = item.id
            this.attrs.exercise = item
        }
    }
}

fun Exercise.toView() = ExerciseRowViewItem(
    id = id.toString(),
    name = name,
    animationUrl = animationUrl,
    imageUrl = imagesUrls?.firstOrNull(),
    category = category
)