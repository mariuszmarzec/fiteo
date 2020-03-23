package sample

import com.marzec.di.DI

fun hello(): String {
    try {
        val obj = DI.provideExercisesReader().parse("""
        {
          "py/object": "crawler.Result",
          "category": {
            "https://vitalia.pl/index.php/mid/109/fid/1355/diety/dieta/group/14": {
              "py/object": "crawler.Category",
              "category": "Rozciagajace",
              "url": "https://vitalia.pl/index.php/mid/109/fid/1355/diety/dieta/group/14"
            },
            "https://vitalia.pl/index.php/mid/109/fid/1355/diety/dieta/group/16": {
              "py/object": "crawler.Category",
              "category": "Łydki",
              "url": "https://vitalia.pl/index.php/mid/109/fid/1355/diety/dieta/group/16"
            },
            "https://vitalia.pl/index.php/mid/109/fid/1355/diety/dieta/group/20": {
              "py/object": "crawler.Category",
              "category": "Interwały",
              "url": "https://vitalia.pl/index.php/mid/109/fid/1355/diety/dieta/group/20"
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
            },
            {
              "py/object": "crawler.Exercise",
              "animationImageName": "anim_147.gif",
              "animationUrl": "https://vitalia.pl/gfx/fitness2/exercises/gif/sd/147.gif",
              "category": {
                "py/tuple": [
                  "https://vitalia.pl/index.php/mid/109/fid/1355/kalorie/diety/group/4",
                  "Klatka piersiowa"
                ]
              },
              "imagesNames": [
                "image_147-1.jpg",
                "image_147-2.jpg",
                "image_147-3.jpg"
              ],
              "imagesUrls": [
                "https://filesrr.vitalia.pl/gfx/fitness2/exercises_stage/147-1.jpg",
                "https://filesrr.vitalia.pl/gfx/fitness2/exercises_stage/147-2.jpg",
                "https://filesrr.vitalia.pl/gfx/fitness2/exercises_stage/147-3.jpg"
              ],
              "name": "Wyciskanie sztangielek w pozycji siedzącej  ",
              "neededEquipment": {
                "py/object": "crawler.NeededEquipment",
                "needed": [
                  "Sztangielki ze zmiennym obciążeniem"
                ],
                "pageUrl": "https://vitalia.pl/index.php/mid/109/fid/1355/diety/odchudzanie/group/5",
                "thumbnail": "//filesrr.vitalia.pl/gfx/fitness2/exercises/thumbnails/147.jpg",
                "url": "https://vitalia.pl/index.php/mid/109/fid/1355/diety/dieta/fcid/147"
              },
              "thumbnailName": "thumbnail_147.jpg",
              "thumbnailUrl": "https://filesrr.vitalia.pl/gfx/fitness2/exercises/thumbnails/147.jpg",
              "url": "https://vitalia.pl/index.php/mid/109/fid/1355/diety/dieta/fcid/147"
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
            },
            "https://vitalia.pl/index.php/mid/109/fid/1355/diety/dieta/fcid/116": {
              "py/object": "crawler.NeededEquipment",
              "needed": [
                "Brak"
              ],
              "pageUrl": "https://vitalia.pl/index.php/mid/109/fid/1355/odchudzanie/diety/group/14/offsetmzd/2",
              "thumbnail": "//filesrr.vitalia.pl/gfx/fitness2/exercises/thumbnails/116.jpg",
              "url": "https://vitalia.pl/index.php/mid/109/fid/1355/diety/dieta/fcid/116"
            },
            "https://vitalia.pl/index.php/mid/109/fid/1355/diety/dieta/fcid/119": {
              "py/object": "crawler.NeededEquipment",
              "needed": [
                "Brak"
              ],
              "pageUrl": "https://vitalia.pl/index.php/mid/109/fid/1355/odchudzanie/diety/group/14/offsetmzd/3",
              "thumbnail": "//filesrr.vitalia.pl/gfx/fitness2/exercises/thumbnails/119.jpg",
              "url": "https://vitalia.pl/index.php/mid/109/fid/1355/diety/dieta/fcid/119"
            },
            "https://vitalia.pl/index.php/mid/109/fid/1355/diety/dieta/fcid/122": {
              "py/object": "crawler.NeededEquipment",
              "needed": [
                "Brak"
              ],
              "pageUrl": "https://vitalia.pl/index.php/mid/109/fid/1355/odchudzanie/diety/group/14/offsetmzd/4",
              "thumbnail": "//filesrr.vitalia.pl/gfx/fitness2/exercises/thumbnails/122.jpg",
              "url": "https://vitalia.pl/index.php/mid/109/fid/1355/diety/dieta/fcid/122"
            }
          }
        }
    """.trimIndent())
        return obj.toString()
    } catch (e: Exception) {
        return e.message.orEmpty()
    }
}