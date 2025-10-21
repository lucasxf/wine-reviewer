#!/bin/bash
# Pre-commit hook: Run tests in quiet mode before committing
# This ensures code quality and prevents broken commits

set -e

echo "🧪 Running tests before commit (quiet mode)..."

cd services/api && ./mvnw test -q

echo "✅ All tests passed! Proceeding with commit..."
