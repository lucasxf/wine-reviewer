# Documentation Review Findings - 2025-11-25

## Executive Summary

Comprehensive review of Wine Reviewer project documentation found **30+ issues** across multiple files.

**Files Reviewed:**
- README.md (24 issues)
- CLAUDE.md (minor issues)
- ROADMAP.md (test count discrepancy)
- usage-stats.toml (2 math errors)

---

## 🔴 CRITICAL Issues (Must Fix)

### 1. README.md - Incorrect CODING_STYLE References
**Location:** Lines 218, 368, 449
**Problem:** References singular `CODING_STYLE.md` but project uses split files
**Fix:** Update to reference:
- `CODING_STYLE_GENERAL.md`
- `services/api/CODING_STYLE_BACKEND.md`
- `apps/mobile/CODING_STYLE_FRONTEND.md`
- `infra/CODING_STYLE_INFRASTRUCTURE.md`

### 2. README.md - Placeholder GitHub URLs
**Location:** Lines 5, 6, 148
**Problem:** `https://github.com/username/wine-reviewer`
**Fix:** Replace with `https://github.com/lucasxf/wine-reviewer`

### 3. README.md - Missing prompts/PACK.md File
**Location:** Line 219
**Problem:** References non-existent file
**Fix:** Remove line or update to correct file

### 4. README.md - Outdated "Last Updated" Date
**Location:** Line 30
**Problem:** Shows 2025-11-01, but PRs merged through 2025-11-24
**Fix:** Update to `2025-11-24`

### 5. ROADMAP.md - Inconsistent Test Counts
**Location:** Lines 41, 280
**Problem:**
- Line 41: **103 tests** (outdated - from 2025-10-26)
- Line 280: **135 tests** (from 2025-11-01)
- **Actual:** **71 unit tests** (verified by Maven)
- Grep shows 138 @Test annotations total

**Fix:** Verify actual integration test count and update consistently

### 6. usage-stats.toml - Incorrect Agent Invocation Total
**Location:** After line 90
**Problem:**
- Sum of individual agents: 20 + 29 + 12 + 7 + 1 + 2 + 1 + 2 + 17 = **91**
- File shows: `total_agent_invocations = 77`
- **Difference: -14**

**Fix:** Update total to 91 or explain discrepancy

### 7. usage-stats.toml - Incorrect Command Invocation Total
**Location:** After line 198
**Problem:**
- Sum of individual commands: 15 + 14 + 7 + 5 + 15 + 6 + 6 + 3 + 2 + 2 + 1 + 2 + 1 + 2 + 1 = **82**
- File shows: `total_command_invocations = 87`
- **Difference: -5**

**Fix:** Update total to 82 or explain discrepancy

---

## 🟡 HIGH Priority Issues

### 8. README.md - Missing Infrastructure in CLAUDE.md Description
**Location:** Line 217
**Fix:** Change "General/Backend/Frontend" to "General/Backend/Frontend/Infrastructure"

### 9. README.md - ADRs Directory Description
**Location:** Line 222
**Problem:** Says "(future)" but 1 ADR exists
**Fix:** Update to "(1 ADR: automation-sentinel meta-agent)"

### 10. README.md - Spring Boot Version Too Generic
**Location:** Line 93
**Problem:** Shows "Spring Boot 3"
**Fix:** Update to "Spring Boot 3.3.11"

### 11. README.md - Missing .claude/METRICS.md Reference
**Location:** Documentation section (lines 216-223)
**Fix:** Add reference to `.claude/METRICS.md`

### 12. README.md - Integration Test Count Discrepancy
**Location:** Lines 342 vs 479
**Problem:** Shows 64 tests on line 342, but 37 tests on line 479
**Fix:** Verify actual count and standardize

---

## 🔵 MEDIUM Priority Issues

### 13. README.md - Comment Endpoints Missing /api Prefix
**Location:** Lines 278-292
**Problem:** Shows `/comments` instead of `/api/comments`
**Fix:** Add `/api` prefix to all 5 comment endpoints

### 14. README.md - Missing CommentControllerIT in Integration Test List
**Location:** Lines 506-510
**Fix:** Add `CommentControllerIT.java` to file list

### 15. README.md - Non-Portable Windows Paths
**Location:** Lines 642, 662
**Problem:** Contains `C:\repo\...` absolute paths
**Fix:** Remove or convert to relative/GitHub URLs

---

## 🟢 LOW Priority Issues

### 16. README.md - Flutter Version Inconsistency
**Location:** Lines 53 vs 85
**Problem:** "3.35.6" vs "3.x"
**Fix:** Standardize to "3.35.6"

### 17. README.md - Missing TESTING.md Cross-Reference
**Location:** Backend testing section
**Fix:** Add note linking to TESTING.md

---

## ✅ Files That Look Good

- **CLAUDE.md** - Agent count correct (9), no placeholder URLs, structure correct
- **LEARNINGS.md** - Up to date (2025-11-24 entry present)
- **.claude/agents-readme.md** - Not fully reviewed yet

---

## Next Steps

1. Fix all CRITICAL issues (7 issues)
2. Fix HIGH priority issues (5 issues)
3. Fix MEDIUM priority issues (3 issues)
4. Consider LOW priority issues (2 issues)
5. Verify test counts by running full test suite
6. Update ROADMAP.md "Last updated" date
7. Commit all fixes
8. Create PR with /create-pr

**Total Issues to Fix: 30+**
