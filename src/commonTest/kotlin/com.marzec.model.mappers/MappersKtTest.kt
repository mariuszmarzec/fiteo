package com.marzec.model.mappers

import com.marzec.uuidCounter
import com.marzec.fiteo.model.domain.Category
import com.marzec.fiteo.model.domain.Equipment
import com.marzec.fiteo.model.domain.Exercise
import com.marzec.fiteo.model.domain.ExercisesData
import com.marzec.fiteo.model.dto.CategoryFileDto
import com.marzec.fiteo.model.dto.ExerciseFileDto
import com.marzec.fiteo.model.dto.ExercisesFileDto
import com.marzec.fiteo.model.dto.NeededEquipmentDto
import com.marzec.fiteo.model.mappers.toDomain
import kotlin.test.Test
import kotlin.test.assertEquals

@Suppress("MaxLineLength")
class MappersKtTest {

    val exerciseFileDto = ExercisesFileDto(
            category = mapOf(
                    "https://vitalia.pl/mid/109/fid/1355/diety/odchudzanie/group/14" to CategoryFileDto(
                            category = "Rozciagajace",
                            url = "https://vitalia.pl/mid/109/fid/1355/diety/odchudzanie/group/14"
                    )
            ),
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
        assertEquals(Category("1", "Rozciagajace"), categoryFileDto.toDomain(uuidCounter))
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
                Equipment("1", "Drążek"),
                Equipment("2", "Sztanga")
        ), equipmentDto.toDomain(uuidCounter))
    }

    @Test
    @Suppress("LongMethod")
    fun exerciseFileDtoToDomain() {
        assertEquals(
                ExercisesData(
                        categories = listOf(
                                Category("2", "Rozciagajace"),
                                Category("1", "Brak")
                                ),
                        exercises = listOf(
                                Exercise(
                                        animationImageName = "anim_14.gif",
                                        animationUrl = "https://vitalia.pl/gfx/fitness2/exercises/gif/sd/14.gif",
                                        category = listOf(Category("2", "Rozciagajace")),
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
                                        id = "https://vitalia.pl/mid/109/fid/1355/diety/odchudzanie/fcid/14".hashCode(),
                                        neededEquipment = listOf(
                                                Equipment("3", "Drążek")
                                        ),
                                        videoUrl = null
                                )
                        ),
                        equipment = listOf(
                                Equipment("3", "Drążek")
                        )
                ),
                exerciseFileDto.toDomain(uuidCounter)
        )
    }
}
