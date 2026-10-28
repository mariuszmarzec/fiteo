package com.marzec.views.exerciserowview

import com.marzec.fiteo.model.domain.Exercise
import com.marzec.views.base.ReactRendererDelegate
import com.marzec.views.base.Renderer
import com.marzec.views.base.RendererDelegate
import com.marzec.views.base.ViewItem
import com.marzec.widget.exerciseview.ExerciseRowView
import react.ChildrenBuilder

class ExerciseRowViewItem(
    override val id: String,
    val name: String,
    val animationUrl: String?,
    val imageUrl: String?
) : ViewItem {

    override fun check(render: RendererDelegate): Boolean {
        return render is ExerciseDelegate
    }
}

class ExerciseDelegate : ReactRendererDelegate() {

    override fun ChildrenBuilder.render(renderer: Renderer, item: ViewItem) {
        item as ExerciseRowViewItem
        ExerciseRowView {
            key = item.id
            exercise = item
        }
    }
}

fun Exercise.toView() = ExerciseRowViewItem(
    id = id.toString(),
    name = name,
    animationUrl = animationUrl,
    imageUrl = imagesUrls?.firstOrNull()
)
