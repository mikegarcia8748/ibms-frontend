package com.puregoldgo.ibms.ui.screen.finance

/**
 * Stand-in data for the finance console.
 *
 * This screen is not wired to the API yet, so — like `SecretarySampleData` —
 * this is what the console actually renders at runtime. It goes away in the same
 * commit that introduces the repositories.
 *
 * Shaped to exercise the layout, not to be pretty: sheets in all three states so
 * both queues have rows and the schedule has something to total, more than one
 * provider so the ISP filter matters, and — deliberately — a sheet whose
 * variance count is non-zero sitting next to one that balances. Approving
 * against variance is the whole job, and a sample where every sheet balanced
 * would hide the number the panel exists to surface.
 */
internal object FinanceSampleData {

    const val PROVIDER_CONVERGE = "prv-converge"
    const val PROVIDER_GLOBE = "prv-globe"
    const val PROVIDER_PLDT = "prv-pldt"
    private const val PROVIDER_BAYANTEL = "prv-bayantel"

    val providers: List<FinanceProviderRow> = listOf(
        FinanceProviderRow(PROVIDER_CONVERGE, "Converge", paymentScheduleDay = 5, isActive = true),
        FinanceProviderRow(PROVIDER_GLOBE, "Globe", paymentScheduleDay = 15, isActive = true),
        FinanceProviderRow(PROVIDER_PLDT, "PLDT", paymentScheduleDay = 20, isActive = true),
        // Retired vendor: kept so the schedule is filtering something out rather
        // than listing everything it is given.
        FinanceProviderRow(PROVIDER_BAYANTEL, "Bayantel", paymentScheduleDay = 28, isActive = false),
    )

    val topSheets: List<FinanceTopSheetRow> = listOf(
        FinanceTopSheetRow(
            id = "ts-2026-06-globe",
            invoiceNumber = "TS-2026-06-GLB",
            providerId = PROVIDER_GLOBE,
            providerName = "Globe",
            period = "2026-06",
            compiledOn = "01 Jul 2026",
            accountCount = 214,
            totalValidated = "760,172.68",
            varianceCount = 3,
            status = TopSheetRecordStatus.Compiled,
        ),
        FinanceTopSheetRow(
            id = "ts-2026-06-converge",
            invoiceNumber = "TS-2026-06-CNV",
            providerId = PROVIDER_CONVERGE,
            providerName = "Converge",
            period = "2026-06",
            compiledOn = "01 Jul 2026",
            accountCount = 96,
            totalValidated = "318,440.00",
            varianceCount = 0,
            status = TopSheetRecordStatus.Compiled,
        ),
        FinanceTopSheetRow(
            id = "ts-2026-05-pldt",
            invoiceNumber = "TS-2026-05-PLD",
            providerId = PROVIDER_PLDT,
            providerName = "PLDT",
            period = "2026-05",
            compiledOn = "02 Jun 2026",
            accountCount = 58,
            totalValidated = "141,905.50",
            varianceCount = 1,
            status = TopSheetRecordStatus.Approved,
        ),
        FinanceTopSheetRow(
            id = "ts-2026-05-globe",
            invoiceNumber = "TS-2026-05-GLB",
            providerId = PROVIDER_GLOBE,
            providerName = "Globe",
            period = "2026-05",
            compiledOn = "02 Jun 2026",
            accountCount = 211,
            totalValidated = "751,008.12",
            varianceCount = 0,
            status = TopSheetRecordStatus.Approved,
        ),
        FinanceTopSheetRow(
            id = "ts-2026-04-globe",
            invoiceNumber = "TS-2026-04-GLB",
            providerId = PROVIDER_GLOBE,
            providerName = "Globe",
            period = "2026-04",
            compiledOn = "03 May 2026",
            accountCount = 209,
            totalValidated = "744,220.40",
            varianceCount = 0,
            status = TopSheetRecordStatus.Paid,
        ),
    )
}
