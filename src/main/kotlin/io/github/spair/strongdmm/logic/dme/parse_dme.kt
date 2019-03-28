package io.github.spair.strongdmm.logic.dme

import com.eclipsesource.json.Json
import com.eclipsesource.json.JsonObject
import com.eclipsesource.json.JsonValue

fun parseDme(envPath: String): Dme {
    val response = parseEnvToJson(envPath)

    if (response == "error") {
        throw RuntimeException("Unable to parse environment with path: $envPath")
    }

    val dmeItems = mutableMapOf<String, DmeItem>()
    val dme = Dme(dmeItems)

    Json.parse(response).asObject().getChildren().forEach { child ->
        traverseTreeRecurs(child.asObject(), dme, dmeItems)
    }

    return dme
}

private fun traverseTreeRecurs(root: JsonObject, dme: Dme, dmeItems: MutableMap<String, DmeItem>) {
    val type = root.getPath()

    val localVars = with(root.getVars()) {
        val vars = mutableMapOf<String, String?>()
        forEach { def -> vars[def.getName()] = sanitizeVar(def.getValue()) }
        vars
    }

    val childrenList = with(root.getChildren()) {
        val list = mutableListOf<String>()

        forEach { child ->
            list.add(child.getPath())
            traverseTreeRecurs(child.asObject(), dme, dmeItems)
        }

        list
    }

    dmeItems[type] = DmeItem(dme, type, localVars, childrenList)
}

private fun sanitizeVar(value: String): String? {
    return if (value == "null") {
        null
    } else if (value.startsWith("{\"") && value.endsWith("\"}")) {
        value.substring(1, value.length - 1)
    } else {
        value
    }
}

private fun JsonObject.getVars() = get("vars").asArray()
private fun JsonObject.getPath() = get("path").asString()
private fun JsonObject.getChildren() = get("children").asArray()
private fun JsonValue.getPath() = asObject().get("path").asString()
private fun JsonValue.getName() = asObject().get("name").asString()
private fun JsonValue.getValue() = asObject().get("value").asString()
