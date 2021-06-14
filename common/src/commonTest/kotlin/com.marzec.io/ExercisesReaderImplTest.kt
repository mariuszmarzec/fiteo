package com.marzec.io

import com.marzec.exercises.json
import com.marzec.fiteo.io.ExercisesReaderImpl
import com.marzec.model.dto.CategoryFileDto
import com.marzec.model.dto.ExerciseFileDto
import com.marzec.model.dto.ExercisesFileDto
import com.marzec.model.dto.NeededEquipmentDto
import kotlin.test.Test
import kotlin.test.assertEquals

class ExercisesReaderImplTest {

    val exercisesReader: ExercisesReaderImpl = ExercisesReaderImpl(json)

    @Test
    fun parse() {
        val obj = exercisesReader.parse("""
            {
              "py/object": "crawler.Result",
              "category": {
                "https://vitalia.pl/index.php/mid/109/fid/1355/kalorie/diety/group/4": {
                  "py/object": "crawler.Category",
                  "category": "Klatka piersiowa",
                  "url": "https://vitalia.pl/index.php/mid/109/fid/1355/kalorie/diety/group/4"
                },
                "https://vitalia.pl/mid/109/fid/1355/diety/odchudzanie/group/14": {
                    "py/object": "crawler.Category",
                    "category": "Rozciagajace",
                    "url": "https://vitalia.pl/mid/109/fid/1355/diety/odchudzanie/group/14"
                }
              },
              "exercises": [
                {
                  "py/object": "crawler.Exercise",
                  "animationImageName": "anim_120.gif",
                  "animationUrl": "https://vitalia.pl/gfx/fitness2/exercises/gif/sd/120.gif",
                  "category": {
                    "py/tuple": [
                      "https://vitalia.pl/index.php/mid/109/fid/1355/kalorie/diety/group/4",
                      "Klatka piersiowa"
                    ]
                  },
                  "descriptionsToImages": [
                    "Klęknij i połóż dłonie z przodu na podłożu - metr od kolan.\r\nKąt pomiędzy udami a tułowiem ma wynosić 90 stopni.\r\nUłóż dłonie na podłożu plecami skierowanymi do kolan.\r\nWzrok skieruj do przodu.",
                    "Bez zmiany odrywania dłoni od podłoża usiądź na piętach. \r\nWykonaj wydech w końcowej fazie ruchu.",
                    "Wróć do pozycji wyjściowej. Wykonaj wdech w końcowej fazie powrotu."
                  ],
                  "descriptionsToMistakes": [],
                  "imagesMistakesNames": [],
                  "imagesMistakesUrls": [],
                  "imagesNames": [
                    "image_120-1.jpg",
                    "image_120-2.jpg",
                    "image_120-3.jpg"
                  ],
                  "imagesUrls": [
                    "https://filesrr.vitalia.pl/gfx/fitness2/exercises_stage/120-1.jpg",
                    "https://filesrr.vitalia.pl/gfx/fitness2/exercises_stage/120-2.jpg",
                    "https://filesrr.vitalia.pl/gfx/fitness2/exercises_stage/120-3.jpg"
                  ],
                  "muscles": [
                    "https://vitalia.pl/gfx/muscle2D/500x311/100.png",
                    "https://vitalia.pl/gfx/muscle2D/500x311/base.png"
                  ],
                  "musclesName": [
                    "muscles_100.png",
                    "muscles_base.png"
                  ],
                  "name": "Rozciąganie przedramiona w klęku podpartym",
                  "neededEquipment": {
                    "py/object": "crawler.NeededEquipment",
                    "needed": [
                      "Brak"
                    ],
                    "pageUrl": "https://vitalia.pl/mid/109/fid/1355/diety/odchudzanie/group/14",
                    "thumbnail": "/gfx/fitness2/exercises/thumbnails/120.jpg",
                    "url": "https://vitalia.pl/mid/109/fid/1355/diety/odchudzanie/fcid/120"
                  },
                  "thumbnailName": "thumbnail_120.jpg",
                  "thumbnailUrl": "https://vitalia.pl/gfx/fitness2/exercises/thumbnails/120.jpg",
                  "url": "https://vitalia.pl/mid/109/fid/1355/diety/odchudzanie/fcid/120"
                },
                {
                  "py/object": "crawler.Exercise",
                  "animationImageName": "anim_14.gif",
                  "animationUrl": "https://vitalia.pl/gfx/fitness2/exercises/gif/sd/14.gif",
                  "category": {
                    "py/tuple": [
                      "https://vitalia.pl/mid/109/fid/1355/diety/odchudzanie/group/14",
                      "Rozciagajace"
                    ]
                  },
                  "descriptionsToImages": [
                    "Stań prosto w lekkim rozkroku. Chwyć drążek za głową na wysokości barków. ",
                    "Wykonaj skręt tułowia w lewą stronę. Stopy całą powierzchnią dotykają podłoża. Nogi i miednica mają być zablokowane. Wykonaj wydech w końcowej fazie skrętu.",
                    "Wróć do pozycji wyjściowej. Wykonaj wdech w końcowej fazie powrotu.",
                    "Wykonaj analogicznie skręt na drugą stronę.",
                    "Wróć do pozycji wyjściowej. Wykonaj wdech w końcowej fazie powrotu."
                  ],
                  "descriptionsToMistakes": [
                    "Odrywanie lub przesuwanie stóp podczas skrętu",
                    "Pochylanie się w przód podczas skrętu"
                  ],
                  "imagesMistakesNames": [
                    "image_mistakes_14-1.jpg",
                    "image_mistakes_14-2.jpg"
                  ],
                  "imagesMistakesUrls": [
                    "https://filesrr.vitalia.pl/gfx/fitness2/exercises_errors/14-1.jpg",
                    "https://filesrr.vitalia.pl/gfx/fitness2/exercises_errors/14-2.jpg"
                  ],
                  "imagesNames": [
                    "image_14-1.jpg",
                    "image_14-2.jpg",
                    "image_14-3.jpg",
                    "image_14-4.jpg",
                    "image_14-5.jpg"
                  ],
                  "imagesUrls": [
                    "https://filesrr.vitalia.pl/gfx/fitness2/exercises_stage/14-1.jpg",
                    "https://filesrr.vitalia.pl/gfx/fitness2/exercises_stage/14-2.jpg",
                    "https://filesrr.vitalia.pl/gfx/fitness2/exercises_stage/14-3.jpg",
                    "https://filesrr.vitalia.pl/gfx/fitness2/exercises_stage/14-4.jpg",
                    "https://filesrr.vitalia.pl/gfx/fitness2/exercises_stage/14-5.jpg"
                  ],
                  "muscles": [
                    "https://vitalia.pl/gfx/muscle2D/500x311/44.png",
                    "https://vitalia.pl/gfx/muscle2D/500x311/46.png",
                    "https://vitalia.pl/gfx/muscle2D/500x311/47.png",
                    "https://vitalia.pl/gfx/muscle2D/500x311/base.png"
                  ],
                  "musclesName": [
                    "muscles_44.png",
                    "muscles_46.png",
                    "muscles_47.png",
                    "muscles_base.png"
                  ],
                  "name": "Skręty tułowia z drążkiem ",
                  "neededEquipment": {
                    "py/object": "crawler.NeededEquipment",
                    "needed": [
                      "Drążek"
                    ],
                    "pageUrl": "https://vitalia.pl/mid/109/fid/1355/diety/odchudzanie/group/11",
                    "thumbnail": "/gfx/fitness2/exercises/thumbnails/14.jpg",
                    "url": "https://vitalia.pl/mid/109/fid/1355/diety/odchudzanie/fcid/14"
                  },
                  "thumbnailName": "thumbnail_14.jpg",
                  "thumbnailUrl": "https://vitalia.pl/gfx/fitness2/exercises/thumbnails/14.jpg",
                  "url": "https://vitalia.pl/mid/109/fid/1355/diety/odchudzanie/fcid/14"
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
                "https://vitalia.pl/mid/109/fid/1355/diety/odchudzanie/fcid/120": {
                  "py/object": "crawler.NeededEquipment",
                  "needed": [
                    "Brak"
                  ],
                  "pageUrl": "https://vitalia.pl/mid/109/fid/1355/diety/odchudzanie/group/14",
                  "thumbnail": "/gfx/fitness2/exercises/thumbnails/120.jpg",
                  "url": "https://vitalia.pl/mid/109/fid/1355/diety/odchudzanie/fcid/120"
                },
                "https://vitalia.pl/mid/109/fid/1355/diety/odchudzanie/fcid/14": {
                  "py/object": "crawler.NeededEquipment",
                  "needed": [
                    "Drążek"
                  ],
                  "pageUrl": "https://vitalia.pl/mid/109/fid/1355/diety/odchudzanie/group/11",
                  "thumbnail": "/gfx/fitness2/exercises/thumbnails/14.jpg",
                  "url": "https://vitalia.pl/mid/109/fid/1355/diety/odchudzanie/fcid/14"
                }
              }
            }
        """.trimIndent())

        val expected = ExercisesFileDto(
                category = mapOf(
                        "https://vitalia.pl/index.php/mid/109/fid/1355/kalorie/diety/group/4" to CategoryFileDto(
                                category = "Klatka piersiowa",
                                url = "https://vitalia.pl/index.php/mid/109/fid/1355/kalorie/diety/group/4"
                        ),
                        "https://vitalia.pl/mid/109/fid/1355/diety/odchudzanie/group/14" to CategoryFileDto(
                                category = "Rozciagajace",
                                url = "https://vitalia.pl/mid/109/fid/1355/diety/odchudzanie/group/14"
                        )),
                exercises = listOf(
                        ExerciseFileDto(
                                animationImageName = "anim_120.gif",
                                animationUrl = "https://vitalia.pl/gfx/fitness2/exercises/gif/sd/120.gif",
                                category = mapOf(
                                        "py/tuple" to listOf(
                                                "https://vitalia.pl/index.php/mid/109/fid/1355/kalorie/diety/group/4",
                                                "Klatka piersiowa"
                                        )
                                ),
                                descriptionsToImages = listOf(
                                        "Klęknij i połóż dłonie z przodu na podłożu - metr od kolan.\r\nKąt pomiędzy udami a tułowiem ma wynosić 90 stopni.\r\nUłóż dłonie na podłożu plecami skierowanymi do kolan.\r\nWzrok skieruj do przodu.",
                                        "Bez zmiany odrywania dłoni od podłoża usiądź na piętach. \r\nWykonaj wydech w końcowej fazie ruchu.",
                                        "Wróć do pozycji wyjściowej. Wykonaj wdech w końcowej fazie powrotu."
                                ),
                                descriptionsToMistakes = listOf(),
                                imagesMistakesNames = listOf(),
                                imagesMistakesUrls = listOf(),
                                imagesNames = listOf(
                                        "image_120-1.jpg",
                                        "image_120-2.jpg",
                                        "image_120-3.jpg"
                                ),
                                imagesUrls = listOf(
                                        "https://filesrr.vitalia.pl/gfx/fitness2/exercises_stage/120-1.jpg",
                                        "https://filesrr.vitalia.pl/gfx/fitness2/exercises_stage/120-2.jpg",
                                        "https://filesrr.vitalia.pl/gfx/fitness2/exercises_stage/120-3.jpg"
                                ),
                                muscles = listOf(
                                        "https://vitalia.pl/gfx/muscle2D/500x311/100.png",
                                        "https://vitalia.pl/gfx/muscle2D/500x311/base.png"
                                ),
                                musclesName = listOf(
                                        "muscles_100.png",
                                        "muscles_base.png"
                                ),
                                name = "Rozciąganie przedramiona w klęku podpartym",
                                thumbnailName = "thumbnail_120.jpg",
                                thumbnailUrl = "https://vitalia.pl/gfx/fitness2/exercises/thumbnails/120.jpg",
                                url = "https://vitalia.pl/mid/109/fid/1355/diety/odchudzanie/fcid/120",
                                neededEquipment = NeededEquipmentDto(
                                        needed = listOf(
                                                "Brak"
                                        ),
                                        pageUrl = "https://vitalia.pl/mid/109/fid/1355/diety/odchudzanie/group/14",
                                        thumbnail = "/gfx/fitness2/exercises/thumbnails/120.jpg",
                                        url = "https://vitalia.pl/mid/109/fid/1355/diety/odchudzanie/fcid/120"
                                )
                        ),
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
                        "null" to NeededEquipmentDto(
                                needed = emptyList(),
                                pageUrl = "https://vitalia.pl/index.php/mid/109/fid/1355/kalorie/diety/group/11/offsetmzd/14",
                                thumbnail = null,
                                url = null
                        ),
                        "https://vitalia.pl/mid/109/fid/1355/diety/odchudzanie/fcid/120" to NeededEquipmentDto(
                                needed = listOf(
                                        "Brak"
                                ),
                                pageUrl = "https://vitalia.pl/mid/109/fid/1355/diety/odchudzanie/group/14",
                                thumbnail = "/gfx/fitness2/exercises/thumbnails/120.jpg",
                                url = "https://vitalia.pl/mid/109/fid/1355/diety/odchudzanie/fcid/120"
                        ),
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
        assertEquals(expected, obj)
    }
}