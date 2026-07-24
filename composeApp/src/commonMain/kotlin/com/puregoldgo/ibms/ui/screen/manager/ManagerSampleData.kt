package com.puregoldgo.ibms.ui.screen.manager

import com.puregoldgo.ibms.ui.screen.sysadmin.registry.BranchRow

/**
 * Stand-in data for the oversight console.
 *
 * This screen is not wired to the API yet, so — like `SecretarySampleData` —
 * this is what the console actually renders at runtime. It goes away in the same
 * commit that introduces the repositories.
 *
 * Shaped to exercise the roll-up rather than to be pretty: more than one period
 * so "this period" is a choice the state has to make rather than a total of
 * everything, more than one ISP so the share column has something to divide, and
 * sheets in all three states so the awaiting and released figures differ from
 * the billed one. A sample where they matched would hide the arithmetic.
 */
internal object ManagerSampleData {

    val topSheets: List<ManagerTopSheetRow> = listOf(
        ManagerTopSheetRow(
            id = "ts-2026-06-globe",
            invoiceNumber = "TS-2026-06-GLB",
            providerName = "Globe",
            period = "2026-06",
            compiledOn = "01 Jul 2026",
            accountCount = 214,
            totalValidated = "760,172.68",
            status = TopSheetRecordStatus.Compiled,
        ),
        ManagerTopSheetRow(
            id = "ts-2026-06-converge",
            invoiceNumber = "TS-2026-06-CNV",
            providerName = "Converge",
            period = "2026-06",
            compiledOn = "01 Jul 2026",
            accountCount = 96,
            totalValidated = "318,440.00",
            status = TopSheetRecordStatus.Approved,
        ),
        ManagerTopSheetRow(
            id = "ts-2026-06-pldt",
            invoiceNumber = "TS-2026-06-PLD",
            providerName = "PLDT",
            period = "2026-06",
            compiledOn = "02 Jul 2026",
            accountCount = 61,
            totalValidated = "148,300.25",
            status = TopSheetRecordStatus.Paid,
        ),
        ManagerTopSheetRow(
            id = "ts-2026-05-globe",
            invoiceNumber = "TS-2026-05-GLB",
            providerName = "Globe",
            period = "2026-05",
            compiledOn = "02 Jun 2026",
            accountCount = 211,
            totalValidated = "751,008.12",
            status = TopSheetRecordStatus.Paid,
        ),
        ManagerTopSheetRow(
            id = "ts-2026-05-converge",
            invoiceNumber = "TS-2026-05-CNV",
            providerName = "Converge",
            period = "2026-05",
            compiledOn = "02 Jun 2026",
            accountCount = 94,
            totalValidated = "311,900.00",
            status = TopSheetRecordStatus.Paid,
        ),
    )

    val activities: List<ActivityRow> = listOf(
        ActivityRow(
            id = "act-1",
            actorName = "Angel V. Rubio",
            actorRole = "secretary",
            summary = "Compiled topsheet TS-2026-06-GLB for Globe (214 accounts)",
            occurredAt = "01 Jul 2026, 09:14",
        ),
        ActivityRow(
            id = "act-2",
            actorName = "Michael Garcia",
            actorRole = "finance",
            summary = "Approved topsheet TS-2026-06-CNV for Converge",
            occurredAt = "01 Jul 2026, 11:02",
        ),
        ActivityRow(
            id = "act-3",
            actorName = "Michael Garcia",
            actorRole = "finance",
            summary = "Marked topsheet TS-2026-06-PLD as paid",
            occurredAt = "02 Jul 2026, 08:40",
        ),
        ActivityRow(
            id = "act-4",
            actorName = "Angel V. Rubio",
            actorRole = "secretary",
            summary = "Transferred account 0917-2288-01 from ALAPAN 1B to AGUADO",
            occurredAt = "02 Jul 2026, 14:27",
        ),
        ActivityRow(
            id = "act-5",
            actorName = "Rina Dela Cruz",
            actorRole = "sysadmin",
            summary = "Changed role for j.santos from pending to secretary",
            occurredAt = "03 Jul 2026, 10:05",
        ),
    )

    val branches: List<BranchRow> = listOf(
        BranchRow(
            id = "store-alapan",
            branchCode = "ALP",
            name = "Alapan 1B",
            city = "Imus",
            providerIds = emptySet(),
            isActive = true,
        ),
        BranchRow(
            id = "store-aguado",
            branchCode = "AGD",
            name = "Aguado",
            city = "Trece Martires",
            providerIds = emptySet(),
            isActive = true,
        ),
        BranchRow(
            id = "store-bacoor",
            branchCode = "BAC",
            name = "Bacoor",
            city = "Bacoor",
            providerIds = emptySet(),
            isActive = true,
        ),
        BranchRow(
            id = "store-dasma",
            branchCode = "DAS",
            name = "Dasmariñas",
            city = "Dasmariñas",
            providerIds = emptySet(),
            isActive = true,
        ),
    )
}
