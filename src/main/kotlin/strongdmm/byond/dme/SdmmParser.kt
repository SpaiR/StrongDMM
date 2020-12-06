package strongdmm.byond.dme

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import strongdmm.byond.VAR_NAME
import java.io.File
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
    private val parserFolder: String = System.getProperty("sdmmparser.path")
    private val parserFile: String = if (System.getProperty("os.name").contains("win", ignoreCase = true)) "sdmmparser.exe" else "sdmmparser"

    fun parseDme(dmeFile: File): Dme {
        val tmpFile = Files.createTempFile("sdmm.", ".json").toFile()

        val p = ProcessBuilder(parserFolder + File.separator + parserFile, dmeFile.absolutePath, tmpFile.absolutePath).start()
        val status = p.waitFor()

        if (status != 0) {
            throw RuntimeException("Unable to parse environment with path: $dmeFile")
        }

        val dmeItems = mutableMapOf<String, DmeItem>()
        val dme = Dme(dmeFile.nameWithoutExtension, dmeFile.parent, dmeFile.absolutePath, dmeItems)
        val objectMapper = ObjectMapper()

        tmpFile.reader(Charsets.UTF_8).use {
            traverseTreeRecurs(objectMapper.readTree(it), null, dme, dmeItems)
        }

        dme.postInit()
        tmpFile.delete()

        return dme
    }

    private fun traverseTreeRecurs(root: JsonNode, parentName: String?, dme: Dme, dmeItems: MutableMap<String, DmeItem>) {
        val type = root.getPath()
        val localVars = mutableMapOf<String, String?>()
        var name: String? = null

        root.getVars().forEach { def ->
            var value = sanitizeVar(def.getValue())

            // Exceptional case for the 'name' variable
            if (def.getName() == VAR_NAME) {
                name = value

                if (value == null) {
                    value = parentName ?: '"' + type.substringAfterLast('/') + '"'
                }
            }

            localVars[def.getName()] = value
        }

        // Exceptional case for the 'name' variable
        if (!localVars.containsKey(VAR_NAME)) {
            localVars[VAR_NAME] = parentName ?: '"' + type.substringAfterLast('/') + '"'
        }

        val childrenList = mutableListOf<String>()

        root.getChildren().forEach { child ->
            childrenList.add(child.getPath())
            traverseTreeRecurs(child, name, dme, dmeItems)
        }

        // Sort names by natural order
        childrenList.sortBy { it.substringAfterLast('/') }

        dmeItems[type] = DmeItem(dme, type, localVars, childrenList)
    }

    private fun sanitizeVar(value: String): String? {
        return if (value.length > 2 && value.startsWith("{\"") && value.endsWith("\"}")) {
            value.substring(1, value.length - 1)
        } else if (value == "null") {
            null
        } else {
            value
        }
    }

    private fun JsonNode.getVars(): JsonNode = get("vars")
    private fun JsonNode.getPath(): String = get("path").asText()
    private fun JsonNode.getChildren(): JsonNode = get("children")
    private fun JsonNode.getName(): String = get("name").asText()
    private fun JsonNode.getValue(): String = get("value").asText()
}
