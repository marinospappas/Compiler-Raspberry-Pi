package mpdev.compilerv5.parser.labels

import mpdev.compilerv5.config.Config

class LabelHandler {

    private val code = Config.codeModule

    var labelPrefix = ""
    var labelIndx = 0

    /** create a unique label*/
    fun newLabel(): String = "${labelPrefix}_L${labelIndx++}_"

    /** post a label to output */
    fun postLabel(label: String) = code.outputLabel(label)
}