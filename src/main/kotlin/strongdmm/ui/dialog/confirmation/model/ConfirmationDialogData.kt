package strongdmm.ui.dialog.confirmation.model

data class ConfirmationDialogData(
    val type: ConfirmationDialogType = ConfirmationDialogType.YES_NO,
    val title: String = "Confirmation",
    val question: String = "Are you sure about that?"
)
