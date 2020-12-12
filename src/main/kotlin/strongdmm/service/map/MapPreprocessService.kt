package strongdmm.service.map

import strongdmm.application.Service
import strongdmm.byond.TYPE_AREA
import strongdmm.byond.TYPE_TURF
import strongdmm.byond.dme.Dme
import strongdmm.byond.dmm.parser.DmmData
import strongdmm.byond.dmm.parser.TileContent
import strongdmm.byond.dmm.parser.TileObject
import strongdmm.event.Event
import strongdmm.event.EventBus
import strongdmm.event.service.TriggerEnvironmentService
import strongdmm.event.service.TriggerMapPreprocessService
import strongdmm.event.ui.TriggerUnknownTypesDialogUi

class MapPreprocessService : Service {
    init {
        EventBus.sign(TriggerMapPreprocessService.Preprocess::class.java, ::handlePreprocess)
    }

    private fun findUnknownTypes(dmmData: DmmData, dme: Dme): Set<UnknownType> {
        val unknownTypes = mutableSetOf<UnknownType>()

        for (key in dmmData.keys) {
            val tileContent = dmmData.getTileContentByKey(key)!!

            for (tileObject in tileContent) {
                if (dme.getItem(tileObject.type) == null) {
                    unknownTypes.add(UnknownType(tileObject))
                }
            }
        }

        return unknownTypes
    }

    private fun processUnknownTypes(dme: Dme, dmmData: DmmData, unknownTypes: Set<UnknownType>) {
        val areaTileObject = TileObject(dme.basicAreaType)
        val turfTileObject = TileObject(dme.basicTurfType)

        unknownTypes.forEach { unknownType ->
            if (unknownType.type.isEmpty()) {
                if (unknownType.originalTileObject.type.startsWith(TYPE_AREA)) {
                    replaceOrAddType(dmmData, unknownType.originalTileObject, areaTileObject)
                } else if (unknownType.originalTileObject.type.startsWith(TYPE_TURF)) {
                    replaceOrAddType(dmmData, unknownType.originalTileObject, turfTileObject)
                } else {
                    removeType(dmmData, unknownType.originalTileObject)
                }
            } else {
                if (unknownType.originalTileObject.type.startsWith(TYPE_AREA) && !unknownType.type.startsWith(TYPE_AREA)) {
                    replaceOrAddType(dmmData, unknownType.originalTileObject, areaTileObject, createTileObject(unknownType))
                } else if (unknownType.originalTileObject.type.startsWith(TYPE_TURF) && !unknownType.type.startsWith(TYPE_TURF)) {
                    replaceOrAddType(dmmData, unknownType.originalTileObject, turfTileObject, createTileObject(unknownType))
                } else {
                    replaceOrAddType(dmmData, unknownType.originalTileObject, createTileObject(unknownType))
                }
            }
        }
    }

    private fun removeType(dmmData: DmmData, originalTileObject: TileObject) {
        for (key in dmmData.keys) {
            val tileContent = dmmData.getTileContentByKey(key)!!

            if (!tileContent.contains(originalTileObject)) {
                continue
            }

            val newTileContent = TileContent()
            newTileContent.content.addAll(tileContent.content)

            val contentIter = newTileContent.content.iterator()
            while (contentIter.hasNext()) {
                if (contentIter.next() == originalTileObject) {
                    contentIter.remove()
                }
            }

            dmmData.replaceTileContentByKey(key, newTileContent)
        }
    }

    private fun replaceOrAddType(dmmData: DmmData, originalTileObject: TileObject, replaceTileObject: TileObject, addTileObject: TileObject? = null) {
        for (key in dmmData.keys) {
            val tileContent = dmmData.getTileContentByKey(key)!!

            if (!tileContent.contains(originalTileObject)) {
                continue
            }

            val newTileContent = TileContent()
            newTileContent.content.addAll(tileContent.content)

            var replaced = false

            tileContent.forEachIndexed { index, tileObject ->
                if (tileObject == originalTileObject) {
                    newTileContent.content.removeAt(index)
                    newTileContent.content.add(index, replaceTileObject)
                    replaced = true
                }
            }

            if (replaced && addTileObject != null) {
                newTileContent.content.add(addTileObject)
            }

            dmmData.replaceTileContentByKey(key, newTileContent)
        }
    }

    private fun createTileObject(unknownType: UnknownType): TileObject {
        val tileObject = TileObject(unknownType.type)
        tileObject.setVars(unknownType.variables.associate { it.name to it.value })
        return tileObject
    }

    private fun handlePreprocess(event: Event<DmmData, Unit>) {
        EventBus.post(TriggerEnvironmentService.FetchOpenedEnvironment { dme ->
            val unknownTypes = findUnknownTypes(event.body, dme)

            if (unknownTypes.isNotEmpty()) {
                EventBus.post(TriggerUnknownTypesDialogUi.Open(unknownTypes) {
                    processUnknownTypes(dme, event.body, unknownTypes)
                    event.reply(Unit)
                })
            } else {
                event.reply(Unit)
            }
        })
    }
}
