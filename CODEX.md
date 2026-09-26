DroidCommand AI — Codex Engineering Instructions

You are an autonomous coding agent integrating useful HackerGPT functionality into the DroidCommand AI Android project. Work incrementally, preserve existing functionality, and verify each change.

1. Inspect Before Editing

Before modifying code, inspect:

- Repository structure and modules
- Gradle, AGP, Kotlin, compileSdk, targetSdk, and minSdk versions
- Application ID, entry activity, UI framework, networking, storage, AI, tools, and tests
- Existing architecture and dependencies
- HackerGPT source, including its AI, agent, prompt, tool, command, filesystem, network, browser, configuration, authentication, UI, and backend components

Use repository search extensively. Do not assume the architecture.

DroidCommand AI remains the primary application. Prefer integration and reuse over replacement. Before changing existing architecture, determine whether it is broken, incompatible, duplicated, or requires migration.

2. Establish the Baseline

Determine and record the project configuration and existing capabilities. Run the current test and build commands before making changes.

Create "docs/PROGRESS.md" with:

Baseline build:
Baseline tests:
Known failures:

Do not fix unrelated pre-existing failures during baseline work.

3. Analyze HackerGPT and Plan the Migration

Create "docs/ARCHITECTURE.md" documenting the actual HackerGPT architecture. Do not guess.

Create "docs/MIGRATION_PLAN.md" using:

Component | HackerGPT | DroidCommand | Action | Reason

Use these actions:

KEEP
PORT
REIMPLEMENT
REPLACE
REMOVE
OPTIONAL

Prioritize functionality that provides practical value in DroidCommand AI and is feasible on Android.

4. Implement the Integration Architecture

Use or adapt this flow:

UI
 ↓
AgentController
 ↓
AIProvider
 ↓
ToolRouter
 ↓
ToolRegistry
 ↓
ToolExecutor
 ↓
Android Tools

Reuse equivalent DroidCommand abstractions instead of creating duplicates.

AIProvider

Create or refactor "AIProvider" to:

- Send prompts or requests
- Receive model responses
- Parse structured tool requests
- Handle API errors, timeouts, and cancellation

"AIProvider" must not execute Android commands directly.

AgentController

Implement or adapt "AgentController" to manage:

- Conversation state
- AI requests and responses
- Tool-request detection
- Tool routing and results
- Continuation between tools and the model
- Cancellation and errors

The expected flow is:

User Input → Agent → AI Provider → Tool Request → Tool Router → Tool → Tool Result → Agent → AI → User

Tool Registry and Execution

Use a central registry. Only registered tools may execute. Each tool must define:

- Name
- Description
- Input schema
- Permissions
- Validation
- Execution
- Result format

For example:

interface Tool {
    val name: String
    val description: String

    suspend fun validate(
        arguments: Map<String, Any?>
    ): ValidationResult

    suspend fun execute(
        arguments: Map<String, Any?>
    ): ToolResult
}

5. Implement Android-Native Capabilities

Implement only capabilities supported by a normal Android application. Do not fake desktop behavior or require unavailable privileges.

Potential tools include:

FileRead
FileWrite
FileList
SystemInfo
NetworkRequest
TextSearch
ProjectSearch
CodeAnalysis
DeviceInfo

Adapt all functionality to Android-native APIs and existing project constraints.

Storage

Use explicit storage boundaries:

- Default file operations to "Context.filesDir", "cacheDir", "noBackupFilesDir", or other app-private directories selected by the application.
- Resolve and canonicalize every path, reject traversal, symlinks or equivalent escapes where applicable, and require the resolved path to remain under an approved root.
- Do not expose databases, shared preferences, keystore material, APK files, native libraries, or other sensitive app-private paths unless a tool explicitly allowlists them.
- Access user-selected external documents only through Storage Access Framework URIs returned by "ACTION_OPEN_DOCUMENT", "ACTION_CREATE_DOCUMENT", or "ACTION_OPEN_DOCUMENT_TREE".
- Persist URI access only after an explicit user selection and only for the required read/write mode using "takePersistableUriPermission"; release it when no longer needed.
- Validate URI schemes, authorities, MIME types, and granted flags before every operation. Do not convert arbitrary URIs into filesystem paths or use raw external-storage paths.
- Keep temporary exports in app-private cache storage, delete them after use, and never grant broad storage access merely for convenience.
- Enforce per-tool read/write boundaries and reject paths or URIs outside the configured boundary before confirmation or execution.

