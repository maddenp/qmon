qmon
====
[![Build Status](https://travis-ci.org/maddenp/qmon.svg)](https://travis-ci.org/maddenp/qmon)

A simple PBS job monitor GUI

###Build
Install [Leiningen](http://leiningen.org/) if you don't have it, then:
`lein uberjar`

###Run

````
Usage: qmon [-h/--help] [-a/--all] [filter-regex]
````

_qmon_ parses the XML output of the [PBS](http://en.wikipedia.org/wiki/Portable_Batch_System) _qstat_ command and separately displays jobs in Queued, Running and Completed states. Job information is updated every 10 seconds.

If the `--all` switch is used, _qmon_ will show jobs for all users. If _filter-regex_ is given instead, _qmon_ will show jobs whose owner name matches the supplied regular expression -- perhaps a simple string specifying a username, or a more complex regular expression (e.g. `'.*'` would give the same output as the `--all` switch). Otherwise, _qmon_ will use the value of the `USER` environment variable to filter the job list.

The _qmon_ wrapper script looks for the Leiningen-generated _qmon.jar_ in the same directory as the script itself. It may be convenient to edit this script for your own use.
