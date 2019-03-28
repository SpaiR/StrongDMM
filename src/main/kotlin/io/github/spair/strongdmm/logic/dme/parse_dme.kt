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

    fillInInitialVars(dme)

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

private fun fillInInitialVars(dme: Dme) {
    val initialVars = mapOf(
        "/atom" to mapOf(
            "alpha" to "255",
            "appearance_flags" to "0",
            "blend_mode" to "0",
            "color" to "null",
            "density" to "0",
            "desc" to "null",
            "dir" to "2",
            "gender" to "\"neuter\"",
            "icon" to "null",
            "icon_state" to "null",
            "infra_luminosity" to "0",
            "invisibility" to "0",
            "layer" to "1",
            "luminosity" to "0",
            "maptext" to "null",
            "maptext_width" to "32",
            "maptext_height" to "32",
            "maptext_x" to "0",
            "maptext_y" to "0",
            "mouse_drag_pointer" to "0",
            "mouse_drop_pointer" to "1",
            "mouse_drop_zone" to "0",
            "mouse_opacity" to "1",
            "mouse_over_pointer" to "0",
            "opacity" to "0",
            "overlays" to "list()",
            "override" to "0",
            "pixel_x" to "0",
            "pixel_y" to "0",
            "pixel_z" to "0",
            "pixel_w" to "0",
            "plane" to "0",
            "suffix" to "null",
            "transform" to "null",
            "underlays" to "list()",
            "verbs" to "list()",
            "name" to "\"atom\""
        ),
        "/atom/movable" to mapOf(
            "animate_movement" to "1",
            "bound_x" to "0",
            "bound_y" to "0",
            "bound_width" to "32",
            "bound_height" to "32",
            "glide_size" to "0",
            "screen_loc" to "null",
            "step_size" to "32",
            "step_x" to "0",
            "step_y" to "0",
            "name" to "\"movable\""
        ),
        "/area" to mapOf(
            "layer" to "1",
            "luminosity" to "1",
            "name" to "\"area\""
        ),
        "/turf" to mapOf(
            "layer" to "2",
            "name" to "\"turf\""
        ),
        "/mob" to mapOf(
            "layer" to "3",
            "name" to "\"mob\""
        ),
        "/world" to mapOf(
            "area" to "\"/area\"",
            "turf" to "\"/turf\"",
            "mob" to "\"/mob\"",
            "icon_size" to "32"
        )
    )

    initialVars.forEach { type, vars ->
        dme.getItem(type)!!.let {
            vars.forEach { name, value ->
                (it.vars as MutableMap).putIfAbsent(name, value)
            }
        }
    }
}

private fun JsonObject.getVars() = get("vars").asArray()
private fun JsonObject.getPath() = get("path").asString()
private fun JsonObject.getChildren() = get("children").asArray()
private fun JsonValue.getPath() = asObject().get("path").asString()
private fun JsonValue.getName() = asObject().get("name").asString()
private fun JsonValue.getValue() = asObject().get("value").asString()
