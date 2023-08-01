#!/bin/bash
sed -e '/full documentation here/ { N; d; }' < README.md > docs/index.md

cp -f CHANGELOG.md docs/change_logs.md

mkdocs gh-deploy

# Clean up.
rm docs/index.md \
