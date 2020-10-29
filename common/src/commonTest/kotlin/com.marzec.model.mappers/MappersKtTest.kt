package com.marzec.model.mappers

import com.marzec.core.Uuid
import com.marzec.model.domain.Category
import com.marzec.model.domain.Equipment
import com.marzec.model.domain.Exercise
import com.marzec.model.domain.ExercisesData
import com.marzec.model.dto.CategoryFileDto
import com.marzec.model.dto.ExerciseFileDto
import com.marzec.model.dto.ExercisesFileDto
import com.marzec.model.dto.NeededEquipmentDto
import kotlin.test.Test
import kotlin.test.assertEquals

class MappersKtTest {

    val uuid: Uuid = object : Uuid {

        private var i = 0

        override fun create(): String {
            return "${i++}"
        }

    }

    val exerciseFileDto = ExercisesFileDto(
            category = mapOf(
                    "https://vitalia.pl/mid/109/fid/1355/diety/odchudzanie/group/14" to CategoryFileDto(
                            category = "Rozciagajace",
                            url = "https://vitalia.pl/mid/109/fid/1355/diety/odchudzanie/group/14"
                    )),
            exercises = listOf(
                    ExerciseFileDto(
                            animationImageName = "anim_14.gif",
                            animationUrl = "https://vitalia.pl/gfx/fitness2/exercises/gif/sd/14.gif",
                            category = mapOf(
                                    "py/tuple" to listOf(
                                            "https://vitalia.pl/mid/109/fid/1355/diety/odchudzanie/group/14",
                                            "Rozciagajace",
                                    )
                            ),
                            descriptionsToImages = listOf(
                                    "Stań prosto w lekkim rozkroku. Chwyć drążek za głową na wysokości barków. ",
                                    "Wykonaj skręt tułowia w lewą stronę. Stopy całą powierzchnią dotykają podłoża. Nogi i miednica mają być zablokowane. Wykonaj wydech w końcowej fazie skrętu.",
                                    "Wróć do pozycji wyjściowej. Wykonaj wdech w końcowej fazie powrotu.",
                                    "Wykonaj analogicznie skręt na drugą stronę.",
                                    "Wróć do pozycji wyjściowej. Wykonaj wdech w końcowej fazie powrotu."
                            ),
                            descriptionsToMistakes = listOf(
                                    "Odrywanie lub przesuwanie stóp podczas skrętu",
                                    "Pochylanie się w przód podczas skrętu"
                            ),
                            imagesMistakesNames = listOf(
                                    "image_mistakes_14-1.jpg",
                                    "image_mistakes_14-2.jpg"
                            ),
                            imagesMistakesUrls = listOf(
                                    "https://filesrr.vitalia.pl/gfx/fitness2/exercises_errors/14-1.jpg",
                                    "https://filesrr.vitalia.pl/gfx/fitness2/exercises_errors/14-2.jpg"
                            ),
                            imagesNames = listOf(
                                    "image_14-1.jpg",
                                    "image_14-2.jpg",
                                    "image_14-3.jpg",
                                    "image_14-4.jpg",
                                    "image_14-5.jpg"
                            ),
                            imagesUrls = listOf(
                                    "https://filesrr.vitalia.pl/gfx/fitness2/exercises_stage/14-1.jpg",
                                    "https://filesrr.vitalia.pl/gfx/fitness2/exercises_stage/14-2.jpg",
                                    "https://filesrr.vitalia.pl/gfx/fitness2/exercises_stage/14-3.jpg",
                                    "https://filesrr.vitalia.pl/gfx/fitness2/exercises_stage/14-4.jpg",
                                    "https://filesrr.vitalia.pl/gfx/fitness2/exercises_stage/14-5.jpg"
                            ),
                            muscles = listOf(
                                    "https://vitalia.pl/gfx/muscle2D/500x311/44.png",
                                    "https://vitalia.pl/gfx/muscle2D/500x311/46.png",
                                    "https://vitalia.pl/gfx/muscle2D/500x311/47.png",
                                    "https://vitalia.pl/gfx/muscle2D/500x311/base.png"
                            ),
                            musclesName = listOf(
                                    "muscles_44.png",
                                    "muscles_46.png",
                                    "muscles_47.png",
                                    "muscles_base.png"
                            ),
                            name = "Skręty tułowia z drążkiem ",
                            thumbnailName = "thumbnail_14.jpg",
                            thumbnailUrl = "https://vitalia.pl/gfx/fitness2/exercises/thumbnails/14.jpg",
                            url = "https://vitalia.pl/mid/109/fid/1355/diety/odchudzanie/fcid/14",
                            neededEquipment = NeededEquipmentDto(
                                    needed = listOf(
                                            "Drążek"
                                    ),
                                    pageUrl = "https://vitalia.pl/mid/109/fid/1355/diety/odchudzanie/group/11",
                                    thumbnail = "/gfx/fitness2/exercises/thumbnails/14.jpg",
                                    url = "https://vitalia.pl/mid/109/fid/1355/diety/odchudzanie/fcid/14"
                            )
                    )
            ),
            neededEquipment = mapOf(
                    "https://vitalia.pl/mid/109/fid/1355/diety/odchudzanie/fcid/14" to NeededEquipmentDto(
                            needed = listOf(
                                    "Drążek"
                            ),
                            pageUrl = "https://vitalia.pl/mid/109/fid/1355/diety/odchudzanie/group/11",
                            thumbnail = "/gfx/fitness2/exercises/thumbnails/14.jpg",
                            url = "https://vitalia.pl/mid/109/fid/1355/diety/odchudzanie/fcid/14"
                    )
            )

    )

