qmon
====

A simple PBS job monitor

### Notes

Usage: _qmon [user]_, where _user_ is the PBS name of the user whose jobs are to be monitored. If _user_ is omitted, its value is taken from the _USER_ environment variable.

_qmon_ parses the XML output of the [PBS](http://en.wikipedia.org/wiki/Portable_Batch_System) _qstat_ command and separately displays jobs in Queued, Running and Completed states. The refresh rate is controled via the _Thread/sleep_ call in _qmon.clj_; its default value is 10 seconds (10000 milliseconds). The _qmon_ wrapper script defines the location of the Clojure .jar file; its default value is _clojure.jar_ in the directory containing _qmon_.
