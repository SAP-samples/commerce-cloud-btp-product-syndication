
npmenv:
	npm set init-author-name "Pawel Wolanski"
	npm set init-author-email "pawel.wolanski@sap.com"
	npm set init-author-url "https://github.com/mikolayek"
	npm set init-license "MIT"
	npm set init-version "1.0.0"

npminit: npmenv
# git init
	npx license $(shell npm get init-license) -o "$(shell npm get init-author-name)" > LICENSE
	npx gitignore node
	npx covgen "$(shell npm get init-author-email)"
	-npm init -y
	git add -A
	git commit -m "Initial commit"