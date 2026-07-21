package com.puregoldgo.ibms.ui.screen.secretary

/**
 * Stand-in data for the secretary console.
 *
 * This screen is not wired to the API yet, so unlike `SysadminSampleData` —
 * which survives only for previews — this is what the console actually renders
 * at runtime. It goes away in the same commit that introduces the repositories.
 *
 * Shaped to exercise the layout, not to be pretty: branches spread across enough
 * letters for the A–Z rail to matter, accounts on more than one ISP and in more
 * than one state so both the status filter and the archive have something to
 * show, and — deliberately — one closed branch whose account is still live, so
 * the floating-accounts panel is not empty by construction. That is the whole
 * point of the panel, and a preview that never shows it would hide the bug it
 * exists to catch.
 */
internal object SecretarySampleData {

    const val PROVIDER_CONVERGE = "prv-converge"
    const val PROVIDER_GLOBE = "prv-globe"
    const val PROVIDER_PLDT = "prv-pldt"
    const val PROVIDER_RADIUS = "prv-radius"
    private const val PROVIDER_BAYANTEL = "prv-bayantel"

    private const val BRANCH_ALAPAN_1B = "br-alapan-1b"

    val providers: List<SecretaryProviderRow> = listOf(
        SecretaryProviderRow(PROVIDER_CONVERGE, "Converge", isActive = true),
        SecretaryProviderRow(PROVIDER_GLOBE, "Globe", isActive = true),
        SecretaryProviderRow(PROVIDER_PLDT, "PLDT", isActive = true),
        SecretaryProviderRow(PROVIDER_RADIUS, "Radius", isActive = true),
        // Retired vendor: kept so the archive's third column has a row when the
        // sample is edited to include one. Inactive by design.
        SecretaryProviderRow(PROVIDER_BAYANTEL, "Bayantel", isActive = false),
    )

