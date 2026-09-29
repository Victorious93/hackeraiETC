import { rankSkillsForTask } from "./skill-ranker";
import type { SubagentSkill } from "./skills/index";

function makeSkill(
  id: string,
  name: string,
  description: string,
): SubagentSkill {
  return {
    id,
    name,
    description,
    category: "test",
    filename: id,
    contentBytes: 0,
    sourcePath: "",
    sourceSha256: "",
  };
}

const XSS_SKILL = makeSkill(
  "web/xss",
  "XSS Testing",
  "Cross-site scripting vulnerability detection and exploitation techniques",
);
const SQLI_SKILL = makeSkill(
  "web/sqli",
  "SQL Injection",
  "SQL injection attack techniques and database enumeration",
);
const RECON_SKILL = makeSkill(
  "recon/subdomain",
  "Subdomain Enumeration",
  "Subdomain discovery and DNS reconnaissance",
);
const CRYPTO_SKILL = makeSkill(
  "crypto/weak-ciphers",
  "Weak Cipher Detection",
  "TLS and cryptographic configuration review",
);
const ANDROID_SKILL = makeSkill(
  "mobile/android",
  "Android App Testing",
  "Android APK static and dynamic analysis, ADB testing",
);
const PRIVESC_SKILL = makeSkill(
  "linux/privesc",
  "Linux Privilege Escalation",
  "Local privilege escalation techniques on Linux systems",
);

const ALL_SKILLS = [
  XSS_SKILL,
  SQLI_SKILL,
  RECON_SKILL,
  CRYPTO_SKILL,
  ANDROID_SKILL,
  PRIVESC_SKILL,
];

describe("rankSkillsForTask", () => {
  it("returns empty array for empty candidates", () => {
    expect(rankSkillsForTask("xss attack", [])).toEqual([]);
  });

  it("returns candidates unchanged when task is empty string", () => {
    const result = rankSkillsForTask("", ALL_SKILLS);
    expect(result).toHaveLength(ALL_SKILLS.length);
  });

  it("ranks xss skill first for an xss task", () => {
    const result = rankSkillsForTask(
      "find cross-site scripting vulnerabilities",
      ALL_SKILLS,
    );
    expect(result[0].id).toBe("web/xss");
  });

  it("ranks sql injection skill first for a sqli task", () => {
    const result = rankSkillsForTask(
      "test for sql injection and database enumeration",
      ALL_SKILLS,
    );
    expect(result[0].id).toBe("web/sqli");
  });

  it("ranks android skill first for android adb task", () => {
    const result = rankSkillsForTask(
      "android APK dynamic analysis with ADB",
      ALL_SKILLS,
    );
    expect(result[0].id).toBe("mobile/android");
  });

  it("ranks privilege escalation first for linux privesc task", () => {
    const result = rankSkillsForTask(
      "linux local privilege escalation techniques",
      ALL_SKILLS,
    );
    expect(result[0].id).toBe("linux/privesc");
  });

  it("ranks subdomain recon skill first for dns task", () => {
    const result = rankSkillsForTask(
      "subdomain discovery and DNS enumeration reconnaissance",
      ALL_SKILLS,
    );
    expect(result[0].id).toBe("recon/subdomain");
  });

  it("returns all candidates when task matches nothing well", () => {
    const result = rankSkillsForTask(
      "completely unrelated topic xyz123",
      ALL_SKILLS,
    );
    expect(result).toHaveLength(ALL_SKILLS.length);
  });

  it("preserves original catalog order for ties (id localeCompare)", () => {
    const a = makeSkill("a/one", "Alpha", "identical description text");
    const b = makeSkill("b/two", "Beta", "identical description text");
    const result = rankSkillsForTask("identical description text", [b, a]);
    // Both have equal scores; should be sorted by id
    expect(result[0].id).toBe("a/one");
    expect(result[1].id).toBe("b/two");
  });

  it("returns single candidate as-is", () => {
    const result = rankSkillsForTask("xss attack", [XSS_SKILL]);
    expect(result).toHaveLength(1);
    expect(result[0].id).toBe("web/xss");
  });

  it("produces stable ranking on repeated calls with the same inputs", () => {
    const task = "web application vulnerability scanning";
    const first = rankSkillsForTask(task, ALL_SKILLS).map((s) => s.id);
    const second = rankSkillsForTask(task, ALL_SKILLS).map((s) => s.id);
    expect(first).toEqual(second);
  });
});
