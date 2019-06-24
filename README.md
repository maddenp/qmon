qmon
====
[![Build Status](https://travis-ci.org/maddenp/qmon.svg)](https://travis-ci.org/maddenp/qmon)

A simple PBS job monitor GUI

_qmon_ parses the XML output of the [PBS](http://en.wikipedia.org/wiki/Portable_Batch_System) _qstat_ command and separately displays jobs in Queued, Running and Completed states. Job information is updated every 10 seconds.
Displayed jobs are filtered by _user_, if specified, or otherwise by the _USER_ environment variable. The _qmon_ wrapper script looks for the Leiningen-generated _qmon.jar_ in the same directory as the script itself. It may be convenient to edit this script for your own use.

### Build

Install [Leiningen](http://leiningen.org/) if you don't have it, then:

```
lein uberjar
```

### Run

````
Usage: qmon [user]
````