Networking

Use Android-compatible networking with:

- HTTPS by default; reject cleartext HTTP unless a narrowly scoped, documented exception is explicitly configured and approved.
- Network Security Configuration to disable cleartext traffic and restrict trust settings; do not add permissive trust managers or hostname verifiers.
- "INTERNET" only when required, with no unnecessary network permissions.
- A vetted HTTP client with certificate and hostname validation, bounded connect/read/write/call timeouts, cancellation propagation, response-size limits, and no main-thread networking.
- Explicit allowlisted hosts, schemes, ports, methods, and redirect behavior for each network tool. Do not permit arbitrary destinations, local-network probing, loopback access, metadata endpoints, or unrestricted proxying.
- Appropriate bounded retries only for safe, idempotent failures, with backoff and no retry of authentication or user-confirmation failures.
- Response validation, content-type checks, and redacted error messages.

Never hard-code or log API keys, tokens, cookies, authorization headers, request bodies containing secrets, or other credentials. Store provider credentials in Android Keystore-backed encrypted storage where persistence is required, keep them out of prompts and tool arguments, and send them only to the configured provider endpoint over validated TLS.

6. Secure Command and Tool Execution

Treat all AI-generated commands and arguments as untrusted. Enforce:

Input → Parse → Validate → Allowlist → Permission Check → User Confirmation → Execute → Result

Only explicitly registered, enabled, and versioned tools may run. Tool names, operations, paths, URI authorities, hosts, methods, and argument values must be validated against typed schemas and allowlists; reject unknown fields, malformed values, oversized inputs, ambiguous encodings, and duplicate or conflicting arguments.

Do not provide unrestricted shell access, "Runtime.exec", "ProcessBuilder", arbitrary code evaluation, dynamic class loading, or access to hidden Android APIs. If a narrowly scoped subprocess is unavoidable, use a fixed executable and fixed argument model, run it in a dedicated sandboxed process with no inherited environment or file descriptors, minimal permissions, bounded CPU/memory/time, no network access, and an app-private working directory. Never construct shell command strings from model output.

Require explicit, user-visible confirmation for writes, deletes, overwrites, external URI access, network requests that transmit user data, account or device changes, subprocesses, and any operation with irreversible or consequential effects. Confirmation must show the exact tool, target, scope, data destination, and relevant arguments; default to deny, expire on timeout or context change, bind approval to a request hash, and never allow the model to approve its own request. Denials and cancellations must prevent execution and be reported clearly.

Do not implement unauthorized access, credential theft, stealth, persistence, privilege escalation, security bypasses, or other harmful capabilities.

Review every tool for unintended access or execution paths.

7. Integrate with the Existing UI

Use the existing DroidCommand UI; do not create a separate HackerGPT UI.

Clearly display:

- AI responses
- Requested tools
- Tool status
- Confirmation prompts
- Execution results
- Errors

Users must understand when the AI is invoking a tool and whether execution is pending, approved, denied, or complete. Confirmation dialogs must identify the requesting tool, exact scope, destination, data being sent or changed, and whether the action is reversible. Do not hide material details behind generic “Allow” labels, and do not treat conversational consent as approval for a later materially different request.

8. Document and Verify Security

Create "docs/SECURITY.md" and verify:

