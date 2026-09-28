import type { SubagentSkill } from "./skills/index";

// TF-IDF skill ranker — no LLM call, pure algorithm.
// Called by resolveSubagentSkills when candidates exceed MAX_SUBAGENT_SKILLS.

// Tokenize a string into lowercase alpha-only tokens (drops punctuation/numbers).
function tokenize(text: string): string[] {
  return text
    .toLowerCase()
    .split(/[^a-z]+/)
    .filter((t) => t.length > 1);
}

// Build an IDF map over a corpus of documents.
function buildIdf(docs: string[][]): Map<string, number> {
  const N = docs.length;
  const df = new Map<string, number>();
  for (const tokens of docs) {
    for (const token of new Set(tokens)) {
      df.set(token, (df.get(token) ?? 0) + 1);
    }
  }
  const idf = new Map<string, number>();
  for (const [token, count] of df) {
    idf.set(token, Math.log((N + 1) / (count + 1)) + 1);
  }
  return idf;
}

// Compute TF for a token list.
function tf(tokens: string[], token: string): number {
  const count = tokens.filter((t) => t === token).length;
  return count / Math.max(tokens.length, 1);
}

// Cosine similarity between two TF-IDF vectors (represented as Maps).
function cosine(a: Map<string, number>, b: Map<string, number>): number {
  let dot = 0;
  let normA = 0;
  let normB = 0;
  for (const [token, valA] of a) {
    dot += valA * (b.get(token) ?? 0);
    normA += valA * valA;
  }
  for (const valB of b.values()) {
    normB += valB * valB;
  }
  const denom = Math.sqrt(normA) * Math.sqrt(normB);
  return denom === 0 ? 0 : dot / denom;
}

// Build a TF-IDF vector for a token list given an IDF map.
function tfidfVector(
  tokens: string[],
  idf: Map<string, number>,
): Map<string, number> {
  const vec = new Map<string, number>();
  for (const token of new Set(tokens)) {
    const score = tf(tokens, token) * (idf.get(token) ?? 1);
    if (score > 0) vec.set(token, score);
  }
  return vec;
}

// Rank skills by TF-IDF cosine similarity to the task description.
// Returns skills sorted descending by score (highest first).
// Skills with zero similarity are sorted by original catalog order (id localeCompare).
export function rankSkillsForTask(
  task: string,
  candidates: SubagentSkill[],
): SubagentSkill[] {
  if (candidates.length === 0) return [];

  const taskTokens = tokenize(task);
  if (taskTokens.length === 0) return [...candidates];

  // Build IDF corpus: task + each skill's name+description combined
  const skillDocs = candidates.map((s) =>
    tokenize(`${s.name} ${s.description}`),
  );
  const corpus = [taskTokens, ...skillDocs];
  const idf = buildIdf(corpus);

  const taskVec = tfidfVector(taskTokens, idf);

  const scored = candidates.map((skill, i) => ({
    skill,
    score: cosine(taskVec, tfidfVector(skillDocs[i], idf)),
  }));

  return scored
    .sort((a, b) => {
      if (b.score !== a.score) return b.score - a.score;
      return a.skill.id.localeCompare(b.skill.id);
    })
    .map((s) => s.skill);
}
