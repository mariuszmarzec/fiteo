package com.marzec.model.mappers

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

//    val exerciseFileDto = ExercisesFileDto(
//            category = mapOf(
//                    "https://vitalia.pl/index.php/mid/109/fid/1355/diety/dieta/group/14" to CategoryFileDto(
//                            category = "Rozciagajace",
//                            url = "https://vitalia.pl/index.php/mid/109/fid/1355/diety/dieta/group/14"
//                    )
//            ),
//            exercises = listOf(
//                    ExerciseFileDto(
//                            animationImageName = "anim_164.gif",
//                            animationUrl = "https://vitalia.pl/gfx/fitness2/exercises/gif/sd/164.gif",
//                            category = mapOf("py/tuple" to listOf(
//                                    "https://vitalia.pl/index.php/mid/109/fid/1355/kalorie/diety/group/4",
//                                    "Klatka piersiowa"
//                            )),
//                            imagesNames = listOf(
//                                    "image_164-1.jpg",
//                                    "image_164-2.jpg",
//                                    "image_164-3.jpg",
//                                    "image_164-4.jpg"
//                            ),
//                            imagesUrls = listOf(
//                                    "https://filesrr.vitalia.pl/gfx/fitness2/exercises_stage/164-1.jpg",
//                                    "https://filesrr.vitalia.pl/gfx/fitness2/exercises_stage/164-2.jpg",
//                                    "https://filesrr.vitalia.pl/gfx/fitness2/exercises_stage/164-3.jpg",
//                                    "https://filesrr.vitalia.pl/gfx/fitness2/exercises_stage/164-4.jpg"
//                            ),
//                            name = "Wznosy barków ze sztangą prostą trzymaną nachwytem w pozycji siedzącej ",
//                            neededEquipment = NeededEquipmentDto(
//                                    needed = listOf(
//                                            "Ławeczka prosta",
//                                            "Sztanga prosta"
//                                    ),
//                                    pageUrl = "https://vitalia.pl/index.php/mid/109/fid/1355/odchudzanie/diety/group/19",
//                                    thumbnail = "//filesrr.vitalia.pl/gfx/fitness2/exercises/thumbnails/164.jpg",
//                                    url = "https://vitalia.pl/index.php/mid/109/fid/1355/kalorie/diety/fcid/164"
//                            ),
//                            thumbnailName = "thumbnail_164.jpg",
//                            thumbnailUrl = "https://filesrr.vitalia.pl/gfx/fitness2/exercises/thumbnails/164.jpg",
//                            url = "https://vitalia.pl/index.php/mid/109/fid/1355/kalorie/diety/fcid/164"
//                    ),
//                    ExerciseFileDto(
//                            animationImageName = "anim_149.gif",
//                            animationUrl = "https://vitalia.pl/gfx/fitness2/exercises/gif/sd/149.gif",
//                            category = mapOf("py/tuple" to listOf(
//                                    "https://vitalia.pl/index.php/mid/109/fid/1355/kalorie/diety/group/4",
//                                    "Klatka piersiowa"
//                            )),
//                            imagesNames = listOf(
//                                    "image_149-1.jpg",
//                                    "image_149-2.jpg",
//                                    "image_149-3.jpg"
//                            ),
//                            imagesUrls = listOf(
//                                    "https://filesrr.vitalia.pl/gfx/fitness2/exercises_stage/149-1.jpg",
//                                    "https://filesrr.vitalia.pl/gfx/fitness2/exercises_stage/149-2.jpg",
//                                    "https://filesrr.vitalia.pl/gfx/fitness2/exercises_stage/149-3.jpg"
//                            ),
//                            name = "Unoszenie ramion ze sztangielkami w pozycji leżącej na boku ",
//                            neededEquipment = NeededEquipmentDto(
//                                    needed = listOf(
//                                            "Sztangielki ze zmiennym obciążeniem"
//                                    ),
//                                    pageUrl = "https://vitalia.pl/index.php/mid/109/fid/1355/diety/odchudzanie/group/5",
//                                    thumbnail = "//filesrr.vitalia.pl/gfx/fitness2/exercises/thumbnails/149.jpg",
//                                    url = "https://vitalia.pl/index.php/mid/109/fid/1355/diety/dieta/fcid/149"
//                            ),
//                            thumbnailName = "thumbnail_149.jpg",
//                            thumbnailUrl = "https://filesrr.vitalia.pl/gfx/fitness2/exercises/thumbnails/149.jpg",
//                            url = "https://vitalia.pl/index.php/mid/109/fid/1355/diety/dieta/fcid/149"
//                    )
//            ),
//            neededEquipment = mapOf(
//                    "null" to NeededEquipmentDto(
//                            needed = emptyList(),
//                            pageUrl = "https://vitalia.pl/index.php/mid/109/fid/1355/kalorie/diety/group/11/offsetmzd/14",
//                            thumbnail = null,
//                            url = null
//                    ),
//                    "https://vitalia.pl/index.php/mid/109/fid/1355/diety/dieta/fcid/106" to NeededEquipmentDto(
//                            needed = listOf(
//                                    "Sztangielki ze zmiennym obciążeniem"
//                            ),
//                            pageUrl = "https://vitalia.pl/index.php/mid/109/fid/1355/odchudzanie/diety/group/9/offsetmzd/1",
//                            thumbnail = "//filesrr.vitalia.pl/gfx/fitness2/exercises/thumbnails/106.jpg",
//                            url = "https://vitalia.pl/index.php/mid/109/fid/1355/diety/dieta/fcid/106"
//                    )
//            )
//    )
//
//    @Test
//    fun categoryToDomain() {
//        val categoryFileDto = CategoryFileDto(
//                category = "Rozciagajace",
//                url = "https://vitalia.pl/index.php/mid/109/fid/1355/diety/dieta/group/14"
//        )
//        assertEquals(Category("Rozciagajace".hashCode().toString(), "Rozciagajace"), categoryFileDto.toDomain())
//    }
//
//    @Test
//    fun equipmentToDomain() {
//        val equipmentDto = NeededEquipmentDto(
//                needed = listOf("Drążek", "Sztanga"),
//                pageUrl = "https://vitalia.pl/index.php/mid/109/fid/1355/kalorie/diety/group/11/offsetmzd/14",
//                thumbnail = null,
//                url = null
//        )
//        assertEquals(listOf(
//                Equipment("Drążek".hashCode().toString(), "Drążek"),
//                Equipment("Sztanga".hashCode().toString(), "Sztanga")
//        ), equipmentDto.toDomain())
//    }
//
//    @Test
//    fun exerciseToDomain() {
//        val exerciseDto = ExerciseFileDto(
//                animationImageName = "anim_149.gif",
//                animationUrl = "https://vitalia.pl/gfx/fitness2/exercises/gif/sd/149.gif",
//                category = mapOf("py/tuple" to listOf(
//                        "https://vitalia.pl/index.php/mid/109/fid/1355/kalorie/diety/group/4",
//                        "Klatka piersiowa"
//                )),
//                imagesNames = listOf(
//                        "image_149-1.jpg",
//                        "image_149-2.jpg",
//                        "image_149-3.jpg"
//                ),
//                imagesUrls = listOf(
//                        "https://filesrr.vitalia.pl/gfx/fitness2/exercises_stage/149-1.jpg",
//                        "https://filesrr.vitalia.pl/gfx/fitness2/exercises_stage/149-2.jpg",
//                        "https://filesrr.vitalia.pl/gfx/fitness2/exercises_stage/149-3.jpg"
//                ),
//                name = "Unoszenie ramion ze sztangielkami w pozycji leżącej na boku ",
//                neededEquipment = NeededEquipmentDto(
//                        needed = listOf(
//                                "Sztangielki ze zmiennym obciążeniem"
//                        ),
//                        pageUrl = "https://vitalia.pl/index.php/mid/109/fid/1355/diety/odchudzanie/group/5",
//                        thumbnail = "//filesrr.vitalia.pl/gfx/fitness2/exercises/thumbnails/149.jpg",
//                        url = "https://vitalia.pl/index.php/mid/109/fid/1355/diety/dieta/fcid/149"
//                ),
//                thumbnailName = "thumbnail_149.jpg",
//                thumbnailUrl = "https://filesrr.vitalia.pl/gfx/fitness2/exercises/thumbnails/149.jpg",
//                url = "https://vitalia.pl/index.php/mid/109/fid/1355/diety/dieta/fcid/149"
//        )
//        assertEquals(
//                Exercise(
//                        "https://vitalia.pl/index.php/mid/109/fid/1355/diety/dieta/fcid/149".hashCode().toString(),
//                        animationImageName = "anim_149.gif",
//                        animationUrl = "https://vitalia.pl/gfx/fitness2/exercises/gif/sd/149.gif",
//                        category = Category(
//                                "Klatka piersiowa".hashCode().toString(),
//                                "Klatka piersiowa"
//                        ),
//                        imagesNames = listOf(
//                                "image_149-1.jpg",
//                                "image_149-2.jpg",
//                                "image_149-3.jpg"
//                        ),
//                        imagesUrls = listOf(
//                                "https://filesrr.vitalia.pl/gfx/fitness2/exercises_stage/149-1.jpg",
//                                "https://filesrr.vitalia.pl/gfx/fitness2/exercises_stage/149-2.jpg",
//                                "https://filesrr.vitalia.pl/gfx/fitness2/exercises_stage/149-3.jpg"
//                        ),
//                        name = "Unoszenie ramion ze sztangielkami w pozycji leżącej na boku ",
//                        neededEquipment = listOf(
//                                Equipment(
//                                        "Sztangielki ze zmiennym obciążeniem".hashCode().toString(),
//                                        "Sztangielki ze zmiennym obciążeniem"
//                                )
//                        ),
//                        thumbnailName = "thumbnail_149.jpg",
//                        thumbnailUrl = "https://filesrr.vitalia.pl/gfx/fitness2/exercises/thumbnails/149.jpg"
//                        ),
//                exerciseDto.toDomain()
//        )
//    }
//
//    @Test
//    fun exerciseFileDtoToDomain() {
//        assertEquals(
//                ExercisesData(
//                        categories = listOf(
//                                Category("Rozciagajace".hashCode().toString(), "Rozciagajace")
//                        ),
//                        exercises = listOf(
//                                Exercise(
//                                        animationImageName = "anim_164.gif",
//                                        animationUrl = "https://vitalia.pl/gfx/fitness2/exercises/gif/sd/164.gif",
//                                        category = Category("Klatka piersiowa".hashCode().toString(), "Klatka piersiowa"),
//                                        imagesNames = listOf(
//                                                "image_164-1.jpg",
//                                                "image_164-2.jpg",
//                                                "image_164-3.jpg",
//                                                "image_164-4.jpg"
//                                        ),
//                                        imagesUrls = listOf(
//                                                "https://filesrr.vitalia.pl/gfx/fitness2/exercises_stage/164-1.jpg",
//                                                "https://filesrr.vitalia.pl/gfx/fitness2/exercises_stage/164-2.jpg",
//                                                "https://filesrr.vitalia.pl/gfx/fitness2/exercises_stage/164-3.jpg",
//                                                "https://filesrr.vitalia.pl/gfx/fitness2/exercises_stage/164-4.jpg"
//                                        ),
//                                        name = "Wznosy barków ze sztangą prostą trzymaną nachwytem w pozycji siedzącej ",
//                                        neededEquipment = listOf(
//                                                Equipment("Ławeczka prosta".hashCode().toString(), "Ławeczka prosta"),
//                                                Equipment("Sztanga prosta".hashCode().toString(), "Sztanga prosta")
//                                        ),
//                                        thumbnailName = "thumbnail_164.jpg",
//                                        thumbnailUrl = "https://filesrr.vitalia.pl/gfx/fitness2/exercises/thumbnails/164.jpg",
//                                        id = "https://vitalia.pl/index.php/mid/109/fid/1355/kalorie/diety/fcid/164".hashCode().toString()
//                                ),
//                                Exercise(
//                                        animationImageName = "anim_149.gif",
//                                        animationUrl = "https://vitalia.pl/gfx/fitness2/exercises/gif/sd/149.gif",
//                                        category = Category("Klatka piersiowa".hashCode().toString(), "Klatka piersiowa"),
//                                        imagesNames = listOf(
//                                                "image_149-1.jpg",
//                                                "image_149-2.jpg",
//                                                "image_149-3.jpg"
//                                        ),
//                                        imagesUrls = listOf(
//                                                "https://filesrr.vitalia.pl/gfx/fitness2/exercises_stage/149-1.jpg",
//                                                "https://filesrr.vitalia.pl/gfx/fitness2/exercises_stage/149-2.jpg",
//                                                "https://filesrr.vitalia.pl/gfx/fitness2/exercises_stage/149-3.jpg"
//                                        ),
//                                        name = "Unoszenie ramion ze sztangielkami w pozycji leżącej na boku ",
//                                        neededEquipment = listOf(
//                                                Equipment("Sztangielki ze zmiennym obciążeniem".hashCode().toString(), "Sztangielki ze zmiennym obciążeniem")
//                                        ),
//                                        thumbnailName = "thumbnail_149.jpg",
//                                        thumbnailUrl = "https://filesrr.vitalia.pl/gfx/fitness2/exercises/thumbnails/149.jpg",
//                                        id = "https://vitalia.pl/index.php/mid/109/fid/1355/diety/dieta/fcid/149".hashCode().toString()
//                                )
//                        ),
//                        equipment = listOf(
//                                Equipment("Brak".hashCode().toString(), "Brak"),
//                                Equipment("Sztangielki ze zmiennym obciążeniem".hashCode().toString(), "Sztangielki ze zmiennym obciążeniem")
//                        )
//                ),
//                exerciseFileDto.toDomain()
//        )
//    }
}