- Tool allowlisting and registry integrity
- Typed input and argument validation, size limits, and rejection of unknown fields
- Canonical path validation, symlink escape prevention, URI scheme/authority checks, and storage-root boundaries
- Android permission checks, least-privilege manifest declarations, and runtime permission handling
- Dangerous-operation confirmation, approval expiry, request binding, denial, and cancellation
- API-key and secret handling through Keystore-backed storage, memory minimization, prompt exclusion, and redacted transport
- Logging safety: never log secrets, authorization headers, cookies, raw prompts containing credentials, full file contents, URI grants, or sensitive personal data; use structured events with request IDs, tool names, outcome, duration, and coarse error categories; redact paths, query parameters, payloads, and identifiers; disable verbose logs in release builds
- Network security, HTTPS enforcement, certificate validation, host/method allowlists, timeouts, response limits, and safe retries
- Error handling that avoids leaking filesystem, account, network, or credential details
- Restrictions on tool capabilities, subprocesses, exported components, pending intents, and inter-process communication
- Tool sandboxing: isolated execution where needed, minimal process permissions, bounded resources, private working directories, no inherited secrets, and no unrestricted network or filesystem access
- Secure cleanup of temporary files, cached responses, clipboard data, and revoked URI permissions

9. Add and Run Tests

Add tests covering at least:

- "AIProvider"
- "AgentController"
- "ToolRegistry"
- Tool validation and execution
- Command and path validation
- Canonical path and symlink escape rejection
- Storage Access Framework URI scheme, authority, grant, and boundary validation
- Permission handling
- Confirmation flow, approval expiry, request binding, denial, and cancellation
- Network host, scheme, method, timeout, redirect, response-size, and retry restrictions
- Secret storage and log redaction, including assertions that credentials do not appear in logs or errors
- Tool sandbox restrictions and resource limits
- Network, AI, and tool failures
- Cancellation

Run:

./gradlew test
./gradlew assembleDebug

For failures:

1. Identify the root cause.
2. Fix it when it is within scope.
3. Re-run the failing command.
4. Repeat until clean or blocked by an external issue.
5. Report unresolved failures; never hide them.

Verify the complete pipeline:

User → DroidCommand UI → Agent → AI Provider → Tool Request → Tool Registry → Tool Executor → Android → Tool Result → Agent → AI Response → UI

Test normal requests, valid and invalid tools, invalid arguments, denied permissions, network and AI failures, dangerous operations, and cancellation.

10. Update Progress After Every Phase

At the end of each phase, update "docs/PROGRESS.md":

PHASE X
Status: COMPLETE / BLOCKED / PARTIAL

Completed:
...

Files changed:
...

Tests:
...

Problems:
...

Next:
...

Mark a phase "COMPLETE" only after its requirements have been verified.

11. Clean Up Safely

Remove dead code, unused dependencies, duplicate implementations, debug secrets, temporary files, unused imports, and unused HackerGPT components only after checking references.

Do not remove code solely because it appears unused.

12. Perform the Final Build and Audit

Run the complete build and verify:

- Compilation
- Unit tests
- APK generation
- Manifest
- Permissions
- Dependencies
- Runtime initialization
- Storage roots, URI grants, exported components, and network security configuration
- Release logging configuration and secret redaction

Record results in "docs/PROGRESS.md".

Audit the source for:

TODO
FIXME
password
api_key
apikey
secret
token
localhost
hard-coded credentials
desktop-only imports
server-only dependencies

Review every result and remove or properly handle inappropriate findings.

13. Produce the Final Report

Report:

Migration Summary

HackerGPT functionality:
- Ported
- Reimplemented
- Replaced
- Removed
- Not possible on Android

DroidCommand changes:
- Files created
- Files modified
- Files removed

Dependencies:
- Added
- Removed

Security:
- Permissions
- Storage boundaries and URI controls
- Network controls
- Secret handling
- Tool sandboxing
- Logging redaction
- Restrictions
- Confirmations

Testing:
- Tests
- Build
- APK

Known limitations:
- ...

Remaining TODO:
- ...

The target is:

DroidCommand AI
        +
Android-native HackerGPT capabilities
        +
Modular AI agent
        +
Controlled tool execution
        +
Secure Android architecture

Do not implement the product as a WebView around HackerGPT or as an Android APK dependent on an unmodified HackerGPT server. Build the functionality into DroidCommand AI wherever technically practical, test continuously, and preserve the existing application.
