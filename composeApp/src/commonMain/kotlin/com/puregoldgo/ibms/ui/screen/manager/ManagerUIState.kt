package com.puregoldgo.ibms.ui.screen.manager

import androidx.compose.runtime.Immutable
import com.puregoldgo.ibms.ui.component.LETTER_ALL
import com.puregoldgo.ibms.ui.screen.store.RegisterBranchForm
import com.puregoldgo.ibms.ui.screen.sysadmin.registry.BranchRow

/** Which of the four panels the oversight console is showing. */
enum class ManagerTab { Overview, TopSheets, Branches, Activity }

@Immutable
data class ManagerUIState(
    // Who is signed in — drawn in the app bar.
    val userName: String = "",
    val userRole: String = "",

    val selectedTab: ManagerTab = ManagerTab.Overview,

    // Data. TopSheets and activities are sample-fed this pass; branches come
    // from the store API because managers can create new stores.
    val topSheets: List<ManagerTopSheetRow> = emptyList(),
    val activities: List<ActivityRow> = emptyList(),
    val branches: List<BranchRow> = emptyList(),

    val isLoading: Boolean = false,
    val loadError: String? = null,

    // Topsheet, activity and branch filters.
    val topSheetQuery: String = "",
    val activityQuery: String = "",
    val branchQuery: String = "",
    val branchLetter: Char = LETTER_ALL,

    // Register new branch.
    val registerBranchForm: RegisterBranchForm? = null,
) {
    /**
     * The period the overview reports on — the newest one anything was compiled
     * for, rather than the calendar month.
     *
     * Read off the data instead of the clock: a topsheet is compiled days into
     * the following month, so on the 2nd the calendar month has nothing in it
     * and an overview keyed to it would show an empty screen every month.
     */
    val currentPeriod: String?
        get() = topSheets.maxOfOrNull { it.period }

    private val currentPeriodSheets: List<ManagerTopSheetRow>
        get() = currentPeriod?.let { period -> topSheets.filter { it.period == period } }.orEmpty()

    /** Everything compiled for the period, whatever stage it has reached. */
    val periodBilled: String
        get() = currentPeriodSheets.sumOfPesos()

    /** Compiled but not yet approved — what finance still has to look at. */
    val periodAwaiting: String
        get() = currentPeriodSheets
            .filter { it.status == TopSheetRecordStatus.Compiled }
            .sumOfPesos()

    /** Money actually out of the door for the period. */
    val periodPaid: String
        get() = currentPeriodSheets
            .filter { it.status == TopSheetRecordStatus.Paid }
            .sumOfPesos()

    val periodAccountCount: Int
        get() = currentPeriodSheets.sumOf { it.accountCount }

    /**
     * The period's spend per ISP, largest first.
     *
     * Largest first rather than alphabetical: the question is where the money
     * goes, and the answer is the top row.
     */
    val ispSpend: List<IspSpendRow>
        get() {
            val periodTotal = currentPeriodSheets.sumOfCentavos()
            if (periodTotal == 0L) return emptyList()

            return currentPeriodSheets
                .groupBy { it.providerName }
                .map { (provider, sheets) -> provider to sheets }
                // Sorted on the centavos rather than on the formatted string a
                // row carries: "98,000.00" sorts above "760,172.68" as text.
                .sortedByDescending { (_, sheets) -> sheets.sumOfCentavos() }
                .map { (provider, sheets) ->
                    IspSpendRow(
                        providerName = provider,
                        total = sheets.sumOfPesos(),
                        share = formatShare(sheets.sumOfCentavos(), periodTotal),
                        accountCount = sheets.sumOf { it.accountCount },
                    )
                }
        }

    /** Every topsheet, newest period first, narrowed by the search. */
    val visibleTopSheets: List<ManagerTopSheetRow>
        get() = topSheets
            .filter { sheet ->
                topSheetQuery.isBlank() || topSheetQuery.trim().let { q ->
                    sheet.invoiceNumber.contains(q, ignoreCase = true) ||
                        sheet.providerName.contains(q, ignoreCase = true) ||
                        sheet.period.contains(q, ignoreCase = true)
                }
            }
            .sortedByDescending { it.period }

    /** The audit trail as the server ordered it, narrowed by the search. */
    val visibleActivities: List<ActivityRow>
        get() = activities.filter { activity ->
            activityQuery.isBlank() || activityQuery.trim().let { q ->
                activity.actorName.contains(q, ignoreCase = true) ||
                    activity.summary.contains(q, ignoreCase = true)
            }
        }

    /**
     * The letters the rail offers — only those that actually file a branch, so
     * the rail never advertises an empty result.
     */
    val branchLetters: List<Char>
        get() = branches.map { it.indexLetter }.distinct().sorted()

    /**
     * Branch filters compose rather than override: picking `A` and then typing
     * narrows within `A` instead of silently dropping the letter.
     */
    val visibleBranches: List<BranchRow>
        get() = branches
            .filter { branchLetter == LETTER_ALL || it.indexLetter == branchLetter }
            .filter { branch ->
                branchQuery.isBlank() || branchQuery.trim().let { q ->
                    branch.name.contains(q, ignoreCase = true) ||
                        branch.branchCode.contains(q, ignoreCase = true) ||
                        branch.city?.contains(q, ignoreCase = true) == true
                }
            }
            .sortedBy { it.name }
}

/**
 * Peso arithmetic over strings that are already grouped for display.
 *
 * On the strings rather than parsing to a number, for the reason `formatMoney`
 * states: these are billing figures and a round trip through `Double` could
 * change one. Kotlin Multiplatform has no `BigDecimal` in common code, so the
 * centavos are summed as `Long` and regrouped.
 */
internal fun List<ManagerTopSheetRow>.sumOfCentavos(): Long = sumOf { row ->
    val digits = row.totalValidated.filter { it.isDigit() || it == '.' }
    val dot = digits.indexOf('.')
    val whole = (if (dot >= 0) digits.substring(0, dot) else digits).toLongOrNull() ?: 0L
    val fraction = if (dot >= 0) digits.substring(dot + 1).take(2).padEnd(2, '0') else "00"
    whole * 100 + (fraction.toLongOrNull() ?: 0L)
}

internal fun List<ManagerTopSheetRow>.sumOfPesos(): String = formatCentavos(sumOfCentavos())

internal fun formatCentavos(centavos: Long): String {
    val grouped = (centavos / 100).toString()
        .reversed()
        .chunked(3)
        .joinToString(",")
        .reversed()
    return "$grouped.${(centavos % 100).toString().padStart(2, '0')}"
}

/** A share to one decimal place, e.g. `"62.4%"`. Integer maths — no locale needed. */
private fun formatShare(part: Long, whole: Long): String {
    if (whole == 0L) return "0.0%"
    val tenths = (part * 1000) / whole
    return "${tenths / 10}.${tenths % 10}%"
}
