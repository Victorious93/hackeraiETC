import {
  SUBAGENT_MAX_COST_DOLLARS,
  SUBAGENT_MAX_PARENT_COST_DOLLARS,
  SUBAGENT_PARENT_SYNTHESIS_RESERVE_DOLLARS,
  MAX_SUBAGENTS_PER_PARENT_RUN,
  MAX_ACTIVE_SUBAGENTS_PER_PARENT_RUN,
} from "./contracts";

// Minimum fraction of the subagent budget each spawned child must receive.
const MIN_SUBAGENT_BUDGET_FRACTION = 0.25;

/**
 * Returns true if the parent run may spawn another subagent, given its
 * current total cost and the number of subagents already spawned.
 *
 * Both the hard subagent-count cap and the parent-cost cap are enforced:
 * - existingSubagentCount >= MAX_SUBAGENTS_PER_PARENT_RUN → reject
 * - parentCostDollars + SYNTHESIS_RESERVE + MIN_SUBAGENT_BUDGET > MAX_PARENT_COST → reject
 */
export function canSpawnSubagent(
  parentCostDollars: number,
  existingSubagentCount: number,
): boolean {
  if (existingSubagentCount >= MAX_SUBAGENTS_PER_PARENT_RUN) return false;
  const remainingForWork =
    SUBAGENT_MAX_PARENT_COST_DOLLARS -
    SUBAGENT_PARENT_SYNTHESIS_RESERVE_DOLLARS -
    parentCostDollars;
  const minNeeded = SUBAGENT_MAX_COST_DOLLARS * MIN_SUBAGENT_BUDGET_FRACTION;
  return remainingForWork >= minNeeded;
}

/**
 * Returns how much budget (in dollars) to allocate to the next subagent,
 * given the parent's remaining spendable budget and the number of subagents
 * already spawned.
 *
 * Distributes remaining budget evenly across the remaining allowed slots,
 * capped at SUBAGENT_MAX_COST_DOLLARS. Returns 0 if no budget remains.
 */
export function computeSubagentBudgetAllocation(
  parentRemainingDollars: number,
  existingSubagentCount: number,
): number {
  if (parentRemainingDollars <= 0) return 0;
  const remainingSlots = Math.max(
    1,
    MAX_SUBAGENTS_PER_PARENT_RUN - existingSubagentCount,
  );
  const evenShare = parentRemainingDollars / remainingSlots;
  return Math.min(evenShare, SUBAGENT_MAX_COST_DOLLARS);
}

/**
 * Returns the maximum number of subagents that may run concurrently.
 * Convenience re-export for callers that import from this module.
 */
export const MAX_CONCURRENT_SUBAGENTS = MAX_ACTIVE_SUBAGENTS_PER_PARENT_RUN;
