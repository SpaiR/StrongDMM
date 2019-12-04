package strongdmm.byond.dme

import com.eclipsesource.json.Json
import com.eclipsesource.json.JsonArray
import com.eclipsesource.json.JsonObject
import com.eclipsesource.json.JsonValue
import strongdmm.byond.VAR_NAME
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.nio.file.Files

/**
 * Under the hood StrongDMM uses SpacemanDMM parser (https://github.com/SpaceManiac/SpacemanDMM).
 * Parser itself is an executable which parses an environment and creates a json file from it.
 *
 * Json contract:
 *  {
 *   "path": "{type_path}",
 *   "vars": [
 *     {
 *       "name": "{var_name}",
 *       "value: "{var_value}"
 *     }
 *   ],
 *   "children: [
 *     ... << object with path, vars and children field
 *   ]
 *  }
 *
 *  Root object contains all #define macros, constants and all other environment objects in children field.
 */
class SdmmParser {
    private val parserPath = System.getProperty("sdmmparser.path")

    fun parseDme(envPath: String): Dme {
        val tmpFile = Files.createTempFile("sdmm.", ".json").toFile()

        val p = ProcessBuilder(parserPath, envPath, tmpFile.absolutePath).start()
        val status = p.waitFor()

        if (status != 0) {
            throw RuntimeException("Unable to parse environment with path: $envPath")
        }

        val dmeItems = mutableMapOf<String, DmeItem>()
        val dme = Dme(File(envPath).parent, dmeItems)

        BufferedReader(FileReader(tmpFile)).use {
            Json.parse(it).asObject().getChildren().forEach { child ->
                traverseTreeRecurs(child.asObject(), dme, dmeItems)
            }
        }

        dme.postInit()
        tmpFile.delete()

        return dme
    }

    private fun traverseTreeRecurs(root: JsonObject, dme: Dme, dmeItems: MutableMap<String, DmeItem>) {
        val type = root.getPath()
        val localVars = mutableMapOf<String, String?>()

        root.getVars().forEach { def ->
            var value = sanitizeVar(def.getValue())

            // Exceptional case for the 'name' variable
            if (def.getName() == VAR_NAME && value == "null") {
                value = '"' + type.substringAfterLast('/') + '"'
            }

            localVars[def.getName()] = value
        }

        // Exceptional case for the 'name' variable
        if (!localVars.containsKey(VAR_NAME)) {
            localVars[VAR_NAME] = '"' + type.substringAfterLast('/') + '"'
        }

        val childrenList = mutableListOf<String>()

        root.getChildren().forEach { child ->
            childrenList.add(child.getPath())
            traverseTreeRecurs(child.asObject(), dme, dmeItems)
        }

        // Sort names by natural order
        childrenList.sortBy { it.substringAfterLast('/') }

        dmeItems[type] = DmeItem(dme, type, localVars, childrenList)
    }

    private fun sanitizeVar(value: String): String? {
        return if (value.startsWith("{\"") && value.endsWith("\"}")) {
            value.substring(1, value.length - 1)
        } else if (value == "null") {
            null
        } else {
            value
        }
    }

    private fun JsonObject.getVars(): JsonArray = get("vars").asArray()
    private fun JsonObject.getPath(): String = get("path").asString()
    private fun JsonObject.getChildren(): JsonArray = get("children").asArray()
    private fun JsonValue.getPath(): String = asObject().get("path").asString()
    private fun JsonValue.getName(): String = asObject().get("name").asString()
    private fun JsonValue.getValue(): String = asObject().get("value").asString()
}
