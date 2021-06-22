package com.marzec.io

import com.marzec.json
import com.marzec.fiteo.io.ExercisesReaderImpl
import com.marzec.fiteo.model.dto.CategoryFileDto
import com.marzec.fiteo.model.dto.ExerciseFileDto
import com.marzec.fiteo.model.dto.ExercisesFileDto
import com.marzec.fiteo.model.dto.NeededEquipmentDto
import kotlin.test.Test
import kotlin.test.assertEquals

class ExercisesReaderImplTest {

    val exercisesReader: ExercisesReaderImpl = ExercisesReaderImpl(json)

    @Test
    fun parse() {
        val obj = exercisesReader.parse(
            """
                {
                  "py/object": "crawler.Result",
                  "category": {
                    "htttp://chest/4": {
                      "py/object": "crawler.Category",
                      "category": "Chest",
                      "url": "htttp://chest/4"
                    },
                    "http://stretching": {
                      "py/object": "crawler.Category",
                      "category": "Stretching",
                      "url": "http://stretching"
                    }
                  },
                  "exercises": [
                    {
                      "py/object": "crawler.Exercise",
                      "animationImageName": "nameAnimation.gif",
                      "animationUrl": "https://2.bp.blogspot.com/-3hVakxD3vQo/V0WOc73BIvI/AAAAAAAAVZ0/KnXV6voD95scDa8Bn74NjO8MlOpgTsU9wCLcB/s1600/z_dizecka_do_kl_ku_10x_6_.gif",
                      "category": {
                        "py/tuple": [
                          "htttp://chest/4",
                          "Chest"
                        ]
                      },
                      "descriptionsToImages": [
                        "description1",
                        "description2",
                        "description3"
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
                        "exercises_stage/120-1.jpg",
                        "exercises_stage/120-2.jpg",
                        "exercises_stage/120-3.jpg"
                      ],
                      "muscles": [
                        "500x311/100.png",
                        "500x311/base.png"
                      ],
                      "musclesName": [
                        "muscles_1.png",
                        "muscles_2.png"
                      ],
                      "name": "Forearm stretching while kneeling",
                      "neededEquipment": {
                        "py/object": "crawler.NeededEquipment",
                        "needed": [
                          "No"
                        ],
                        "pageUrl": "http://stretching",
                        "thumbnail": "thumbnail",
                        "url": "http://stretching/exer/1"
                      },
                      "thumbnailName": "thumbnail_120.jpg",
                      "thumbnailUrl": "thumbnails_url/120.jpg",
                      "url": "http://stretching/exer/1"
                    },
                    {
                      "py/object": "crawler.Exercise",
                      "animationImageName": "anim_14.gif",
                      "animationUrl": "https://thumbs.gfycat.com/TautElementaryCrab-size_restricted.gif",
                      "category": {
                        "py/tuple": [
                          "http://stretching",
                          "Stretching"
                        ]
                      },
                      "descriptionsToImages": [
                        "description_1",
                        "description_2",
                        "description_3",
                        "description_4",
                        "description_5"
                      ],
                      "descriptionsToMistakes": [
                        "mistakes_1_description",
                        "mistakes_2_description"
                      ],
                      "imagesMistakesNames": [
                        "mistakes_1.jpg",
                        "mistakes_2.jpg"
                      ],
                      "imagesMistakesUrls": [
                        "exercises_errors/14-1.jpg",
                        "exercises_errors/14-2.jpg"
                      ],
                      "imagesNames": [
                        "image_1.jpg",
                        "image_1.jpg",
                        "image_1.jpg",
                        "image_1.jpg",
                        "image_1.jpg"
                      ],
                      "imagesUrls": [
                        "exercises_stage/14-1.jpg",
                        "exercises_stage/14-2.jpg",
                        "exercises_stage/14-3.jpg",
                        "exercises_stage/14-4.jpg",
                        "exercises_stage/14-5.jpg"
                      ],
                      "muscles": [
                        "url_muscles_1.png",
                        "url_muscles_2.png",
                        "url_muscles_3.png",
                        "url_muscles_4.png"
                      ],
                      "musclesName": [
                        "muscles_1.png",
                        "muscles_2.png",
                        "muscles_3.png",
                        "muscles_4.png"
                      ],
                      "name": "Torso twists with a bar",
                      "neededEquipment": {
                        "py/object": "crawler.NeededEquipment",
                        "needed": [
                          "Bar"
                        ],
                        "pageUrl": "http://chest/equip/1",
                        "thumbnail": "thumbnail_url.png",
                        "url": "http://chest/exer/2"
                      },
                      "thumbnailName": "thumbnail_name",
                      "thumbnailUrl": "thumbnail_url.png",
                      "url": "http://chest/exer/2"
                    }
                  ],
                  "neededEquipment": {
                    "null": {
                      "py/object": "crawler.NeededEquipment",
                      "needed": [],
                      "pageUrl": "http://no/equip/no",
                      "thumbnail": null,
                      "url": null
                    },
                    "http://stretching/exer/1": {
                      "py/object": "crawler.NeededEquipment",
                      "needed": [
                        "No"
                      ],
                      "pageUrl": "http://stretching",
                      "thumbnail": "thumbnail",
                      "url": "http://stretching/exer/1"
                    },
                    "http://chest/exer/2": {
                      "py/object": "crawler.NeededEquipment",
                      "needed": [
                        "Bar"
                      ],
                      "pageUrl": "http://chest/equip/1",
                      "thumbnail": "thumbnail_url.png",
                      "url": "http://chest/exer/2"
                    }
                  }
                }
            """.trimIndent()
        )

        val expected = ExercisesFileDto(
            category = mapOf(
                "htttp://chest/4" to CategoryFileDto(
                    category = "Chest",
                    url = "htttp://chest/4"
                ),
                "http://stretching" to CategoryFileDto(
                    category = "Stretching",
                    url = "http://stretching"
                )
            ),
            exercises = listOf(
                ExerciseFileDto(
                    animationImageName = "nameAnimation.gif",
                    animationUrl = "https://2.bp.blogspot.com/-3hVakxD3vQo/V0WOc73BIvI/AAAAAAAAVZ0/KnXV6voD95scDa8Bn74NjO8MlOpgTsU9wCLcB/s1600/z_dizecka_do_kl_ku_10x_6_.gif",
                    category = mapOf(
                        "py/tuple" to listOf(
                            "htttp://chest/4",
                            "Chest"
                        )
                    ),
                    descriptionsToImages = listOf(
                        "description1",
                        "description2",
                        "description3"
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
                        "exercises_stage/120-1.jpg",
                        "exercises_stage/120-2.jpg",
                        "exercises_stage/120-3.jpg"
                    ),
                    muscles = listOf(
                        "500x311/100.png",
                        "500x311/base.png"
                    ),
                    musclesName = listOf(
                        "muscles_1.png",
                        "muscles_2.png"
                    ),
                    name = "Forearm stretching while kneeling",
                    thumbnailName = "thumbnail_120.jpg",
                    thumbnailUrl = "thumbnails_url/120.jpg",
                    url = "http://stretching/exer/1",
                    neededEquipment = NeededEquipmentDto(
                        needed = listOf(
                            "No"
                        ),
                        pageUrl = "http://stretching",
                        thumbnail = "thumbnail",
                        url = "http://stretching/exer/1"
                    )
                ),
                ExerciseFileDto(
                    animationImageName = "anim_14.gif",
                    animationUrl = "https://thumbs.gfycat.com/TautElementaryCrab-size_restricted.gif",
                    category = mapOf(
                        "py/tuple" to listOf(
                            "http://stretching",
                            "Stretching",
                        )
                    ),
                    descriptionsToImages = listOf(
                        "description_1",
                        "description_2",
                        "description_3",
                        "description_4",
                        "description_5"
                    ),
                    descriptionsToMistakes = listOf(
                        "mistakes_1_description",
                        "mistakes_2_description"
                    ),
                    imagesMistakesNames = listOf(
                        "mistakes_1.jpg",
                        "mistakes_2.jpg"
                    ),
                    imagesMistakesUrls = listOf(
                        "exercises_errors/14-1.jpg",
                        "exercises_errors/14-2.jpg"
                    ),
                    imagesNames = listOf(
                        "image_1.jpg",
                        "image_1.jpg",
                        "image_1.jpg",
                        "image_1.jpg",
                        "image_1.jpg"
                    ),
                    imagesUrls = listOf(
                        "exercises_stage/14-1.jpg",
                        "exercises_stage/14-2.jpg",
                        "exercises_stage/14-3.jpg",
                        "exercises_stage/14-4.jpg",
                        "exercises_stage/14-5.jpg"
                    ),
                    muscles = listOf(
                        "url_muscles_1.png",
                        "url_muscles_2.png",
                        "url_muscles_3.png",
                        "url_muscles_4.png"
                    ),
                    musclesName = listOf(
                        "muscles_1.png",
                        "muscles_2.png",
                        "muscles_3.png",
                        "muscles_4.png"
                    ),
                    name = "Torso twists with a bar",
                    thumbnailName = "thumbnail_name",
                    thumbnailUrl = "thumbnail_url.png",
                    url = "http://chest/exer/2",
                    neededEquipment = NeededEquipmentDto(
                        needed = listOf(
                            "Bar"
                        ),
                        pageUrl = "http://chest/equip/1",
                        thumbnail = "thumbnail_url.png",
                        url = "http://chest/exer/2"
                    )
                )
            ),
            neededEquipment = mapOf(
                "null" to NeededEquipmentDto(
                    needed = emptyList(),
                    pageUrl = "http://no/equip/no",
                    thumbnail = null,
                    url = null
                ),
                "http://stretching/exer/1" to NeededEquipmentDto(
                    needed = listOf(
                        "No"
                    ),
                    pageUrl = "http://stretching",
                    thumbnail = "thumbnail",
                    url = "http://stretching/exer/1"
                ),
                "http://chest/exer/2" to NeededEquipmentDto(
                    needed = listOf(
                        "Bar"
                    ),
                    pageUrl = "http://chest/equip/1",
                    thumbnail = "thumbnail_url.png",
                    url = "http://chest/exer/2"
                )
            )

        )
        assertEquals(expected, obj)
    }
}