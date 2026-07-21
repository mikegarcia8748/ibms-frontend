package com.puregoldgo.ibms.ui.screen.sysadmin

import com.puregoldgo.ibms.shared.model.Role
import com.puregoldgo.ibms.shared.model.UserStatus

/**
 * Stand-in data for the control panel's `@Preview`s.
 *
 * Every list here now comes from the API at runtime; this survives only so the
 * previews can draw a populated screen without a ViewModel, a session or a
 * running backend.
 *
 * Shaped to exercise the layout, not to be pretty: a user still holding a
 * temporary password and a deactivated one so both row states are visible at a
 * glance, one account with no role yet, branches spread across enough letters
 * for the A–Z rail to matter, and accounts from more than one provider so the
 * ISP filter visibly does something.
 */
internal object SysadminSampleData {

    /** Which of [users] the previews treat as the signed-in sysadmin. */
    const val SIGNED_IN_USER_ID = "usr-2"

    const val PROVIDER_CONVERGE = "prv-converge"
    const val PROVIDER_GLOBE = "prv-globe"
    const val PROVIDER_PLDT = "prv-pldt"
    const val PROVIDER_RADIUS = "prv-radius"

    val users: List<DirectoryUser> = listOf(
        DirectoryUser(
            id = "usr-1",
            name = "Angel V Rubio",
            username = "arubio",
            employeeNumber = "010005869",
            role = Role.SECRETARY,
            status = UserStatus.ACTIVE,
            mustChangePassword = false,
        ),
        DirectoryUser(
            id = "usr-2",
            name = "Michael Garcia",
            username = "mgarcia",
            employeeNumber = null,
            role = Role.SYSADMIN,
            status = UserStatus.ACTIVE,
            mustChangePassword = false,
        ),
        DirectoryUser(
            id = "usr-3",
            name = "Samuel P Baricaua Jr",
            username = "sbaricaua",
            employeeNumber = "350000017",
            role = Role.PAYABLES,
            status = UserStatus.ACTIVE,
            mustChangePassword = false,
        ),
        // Left the company: still listed, because a row that is gone cannot be
        // turned back on.
        DirectoryUser(
            id = "usr-4",
            name = "Gilbert Arciaga",
            username = "garciaga",
            employeeNumber = "010000000",
            role = Role.SECRETARY,
            status = UserStatus.INACTIVE,
            mustChangePassword = false,
        ),
        // Just provisioned: no role decided yet, and still on the temporary
        // password the admin was handed.
        DirectoryUser(
            id = "usr-5",
            name = "Rosario D Lim",
            username = "rlim",
            employeeNumber = "010007422",
            role = Role.PENDING,
            status = UserStatus.ACTIVE,
            mustChangePassword = true,
        ),
    )

    /**
     * A freshly issued temporary password, for the credential panel's preview.
     *
     * Obviously fake — nothing that looks like a real generated secret belongs
     * in a source file, even a preview one.
     */
    val issuedCredential = IssuedCredential(
        username = "rlim",
        name = "Rosario D Lim",
        temporaryPassword = "SAMPLE-not-a-real-password",
        expiresAt = "2026-07-24T09:00:00Z",
        isNewUser = true,
    )

    val activeProviders: List<IspProviderRow> = listOf(
        IspProviderRow(id = PROVIDER_CONVERGE, name = "Converge", paymentScheduleDay = 5),
        IspProviderRow(id = PROVIDER_GLOBE, name = "Globe", paymentScheduleDay = 5),
        IspProviderRow(id = PROVIDER_PLDT, name = "PLDT", paymentScheduleDay = 5),
        IspProviderRow(id = PROVIDER_RADIUS, name = "Radius", paymentScheduleDay = 5),
    )

