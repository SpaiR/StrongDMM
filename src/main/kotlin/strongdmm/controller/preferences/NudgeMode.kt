package strongdmm.controller.preferences

enum class NudgeMode : Selectable {
    PIXEL {
        override fun toString(): String {
            return "pixel_x/pixel_y"
        }
    },
    STEP {
        override fun toString(): String {
            return "step_x/step_y"
        }
    }
}
