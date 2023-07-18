#!/bin/bash
sed -e '/full documentation here/ { N; d; }' < README.md > docs/index.md

mkdocs gh-deploy

# Clean up.
rm docs/index.md \
