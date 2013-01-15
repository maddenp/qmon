;; The contents of this repository are released under the Apache 2.0 license.
;; See the LICENSE file for details.

(ns qmon
  (:import [java.awt BorderLayout Font TextArea]
           [java.io StringBufferInputStream]
           [javax.swing JFrame])
  (:use [clojure.java.shell :only [sh]])
  (:use [clojure.xml :only [parse]])
  (:use [clojure.pprint]))

(def fmtstr "%-8s %-16s %-8s %-8s %-5s %-5s %-1s %-8s %-9s %s")

(declare tags tree xml xprocs xres xtime)

(defn contents [job]  (map #(:content %) job))
(defn d        [n]    (apply str (repeat n "-")))
(defn jobraw   [job]  (zipmap (tags job) (contents job)))
(defn jobs     []     (map :content (filter #(= (:tag %) :Job) (tree))))
(defn re       [user] (re-pattern (str user "@.*")))
(defn tags     [job]  (map #(:tag %) job))
(defn xattr    [j,k]  (first (k j)))
(defn xjobid   [j]    (xattr j :Job_Id))
(defn xml      []     (:out (sh "qstat" "-x")))
(defn xname    [j]    (xattr j :Job_Name))
(defn xnodes   [j]    (if (= "-" (xprocs j)) (xres j :nodect) "-"))
(defn xowner   [j]    (xattr j :Job_Owner))
(defn xqueue   [j]    (xattr j :queue))
(defn xprocs   [j]    (let [procs (xres j :procs) ] (if (nil? procs) "-" procs)))
(defn xres     [j,k]  (first (:content (first (filter #(= (:tag %) k) (:Resource_List j))))))
(defn xrtime   [j]    (xtime j :Resource_List))
(defn xstate   [j]    (xattr j :job_state))
(defn xtime    [j,k]  (first (:content (first (filter #(= (:tag %) :walltime) (k j))))))
(defn xutime   [j]    (let [t (xtime j :resources_used)] (if (nil? t) "-" t)))

(defn tree []
  (try (:content (parse (java.io.StringBufferInputStream. (xml))))
       (catch Exception e)))

(defn xpart [j]
  (let [p (first (:content (first (filter #(= (:tag %) :partition) (:Resource_List j)))))]
    (if (nil? p) "-" p)))

(defn jobinfo [job]
  (let [j (jobraw job)]
    {:jobid (xjobid j)
     :name  (xname  j)
     :nodes (xnodes j)
     :owner (xowner j)
     :part  (xpart  j)
     :procs (xprocs j)
     :queue (xqueue j)
     :rtime (xrtime j)
     :state (xstate j)
     :utime (xutime j)}))

(defn user []
  (if (first *command-line-args*)
    (first *command-line-args*)
    (get (System/getenv) "USER")))

(defn mine [u]
  (filter #(re-matches (re u) (:owner %)) (map #(jobinfo %) (jobs))))

(defn split [u]
  (let [m (mine u)]
    {:Q (filter #(= "Q" (:state %)) m)
     :R (filter #(= "R" (:state %)) m)
     :C (filter #(= "C" (:state %)) m)}))

(defn prjob [j]
  (format fmtstr 
          (re-find (re-matcher #"^\d+" (:jobid j)))
          (re-find (re-matcher #"^[^@]+" (:owner j)))
          (:rtime j)
          (:utime j)
          (:procs j)
          (:nodes j)
          (:state j)
          (:queue j)
          (:part j)
          (:name  j)))

(defn prhead [msg]
  (str (format "\n%s\n\n" msg)
       (format fmtstr "Job ID" "Owner" "Req'd" "Used" "Procs" "Nodes" "S" "Queue" "Partition" "Job Name\n")
       (format fmtstr (d 8) (d 16) (d 8) (d 8) (d 5) (d 5) (d 1) (d 8) (d 9) (d 32))
       "\n"))

(defn prjobs [key,msg,s]
  (let [jobs (key s)]
    (if (empty? jobs)
      nil
      (str (prhead msg) (reduce #(str %1 %2) (map #(str (prjob %) "\n") (key s)))))))

(defn show [u]
  (let [s (split u)]
    (str (format "\n%s\n" (.toString (java.util.Date.)))
         (prjobs :Q "QUEUED" s)
         (prjobs :R "RUNNING" s)
         (prjobs :C "COMPLETED" s))))

(let [fr (JFrame. (str "qmon " (user)))
      ta (TextArea. "\nLoading..." )]
  (do
    (.setFont ta (Font. "Monospaced" (Font/PLAIN) 12))
    (doto fr
      (.setSize 800 600)
      (.setLayout (BorderLayout.))
      (.add ta BorderLayout/CENTER)
      (.setDefaultCloseOperation JFrame/EXIT_ON_CLOSE)
      (.setVisible true))
    (doto ta
      (.setEditable false))
    (while true
      (.setText ta (show (user)))
      (Thread/sleep 5000))))
