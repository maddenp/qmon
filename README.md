qmon
====

A simple PBS job monitor

### Notes

Usage: `qmon [user]`, where `user` is the PBS name of the user whose jobs are to be monitored. If `user` is omitted, its value is taken from the _USER_ environment variable.

_qmon_ parses the XML output of the [PBS](http://en.wikipedia.org/wiki/Portable_Batch_System) `qstat` command and separately displays jobs in Queued, Running and Completed states. The refresh rate is controled via the `Thread/sleep` call in _qmon.clj_; its default value is 5 seconds (5000 milliseconds).

The `qmon` script defines the location of the Clojure .jar file. By default, it looks for `clojure-1.4.0.jar` in the directory containing `qmon`.

### License

The contents of this repository are released under the [Apache 2.0](http://www.apache.org/licenses/LICENSE-2.0) license. See the LICENSE file for details.