    val branches: List<BranchRow> = listOf(
        BranchRow("br-1", "316", "888 CHINA TOWN SQUARE, BACOLOD CITY", "Bacolod City", setOf(PROVIDER_GLOBE), true),
        BranchRow("br-2", "8127", "ADMIRAL", "Las Pinas", setOf(PROVIDER_CONVERGE), true),
        BranchRow("br-3", "8079", "AGUADO", "Trece Martires Cavite", setOf(PROVIDER_GLOBE), true),
        BranchRow("br-4", "8039", "ALAPAN 1A", "Imus Cavite", setOf(PROVIDER_GLOBE), true),
        BranchRow("br-5", "8094", "ALAPAN 1B", "Imus Cavite", setOf(PROVIDER_PLDT), true),
        BranchRow("br-6", "616", "ALLACAPAN, CAGAYAN", "Cagayan", setOf(PROVIDER_CONVERGE), true),
        BranchRow("br-7", "8076", "AMAYA 5", "Tanza Cavite", setOf(PROVIDER_GLOBE), true),
        BranchRow("br-8", "8210", "BACOOR", "Bacoor Cavite", setOf(PROVIDER_RADIUS), true),
        BranchRow("br-9", "412", "BALIWAG", "Bulacan", setOf(PROVIDER_PLDT), true),
        BranchRow("br-10", "8301", "CALAMBA", "Laguna", setOf(PROVIDER_CONVERGE), true),
        BranchRow("br-11", "8322", "CARMONA", "Cavite", setOf(PROVIDER_GLOBE), false),
        BranchRow("br-12", "509", "DAGUPAN", "Pangasinan", setOf(PROVIDER_PLDT), true),
        BranchRow("br-13", "8401", "DASMARINAS", "Cavite", setOf(PROVIDER_GLOBE), true),
        BranchRow("br-14", "701", "EDSA BALINTAWAK", "Quezon City", setOf(PROVIDER_CONVERGE), true),
        BranchRow("br-15", "8455", "GENERAL TRIAS", "Cavite", setOf(PROVIDER_RADIUS), true),
        BranchRow("br-16", "220", "GUAGUA", "Pampanga", setOf(PROVIDER_GLOBE), true),
        BranchRow("br-17", "8512", "IMUS", "Cavite", setOf(PROVIDER_PLDT), true),
        BranchRow("br-18", "133", "KALOOKAN", "Caloocan City", setOf(PROVIDER_CONVERGE), true),
        BranchRow("br-19", "8604", "LAS PINAS", "Las Pinas", setOf(PROVIDER_GLOBE), true),
        BranchRow("br-20", "877", "MALOLOS", "Bulacan", setOf(PROVIDER_GLOBE), true),
        BranchRow("br-21", "902", "NAGA", "Camarines Sur", setOf(PROVIDER_PLDT), true),
        BranchRow("br-22", "8711", "ORTIGAS", "Pasig City", setOf(PROVIDER_CONVERGE), true),
        BranchRow("br-23", "8802", "PARANAQUE", "Paranaque City", setOf(PROVIDER_GLOBE), true),
        // Two ISPs at one branch — a primary and a backup line. The ISP filter
        // has to match either, so the previews carry at least one of these.
        BranchRow("br-24", "155", "SHAW", "Mandaluyong City", setOf(PROVIDER_GLOBE, PROVIDER_PLDT), true),
        BranchRow("br-25", "8910", "TAYUMAN", "Tondo Manila", setOf(PROVIDER_CONVERGE), true),
    )

    val accounts: List<IspAccountRow> = listOf(
        IspAccountRow("acc-1", "48575443CE030DA8", "PUREGOLD PRICE CLUB - SHAW", PROVIDER_GLOBE, "2,000.00", true),
        IspAccountRow("acc-2", "PPCI01-082022-10827", "TAYUMAN, TONDO, MANILA", PROVIDER_CONVERGE, "2,489.99", true),
        IspAccountRow("acc-3", "ZTEGC44534B3", "PUREGOLD PRICE CLUB - PARAÑAQUE", PROVIDER_GLOBE, "2,000.00", true),
        IspAccountRow("acc-4", "48575443a19c8ca5", "PUREGOLD PRICE CLUB - DAU", PROVIDER_GLOBE, "2,000.00", true),
        IspAccountRow("acc-5", "ZTEGCE49520C", "PUREGOLD PRICE CLUB - IMUS", PROVIDER_PLDT, "3,150.00", true),
        IspAccountRow("acc-6", "71214756", "PUREGOLD PRICE CLUB - QI CENTRAL", PROVIDER_GLOBE, "5,598.00", true),
        IspAccountRow("acc-7", "IC-AWZ-2200", "PUREGOLD PRICE CLUB - BACOOR", PROVIDER_RADIUS, "2,798.00", true),
        IspAccountRow("acc-8", "PPCI01-032023-11204", "PUREGOLD PRICE CLUB - CALAMBA", PROVIDER_CONVERGE, "2,489.99", true),
        IspAccountRow("acc-9", "48575443CE0311B2", "PUREGOLD PRICE CLUB - MALOLOS", PROVIDER_GLOBE, "2,000.00", true),
        IspAccountRow("acc-10", "ZTEGC4471A02", "PUREGOLD PRICE CLUB - DAGUPAN", PROVIDER_PLDT, "3,150.00", false),
        IspAccountRow("acc-11", "PPCI01-112021-09933", "PUREGOLD PRICE CLUB - ORTIGAS", PROVIDER_CONVERGE, "2,489.99", true),
        IspAccountRow("acc-12", "48575443a19c9f01", "PUREGOLD PRICE CLUB - LAS PINAS", PROVIDER_GLOBE, "2,000.00", true),
        IspAccountRow("acc-13", "IC-AWZ-3187", "PUREGOLD PRICE CLUB - GENERAL TRIAS", PROVIDER_RADIUS, "2,798.00", true),
        IspAccountRow("acc-14", "ZTEGCE4188D1", "PUREGOLD PRICE CLUB - NAGA", PROVIDER_PLDT, "3,150.00", true),
        IspAccountRow("acc-15", "48575443CE0298F4", "PUREGOLD PRICE CLUB - GUAGUA", PROVIDER_GLOBE, "2,000.00", true),
    )

    /**
     * What a successful import answers with. Canned, for the dialog's preview.
     *
     * Two providers, because one spreadsheet routinely carries several and the
     * per-provider breakdown only appears when it does.
     */
    val bulkImportSummary = BulkImportSummary(
        providers = listOf(
            ProviderImportSummary(
                name = "Converge",
                created = false,
                accountsCreated = 120,
                accountsReused = 0,
            ),
            ProviderImportSummary(
                name = "Globe",
                created = true,
                accountsCreated = 50,
                accountsReused = 0,
            ),
        ),
        storesCreated = 15,
        storesReused = 0,
        accountsCreated = 170,
        accountsReused = 0,
        rowsSkipped = 2,
        skipReasons = listOf(
            "Row 45: missing Store Code",
            "Row 112: invalid or zero Monthly Recurring Amount",
        ),
        totalRows = 172,
    )
}