    val branches: List<SecretaryBranchRow> = listOf(
        SecretaryBranchRow(
            id = "br-aguado",
            branchCode = "8079",
            name = "AGUADO",
            city = "Trece Martires Cavite",
            providerIds = setOf(PROVIDER_GLOBE),
            status = BranchRecordStatus.Active,
        ),
        SecretaryBranchRow(
            id = "br-alapan-1a",
            branchCode = "8039",
            name = "ALAPAN 1A",
            city = "Imus Cavite",
            providerIds = setOf(PROVIDER_GLOBE),
            status = BranchRecordStatus.Active,
        ),
        SecretaryBranchRow(
            id = "br-allacapan",
            branchCode = "616",
            name = "ALLACAPAN, CAGAYAN",
            city = null,
            providerIds = setOf(PROVIDER_CONVERGE),
            status = BranchRecordStatus.Active,
        ),
        SecretaryBranchRow(
            id = "br-amaya",
            branchCode = "8076",
            name = "AMAYA 5",
            city = "Tanza Cavite",
            providerIds = setOf(PROVIDER_GLOBE),
            status = BranchRecordStatus.Active,
        ),
        SecretaryBranchRow(
            id = "br-amityville",
            branchCode = "8146",
            name = "AMITYVILLE, Rodriguez, Rizal",
            city = null,
            providerIds = setOf(PROVIDER_RADIUS),
            status = BranchRecordStatus.Active,
        ),
        SecretaryBranchRow(
            id = "br-angat",
            branchCode = "8008",
            name = "ANGAT",
            city = "Angat Bulacan",
            providerIds = setOf(PROVIDER_GLOBE),
            status = BranchRecordStatus.Active,
        ),
        SecretaryBranchRow(
            id = "br-balibago",
            branchCode = "8231",
            name = "BALIBAGO",
            city = "Sta. Rosa Laguna",
            providerIds = setOf(PROVIDER_GLOBE),
            status = BranchRecordStatus.Active,
        ),
        SecretaryBranchRow(
            id = "br-calamba",
            branchCode = "8301",
            name = "CALAMBA",
            city = "Laguna",
            providerIds = setOf(PROVIDER_CONVERGE),
            status = BranchRecordStatus.Active,
        ),
        SecretaryBranchRow(
            id = "br-dagupan",
            branchCode = "509",
            name = "DAGUPAN",
            city = "Pangasinan",
            providerIds = setOf(PROVIDER_PLDT),
            status = BranchRecordStatus.Active,
        ),
        SecretaryBranchRow(
            id = "br-general-trias",
            branchCode = "8455",
            name = "GENERAL TRIAS",
            city = "Cavite",
            providerIds = setOf(PROVIDER_RADIUS),
            status = BranchRecordStatus.Active,
        ),
        SecretaryBranchRow(
            id = "br-guagua",
            branchCode = "220",
            name = "GUAGUA",
            city = "Pampanga",
            providerIds = setOf(PROVIDER_GLOBE),
            status = BranchRecordStatus.Active,
        ),
        SecretaryBranchRow(
            id = "br-imus",
            branchCode = "8512",
            name = "IMUS",
            city = "Cavite",
            providerIds = setOf(PROVIDER_PLDT),
            status = BranchRecordStatus.Active,
        ),
        SecretaryBranchRow(
            id = "br-las-pinas",
            branchCode = "8604",
            name = "LAS PINAS",
            city = "Las Pinas",
            providerIds = setOf(PROVIDER_GLOBE),
            status = BranchRecordStatus.Active,
        ),
        SecretaryBranchRow(
            id = "br-malolos",
            branchCode = "877",
            name = "MALOLOS",
            city = "Bulacan",
            providerIds = setOf(PROVIDER_GLOBE),
            status = BranchRecordStatus.Active,
        ),
        SecretaryBranchRow(
            id = "br-naga",
            branchCode = "902",
            name = "NAGA",
            city = "Camarines Sur",
            providerIds = setOf(PROVIDER_PLDT),
            status = BranchRecordStatus.Active,
        ),
        SecretaryBranchRow(
            id = "br-ortigas",
            branchCode = "8711",
            name = "ORTIGAS",
            city = "Pasig City",
            providerIds = setOf(PROVIDER_CONVERGE),
            status = BranchRecordStatus.Active,
        ),
        // Two ISPs at one branch — a primary and a backup line. The ISP filter
        // has to match either, so the sample carries at least one of these.
        SecretaryBranchRow(
            id = "br-shaw",
            branchCode = "155",
            name = "SHAW",
            city = "Mandaluyong City",
            providerIds = setOf(PROVIDER_GLOBE, PROVIDER_PLDT),
            status = BranchRecordStatus.Active,
        ),
        SecretaryBranchRow(
            id = "br-tayuman",
            branchCode = "8910",
            name = "TAYUMAN",
            city = "Tondo Manila",
            providerIds = setOf(PROVIDER_CONVERGE),
            status = BranchRecordStatus.Active,
        ),

        // ── Closed: the archive's middle column ──────────────────────────────
        SecretaryBranchRow(
            id = "br-china-town",
            branchCode = "316",
            name = "888 CHINA TOWN SQUARE, BACOLOD CITY",
            city = null,
            providerIds = setOf(PROVIDER_GLOBE),
            status = BranchRecordStatus.Closed,
            closureReason = "Contract Termination",
        ),
        // The one that strands a line — see [accounts].
        SecretaryBranchRow(
            id = BRANCH_ALAPAN_1B,
            branchCode = "8094",
            name = "ALAPAN 1B",
            city = "Imus Cavite",
            providerIds = setOf(PROVIDER_PLDT),
            status = BranchRecordStatus.Closed,
            closureReason = "Store Closed (Permanent)",
        ),
        SecretaryBranchRow(
            id = "br-admiral",
            branchCode = "8127",
            name = "ADMIRAL",
            city = "Las Pinas",
            providerIds = setOf(PROVIDER_CONVERGE),
            status = BranchRecordStatus.Closed,
            closureReason = "Store Closed (Permanent)",
        ),
    )

