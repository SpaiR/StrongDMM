package strongdmm.byond.dmm.parser

import java.io.File

private val NEW_LINE: String = System.lineSeparator()

fun DmmData.saveAsByond(fileToSave: File) {
    fileToSave.writer(Charsets.UTF_8).use { w ->
        for (key in keys) {
            w.write(toByondString(key, getTileContentByKey(key)!!) + NEW_LINE)
        }

        for (z in 1..maxZ) {
            w.write(NEW_LINE)
            w.write("(1,1,$z) = {\"$NEW_LINE")

            for (y in maxY downTo 1) {
                for (x in 1..maxX) {
                    w.write(getKeyByLocation(x, y, z)!!)
                }
                w.write(NEW_LINE)
            }

            w.write("\"}")
        }

        w.write(NEW_LINE)
    }
}

fun DmmData.saveAsTGM(fileToSave: File) {
    fileToSave.writer(Charsets.UTF_8).use { w ->
        w.write("//MAP CONVERTED BY dmm2tgm.py THIS HEADER COMMENT PREVENTS RECONVERSION, DO NOT REMOVE")
        w.write(NEW_LINE)

        for (key in keys) {
            w.write(toTGMString(key, getTileContentByKey(key)!!) + NEW_LINE)
        }

        for (z in 1..maxZ) {
            w.write(NEW_LINE)

            for (x in 1..maxX) {
                w.write("($x,1,$z) = {\"$NEW_LINE")

                for (y in maxY downTo 1) {
                    w.write(getKeyByLocation(x, y, z)!!)
                    w.write(NEW_LINE)
                }

                w.write("\"}$NEW_LINE")
            }
        }
    }
}

private fun toByondString(key: String, tileContent: TileContent): String {
    val sb = StringBuilder("\"$key\" = (")
    val objIter = tileContent.content.iterator()

    while (objIter.hasNext()) {
        val tileObject = objIter.next()

        sb.append(tileObject.type)

        if (!tileObject.vars.isNullOrEmpty()) {
            sb.append('{')

            val varIter = tileObject.vars!!.iterator()

            while (varIter.hasNext()) {
                val varEntry = varIter.next()
                val varName = varEntry.key
                val varValue = varEntry.value

                sb.append(varName).append(" = ").append(varValue)

                if (varIter.hasNext()) {
                    sb.append("; ")
                }
            }

            sb.append('}')
        }

        if (objIter.hasNext()) {
            sb.append(',')
        }
    }

    sb.append(')')

    return sb.toString()
}

private fun toTGMString(key: String, tileContent: TileContent): String {
    val sb = StringBuilder("\"$key\" = (").append(NEW_LINE)
    val objIter = tileContent.content.iterator()

    while (objIter.hasNext()) {
        val tileObject = objIter.next()

        sb.append(tileObject.type)

        if (!tileObject.vars.isNullOrEmpty()) {
            sb.append('{').append(NEW_LINE)

            val varIter = tileObject.vars!!.iterator()

            while (varIter.hasNext()) {
                val varEntry = varIter.next()
                val varName = varEntry.key
                val varValue = varEntry.value

                sb.append('\t').append(varName).append(" = ").append(varValue)

                if (varIter.hasNext()) {
                    sb.append(";")
                }

                sb.append(NEW_LINE)
            }

            sb.append('\t').append('}')
        }

        if (objIter.hasNext()) {
            sb.append(',').append(NEW_LINE)
        }
    }

    sb.append(')')

    return sb.toString()
}
