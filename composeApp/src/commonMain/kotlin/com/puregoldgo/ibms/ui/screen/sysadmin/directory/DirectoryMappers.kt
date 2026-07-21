package com.puregoldgo.ibms.ui.screen.sysadmin.directory

import com.puregoldgo.ibms.shared.model.UserProfile

/** Turns an API user into the row the directory draws. */
internal fun UserProfile.toRow() = DirectoryUser(
    id = id,
    name = name,
    username = username,
    employeeNumber = employeeNumber,
    role = role,
    status = status,
    mustChangePassword = mustChangePassword,
)
