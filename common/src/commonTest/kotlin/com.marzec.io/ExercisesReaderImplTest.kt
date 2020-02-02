package com.marzec.io

import com.marzec.model.dto.CategoryFileDto
import com.marzec.model.dto.ExerciseFileDto
import com.marzec.model.dto.ExercisesFileDto
import com.marzec.model.dto.NeededEquipmentDto
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlin.test.Test
import kotlin.test.assertEquals

class ExercisesReaderImplTest {

    val json = Json(JsonConfiguration.Stable.copy(strictMode = false, useArrayPolymorphism = true))
    val exercisesReader: ExercisesReaderImpl = ExercisesReaderImpl(json)

    @Test
    fun parse() {
        val obj = exercisesReader.parse("""
            {
              "py/object": "crawler.Result",
              "category": {
                "https://vitalia.pl/index.php/mid/109/fid/1355/diety/dieta/group/14": {
                  "py/object": "crawler.Category",
                  "category": "Rozciagajace",
                  "url": "https://vitalia.pl/index.php/mid/109/fid/1355/diety/dieta/group/14"
                }
              },
              "exercises": [
                {
                  "py/object": "crawler.Exercise",
                  "animationImageName": "anim_164.gif",
                  "animationUrl": "https://vitalia.pl/gfx/fitness2/exercises/gif/sd/164.gif",
                  "category": {
                    "py/tuple": [
                      "https://vitalia.pl/index.php/mid/109/fid/1355/kalorie/diety/group/4",
                      "Klatka piersiowa"
                    ]
                  },
                  "imagesNames": [
                    "image_164-1.jpg",
                    "image_164-2.jpg",
                    "image_164-3.jpg",
                    "image_164-4.jpg"
                  ],
                  "imagesUrls": [
                    "https://filesrr.vitalia.pl/gfx/fitness2/exercises_stage/164-1.jpg",
                    "https://filesrr.vitalia.pl/gfx/fitness2/exercises_stage/164-2.jpg",
                    "https://filesrr.vitalia.pl/gfx/fitness2/exercises_stage/164-3.jpg",
                    "https://filesrr.vitalia.pl/gfx/fitness2/exercises_stage/164-4.jpg"
                  ],
                  "name": "Wznosy barków ze sztangą prostą trzymaną nachwytem w pozycji siedzącej ",
                  "neededEquipment": {
                    "py/object": "crawler.NeededEquipment",
                    "needed": [
                      "Ławeczka prosta",
                      "Sztanga prosta"
                    ],
                    "pageUrl": "https://vitalia.pl/index.php/mid/109/fid/1355/odchudzanie/diety/group/19",
                    "thumbnail": "//filesrr.vitalia.pl/gfx/fitness2/exercises/thumbnails/164.jpg",
                    "url": "https://vitalia.pl/index.php/mid/109/fid/1355/kalorie/diety/fcid/164"
                  },
                  "thumbnailName": "thumbnail_164.jpg",
                  "thumbnailUrl": "https://filesrr.vitalia.pl/gfx/fitness2/exercises/thumbnails/164.jpg",
                  "url": "https://vitalia.pl/index.php/mid/109/fid/1355/kalorie/diety/fcid/164"
                },
                {
                  "py/object": "crawler.Exercise",
                  "animationImageName": "anim_149.gif",
                  "animationUrl": "https://vitalia.pl/gfx/fitness2/exercises/gif/sd/149.gif",
                  "category": {
                    "py/tuple": [
                      "https://vitalia.pl/index.php/mid/109/fid/1355/kalorie/diety/group/4",
                      "Klatka piersiowa"
                    ]
                  },
                  "imagesNames": [
                    "image_149-1.jpg",
                    "image_149-2.jpg",
                    "image_149-3.jpg"
                  ],
                  "imagesUrls": [
                    "https://filesrr.vitalia.pl/gfx/fitness2/exercises_stage/149-1.jpg",
                    "https://filesrr.vitalia.pl/gfx/fitness2/exercises_stage/149-2.jpg",
                    "https://filesrr.vitalia.pl/gfx/fitness2/exercises_stage/149-3.jpg"
                  ],
                  "name": "Unoszenie ramion ze sztangielkami w pozycji leżącej na boku ",
                  "neededEquipment": {
                    "py/object": "crawler.NeededEquipment",
                    "needed": [
                      "Sztangielki ze zmiennym obciążeniem"
                    ],
                    "pageUrl": "https://vitalia.pl/index.php/mid/109/fid/1355/diety/odchudzanie/group/5",
                    "thumbnail": "//filesrr.vitalia.pl/gfx/fitness2/exercises/thumbnails/149.jpg",
                    "url": "https://vitalia.pl/index.php/mid/109/fid/1355/diety/dieta/fcid/149"
                  },
                  "thumbnailName": "thumbnail_149.jpg",
                  "thumbnailUrl": "https://filesrr.vitalia.pl/gfx/fitness2/exercises/thumbnails/149.jpg",
                  "url": "https://vitalia.pl/index.php/mid/109/fid/1355/diety/dieta/fcid/149"
                }
              ],
              "neededEquipment": {
                "null": {
                  "py/object": "crawler.NeededEquipment",
                  "needed": [],
                  "pageUrl": "https://vitalia.pl/index.php/mid/109/fid/1355/kalorie/diety/group/11/offsetmzd/14",
                  "thumbnail": null,
                  "url": null
                },
                "https://vitalia.pl/index.php/mid/109/fid/1355/diety/dieta/fcid/106": {
                  "py/object": "crawler.NeededEquipment",
                  "needed": [
                    "Sztangielki ze zmiennym obciążeniem"
                  ],
                  "pageUrl": "https://vitalia.pl/index.php/mid/109/fid/1355/odchudzanie/diety/group/9/offsetmzd/1",
                  "thumbnail": "//filesrr.vitalia.pl/gfx/fitness2/exercises/thumbnails/106.jpg",
                  "url": "https://vitalia.pl/index.php/mid/109/fid/1355/diety/dieta/fcid/106"
                }
              }
            }

        """.trimIndent())

        val expected = ExercisesFileDto(
                category = mapOf(
                        "https://vitalia.pl/index.php/mid/109/fid/1355/diety/dieta/group/14" to CategoryFileDto(
                                category = "Rozciagajace",
                                url = "https://vitalia.pl/index.php/mid/109/fid/1355/diety/dieta/group/14"
                        )
                ),
                exercises = listOf(
                        ExerciseFileDto(
                                animationImageName = "anim_164.gif",
                                animationUrl = "https://vitalia.pl/gfx/fitness2/exercises/gif/sd/164.gif",
                                category = mapOf("py/tuple" to listOf(
                                        "https://vitalia.pl/index.php/mid/109/fid/1355/kalorie/diety/group/4",
                                        "Klatka piersiowa"
                                )),
                                imagesNames = listOf(
                                        "image_164-1.jpg",
                                        "image_164-2.jpg",
                                        "image_164-3.jpg",
                                        "image_164-4.jpg"
                                ),
                                imagesUrls = listOf(
                                        "https://filesrr.vitalia.pl/gfx/fitness2/exercises_stage/164-1.jpg",
                                        "https://filesrr.vitalia.pl/gfx/fitness2/exercises_stage/164-2.jpg",
                                        "https://filesrr.vitalia.pl/gfx/fitness2/exercises_stage/164-3.jpg",
                                        "https://filesrr.vitalia.pl/gfx/fitness2/exercises_stage/164-4.jpg"
                                ),
                                name = "Wznosy barków ze sztangą prostą trzymaną nachwytem w pozycji siedzącej ",
                                neededEquipment = NeededEquipmentDto(
                                        needed = listOf(
                                                "Ławeczka prosta",
                                                "Sztanga prosta"
                                        ),
                                        pageUrl = "https://vitalia.pl/index.php/mid/109/fid/1355/odchudzanie/diety/group/19",
                                        thumbnail = "//filesrr.vitalia.pl/gfx/fitness2/exercises/thumbnails/164.jpg",
                                        url = "https://vitalia.pl/index.php/mid/109/fid/1355/kalorie/diety/fcid/164"
                                ),
                                thumbnailName = "thumbnail_164.jpg",
                                thumbnailUrl = "https://filesrr.vitalia.pl/gfx/fitness2/exercises/thumbnails/164.jpg",
                                url = "https://vitalia.pl/index.php/mid/109/fid/1355/kalorie/diety/fcid/164"
                        ),
                        ExerciseFileDto(
                                animationImageName = "anim_149.gif",
                                animationUrl = "https://vitalia.pl/gfx/fitness2/exercises/gif/sd/149.gif",
                                category = mapOf("py/tuple" to listOf(
                                        "https://vitalia.pl/index.php/mid/109/fid/1355/kalorie/diety/group/4",
                                        "Klatka piersiowa"
                                )),
                                imagesNames = listOf(
                                        "image_149-1.jpg",
                                        "image_149-2.jpg",
                                        "image_149-3.jpg"
                                ),
                                imagesUrls = listOf(
                                        "https://filesrr.vitalia.pl/gfx/fitness2/exercises_stage/149-1.jpg",
                                        "https://filesrr.vitalia.pl/gfx/fitness2/exercises_stage/149-2.jpg",
                                        "https://filesrr.vitalia.pl/gfx/fitness2/exercises_stage/149-3.jpg"
                                ),
                                name = "Unoszenie ramion ze sztangielkami w pozycji leżącej na boku ",
                                neededEquipment = NeededEquipmentDto(
                                        needed = listOf(
                                                "Sztangielki ze zmiennym obciążeniem"
                                        ),
                                        pageUrl = "https://vitalia.pl/index.php/mid/109/fid/1355/diety/odchudzanie/group/5",
                                        thumbnail = "//filesrr.vitalia.pl/gfx/fitness2/exercises/thumbnails/149.jpg",
                                        url = "https://vitalia.pl/index.php/mid/109/fid/1355/diety/dieta/fcid/149"
                                ),
                                thumbnailName = "thumbnail_149.jpg",
                                thumbnailUrl = "https://filesrr.vitalia.pl/gfx/fitness2/exercises/thumbnails/149.jpg",
                                url = "https://vitalia.pl/index.php/mid/109/fid/1355/diety/dieta/fcid/149"
                        )
                ),
                neededEquipment = mapOf(
                        "null" to NeededEquipmentDto(
                                needed = emptyList(),
                                pageUrl = "https://vitalia.pl/index.php/mid/109/fid/1355/kalorie/diety/group/11/offsetmzd/14",
                                thumbnail = null,
                                url = null
                        ),
                        "https://vitalia.pl/index.php/mid/109/fid/1355/diety/dieta/fcid/106" to NeededEquipmentDto(
                                needed = listOf(
                                        "Sztangielki ze zmiennym obciążeniem"
                                ),
                                pageUrl = "https://vitalia.pl/index.php/mid/109/fid/1355/odchudzanie/diety/group/9/offsetmzd/1",
                                thumbnail = "//filesrr.vitalia.pl/gfx/fitness2/exercises/thumbnails/106.jpg",
                                url = "https://vitalia.pl/index.php/mid/109/fid/1355/diety/dieta/fcid/106"
                        )
                )
        )
        assertEquals(expected, obj)
    }
}