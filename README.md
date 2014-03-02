qmon
====

A simple PBS job monitor

### Notes

Usage: _qmon [user]_, where _user_ is the PBS name of the user whose jobs are to be monitored. If _user_ is omitted, its value is taken from the _USER_ environment variable. Click the window's text area to toggle active/sleep mode.

_qmon_ parses the XML output of the [PBS](http://en.wikipedia.org/wiki/Portable_Batch_System) _qstat_ command and separately displays jobs in Queued, Running and Completed states. The refresh rate is controled via the _Thread/sleep_ call in _qmon.clj_; its default value is 5 seconds (5000 milliseconds). The _qmon_ wrapper script defines the location of the Clojure .jar file; its default value is _clojure-1.4.0.jar_ in the directory containing _qmon_.

### License

The contents of this repository are released under the [Apache 2.0](http://www.apache.org/licenses/LICENSE-2.0) license. See the LICENSE file for details.