    val accounts: List<SecretaryAccountRow> = listOf(
        SecretaryAccountRow(
            id = "acc-alapan-1a",
            accountNumber = "462387514",
            storeId = "br-alapan-1a",
            storeName = "ALAPAN 1A (Imus Cavite)",
            providerId = PROVIDER_GLOBE,
            monthlyRate = "1,998.00",
            status = AccountRecordStatus.Active,
        ),
        SecretaryAccountRow(
            id = "acc-angat",
            accountNumber = "443086276",
            storeId = "br-angat",
            storeName = "ANGAT (Angat Bulacan)",
            providerId = PROVIDER_GLOBE,
            monthlyRate = "1,998.00",
            status = AccountRecordStatus.Pending,
        ),
        SecretaryAccountRow(
            id = "acc-api",
            accountNumber = "IC-AUZ-1873",
            storeId = "br-amityville",
            storeName = "API CONNECTIVITY",
            providerId = PROVIDER_RADIUS,
            monthlyRate = "210,435.68",
            status = AccountRecordStatus.Pending,
        ),
        SecretaryAccountRow(
            id = "acc-balibago",
            accountNumber = "493107236",
            storeId = "br-balibago",
            storeName = "BALIBAGO (Sta. Rosa Laguna)",
            providerId = PROVIDER_GLOBE,
            monthlyRate = "1,998.00",
            status = AccountRecordStatus.Active,
        ),
        SecretaryAccountRow(
            id = "acc-calamba",
            accountNumber = "PPCI01-032023-11204",
            storeId = "br-calamba",
            storeName = "CALAMBA (Laguna)",
            providerId = PROVIDER_CONVERGE,
            monthlyRate = "2,489.99",
            status = AccountRecordStatus.Active,
        ),
        SecretaryAccountRow(
            id = "acc-dagupan",
            accountNumber = "ZTEGC4471A02",
            storeId = "br-dagupan",
            storeName = "DAGUPAN (Pangasinan)",
            providerId = PROVIDER_PLDT,
            monthlyRate = "3,150.00",
            status = AccountRecordStatus.Active,
        ),
        SecretaryAccountRow(
            id = "acc-general-trias",
            accountNumber = "IC-AWZ-3187",
            storeId = "br-general-trias",
            storeName = "GENERAL TRIAS (Cavite)",
            providerId = PROVIDER_RADIUS,
            monthlyRate = "2,798.00",
            status = AccountRecordStatus.Active,
        ),
        SecretaryAccountRow(
            id = "acc-guagua",
            accountNumber = "48575443CE0298F4",
            storeId = "br-guagua",
            storeName = "GUAGUA (Pampanga)",
            providerId = PROVIDER_GLOBE,
            monthlyRate = "2,000.00",
            status = AccountRecordStatus.Active,
        ),
        SecretaryAccountRow(
            id = "acc-imus",
            accountNumber = "ZTEGCE49520C",
            storeId = "br-imus",
            storeName = "IMUS (Cavite)",
            providerId = PROVIDER_PLDT,
            monthlyRate = "3,150.00",
            status = AccountRecordStatus.Active,
        ),
        SecretaryAccountRow(
            id = "acc-malolos",
            accountNumber = "48575443CE0311B2",
            storeId = "br-malolos",
            storeName = "MALOLOS (Bulacan)",
            providerId = PROVIDER_GLOBE,
            monthlyRate = "2,000.00",
            status = AccountRecordStatus.Active,
        ),
        SecretaryAccountRow(
            id = "acc-ortigas",
            accountNumber = "PPCI01-112021-09933",
            storeId = "br-ortigas",
            storeName = "ORTIGAS (Pasig City)",
            providerId = PROVIDER_CONVERGE,
            monthlyRate = "2,489.99",
            status = AccountRecordStatus.Active,
        ),
        SecretaryAccountRow(
            id = "acc-shaw",
            accountNumber = "48575443CE030DA8",
            storeId = "br-shaw",
            storeName = "SHAW (Mandaluyong City)",
            providerId = PROVIDER_GLOBE,
            monthlyRate = "2,000.00",
            status = AccountRecordStatus.Active,
        ),
        SecretaryAccountRow(
            id = "acc-tayuman",
            accountNumber = "PPCI01-082022-10827",
            storeId = "br-tayuman",
            storeName = "TAYUMAN (Tondo Manila)",
            providerId = PROVIDER_CONVERGE,
            monthlyRate = "2,489.99",
            status = AccountRecordStatus.Active,
        ),

        // ── The floating one ─────────────────────────────────────────────────
        // Still billing, but ALAPAN 1B closed underneath it. This is exactly the
        // discrepancy the Floating Accounts panel exists to surface.
        SecretaryAccountRow(
            id = "acc-alapan-1b",
            accountNumber = "ZTEGCE4188D1",
            storeId = BRANCH_ALAPAN_1B,
            storeName = "ALAPAN 1B (Imus Cavite)",
            providerId = PROVIDER_PLDT,
            monthlyRate = "3,150.00",
            status = AccountRecordStatus.Active,
        ),

        // ── Archived: the first column ───────────────────────────────────────
        SecretaryAccountRow(
            id = "acc-china-town",
            accountNumber = "443086277",
            storeId = "br-china-town",
            storeName = "888 CHINA TOWN SQUARE",
            providerId = PROVIDER_GLOBE,
            monthlyRate = "1,998.00",
            status = AccountRecordStatus.ForDeactivation,
        ),
        SecretaryAccountRow(
            id = "acc-admiral",
            accountNumber = "IC-AUZ-1874",
            storeId = "br-admiral",
            storeName = "ADMIRAL (Las Pinas)",
            providerId = PROVIDER_CONVERGE,
            monthlyRate = "2,489.99",
            status = AccountRecordStatus.Terminated,
        ),
    )

    val topSheets: List<TopSheetRow> = listOf(
        TopSheetRow(
            id = "ts-glob-202607",
            invoiceNumber = "GLOB-202607-0007",
            providerName = "Globe",
            period = "2026-07",
            generatedOn = "7/19/2026",
            accountCount = 214,
            totalValidated = "760,172.68",
            status = TopSheetRecordStatus.Compiled,
        ),
    )
}
