#!/bin/sh

git checkout gh-pages
git rebase master
lein marg
cp docs/uberdoc.html index.html
git add index.html
git commit -m "updated docs"
git push -f origin gh-pages
git checkout master

