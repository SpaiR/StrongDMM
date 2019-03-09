package io.github.spair.strongdmm.logic

import io.github.spair.byond.dme.Dme
import io.github.spair.byond.dme.parser.DmeParser
import io.github.spair.strongdmm.gui.controller.ObjectTreeViewController
import io.github.spair.strongdmm.kodein
import org.kodein.di.erased.instance
import java.io.File

class Environment {

    private lateinit var dme: Dme

    private val treeViewController by kodein.instance<ObjectTreeViewController>()

    fun parseAndPrepareEnv(dmeFile: File): Dme {
        val s = System.currentTimeMillis()

        dme = DmeParser.parse(dmeFile)
        treeViewController.populateTree(dme)

        println(System.currentTimeMillis() - s)

        return dme
    }
}
