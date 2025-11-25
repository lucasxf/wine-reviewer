#!/usr/bin/env python3
"""
Validate mathematical consistency of usage-stats.toml metrics file.

This script ensures that:
1. total_agent_invocations = sum of all agent_usage.*.invocations
2. total_command_invocations = sum of all command_usage.*.invocations
3. combined_automation_invocations = total_agent_invocations + total_command_invocations

Usage:
    python .claude/scripts/validate-metrics.py

Exit codes:
    0 - All validations passed
    1 - Validation errors found
"""

import sys
from pathlib import Path
from typing import Tuple, List


def parse_toml_simple(content: str) -> dict:
    """
    Simple TOML parser for our specific use case (no dependencies needed).
    Only extracts the values we need: invocations counts and totals.
    """
    data = {'agents': {}, 'commands': {}, 'summary': {}}
    current_section = None

    for line in content.split('\n'):
        line = line.strip()

        # Skip comments and empty lines
        if not line or line.startswith('#'):
            continue

        # Section headers
        if line.startswith('[agent_usage.'):
            agent_name = line.replace('[agent_usage.', '').replace(']', '')
            current_section = ('agent', agent_name)
        elif line.startswith('[command_usage.'):
            command_name = line.replace('[command_usage.', '').replace(']', '')
            current_section = ('command', command_name)
        elif line.startswith('[summary]'):
            current_section = ('summary', None)
        elif line.startswith('['):
            current_section = None

        # Extract invocations values
        if current_section and '=' in line:
            key, value = line.split('=', 1)
            key = key.strip()
            value = value.strip()

            # Parse numeric values
            if key == 'invocations':
                try:
                    invocations = int(value)
                    section_type, section_name = current_section
                    if section_type == 'agent':
                        data['agents'][section_name] = invocations
                    elif section_type == 'command':
                        data['commands'][section_name] = invocations
                except ValueError:
                    pass

            # Parse summary totals
            if current_section[0] == 'summary':
                if key in ('total_agent_invocations', 'total_command_invocations',
                          'combined_automation_invocations'):
                    try:
                        data['summary'][key] = int(value)
                    except ValueError:
                        # If summary value is not an integer, skip it; validation will catch missing/invalid values.
                        pass

    return data


def validate_metrics(toml_path: Path) -> Tuple[bool, List[str]]:
    """
    Validate metrics file mathematical consistency.

    Returns:
        Tuple of (is_valid, error_messages)
    """
    errors = []

    # Read TOML file
    try:
        with open(toml_path, 'r', encoding='utf-8') as f:
            content = f.read()
    except FileNotFoundError:
        return False, [f"ERROR: Metrics file not found: {toml_path}"]
    except Exception as e:
        return False, [f"ERROR: Failed to read file: {e}"]

    # Parse TOML
    data = parse_toml_simple(content)

    # Extract summary totals
    summary = data['summary']
    total_agent_invocations = summary.get('total_agent_invocations')
    total_command_invocations = summary.get('total_command_invocations')
    combined_automation_invocations = summary.get('combined_automation_invocations')

    if total_agent_invocations is None:
        errors.append("ERROR: Missing summary.total_agent_invocations")
    if total_command_invocations is None:
        errors.append("ERROR: Missing summary.total_command_invocations")
    if combined_automation_invocations is None:
        errors.append("ERROR: Missing summary.combined_automation_invocations")

    if errors:
        return False, errors

    # Calculate actual sums from individual entries
    actual_agent_sum = sum(data['agents'].values())
    actual_command_sum = sum(data['commands'].values())
    actual_combined_sum = actual_agent_sum + actual_command_sum

    # Validation 1: Agent invocations total
    if actual_agent_sum != total_agent_invocations:
        errors.append(
            f"ERROR: Agent invocations mismatch\n"
            f"  Stated: total_agent_invocations = {total_agent_invocations}\n"
            f"  Actual: sum of all agent_usage.*.invocations = {actual_agent_sum}\n"
            f"  Difference: {total_agent_invocations - actual_agent_sum:+d}"
        )

    # Validation 2: Command invocations total
    if actual_command_sum != total_command_invocations:
        errors.append(
            f"ERROR: Command invocations mismatch\n"
            f"  Stated: total_command_invocations = {total_command_invocations}\n"
            f"  Actual: sum of all command_usage.*.invocations = {actual_command_sum}\n"
            f"  Difference: {total_command_invocations - actual_command_sum:+d}"
        )

    # Validation 3: Combined total
    if actual_combined_sum != combined_automation_invocations:
        errors.append(
            f"ERROR: Combined invocations mismatch\n"
            f"  Stated: combined_automation_invocations = {combined_automation_invocations}\n"
            f"  Actual: {actual_agent_sum} + {actual_command_sum} = {actual_combined_sum}\n"
            f"  Difference: {combined_automation_invocations - actual_combined_sum:+d}"
        )

    return len(errors) == 0, errors


def main():
    """Main entry point."""
    # Locate metrics file relative to script location
    script_dir = Path(__file__).parent
    repo_root = script_dir.parent.parent
    metrics_file = repo_root / '.claude' / 'metrics' / 'usage-stats.toml'

    print(f"Validating metrics file: {metrics_file}")
    print("-" * 60)

    is_valid, errors = validate_metrics(metrics_file)

    if is_valid:
        print("[OK] All validations PASSED")
        print("   - Agent invocations total matches sum")
        print("   - Command invocations total matches sum")
        print("   - Combined total matches agent + command sum")
        return 0
    else:
        print("[FAILED] Validation FAILED\n")
        for error in errors:
            print(error)
            print()
        print("Please fix the metrics file before committing.")
        return 1


if __name__ == '__main__':
    sys.exit(main())