    @Test
    fun categoryToDomain() {
        val categoryFileDto = CategoryFileDto(
                category = "Rozciagajace",
                url = "https://vitalia.pl/index.php/mid/109/fid/1355/diety/dieta/group/14"
        )
        assertEquals(Category("0", "Rozciagajace"), categoryFileDto.toDomain(uuid))
    }

    @Test
    fun equipmentToDomain() {
        val equipmentDto = NeededEquipmentDto(
                needed = listOf("Drążek", "Sztanga"),
                pageUrl = "https://vitalia.pl/index.php/mid/109/fid/1355/kalorie/diety/group/11/offsetmzd/14",
                thumbnail = null,
                url = null
        )
        assertEquals(listOf(
                Equipment("0", "Drążek"),
                Equipment("1", "Sztanga")
        ), equipmentDto.toDomain(uuid))
    }

    @Test
    fun exerciseFileDtoToDomain() {
        assertEquals(
                ExercisesData(
                        categories = listOf(
                                Category("1", "Rozciagajace"),
                                Category("0", "Unknown")
                                ),
                        exercises = listOf(
                                Exercise(
                                        animationImageName = "anim_14.gif",
                                        animationUrl = "https://vitalia.pl/gfx/fitness2/exercises/gif/sd/14.gif",
                                        category = Category("1", "Rozciagajace"),
                                        descriptionsToImages = listOf(
                                                "Stań prosto w lekkim rozkroku. Chwyć drążek za głową na wysokości barków. ",
                                                "Wykonaj skręt tułowia w lewą stronę. Stopy całą powierzchnią dotykają podłoża. Nogi i miednica mają być zablokowane. Wykonaj wydech w końcowej fazie skrętu.",
                                                "Wróć do pozycji wyjściowej. Wykonaj wdech w końcowej fazie powrotu.",
                                                "Wykonaj analogicznie skręt na drugą stronę.",
                                                "Wróć do pozycji wyjściowej. Wykonaj wdech w końcowej fazie powrotu."
                                        ),
                                        descriptionsToMistakes = listOf(
                                                "Odrywanie lub przesuwanie stóp podczas skrętu",
                                                "Pochylanie się w przód podczas skrętu"
                                        ),
                                        imagesMistakesNames = listOf(
                                                "image_mistakes_14-1.jpg",
                                                "image_mistakes_14-2.jpg"
                                        ),
                                        imagesMistakesUrls = listOf(
                                                "https://filesrr.vitalia.pl/gfx/fitness2/exercises_errors/14-1.jpg",
                                                "https://filesrr.vitalia.pl/gfx/fitness2/exercises_errors/14-2.jpg"
                                        ),
                                        imagesNames = listOf(
                                                "image_14-1.jpg",
                                                "image_14-2.jpg",
                                                "image_14-3.jpg",
                                                "image_14-4.jpg",
                                                "image_14-5.jpg"
                                        ),
                                        imagesUrls = listOf(
                                                "https://filesrr.vitalia.pl/gfx/fitness2/exercises_stage/14-1.jpg",
                                                "https://filesrr.vitalia.pl/gfx/fitness2/exercises_stage/14-2.jpg",
                                                "https://filesrr.vitalia.pl/gfx/fitness2/exercises_stage/14-3.jpg",
                                                "https://filesrr.vitalia.pl/gfx/fitness2/exercises_stage/14-4.jpg",
                                                "https://filesrr.vitalia.pl/gfx/fitness2/exercises_stage/14-5.jpg"
                                        ),
                                        muscles = listOf(
                                                "https://vitalia.pl/gfx/muscle2D/500x311/44.png",
                                                "https://vitalia.pl/gfx/muscle2D/500x311/46.png",
                                                "https://vitalia.pl/gfx/muscle2D/500x311/47.png",
                                                "https://vitalia.pl/gfx/muscle2D/500x311/base.png"
                                        ),
                                        musclesName = listOf(
                                                "muscles_44.png",
                                                "muscles_46.png",
                                                "muscles_47.png",
                                                "muscles_base.png"
                                        ),
                                        name = "Skręty tułowia z drążkiem ",
                                        thumbnailName = "thumbnail_14.jpg",
                                        thumbnailUrl = "https://vitalia.pl/gfx/fitness2/exercises/thumbnails/14.jpg",
                                        id = "https://vitalia.pl/mid/109/fid/1355/diety/odchudzanie/fcid/14".hashCode().toString(),
                                        neededEquipment = listOf(
                                                Equipment("2", "Drążek")
                                        )
                                )
                        ),
                        equipment = listOf(
                                Equipment("2", "Drążek")
                        )
                ),
                exerciseFileDto.toDomain(uuid)
        )
    }
}
