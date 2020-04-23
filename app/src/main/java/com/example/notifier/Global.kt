package com.example.notifier

class Global {

    companion object Chosen {
        var selectedUser: Model? = null
        var editUser: Model? = null
        var list = mutableListOf<Model>()

        var canDelete = false
        var canEdit = false

        fun returnCanDelete(): Boolean {
            return canDelete
        }

        fun returnCanEdit(): Boolean {
            return canEdit
        }

        fun returnSelected(): Model {
            return selectedUser!!;
        }

        fun returnToEdit(): Model {
            return editUser!!;
        }

        fun returnList(): MutableList<Model> {
            return list
        }
    }
}