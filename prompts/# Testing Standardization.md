# Testing Standardization

## Goal

- The main goal of this session is to standardize this project's test suite (unit and integration tests).

## Directives and premisses

- This session will most likely be a big one, we'll need to optimize token usage throughout it
- Use `TodoWrite` to break the changes into tasks of a plan: we'll advance via milestone in case we need to stop the session mid-work and resume it later with `/resume-session`
- Feel free to use `tech-writer`, `session-optimizer`, `/directive`, and any other agents, commands, and tools you find necessary
- Open and the following URLs for better context. Confirm you have successfully read them before going further:
  - GIVEN/WHEN/THEN: https://martinfowler.com/bliki/GivenWhenThen.html
  - TDD: https://martinfowler.com/bliki/TestDrivenDevelopment.html
  - BDD: https://dannorth.net/blog/introducing-bdd/
- if any of the instructions on this prompt is a bad-choice, an anti-pattern or something like that, be quite explicit and warn me. Let me decide. 
- Don't follow directives blindly if they might lead to problems in the future

## Context

- Following the best practices for BDD (behavior-driven design), TDD (test-driven development), and overall testing, I want this projects tests to follow the given/when/then structure
- Some of the tests currently already follow that pattern, some others don't
- I DO NOT know whether said practice is a best practice for frontend testing, feel free to search the web to be sure of that

## New directives

- From now on, whenever Claude Code is creating/coding a new class, functionality, flow or feature, it should ALWAYS follow the TDD + BDD conventions
  - What does that mean?
    - Claude Code will create "empty" tests first that will always break - until the target of the tests is fully developed. (TDD)
    - Those new empty broken tests will validate the business rules and expected behaviors following the BDD format (give / when / then)
    - After the test suite is structured, Claude Code will proceed to develop that session's goal task or feature
    - During the development steps, the tests should be updated accordingly
    - The previously broken tests, should now be passing (at least partially)
    - once the feature is done, them we finish the tests, run them, and move on with the regular workflow
  - This is CRITICAL, a recap of learnings from previous sessions:
    - testing should be easy (or at the very least, not incredibly hard)
    -  if tests are constantly breaking even after their respective flows were developed, maybe there's room for improvement. 
    -  if tests keep breaking after we try to fix them, perhaps it's a symptom of poor design choices, anti-patterns, and code-smells
    - Because in the past we had Claude Code trying to fix broken tests when the problem was in fact with a poor design choice by either Claude or Lucas (myself), DO NOT try to adjust the broken tests BEFORE analysis the code that is subject to them, i. e., analyze the classes being tested to check for code smells instead of doing trial and error until the tests pass. This should improve overall code quality and simultaneously optimize token consumption

## What success looks like

I'll be happy if by the end of this session we:
- updated the required files, agents, commands, and documentation with the new directives and conventions
- successfully created new tests (if there are currently scenarios missing) following the new conventions and directives
- successfully updated the current tests that doesn't follow the new conventions and directives
- run all of them and make sure nothing is broken

