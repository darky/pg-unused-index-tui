#!/usr/bin/env bash

npx nbb --classpath "src:test" test/indexes.test.cljs &&
npx nbb --classpath "src:test" test/connection.test.cljs
